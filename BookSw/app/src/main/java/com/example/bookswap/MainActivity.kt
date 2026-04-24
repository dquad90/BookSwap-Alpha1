package com.example.bookswap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.bookswap.ui.theme.BookSwapTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val bookViewModel: BookViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BookSwapTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf("splash") }
                    var selectedBook by remember { mutableStateOf<Book?>(null) }
                    var selectedChatRequestId by remember { mutableStateOf<Long?>(null) }
                    var tempEmail by remember { mutableStateOf("") }

                    LaunchedEffect(key1 = currentScreen) {
                        if (currentScreen == "splash") {
                            delay(2000)
                            if (authViewModel.user.value != null) {
                                bookViewModel.fetchBooks()
                                chatViewModel.fetchChatRequests()
                                authViewModel.fetchProfile()
                                currentScreen = "home"
                            } else {
                                currentScreen = "login"
                            }
                        }
                    }

                    val currentUser = authViewModel.user.value
                    val currentProfile = authViewModel.profile.value
                    val userName = currentProfile?.fullName ?: currentUser?.userMetadata?.get("full_name")?.toString()?.replace("\"", "") 
                        ?: currentUser?.email?.substringBefore("@") ?: "User"
                    val userPhotoUrl = null

                    when (currentScreen) {
                        "splash" -> SplashScreen()
                        "login" -> LoginScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = { 
                                bookViewModel.fetchBooks()
                                chatViewModel.fetchChatRequests()
                                authViewModel.fetchProfile()
                                currentScreen = "home" 
                            },
                            onSignUpClick = { currentScreen = "signup" },
                            onForgotPasswordClick = { currentScreen = "forgot_password" }
                        )
                        "signup" -> SignUpScreen(
                            viewModel = authViewModel,
                            onSignUpSuccess = { email -> 
                                tempEmail = email
                                currentScreen = "verify" 
                            },
                            onLoginClick = { currentScreen = "login" }
                        )
                        "forgot_password" -> ForgotPasswordScreen(
                            viewModel = authViewModel,
                            onBack = { currentScreen = "login" }
                        )
                        "verify" -> VerifyScreen(
                            email = tempEmail,
                            viewModel = authViewModel,
                            onVerificationSuccess = { currentScreen = "login" },
                            onBack = { currentScreen = "signup" }
                        )
                        "home" -> HomeScreen(
                            userName = userName,
                            userPhotoUrl = userPhotoUrl,
                            books = bookViewModel.books,
                            onBookClick = { book ->
                                selectedBook = book
                                currentScreen = "details"
                            },
                            onAddClick = {
                                currentScreen = "add_book"
                            },
                            onChatClick = {
                                chatViewModel.fetchChatRequests()
                                currentScreen = "chat_list"
                            },
                            onProfileClick = {
                                authViewModel.fetchProfile()
                                currentScreen = "profile"
                            },
                            onLogout = {
                                authViewModel.logout()
                                currentScreen = "login"
                            }
                        )
                        "details" -> selectedBook?.let { book ->
                            BookDetailsScreen(
                                book = book,
                                viewModel = chatViewModel,
                                onBack = { currentScreen = "home" },
                                onRequestSent = {
                                    chatViewModel.fetchChatRequests()
                                    currentScreen = "chat_list"
                                }
                            )
                        }
                        "chat_list" -> ChatListScreen(
                            viewModel = chatViewModel,
                            currentUserId = currentUser?.id ?: "",
                            onChatClick = { requestId ->
                                selectedChatRequestId = requestId
                                currentScreen = "chat_messages"
                            },
                            onBack = { currentScreen = "home" }
                        )
                        "chat_messages" -> selectedChatRequestId?.let { requestId ->
                            ChatMessagesScreen(
                                viewModel = chatViewModel,
                                requestId = requestId,
                                currentUserId = currentUser?.id ?: "",
                                onBack = { currentScreen = "chat_list" }
                            )
                        }
                        "profile" -> {
                            val myBooksCount = bookViewModel.books.count { it.ownerId == currentUser?.id }
                            val mySwapsCount = chatViewModel.chatRequests.count { 
                                (it.senderId == currentUser?.id || it.receiverId == currentUser?.id) && it.status == "accepted" 
                            }
                            val myFavoritesCount = 0 
                            
                            ProfileScreen(
                                 userName = userName,
                                profile = currentProfile,
                                booksCount = myBooksCount,
                                swapsCount = mySwapsCount,
                                favoritesCount = myFavoritesCount,
                                onBack = { currentScreen = "home" },
                                onLogout = {
                                    authViewModel.logout()
                                    currentScreen = "login"
                                },
                                onAddBookClick = {
                                    currentScreen = "add_book"
                                }
                            )
                        }
                        "add_book" -> AddBookScreen(
                            viewModel = bookViewModel,
                            onBack = { currentScreen = "home" },
                            onBookAdded = { 
                                bookViewModel.fetchBooks()
                                currentScreen = "home" 
                            }
                        )
                    }
                }
            }
        }
    }
}
