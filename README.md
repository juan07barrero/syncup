# 🎵 SyncUp - Music Player

Un reproductor musical interactivo desarrollado en **Java con JavaFX**, con características avanzadas de búsqueda, recomendaciones personalizadas y gestión social.

## 🌟 Características principales

### 🎶 Reproducción de Música
- Reproductor multimedia con controles básicos (play, pause, stop)
- Barra de progreso interactiva
- Información en tiempo real de la canción que se reproduce

### 🔍 Búsqueda Avanzada
- **Autocompletado** mediante estructura Trie
- **Búsqueda fuzzy** usando Árbol BK (distancia Levenshtein)
- Búsqueda por título, artista o género
- Filtrado dinámico en tiempo real

### 💿 Gestión de Biblioteca
- Carga de canciones desde archivos CSV
- Indexación múltiple para búsquedas eficientes
- Estructura de datos personalizada: Lista Enlazada

### ❤️ Sistema de Favoritos
- Marcar/desmarcar canciones como favoritas
- Persistencia en archivos CSV individuales por usuario

### 📋 Playlists Personalizadas
- Crear, editar y eliminar playlists
- Gestionar canciones dentro de playlists
- Almacenamiento persistente

### 👥 Red Social
- Seguir/dejar de seguir otros usuarios
- Sugerencias de usuarios
- Visualización de seguidos

### 📊 Recomendaciones
- **Recomendador basado en favoritos**: Sugiere canciones similares por género y artista
- **Recomendador basado en historial**: Analiza el historial de reproducción
- Algoritmo de puntuación: género (+3), artista (+2)

### 📈 Estadísticas
- Panel de usuario con estadísticas personales
- Dashboard administrativo con métricas globales
- Exportación de reportes

## 🏗️ Arquitectura

### Patrón de Diseño
- **MVC**: Separación entre controladores, modelos y vistas
- **Singleton**: DataStore como hub central de acceso

### Componentes Principales

#### Modelo (`com.syncup.app.model`)
- `Cancion`: Entidad que representa una canción
- `Usuario`: Entidad de usuario con roles (admin/usuario)
- `BibliotecaMusical`: Gestión de biblioteca
- `ListaCanciones`: Lista enlazada para almacenamiento

#### Lógica (`com.syncup.app.logic`)
- `DataStore`: Singleton central
- `UsuarioManager`: Gestión de usuarios
- `FavoritosManager`: Sistema de favoritos
- `PlaylistManager`: Gestión de playlists
- `HistorialManager`: Registro de reproducción
- `GrafoSocial`: Red social
- `Recomendador`: Engine de recomendaciones
- `RecomendadorMusical`: Recomendaciones por historial
- `EstadisticasGlobales`: Métricas globales

#### Estructuras de Datos
- `Trie`: Autocompletado por prefijo - O(m) donde m es longitud del prefijo
- `BKTree`: Búsqueda fuzzy con distancia Levenshtein
- `ArbolSimilitud`: Árbol jerárquico género→artista
- `ListaCanciones`: Estructura personalizada de lista enlazada

#### Controladores (`com.syncup.app.controllers`)
- `LoginController`: Autenticación y registro
- `UsuarioController`: Panel principal
- `PlaylistController`: Gestión de playlists
- `EstadisticasController`: Estadísticas personales
- `EstadisticasAdminController`: Dashboard admin
- `DashboardChartsController`: Gráficos de métricas
- `EditarPerfilController`: Edición de perfil

### Capa de Persistencia
- Archivos CSV para usuarios, canciones, historial, favoritos y playlists
- Rutas: `src/main/resources/data/`

## 🛠️ Tecnologías

- **Lenguaje**: Java 21
- **Framework UI**: JavaFX 21
- **Build Tool**: Maven 3.8.1+
- **Persistencia**: CSV
- **Testing**: JUnit 5

## 📦 Instalación

### Prerequisitos
- Java 21 o superior
- Maven 3.8.1 o superior

### Clonar y Compilar
```bash
git clone https://github.com/tu-usuario/syncup.git
cd syncup
mvn clean install
```

### Ejecutar
```bash
mvn javafx:run
```

## 📚 Generación de Documentación

### JavaDoc
```bash
mvn javadoc:javadoc
```
Documentación disponible en: `target/reports/apidocs/index.html`

## 🧪 Testing

```bash
mvn test
```

## 📁 Estructura de Directorios

```
syncup/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/syncup/app/
│   │   │       ├── controllers/
│   │   │       ├── logic/
│   │   │       ├── model/
│   │   │       └── Main.java
│   │   └── resources/
│   │       ├── data/           # Datos CSV
│   │       ├── styles/         # Estilos CSS
│   │       └── views/          # Archivos FXML
│   └── test/
│       └── java/               # Tests JUnit
├── docs/
│   └── uml/
│       └── Syncup_UML.puml     # Diagrama UML
├── pom.xml
└── README.md
```

## 🎮 Uso Básico

1. **Iniciar sesión** con credenciales de prueba
   - Usuario: `juan` / Contraseña: `123`
   - Usuario: `admin` / Contraseña: `admin`

2. **Reproducir música**: Selecciona canción → Click en play

3. **Buscar canciones**: Usa el campo de búsqueda con autocompletado

4. **Gestionar favoritos**: Click en corazón

5. **Ver recomendaciones**: Panel social con sugerencias

## 👨‍💻 Autores

- Juan Barrero - Desarrollo principal

## 📝 Licencia

Este proyecto está bajo licencia MIT.

## 🐛 Problemas y Soporte

Para reportar problemas o sugerencias, abre un issue en GitHub.

---

**Versión**: 1.0-SNAPSHOT  
**Última actualización**: Noviembre 2025
