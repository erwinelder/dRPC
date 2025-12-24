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

## Examples

### Interface definition

```kotlin
@Serializable
sealed class AuthError {
    @Serializable object ServiceNotAvailable : NodeError()
    @Serializable object InvalidCredentials : AuthError()
    @Serializable object TokenExpired : AuthError()
}

interface AuthService {
    
    context(ctx: DrpcContext)
    suspend fun health(): SimpleResult<AuthError>
    
    context(ctx: DrpcContext)
    suspend fun signIn(username: String, password: String): ResultData<User, AuthError>
    
    context(ctx: DrpcContext)
    suspend fun verifyToken(token: String): SimpleResult<AuthError>
    
}
```

### Calling from code

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
