"""openiddemoproject URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/1.11/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  url(r'^$', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  url(r'^$', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.conf.urls import url, include
    2. Add a URL to urlpatterns:  url(r'^blog/', include('blog.urls'))
"""
from django.conf.urls import url
from django.contrib import admin
from django.conf import settings

from openiddemoproject import views

urlpatterns = [
    url(r'^' + settings.CONTEXT_PREFIX + '$', views.index, name='index'),
    url(r'^' + settings.CONTEXT_PREFIX + 'promptPIN/?$', views.promptPIN, name='promptPIN'),
    url(r'^' + settings.CONTEXT_PREFIX + 'oauth2client/?$', views.oauth2client, name='oauth2client'),
    url(r'^' + settings.CONTEXT_PREFIX + 'validatePIN/?$', views.validatePIN, name='validatePIN'),
    url(r'^' + settings.CONTEXT_PREFIX + 'patients/$', views.patients, name='patients'),
    url(r'^' + settings.CONTEXT_PREFIX + 'getPatients/$', views.getPatients),   # Only POST
]
