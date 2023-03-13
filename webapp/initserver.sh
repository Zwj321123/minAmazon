#!/bin/bash
chmod 777 ./amazon/static/img
python manage.py makemigrations
python manage.py migrate



res="$?"
while [ "$res" != "0" ]
do
    sleep 3;
    python manage.py migrate
    res="$?"
done


echo "from amazon.models import *;
from carts.models import Cart;
from django.contrib.auth import get_user_model;
Cart.objects.all().delete();
UserProduct.objects.all().delete();
Package.objects.all().delete();
Product.objects.all().delete();
User = get_user_model();
User.objects.all().delete();
User.objects.create_superuser('admin', 'admin@myproject.com', 'password')" | python manage.py shell

echo "from amazon.models import WareHouse;
WareHouse.objects.all().delete();
wh_1 = WareHouse(id=1, x=1, y=1);
wh_1.save();
wh_2 = WareHouse(id=2, x=2, y=2);
wh_2.save();
wh_3 = WareHouse(id=3, x=3, y=3);
wh_3.save();" | python manage.py shell

echo "from amazon.models import Category, Product, WareHouse;
Category.objects.all().delete();
cg_1 = Category(categoryName='Food', img='amazon/food.png');
cg_1.save();
cg_2 = Category(categoryName='Electronic Products', img='amazon/electronic_product.jpg');
cg_2.save();
cg_3 = Category(categoryName='Furniture', img='amazon/furniture.jpg');
cg_3.save();
Product.objects.all().delete();
wh_1 = WareHouse.objects.filter(id=1).first()
wh_2 = WareHouse.objects.filter(id=2).first()
wh_3 = WareHouse.objects.filter(id=3).first()
p_1 = Product(title='mac book', description='this is an awesome mac book', price = 1999.99, img='amazon/mac_book.jpg', category=cg_2, whnum = wh_1, quality=0);
p_1.save();
p_2 = Product(title='ipad', description='this is an awesome ipad', price = 599.99, img='amazon/ipad.jpg', category=cg_2, whnum = wh_1, quality=0);
p_2.save();
p_3 = Product(title='iphone', description='this is an awesome iphone', price = 999.99,img='amazon/iphone.jpg', category=cg_2, whnum = wh_1, quality=0);
p_3.save();
p_4 = Product(title='banana', description='our bananas are delicious', price = 2.99, img='amazon/banana.jpg', category=cg_1, whnum = wh_2, quality=0);
p_4.save();
p_5 = Product(title='orange', description='our oranges are sweet', price = 4.99, img='amazon/orange.jpg', category=cg_1, whnum = wh_2, quality=0);
p_5.save();
p_6 = Product(title='apple', description='our apples are 100% organic', price = 3.99, img='amazon/apple.jpg', category=cg_1, whnum = wh_2, quality=0);
p_6.save();
p_7 = Product(title='bed', description='Bamboo Charcoal Gel Infused Bed in a Box with Fabric Cover, Made in USA', price = 256.99, img='amazon/bed.jpg', category=cg_3, whnum = wh_3, quality=0);
p_7.save();
p_8 = Product(title='chair', description='Ergonomic Office Chair with Adjustable Sponge lumbar Support, Comfortable Thick Cushion High Back Desk Chair with Adjustable Headrest and PU Armrests', price = 349.99, img='amazon/chair.jpg', category=cg_3, whnum = wh_3, quality=0);
p_8.save();
p_9 = Product(title='desk', description='63 inch Super Large Computer Writing Desk', price = 118.99, img='amazon/desk.jpg', category=cg_3, whnum = wh_3, quality=0);
p_9.save();"| python manage.py shell