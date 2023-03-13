from django.urls import path

from .views import (ProductListView,
                    ProductDetailSlugView,
                    CategoryListView,
                    ProductByCategoryView,
                    list_package,
                    package_detail,
                    )
app_name="amazon"

urlpatterns = [
    path('products/', ProductListView.as_view(), name='list'),
    #path('products-fbv/', product_list_view),
    #path('products/<int:pk>/', ProductDetailView.as_view()),
    path('products/<slug:slug>/', ProductDetailSlugView.as_view(), name='detail'),
    #path('products-fbv/<int:pk>/', product_detail_view),
    path('package/', list_package, name='list-package'),
    path('package/<int:packageID>/', package_detail, name='package_detail'),
    path('category/', CategoryListView.as_view(), name='category'),
    path('category/<str:categoryName>/', ProductByCategoryView.as_view(), name='productsByCategory'),
]
