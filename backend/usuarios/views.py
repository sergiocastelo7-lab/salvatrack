import json
import uuid
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.contrib.auth.models import User
from django.contrib.auth.hashers import make_password, check_password
from .models import UserProfile, Favorito, BusquedaReciente, CronoTiempo

MAX_HISTORIAL = 15


def get_user_from_token(request):
    auth = request.headers.get('Authorization', '')
    if not auth.startswith('Token '):
        return None
    token = auth[6:].strip()
    try:
        profile = UserProfile.objects.get(session_token=token)
        return profile.user
    except UserProfile.DoesNotExist:
        return None


def json_body(request):
    try:
        return json.loads(request.body)
    except Exception:
        return {}


def error(msg, status=400):
    return JsonResponse({'error': msg}, status=status)


def perfil_data(user):
    try:
        profile = user.profile
        is_admin = profile.is_admin
        token = profile.session_token
    except UserProfile.DoesNotExist:
        is_admin = False
        token = None
    favoritos = list(user.favoritos.values('nombre', 'ano_nacimiento', 'club', 'categoria', 'genero'))
    historial = list(user.busquedas.values_list('query', flat=True)[:MAX_HISTORIAL])
    return {
        'token': token,
        'nombre': user.username,
        'is_admin': is_admin,
        'favoritos': favoritos,
        'historial': historial,
    }


@csrf_exempt
def register(request):
    if request.method != 'POST':
        return error('Método no permitido', 405)
    data = json_body(request)
    nombre = data.get('nombre', '').strip()
    password = data.get('password', '').strip()
    if not nombre or not password:
        return error('El nombre y la contraseña son obligatorios.')
    if len(password) < 4:
        return error('La contraseña debe tener al menos 4 caracteres.')
    if User.objects.filter(username=nombre).exists():
        return error('Ya existe una cuenta con ese nombre.', 409)
    user = User.objects.create(username=nombre, password=make_password(password))
    token = str(uuid.uuid4())
    UserProfile.objects.create(user=user, session_token=token)
    return JsonResponse(perfil_data(user), status=201)


@csrf_exempt
def login(request):
    if request.method != 'POST':
        return error('Método no permitido', 405)
    data = json_body(request)
    nombre = data.get('nombre', '').strip()
    password = data.get('password', '').strip()
    if not nombre or not password:
        return error('El nombre y la contraseña son obligatorios.')
    try:
        user = User.objects.get(username=nombre)
    except User.DoesNotExist:
        return error('Nombre o contraseña incorrectos.', 401)
    if not check_password(password, user.password):
        return error('Nombre o contraseña incorrectos.', 401)
    token = str(uuid.uuid4())
    profile, _ = UserProfile.objects.get_or_create(user=user)
    profile.session_token = token
    profile.save()
    return JsonResponse(perfil_data(user), status=200)


@csrf_exempt
def delete_account(request):
    if request.method != 'DELETE':
        return error('Método no permitido', 405)
    user = get_user_from_token(request)
    if not user:
        return error('No autorizado.', 401)
    user.delete()
    return JsonResponse({'mensaje': 'Cuenta eliminada correctamente.'})


@csrf_exempt
def toggle_favorito(request):
    if request.method != 'POST':
        return error('Método no permitido', 405)
    user = get_user_from_token(request)
    if not user:
        return error('No autorizado.', 401)
    data = json_body(request)
    nombre = data.get('nombre', '').strip()
    if not nombre:
        return error('Falta el nombre del atleta.')
    fav = Favorito.objects.filter(user=user, nombre=nombre).first()
    if fav:
        fav.delete()
        return JsonResponse({'is_favorito': False})
    Favorito.objects.create(
        user=user,
        nombre=nombre,
        ano_nacimiento=data.get('ano_nacimiento'),
        club=data.get('club', ''),
        categoria=data.get('categoria', ''),
        genero=data.get('genero', ''),
    )
    return JsonResponse({'is_favorito': True})


def get_favoritos(request):
    if request.method != 'GET':
        return error('Método no permitido', 405)
    user = get_user_from_token(request)
    if not user:
        return error('No autorizado.', 401)
    favs = list(user.favoritos.values('nombre', 'ano_nacimiento', 'club', 'categoria', 'genero'))
    return JsonResponse({'favoritos': favs})


@csrf_exempt
def add_busqueda(request):
    if request.method != 'POST':
        return error('Método no permitido', 405)
    user = get_user_from_token(request)
    if not user:
        return error('No autorizado.', 401)
    query = json_body(request).get('query', '').strip().upper()
    if len(query) < 2:
        return error('Mínimo 2 caracteres.')
    BusquedaReciente.objects.filter(user=user, query=query).delete()
    BusquedaReciente.objects.create(user=user, query=query)
    ids_viejos = list(BusquedaReciente.objects.filter(user=user).values_list('id', flat=True)[MAX_HISTORIAL:])
    if ids_viejos:
        BusquedaReciente.objects.filter(id__in=ids_viejos).delete()
    return JsonResponse({'ok': True})


@csrf_exempt
def remove_busqueda(request):
    if request.method != 'DELETE':
        return error('Método no permitido', 405)
    user = get_user_from_token(request)
    if not user:
        return error('No autorizado.', 401)
    query = json_body(request).get('query', '').strip().upper()
    BusquedaReciente.objects.filter(user=user, query=query).delete()
    return JsonResponse({'ok': True})


@csrf_exempt
def clear_historial(request):
    if request.method != 'DELETE':
        return error('Método no permitido', 405)
    user = get_user_from_token(request)
    if not user:
        return error('No autorizado.', 401)
    user.busquedas.all().delete()
    return JsonResponse({'ok': True})


@csrf_exempt
def guardar_tiempo(request):
    if request.method != 'POST':
        return error('Método no permitido', 405)
    user = get_user_from_token(request)
    if not user:
        return error('No autorizado.', 401)
    data = json_body(request)
    tiempo_ms = data.get('tiempo_ms')
    if tiempo_ms is None:
        return error('Falta el tiempo.')
    t = CronoTiempo.objects.create(
        user=user,
        nombre=data.get('nombre', ''),
        prueba=data.get('prueba', ''),
        piscina=data.get('piscina', ''),
        modo=data.get('modo', CronoTiempo.MODO_INDIVIDUAL),
        jugador=data.get('jugador', ''),
        tiempo_ms=tiempo_ms,
        parciales=data.get('parciales', []),
    )
    return JsonResponse({'id': t.id, 'mensaje': 'Tiempo guardado.'}, status=201)


def get_tiempos(request):
    if request.method != 'GET':
        return error('Método no permitido', 405)
    user = get_user_from_token(request)
    if not user:
        return error('No autorizado.', 401)
    limite = int(request.GET.get('limit', 5))
    tiempos = list(user.tiempos.values(
        'id', 'nombre', 'prueba', 'piscina', 'modo',
        'jugador', 'tiempo_ms', 'fecha', 'parciales')[:limite])
    for t in tiempos:
        t['fecha'] = t['fecha'].isoformat()
    return JsonResponse({'tiempos': tiempos})


@csrf_exempt
def editar_tiempo(request, tiempo_id):
    if request.method != 'PUT':
        return error('Método no permitido', 405)
    user = get_user_from_token(request)
    if not user:
        return error('No autorizado.', 401)
    try:
        tiempo = CronoTiempo.objects.get(id=tiempo_id, user=user)
    except CronoTiempo.DoesNotExist:
        return error('Tiempo no encontrado.', 404)
    data = json_body(request)
    if 'nombre' in data:
        tiempo.nombre = data['nombre']
    if 'prueba' in data:
        tiempo.prueba = data['prueba']
    if 'piscina' in data:
        tiempo.piscina = data['piscina']
    tiempo.save()
    return JsonResponse({'ok': True, 'mensaje': 'Tiempo actualizado.'})


@csrf_exempt
def eliminar_tiempo(request, tiempo_id):
    if request.method != 'DELETE':
        return error('Método no permitido', 405)
    user = get_user_from_token(request)
    if not user:
        return error('No autorizado.', 401)
    deleted, _ = CronoTiempo.objects.filter(user=user, id=tiempo_id).delete()
    if deleted:
        return JsonResponse({'ok': True})
    return error('No encontrado.', 404)