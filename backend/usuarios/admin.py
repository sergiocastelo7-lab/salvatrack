from django.contrib import admin
from .models import UserProfile, Favorito, BusquedaReciente, CronoTiempo


@admin.register(UserProfile)
class UserProfileAdmin(admin.ModelAdmin):
    list_display = ['user', 'is_admin', 'session_token']
    list_editable = ['is_admin']
    search_fields = ['user__username']


@admin.register(Favorito)
class FavoritoAdmin(admin.ModelAdmin):
    list_display = ['user', 'nombre', 'club', 'categoria']
    search_fields = ['user__username', 'nombre']


@admin.register(BusquedaReciente)
class BusquedaRecienteAdmin(admin.ModelAdmin):
    list_display = ['user', 'query', 'fecha']
    search_fields = ['user__username', 'query']


@admin.register(CronoTiempo)
class CronoTiempoAdmin(admin.ModelAdmin):
    list_display = ['user', 'prueba', 'piscina', 'modo', 'tiempo_ms', 'fecha']
    search_fields = ['user__username', 'prueba']