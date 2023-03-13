from django.contrib import admin

from .models import *
# Register your models here.

class ProductAdmin(admin.ModelAdmin):
    list_display = ("title", "slug", "price",)


admin.site.register(Product, ProductAdmin)
admin.site.register(Package)
admin.site.register(UserProduct)
admin.site.register(WareHouse)
admin.site.register(Category)
