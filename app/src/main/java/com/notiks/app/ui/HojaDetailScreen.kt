package com.notiks.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.notiks.app.data.Item
import com.notiks.app.data.Origen
import com.notiks.app.util.OrigenDetector
import java.text.SimpleDateFormat
import java.util.*

/** Formas en que el usuario puede organizar los artículos guardados dentro de una Hoja. */
private enum class OrdenItems(val etiqueta: String) {
    FECHA_ANTIGUA("Fecha: más antiguos primero"),
    FECHA_RECIENTE("Fecha: más recientes primero"),
    CALIFICACION_ALTA("Calificación: mejor primero"),
    CALIFICACION_BAJA("Calificación: peor primero")
}

private fun ordenar(items: List<Item>, orden: OrdenItems): List<Item> = when (orden) {
    OrdenItems.FECHA_ANTIGUA -> items.sortedBy { it.timestamp }
    OrdenItems.FECHA_RECIENTE -> items.sortedByDescending { it.timestamp }
    OrdenItems.CALIFICACION_ALTA -> items.sortedWith(compareByDescending<Item> { it.calificacion }.thenByDescending { it.timestamp })
    OrdenItems.CALIFICACION_BAJA -> items.sortedWith(compareBy<Item> { it.calificacion }.thenByDescending { it.timestamp })
}

private fun esOrdenPorFecha(orden: OrdenItems) =
    orden == OrdenItems.FECHA_ANTIGUA || orden == OrdenItems.FECHA_RECIENTE

private fun mismoDia(a: Calendar, b: Calendar) =
    a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

private fun etiquetaFecha(timestamp: Long): String {
    val dia = Calendar.getInstance().apply { timeInMillis = timestamp }
    val hoy = Calendar.getInstance()
    val ayer = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    return when {
        mismoDia(dia, hoy) -> "Hoy"
        mismoDia(dia, ayer) -> "Ayer"
        else -> SimpleDateFormat("d 'de' MMMM, yyyy", Locale("es")).format(dia.time)
    }
}

private fun etiquetaCalificacion(calificacion: Int): String = when (calificacion) {
    5 -> "★★★★★ · 5 estrellas"
    4 -> "★★★★ · 4 estrellas"
    3 -> "★★★ · 3 estrellas"
    2 -> "★★ · 2 estrellas"
    1 -> "★ · 1 estrella"
    else -> "Sin calificar"
}

private data class GrupoItems(val etiqueta: String, val items: List<Item>)

/**
 * Agrupa la lista ya ordenada por día (si el orden es por fecha) o por
 * cantidad de estrellas (si el orden es por calificación). Como la lista
 * ya viene ordenada, los ítems de un mismo grupo quedan contiguos.
 */
private fun agrupar(itemsOrdenados: List<Item>, orden: OrdenItems): List<GrupoItems> {
    val agrupado = if (esOrdenPorFecha(orden)) {
        itemsOrdenados.groupBy { etiquetaFecha(it.timestamp) }
    } else {
        itemsOrdenados.groupBy { etiquetaCalificacion(it.calificacion) }
    }
    return agrupado.map { (etiqueta, items) -> GrupoItems(etiqueta, items) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaDetailScreen(
    viewModel: NotiksViewModel,
    hojaId: Long,
    tituloHoja: String,
    onVolver: () -> Unit
) {
    val items by viewModel.itemsDe(hojaId).collectAsState(initial = emptyList())
    val context = LocalContext.current
    var itemAEliminar by remember { mutableStateOf<Item?>(null) }
    var orden by remember { mutableStateOf(OrdenItems.FECHA_ANTIGUA) }
    var mostrarMenuOrden by remember { mutableStateOf(false) }
    val itemsOrdenados = remember(items, orden) { ordenar(items, orden) }
    val grupos = remember(itemsOrdenados, orden) { agrupar(itemsOrdenados, orden) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tituloHoja) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { mostrarMenuOrden = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Organizar por")
                        }
                        DropdownMenu(
                            expanded = mostrarMenuOrden,
                            onDismissRequest = { mostrarMenuOrden = false }
                        ) {
                            OrdenItems.entries.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion.etiqueta) },
                                    leadingIcon = {
                                        if (opcion == orden) {
                                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    },
                                    onClick = {
                                        orden = opcion
                                        mostrarMenuOrden = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    "Comparte un enlace de YouTube, X, Instagram, Discover o cualquier web\ny elige esta hoja para verlo aquí.",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                grupos.forEach { grupo ->
                    item(key = "encabezado_${grupo.etiqueta}") {
                        Text(
                            grupo.etiqueta,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                        )
                    }
                    items(grupo.items, key = { it.id }) { item ->
                        ItemBubble(
                            item = item,
                            onClick = {
                                item.url?.let { url ->
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                }
                            },
                            onCompartir = { compartirItem(context, item) },
                            onEliminar = { itemAEliminar = item },
                            onCalificar = { estrellas -> viewModel.calificarItem(item, estrellas) }
                        )
                    }
                }
            }
        }
    }

    itemAEliminar?.let { item ->
        AlertDialog(
            onDismissRequest = { itemAEliminar = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null) },
            title = { Text("¿Eliminar este artículo?") },
            text = { Text("Se quitará de esta hoja. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarItem(item)
                    itemAEliminar = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { itemAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

private fun iconoDe(origen: Origen): ImageVector = when (origen) {
    Origen.YOUTUBE -> Icons.Default.PlayCircle
    Origen.X -> Icons.Default.Tag
    Origen.INSTAGRAM -> Icons.Default.Language
    Origen.DISCOVER -> Icons.Default.Search
    Origen.WEB -> Icons.Default.Link
    Origen.TEXTO -> Icons.Default.Notes
}

/** Abre el selector nativo de Android para reenviar un ítem guardado a cualquier app. */
private fun compartirItem(context: android.content.Context, item: Item) {
    val texto = buildString {
        append(item.resumen)
        if (item.url != null) {
            append("\n\n")
            append(item.url)
        }
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir con"))
}

@Composable
private fun ItemBubble(
    item: Item,
    onClick: () -> Unit,
    onCompartir: () -> Unit,
    onEliminar: () -> Unit,
    onCalificar: (Int) -> Unit
) {
    val formato = remember { SimpleDateFormat("d MMM, HH:mm", Locale("es")) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .clickable(enabled = item.url != null, onClick = onClick)
            .padding(start = 14.dp, top = 6.dp, bottom = 14.dp, end = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(iconoDe(item.origen), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                OrigenDetector.nombreLegible(item.origen),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onCompartir, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Compartir este artículo",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Eliminar este artículo",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(item.resumen, style = MaterialTheme.typography.bodyLarge)
        if (item.url != null) {
            Spacer(Modifier.height(4.dp))
            Text(item.url, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
        Spacer(Modifier.height(6.dp))
        CalificacionEstrellas(calificacion = item.calificacion, onCalificar = onCalificar)
        Spacer(Modifier.height(2.dp))
        Text(
            formato.format(Date(item.timestamp)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Fila de 1 a 5 estrellas para calificar qué tanto le interesó el artículo
 * al usuario. Tocar una estrella califica hasta ahí; tocar la misma
 * estrella que ya estaba marcada como última quita la calificación.
 */
@Composable
private fun CalificacionEstrellas(calificacion: Int, onCalificar: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        for (posicion in 1..5) {
            IconButton(
                onClick = { onCalificar(if (posicion == calificacion) 0 else posicion) },
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = if (posicion <= calificacion) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Calificar con $posicion estrella" + if (posicion != 1) "s" else "",
                    tint = if (posicion <= calificacion) com.notiks.app.ui.theme.NotiksAmarillo
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
