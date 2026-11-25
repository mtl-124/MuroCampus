# Prompts Usados

## Prompt 1

Quiero implementar la mejora "Encabezado con interceptor" en mi aplicación Android MuroCampus.

CONTEXTO DEL PROYECTO
MuroCampus es una app Android hecha con Kotlin, Jetpack Compose y Material 3. Consume el servicio web REST de prueba JSONPlaceholder (https://jsonplaceholder.typicode.com/) con Retrofit 2.11.0, OkHttp 4.12.0, logging-interceptor 4.12.0 y kotlinx.serialization. Usa arquitectura MVVM + Repository. El paquete base es com.lajara.murocampus.

Estructura actual:
- MainActivity.kt
- model/Modelos.kt: data class Usuario, Post(userId, id, title, body), Comentario(postId, id, name, email, body) y NuevoPost(userId, title, body), todas con @Serializable.
- data/remote/PostApi.kt: interface de Retrofit con las funciones suspend listar(), detalle(id), comentarios(id), porUsuario(userId) y crear(nuevo).
- data/remote/RetrofitClient.kt: object que crea el cliente una sola vez.
- data/PostRepository.kt: interface PostRepository y su implementación PostRepositoryRemote(api: PostApi), que devuelve Result y relanza CancellationException.
- viewmodel/PostViewModel.kt: contiene la clase PostsViewModel(repo: PostRepository), con StateFlow para la lista, el filtro, el detalle y la creación.
- viewmodel/UiState.kt: sealed interface UiState con Cargando, Exito y Error, más la función mensajeDe(e: Throwable).
- viewmodel/PostsViewModelFactory.kt
- ui/: Componentes.kt, PantallaLista.kt, PantallaDetalle.kt, PantallaCrear.kt y theme/.

Estado actual de RetrofitClient.kt:
- Es un object con un val privado json = Json { ignoreUnknownKeys = true }.
- Un OkHttpClient privado llamado "cliente", construido con OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }).build().
- Un val api: PostApi by lazy que construye Retrofit con baseUrl("https://jsonplaceholder.typicode.com/"), el cliente y el conversor json.asConverterFactory("application/json".toMediaType()).

OBJETIVO DE LA MEJORA
Agregar un Interceptor de OkHttp que incluya el encabezado "Accept: application/json" en TODAS las peticiones que haga la aplicación (GET, POST y cualquier otra que se agregue después).

COMPORTAMIENTO ESPERADO
- Cada petición saliente debe llevar el encabezado Accept con el valor application/json.
- El encabezado debe verse en Logcat dentro de las líneas del HttpLoggingInterceptor, por lo que el nuevo interceptor debe registrarse ANTES que el de logging.
- Debe usar header() y no addHeader(), para reemplazar cualquier valor previo de Accept en lugar de duplicarlo.
- El resto del cliente debe seguir igual: la URL base, el conversor JSON, el nivel BODY del logging y el inicializador by lazy.

ARCHIVOS Y CAPAS QUE DEBEN CAMBIAR
- Solo data/remote/RetrofitClient.kt.
- NO debe cambiar PostApi.kt, PostRepository.kt, ningún ViewModel, ninguna pantalla ni MainActivity.kt. Esta mejora no afecta a las capas View, ViewModel ni Repository.

ANTES DE MODIFICAR EL CÓDIGO
1. Revisa la estructura actual del proyecto.
2. Identifica los archivos y capas que deben cambiar.
3. Explica brevemente qué cambios propones y por qué el interceptor debe ir antes del de logging.

REGLAS OBLIGATORIAS
- La pantalla solo debe comunicarse con el ViewModel.
- El ViewModel solo debe comunicarse con la interface del Repository.
- No coloques Retrofit, PostApi ni HttpException en el ViewModel.
- El Repository debe encargarse de las operaciones de datos y devolver Result.
- No realices llamadas a la API desde un Composable.
- No utilices GlobalScope.
- No uses direcciones http:// sin necesidad: la URL base debe seguir siendo https://.
- No dejes ningún bloque catch vacío que oculte excepciones.
- Conserva todo el código existente que no necesite cambios y no reformatees ni renombres nada que no sea parte de la mejora.

FORMATO DE LA RESPUESTA
Entrégame el archivo RetrofitClient.kt completo, con todos los imports necesarios, listo para pegar. Después explica en prose, sin comentarios dentro del código, qué hace cada parte del interceptor.

CÓMO PROBARLA
Explícame cómo comprobar en Logcat, filtrando por okhttp, que la línea "Accept: application/json" aparece en las peticiones.

Implementa únicamente esta mejora.
No continúes con otra mejora hasta que revise y pruebe esta.

## Prompt 2

Quiero implementar la mejora "Búsqueda por título" en mi aplicación Android MuroCampus.

CONTEXTO DEL PROYECTO
MuroCampus es una app Android hecha con Kotlin, Jetpack Compose y Material 3. Consume el servicio web REST de prueba JSONPlaceholder con Retrofit, OkHttp y kotlinx.serialization. Usa arquitectura MVVM + Repository. El paquete base es com.lajara.murocampus. Ya existe un Interceptor de OkHttp que agrega el encabezado Accept en RetrofitClient.kt (mejora anterior terminada y subida).

Estructura actual:
- MainActivity.kt: maneja la navegación con variables de estado (idSeleccionado: Int? y mostrandoCrear: Boolean) y BackHandler. Muestra PantallaLista, PantallaDetalle o PantallaCrear según el estado, y un FloatingActionButton con el texto "+" sobre la lista.
- model/Modelos.kt: data class Post(userId, id, title, body), Comentario, NuevoPost y Usuario, todas @Serializable.
- data/remote/PostApi.kt y data/remote/RetrofitClient.kt
- data/PostRepository.kt: interface PostRepository con listar(), detalle(id), comentarios(id), porUsuario(userId) y crear(nuevo), todas suspend y devolviendo Result. Implementada por PostRepositoryRemote.
- viewmodel/PostViewModel.kt: contiene la clase PostsViewModel(private val repo: PostRepository). Expone:
    - lista: StateFlow<UiState<List<Post>>> (privado _lista, empieza en UiState.Cargando)
    - usuarioFiltro: StateFlow<Int?> (null significa Todos)
    - detalle: StateFlow<UiState<DetalleUi>>
    - crear: StateFlow<UiState<Post>?>
    - funciones cargar(), filtrar(userId: Int?), abrir(id: Int), publicar(titulo, texto) y limpiarCrear().
    - cargar() pone Cargando, llama a repo.listar() o repo.porUsuario(userId) según el filtro y convierte el Result en UiState con onSuccess y onFailure.
- viewmodel/UiState.kt: sealed interface UiState<out T> con Cargando, Exito(datos) y Error(mensaje), más mensajeDe(e: Throwable).
- ui/Componentes.kt: EtiquetaUsuario, TarjetaPost(post, onClick), Cargando() y ErrorConReintento(mensaje, onReintentar).
- ui/PantallaLista.kt: función PantallaLista(vm: PostsViewModel, onPostClick: (Int) -> Unit). Tiene una TopAppBar azul con el título "MuroCampus", una fila de FilterChip dentro de un LazyRow (Todos y Usuario 1 a 10) y debajo un when sobre el UiState: Cargando, Error con Reintentar y Exito. En Exito muestra "Sin publicaciones" si la lista está vacía o una LazyColumn de TarjetaPost. Lee el estado con collectAsStateWithLifecycle().
- ui/PantallaDetalle.kt y ui/PantallaCrear.kt

OBJETIVO DE LA MEJORA
Agregar un campo de búsqueda en la pantalla de lista que filtre por título las publicaciones que ya están descargadas. La búsqueda NO debe hacer una nueva petición a la red: trabaja sobre la lista que el ViewModel ya tiene en memoria.

COMPORTAMIENTO ESPERADO
- Un OutlinedTextField con la etiqueta "Buscar por título" y un icono o botón para limpiar el texto, ubicado entre la barra superior y la fila de chips de usuario.
- Mientras el usuario escribe, la lista se filtra en vivo, comparando el texto con el título de cada publicación sin distinguir mayúsculas de minúsculas y ignorando espacios al inicio y al final.
- Si el campo está vacío, se muestra la lista completa del usuario elegido en los chips.
- La búsqueda debe combinarse con el filtro por usuario: si está elegido "Usuario 2" y se escribe un texto, se buscan coincidencias solo entre las publicaciones del usuario 2.
- Si no hay coincidencias, mostrar el mensaje "Sin resultados para la búsqueda", distinto del mensaje "Sin publicaciones" que ya existe.
- Los estados Cargando y Error deben seguir funcionando igual; la búsqueda solo afecta al estado Exito.
- El texto de búsqueda debe sobrevivir a un giro de pantalla, por lo que debe vivir en el ViewModel y no en un remember dentro del Composable.
- Al cambiar de chip de usuario, el texto de búsqueda se conserva y se vuelve a aplicar sobre la nueva lista.

ARCHIVOS Y CAPAS QUE DEBEN CAMBIAR
- viewmodel/PostViewModel.kt (clase PostsViewModel): agregar un StateFlow<String> para la consulta, una función para actualizarla y un StateFlow derivado que combine la lista descargada con la consulta (por ejemplo con combine y stateIn en viewModelScope, o con un map sobre los flujos).
- ui/PantallaLista.kt: agregar el campo de búsqueda y leer la lista filtrada del ViewModel.
- NO debe cambiar PostApi.kt, RetrofitClient.kt, PostRepository.kt, UiState.kt ni el modelo. Esta mejora solo modifica ViewModel y View, no el Repository ni la API.

ANTES DE MODIFICAR EL CÓDIGO
1. Revisa la estructura actual del proyecto.
2. Identifica los archivos y capas que deben cambiar.
3. Explica brevemente qué cambios propones y por qué la lógica de filtrado debe vivir en el ViewModel y no en el Composable.

REGLAS OBLIGATORIAS
- La pantalla solo debe comunicarse con el ViewModel.
- El ViewModel solo debe comunicarse con la interface del Repository.
- No coloques Retrofit, PostApi ni HttpException en el ViewModel.
- El Repository debe encargarse de las operaciones de datos y devolver Result.
- No realices llamadas a la API desde un Composable.
- No utilices GlobalScope; usa viewModelScope.
- No uses direcciones http:// sin necesidad.
- No dejes ningún bloque catch vacío que oculte excepciones.
- No dupliques la lista de publicaciones en un segundo estado mutable que pueda quedar desincronizado: deriva la lista filtrada de la lista original.
- Conserva todo el código existente que no necesite cambios (chips, estados, navegación, botón +) y no renombres funciones ni archivos existentes.

FORMATO DE LA RESPUESTA
Entrégame los archivos modificados COMPLETOS (PostViewModel.kt y PantallaLista.kt), con todos los imports necesarios, listos para pegar. Después explica en prose, sin comentarios dentro del código, cómo fluye el texto desde el campo hasta el ViewModel y de vuelta a la pantalla.

CÓMO PROBARLA
Explícame una lista de pruebas manuales: buscar una palabra que exista, buscar una que no exista, combinar con un chip de usuario, borrar el texto, girar la pantalla con texto escrito y comprobar en Logcat que NO se hace una nueva petición HTTP al escribir.

Implementa únicamente esta mejora.
No continúes con otra mejora hasta que revise y pruebe esta.

## Prompt 3


