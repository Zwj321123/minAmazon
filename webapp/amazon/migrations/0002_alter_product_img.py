# Generated by Django 4.0.4 on 2022-04-17 03:42

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('amazon', '0001_initial'),
    ]

    operations = [
        migrations.AlterField(
            model_name='product',
            name='img',
            field=models.ImageField(default='/static/img/sample.png', null=True, upload_to='blah'),
        ),
    ]