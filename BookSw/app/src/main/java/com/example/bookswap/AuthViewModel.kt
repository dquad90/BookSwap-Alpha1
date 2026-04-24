package com.example.bookswap

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthViewModel : ViewModel() {
    private val auth = supabase.auth
    private val postgrest = supabase.postgrest

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _user = mutableStateOf<UserInfo?>(auth.currentUserOrNull())
    val user: State<UserInfo?> = _user

    private val _profile = mutableStateOf<Profile?>(null)
    val profile: State<Profile?> = _profile

    init {
        if (_user.value != null) {
            fetchProfile()
        }
    }

    fun fetchProfile() {
        val currentUser = auth.currentUserOrNull() ?: return
        viewModelScope.launch {
            try {
                val result = postgrest["profiles"].select {
                    filter {
                        eq("id", currentUser.id)
                    }
                }.decodeSingle<Profile>()
                _profile.value = result
            } catch (e: Exception) {
                // Profile might not exist yet if trigger failed or sync is slow
                _error.value = "Failed to load profile: ${e.message}"
            }
        }
    }

    fun login(identifier: String, password: String, onSuccess: () -> Unit) {
        if (identifier.isBlank() || password.isBlank()) {
            _error.value = "Please fill in all fields"
            return
        }
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val emailToUse = if (identifier.contains("@")) {
                    identifier
                } else {
                    val response = postgrest["profiles"].select {
                        filter {
                            eq("full_name", identifier)
                        }
                    }
                    val data = response.decodeList<Profile>()
                    if (data.isNotEmpty()) {
                        data[0].email
                    } else {
                        throw Exception("User not found with this name")
                    }
                }

                auth.signInWith(Email) {
                    this.email = emailToUse
                    this.password = password
                }
                _user.value = auth.currentUserOrNull()
                fetchProfile()
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Login failed"
            } finally {
                _loading.value = false
            }
        }
    }

    fun signUp(
        email: String,
        password: String,
        name: String,
        phone: String,
        address: String,
        onSuccess: () -> Unit
    ) {
        if (email.isBlank() || password.isBlank() || name.isBlank() || phone.isBlank() || address.isBlank()) {
            _error.value = "Please fill in all fields"
            return
        }
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                    data = buildJsonObject {
                        put("full_name", name)
                        put("phone", phone)
                        put("address", address)
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Sign up failed"
            } finally {
                _loading.value = false
            }
        }
    }

    fun verifyCode(email: String, code: String, onSuccess: () -> Unit) {
        if (code.isBlank() || code.length < 6) {
            _error.value = "Please enter the verification code"
            return
        }
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                auth.verifyEmailOtp(
                    type = OtpType.Email.SIGNUP,
                    email = email,
                    token = code
                )
                auth.signOut() 
                _user.value = null
                _profile.value = null
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Verification failed"
            } finally {
                _loading.value = false
            }
        }
    }

    fun sendResetPassword(email: String, onSuccess: () -> Unit) {
        if (email.isBlank()) {
            _error.value = "Please enter your email"
            return
        }
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                auth.resetPasswordForEmail(email)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to send reset link"
            } finally {
                _loading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            auth.signOut()
            _user.value = null
            _profile.value = null
        }
    }

    fun clearError() {
        _error.value = null
    }
}

@Serializable
data class Profile(
    val id: String? = null,
    @SerialName("full_name")
    val fullName: String,
    val email: String,
    val phone: String? = null,
    val address: String? = null
)
