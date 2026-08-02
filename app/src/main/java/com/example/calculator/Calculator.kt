package com.example.calculator

data class CalculatorState(
    val display: String = "0",
    val expression: String = "",
    val currentOperand: String = "",
    val currentOperator: String = "",
    val previousOperand: String = "",
    val shouldResetDisplay: Boolean = false
)

class CalculatorEngine {

    fun handleInput(state: CalculatorState, input: String): CalculatorState {
        return when (input) {
            "C" -> CalculatorState()
            "⌫" -> handleBackspace(state)
            "±" -> handleNegate(state)
            "%" -> handlePercent(state)
            "÷", "×", "-", "+" -> handleOperator(state, input)
            "=" -> handleEquals(state)
            "." -> handleDecimal(state)
            else -> handleDigit(state, input)
        }
    }

    private fun handleDigit(state: CalculatorState, digit: String): CalculatorState {
        val current = if (state.shouldResetDisplay) "" else state.display
        val newDisplay = if (current == "0" || current == "Ошибка") digit else current + digit

        return state.copy(
            display = newDisplay,
            currentOperand = newDisplay,
            shouldResetDisplay = false
        )
    }

    private fun handleDecimal(state: CalculatorState): CalculatorState {
        val display = if (state.shouldResetDisplay) "0" else state.display
        if (display.contains(".")) return state

        val newDisplay = "$display."
        return state.copy(
            display = newDisplay,
            currentOperand = newDisplay,
            shouldResetDisplay = false
        )
    }

    private fun handleOperator(state: CalculatorState, op: String): CalculatorState {
        val current = state.currentOperand.toDoubleOrNull()
            ?: return state

        // Если уже есть оператор и предыдущий операнд — цепочка вычислений
        if (state.previousOperand.isNotEmpty() && state.currentOperator.isNotEmpty() && !state.shouldResetDisplay) {
            val result = calculate(
                state.previousOperand.toDoubleOrNull() ?: return state,
                current,
                state.currentOperator
            )
            val resultStr = formatResult(result)
            return state.copy(
                display = resultStr,
                expression = "$resultStr $op",
                currentOperand = resultStr,
                previousOperand = resultStr,
                currentOperator = op,
                shouldResetDisplay = true
            )
        }

        return state.copy(
            display = formatDisplay(current),
            expression = "${formatDisplay(current)} $op",
            previousOperand = current,
            currentOperator = op,
            shouldResetDisplay = true
        )
    }

    private fun handleEquals(state: CalculatorState): CalculatorState {
        val second = state.currentOperand.toDoubleOrNull()
        val first = state.previousOperand.toDoubleOrNull()
        val op = state.currentOperator

        if (second == null || first == null || op.isEmpty()) return state

        val result = calculate(first, second, op)
        val resultStr = formatResult(result)
        val expr = "${formatDisplay(first)} $op ${formatDisplay(second)} ="

        return state.copy(
            display = resultStr,
            expression = expr,
            currentOperand = resultStr,
            previousOperand = "",
            currentOperator = "",
            shouldResetDisplay = true
        )
    }

    private fun handleNegate(state: CalculatorState): CalculatorState {
        val display = state.display
        if (display.isEmpty() || display == "0") return state

        val newDisplay = if (display.startsWith("-")) display.drop(1) else "-$display"
        return state.copy(
            display = newDisplay,
            currentOperand = newDisplay
        )
    }

    private fun handlePercent(state: CalculatorState): CalculatorState {
        val current = state.currentOperand.toDoubleOrNull() ?: return state
        val result = current / 100.0
        val resultStr = formatResult(result)

        return state.copy(
            display = resultStr,
            currentOperand = resultStr
        )
    }

    private fun handleBackspace(state: CalculatorState): CalculatorState {
        val display = state.display
        if (display.isEmpty() || display == "0" || state.shouldResetDisplay) return state

        val newDisplay = if (display.length <= 1 || (display.startsWith("-") && display.length == 2)) {
            "0"
        } else {
            display.dropLast(1)
        }

        return state.copy(
            display = newDisplay,
            currentOperand = newDisplay
        )
    }

    private fun calculate(a: Double, b: Double, operator: String): Double {
        return when (operator) {
            "+" -> a + b
            "-" -> a - b
            "×" -> a * b
            "÷" -> {
                if (b == 0.0) return Double.NaN
                a / b
            }
            else -> b
        }
    }

    private fun formatResult(value: Double): String {
        if (value.isNaN()) return "Ошибка"
        if (value.isInfinite()) return "Ошибка"

        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            val formatted = "%.10f".format(value).trimEnd('0').trimEnd('.')
            formatted
        }
    }

    private fun formatDisplay(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            value.toString()
        }
    }
}