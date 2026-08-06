# Notiks

App Android para guardar enlaces compartidos (YouTube, X, Instagram, Google Discover, cualquier web)
organizados en **Cuadernos → Hojas → Ítems**, como si cada Hoja fuera una sesión de chat.

## Cómo generar el APK

1. Instala **Android Studio** (versión Koala 2024.1 o más reciente): https://developer.android.com/studio
2. Abre Android Studio → **Open** → selecciona la carpeta `notiks` (la que contiene `settings.gradle.kts`).
3. Espera a que Gradle sincronice (descarga dependencias automáticamente, requiere internet).
4. Conecta un celular Android (con "Depuración USB" activada) o crea un emulador (AVD) desde
   **Device Manager**.
5. Pulsa el botón ▶ **Run 'app'**. Esto compila e instala Notiks en el dispositivo.

### Para obtener el archivo `.apk` instalable
Menú **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
El archivo queda en `app/build/outputs/apk/debug/app-debug.apk`. Puedes copiarlo y enviarlo
a cualquier celular para instalarlo (activando "Instalar apps de orígenes desconocidos").

## Requisitos
- Android 8.0 (API 26) o superior.
- No requiere conexión a internet para funcionar (todo se guarda localmente con Room/SQLite).

## Cómo se usa

1. **Compartir:** desde YouTube, X, Instagram, Chrome, o al tocar un artículo de Google Discover,
   usa el botón "Compartir" → elige **Notiks** en la lista de apps.
2. Aparece una ventana flotante: escribe un resumen breve (máx. 30 palabras), elige una Hoja
   existente o crea una nueva, y toca para guardar. Vuelves automáticamente a la app de origen.
3. **Consultar:** abre Notiks → entra a un Cuaderno → entra a una Hoja → verás el listado
   cronológico de todo lo guardado ahí, como una conversación. Toca cualquier ítem para abrir
   el enlace original.
4. **Respaldo:** desde la pantalla principal, toca el ícono de descarga (arriba a la derecha)
   para exportar todo tu contenido a un archivo `.json`. Guárdalo en Google Drive o donde
   prefieras: si formateas el celular, puedes usar ese archivo como referencia de lo guardado
   (la importación automática no está incluida en esta primera versión, pero el archivo queda
   legible y ordenado por cuaderno/hoja).

## Estructura del proyecto
```
app/src/main/java/com/notiks/app/
├── MainActivity.kt              → navegación principal (Cuadernos → Hojas → Ítems)
├── ShareReceiverActivity.kt      → pantalla que aparece al "Compartir" desde otras apps
├── data/                         → entidades Room, DAOs, base de datos, repositorio
├── ui/                           → pantallas Compose y ViewModel
└── util/                         → detección de origen (YouTube/X/Instagram/Discover) y exportación
```
