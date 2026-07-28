package my.id.kentoes.rsudajibarangapp.auth.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class UserOut(
    val id: Int,
    val username: String,
    val role: String,
    @SerialName("is_active")
    val isActive: Boolean
)

@Serializable
data class TokenResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    val user: UserOut
)

@Serializable
data class RefreshRequest(
    @SerialName("refresh_token")
    val refreshToken: String
)

@Serializable
data class LogoutRequest(
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("access_token")
    val accessToken: String
)

@Serializable
data class ChangePasswordRequest(
    @SerialName("old_password")
    val oldPassword: String,
    @SerialName("new_password")
    val newPassword: String
)

interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): TokenResponse

    @POST("auth/logout")
    suspend fun logout(@Body request: LogoutRequest): Unit

    @GET("auth/me")
    suspend fun me(): UserOut

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Unit
}
