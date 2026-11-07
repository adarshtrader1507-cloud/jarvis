package com.jarvis.assistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.State

/**
 * A genuinely working offline calculator (Phase 7) — supports + − × ÷, decimals and
 * parentheses via a small recursive-descent evaluator. No third-party expression lib needed.
 */
class CalculatorViewModel : ViewModel() {
    private val _expression = mutableStateOf("")
    val expression: State<String> get() = _expression

    private val _result = mutableStateOf("")
    val result: State<String> get() = _result

    fun append(token: String) {
        _expression.value += token
        evaluateLive()
    }

    fun clear() {
        _expression.value = ""
        _result.value = ""
    }

    fun backspace() {
        if (_expression.value.isNotEmpty()) _expression.value = _expression.value.dropLast(1)
        evaluateLive()
    }

    fun equals() {
        evaluateLive()
        if (_result.value.isNotEmpty() && _result.value != "Error") {
            _expression.value = _result.value
        }
    }

    private fun evaluateLive() {
        _result.value = try {
            val value = ExpressionEvaluator.evaluate(_expression.value)
            if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
        } catch (e: Exception) {
            if (_expression.value.isBlank()) "" else "Error"
        }
    }
}

/** Minimal recursive-descent arithmetic parser: + - * / ( ) and decimals. */
private object ExpressionEvaluator {
    fun evaluate(input: String): Double {
        val tokens = input.replace("×", "*").replace("÷", "/")
        val parser = Parser(tokens)
        val value = parser.parseExpression()
        return value
    }

    private class Parser(private val s: String) {
        var pos = 0

        fun parseExpression(): Double {
            var value = parseTerm()
            while (pos < s.length) {
                when (s[pos]) {
                    '+' -> { pos++; value += parseTerm() }
                    '-' -> { pos++; value -= parseTerm() }
                    else -> return value
                }
            }
            return value
        }

        private fun parseTerm(): Double {
            var value = parseFactor()
            while (pos < s.length) {
                when (s[pos]) {
                    '*' -> { pos++; value *= parseFactor() }
                    '/' -> { pos++; value /= parseFactor() }
                    else -> return value
                }
            }
            return value
        }

        private fun parseFactor(): Double {
            if (pos < s.length && s[pos] == '(') {
                pos++
                val value = parseExpression()
                if (pos < s.length && s[pos] == ')') pos++
                return value
            }
            if (pos < s.length && s[pos] == '-') {
                pos++
                return -parseFactor()
            }
            val start = pos
            while (pos < s.length && (s[pos].isDigit() || s[pos] == '.')) pos++
            if (start == pos) throw NumberFormatException("Unexpected char at $pos")
            return s.substring(start, pos).toDouble()
        }
    }
}
