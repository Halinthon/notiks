package com.notiks.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notiks.app.data.Cuaderno

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuadernosScreen(
    viewModel: NotiksViewModel,
    onAbrirCuaderno: (Long, String) -> Unit,
    onExportar: () -> Unit
) {
    val cuadernos by viewModel.cuadernos.collectAsState(initial = emptyList())
    var mostrarDialogo by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notiks", fontWeight = FontWeight.Bold) },
                actions = {
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
                    CuadernoCard(cuaderno = cuaderno, onClick = { onAbrirCuaderno(cuaderno.id, cuaderno.nombre) })
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
}

@Composable
private fun CuadernoCard(cuaderno: Cuaderno, onClick: () -> Unit) {
    val color = try { Color(android.graphics.Color.parseColor(cuaderno.colorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(16.dp),
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
        Text(cuaderno.nombre, style = MaterialTheme.typography.titleMedium)
    }
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
