package com.example.namazapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.namazapp.data.model.Gender
import com.example.namazapp.data.model.Madhhab
import com.example.namazapp.ui.theme.EmeraldLight
import com.example.namazapp.ui.theme.EmeraldPrimary
import com.example.namazapp.ui.theme.GoldAccent

@Composable
fun SettingsScreen(
    currentMadhhab: Madhhab,
    currentGender: Gender,
    onSelectMadhhab: (Madhhab) -> Unit,
    onSelectGender: (Gender) -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C1410))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Заголовок
        item {
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
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Настройки и Справочник",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Мазхабы, правила и условия молитвы",
                        color = GoldAccent,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Выбор Мазхаба
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13221A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1D3B2C))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ВЫБЕРИТЕ ВАШ МАЗХАБ / НАПРАВЛЕНИЕ",
                        color = GoldAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Madhhab.values().forEach { m ->
                        val isSelected = m == currentMadhhab
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF1B402E) else Color(0xFF15261D))
                                .border(
                                    1.dp,
                                    if (isSelected) GoldAccent.copy(alpha = 0.6f) else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { onSelectMadhhab(m) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectMadhhab(m) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = GoldAccent,
                                    unselectedColor = Color(0xFF7A9E8C)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
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
                        }
                    }
                }
            }
        }

        // Выбор пола
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13221A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1D3B2C))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ДЛЯ КОГО ОБУЧЕНИЕ",
                        color = GoldAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Gender.values().forEach { g ->
                            val isSelected = g == currentGender
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) EmeraldPrimary else Color(0xFF172B20))
                                    .border(
                                        1.dp,
                                        if (isSelected) GoldAccent else Color(0xFF233E2F),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onSelectGender(g) }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = g.displayName,
                                    color = if (isSelected) Color.White else Color(0xFF90B5A0),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Справочник: Условия намаза (Шарт)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13221A))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = GoldAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "УСЛОВИЯ НАМАЗА (ШАРТЫ ДО МОЛИТВЫ)",
                            color = GoldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Ислам (быть мусульманином)\n" +
                                "2. Разум (быть в здравом рассудке)\n" +
                                "3. Достижение возраста различения (тамииз)\n" +
                                "4. Очищение от малого и большого осквернения (Вуду и Гусль)\n" +
                                "5. Чистота тела, одежды и места молитвы от нечистот (наджасы)\n" +
                                "6. Сокрытие аурата (мужчины — от пупка до колен; женщины — все тело, кроме лица и кистей рук)\n" +
                                "7. Наступление времени намаза\n" +
                                "8. Обращение лицом к Кибле (в сторону Каабы в Мекке)\n" +
                                "9. Намерение (Ният) в сердце",
                        color = Color(0xFFD3E7DC),
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Справочник: Частые ошибки
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13221A))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF87171))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ЧАСТЫЕ ОШИБКИ НОВИЧКОВ",
                            color = Color(0xFFF87171),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Спешка и отсутствие спокойствия (тума'нина) в поклонах\n" +
                                "• Опережение имама в коллективном намазе\n" +
                                "• Локти на полу у мужчин во время суджуда (запрещено хадисом)\n" +
                                "• Блуждающий взгляд по сторонам или вверх в небо\n" +
                                "• Чтение суры Фатиха без шевеления губами и языком\n" +
                                "• Открытие аурата при наклоне (короткая футболка сзади)",
                        color = Color(0xFFD3E7DC),
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
