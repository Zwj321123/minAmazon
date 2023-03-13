from django.contrib.auth.models import User
from django.db import models
from django.urls import reverse
import random
import os
from django.db.models.signals import pre_save, post_save
from webapp.utils import unique_slug_generator
from django.utils.timezone import now

def get_filename_ext(filename):
    base_name = os.path.basename(filename)
    name, ext = os.path.splitext(filename)
    return name, ext

def upload_image_path(instance, filename):
    #print(instance)
    #print(filename)
    new_filename = random.randint(1, 3910203912)
    name, ext = get_filename_ext(filename)
    final_filename = '{new_filename}{filename}{ext}'.format(new_filename=new_filename, filename=filename, ext=ext)
    return "amazon/{new_filename}{filename}/{final_filename}".format(
        new_filename=new_filename,
        filename=filename,
        final_filename=final_filename
    )

# Create your models here.
class WareHouse(models.Model):
    x = models.IntegerField(default=1)
    y = models.IntegerField(default=1)

    def __str__(self):
        return str(self.id) + ": <"+str(self.x) + ", " + str(self.y) + ">"

class Category(models.Model):
    categoryName = models.CharField(max_length=50, blank=False, null=False)
    img = models.ImageField(upload_to=upload_image_path, default="amazon/sample.png", null=True)

    def __str__(self):
        return self.categoryName

    def get_absolute_url(self):
        return reverse("amazon:productsByCategory", kwargs={"categoryName": self.categoryName})


class ProductManager(models.Manager):
    def get_by_category(self, categoryName):
        qs = self.get_queryset().filter(category__categoryName=categoryName)
        if qs != None:
            return qs
        return None

    def get_by_id(self, id):
        qs = self.get_queryset().filter(id = id)
        if qs.count() == 1:
            return qs.first()
        return None

class Product(models.Model):
    title = models.CharField(max_length=100, blank=False, null=False)
    slug = models.SlugField(blank=True, unique=True)
    description = models.TextField()
    price = models.DecimalField(default=0.99, blank=False, null=False, max_digits=19, decimal_places=2)
    img = models.ImageField(upload_to=upload_image_path, default="amazon/sample.png", null=True)
    category = models.ForeignKey(Category, on_delete=models.SET_NULL, null=True)
    whnum = models.ForeignKey(WareHouse, on_delete=models.SET_NULL, null=True)
    quality = models.IntegerField(default=0)
    objects = ProductManager()

    def get_absolute_url(self):
        return reverse("amazon:detail", kwargs={"slug": self.slug})

    def __str__(self):
        return self.title

#signify before saving
def product_pre_save_receiver(sender, instance, *args, **kwargs):
    if not instance.slug:
        instance.slug = unique_slug_generator(instance)

pre_save.connect(product_pre_save_receiver, sender=Product)

class Package(models.Model):
    owner = models.ForeignKey(User, on_delete=models.CASCADE, related_name="packages")
    #destination
    dest_x = models.IntegerField(default=10)
    dest_y = models.IntegerField(default=10)
    status = models.CharField(max_length=100, default="packing")
    #status: packing, packed, loading, loaded, delivering, delivered
    warehouse = models.IntegerField(default=1)
    timestamp = models.DateTimeField(default=now)

    def __str__(self):
        return str(self.id) + ": " + str(self.owner)

#user porduct = prudct_id + count
class UserProduct(models.Model):
    productID = models.ForeignKey(Product, on_delete=models.SET_NULL, null=True)
    count = models.IntegerField(default=1)
    #package id
    package = models.ForeignKey(Package, on_delete=models.CASCADE, related_name="userProducts", null=True, blank=True)
    def __str__(self):
        return str(self.id) + ": " + str(self.productID) + ", " + str(self.package)