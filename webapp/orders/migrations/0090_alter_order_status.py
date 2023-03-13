# Generated by Django 4.0.4 on 2022-04-25 00:29

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('orders', '0089_alter_order_status'),
    ]

    operations = [
        migrations.AlterField(
            model_name='order',
            name='status',
            field=models.CharField(choices=[('created', 'Created'), ('paid', 'Paid'), ('shipped', 'Shipped'), ('refunded', 'Redunded')], default='created', max_length=120),
        ),
    ]
