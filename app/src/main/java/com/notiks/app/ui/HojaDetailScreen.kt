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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tituloHoja) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
                items(items) { item ->
                    ItemBubble(
                        item = item,
                        onClick = {
                            item.url?.let { url ->
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                        },
                        onCompartir = { compartirItem(context, item) }
                    )
                }
            }
        }
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
private fun ItemBubble(item: Item, onClick: () -> Unit, onCompartir: () -> Unit) {
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
        }
        Spacer(Modifier.height(2.dp))
        Text(item.resumen, style = MaterialTheme.typography.bodyLarge)
        if (item.url != null) {
            Spacer(Modifier.height(4.dp))
            Text(item.url, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            formato.format(Date(item.timestamp)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
