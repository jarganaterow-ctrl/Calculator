package com.example.namazapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.namazapp.data.model.Gender
import com.example.namazapp.data.model.Madhhab
import com.example.namazapp.data.repository.PrayerRepository
import com.example.namazapp.ui.components.PostureIllustration
import com.example.namazapp.ui.theme.EmeraldLight
import com.example.namazapp.ui.theme.EmeraldPrimary
import com.example.namazapp.ui.theme.GoldAccent

@Composable
fun PrayerSimulatorScreen(
    prayerId: String,
    isFard: Boolean,
    madhhab: Madhhab,
    gender: Gender,
    onBack: () -> Unit,
    onPlayAudio: (textToSpeak: String, isArabic: Boolean) -> Unit
) {
    val prayer = remember(prayerId) { PrayerRepository.getPrayerById(prayerId) }
    val steps = remember(prayerId, isFard, madhhab, gender) {
        PrayerRepository.generatePrayerSteps(
            prayerId = prayerId,
            isFard = isFard,
            madhhab = madhhab,
            gender = gender
        )
    }

    var currentStepIndex by remember { mutableStateOf(0) }
    val currentStep = steps.getOrNull(currentStepIndex) ?: steps.first()
    val scrollState = rememberScrollState()

    var isPlaying by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C1410))
    ) {
        // Верхняя панель
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF13241C))
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${prayer.nameRu} • ${if (isFard) "Фард" else "Сунна"}",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ракаат ${currentStep.rakahNumber} из ${steps.last().rakahNumber} (Шаг ${currentStepIndex + 1}/${steps.size})",
                        color = GoldAccent,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1C362A))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${madhhab.displayName.take(5)} • ${if (gender == Gender.MALE) "М" else "Ж"}",
                        color = Color(0xFF90B5A0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Прогресс бар
        val progress = (currentStepIndex + 1).toFloat() / steps.size.toFloat()
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = GoldAccent,
            trackColor = Color(0xFF1D3327)
        )

        // Основное тело шага
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Иллюстрация позы
            PostureIllustration(
                posture = currentStep.posture,
                madhhab = madhhab,
                gender = gender
            )

            // Заголовок действия
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentStep.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                if (currentStep.repetitions > 1) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(GoldAccent.copy(alpha = 0.2f))
                            .border(1.dp, GoldAccent, CircleShape)
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "× ${currentStep.repetitions}",
                            color = GoldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Арабский текст
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13221A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3C2B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "АРАБСКИЙ ТЕКСТ",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        IconButton(
                            onClick = {
                                isPlaying = !isPlaying
                                onPlayAudio(currentStep.arabicText, true)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.VolumeUp else Icons.Default.PlayArrow,
                                contentDescription = "Слушать произношение",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentStep.arabicText,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Right,
                        lineHeight = 36.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Транскрипция на русском
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16281F))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ТРАНСКРИПЦИЯ",
                        color = Color(0xFF7DEBA9),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentStep.transcription,
                        color = Color(0xFFE5F5EC),
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }
            }

            // Перевод на русский
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF14201A))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "СМЫСЛОВОЙ ПЕРЕВОД",
                        color = Color(0xFF90B5A0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentStep.translation,
                        color = Color(0xFFC0D5CB),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            // Инструкция положения тела
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF11261D))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "КАК ДЕРЖАТЬ ТЕЛО (${gender.displayName.uppercase()})",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = currentStep.instruction,
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }
            }

            // Особенность мазхаба (если есть)
            if (currentStep.madhhabSpecificNote != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222915)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ОСОБЕННОСТЬ: ${madhhab.displayName.uppercase()}",
                                color = GoldAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = currentStep.madhhabSpecificNote,
                                color = Color(0xFFF3E5AB),
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }
        }

        // Нижняя панель навигации (Назад / Далее)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF13241C))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Кнопка Назад
                OutlinedButton(
                    onClick = {
                        if (currentStepIndex > 0) {
                            currentStepIndex--
                        }
                    },
                    enabled = currentStepIndex > 0,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (currentStepIndex > 0) GoldAccent else Color.Gray.copy(alpha = 0.3f)
                    )
                ) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Назад", color = if (currentStepIndex > 0) GoldAccent else Color.Gray)
                }

                // Кнопка Далее или Завершить
                val isLast = currentStepIndex == steps.size - 1
                Button(
                    onClick = {
                        if (isLast) {
                            onBack()
                        } else {
                            currentStepIndex++
                        }
                    },
                    modifier = Modifier.weight(1.3f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLast) GoldAccent else EmeraldPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isLast) "Завершить намаз" else "Далее",
                        color = if (isLast) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isLast) Icons.Default.Check else Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = if (isLast) Color.Black else Color.White
                    )
                }
            }
        }
    }
}
