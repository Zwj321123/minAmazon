# Generated by Django 4.0.4 on 2022-04-19 22:47

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('amazon', '0009_package_timestamp'),
    ]

    operations = [
        migrations.AlterField(
            model_name='package',
            name='status',
            field=models.CharField(default='packing', max_length=100),
        ),
    ]
