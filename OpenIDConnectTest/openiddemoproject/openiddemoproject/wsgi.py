"""
WSGI config for openiddemoproject project.

It exposes the WSGI callable as a module-level variable named ``application``.

For more information on this file, see
https://docs.djangoproject.com/en/1.11/howto/deployment/wsgi/
"""

import os

from django.core.wsgi import get_wsgi_application

from dj_static import Cling

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "openiddemoproject.settings")

#application = get_wsgi_application()

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
STATIC_BASE_PATH = os.path.join(BASE_DIR, 'openiddemoproject')

application = Cling(get_wsgi_application(), STATIC_BASE_PATH)
