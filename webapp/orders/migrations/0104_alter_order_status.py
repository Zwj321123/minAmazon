# Generated by Django 4.0.4 on 2022-04-26 15:50

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('orders', '0103_alter_order_status'),
    ]

    operations = [
        migrations.AlterField(
            model_name='order',
            name='status',
            field=models.CharField(choices=[('created', 'Created'), ('shipped', 'Shipped'), ('refunded', 'Redunded'), ('paid', 'Paid')], default='created', max_length=120),
        ),
    ]
