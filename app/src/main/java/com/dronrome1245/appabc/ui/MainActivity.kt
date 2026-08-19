package com.dronrome1245.appabc.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.dronrome1245.appabc.core.audio.AudioPlayer
import com.dronrome1245.appabc.core.audio.HybridAudioPlayer
import com.dronrome1245.appabc.core.audio.TtsAudioPlayer
import com.dronrome1245.appabc.core.theme.AppABCTheme
import com.dronrome1245.appabc.data.local.db.AppDatabase
import com.dronrome1245.appabc.data.repository.AppRepositoryImpl
import com.dronrome1245.appabc.domain.curriculum.ApprovedCurriculum
import com.dronrome1245.appabc.ui.exercise.ExerciseScreen
import com.dronrome1245.appabc.ui.exercise.ExerciseViewModel
import com.dronrome1245.appabc.ui.home.HomeScreen
import com.dronrome1245.appabc.ui.home.HomeViewModel
import com.dronrome1245.appabc.ui.result.ResultScreen
import com.dronrome1245.appabc.ui.result.ResultViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var repository: AppRepositoryImpl
    private lateinit var audioPlayer: AudioPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "app_abc_db"
        ).build()
        repository = AppRepositoryImpl(database.attemptDao(), database.letterDao())

        val ttsPlayer = TtsAudioPlayer(this)
        audioPlayer = HybridAudioPlayer(
            context = this,
            tts = ttsPlayer,
            spokenNameProvider = { symbol ->
                ApprovedCurriculum.findLetter(symbol)?.spokenName ?: symbol.toString()
            }
        )

        MainScope().launch {
            repository.ensureInitialLetters()
        }

        setContent {
            AppABCTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(repository, audioPlayer)
                }
            }
        }
    }

    override fun onDestroy() {
        audioPlayer.release()
        super.onDestroy()
    }
}

@Composable
fun AppNavigation(repository: AppRepositoryImpl, audioPlayer: AudioPlayer) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onStartClick = { navController.navigate("exercise") },
                viewModel = viewModel { HomeViewModel(repository) }
            )
        }
        composable("exercise") {
            ExerciseScreen(
                onFinish = { sessionId ->
                    navController.navigate("result/$sessionId") {
                        popUpTo("home")
                    }
                },
                viewModel = viewModel { ExerciseViewModel(repository, audioPlayer) }
            )
        }
        composable(
            "result/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            ResultScreen(
                onRestart = { navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                } },
                viewModel = viewModel { ResultViewModel(repository, sessionId) }
            )
        }
    }
}
