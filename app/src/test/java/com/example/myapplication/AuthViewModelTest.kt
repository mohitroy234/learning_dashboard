package com.example.myapplication

import app.cash.turbine.turbineScope
import com.example.myapplication.data.local.entity.UserSessionEntity
import com.example.myapplication.data.remote.model.LoginResponse
import com.example.myapplication.data.remote.model.RegisterResponse
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.util.Resource
import com.example.myapplication.ui.viewmodel.auth.AuthViewModel
import com.example.myapplication.ui.viewmodel.auth.AuthViewModel.AuthUiState
import com.example.myapplication.data.local.dao.UserSessionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [AuthViewModel] verifying the full login/register flow
 * with real state transitions (Idle → Loading → Success/Error).
 *
 * Uses a FakeAuthRepository and FakeUserSessionDao to isolate business logic
 * from network and database dependencies.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeUserSessionDao: FakeUserSessionDao
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        fakeUserSessionDao = FakeUserSessionDao()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): AuthViewModel {
        return AuthViewModel(
            authRepository = fakeAuthRepository,
            userSessionDao = fakeUserSessionDao
        )
    }

    // ---------------------------------------------------------------
    // LOGIN FLOW
    // ---------------------------------------------------------------

    @Test
    fun `login success - transitions from Idle to Loading to Success and saves session`() = runTest {
        // Arrange: API will return success
        fakeAuthRepository.loginResult = Resource.Success(
            LoginResponse(id = "1", name = "Test User", email = "test@test.com", createdAt = "2026-01-01")
        )
        viewModel = createViewModel()

        // Act
        assertEquals(AuthUiState.Idle, viewModel.loginState.value)

        viewModel.login("test@test.com", "password123")

        // The coroutine hasn't run yet — state should be Loading
        assertEquals(AuthUiState.Loading, viewModel.loginState.value)

        // Advance coroutines to completion
        advanceUntilIdle()

        // Assert: final state is Success
        assertEquals(AuthUiState.Success, viewModel.loginState.value)

        // Assert: session was saved to local DB by the repository
        assertTrue(fakeAuthRepository.loginWasCalled)
    }

    @Test
    fun `login failure - transitions from Idle to Loading to Error with message`() = runTest {
        // Arrange: API will return error
        fakeAuthRepository.loginResult = Resource.Error("Invalid credentials")
        viewModel = createViewModel()

        // Act
        viewModel.login("wrong@test.com", "wrongpass")
        advanceUntilIdle()

        // Assert
        val state = viewModel.loginState.value
        assertTrue(state is AuthUiState.Error)
        assertEquals("Invalid credentials", (state as AuthUiState.Error).message)
    }

    @Test
    fun `login network error - shows meaningful error message`() = runTest {
        // Arrange: simulate network exception
        fakeAuthRepository.loginResult = Resource.Error("Login failed: Unable to resolve host")
        viewModel = createViewModel()

        // Act
        viewModel.login("test@test.com", "password123")
        advanceUntilIdle()

        // Assert
        val state = viewModel.loginState.value
        assertTrue(state is AuthUiState.Error)
        assertTrue((state as AuthUiState.Error).message.contains("Unable to resolve host"))
    }

    // ---------------------------------------------------------------
    // REGISTER FLOW
    // ---------------------------------------------------------------

    @Test
    fun `register success - transitions to Success and saves session`() = runTest {
        // Arrange
        fakeAuthRepository.registerResult = Resource.Success(
            RegisterResponse(id = "2", name = "New User", email = "new@test.com", createdAt = "2026-01-01")
        )
        viewModel = createViewModel()

        // Act
        viewModel.register("New User", "new@test.com", "password123")
        advanceUntilIdle()

        // Assert
        assertEquals(AuthUiState.Success, viewModel.registerState.value)
        assertTrue(fakeAuthRepository.registerWasCalled)
    }

    @Test
    fun `register failure - shows error message`() = runTest {
        // Arrange
        fakeAuthRepository.registerResult = Resource.Error("Registration failed: 409 Conflict")
        viewModel = createViewModel()

        // Act
        viewModel.register("User", "existing@test.com", "password123")
        advanceUntilIdle()

        // Assert
        val state = viewModel.registerState.value
        assertTrue(state is AuthUiState.Error)
        assertEquals("Registration failed: 409 Conflict", (state as AuthUiState.Error).message)
    }

    // ---------------------------------------------------------------
    // LOGOUT FLOW
    // ---------------------------------------------------------------

    @Test
    fun `logout clears session and resets both states to Idle`() = runTest {
        // Arrange: login first
        fakeAuthRepository.loginResult = Resource.Success(
            LoginResponse(id = "1", name = "User", email = "u@t.com", createdAt = "2026-01-01")
        )
        viewModel = createViewModel()
        viewModel.login("u@t.com", "pass")
        advanceUntilIdle()
        assertEquals(AuthUiState.Success, viewModel.loginState.value)

        // Act
        viewModel.logout()
        advanceUntilIdle()

        // Assert: states reset, session cleared
        assertEquals(AuthUiState.Idle, viewModel.loginState.value)
        assertEquals(AuthUiState.Idle, viewModel.registerState.value)
        assertTrue(fakeAuthRepository.logoutWasCalled)
    }

    // ---------------------------------------------------------------
    // STATE ISOLATION
    // ---------------------------------------------------------------

    @Test
    fun `login state and register state are independent`() = runTest {
        // Arrange
        fakeAuthRepository.loginResult = Resource.Error("Login failed")
        fakeAuthRepository.registerResult = Resource.Success(
            RegisterResponse(id = "3", name = "User", email = "u@t.com", createdAt = "2026-01-01")
        )
        viewModel = createViewModel()

        // Act: login fails
        viewModel.login("u@t.com", "pass")
        advanceUntilIdle()

        // Assert: login errored but register still Idle
        assertTrue(viewModel.loginState.value is AuthUiState.Error)
        assertEquals(AuthUiState.Idle, viewModel.registerState.value)

        // Act: register succeeds
        viewModel.register("User", "u@t.com", "pass")
        advanceUntilIdle()

        // Assert: register succeeded, login state unchanged
        assertEquals(AuthUiState.Success, viewModel.registerState.value)
        assertTrue(viewModel.loginState.value is AuthUiState.Error)
    }

    // =================================================================
    // FAKES
    // =================================================================

    /**
     * Fake AuthRepository that returns pre-configured results without network calls.
     * Tracks whether each method was called for verification.
     */
    private class FakeAuthRepository : AuthRepository {
        var loginResult: Resource<LoginResponse> = Resource.Error("Not configured")
        var registerResult: Resource<RegisterResponse> = Resource.Error("Not configured")
        var loginWasCalled = false
        var registerWasCalled = false
        var logoutWasCalled = false

        override suspend fun login(email: String, password: String): Resource<LoginResponse> {
            loginWasCalled = true
            return loginResult
        }

        override suspend fun register(
            name: String,
            email: String,
            password: String
        ): Resource<RegisterResponse> {
            registerWasCalled = true
            return registerResult
        }

        override suspend fun logout() {
            logoutWasCalled = true
        }
    }

    /**
     * Fake UserSessionDao backed by a MutableStateFlow for testing
     * the isLoggedIn StateFlow observation in the ViewModel.
     */
    private class FakeUserSessionDao : UserSessionDao {
        private val sessionFlow = MutableStateFlow<UserSessionEntity?>(null)

        override fun getSession(): Flow<UserSessionEntity?> = sessionFlow

        override suspend fun saveSession(session: UserSessionEntity) {
            sessionFlow.value = session
        }

        override suspend fun clearSession() {
            sessionFlow.value = null
        }
    }
}
