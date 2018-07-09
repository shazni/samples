# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('openiddemoproject', '0001_initial'),
    ]

    operations = [
        migrations.CreateModel(
            name='CurrentSession',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('currentPINNumber', models.CharField(max_length=10)),
                ('accessToken', models.CharField(max_length=40)),
                ('lastAccessTime', models.IntegerField()),
            ],
        ),
    ]
