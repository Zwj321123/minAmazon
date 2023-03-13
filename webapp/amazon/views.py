from django.http import Http404
from django.views.generic import ListView, DetailView
from django.shortcuts import render, get_object_or_404
from django.contrib.auth.decorators import login_required

from carts.models import Cart
from .models import Product, Category, Package, UserProduct
# Create your views here.

class ProductListView(ListView):
    template_name = "amazon/product_list.html"

    #get context for any given query set
    # def get_context_data(self, *args, **kwargs):
    #     context = super(ProductListView, self).get_context_data(*args, **kwargs)
    #     print(context)
    #     return context
    def get_queryset(self):
        return Product.objects.all()


# def product_list_view(request):
#     queryset = Product.objects.all()
#     context = {
#         'object_list': queryset
#     }
#     return render(request, "amazon/product_list.html", context)

class ProductDetailSlugView(DetailView):
    queryset = Product.objects.all()
    template_name = "amazon/detail.html"

    def get_context_data(self, *args, **kwargs):
        context = super(ProductDetailSlugView, self).get_context_data(*args, **kwargs)
        cart_obj, new_obj = Cart.objects.new_or_get(self.request)
        context['cart'] = cart_obj
        return context

    def get_object(self, *args, **kwargs):
        requests = self.request
        slug = self.kwargs.get('slug')
        try:
            instance = Product.objects.get(slug=slug)
        except Product.DoesNotExist:
            raise Http404("Not found...")
        except Product.MultipleObjectsReturned:
            qs = Product.objects.filter(slug=slug)
            instance = qs.first()
        except:
            raise Http404("Uhhmmm")
        return instance

# class ProductDetailView(DetailView):
#     template_name = "amazon/detail.html"
#
#     #get context for any given query set
#     def get_context_data(self, *args, **kwargs):
#         context = super(ProductDetailView, self).get_context_data(*args, **kwargs)
#         return context
#
#     def get_object(self, *args, **kwargs):
#         requests = self.request
#         pk = self.kwargs.get('pk')
#         instance = Product.objects.get_by_id(pk)
#         if instance is None:
#             raise Http404("Product doesn't exist")
#         return instance


class CategoryListView(ListView):
    template_name = "amazon/category_list.html"

    def get_queryset(self):
        return Category.objects.all()


class ProductByCategoryView(ListView):
    template_name = "amazon/product_list.html"
    def get_queryset(self, *args, **kwargs):
        categoryName = self.kwargs.get('categoryName')
        return Product.objects.get_by_category(categoryName)

def product_by_Category_view(request, categoryName=None, *args, **kwargs):
    qs = Product.objects.filter(category__categoryName=categoryName)
    print(qs)
    if qs.count() < 1:
        raise Http404("Product doesn't exist")
    context = {
        'object_list': qs
    }
    return render(request, "amazon/product_list.html", context)

@login_required
def list_package(request):
    package_list = Package.objects.filter(owner=request.user).order_by('timestamp').all()
    user_product_list = UserProduct.objects.all()
    context = {
        'package_list': package_list,
        'user_product_list': user_product_list,
    }
    return render(request, 'amazon/list_package.html', context)

@login_required
def list_user_products(request):
    package_list = Package.objects.filter(owner=request.user).order_by('timestamp').all()
    context = {
        'package_list': package_list,
    }
    return render(request, 'amazon/list_package.html', context)

@login_required
def package_detail(request, packageID=None, *args, **kwargs):
    qs = UserProduct.objects.filter(package__id=packageID)
    print(qs)
    package = Package.objects.filter(id=packageID).first()
    status = package.status
    if qs.count() < 1:
        raise Http404("Product doesn't exist")
    context = {
        'object_list': qs,
        'package_id': packageID,
        'status': status,
    }
    return render(request, "amazon/user_product_list.html", context)