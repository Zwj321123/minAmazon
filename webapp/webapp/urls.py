"""webapp URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/4.0/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.conf import settings
from django.conf.urls.static import static

from django.contrib import admin
from django.contrib.auth.views import LogoutView
from django.urls import path, include
from django.views.generic import TemplateView
from django.contrib.auth import views as auth_views

from accounts.views import register_page
from .views import home_page, about_page, contact_page
from carts.views import success

urlpatterns = [
    path('', home_page, name='home'),
    path('bootstrap/', TemplateView.as_view(template_name='bootstrap/example.html')),
    path('about/', about_page, name='about'),
    path('contact/', contact_page, name='contact'),
    path('login/', auth_views.LoginView.as_view(template_name='accounts/login.html'), name='login'),
    path('logout/', LogoutView.as_view(), name='logout'),
    path('register/', register_page, name='register'),
    path('', include('amazon.urls', namespace='amazon')),
    path('search/', include('search.urls', namespace='search')),
    path('cart/', include('carts.urls', namespace='carts')),
    path('success/', success, name='success'),
    path('admin/', admin.site.urls),
]

#<slug:slug>
if settings.DEBUG:
    urlpatterns = urlpatterns + static(settings.STATIC_URL, document_root=settings.STATIC_ROOT)
    urlpatterns = urlpatterns + static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
