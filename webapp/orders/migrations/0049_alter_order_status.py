# Generated by Django 4.0.4 on 2022-04-24 15:17

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('orders', '0048_alter_order_status'),
    ]

    operations = [
        migrations.AlterField(
            model_name='order',
            name='status',
            field=models.CharField(choices=[('refunded', 'Redunded'), ('shipped', 'Shipped'), ('created', 'Created'), ('paid', 'Paid')], default='created', max_length=120),
        ),
    ]