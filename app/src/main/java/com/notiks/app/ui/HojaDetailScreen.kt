package com.notiks.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import com.notiks.app.data.HojaConCuaderno
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

/** Granularidad con la que se agrupan los artículos cuando el orden es por fecha. */
private enum class Granularidad(val etiqueta: String) {
    DIA("Agrupar por día"),
    SEMANA("Agrupar por semana"),
    MES("Agrupar por mes")
}

private fun mismoDia(a: Calendar, b: Calendar) =
    a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

private fun inicioDeSemana(cal: Calendar): Calendar {
    val c = cal.clone() as Calendar
    // Retrocede hasta el lunes de esa semana (independiente del idioma/región del dispositivo).
    val diaSemana = c.get(Calendar.DAY_OF_WEEK) // domingo=1 ... sábado=7
    val diasDesdeElLunes = (diaSemana - Calendar.MONDAY + 7) % 7
    c.add(Calendar.DAY_OF_YEAR, -diasDesdeElLunes)
    c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0)
    return c
}

private fun etiquetaSemana(timestamp: Long): String {
    val lunes = inicioDeSemana(Calendar.getInstance().apply { timeInMillis = timestamp })
    val domingo = (lunes.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 6) }
    val lunesActual = inicioDeSemana(Calendar.getInstance())

    if (lunes.get(Calendar.YEAR) == lunesActual.get(Calendar.YEAR) &&
        lunes.get(Calendar.DAY_OF_YEAR) == lunesActual.get(Calendar.DAY_OF_YEAR)
    ) {
        return "Esta semana"
    }
    val semanaPasada = (lunesActual.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -7) }
    if (lunes.get(Calendar.YEAR) == semanaPasada.get(Calendar.YEAR) &&
        lunes.get(Calendar.DAY_OF_YEAR) == semanaPasada.get(Calendar.DAY_OF_YEAR)
    ) {
        return "Semana pasada"
    }

    val mismoMes = lunes.get(Calendar.MONTH) == domingo.get(Calendar.MONTH)
    val formatoDia = SimpleDateFormat("d", Locale("es"))
    val formatoDiaMes = SimpleDateFormat("d MMM", Locale("es"))
    val rango = if (mismoMes) {
        "${formatoDia.format(lunes.time)}–${formatoDiaMes.format(domingo.time)}"
    } else {
        "${formatoDiaMes.format(lunes.time)}–${formatoDiaMes.format(domingo.time)}"
    }
    return "Semana del $rango, ${domingo.get(Calendar.YEAR)}"
}

private fun etiquetaMes(timestamp: Long): String {
    val texto = SimpleDateFormat("MMMM yyyy", Locale("es")).format(Date(timestamp))
    return texto.replaceFirstChar { it.uppercase() }
}

private fun etiquetaFecha(timestamp: Long, granularidad: Granularidad): String = when (granularidad) {
    Granularidad.SEMANA -> etiquetaSemana(timestamp)
    Granularidad.MES -> etiquetaMes(timestamp)
    Granularidad.DIA -> {
        val dia = Calendar.getInstance().apply { timeInMillis = timestamp }
        val hoy = Calendar.getInstance()
        val ayer = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        when {
            mismoDia(dia, hoy) -> "Hoy"
            mismoDia(dia, ayer) -> "Ayer"
            else -> SimpleDateFormat("d 'de' MMMM, yyyy", Locale("es")).format(dia.time)
        }
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
 * Agrupa la lista ya ordenada por día/semana/mes (si el orden es por fecha)
 * o por cantidad de estrellas (si el orden es por calificación). Como la
 * lista ya viene ordenada, los ítems de un mismo grupo quedan contiguos.
 */
private fun agrupar(itemsOrdenados: List<Item>, orden: OrdenItems, granularidad: Granularidad): List<GrupoItems> {
    val agrupado = if (esOrdenPorFecha(orden)) {
        itemsOrdenados.groupBy { etiquetaFecha(it.timestamp, granularidad) }
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
    var itemAEditar by remember { mutableStateOf<Item?>(null) }
    var itemAMover by remember { mutableStateOf<Item?>(null) }
    var orden by remember { mutableStateOf(OrdenItems.FECHA_ANTIGUA) }
    var granularidad by remember { mutableStateOf(Granularidad.DIA) }
    var mostrarMenuOrden by remember { mutableStateOf(false) }
    var mostrarMenuGranularidad by remember { mutableStateOf(false) }
    var gruposColapsados by remember { mutableStateOf(setOf<String>()) }
    val itemsOrdenados = remember(items, orden) { ordenar(items, orden) }
    val grupos = remember(itemsOrdenados, orden, granularidad) { agrupar(itemsOrdenados, orden, granularidad) }
    val hojasConCuaderno by viewModel.hojasConCuaderno.collectAsState(initial = emptyList())

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
                    if (esOrdenPorFecha(orden)) {
                        Box {
                            IconButton(onClick = { mostrarMenuGranularidad = true }) {
                                Icon(Icons.Default.CalendarViewMonth, contentDescription = "Agrupar por")
                            }
                            DropdownMenu(
                                expanded = mostrarMenuGranularidad,
                                onDismissRequest = { mostrarMenuGranularidad = false }
                            ) {
                                Granularidad.entries.forEach { opcion ->
                                    DropdownMenuItem(
                                        text = { Text(opcion.etiqueta) },
                                        leadingIcon = {
                                            if (opcion == granularidad) {
                                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                                            }
                                        },
                                        onClick = {
                                            granularidad = opcion
                                            gruposColapsados = emptySet()
                                            mostrarMenuGranularidad = false
                                        }
                                    )
                                }
                            }
                        }
                    }
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
                    val colapsado = grupo.etiqueta in gruposColapsados
                    item(key = "encabezado_${grupo.etiqueta}") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    gruposColapsados = if (colapsado) {
                                        gruposColapsados - grupo.etiqueta
                                    } else {
                                        gruposColapsados + grupo.etiqueta
                                    }
                                }
                                .padding(top = 8.dp, bottom = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (colapsado) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                contentDescription = if (colapsado) "Expandir grupo" else "Contraer grupo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${grupo.etiqueta} (${grupo.items.size})",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (!colapsado) {
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
                                onEditar = { itemAEditar = item },
                                onMover = { itemAMover = item },
                                onCalificar = { estrellas -> viewModel.calificarItem(item, estrellas) }
                            )
                        }
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

    itemAEditar?.let { item ->
        EditarResumenDialog(
            resumenActual = item.resumen,
            onDismiss = { itemAEditar = null },
            onGuardar = { nuevoResumen ->
                viewModel.editarResumen(item, nuevoResumen)
                itemAEditar = null
            }
        )
    }

    itemAMover?.let { item ->
        MoverAHojaDialog(
            hojaActualId = hojaId,
            hojasConCuaderno = hojasConCuaderno,
            onDismiss = { itemAMover = null },
            onMover = { nuevaHojaId ->
                viewModel.moverItem(item, nuevaHojaId)
                itemAMover = null
            }
        )
    }
}

@Composable
private fun MoverAHojaDialog(
    hojaActualId: Long,
    hojasConCuaderno: List<HojaConCuaderno>,
    onDismiss: () -> Unit,
    onMover: (Long) -> Unit
) {
    val opciones = hojasConCuaderno.filter { it.id != hojaActualId }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.DriveFileMove, contentDescription = null) },
        title = { Text("Mover a otra hoja") },
        text = {
            if (opciones.isEmpty()) {
                Text("No tienes otra hoja disponible. Crea una nueva hoja primero para poder mover este artículo.")
            } else {
                Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                    opciones.forEach { hoja ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onMover(hoja.id) }
                                .padding(vertical = 10.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(hoja.titulo, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    hoja.nombreCuaderno,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun EditarResumenDialog(
    resumenActual: String,
    onDismiss: () -> Unit,
    onGuardar: (String) -> Unit
) {
    var texto by remember { mutableStateOf(resumenActual) }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Edit, contentDescription = null) },
        title = { Text("Editar resumen") },
        text = {
            OutlinedTextField(
                value = texto,
                onValueChange = { if (it.split(Regex("\\s+")).size <= 20) texto = it },
                label = { Text("Resumen (máx. 20 palabras)") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                enabled = texto.isNotBlank(),
                onClick = { onGuardar(texto.trim()) }
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
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
    onEditar: () -> Unit,
    onMover: () -> Unit,
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
            IconButton(onClick = onEditar, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar resumen",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onMover, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.DriveFileMove,
                    contentDescription = "Mover a otra hoja",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
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
