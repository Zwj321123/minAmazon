from django.shortcuts import render, redirect
from django.contrib.auth.decorators import login_required
from amazon.models import Product, UserProduct, Package
from accounts.models import GuestEmail
from billing.models import BillingProfile
from .models import Cart, CartProduct
from orders.models import Order
from accounts.forms import LoginForm, GuestForm
from .utils import send

# Create your views here.
def cart_create(user=None):
    cart_obj = Cart.objects.create(user=None)
    print('create new cart')
    return cart_obj

def cart_home(request):
    cart_obj, new_obj = Cart.objects.new_or_get(request)
    cartProducts = CartProduct.objects.filter(cart=cart_obj)
    total = 0
    for x in cartProducts:
        unit = x.product.price * x.count
        total += unit
    print("total ", total)
    if cart_obj.subtotal != total:
        cart_obj.subtotal = format(total, '.2f')
        cart_obj.save()
    return render(request, "carts/home.html", {"cart": cart_obj, "cartProducts": cartProducts})

def cart_update(request):
    #print(request.POST)
    product_id = request.POST.get('product')
    qty = request.POST.get('qty')
    print('qty')
    print(qty)
    if product_id is not None:
        try:
            product_obj = Product.objects.get(id=product_id)
        except Product.DoesNotExist:
            print("Show message to user, product is gone")
            return redirect("carts:home")
        cart_obj, new_obj = Cart.objects.new_or_get(request)
        #remove
        if product_obj in cart_obj.products.all():
            cart_obj.products.remove(product_obj)
            cartProduct_qs = CartProduct.objects.filter(cart=cart_obj)
            for cartProduct in cartProduct_qs:
                if cartProduct.product.id == product_obj.id:
                    cartProduct.delete()
        #add
        else:
            #many to many field
            cart_obj.products.add(product_obj) #  cart_obj.products.add(product_id)
            if qty:
                cartProduct = CartProduct.objects.create(cart=cart_obj, product=product_obj, count=qty)
            else:
                cartProduct = CartProduct.objects.create(cart=cart_obj, product=product_obj, count=1)
            cartProduct.save()
        request.session['cart_items'] = cart_obj.products.count()
    return redirect("carts:home")

@login_required
def checkout_home(request):
    cart_obj, cart_created = Cart.objects.new_or_get(request)
    order_obj = None
    if cart_created or cart_obj.products.count() == 0:
        return redirect("carts:home")
    user = request.user
    billing_profile = None
    login_form = LoginForm()
    if user.is_authenticated:
        billing_profile, billing_profile_created = BillingProfile.objects.get_or_create(user=user, email=user.email)
    order_qs = Order.objects.filter(cart=cart_obj, active=True)
    if order_qs.exists():
        order_obj = order_qs.first()
    else:
        order_obj = Order.objects.create(billing_profile=billing_profile, cart=cart_obj)

    if request.method == "POST":
        x = int(request.POST["x"])
        y = int(request.POST["y"])
        #each package has one product
        packageID_lst = []
        for product in cart_obj.products.all():
            productID = product.id
            warehouseID = product.whnum.id
            curr_package = Package.objects.create(owner=user,
                                                dest_x=x,
                                                dest_y=y,
                                                warehouse=warehouseID
                                                )
            curr_package.save()
            packageID_lst.append(curr_package.pk)
            cartProduct = CartProduct.objects.filter(cart=cart_obj, product=product).first()
            if cartProduct:
                userProduct = UserProduct.objects.create(productID=product,
                                                         count=cartProduct.count,
                                                         package=curr_package
                                                         )
            else:
                userProduct = UserProduct.objects.create(productID=product,
                                                         count=1,
                                                         package=curr_package
                                                         )
            userProduct.save()
        #delete orders and update cart
        order_obj.delete()
        cart_obj.delete()
        #send to backend
        send(packageID_lst)

        request.session['cart_items'] = 0
        return redirect("success")
    context = {
        "object": order_obj,
        "billing_profile": billing_profile,
        "login_form": login_form
    }
    return render(request, "carts/checkout.html", context)

def success(request):
    return render(request, "carts/success.html", {})


# for product in cart_obj.products.all():
#     productID = product.id
#     warehouseID = product.whnum.id
#     if warehouseID in whDict.keys():
#         whDict[warehouseID].append(product)
#     else:
#         whDict[warehouseID] = [product]
# # create package
# print(whDict)
# packageID_lst = []
# for key, value_lst in whDict.items():
#     curr_package = Package.objects.create(owner=user,
#                                           dest_x=x,
#                                           dest_y=y,
#                                           warehouse=key
#                                           )
#     curr_package.save()
#     # add userProduct
#     for i in value_lst:
#         userProduct = UserProduct.objects.create(productID=i,
#                                                  count=1,
#                                                  package=curr_package
#                                                  )
#         userProduct.save()
#         cart_obj.products.remove(i)
#     id = curr_package.pk
#     packageID_lst.append(id)
