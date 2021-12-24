package org.aoc2021

import java.nio.file.Files
import java.nio.file.Path

private fun <K, V> Map<K, V>.replaceKeyValue(k: K, v: V): Map<K, V> {
    return this.entries.associate { (thisKey, thisValue) ->
        if (thisKey == k) (k to v) else (thisKey to thisValue)
    }
}

object Day24 {
    data class Instruction(val operator: String, val a: String, val b: String? = null)

    private fun solve(lines: List<String>, findMax: Boolean): String {
        val program = parseProgram(lines)

        val expressions = generateExpressions(program)

        expressions.find { (_, variables) -> variables["z"] == LiteralValue(0) }?.let { (conditions, _) ->
            if (conditions.any { !it.value }) {
                throw IllegalStateException("unexpected condition: $conditions")
            }

            val result = Array(14) { 0 }
            conditions.forEach { condition ->
                val compare = condition.compare
                if (compare.a !is Add || compare.a.a !is Input || compare.a.b !is LiteralValue || compare.b !is Input) {
                    throw IllegalStateException("unexpected compare: $compare")
                }

                if (compare.a.b.value < 0) {
                    result[compare.a.a.index] = if (findMax) 9 else (1 - compare.a.b.value)
                    result[compare.b.index] = result[compare.a.a.index] + compare.a.b.value
                } else {
                    result[compare.b.index] = if (findMax) 9 else (1 + compare.a.b.value)
                    result[compare.a.a.index] = result[compare.b.index] - compare.a.b.value
                }
            }

            return result.map(Int::digitToChar).joinToString(separator = "")
        }

        throw IllegalArgumentException("no solution found")
    }

    sealed interface Expression {
        fun simplify(): Expression
    }

    data class Input(val index: Int) : Expression {
        override fun simplify() = this

        override fun toString() = "input$index"
    }

    data class LiteralValue(val value: Int) : Expression {
        override fun simplify() = this

        override fun toString() = value.toString()
    }

    data class Add(val a: Expression, val b: Expression) : Expression {
        override fun simplify(): Expression {
            return if (a is LiteralValue && b is LiteralValue) {
                LiteralValue(a.value + b.value)
            } else if (a == LiteralValue(0)) {
                b
            } else if (b == LiteralValue(0)) {
                a
            } else if (a is Add && a.b is LiteralValue && b is LiteralValue) {
                Add(a.a, LiteralValue(a.b.value + b.value)).simplify()
            } else {
                this
            }
        }

        override fun toString() = "($a + $b)"
    }

    data class Multiply(val a: Expression, val b: Expression) : Expression {
        override fun simplify(): Expression {
            return if (a is LiteralValue && b is LiteralValue) {
                LiteralValue(a.value * b.value)
            } else if (a == LiteralValue(0) || b == LiteralValue(0)) {
                LiteralValue(0)
            } else if (a == LiteralValue(1)) {
                b
            } else if (b == LiteralValue(1)) {
                a
            } else if (a is Multiply && a.b is LiteralValue && b is LiteralValue) {
                Multiply(a.a, LiteralValue(a.b.value * b.value)).simplify()
            } else {
                this
            }
        }

        override fun toString() = "($a * $b)"
    }

    data class Divide(val a: Expression, val b: Expression) : Expression {
        override fun simplify(): Expression {
            return if (a is LiteralValue && b is LiteralValue) {
                LiteralValue(a.value / b.value)
            } else if (a == LiteralValue(0)) {
                LiteralValue(0)
            } else if (b == LiteralValue(1)) {
                a
            } else if (a is Multiply && a.b is LiteralValue && b is LiteralValue && (a.b.value % b.value == 0)) {
                Multiply(a.a, LiteralValue(a.b.value / b.value)).simplify()
            } else if (a is Add && a.a is Input && a.b is LiteralValue && b is LiteralValue && a.b.value < b.value - 9) {
                LiteralValue(0)
            } else if (a is Add && a.b is Add && a.b.a is Input && a.b.b is LiteralValue && b is LiteralValue && a.b.b.value >= 0 && a.b.b.value < b.value - 9) {
                Divide(a.a, b).simplify()
            } else {
                this
            }
        }

        override fun toString() = "($a / $b)"
    }

    data class Modulo(val a: Expression, val b: Expression): Expression {
        override fun simplify(): Expression {
            return if (a is LiteralValue && b is LiteralValue) {
                LiteralValue(a.value % b.value)
            } else if (a == LiteralValue(0)) {
                LiteralValue(0)
            } else if (a is Add && b is LiteralValue) {
                Add(Modulo(a.a, b).simplify(), Modulo(a.b, b).simplify()).simplify()
            } else if (a is Multiply && a.b is LiteralValue && b is LiteralValue && (a.b.value % b.value == 0)) {
                LiteralValue(0)
            } else if (a is Input && b is LiteralValue && b.value > 9) {
                a
            } else {
                this
            }
        }

        override fun toString() = "($a % $b)"
    }

    data class Compare(val a: Expression, val b: Expression, val invert: Boolean = false): Expression {
        override fun simplify(): Expression {
            return if (a is LiteralValue && b is LiteralValue) {
                LiteralValue(if (a.value == b.value) 1 else 0)
            } else if (a is Compare && b == LiteralValue(0)) {
                Compare(a.a, a.b, invert = true).simplify()
            } else if (isConditionImpossible(a, b)) {
                LiteralValue(if (invert) 1 else 0)
            } else {
                this
            }
        }

        override fun toString() = if (!invert) {
            "($a == $b ? 1 : 0)"
        } else {
            "($a == $b ? 0 : 1)"
        }
    }

    private fun isConditionImpossible(a: Expression, b: Expression): Boolean {
        if (b !is Input) {
            return false
        }

        if (a is LiteralValue && (a.value < 1 || a.value > 9)) {
            return true
        }

        if (a is Add && a.a is Input && a.b is LiteralValue && a.b.value >= 9) {
            return true
        }

        if (a is Add && a.a is Modulo && a.b is LiteralValue && a.b.value > 9) {
            return true
        }

        return false
    }

    data class Condition(val compare: Compare, val value: Boolean)

    private fun generateExpressions(
        program: List<Instruction>,
        initialState: Map<String, Expression> = listOf("w", "x", "y", "z").associateWith { LiteralValue(0) },
        i: Int = 0,
        initialInputIndex: Int = 0,
        conditions: List<Condition> = listOf(),
    ): List<Pair<List<Condition>, Map<String, Expression>>> {
        val variables = initialState.toMutableMap()

        var inputIndex = initialInputIndex
        for (j in i until program.size) {
            val instruction = program[j]
            when (instruction.operator) {
                "inp" -> {
                    variables[instruction.a] = Input(inputIndex)
                    inputIndex++
                }
                "add" -> {
                    val aValue = variables[instruction.a]!!
                    val bValue = getExpressionValueOf(instruction.b!!, variables)
                    variables[instruction.a] = Add(aValue, bValue).simplify()
                }
                "mul" -> {
                    val aValue = variables[instruction.a]!!
                    val bValue = getExpressionValueOf(instruction.b!!, variables)
                    variables[instruction.a] = Multiply(aValue, bValue).simplify()
                }
                "div" -> {
                    val aValue = variables[instruction.a]!!
                    val bValue = getExpressionValueOf(instruction.b!!, variables)
                    variables[instruction.a] = Divide(aValue, bValue).simplify()
                }
                "mod" -> {
                    val aValue = variables[instruction.a]!!
                    val bValue = getExpressionValueOf(instruction.b!!, variables)
                    variables[instruction.a] = Modulo(aValue, bValue).simplify()
                }
                "eql" -> {
                    val aValue = variables[instruction.a]!!
                    val bValue = getExpressionValueOf(instruction.b!!, variables)
                    val compare = Compare(aValue, bValue).simplify()
                    if (compare is Compare) {
                        val oneBranch = variables.replaceKeyValue(instruction.a, LiteralValue(1))
                        val zeroBranch = variables.replaceKeyValue(instruction.a, LiteralValue(0))
                        return generateExpressions(program, oneBranch.toMap(), j + 1, inputIndex, conditions.plus(Condition(compare, true)))
                            .plus(generateExpressions(program, zeroBranch.toMap(), j + 1, inputIndex, conditions.plus(Condition(compare, false))))
                    }
                    variables[instruction.a] = compare
                }
            }
        }

        return listOf(conditions to variables.toMap())
    }

    private fun getExpressionValueOf(value: String, variables: Map<String, Expression>): Expression {
        return variables[value] ?: LiteralValue(value.toInt())
    }

    private fun parseProgram(lines: List<String>): List<Instruction> {
        return lines.map { line ->
            val split = line.split(" ")
            if (split[0] == "inp") {
                Instruction(split[0], split[1])
            } else {
                Instruction(split[0], split[1], split[2])
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val lines = Files.readAllLines(Path.of("input24.txt"), Charsets.UTF_8)
        val solution1 = solve(lines, true)
        println(solution1)
        val solution2 = solve(lines, false)
        println(solution2)
    }
}