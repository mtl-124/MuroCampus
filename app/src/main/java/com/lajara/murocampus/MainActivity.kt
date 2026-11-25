package com.lajara.murocampus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lajara.murocampus.data.PostRepositoryRemote
import com.lajara.murocampus.data.remote.RetrofitClient
import com.lajara.murocampus.ui.PantallaCrear
import com.lajara.murocampus.ui.PantallaDetalle
import com.lajara.murocampus.ui.PantallaLista
import com.lajara.murocampus.ui.theme.MuroCampusTheme
import com.lajara.murocampus.viewmodel.PostsViewModel
import com.lajara.murocampus.viewmodel.PostsViewModelFactory

class MainActivity : ComponentActivity() {

    private val vm: PostsViewModel by viewModels {
        PostsViewModelFactory(PostRepositoryRemote(RetrofitClient.api))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MuroCampusTheme {
                var idSeleccionado by rememberSaveable { mutableStateOf<Int?>(null) }
                var mostrandoCrear by rememberSaveable { mutableStateOf(false) }
                val id = idSeleccionado

                BackHandler(enabled = id != null || mostrandoCrear) {
                    if (mostrandoCrear) {
                        mostrandoCrear = false
                        vm.limpiarCrear()
                    } else {
                        idSeleccionado = null
                    }
                }

                when {
                    mostrandoCrear -> PantallaCrear(vm = vm)

                    id != null -> PantallaDetalle(vm = vm, id = id)

                    else -> Box(modifier = Modifier.fillMaxSize()) {
                        PantallaLista(
                            vm = vm,
                            onPostClick = { postId ->
                                vm.abrir(postId)
                                idSeleccionado = postId
                            }
                        )
                        FloatingActionButton(
                            onClick = {
                                vm.limpiarCrear()
                                mostrandoCrear = true
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .navigationBarsPadding()
                                .padding(16.dp)
                        ) {
                            Text("+", fontSize = 26.sp)
                        }
                    }
                }
            }
        }
    }
}