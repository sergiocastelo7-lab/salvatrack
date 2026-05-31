# SalvaTrack — Backend Django

API REST para la gestión de usuarios, favoritos, historial de búsqueda y tiempos de cronómetro de la app **SalvaTrack** (cronómetro y gestión de resultados de Salvamento Acuático).

## Tecnologías

- Python 3.11+
- Django 4.2+
- SQLite (base de datos local)
- django-cors-headers

## Instalación

```bash
pip install django django-cors-headers
python manage.py makemigrations
python manage.py migrate
python manage.py createsuperuser
python manage.py runserver
```

## Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/register/` | Crear cuenta |
| POST | `/api/login/` | Iniciar sesión |
| DELETE | `/api/delete-account/` | Eliminar cuenta |
| GET | `/api/favoritos/` | Obtener favoritos |
| POST | `/api/favoritos/toggle/` | Añadir/quitar favorito |
| POST | `/api/historial/add/` | Añadir búsqueda |
| DELETE | `/api/historial/remove/` | Eliminar búsqueda |
| DELETE | `/api/historial/clear/` | Borrar historial |
| POST | `/api/crono/guardar/` | Guardar tiempo |
| GET | `/api/crono/tiempos/?limit=N` | Obtener tiempos |
| PUT | `/api/crono/editar/<id>/` | Editar tiempo |
| DELETE | `/api/crono/eliminar/<id>/` | Eliminar tiempo |

## Autenticación

Todas las peticiones (excepto `/register/` y `/login/`) requieren el header:
```
Authorization: Token <token>
```

El token se obtiene al hacer login o registro.
