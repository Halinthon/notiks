package com.notiks.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.notiks.app.data.Hoja
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojasScreen(
    viewModel: NotiksViewModel,
    cuadernoId: Long,
    nombreCuaderno: String,
    onVolver: () -> Unit,
    onAbrirHoja: (Long, String) -> Unit
) {
    val hojas by viewModel.hojasDe(cuadernoId).collectAsState(initial = emptyList())
    var mostrarDialogo by remember { mutableStateOf(false) }
    var hojaAEliminar by remember { mutableStateOf<Hoja?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(nombreCuaderno) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogo = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nueva hoja")
            }
        }
    ) { padding ->
        if (hojas.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Este cuaderno no tiene hojas todavía.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(hojas, key = { it.id }) { hoja ->
                    HojaRow(
                        hoja = hoja,
                        onClick = { onAbrirHoja(hoja.id, hoja.titulo) },
                        onEliminarClick = { hojaAEliminar = hoja }
                    )
                }
            }
        }
    }

    if (mostrarDialogo) {
        NuevaHojaDialog(
            onDismiss = { mostrarDialogo = false },
            onCrear = { titulo ->
                viewModel.crearHoja(cuadernoId, titulo)
                mostrarDialogo = false
            }
        )
    }

    hojaAEliminar?.let { hoja ->
        AlertDialog(
            onDismissRequest = { hojaAEliminar = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null) },
            title = { Text("¿Eliminar \"${hoja.titulo}\"?") },
            text = { Text("Se eliminarán también todos los artículos guardados dentro de esta hoja. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarHoja(hoja)
                    hojaAEliminar = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { hojaAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun HojaRow(hoja: Hoja, onClick: () -> Unit, onEliminarClick: () -> Unit) {
    val formato = remember { SimpleDateFormat("d MMM, HH:mm", Locale("es")) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(start = 14.dp, top = 6.dp, bottom = 6.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(hoja.titulo, style = MaterialTheme.typography.titleMedium)
            Text(
                "Última actividad: ${formato.format(Date(hoja.fechaUltimaActividad))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onEliminarClick) {
            Icon(
                Icons.Default.DeleteOutline,
                contentDescription = "Eliminar hoja",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NuevaHojaDialog(onDismiss: () -> Unit, onCrear: (String) -> Unit) {
    var titulo by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva hoja") },
        text = {
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título de la hoja") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { if (titulo.isNotBlank()) onCrear(titulo) }) { Text("Crear") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
