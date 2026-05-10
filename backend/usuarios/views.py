import json
import uuid
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.contrib.auth.models import User
from django.contrib.auth.hashers import make_password, check_password
from .models import UserProfile


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
        profile  = user.profile
        is_admin = profile.is_admin
        token    = profile.session_token
    except UserProfile.DoesNotExist:
        is_admin = False
        token    = None
    return {
        'token':    token,
        'nombre':   user.username,
        'is_admin': is_admin,
    }


@csrf_exempt
def register(request):
    if request.method != 'POST':
        return error('Método no permitido', 405)

    data     = json_body(request)
    nombre   = data.get('nombre', '').strip()
    password = data.get('password', '').strip()

    if not nombre or not password:
        return error('El nombre y la contraseña son obligatorios.')
    if len(password) < 4:
        return error('La contraseña debe tener al menos 4 caracteres.')
    if User.objects.filter(username=nombre).exists():
        return error('Ya existe una cuenta con ese nombre.', 409)

    user  = User.objects.create(username=nombre, password=make_password(password))
    token = str(uuid.uuid4())
    UserProfile.objects.create(user=user, session_token=token)

    return JsonResponse(perfil_data(user), status=201)


@csrf_exempt
def login(request):
    if request.method != 'POST':
        return error('Método no permitido', 405)

    data     = json_body(request)
    nombre   = data.get('nombre', '').strip()
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