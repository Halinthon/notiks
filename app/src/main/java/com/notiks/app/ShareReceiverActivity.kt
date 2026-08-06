package com.notiks.app

import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.notiks.app.data.Hoja
import com.notiks.app.ui.NotiksViewModel
import com.notiks.app.ui.theme.NotiksTheme
import com.notiks.app.util.OrigenDetector
import com.notiks.app.util.ResumenFetcher

/**
 * Esta Activity es la que Android muestra dentro del menú "Compartir" del
 * sistema (gracias al intent-filter ACTION_SEND en el Manifest). Se abre
 * como una ventana flotante (bottom sheet) sobre la app de origen, para que
 * el usuario nunca tenga que abandonarla.
 */
class ShareReceiverActivity : ComponentActivity() {

    private val viewModel: NotiksViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ancla la ventana flotante en la parte inferior de la pantalla,
        // simulando un bottom sheet nativo.
        window.setGravity(Gravity.BOTTOM)
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

        val textoCompartido = intent?.getStringExtra(android.content.Intent.EXTRA_TEXT).orEmpty()
        val urlDetectada = OrigenDetector.extraerUrl(textoCompartido)
        val origen = OrigenDetector.detectar(textoCompartido)

        setContent {
            NotiksTheme {
                ShareSheet(
                    viewModel = viewModel,
                    textoOriginal = textoCompartido,
                    url = urlDetectada,
                    origen = origen,
                    onGuardado = { finish() },
                    onCancelar = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareSheet(
    viewModel: NotiksViewModel,
    textoOriginal: String,
    url: String?,
    origen: com.notiks.app.data.Origen,
    onGuardado: () -> Unit,
    onCancelar: () -> Unit
) {
    val hojasRecientes by viewModel.hojasRecientes.collectAsState(initial = emptyList())
    val cuadernos by viewModel.cuadernos.collectAsState(initial = emptyList())

    val textoSinUrl = remember(textoOriginal, url) {
        textoOriginal.replace(url ?: "", "").trim()
    }
    var resumen by remember { mutableStateOf(ResumenFetcher.limitarPalabras(textoSinUrl, 20)) }
    var editadoPorUsuario by remember { mutableStateOf(false) }
    var cargandoResumen by remember { mutableStateOf(false) }
    var mostrarNuevaHoja by remember { mutableStateOf(false) }
    var cuadernoSeleccionado by remember { mutableStateOf<Long?>(null) }

    // La app de origen a veces solo comparte el link, sin título ni texto.
    // En ese caso, entramos a la página y traemos el título/descripción real
    // del contenido para no dejar guardado solo la URL.
    LaunchedEffect(url) {
        if (url != null && textoSinUrl.length < 15) {
            cargandoResumen = true
            val automatico = ResumenFetcher.obtenerResumenDesdeUrl(url)
            if (!automatico.isNullOrBlank() && !editadoPorUsuario) {
                resumen = ResumenFetcher.limitarPalabras(automatico, 20)
            }
            cargandoResumen = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 4.dp
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Guardar en Notiks", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(
                "Origen detectado: ${OrigenDetector.nombreLegible(origen)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = resumen,
                onValueChange = {
                    editadoPorUsuario = true
                    if (it.split(Regex("\\s+")).size <= 20) resumen = it
                },
                label = { Text("Resumen (máx. 20 palabras)") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
            if (cargandoResumen) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Buscando de qué trata el contenido…",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Elige una hoja", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(hojasRecientes, key = { it.id }) { hoja ->
                    HojaOpcion(hoja = hoja) {
                        viewModel.guardarItem(hoja.id, url, resumen, origen)
                        onGuardado()
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = { mostrarNuevaHoja = true }) {
                Text("+ Crear hoja nueva")
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    }

    if (mostrarNuevaHoja) {
        NuevaHojaParaGuardarDialog(
            cuadernos = cuadernos,
            onDismiss = { mostrarNuevaHoja = false },
            onConfirmar = { cuadernoId, titulo ->
                viewModel.crearHoja(cuadernoId, titulo) { nuevaHojaId ->
                    viewModel.guardarItem(nuevaHojaId, url, resumen, origen)
                }
                mostrarNuevaHoja = false
                onGuardado()
            }
        )
    }
}

@Composable
private fun HojaOpcion(hoja: Hoja, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(10.dp))
        Text(hoja.titulo, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun NuevaHojaParaGuardarDialog(
    cuadernos: List<com.notiks.app.data.Cuaderno>,
    onDismiss: () -> Unit,
    onConfirmar: (Long, String) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var cuadernoId by remember { mutableStateOf(cuadernos.firstOrNull()?.id) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva hoja") },
        text = {
            Column {
                if (cuadernos.isEmpty()) {
                    Text("Primero crea un cuaderno desde la app principal.", textAlign = TextAlign.Center)
                } else {
                    Text("Cuaderno: ${cuadernos.first { it.id == cuadernoId }.nombre}")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        label = { Text("Título de la hoja") },
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = titulo.isNotBlank() && cuadernoId != null,
                onClick = { cuadernoId?.let { onConfirmar(it, titulo) } }
            ) { Text("Crear y guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
