package com.notiks.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.notiks.app.ui.CuadernosScreen
import com.notiks.app.ui.HojaDetailScreen
import com.notiks.app.ui.HojasScreen
import com.notiks.app.ui.NotiksViewModel
import com.notiks.app.ui.theme.NotiksTheme
import com.notiks.app.util.PreferenciasTema
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: NotiksViewModel by viewModels()

    // Lanzador para exportar el respaldo JSON a la ubicación que el usuario elija
    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            lifecycleScope.launch {
                val json = viewModel.exportarJson()
                contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var colorFondoHex by remember { mutableStateOf(PreferenciasTema.obtenerColorGuardado(this)) }
            val esOscuro = remember(colorFondoHex) { PreferenciasTema.esColorOscuro(colorFondoHex) }

            NotiksTheme(
                colorFondo = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(colorFondoHex)),
                esOscuro = esOscuro
            ) {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "cuadernos") {
                    composable("cuadernos") {
                        CuadernosScreen(
                            viewModel = viewModel,
                            onAbrirCuaderno = { id, nombre ->
                                navController.navigate("hojas/$id/$nombre")
                            },
                            onExportar = {
                                exportLauncher.launch("notiks_respaldo.json")
                            },
                            colorFondoActual = colorFondoHex,
                            onCambiarColorFondo = { nuevoColor ->
                                colorFondoHex = nuevoColor
                                PreferenciasTema.guardarColor(this@MainActivity, nuevoColor)
                            }
                        )
                    }
                    composable(
                        "hojas/{cuadernoId}/{nombre}",
                        arguments = listOf(navArgument("cuadernoId") { type = NavType.LongType })
                    ) { entry ->
                        val cuadernoId = entry.arguments?.getLong("cuadernoId") ?: 0L
                        val nombre = entry.arguments?.getString("nombre") ?: ""
                        HojasScreen(
                            viewModel = viewModel,
                            cuadernoId = cuadernoId,
                            nombreCuaderno = nombre,
                            onVolver = { navController.popBackStack() },
                            onAbrirHoja = { hojaId, titulo ->
                                navController.navigate("hoja/$hojaId/$titulo")
                            }
                        )
                    }
                    composable(
                        "hoja/{hojaId}/{titulo}",
                        arguments = listOf(navArgument("hojaId") { type = NavType.LongType })
                    ) { entry ->
                        val hojaId = entry.arguments?.getLong("hojaId") ?: 0L
                        val titulo = entry.arguments?.getString("titulo") ?: ""
                        HojaDetailScreen(
                            viewModel = viewModel,
                            hojaId = hojaId,
                            tituloHoja = titulo,
                            onVolver = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
