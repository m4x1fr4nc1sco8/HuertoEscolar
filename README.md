# Huerto Escolar - Android App

**Huerto Escolar** es una aplicación móvil nativa para Android desarrollada en **Kotlin** y **Jetpack Compose**. Representa una solución integral para la gestión, seguimiento participativo y registro de cultivos, sectores y tareas dentro de un huerto escolar, separando permisos y flujos según el rol del usuario (Administrador y Estudiante).

---

## Integrantes del Equipo
* **Estudiante 1:** [Maximiliano Araos]
* **Asignatura:** Desarrollo de Aplicaciones Móviles (DSY1105)
* **Institución:** Duoc UC

---

## Planificación y Gestión del Proyecto
* **Tablero de Trello:** [PEGA_AQUÍ_EL_ENLACE_A_SU_TRELLO_PÚBLICO]

---

## Funcionalidades Implementadas

### Autenticación y Control de Roles
* **Acceso y Registro:** Inicio de sesión y registro de usuarios con validación de campos.
* **Roles (RBAC):**
  * **Administrador:** Gestión de sectores, registro de cultivos y asignación/revisión de tareas.
  * **Estudiante:** Consulta de información y resolución de tareas asignadas.

### Gestión de Sectores y Cultivos
* Registro y listado de sectores del huerto con estado visual.
* Formulario de cultivos con fecha de siembra, cosecha estimada y frecuencia de riego.
* Validaciones desacopladas desde la lógica de negocio (`Validaciones.kt`).

### Recursos Nativos del Dispositivo (IE 2.4.1)
1. **Cámara Nativa:** Captura directa de fotografías para evidencias y fotos referenciales mediante `ActivityResultContracts.TakePicture`.
2. **Galería / Selector Visual:** Selección de imágenes desde el almacenamiento del dispositivo mediante `ActivityResultContracts.PickVisualMedia`.

---

## Arquitectura y Tecnologías

* **Lenguaje:** Kotlin
* **UI:** Jetpack Compose (Material Design 3)
* **Arquitectura:** MVVM (Model-View-ViewModel) con desacoplamiento de lógica
* **Persistencia Local:** Room Database (SQLite)
* **Navegación:** Jetpack Navigation Compose con animaciones entre pantallas (`Crossfade`, `AnimatedVisibility`)
* **Carga de Imágenes:** Coil 3 (`AsyncImage`)

---

## Estructura del Proyecto

```text
cl.duoc.huertoescolar/
├── data/              # Base de datos Room (Entities, DAOs, Repositorios)
├── model/             # Modelos de dominio y Enums (Cultivo, Sector, Usuario, Tarea)
├── ui/
│   ├── components/    # Componentes UI reutilizables (CampoTexto, CampoDesplegable)
│   ├── navigation/    # NavHost, Rutas y transiciones
│   ├── screens/       # Pantallas principales
│   └── theme/         # Tema visual y paleta de colores
├── util/              # Helpers para fotos, validaciones y etiquetas
└── viewmodel/         # ViewModels y UiStates
