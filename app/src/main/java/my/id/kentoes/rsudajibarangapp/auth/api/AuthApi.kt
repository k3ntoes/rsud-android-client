package my.id.kentoes.rsudajibarangapp.auth.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import my.id.kentoes.rsudajibarangapp.core.model.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("expires_in")
    val expiresIn: Long? = null
)

@Serializable
data class RefreshRequest(
    @SerialName("refresh_token")
    val refreshToken: String
)

@Serializable
data class RefreshResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("expires_in")
    val expiresIn: Long? = null
)

interface AuthApi {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    @POST("refresh")
    suspend fun refresh(@Body request: RefreshRequest): ApiResponse<RefreshResponse>

    @POST("logout")
    suspend fun logout(@Body request: RefreshRequest): ApiResponse<Unit>
}
