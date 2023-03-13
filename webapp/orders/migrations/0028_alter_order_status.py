# Generated by Django 4.0.4 on 2022-04-24 13:03

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('orders', '0027_alter_order_status'),
    ]

    operations = [
        migrations.AlterField(
            model_name='order',
            name='status',
            field=models.CharField(choices=[('paid', 'Paid'), ('created', 'Created'), ('shipped', 'Shipped'), ('refunded', 'Redunded')], default='created', max_length=120),
        ),
    ]
