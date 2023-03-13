from django.urls import path

from .views import (SearchProducView,
                    )
app_name="search"

urlpatterns = [
    path('', SearchProducView.as_view(), name='query'),
]