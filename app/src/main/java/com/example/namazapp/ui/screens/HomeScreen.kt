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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.namazapp.data.model.Gender
import com.example.namazapp.data.model.Madhhab
import com.example.namazapp.data.model.PrayerInfo
import com.example.namazapp.data.repository.PrayerRepository
import com.example.namazapp.ui.theme.EmeraldLight
import com.example.namazapp.ui.theme.EmeraldPrimary
import com.example.namazapp.ui.theme.GoldAccent

@Composable
fun HomeScreen(
    currentMadhhab: Madhhab,
    currentGender: Gender,
    onSelectMadhhab: (Madhhab) -> Unit,
    onSelectGender: (Gender) -> Unit,
    onStartPrayer: (prayerId: String, isFard: Boolean) -> Unit,
    onNavigateToWudu: () -> Unit,
    onNavigateToSurahs: () -> Unit,
    onNavigateToTasbih: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var showMadhhabDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C1410))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Заголовок и текущие настройки
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13241C))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF194030), Color(0xFF11221A))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ОБУЧЕНИЕ НАМАЗУ",
                                    color = GoldAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Муаллим Намаз",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            IconButton(
                                onClick = onNavigateToSettings,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x22FFFFFF))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Настройки",
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Плашка выбора Мазхаба и Пола
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Мазхаб
                            Box(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1C362A))
                                    .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .clickable { showMadhhabDialog = true }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text("Мазхаб", color = Color(0xFF90B5A0), fontSize = 10.sp)
                                        Text(
                                            currentMadhhab.displayName,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = GoldAccent
                                    )
                                }
                            }

                            // Пол (М / Ж)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1C362A))
                                    .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        val nextGender = if (currentGender == Gender.MALE) Gender.FEMALE else Gender.MALE
                                        onSelectGender(nextGender)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text("Для кого", color = Color(0xFF90B5A0), fontSize = 10.sp)
                                        Text(
                                            currentGender.displayName,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.SwapHoriz,
                                        contentDescription = null,
                                        tint = GoldAccent
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = currentMadhhab.regionNote,
                            color = Color(0xFFB5D3C2),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Быстрые разделы (Омовение, Суры, Тасбих, Тесты)
        item {
            Text(
                text = "ОСНОВНЫЕ РАЗДЕЛЫ",
                color = GoldAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    title = "Омовение",
                    subtitle = "Вуду, Гусль",
                    icon = Icons.Default.WaterDrop,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToWudu
                )
                QuickActionCard(
                    title = "Суры и Дуа",
                    subtitle = "Текст и аудио",
                    icon = Icons.Default.MenuBook,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSurahs
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    title = "Тасбих",
                    subtitle = "Зикр 33 / 99",
                    icon = Icons.Default.FiberManualRecord,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToTasbih
                )
                QuickActionCard(
                    title = "Тест знаний",
                    subtitle = "Проверь себя",
                    icon = Icons.Default.School,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToQuiz
                )
            }
        }

        // Блок 5 ежедневных молитв
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "5 ОБЯЗАТЕЛЬНЫХ НАМАЗОВ",
                    color = GoldAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Выберите для обучения",
                    color = Color(0xFF7A9B89),
                    fontSize = 11.sp
                )
            }
        }

        items(PrayerRepository.prayersList) { prayer ->
            PrayerCard(
                prayer = prayer,
                onStartFard = { onStartPrayer(prayer.id, true) },
                onStartSunnah = { onStartPrayer(prayer.id, false) }
            )
        }

        // Цитата/Хадис дня
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF12221A))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Слова Пророка Мухаммада ﷺ",
                            color = GoldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "«Молитесь так, как вы видели меня молящимся». (Сахих аль-Бухари)",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }

    // Диалог выбора мазхаба
    if (showMadhhabDialog) {
        AlertDialog(
            onDismissRequest = { showMadhhabDialog = false },
            title = {
                Text(
                    "Выберите мазхаб",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Madhhab.values().forEach { m ->
                        val isSelected = m == currentMadhhab
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF1E4633) else Color(0xFF14241C))
                                .clickable {
                                    onSelectMadhhab(m)
                                    showMadhhabDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = m.displayName,
                                    color = if (isSelected) GoldAccent else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = m.shortDesc,
                                    color = Color(0xFF90B5A0),
                                    fontSize = 11.sp
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = GoldAccent
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMadhhabDialog = false }) {
                    Text("Закрыть", color = GoldAccent)
                }
            },
            containerColor = Color(0xFF11221A)
        )
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF15281F)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(EmeraldPrimary.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF86A896),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun PrayerCard(
    prayer: PrayerInfo,
    onStartFard: () -> Unit,
    onStartSunnah: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13231A))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = prayer.nameRu,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = prayer.timeDescription,
                        color = Color(0xFF8EAFA0),
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = prayer.nameArabic,
                    color = GoldAccent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Метки ракаатов
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (prayer.sunnahBeforeRakahs > 0) {
                    RakahBadge(text = "${prayer.sunnahBeforeRakahs} ракаата сунны до", isFard = false)
                }
                RakahBadge(text = "${prayer.fardRakahs} ракаата фард", isFard = true)
                if (prayer.sunnahAfterRakahs > 0) {
                    RakahBadge(text = "${prayer.sunnahAfterRakahs} ракаата сунны после", isFard = false)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Кнопки запуска
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStartFard,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Учить Фард", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (prayer.sunnahBeforeRakahs > 0 || prayer.sunnahAfterRakahs > 0) {
                    OutlinedButton(
                        onClick = onStartSunnah,
                        modifier = Modifier.weight(1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Учить Сунну", color = GoldAccent, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RakahBadge(text: String, isFard: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isFard) EmeraldLight.copy(alpha = 0.2f) else Color(0xFF23362C))
            .border(
                0.5.dp,
                if (isFard) EmeraldLight.copy(alpha = 0.5f) else Color(0xFF385546),
                RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = if (isFard) Color(0xFF7DEBA9) else Color(0xFFC0D5CB),
            fontSize = 11.sp,
            fontWeight = if (isFard) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
