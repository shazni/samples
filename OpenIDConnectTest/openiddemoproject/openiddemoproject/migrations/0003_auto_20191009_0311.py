# Generated by Django 2.1.5 on 2019-10-09 03:11

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('openiddemoproject', '0002_currentsession'),
    ]

    operations = [
        migrations.AddField(
            model_name='currentsession',
            name='id_token',
            field=models.TextField(default=0),
            preserve_default=False,
        ),
        migrations.AddField(
            model_name='currentsession',
            name='refreshToken',
            field=models.CharField(default=1, max_length=40),
            preserve_default=False,
        ),
    ]
