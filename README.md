# 📚 ReadLog

**ReadLog** es una aplicación Android nativa para gestionar tu biblioteca personal de lectura. Registra tus libros, rastrea tu progreso, visualiza estadísticas y mantén un calendario de tu actividad lectora.

## 🌟 Características

### 📖 Gestión de Libros
- **Añadir libros** con información detallada (título, autor, notas)
- **Estados de lectura**: Pendiente, En Progreso, Leído
- **Seguimiento de páginas**: Registra la página actual y total de páginas
- **Sistema de favoritos**: Marca tus libros preferidos con ⭐
- **Búsqueda**: Encuentra libros rápidamente por título o autor
- **Edición y eliminación**: Actualiza o borra libros de tu biblioteca

### 📊 Estadísticas
- Total de libros en la biblioteca
- Libros leídos completados
- Libros pendientes
- Páginas totales leídas

### 📅 Calendario
- Visualiza un calendario con la actividad de lectura
- Identifica los días en que has actualizado tus libros
- Navega entre diferentes meses

### ⚙️ Configuración
- Interfaz de configuración personalizable
- Tema Material Design 3
- Soporte multiidioma (Español e Inglés)

## 🛠️ Tecnologías

- **Lenguaje**: Java
- **Plataforma**: Android (API 34+)
- **Base de datos**: SQLite
- **UI**: Material Design 3 Components
- **Build System**: Gradle (Kotlin DSL)

## 📋 Requisitos

- Android Studio (versión más reciente)
- Android SDK 34 o superior
- Dispositivo o emulador con Android 14+ (API 34)

## 🚀 Instalación

1. **Clona el repositorio**:
   ```bash
   git clone https://github.com/tu-usuario/ReadLog.git
   cd ReadLog
   ```

2. **Abre el proyecto en Android Studio**:
   - File > Open > Selecciona la carpeta del proyecto

3. **Sincroniza las dependencias**:
   - Android Studio sincronizará automáticamente las dependencias de Gradle

4. **Ejecuta la aplicación**:
   - Conecta un dispositivo Android o inicia un emulador
   - Haz clic en "Run" o presiona `Shift + F10`

## 📱 Estructura del Proyecto

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/readlog/
│   │   │   ├── MainActivity.java           # Pantalla principal con lista de libros
│   │   │   ├── LibroActivity.java          # Formulario para añadir/editar libros
│   │   │   ├── EstadisticasActivity.java   # Pantalla de estadísticas
│   │   │   ├── CalendarioActivity.java     # Calendario de actividad
│   │   │   ├── ConfiguracionActivity.java  # Pantalla de configuración
│   │   │   ├── BaseActivity.java           # Actividad base con barra de navegación
│   │   │   ├── Libro.java                  # Modelo de datos
│   │   │   ├── LibroAdapter.java           # Adaptador RecyclerView
│   │   │   └── AdminSQLiteOpenHelper.java  # Gestión de base de datos
│   │   ├── res/
│   │   │   ├── layout/                     # Diseños XML
│   │   │   ├── values/                     # Recursos (strings, colors, themes)
│   │   │   └── drawable/                   # Iconos y recursos gráficos
│   │   └── AndroidManifest.xml
│   ├── androidTest/                        # Tests instrumentados
│   └── test/                               # Tests unitarios
└── build.gradle.kts                        # Configuración de Gradle
```

## 🗄️ Base de Datos

La aplicación utiliza **SQLite** con la siguiente estructura:

### Tabla `libros`
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | INTEGER | Clave primaria (autoincremental) |
| titulo | TEXT | Título del libro |
| autor | TEXT | Autor del libro |
| notas | TEXT | Notas personales |
| leido | INTEGER | 1 si está leído, 0 si no |
| pagina_actual | INTEGER | Página actual de lectura |
| paginas_totales | INTEGER | Total de páginas del libro |
| favorito | INTEGER | 1 si es favorito, 0 si no |
| estado | TEXT | 'pendiente', 'en_progreso' o 'leido' |

## 🎨 Diseño

La aplicación sigue los principios de **Material Design 3**:
- Colores dinámicos y tema oscuro/claro
- Componentes modernos (FloatingActionButton, MaterialToolbar, CardView)
- Navegación intuitiva con BottomNavigationBar
- Diseño responsive y accesible

## 📝 Uso

### Añadir un libro
1. En la pantalla principal, toca el botón flotante "AÑADIR LIBRO"
2. Completa los campos de título y autor (obligatorios)
3. Opcionalmente añade notas, páginas y marca como favorito
4. Toca "GUARDAR"

### Editar un libro
1. En la lista principal, toca el libro que deseas editar
2. Modifica los campos necesarios
3. Toca "ACTUALIZAR"

### Ver estadísticas
1. Toca el ícono de estadísticas en la barra superior
2. Visualiza tus métricas de lectura

### Consultar el calendario
1. Toca el ícono de calendario en la barra superior
2. Navega entre meses para ver tu actividad de lectura

## 🌐 Idiomas Soportados

- 🇪🇸 **Español** (predeterminado)
- 🇬🇧 **English**

## 🔧 Configuración de Desarrollo

### Dependencias principales
```gradle
dependencies {
    implementation 'androidx.appcompat:appcompat'
    implementation 'com.google.android.material:material'
    implementation 'androidx.activity:activity'
    implementation 'androidx.constraintlayout:constraintlayout'
}
```

### Versiones
- **Gradle**: 8.0+
- **Compile SDK**: 36
- **Min SDK**: 34
- **Target SDK**: 36
- **Java**: 11



**¡Disfruta organizando tu biblioteca de lectura con ReadLog! 📚✨**
