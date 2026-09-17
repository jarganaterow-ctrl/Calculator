package com.example.namazapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.namazapp.ui.theme.EmeraldLight
import com.example.namazapp.ui.theme.EmeraldPrimary
import com.example.namazapp.ui.theme.GoldAccent

data class DhikrPreset(
    val title: String,
    val arabic: String,
    val target: Int
)

val dhikrPresets = listOf(
    DhikrPreset("Субханаллах", "سُبْحَانَ اللَّهِ", 33),
    DhikrPreset("Альхамдулиллях", "الْحَمْدُ لِلَّهِ", 33),
    DhikrPreset("Аллаху Акбар", "اللَّهُ أَكْبَرُ", 33),
    DhikrPreset("Астагфируллах", "أَسْتَغْفِرُ اللَّهَ", 33),
    DhikrPreset("Ля иляха илля-Ллах", "لَا إِلَٰهَ إِلَّا اللَّهُ", 100),
    DhikrPreset("Салават Пророку ﷺ", "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ", 100)
)

@Composable
fun TasbihScreen(
    onBack: () -> Unit
) {
    var selectedPresetIndex by remember { mutableStateOf(0) }
    val currentPreset = dhikrPresets[selectedPresetIndex]

    var counter by remember { mutableStateOf(0) }
    var totalDhikrs by remember { mutableStateOf(0) }

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                            text = "Электронный Тасбих",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Зикр и поминание Аллаха",
                            color = GoldAccent,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = { counter = 0 },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1F382C))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Сброс",
                        tint = GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Выбор зикра
        ScrollableTabRow(
            selectedTabIndex = selectedPresetIndex,
            containerColor = Color(0xFF13241C),
            contentColor = GoldAccent,
            edgePadding = 16.dp,
            divider = {}
        ) {
            dhikrPresets.forEachIndexed { index, preset ->
                Tab(
                    selected = selectedPresetIndex == index,
                    onClick = {
                        selectedPresetIndex = index
                        counter = 0
                    },
                    text = {
                        Text(
                            text = preset.title,
                            color = if (selectedPresetIndex == index) GoldAccent else Color(0xFF86A596),
                            fontWeight = if (selectedPresetIndex == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        // Центральная зона нажатия
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clickable {
                    counter++
                    totalDhikrs++
                    if (counter >= currentPreset.target) {
                        // Переход на следующий зикр после 33, если это блок после намаза
                        if (selectedPresetIndex < 2) {
                            selectedPresetIndex++
                            counter = 0
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Арабский текст зикра
                Text(
                    text = currentPreset.arabic,
                    color = GoldAccent,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = currentPreset.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Круговой счетчик
                Box(
                    modifier = Modifier.size(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val progress = (counter % currentPreset.target).toFloat() / currentPreset.target.toFloat()

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Фоновый круг
                        drawCircle(
                            color = Color(0xFF172B20),
                            radius = size.minDimension / 2,
                            style = Stroke(width = 12.dp.toPx())
                        )
                        // Прогресс
                        drawArc(
                            color = GoldAccent,
                            startAngle = -90f,
                            sweepAngle = progress * 360f,
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$counter",
                            color = Color.White,
                            fontSize = 54.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "/ ${currentPreset.target}",
                            color = Color(0xFF8FAEA1),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Нажмите в любом месте экрана для счета",
                    color = Color(0xFF7B9B8C),
                    fontSize = 12.sp
                )
            }
        }

        // Нижняя статистика
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF13241C))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "ВСЕГО ПРОЧТЕНО", color = Color(0xFF8CAFA0), fontSize = 11.sp)
                    Text(text = "$totalDhikrs раз", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { counter = 0 },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3B2C)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Обнулить круг", color = GoldAccent, fontSize = 12.sp)
                }
            }
        }
    }
}
