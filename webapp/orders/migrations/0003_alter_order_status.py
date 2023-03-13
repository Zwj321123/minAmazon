# Generated by Django 4.0.4 on 2022-04-18 22:16

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('orders', '0002_alter_order_status'),
    ]

    operations = [
        migrations.AlterField(
            model_name='order',
            name='status',
            field=models.CharField(choices=[('refunded', 'Redunded'), ('shipped', 'Shipped'), ('paid', 'Paid'), ('created', 'Created')], default='created', max_length=120),
        ),
    ]
