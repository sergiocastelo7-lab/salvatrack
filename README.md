<h1 align="center">🏊 SalvaTrack</h1>

<p align="center">
  <img src="https://img.shields.io/badge/STATUS-COMPLETADO-green" />
  <img src="https://img.shields.io/badge/Android-Java-brightgreen?logo=android" />
  <img src="https://img.shields.io/badge/Backend-Django-darkgreen?logo=django" />
  <img src="https://img.shields.io/badge/Firebase-Firestore-orange?logo=firebase" />
  <img src="https://img.shields.io/badge/Python-3.11+-blue?logo=python" />
</p>

<p align="center">
  Aplicación Android para cronometrar y gestionar resultados de <strong>Salvamento Acuático</strong>.
  Conecta un backend Django propio con Firebase Firestore para ofrecer
  información federativa en tiempo real, perfiles de atletas y registro de tiempos.
</p>

---

## 📋 Índice

- [Descripción](#-descripción)
- [Funcionalidades](#-funcionalidades)
- [Tecnologías utilizadas](#-tecnologías-utilizadas)
- [Arquitectura](#-arquitectura)
- [Instalación y ejecución](#-instalación-y-ejecución)
- [Autor](#-autor)

---

## 📖 Descripción

SalvaTrack es una app móvil Android orientada a deportistas y técnicos de Salvamento Acuático.
Permite cronometrar entrenamientos, consultar el calendario de eventos federativos,
buscar perfiles de atletas con sus marcas personales y guardar los tiempos en una cuenta propia.

El proyecto combina dos fuentes de datos:
- **Firebase Firestore** para los datos públicos del deporte (atletas, eventos, mínimas, récords).
- **Backend Django propio** para los datos del usuario (cuenta, favoritos, tiempos guardados).

---

## ✨ Funcionalidades

- `Cronómetro individual` — alta precisión con parciales y guardado de tiempos.
- `Cronómetro multijugador` — hasta 4 atletas en paralelo con guardado global.
- `Federaciones` — próximo evento, accesos rápidos y calendario con eventos marcados.
- `Evento en Directo` — resultados, lista de salida y streaming editables por el admin.
- `Buscador de atletas` — búsqueda con normalización de tildes y caché local.
- `Perfil de deportista` — Mis Marcas comparadas con mínimas/récords e historial por temporadas.
- `Configuración` — historial de tiempos, editar/eliminar, gestión de cuenta y panel admin.

---

## 🛠️ Tecnologías utilizadas

| Capa | Tecnología |
|------|------------|
| Cliente | Android (Java), Android Studio Ladybug |
| Backend | Python 3.11, Django 6.0.5 |
| Base de datos servidor | SQLite |
| Base de datos pública | Firebase Firestore |
| Autenticación | Tokens UUID propios (sin librerías externas) |
| Control de versiones | Git / GitHub |

---

## 🏗️ Arquitectura

```
📁 frontend/   → App Android (Java)
📁 backend/    → API REST Django
```

### Endpoints API REST

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/register/` | Crear cuenta |
| POST | `/api/login/` | Iniciar sesión |
| DELETE | `/api/account/` | Eliminar cuenta |
| GET | `/api/favoritos/` | Obtener favoritos |
| POST | `/api/favoritos/toggle/` | Añadir/quitar favorito |
| POST | `/api/historial/` | Añadir búsqueda |
| DELETE | `/api/historial/<query>/` | Eliminar búsqueda concreta |
| DELETE | `/api/historial/` | Borrar todo el historial |
| POST | `/api/crono/` | Guardar tiempo |
| GET | `/api/crono/?limit=N` | Obtener últimos N tiempos |
| PUT | `/api/crono/<id>/` | Editar tiempo guardado |
| DELETE | `/api/crono/<id>/` | Eliminar tiempo guardado |

> Todas las peticiones excepto `/register/` y `/login/` requieren el header `Authorization: Token <token>`

---

## 🚀 Instalación y ejecución

### 1. Clonar el repositorio
```bash
git clone https://github.com/tu-usuario/salvatrack.git
```

### 2. Arrancar el backend
```bash
cd backend
pip install django django-cors-headers
python manage.py migrate
python manage.py createsuperuser
python manage.py runserver
```

### 3. Ejecutar la app Android
1. Abrir la carpeta `frontend` con Android Studio.
2. Compilar y ejecutar en un emulador o dispositivo con Android 8.0+.

---

## 👤 Autor

| [<img src="https://github.com/identicons/sergio.png" width=80><br><sub>Sergio Castelo Varela</sub>](https://github.com/sergi) |
|:---:|

CFP Afundación — Proyecto Intermodular DAM · Mayo 2026
