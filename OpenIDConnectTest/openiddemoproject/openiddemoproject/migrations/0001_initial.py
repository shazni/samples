# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
    ]

    operations = [
        migrations.CreateModel(
            name='Patient',
            fields=[
                ('firstName', models.CharField(max_length=20)),
                ('lastName', models.CharField(max_length=20, null=True, blank=True)),
                ('patientID', models.CharField(max_length=20, serialize=False, primary_key=True)),
                ('address', models.CharField(max_length=80, null=True, blank=True)),
                ('dateOfBirth', models.DateField()),
                ('gender', models.BooleanField(default=True)),
                ('contactNumber', models.CharField(max_length=20, null=True, blank=True)),
            ],
            options={
                'ordering': ['firstName'],
            },
        ),
    ]
