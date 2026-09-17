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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.namazapp.data.repository.WuduRepository
import com.example.namazapp.ui.theme.EmeraldLight
import com.example.namazapp.ui.theme.EmeraldPrimary
import com.example.namazapp.ui.theme.GoldAccent

@Composable
fun WuduScreen(
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Вуду (Малое)", "Гусль (Полное)", "Таяммум", "Что нарушает")

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
                        text = "Омовение (Тахарат)",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ключ к намазу — чистота",
                        color = GoldAccent,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Вкладки
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF13241C),
            contentColor = GoldAccent,
            edgePadding = 16.dp,
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
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        // Контент выбранной вкладки
        when (selectedTab) {
            0 -> WuduTabContent()
            1 -> GhuslTabContent()
            2 -> TayammumTabContent()
            3 -> NullifiersTabContent()
        }
    }
}

@Composable
fun WuduTabContent() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF193427))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "4 ОБЯЗАТЕЛЬНЫХ ФАРДА ОМОВЕНИЯ",
                        color = GoldAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1) Мытье лица. 2) Мытье рук до локтей. 3) Протирание головы (масх). 4) Мытье ног до щиколоток. Без них омовение недействительно.",
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        items(WuduRepository.wuduSteps) { step ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13221A)),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (step.isFard) EmeraldLight.copy(alpha = 0.5f) else Color(0xFF1B3627)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (step.isFard) EmeraldPrimary else Color(0xFF22382C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${step.stepNumber}",
                            color = if (step.isFard) GoldAccent else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = step.title,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (step.isFard) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(EmeraldLight.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "ФАРД",
                                        color = Color(0xFF7DEBA9),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = step.actionText,
                            color = Color(0xFFC0D5CB),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        if (step.duaArabic != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = step.duaArabic,
                                color = GoldAccent,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Дуа после омовения
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF162B20)),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ДУА ПОСЛЕ СОВЕРШЕНИЯ ОМОВЕНИЯ",
                        color = GoldAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = WuduRepository.afterWuduDua["arabic"] ?: "",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 26.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = WuduRepository.afterWuduDua["transcription"] ?: "",
                        color = Color(0xFF7DEBA9),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = WuduRepository.afterWuduDua["translation"] ?: "",
                        color = Color(0xFFB5CCC1),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun GhuslTabContent() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF193427))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "КОГДА ГУСЛЬ ОБЯЗАТЕЛЕН (ФАРД)?",
                        color = GoldAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• После интимной близости (джанаба)\n• После поллюции во сне\n• По окончании менструации (хайд) и послеродовых кровотечений (нифас)\n• При принятии Ислама новым мусульманином",
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        items(WuduRepository.ghuslSteps) { step ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13221A))
            ) {
                Text(
                    text = step,
                    color = Color(0xFFD6E8DE),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}

@Composable
fun TayammumTabContent() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF193427))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ЧТО ТАКОЕ ТАЯММУМ?",
                        color = GoldAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Сухое ритуальное очищение землей/песком/камнем, заменяющее малое и полное омовение в случае отсутствия воды или опасности для здоровья от ее использования.",
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        items(WuduRepository.tayammumSteps) { step ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13221A))
            ) {
                Text(
                    text = step,
                    color = Color(0xFFD6E8DE),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}

@Composable
fun NullifiersTabContent() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "ДЕЙСТВИЯ, НАРУШАЮЩИЕ ОМОВЕНИЕ",
                color = GoldAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(WuduRepository.nullifiers) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF15261D))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null,
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = item,
                        color = Color(0xFFE2EFE7),
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}
