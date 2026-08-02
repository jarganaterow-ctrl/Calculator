package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorApp()
        }
    }
}

// Цветовая схема
private val DarkBackground = Color(0xFF1C1C1E)
private val DisplayBackground = Color(0xFF1C1C1E)
private val NumberButton = Color(0xFF505050)
private val OperatorButton = Color(0xFFFF9500)
private val FunctionButton = Color(0xFFD4D4D2)
private val TextWhite = Color.White
private val TextBlack = Color.Black

@Composable
fun CalculatorApp() {
    val engine = remember { CalculatorEngine() }
    var state by remember { mutableStateOf(CalculatorState()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .systemBarsPadding()
    ) {
        // Дисплей
        DisplayPanel(
            expression = state.expression,
            display = state.display,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        // Кнопки
        ButtonsPanel(
            onButtonClick = { input ->
                state = engine.handleInput(state, input)
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
        )
    }
}

@Composable
fun DisplayPanel(
    expression: String,
    display: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Выражение (вверху)
        Text(
            text = expression,
            color = Color(0xFF8E8E93),
            fontSize = 24.sp,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Основной результат (внизу)
        Text(
            text = display,
            color = TextWhite,
            fontSize = if (display.length > 10) 48.sp else 64.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ButtonsPanel(
    onButtonClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val buttons = listOf(
        listOf("C", "±", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("⌫", "0", ".", "=")
    )

    Column(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { label ->
                    val isOperator = label in listOf("÷", "×", "-", "+", "=")
                    val isFunction = label in listOf("C", "±", "%", "⌫")
                    val isZero = label == "0"

                    val bgColor = when {
                        isOperator -> OperatorButton
                        isFunction -> FunctionButton
                        else -> NumberButton
                    }

                    val textColor = when {
                        isFunction -> Color.Black
                        else -> TextWhite
                    }

                    val weightModifier = if (isZero) Modifier.weight(2f) else Modifier.weight(1f)

                    Box(
                        modifier = Modifier
                            .then(weightModifier)
                            .aspectRatio(if (isZero) 2f else 1f)
                            .clip(
                                if (isZero) RoundedCornerShape(percent = 50)
                                else CircleShape
                            )
                            .background(bgColor)
                            .clickable { onButtonClick(label) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = textColor,
                            fontSize = if (isZero) 28.sp else 28.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}