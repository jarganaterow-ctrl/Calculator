package com.example.namazapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.namazapp.data.model.Gender
import com.example.namazapp.data.model.Madhhab
import com.example.namazapp.data.model.PostureType
import com.example.namazapp.ui.theme.EmeraldLight
import com.example.namazapp.ui.theme.GoldAccent

@Composable
fun PostureIllustration(
    posture: PostureType,
    madhhab: Madhhab,
    gender: Gender,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF13221A))
            .border(1.dp, GoldAccent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Фоновые круги декора
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFF1B3326),
                radius = size.minDimension * 0.45f,
                center = Offset(size.width / 2, size.height / 2)
            )
        }

        // Рисуем стилизованную фигуру в зависимости от позы
        Canvas(modifier = Modifier.size(140.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val strokeWidth = 5f
            val figureColor = Color(0xFFE2F0D9)
            val headRadius = 14f

            when (posture) {
                PostureType.INTENTION, PostureType.TAKBEER -> {
                    // Стояние: Голова
                    drawCircle(figureColor, radius = headRadius, center = Offset(center.x, center.y - 45f))
                    // Тело
                    drawLine(figureColor, Offset(center.x, center.y - 30f), Offset(center.x, center.y + 15f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    // Ноги
                    drawLine(figureColor, Offset(center.x, center.y + 15f), Offset(center.x - 12f, center.y + 55f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(figureColor, Offset(center.x, center.y + 15f), Offset(center.x + 12f, center.y + 55f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    // Руки подняты к ушам/плечам
                    drawLine(figureColor, Offset(center.x, center.y - 25f), Offset(center.x - 24f, center.y - 35f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(figureColor, Offset(center.x - 24f, center.y - 35f), Offset(center.x - 24f, center.y - 50f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(figureColor, Offset(center.x, center.y - 25f), Offset(center.x + 24f, center.y - 35f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(figureColor, Offset(center.x + 24f, center.y - 35f), Offset(center.x + 24f, center.y - 50f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                }

                PostureType.QIYAM_FOLDED -> {
                    // Голова
                    drawCircle(figureColor, radius = headRadius, center = Offset(center.x, center.y - 45f))
                    // Тело
                    drawLine(figureColor, Offset(center.x, center.y - 30f), Offset(center.x, center.y + 15f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    // Ноги
                    drawLine(figureColor, Offset(center.x, center.y + 15f), Offset(center.x - 10f, center.y + 55f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(figureColor, Offset(center.x, center.y + 15f), Offset(center.x + 10f, center.y + 55f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    // Руки сложены на животе или груди
                    val handY = if (madhhab == Madhhab.HANAFI && gender == Gender.MALE) center.y + 5f else center.y - 12f
                    drawLine(figureColor, Offset(center.x, center.y - 25f), Offset(center.x - 18f, handY), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(figureColor, Offset(center.x, center.y - 25f), Offset(center.x + 18f, handY), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(GoldAccent, Offset(center.x - 18f, handY), Offset(center.x + 18f, handY), strokeWidth = strokeWidth + 1f, cap = StrokeCap.Round)
                }

                PostureType.QIYAM_SADL -> {
                    // Руки опущены вдоль тела (Садль - Малики)
                    drawCircle(figureColor, radius = headRadius, center = Offset(center.x, center.y - 45f))
                    drawLine(figureColor, Offset(center.x, center.y - 30f), Offset(center.x, center.y + 15f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(figureColor, Offset(center.x, center.y + 15f), Offset(center.x - 10f, center.y + 55f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(figureColor, Offset(center.x, center.y + 15f), Offset(center.x + 10f, center.y + 55f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    // Руки по бокам
                    drawLine(figureColor, Offset(center.x, center.y - 25f), Offset(center.x - 16f, center.y + 10f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(figureColor, Offset(center.x, center.y - 25f), Offset(center.x + 16f, center.y + 10f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                }

                PostureType.RUKU -> {
                    // Поясной поклон (наклон влево)
                    val hip = Offset(center.x + 20f, center.y)
                    val shoulder = Offset(center.x - 25f, center.y)
                    val head = Offset(center.x - 42f, center.y)
                    val knees = Offset(center.x + 20f, center.y + 25f)
                    val feet = Offset(center.x + 20f, center.y + 55f)

                    drawCircle(figureColor, radius = headRadius, center = head)
                    drawLine(figureColor, shoulder, hip, strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(figureColor, hip, knees, strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(figureColor, knees, feet, strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    // Руки держат колени
                    drawLine(GoldAccent, shoulder, knees, strokeWidth = strokeWidth, cap = StrokeCap.Round)
                }

                PostureType.ITIDAL -> {
                    // Выпрямление после рукуъ
                    drawCircle(figureColor, radius = headRadius, center = Offset(center.x, center.y - 45f))
                    drawLine(figureColor, Offset(center.x, center.y - 30f), Offset(center.x, center.y + 15f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(figureColor, Offset(center.x, center.y + 15f), Offset(center.x - 10f, center.y + 55f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(figureColor, Offset(center.x, center.y + 15f), Offset(center.x + 10f, center.y + 55f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(figureColor, Offset(center.x, center.y - 25f), Offset(center.x - 18f, center.y - 2f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(figureColor, Offset(center.x, center.y - 25f), Offset(center.x + 18f, center.y - 2f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                }

                PostureType.SUJUD -> {
                    // Земной поклон
                    val groundY = center.y + 45f
                    // Земля
                    drawLine(Color(0xFF2C4A38), Offset(center.x - 65f, groundY), Offset(center.x + 65f, groundY), strokeWidth = 3f)
                    // Голова на земле
                    drawCircle(figureColor, radius = headRadius, center = Offset(center.x - 40f, groundY - headRadius))
                    // Спина поднята
                    val hip = Offset(center.x + 10f, groundY - 35f)
                    val shoulder = Offset(center.x - 25f, groundY - 15f)
                    drawLine(figureColor, shoulder, hip, strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    // Бедра к коленям
                    val knee = Offset(center.x + 25f, groundY - 6f)
                    val feet = Offset(center.x + 45f, groundY - 6f)
                    drawLine(figureColor, hip, knee, strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(figureColor, knee, feet, strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    // Руки на земле рядом с головой
                    drawLine(GoldAccent, shoulder, Offset(center.x - 25f, groundY - 4f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                }

                PostureType.SITTING_JALSA, PostureType.SITTING_TASHAHHUD -> {
                    // Сидение на коленях
                    val groundY = center.y + 45f
                    drawLine(Color(0xFF2C4A38), Offset(center.x - 60f, groundY), Offset(center.x + 60f, groundY), strokeWidth = 3f)
                    val head = Offset(center.x - 5f, center.y - 20f)
                    drawCircle(figureColor, radius = headRadius, center = head)
                    val hip = Offset(center.x - 5f, groundY - 15f)
                    drawLine(figureColor, Offset(center.x - 5f, center.y - 5f), hip, strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    val knees = Offset(center.x - 30f, groundY - 6f)
                    val feet = Offset(center.x + 20f, groundY - 6f)
                    drawLine(figureColor, hip, knees, strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(figureColor, hip, feet, strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    // Руки на коленях
                    drawLine(GoldAccent, Offset(center.x - 5f, center.y), knees, strokeWidth = strokeWidth, cap = StrokeCap.Round)
                }

                PostureType.SALAM_RIGHT -> {
                    // Поворот направо
                    drawCircle(figureColor, radius = headRadius, center = Offset(center.x, center.y - 20f))
                    // Стрелка поворота вправо
                    drawLine(GoldAccent, Offset(center.x + 10f, center.y - 20f), Offset(center.x + 35f, center.y - 20f), strokeWidth = 4f, cap = StrokeCap.Round)
                    drawLine(GoldAccent, Offset(center.x + 35f, center.y - 20f), Offset(center.x + 26f, center.y - 28f), strokeWidth = 4f, cap = StrokeCap.Round)
                    drawLine(GoldAccent, Offset(center.x + 35f, center.y - 20f), Offset(center.x + 26f, center.y - 12f), strokeWidth = 4f, cap = StrokeCap.Round)
                    val groundY = center.y + 45f
                    val hip = Offset(center.x, groundY - 15f)
                    drawLine(figureColor, Offset(center.x, center.y - 5f), hip, strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(figureColor, hip, Offset(center.x - 25f, groundY - 6f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                }

                PostureType.SALAM_LEFT -> {
                    // Поворот налево
                    drawCircle(figureColor, radius = headRadius, center = Offset(center.x, center.y - 20f))
                    // Стрелка поворота влево
                    drawLine(GoldAccent, Offset(center.x - 10f, center.y - 20f), Offset(center.x - 35f, center.y - 20f), strokeWidth = 4f, cap = StrokeCap.Round)
                    drawLine(GoldAccent, Offset(center.x - 35f, center.y - 20f), Offset(center.x - 26f, center.y - 28f), strokeWidth = 4f, cap = StrokeCap.Round)
                    drawLine(GoldAccent, Offset(center.x - 35f, center.y - 20f), Offset(center.x - 26f, center.y - 12f), strokeWidth = 4f, cap = StrokeCap.Round)
                    val groundY = center.y + 45f
                    val hip = Offset(center.x, groundY - 15f)
                    drawLine(figureColor, Offset(center.x, center.y - 5f), hip, strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(figureColor, hip, Offset(center.x + 25f, groundY - 6f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                }
            }
        }

        // Лейбл позы внизу слева
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .background(Color(0x99000000), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = when (posture) {
                    PostureType.INTENTION -> "Намерение"
                    PostureType.TAKBEER -> "Такбир"
                    PostureType.QIYAM_FOLDED -> "Кыям (Руки сложены)"
                    PostureType.QIYAM_SADL -> "Кыям (Руки опущены)"
                    PostureType.RUKU -> "Рукуъ (Поясной поклон)"
                    PostureType.ITIDAL -> "И'тидаль (Выпрямление)"
                    PostureType.SUJUD -> "Саджда (Земной поклон)"
                    PostureType.SITTING_JALSA -> "Джалса (Пауза)"
                    PostureType.SITTING_TASHAHHUD -> "Ташаххуд (Сидение)"
                    PostureType.SALAM_RIGHT -> "Салям направо"
                    PostureType.SALAM_LEFT -> "Салям налево"
                },
                color = GoldAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Лейбл мазхаба и пола вверху справа
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(EmeraldLight.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                .border(0.5.dp, EmeraldLight.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${madhhab.displayName} • ${gender.displayName}",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
