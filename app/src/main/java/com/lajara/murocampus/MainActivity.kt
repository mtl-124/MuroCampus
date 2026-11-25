package com.lajara.murocampus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.lajara.murocampus.data.PostRepositoryRemote
import com.lajara.murocampus.data.remote.RetrofitClient
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
                PantallaLista(vm = vm)
            }
        }
    }
}