# Generated by Django 4.0.4 on 2022-04-17 15:12

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('amazon', '0004_product_slug'),
    ]

    operations = [
        migrations.AlterField(
            model_name='product',
            name='slug',
            field=models.SlugField(default='abc'),
        ),
    ]
