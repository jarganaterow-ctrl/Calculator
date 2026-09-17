package com.example.namazapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.namazapp.data.model.DuaItem
import com.example.namazapp.data.model.SurahItem
import com.example.namazapp.data.repository.SurahAndDuaRepository
import com.example.namazapp.ui.theme.EmeraldLight
import com.example.namazapp.ui.theme.EmeraldPrimary
import com.example.namazapp.ui.theme.GoldAccent

@Composable
fun SurahsScreen(
    onBack: () -> Unit,
    onPlayAudio: (text: String, isArabic: Boolean) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Короткие суры", "Основные дуа")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C1410))
    ) {
        // Заголовок
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF13241C))
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Суры и Дуа",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Арабский оригинал, транскрипция и перевод",
                        color = GoldAccent,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Вкладки
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF13241C),
            contentColor = GoldAccent,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) GoldAccent else Color(0xFF8BAAA0),
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }

        if (selectedTab == 0) {
            SurahsListContent(onPlayAudio = onPlayAudio)
        } else {
            DuasListContent(onPlayAudio = onPlayAudio)
        }
    }
}

@Composable
fun SurahsListContent(
    onPlayAudio: (text: String, isArabic: Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(SurahAndDuaRepository.surahs) { surah ->
            SurahCard(surah = surah, onPlayAudio = onPlayAudio)
        }
    }
}

@Composable
fun SurahCard(
    surah: SurahItem,
    onPlayAudio: (text: String, isArabic: Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13221A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A2B))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${surah.number}",
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = surah.nameRu,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${surah.meaningRu} • ${surah.versesCount} аята",
                            color = Color(0xFF8FAEA1),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = surah.nameArabic,
                        color = GoldAccent,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = Color(0xFF223E2F))
                Spacer(modifier = Modifier.height(12.dp))

                // Кнопка прослушать всю суру
                Button(
                    onClick = {
                        val fullArabic = surah.verses.joinToString(" ") { it.arabic }
                        onPlayAudio(fullArabic, true)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Слушать всю суру", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                surah.verses.forEach { verse ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E382A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${verse.verseNumber}",
                                    color = GoldAccent,
                                    fontSize = 10.sp
                                )
                            }
                            IconButton(
                                onClick = { onPlayAudio(verse.arabic, true) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Озвучить аят",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Арабский аят
                        Text(
                            text = verse.arabic,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth(),
                            lineHeight = 32.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Транскрипция
                        Text(
                            text = verse.transcription,
                            color = Color(0xFF7DEBA9),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Перевод
                        Text(
                            text = verse.translation,
                            color = Color(0xFFB0C9BD),
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Divider(color = Color(0xFF1B3125), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun DuasListContent(
    onPlayAudio: (text: String, isArabic: Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(SurahAndDuaRepository.duas) { dua ->
            DuaCard(dua = dua, onPlayAudio = onPlayAudio)
        }
    }
}

@Composable
fun DuaCard(
    dua: DuaItem,
    onPlayAudio: (text: String, isArabic: Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13221A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A2B))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dua.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dua.occasion,
                        color = GoldAccent,
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = { onPlayAudio(dua.arabic, true) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Слушать дуа",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Арабский
            Text(
                text = dua.arabic,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 30.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Транскрипция
            Text(
                text = dua.transcription,
                color = Color(0xFF7DEBA9),
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Перевод
            Text(
                text = dua.translation,
                color = Color(0xFFB4CBBF),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}
