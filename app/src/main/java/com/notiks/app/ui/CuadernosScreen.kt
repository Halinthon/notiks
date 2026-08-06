package com.notiks.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notiks.app.data.Cuaderno
import com.notiks.app.util.CuadernoImportado
import com.notiks.app.util.ImportUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuadernosScreen(
    viewModel: NotiksViewModel,
    onAbrirCuaderno: (Long, String) -> Unit,
    onExportar: () -> Unit
) {
    val cuadernos by viewModel.cuadernos.collectAsState(initial = emptyList())
    var mostrarDialogo by remember { mutableStateOf(false) }
    var cuadernoAEliminar by remember { mutableStateOf<Cuaderno?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var respaldoParaConfirmar by remember { mutableStateOf<List<CuadernoImportado>?>(null) }
    var errorImportacion by remember { mutableStateOf(false) }
    var importando by remember { mutableStateOf(false) }

    val selectorArchivo = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val texto = context.contentResolver.openInputStream(uri)
                        ?.bufferedReader()
                        ?.use { it.readText() }
                    val cuadernosImportados = texto?.let { ImportUtil.parsear(it) }
                    if (cuadernosImportados.isNullOrEmpty()) {
                        errorImportacion = true
                    } else {
                        respaldoParaConfirmar = cuadernosImportados
                    }
                } catch (e: Exception) {
                    errorImportacion = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notiks", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { selectorArchivo.launch("application/json") }) {
                        Icon(Icons.Default.FileUpload, contentDescription = "Importar respaldo")
                    }
                    IconButton(onClick = onExportar) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Exportar respaldo")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogo = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo cuaderno")
            }
        }
    ) { padding ->
        if (cuadernos.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Aún no tienes cuadernos.\nToca + para crear el primero.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cuadernos) { cuaderno ->
                    CuadernoCard(
                        cuaderno = cuaderno,
                        onClick = { onAbrirCuaderno(cuaderno.id, cuaderno.nombre) },
                        onEliminarClick = { cuadernoAEliminar = cuaderno }
                    )
                }
            }
        }
    }

    if (mostrarDialogo) {
        NuevoCuadernoDialog(
            onDismiss = { mostrarDialogo = false },
            onCrear = { nombre ->
                viewModel.crearCuaderno(nombre)
                mostrarDialogo = false
            }
        )
    }

    cuadernoAEliminar?.let { cuaderno ->
        EliminarCuadernoDialog(
            nombreCuaderno = cuaderno.nombre,
            onDismiss = { cuadernoAEliminar = null },
            onConfirmar = {
                viewModel.eliminarCuaderno(cuaderno)
                cuadernoAEliminar = null
            }
        )
    }

    respaldoParaConfirmar?.let { cuadernosImportados ->
        AlertDialog(
            onDismissRequest = { if (!importando) respaldoParaConfirmar = null },
            icon = { Icon(Icons.Default.FileUpload, contentDescription = null) },
            title = { Text("¿Importar este respaldo?") },
            text = {
                if (importando) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Importando...")
                    }
                } else {
                    Text(
                        "Se agregarán ${ImportUtil.contarCuadernos(cuadernosImportados)} cuadernos, " +
                            "${ImportUtil.contarHojas(cuadernosImportados)} hojas y " +
                            "${ImportUtil.contarItems(cuadernosImportados)} enlaces a lo que ya tienes en esta app. " +
                            "No se borra nada existente."
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !importando,
                    onClick = {
                        importando = true
                        viewModel.importarRespaldo(cuadernosImportados) {
                            importando = false
                            respaldoParaConfirmar = null
                        }
                    }
                ) { Text("Importar") }
            },
            dismissButton = {
                TextButton(enabled = !importando, onClick = { respaldoParaConfirmar = null }) { Text("Cancelar") }
            }
        )
    }

    if (errorImportacion) {
        AlertDialog(
            onDismissRequest = { errorImportacion = false },
            title = { Text("No se pudo leer el archivo") },
            text = { Text("Verifica que sea el archivo .json exportado desde Notiks (por ejemplo, \"notiks_respaldo.json\").") },
            confirmButton = {
                TextButton(onClick = { errorImportacion = false }) { Text("Entendido") }
            }
        )
    }
}

@Composable
private fun CuadernoCard(cuaderno: Cuaderno, onClick: () -> Unit, onEliminarClick: () -> Unit) {
    val color = try { Color(android.graphics.Color.parseColor(cuaderno.colorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color.White)
        }
        Spacer(Modifier.width(14.dp))
        Text(cuaderno.nombre, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        IconButton(onClick = onEliminarClick) {
            Icon(
                Icons.Default.DeleteOutline,
                contentDescription = "Eliminar cuaderno",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EliminarCuadernoDialog(
    nombreCuaderno: String,
    onDismiss: () -> Unit,
    onConfirmar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null) },
        title = { Text("¿Eliminar \"$nombreCuaderno\"?") },
        text = {
            Text("Se eliminarán también todas sus hojas y los enlaces guardados dentro de ellas. Esta acción no se puede deshacer.")
        },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text("Eliminar", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun NuevoCuadernoDialog(onDismiss: () -> Unit, onCrear: (String) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo cuaderno") },
        text = {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { if (nombre.isNotBlank()) onCrear(nombre) }) { Text("Crear") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
