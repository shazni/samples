from django.conf import settings

def commom_processor(request):
    return {
        'context_prefix' : settings.CONTEXT_PREFIX
    }