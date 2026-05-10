from django.db import models
from django.contrib.auth.models import User


class UserProfile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE, related_name='profile')
    is_admin = models.BooleanField(default=False)
    session_token = models.CharField(max_length=200, unique=True, null=True, blank=True)

    def __str__(self):
        return f"{self.user.username} ({'admin' if self.is_admin else 'user'})"


class Favorito(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='favoritos')
    nombre = models.CharField(max_length=200)
    ano_nacimiento = models.IntegerField(null=True, blank=True)
    club = models.CharField(max_length=200, blank=True)
    categoria = models.CharField(max_length=100, blank=True)
    genero = models.CharField(max_length=20, blank=True)

    class Meta:
        unique_together = ['user', 'nombre']

    def __str__(self):
        return f"{self.user.username} → {self.nombre}"


class BusquedaReciente(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='busquedas')
    query = models.CharField(max_length=200)
    fecha = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ['-fecha']
        unique_together = ['user', 'query']

    def __str__(self):
        return f"{self.user.username}: {self.query}"