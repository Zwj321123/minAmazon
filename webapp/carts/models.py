import math
from django.contrib.auth.models import User
from django.db import models
from django.db.models.signals import pre_save, m2m_changed

from amazon.models import Product


# Create your models here.
class CartManager(models.Manager):
    def new_or_get(self, request):
        cart_id = request.session.get("cart_id", None)
        qs = self.get_queryset().filter(id=cart_id)
        if qs.count() == 1:
            new_obj = False
            #print('Cart ID exists')
            cart_obj = qs.first()
            if request.user.is_authenticated and cart_obj.user is None:
                cart_obj.user = request.user
                cart_obj.save()
        else:
            cart_obj = Cart.objects.new(user=request.user)
            new_obj = True
            request.session['cart_id'] = cart_obj.id
        return cart_obj, new_obj

    def new(self, user=None):
        user_obj = None
        if user is not None:
            if user.is_authenticated:
                user_obj = user
        return self.model.objects.create(user=user_obj)

class Cart(models.Model):
    user = models.ForeignKey(User, null=True, blank=True, on_delete=models.SET_NULL)
    products = models.ManyToManyField(Product, blank=True)
    subtotal = models.DecimalField(default=0.00, max_digits=19, decimal_places=2)
    total = models.DecimalField(default=0.00, max_digits=19, decimal_places=2)
    updated = models.DateTimeField(auto_now=True)
    timestamp = models.DateTimeField(auto_now_add=True)

    objects = CartManager()

    def __str__(self):
        return str(self.id)

class CartProduct(models.Model):
    cart = models.ForeignKey(Cart, null=True, blank=True, on_delete=models.CASCADE)
    product = models.ForeignKey(Product, blank=True, on_delete=models.CASCADE)
    count = models.IntegerField(default=0)

    def __str__(self):
        return str(self.cart)+", "+str(self.product)

#action: pre_remove, post_add. pre_add...
# def m2m_changed_cart_receiver(sender, instance, action, *args, **kwargs):
#     #update total only when the following actions occur
#     if action == 'post_add' or action == 'post_remove' or action == 'post_clear':
#         products = instance.products.all()
#         cartProducts = CartProduct.objects.filter(cart=instance)
#         total = 0
#         for x in cartProducts:
#             unit = x.product.price * x.count
#             total += unit
#         if instance.subtotal != total:
#             instance.subtotal = total
#             instance.save()
# #called when the obj is being saved
# #sender = many2many field. through
# m2m_changed.connect(m2m_changed_cart_receiver, sender=Cart.products.through)

# def pre_save_changed_cart_receiver(sender, instance, *args, **kwargs):
#     cartProducts = CartProduct.objects.filter(cart=instance)
#     print(cartProducts)
#     total = 0
#     for x in cartProducts:
#         unit = x.product.price
#         total += unit
#     print("total ", total)
#     if instance.subtotal != total:
#         instance.subtotal = total
#         instance.save()
#
# pre_save.connect(pre_save_changed_cart_receiver, sender=Cart)


def pre_save_cart_receiver(sender, instance, *args, **kwargs):
    if instance.subtotal == 0:
        instance.total = 0.00
    else:
        #include tax (12%)
        new_total = float(instance.subtotal) * float(1.12)
        instance.total = format(new_total, '.2f')

pre_save.connect(pre_save_cart_receiver, sender=Cart)