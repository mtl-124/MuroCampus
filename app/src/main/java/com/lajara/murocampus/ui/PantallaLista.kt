package com.lajara.murocampus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lajara.murocampus.viewmodel.PostsViewModel
import com.lajara.murocampus.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLista(
    vm: PostsViewModel,
    onPostClick: (Int) -> Unit = {}
) {
    val estado by vm.listaFiltrada.collectAsStateWithLifecycle()
    val filtro by vm.usuarioFiltro.collectAsStateWithLifecycle()
    val busqueda by vm.busqueda.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("MuroCampus", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B6CA8),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { vm.buscar(it) },
                label = { Text("Buscar por título") },
                leadingIcon = {
                    Text("🔍", fontSize = 16.sp)
                },
                trailingIcon = {
                    if (busqueda.isNotEmpty()) {
                        IconButton(onClick = { vm.buscar("") }) {
                            Text("✕", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filtro == null,
                        onClick = { vm.filtrar(null) },
                        label = { Text("Todos") }
                    )
                }
                items((1..10).toList()) { userId ->
                    FilterChip(
                        selected = filtro == userId,
                        onClick = { vm.filtrar(userId) },
                        label = { Text("Usuario $userId") }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (val e = estado) {
                    is UiState.Cargando -> Cargando()
                    is UiState.Error -> ErrorConReintento(
                        mensaje = e.mensaje,
                        onReintentar = { vm.cargar() }
                    )
                    is UiState.Exito -> {
                        if (e.datos.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                val mensajeVacio = if (busqueda.trim().isNotEmpty()) {
                                    "Sin resultados para la búsqueda"
                                } else {
                                    "Sin publicaciones"
                                }
                                Text(mensajeVacio, color = Color(0xFF555555))
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(e.datos, key = { it.id }) { post ->
                                    TarjetaPost(
                                        post = post,
                                        onClick = { onPostClick(post.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}