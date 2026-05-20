package com.example.bookswap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.bookswap.ui.theme.BookSwapTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val bookViewModel: BookViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize Notification Channel
        NotificationHelper.createNotificationChannel(this)

        setContent {
            BookSwapTheme {
                val context = LocalContext.current
                
                // Permission Request for Android 13+
                var hasNotificationPermission by remember {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        mutableStateOf(
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        )
                    } else {
                        mutableStateOf(true)
                    }
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        hasNotificationPermission = isGranted
                    }
                )

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf("splash") }
                    var selectedBookId by remember { mutableStateOf<Long?>(null) }
                    var selectedChatRequestId by remember { mutableStateOf<Long?>(null) }
                    var viewingUserId by remember { mutableStateOf<String?>(null) }
                    var tempEmail by remember { mutableStateOf("") }

                    val bookExists = remember(selectedBookId, bookViewModel.books.size) {
                        selectedBookId == null || bookViewModel.books.any { it.id == selectedBookId }
                    }
                    
                    if (currentScreen == "details" && !bookExists) {
                        currentScreen = "home"
                        selectedBookId = null
                    }

                    LaunchedEffect(key1 = currentScreen) {
                        if (currentScreen == "splash") {
                            delay(2000)
                            if (authViewModel.user.value != null) {
                                bookViewModel.fetchBooks()
                                chatViewModel.fetchChatRequests()
                                chatViewModel.startListeningToRequests(context)
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

                    when (currentScreen) {
                        "splash" -> SplashScreen()
                        "login" -> LoginScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = { 
                                bookViewModel.fetchBooks()
                                chatViewModel.fetchChatRequests()
                                chatViewModel.startListeningToRequests(context)
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
                            userPhotoUrl = currentProfile?.avatarUrl,
                            viewModel = bookViewModel,
                            onBookClick = { book ->
                                selectedBookId = book.id
                                currentScreen = "details"
                            },
                            onExploreClick = { currentScreen = "explore" },
                            onAddClick = { currentScreen = "add_book" },
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
                        "explore" -> ExploreScreen(
                            viewModel = bookViewModel,
                            onBookClick = { book ->
                                selectedBookId = book.id
                                currentScreen = "details"
                            },
                            onHomeClick = { currentScreen = "home" },
                            onAddClick = { currentScreen = "add_book" },
                            onChatClick = {
                                chatViewModel.fetchChatRequests()
                                currentScreen = "chat_list"
                            },
                            onProfileClick = {
                                authViewModel.fetchProfile()
                                currentScreen = "profile"
                            }
                        )
                        "details" -> selectedBookId?.let { id ->
                            BookDetailsScreen(
                                bookId = id,
                                currentUserId = currentUser?.id ?: "",
                                chatViewModel = chatViewModel,
                                bookViewModel = bookViewModel,
                                onBack = { 
                                    currentScreen = "home"
                                    selectedBookId = null 
                                },
                                onEditClick = { currentScreen = "edit_book" },
                                onRequestSent = {
                                    chatViewModel.fetchChatRequests()
                                    currentScreen = "chat_list"
                                }
                            )
                        }
                        "edit_book" -> selectedBookId?.let { id ->
                            val bookToEdit = bookViewModel.books.find { it.id == id }
                            bookToEdit?.let {
                                EditBookScreen(
                                    book = it,
                                    viewModel = bookViewModel,
                                    onBack = { currentScreen = "details" },
                                    onBookUpdated = { currentScreen = "details" }
                                )
                            }
                        }
                        "chat_list" -> ChatListScreen(
                            viewModel = chatViewModel,
                            currentUserId = currentUser?.id ?: "",
                            onChatClick = { requestId ->
                                selectedChatRequestId = requestId
                                currentScreen = "chat_messages"
                            },
                            onProfileClick = { userId ->
                                if (userId == currentUser?.id) {
                                    currentScreen = "profile"
                                } else {
                                    viewingUserId = userId
                                    authViewModel.fetchUserProfile(userId)
                                    currentScreen = "view_profile"
                                }
                            },
                            onBack = { currentScreen = "home" }
                        )
                        "chat_messages" -> selectedChatRequestId?.let { requestId ->
                            ChatMessagesScreen(
                                viewModel = chatViewModel,
                                requestId = requestId,
                                currentUserId = currentUser?.id ?: "",
                                onProfileClick = { userId ->
                                    if (userId == currentUser?.id) {
                                        currentScreen = "profile"
                                    } else {
                                        viewingUserId = userId
                                        authViewModel.fetchUserProfile(userId)
                                        currentScreen = "view_profile"
                                    }
                                },
                                onBack = { currentScreen = "chat_list" }
                            )
                        }
                        "profile" -> {
                            val myBooks = bookViewModel.books.filter { it.ownerId == currentUser?.id }
                            val favoriteBooks = bookViewModel.books.filter { it.id in bookViewModel.favorites }
                            
                            val mySwapsCount = chatViewModel.chatRequests.count { 
                                (it.senderId == currentUser?.id || it.receiverId == currentUser?.id) && it.status == "accepted" 
                            }
                            
                            ProfileScreen(
                                userName = userName,
                                profile = currentProfile,
                                booksCount = myBooks.size,
                                swapsCount = mySwapsCount,
                                favoritesCount = favoriteBooks.size,
                                wishlistCount = bookViewModel.wishlist.size,
                                ratingCount = 0,
                                myBooks = myBooks,
                                favoriteBooks = favoriteBooks,
                                onBookClick = { book ->
                                    selectedBookId = book.id
                                    currentScreen = "details"
                                },
                                onUpdateProfile = { name, username, phone, address, bitmap ->
                                    authViewModel.updateProfile(name, username, phone, address, bitmap) {}
                                },
                                onBack = { currentScreen = "home" },
                                onLogout = {
                                    authViewModel.logout()
                                    currentScreen = "login"
                                },
                                onAddBookClick = {
                                    currentScreen = "add_book"
                                },
                                onWishlistClick = {
                                    currentScreen = "wishlist"
                                },
                                onSwapRequestsClick = {
                                    currentScreen = "chat_list"
                                },
                                isCurrentUser = true,
                                chatViewModel = chatViewModel
                            )
                        }
                        "view_profile" -> viewingUserId?.let { userId ->
                            val userProfile = authViewModel.viewedProfile.value
                            val userBooks = bookViewModel.books.filter { it.ownerId == userId }
                            val userSwapsCount = chatViewModel.chatRequests.count { 
                                (it.senderId == userId || it.receiverId == userId) && it.status == "accepted" 
                            }

                            ProfileScreen(
                                userName = userProfile?.fullName ?: "User",
                                profile = userProfile,
                                booksCount = userBooks.size,
                                swapsCount = userSwapsCount,
                                favoritesCount = 0,
                                wishlistCount = 0,
                                ratingCount = 0,
                                myBooks = userBooks,
                                onBookClick = { book ->
                                    selectedBookId = book.id
                                    currentScreen = "details"
                                },
                                onBack = { 
                                    currentScreen = "chat_list"
                                    authViewModel.clearViewedProfile()
                                },
                                isCurrentUser = false,
                                chatViewModel = chatViewModel
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
                        "wishlist" -> WishlistScreen(
                            viewModel = bookViewModel,
                            onBookClick = { book ->
                                selectedBookId = book.id
                                currentScreen = "details"
                            },
                            onHomeClick = { currentScreen = "home" },
                            onExploreClick = { currentScreen = "explore" },
                            onAddClick = { currentScreen = "add_book" },
                            onChatClick = {
                                chatViewModel.fetchChatRequests()
                                currentScreen = "chat_list"
                            },
                            onProfileClick = {
                                authViewModel.fetchProfile()
                                currentScreen = "profile"
                            }
                        )
                    }
                }
            }
        }
    }
}
