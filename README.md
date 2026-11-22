# Huerto Escolar - Android App

Huerto Escolar es una aplicación móvil nativa para Android desarrollada en **Kotlin** y **Jetpack Compose**. Representa una solución integral para la gestión, seguimiento participativo y registro de cultivos, sectores y tareas dentro de un huerto escolar, separando permisos y flujos según el rol del usuario (Administrador y Estudiante).

Los sectores y cultivos se leen y escriben en un microservicio propio hecho con Spring Boot, y la app consume además una API externa del clima.

## Autor

- **Estudiante:** Maximiliano Araos
- **Asignatura:** Desarrollo de Aplicaciones Móviles (DSY1105)
- **Institución:** Duoc UC
- **Evaluación:** Parcial 4 (continuación de HuertoEscolar)

## Repositorios

- Aplicación Android (este repositorio): https://github.com/m4x1fr4nc1sco8/HuertoEscolar
- Microservicio Spring Boot: https://github.com/m4x1fr4nc1sco8/HuertoBackend (README con la lista de endpoints)

## Funcionalidades implementadas

### Autenticación y control de roles

- **Acceso y registro:** inicio de sesión y registro de usuarios con validación de campos. Los usuarios se guardan en Room.
- **Roles (RBAC):**
  - **Administrador:** gestión de sectores, registro de cultivos y asignación/revisión de tareas.
  - **Estudiante:** consulta de información y resolución de tareas asignadas.

### Gestión de sectores y cultivos

- Registro y listado de sectores del huerto con estado visual.
- Formulario de cultivos con fecha de siembra, cosecha estimada y frecuencia de riego.
- Validaciones desacopladas de la interfaz (`domain/validation`).

### Consumo de microservicio y API externa

- **Microservicio propio (Spring Boot):** crear, listar, editar y eliminar sectores y cultivos mediante Retrofit. Room funciona como copia local de los datos del servidor.
- **Aviso sin conexión:** la app informa cuando no puede comunicarse con el servidor.
- **API externa del clima:** consulta del clima de San Bernardo con Open-Meteo.

### Recursos nativos del dispositivo

1. **Cámara nativa:** captura directa de fotografías para evidencias y fotos referenciales con `ActivityResultContracts.TakePicture`.
2. **Galería / selector visual:** selección de imágenes desde el almacenamiento del dispositivo con `ActivityResultContracts.PickVisualMedia`.

## Arquitectura y tecnologías

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose (Material Design 3)
- **Arquitectura:** MVVM con separación por capas (`data`, `domain`, `ui`), repositorios inyectados por constructor y fábricas de ViewModel
- **Persistencia local:** Room Database (SQLite)
- **Red:** Retrofit con converter-gson
- **Navegación:** Jetpack Navigation Compose, con animaciones entre pantallas (`Crossfade`, `AnimatedVisibility`)
- **Carga de imágenes:** Coil 3 (`AsyncImage`)
- **Pruebas unitarias:** JUnit 4, MockK, kotlinx-coroutines-test y cobertura con Kover

## Estructura del proyecto

```
cl.duoc.huertoescolar/
├── data/
│   ├── local/         # Room (Entities, DAOs, AppDatabase)
│   ├── remote/        # Retrofit: interfaces de API, clientes y DTOs
│   ├── repository/    # Repositorios (microservicio, clima, Room)
│   └── session/       # Sesión del usuario actual
├── domain/
│   ├── model/         # Modelos de dominio y enums (Cultivo, Sector, Usuario, Tarea)
│   └── validation/    # Reglas de validación (Validaciones.kt)
├── ui/
│   ├── components/    # Componentes reutilizables (CampoTexto, CampoDesplegable)
│   ├── navigation/    # NavHost, rutas y transiciones
│   ├── screens/       # Pantallas principales
│   ├── theme/         # Tema visual y paleta verde del huerto
│   └── viewmodel/     # ViewModels, UiStates y fábricas
└── util/              # Helpers para fotos y etiquetas
```

## Cómo ejecutar el proyecto

### 1. Levantar el backend

1. Inicia **MySQL** desde XAMPP.
2. Importa `database/huerto_escolar.sql` del repositorio HuertoBackend en phpMyAdmin.
3. Ejecuta el microservicio (`.\mvnw spring-boot:run`, o `HuertoApiApplication` desde IntelliJ IDEA).
4. Comprueba que Swagger abre en http://localhost:8080/swagger-ui.html

Desde el emulador, la app accede al backend en `http://10.0.2.2:8080/`.

### 2. Ejecutar la app

1. Abre el proyecto en Android Studio y espera la sincronización de Gradle.
2. Usa el emulador **Pixel 8 con API 36** (minSdk 24).
3. Ejecuta la configuración `app`.

### Cuentas de prueba

| Rol | Correo | Contraseña |
|-----|--------|------------|
| Administrador | admin@huerto.cl | huerto1234 |
| Estudiante | estudiante@huerto.cl | huerto1234 |

## APK firmado

El APK firmado con keystore (`.jks`) está en la carpeta `apk-firmada/`:

- `apk-firmada/HuertoEscolar-v1.0-build1.apk`

Instalación en el emulador o en un dispositivo conectado:

```powershell
adb install apk-firmada\HuertoEscolar-v1.0-build1.apk
```

Verificación de la firma:

```powershell
apksigner verify --verbose --print-certs apk-firmada\HuertoEscolar-v1.0-build1.apk
```

El archivo `.jks` y `keystore.properties` no se incluyen en el repositorio por seguridad.

Si ya había una versión instalada con otra firma (por ejemplo la de debug), primero se desinstala con `adb uninstall cl.duoc.huertoescolar`.

## Pruebas unitarias

Las pruebas cubren la lógica de ViewModels, repositorios y validaciones. Para ejecutarlas y generar el informe de cobertura:

```powershell
.\gradlew testDebugUnitTest
.\gradlew koverHtmlReportDebug
```

El informe HTML queda en `app/build/reports/kover/htmlDebug/index.html`.

- **Pruebas:** 294 en verde (completar si cambió la cifra)
- **Cobertura de la lógica:** superior al 98 % (completar con la cifra del último informe)
