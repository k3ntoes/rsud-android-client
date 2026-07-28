package my.id.kentoes.rsudajibarangapp.auth

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import my.id.kentoes.rsudajibarangapp.auth.api.AuthApi
import my.id.kentoes.rsudajibarangapp.auth.api.LogoutRequest
import my.id.kentoes.rsudajibarangapp.auth.api.RefreshRequest
import my.id.kentoes.rsudajibarangapp.auth.api.TokenResponse
import my.id.kentoes.rsudajibarangapp.auth.api.UserOut
import my.id.kentoes.rsudajibarangapp.core.datastore.TokenManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryTest {

    private val authApi = mockk<AuthApi>(relaxed = true)
    private val tokenManager = mockk<TokenManager>(relaxed = true)
    private lateinit var repository: AuthRepository

    @Before
    fun setup() {
        repository = AuthRepository(authApi, tokenManager)
    }

    // ── init() ──

    @Test
    fun `init sets Unauthenticated when no token`() = runTest {
        coEvery { tokenManager.isLoggedIn() } returns false
        repository.init()
        assertTrue(repository.authState.value is AuthState.Unauthenticated)
    }

    @Test
    fun `init sets Authenticated when token exists`() = runTest {
        coEvery { tokenManager.isLoggedIn() } returns true
        coEvery { tokenManager.getUser() } returns sampleUser
        coEvery { authApi.me() } returns sampleUser
        repository.init()
        assertTrue(repository.authState.value is AuthState.Authenticated)
        assertEquals(sampleUser, repository.currentUser.value)
    }

    // ── login() ──

    private val sampleUser = UserOut(id = 1, username = "user", role = "inspector", isActive = true)

    @Test
    fun `login success saves tokens and user`() = runTest {
        val response = TokenResponse(accessToken = "at1", refreshToken = "rt1", user = sampleUser)
        coEvery { authApi.login(any()) } returns response
        coEvery { tokenManager.saveTokens(any(), any()) } just runs
        coEvery { tokenManager.saveUser(any()) } just runs

        val result = repository.login("user", "pass")

        assertTrue(result)
        coVerify { tokenManager.saveTokens("at1", "rt1") }
        coVerify { tokenManager.saveUser(sampleUser) }
        assertEquals(sampleUser, repository.currentUser.value)
        assertTrue(repository.authState.value is AuthState.Authenticated)
    }

    @Test
    fun `login failure returns false and sets Error state`() = runTest {
        coEvery { authApi.login(any()) } throws RuntimeException("Login gagal")

        val result = repository.login("user", "wrong")

        assertFalse(result)
        assertEquals(
            AuthState.Error("Login gagal"),
            repository.authState.value
        )
    }

    @Test
    fun `login network error returns false with error message`() = runTest {
        coEvery { authApi.login(any()) } throws RuntimeException("Connection timeout")

        val result = repository.login("user", "pass")

        assertFalse(result)
        assertEquals(
            AuthState.Error("Connection timeout"),
            repository.authState.value
        )
    }

    @Test
    fun `login sends correct LoginRequest`() = runTest {
        coEvery { authApi.login(any()) } returns TokenResponse(
            accessToken = "at", refreshToken = "rt", user = sampleUser
        )
        coEvery { tokenManager.saveTokens(any(), any()) } just runs

        repository.login("testuser", "testpass")

        coVerify {
            authApi.login(
                withArg { req ->
                    assertEquals("testuser", req.username)
                    assertEquals("testpass", req.password)
                }
            )
        }
    }

    // ── refreshToken() ──

    @Test
    fun `refreshToken returns false when no refresh token`() = runTest {
        coEvery { tokenManager.getRefreshToken() } returns null

        val result = repository.refreshToken()

        assertFalse(result)
    }

    @Test
    fun `refreshToken success saves new access token`() = runTest {
        coEvery { tokenManager.getRefreshToken() } returns "old-refresh"
        coEvery { authApi.refresh(any()) } returns TokenResponse(
            accessToken = "new-at", refreshToken = "new-rt", user = sampleUser
        )
        coEvery { tokenManager.saveTokens(any(), any()) } just runs

        val result = repository.refreshToken()

        assertTrue(result)
        // refresh token tetap sama, access token baru
        coVerify { tokenManager.saveTokens("new-at", "old-refresh") }
    }

    @Test
    fun `refreshToken failure calls forceLogout`() = runTest {
        coEvery { tokenManager.getRefreshToken() } returns "old-refresh"
        coEvery { authApi.refresh(any()) } throws RuntimeException("Refresh gagal")
        coEvery { tokenManager.clearTokens() } just runs

        val result = repository.refreshToken()

        assertFalse(result)
        coVerify { tokenManager.clearTokens() }
        assertTrue(repository.authState.value is AuthState.Unauthenticated)
    }

    @Test
    fun `refreshToken exception calls forceLogout`() = runTest {
        coEvery { tokenManager.getRefreshToken() } returns "old-refresh"
        coEvery { authApi.refresh(any()) } throws RuntimeException("Network error")
        coEvery { tokenManager.clearTokens() } just runs

        val result = repository.refreshToken()

        assertFalse(result)
        coVerify { tokenManager.clearTokens() }
    }

    // ── logout() ──

    @Test
    fun `logout calls logout API then clears tokens`() = runTest {
        coEvery { tokenManager.getRefreshToken() } returns "rt"
        coEvery { tokenManager.getAccessToken() } returns "at"
        coEvery { authApi.logout(any()) } returns Unit
        coEvery { tokenManager.clearTokens() } just runs

        repository.logout()

        coVerify { authApi.logout(LogoutRequest("rt", "at")) }
        coVerify { tokenManager.clearTokens() }
        assertTrue(repository.authState.value is AuthState.Unauthenticated)
    }

    @Test
    fun `logout clears tokens even when no refresh token`() = runTest {
        coEvery { tokenManager.getRefreshToken() } returns null
        coEvery { tokenManager.clearTokens() } just runs

        repository.logout()

        coVerify { tokenManager.clearTokens() }
        assertTrue(repository.authState.value is AuthState.Unauthenticated)
    }

    @Test
    fun `logout still clears tokens even if API fails`() = runTest {
        coEvery { tokenManager.getRefreshToken() } returns "rt"
        coEvery { tokenManager.getAccessToken() } returns "at"
        coEvery { authApi.logout(any()) } throws RuntimeException("Server down")
        coEvery { tokenManager.clearTokens() } just runs

        repository.logout()

        coVerify { authApi.logout(LogoutRequest("rt", "at")) }
        coVerify { tokenManager.clearTokens() }
        assertTrue(repository.authState.value is AuthState.Unauthenticated)
    }

    // ── forceLogout() ──

    @Test
    fun `forceLogout clears tokens and sets Unauthenticated`() = runTest {
        coEvery { tokenManager.clearTokens() } just runs

        repository.forceLogout()

        coVerify { tokenManager.clearTokens() }
        assertTrue(repository.authState.value is AuthState.Unauthenticated)
    }

    // ── getAccessToken() ──

    @Test
    fun `getAccessToken delegates to TokenManager`() = runTest {
        coEvery { tokenManager.getAccessToken() } returns "my-token"

        val result = repository.getAccessToken()

        assertEquals("my-token", result)
    }
}
