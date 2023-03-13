# Generated by Django 4.0.4 on 2022-04-24 20:13

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('orders', '0073_alter_order_status'),
    ]

    operations = [
        migrations.AlterField(
            model_name='order',
            name='status',
            field=models.CharField(choices=[('shipped', 'Shipped'), ('created', 'Created'), ('refunded', 'Redunded'), ('paid', 'Paid')], default='created', max_length=120),
        ),
    ]