package my.id.kentoes.rsudajibarangapp.auth

import android.content.Context
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import my.id.kentoes.rsudajibarangapp.sync.SyncWorker
import org.junit.After
import org.junit.Before
import org.junit.Test

class AuthViewModelTest {

    private val context = mockk<Context>()
    private val authRepository = mockk<AuthRepository>()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Mock SyncWorker.enqueue agar tidak beneran panggil WorkManager
        mockkObject(SyncWorker.Companion)
        every { SyncWorker.enqueue(any()) } returns Unit
        // AuthViewModel.init memanggil authRepository.init() saat konstruksi
        coEvery { authRepository.init() } returns Unit
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init with restored session enqueues background sync`() = runTest(testDispatcher) {
        coEvery { authRepository.init() } returns Unit
        every { authRepository.authState } returns MutableStateFlow(AuthState.Authenticated())
        every { authRepository.currentUser } returns MutableStateFlow(null)

        AuthViewModel(context, authRepository)
        advanceUntilIdle()

        // Sesi valid saat app dibuka ulang → sync master data agar cache kosong terisi
        verify(exactly = 1) { SyncWorker.enqueue(context) }
    }

    @Test
    fun `init without session does not enqueue sync`() = runTest(testDispatcher) {
        coEvery { authRepository.init() } returns Unit
        every { authRepository.authState } returns MutableStateFlow(AuthState.Unauthenticated)
        every { authRepository.currentUser } returns MutableStateFlow(null)

        AuthViewModel(context, authRepository)
        advanceUntilIdle()

        verify(exactly = 0) { SyncWorker.enqueue(any()) }
    }

    @Test
    fun `login success enqueues background sync so master data loads`() = runTest(testDispatcher) {
        coEvery { authRepository.login("user", "pass") } returns true
        every { authRepository.authState } returns MutableStateFlow(AuthState.Unauthenticated)
        every { authRepository.currentUser } returns MutableStateFlow(null)

        val viewModel = AuthViewModel(context, authRepository)
        viewModel.onUsernameChanged("user")
        viewModel.onPasswordChanged("pass")
        viewModel.login()
        advanceUntilIdle()

        // Bug: tanpa enqueue SyncWorker di login, dashboard pertama kosong
        // (master data tidak pernah di-sync sampai user membuka daftar ruangan).
        verify(exactly = 1) { SyncWorker.enqueue(context) }
    }

    @Test
    fun `login failure does not enqueue sync`() = runTest(testDispatcher) {
        coEvery { authRepository.login(any(), any()) } returns false
        every { authRepository.authState } returns MutableStateFlow(AuthState.Error("Login gagal"))
        every { authRepository.currentUser } returns MutableStateFlow(null)

        val viewModel = AuthViewModel(context, authRepository)
        viewModel.onUsernameChanged("user")
        viewModel.onPasswordChanged("pass")
        viewModel.login()
        advanceUntilIdle()

        verify(exactly = 0) { SyncWorker.enqueue(any()) }
    }
}
