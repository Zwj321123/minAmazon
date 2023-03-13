# Generated by Django 4.0.4 on 2023-03-11 21:30

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('orders', '0119_alter_order_status'),
    ]

    operations = [
        migrations.AlterField(
            model_name='order',
            name='status',
            field=models.CharField(choices=[('paid', 'Paid'), ('refunded', 'Redunded'), ('shipped', 'Shipped'), ('created', 'Created')], default='created', max_length=120),
        ),
    ]