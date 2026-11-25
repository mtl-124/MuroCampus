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



## Prompt 3


