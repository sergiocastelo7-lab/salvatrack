"""
URL configuration for salvatrack_api project.

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/6.0/topics/http/urls/
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
from django.contrib import admin
from django.urls import path
from usuarios import views

urlpatterns = [
    path('admin/', admin.site.urls),

    # Auth
    path('api/register/', views.register, name='register'),
    path('api/login/', views.login, name='login'),
    path('api/delete-account/', views.delete_account, name='delete_account'),

    # Favoritos
    path('api/favoritos/', views.get_favoritos, name='get_favoritos'),
    path('api/favoritos/toggle/', views.toggle_favorito, name='toggle_favorito'),

    # Historial
    path('api/historial/add/', views.add_busqueda, name='add_busqueda'),
    path('api/historial/remove/', views.remove_busqueda, name='remove_busqueda'),
    path('api/historial/clear/', views.clear_historial, name='clear_historial'),

    # Crono
    path('api/crono/guardar/', views.guardar_tiempo, name='guardar_tiempo'),
    path('api/crono/tiempos/', views.get_tiempos, name='get_tiempos'),
]
