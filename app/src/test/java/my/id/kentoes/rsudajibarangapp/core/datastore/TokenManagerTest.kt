package my.id.kentoes.rsudajibarangapp.core.datastore

import androidx.datastore.core.DataStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TokenManagerTest {

    private lateinit var dataStore: DataStore<TokenData>
    private lateinit var tokenManager: TokenManager
    private val tokenFlow = MutableStateFlow(TokenData())

    @Before
    fun setup() {
        dataStore = mockk(relaxed = true)
        tokenManager = TokenManager(dataStore)

        // Default: DataStore returns empty token
        every { dataStore.data } returns tokenFlow
        coEvery { dataStore.updateData(any()) } coAnswers {
            val transform = firstArg<suspend (TokenData) -> TokenData>()
            tokenFlow.value = transform(tokenFlow.value)
            tokenFlow.value // Return the updated value (method signature expects T)
        }
    }

    @Test
    fun `getAccessToken returns null when no token saved`() = runTest {
        tokenFlow.value = TokenData()
        assertNull(tokenManager.getAccessToken())
    }

    @Test
    fun `getAccessToken returns token after saveTokens`() = runTest {
        tokenManager.saveTokens("access123", "refresh456")
        assertEquals("access123", tokenManager.getAccessToken())
    }

    @Test
    fun `getRefreshToken returns token after saveTokens`() = runTest {
        tokenManager.saveTokens("access123", "refresh456")
        assertEquals("refresh456", tokenManager.getRefreshToken())
    }

    @Test
    fun `clearTokens resets access and refresh to null`() = runTest {
        tokenManager.saveTokens("access123", "refresh456")
        tokenManager.clearTokens()
        assertNull(tokenManager.getAccessToken())
        assertNull(tokenManager.getRefreshToken())
    }

    @Test
    fun `isLoggedIn returns false when no token`() = runTest {
        tokenFlow.value = TokenData()
        assertFalse(tokenManager.isLoggedIn())
    }

    @Test
    fun `isLoggedIn returns true after saveTokens`() = runTest {
        tokenManager.saveTokens("access123", "refresh456")
        assertTrue(tokenManager.isLoggedIn())
    }

    @Test
    fun `saveTokens calls updateData on DataStore`() = runTest {
        tokenManager.saveTokens("tok1", "tok2")
        coVerify { dataStore.updateData(any()) }
    }

    @Test
    fun `clearTokens calls updateData on DataStore`() = runTest {
        tokenManager.clearTokens()
        coVerify { dataStore.updateData(any()) }
    }

    @Test
    fun `saveTokens with blank token returns null on getAccessToken`() = runTest {
        tokenManager.saveTokens("", "refresh456")
        assertNull(tokenManager.getAccessToken())
    }

    @Test
    fun `multiple saveTokens overwrites previous values`() = runTest {
        tokenManager.saveTokens("first", "old-refresh")
        tokenManager.saveTokens("second", "new-refresh")
        assertEquals("second", tokenManager.getAccessToken())
        assertEquals("new-refresh", tokenManager.getRefreshToken())
    }
}
