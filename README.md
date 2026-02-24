# dRPC (Docta Remote Procedure Call)

Interacting with an application that uses dRPC for communication is intended to be done via service interfaces. Even though each service method can be called via HTTP endpoints.

To map service methods to HTTP endpoints, the following conventions are used:

- All endpoints have POST method (besides websocket endpoints).
- Endpoint path is formed as '{ServiceName}/{MethodName}'.
- Request and Response bodies are serialized/deserialized as JSON.
- If a method has no parameters, the request body is empty.
- if a method has parameters, they are passed as a JSON object in the request body, where keys are parameters indices (0-based).
- Response always contains 200 OK status code (if no transport or server errors occurred) and the body contains one of the following:
    - SimpleResult.Success - if the method executed successfully and has no return value.
    - SimpleResult.Error - if the method execution failed. The 'error' field contains error details.
    - ResultData.Success - if the method executed successfully and has a return value; the 'data' field contains the return value.
    - ResultData.Error - if the method execution failed. The 'error' field contains error details.

## Dependency
dRPC is published on Maven Central, so you can add it as a dependency in your project using the code below.

### Gradle
```kotlin
dependencies {
    // For both client and server APIs:
    implementation("io.github.erwinelder:drpc:0.4.0")
    ksp("io.github.erwinelder:drpc-processor:0.4.0")
    // For client API only:
    implementation("io.github.erwinelder:drpc-client:0.4.0")
    ksp("io.github.erwinelder:drpc-client-processor:0.4:0")
    // For server API only:
    implementation("io.github.erwinelder:drpc-server:0.4.0")
    ksp("io.github.erwinelder:drpc-server-processor:0.4:0")
}
```

### Gradle (version catalog)
```gradle
[versions]
drpc-version = "0.4.0"

[libraries]
# For both client and server APIs:
drpc = { module = "io.github.erwinelder:drpc", version.ref = "drpc-version" }
drpc-processor = { module = "io.github.erwinelder:drpc-processor", version.ref = "drpc-version" }
# For client API only:
drpc-client = { module = "io.github.erwinelder:drpc-client", version.ref = "drpc-version" }
drpc-client-processor = { module = "io.github.erwinelder:drpc-client-processor", version.ref = "drpc-version" }
# For server API only:
drpc-server = { module = "io.github.erwinelder:drpc-server", version.ref = "drpc-version" }
drpc-server-processor = { module = "io.github.erwinelder:drpc-server-processor", version.ref = "drpc-version" }
```
```kotlin
dependencies {
    // For both client and server APIs:
    implementation(libs.drpc)
    ksp(libs.drpc.processor)
    // For client API only:
    implementation(libs.drpc.client)
    ksp(libs.drpc.client.processor)
    // For server API only:
    implementation(libs.drpc.server)
    ksp(libs.drpc.server.processor)
}
```

## Examples

### Interface definition

```kotlin
// Define error class
@Serializable
sealed class AuthError {
    @Serializable object ServiceNotAvailable : NodeError()
    @Serializable object InvalidCredentials : AuthError()
    @Serializable object TokenExpired : AuthError()
}

// Annotate the service interface with @Rpc and specify the base HTTP URL for the service (essential)
@Rpc(serviceBaseHttpUrl = "http://0.0.0.0:8080")
interface AuthService {
    
    // Use context receiver for each method to get access to dRPC context in service methods (essential)
    context(ctx: DrpcContext)
    suspend fun health(): SimpleResult<AuthError>
    
    context(ctx: DrpcContext)
    suspend fun signIn(username: String, password: String): ResultData<User, AuthError>
    
    context(ctx: DrpcContext)
    suspend fun verifyToken(token: String): SimpleResult<AuthError>
    
}
```

### Calling from the client side

```kotlin
fun getUserData(username: String, password: String): ResultData<User, AuthError> {
    // Sign-in to get user data (get user object with success or return error)
    val user = callCatching {
        authService.login(username = "username", password = "password")
    }
        .getOrElse { return ResultData.Error(AuthError.ServiceNotAvailable) } // extract ResultData from Kotlin Result or return ServiceNotAvailable error
        .getOrElse { return ResultData.Error(it) } // extract User from ResultData or return error

    return ResultData.Success(data = user)
}

fun getUserData(username: String, password: String): User {
    // Sign-in to get user data (get user object with success or return error)
    val user = callCatching {
        authService.login(username = "username", password = "password")
    }
        .getOrElse { throw Exception("Auth service is not available") } // extract ResultData from Kotlin Result or throw an exception
        .getOrElse { throw Exception("Server error: $it") } // extract User from ResultData or return throw an exception

    return user
}

fun verifyUserToken(token: String): Boolean {
    // Verify token (return false if error is present)
    callCatching {
        authService.verifyToken(token = token)
    }
        .getOrElse { return false } // extract SimpleResult from Kotlin Result or return false
        .onError { return false } // return false if error is present

    return true // token is valid
}

fun checkAuthServiceHealth(): Boolean {
    // Check if auth service is healthy (return false if error is present)
    callCatching {
        authService.health()
    }
        .getOrElse { return false } // extract SimpleResult from Kotlin Result or return false
        .onError { return false } // return false if error is present

    return true // service is healthy
}
```

### Setting up the server side

```kotlin
fun main() {
    embeddedServer(
        factory = Netty,
        port = System.getenv("PORT")?.toIntOrNull() ?: 8080,
        host = "0.0.0.0",
        module = Application::appModule
    ).start(wait = true)
}

fun Application.appModule() {
    // ... set up your dependencies and other configurations
    
    // Configure content negotiation to use JSON serialization for request and response bodies
    configureSerialization()
    // Install dRPC plugin for Ktor in order to bind service interfaces to HTTP endpoints
    installDrpc()
    
    configureRouting()
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}

fun Application.configureRouting() {
    routing {
        // Register the service providing an actual implementation of the service interface (essential)
        registerService<AuthService> { AuthServiceImpl() }
    }
}
```

### Calling via HTTP
```
curl -X POST http://localhost:8080/AuthService/signIn \
    -H "Content-Type: application/json" \
    -d '{
        "0": "username",
        "1": "password"
    }'
    
curl -X POST http://localhost:8080/AuthService/verifyToken \
    -H "Content-Type: application/json" \
    -d '{
        "0": "token_string"
    }'
    
curl -X POST http://localhost:8080/AuthService/health \
    -H "Content-Type: application/json" \
    -d '{}'
```
