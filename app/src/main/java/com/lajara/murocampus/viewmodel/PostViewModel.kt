package com.lajara.murocampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lajara.murocampus.data.PostRepository
import com.lajara.murocampus.model.Comentario
import com.lajara.murocampus.model.NuevoPost
import com.lajara.murocampus.model.Post
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetalleUi(
    val post: Post,
    val comentarios: List<Comentario>
)

class PostsViewModel(private val repo: PostRepository) : ViewModel() {

    private val _lista = MutableStateFlow<UiState<List<Post>>>(UiState.Cargando)
    val lista: StateFlow<UiState<List<Post>>> = _lista.asStateFlow()

    private val _usuarioFiltro = MutableStateFlow<Int?>(null)
    val usuarioFiltro: StateFlow<Int?> = _usuarioFiltro.asStateFlow()

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    val listaFiltrada: StateFlow<UiState<List<Post>>> = combine(_lista, _busqueda) { estado, query ->
        when (estado) {
            is UiState.Cargando -> UiState.Cargando
            is UiState.Error -> estado
            is UiState.Exito -> {
                val q = query.trim()
                if (q.isEmpty()) {
                    estado
                } else {
                    val postsFiltrados = estado.datos.filter { post ->
                        post.title.contains(q, ignoreCase = true)
                    }
                    UiState.Exito(postsFiltrados)
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Cargando
    )

    private val _detalle = MutableStateFlow<UiState<DetalleUi>>(UiState.Cargando)
    val detalle: StateFlow<UiState<DetalleUi>> = _detalle.asStateFlow()

    private val _crear = MutableStateFlow<UiState<Post>?>(null)
    val crear: StateFlow<UiState<Post>?> = _crear.asStateFlow()

    private val _eliminacion = MutableStateFlow<UiState<Unit>?>(null)
    val eliminacion: StateFlow<UiState<Unit>?> = _eliminacion.asStateFlow()

    init {
        cargar()
    }

    fun buscar(query: String) {
        _busqueda.value = query
    }

    fun cargar() {
        viewModelScope.launch {
            _lista.value = UiState.Cargando
            val userId = _usuarioFiltro.value
            val resultado = if (userId == null) repo.listar() else repo.porUsuario(userId)
            resultado
                .onSuccess { _lista.value = UiState.Exito(it) }
                .onFailure { _lista.value = UiState.Error(mensajeDe(it)) }
        }
    }

    fun filtrar(userId: Int?) {
        _usuarioFiltro.value = userId
        cargar()
    }

    fun abrir(id: Int) {
        viewModelScope.launch {
            _detalle.value = UiState.Cargando
            coroutineScope {
                val post = async { repo.detalle(id) }
                val comentarios = async { repo.comentarios(id) }
                val resultadoPost = post.await()
                val resultadoComentarios = comentarios.await()
                val error = resultadoPost.exceptionOrNull()
                    ?: resultadoComentarios.exceptionOrNull()
                _detalle.value = if (error != null) {
                    UiState.Error(mensajeDe(error))
                } else {
                    UiState.Exito(
                        DetalleUi(
                            post = resultadoPost.getOrThrow(),
                            comentarios = resultadoComentarios.getOrThrow()
                        )
                    )
                }
            }
        }
    }

    fun publicar(titulo: String, texto: String) {
        viewModelScope.launch {
            _crear.value = UiState.Cargando
            repo.crear(NuevoPost(userId = 1, title = titulo, body = texto))
                .onSuccess { _crear.value = UiState.Exito(it) }
                .onFailure { _crear.value = UiState.Error(mensajeDe(it)) }
        }
    }

    fun limpiarCrear() {
        _crear.value = null
    }

    fun eliminar(id: Int) {
        viewModelScope.launch {
            _eliminacion.value = UiState.Cargando
            repo.eliminar(id)
                .onSuccess {
                    val actual = _lista.value
                    if (actual is UiState.Exito) {
                        _lista.value = UiState.Exito(actual.datos.filter { it.id != id })
                    }
                    _eliminacion.value = UiState.Exito(Unit)
                }
                .onFailure {
                    _eliminacion.value = UiState.Error(mensajeDe(it))
                }
        }
    }

    fun limpiarEliminacion() {
        _eliminacion.value = null
    }
}