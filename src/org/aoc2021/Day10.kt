package org.aoc2021

import java.nio.file.Files
import java.nio.file.Path

object Day10 {
    private val values = mapOf(
        ')' to 3,
        ']' to 57,
        '}' to 1197,
        '>' to 25137,
    )

    private fun solvePart1(lines: List<String>): Int {
        return lines.sumOf { line ->
            doLine(line)
        }
    }

    private fun solvePart2(lines: List<String>): Long {
        val scores = lines.filter { doLine(it) == 0 }
            .map { line ->
                solveIncompleteLine(line)
            }
            .sorted()
        return scores[scores.size / 2]
    }

    private fun doLine(line: String): Int {
        val chars = mutableListOf<Char>()

        for (c in line) {
            if (c in setOf('(', '[', '{', '<')) {
                chars.add(c)
            } else if (chars.isEmpty()) {
                return values[c]!!
            } else {
                if (
                    (c == ')' && chars.last() != '(') ||
                    (c == ']' && chars.last() != '[') ||
                    (c == '}' && chars.last() != '{') ||
                    (c == '>' && chars.last() != '<')
                ) {
                    return values[c]!!
                }
                chars.removeLast()
            }
        }

        return 0
    }

    private fun solveIncompleteLine(line: String): Long {
        val chars = mutableListOf<Char>()

        for (c in line) {
            if (c in setOf('(', '[', '{', '<')) {
                chars.add(c)
            } else {
                chars.removeLast()
            }
        }

        return chars.reversed().fold(0L) { p, c ->
            5 * p + when (c) {
                '(' -> 1
                '[' -> 2
                '{' -> 3
                '<' -> 4
                else -> throw IllegalArgumentException("$c")
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val lines = Files.readAllLines(Path.of("input10.txt"), Charsets.UTF_8)
        val solution1 = solvePart1(lines)
        println(solution1)
        val solution2 = solvePart2(lines)
        println(solution2)
    }
}