package com.example.namazapp

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.namazapp.data.model.Gender
import com.example.namazapp.data.model.Madhhab
import com.example.namazapp.ui.screens.*
import com.example.namazapp.ui.theme.EmeraldLight
import com.example.namazapp.ui.theme.EmeraldPrimary
import com.example.namazapp.ui.theme.GoldAccent
import com.example.namazapp.ui.theme.NamazAppTheme
import java.util.Locale

enum class AppScreen {
    HOME,
    SIMULATOR,
    WUDU,
    SURAHS,
    TASBIH,
    QUIZ,
    SETTINGS
}

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            tts = TextToSpeech(this, this)
        } catch (_: Exception) {
            // Игнорируем в средах без сервиса TTS
        }

        setContent {
            NamazAppTheme {
                MainContent(
                    onPlayAudio = { text, isArabic ->
                        playSpeech(text, isArabic)
                    }
                )
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            tts?.language = Locale("ar")
        }
    }

    private fun playSpeech(text: String, isArabic: Boolean) {
        if (!isTtsReady || tts == null) return
        val locale = if (isArabic) Locale("ar") else Locale("ru")
        tts?.language = locale
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "namaz_speech_id")
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

@Composable
fun MainContent(
    onPlayAudio: (text: String, isArabic: Boolean) -> Unit
) {
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var currentMadhhab by remember { mutableStateOf(Madhhab.HANAFI) }
    var currentGender by remember { mutableStateOf(Gender.MALE) }

    // Параметры симулятора
    var activePrayerId by remember { mutableStateOf("fajr") }
    var activeIsFard by remember { mutableStateOf(true) }

    Scaffold(
        bottomBar = {
            if (currentScreen != AppScreen.SIMULATOR) {
                NavigationBar(
                    containerColor = Color(0xFF101E17),
                    contentColor = GoldAccent,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.HOME,
                        onClick = { currentScreen = AppScreen.HOME },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Главная") },
                        label = { Text("Главная", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GoldAccent,
                            selectedTextColor = GoldAccent,
                            unselectedIconColor = Color(0xFF7E9E8D),
                            unselectedTextColor = Color(0xFF7E9E8D),
                            indicatorColor = EmeraldPrimary.copy(alpha = 0.5f)
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.WUDU,
                        onClick = { currentScreen = AppScreen.WUDU },
                        icon = { Icon(Icons.Default.WaterDrop, contentDescription = "Омовение") },
                        label = { Text("Омовение", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GoldAccent,
                            selectedTextColor = GoldAccent,
                            unselectedIconColor = Color(0xFF7E9E8D),
                            unselectedTextColor = Color(0xFF7E9E8D),
                            indicatorColor = EmeraldPrimary.copy(alpha = 0.5f)
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.SURAHS,
                        onClick = { currentScreen = AppScreen.SURAHS },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "Суры") },
                        label = { Text("Суры/Дуа", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GoldAccent,
                            selectedTextColor = GoldAccent,
                            unselectedIconColor = Color(0xFF7E9E8D),
                            unselectedTextColor = Color(0xFF7E9E8D),
                            indicatorColor = EmeraldPrimary.copy(alpha = 0.5f)
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.TASBIH,
                        onClick = { currentScreen = AppScreen.TASBIH },
                        icon = { Icon(Icons.Default.FiberManualRecord, contentDescription = "Тасбих") },
                        label = { Text("Тасбих", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GoldAccent,
                            selectedTextColor = GoldAccent,
                            unselectedIconColor = Color(0xFF7E9E8D),
                            unselectedTextColor = Color(0xFF7E9E8D),
                            indicatorColor = EmeraldPrimary.copy(alpha = 0.5f)
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.QUIZ,
                        onClick = { currentScreen = AppScreen.QUIZ },
                        icon = { Icon(Icons.Default.School, contentDescription = "Тест") },
                        label = { Text("Тест", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GoldAccent,
                            selectedTextColor = GoldAccent,
                            unselectedIconColor = Color(0xFF7E9E8D),
                            unselectedTextColor = Color(0xFF7E9E8D),
                            indicatorColor = EmeraldPrimary.copy(alpha = 0.5f)
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.SETTINGS,
                        onClick = { currentScreen = AppScreen.SETTINGS },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Настройки") },
                        label = { Text("Мазхабы", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GoldAccent,
                            selectedTextColor = GoldAccent,
                            unselectedIconColor = Color(0xFF7E9E8D),
                            unselectedTextColor = Color(0xFF7E9E8D),
                            indicatorColor = EmeraldPrimary.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        currentMadhhab = currentMadhhab,
                        currentGender = currentGender,
                        onSelectMadhhab = { currentMadhhab = it },
                        onSelectGender = { currentGender = it },
                        onStartPrayer = { pId, fard ->
                            activePrayerId = pId
                            activeIsFard = fard
                            currentScreen = AppScreen.SIMULATOR
                        },
                        onNavigateToWudu = { currentScreen = AppScreen.WUDU },
                        onNavigateToSurahs = { currentScreen = AppScreen.SURAHS },
                        onNavigateToTasbih = { currentScreen = AppScreen.TASBIH },
                        onNavigateToQuiz = { currentScreen = AppScreen.QUIZ },
                        onNavigateToSettings = { currentScreen = AppScreen.SETTINGS }
                    )
                }

                AppScreen.SIMULATOR -> {
                    PrayerSimulatorScreen(
                        prayerId = activePrayerId,
                        isFard = activeIsFard,
                        madhhab = currentMadhhab,
                        gender = currentGender,
                        onBack = { currentScreen = AppScreen.HOME },
                        onPlayAudio = onPlayAudio
                    )
                }

                AppScreen.WUDU -> {
                    WuduScreen(
                        onBack = { currentScreen = AppScreen.HOME }
                    )
                }

                AppScreen.SURAHS -> {
                    SurahsScreen(
                        onBack = { currentScreen = AppScreen.HOME },
                        onPlayAudio = onPlayAudio
                    )
                }

                AppScreen.TASBIH -> {
                    TasbihScreen(
                        onBack = { currentScreen = AppScreen.HOME }
                    )
                }

                AppScreen.QUIZ -> {
                    QuizScreen(
                        onBack = { currentScreen = AppScreen.HOME }
                    )
                }

                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        currentMadhhab = currentMadhhab,
                        currentGender = currentGender,
                        onSelectMadhhab = { currentMadhhab = it },
                        onSelectGender = { currentGender = it },
                        onBack = { currentScreen = AppScreen.HOME }
                    )
                }
            }
        }
    }
}
