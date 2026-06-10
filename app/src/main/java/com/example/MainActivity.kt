package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.FamilyTreeDatabase
import com.example.data.repository.FamilyTreeRepository
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.Screen
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase programmatic configuration fallback safely
        try {
            com.google.firebase.FirebaseApp.getInstance()
        } catch (e: Exception) {
            try {
                val options = com.google.firebase.FirebaseOptions.Builder()
                    .setApiKey("AIzaSyD-FakeGooglServicesOptionKeyRef")
                    .setApplicationId("1:56789012345:android:6a7b8c9d0e1f2g3h")
                    .setProjectId("family-tree-applet-sandbox")
                    .build()
                com.google.firebase.FirebaseApp.initializeApp(applicationContext, options)
            } catch (initException: Exception) {
                android.util.Log.e("FirebaseInit", "Failed to initialize Firebase App programmatic options: ${initException.message}")
            }
        }

        // 1. Initialize DB, DAO, and Repository
        val database = FamilyTreeDatabase.getDatabase(this)
        val dao = database.familyTreeDao()
        val repository = FamilyTreeRepository(dao)

        // 2. Initialize ViewModel with Factory
        val factory = MainViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        setContent {
            val themeOption by viewModel.themeOption.collectAsState()
            MyApplicationTheme(themeOption = themeOption) {
                val currentScreen by viewModel.currentScreen.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        is Screen.Auth -> AuthScreen(viewModel)
                        is Screen.Register -> RegisterScreen(viewModel)
                        is Screen.ForgotPassword -> ForgotPasswordScreen(viewModel)
                        is Screen.Home -> HomeScreen(viewModel)
                        is Screen.TreeView -> TreeViewScreen(viewModel)
                        is Screen.ProfileDetails -> ProfileDetailsScreen(viewModel)
                        is Screen.EditMember -> EditMemberScreen(viewModel)
                        is Screen.AiAssistant -> AiAssistantScreen(viewModel)
                        else -> AuthScreen(viewModel)
                    }
                }
            }
        }
    }
}
