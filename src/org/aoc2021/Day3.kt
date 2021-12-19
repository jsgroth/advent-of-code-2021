package org.aoc2021

import java.nio.file.Files
import java.nio.file.Path

object Day3 {
    private fun solvePart1(filename: String): Int {
        val lines = Files.readAllLines(Path.of(filename), Charsets.UTF_8)
        val numBits = lines[0].length

        val oneCounts = Array(numBits) { 0 }
        lines.forEach { line ->
            line.forEachIndexed { i, bit ->
                if (bit == '1') {
                    oneCounts[i]++
                }
            }
        }

        val majorityLine = if (lines.size % 2 == 0) {
            lines.size / 2
        } else {
            lines.size / 2 + 1
        }

        val gammaString = oneCounts.map { if (it >= majorityLine) '1' else '0' }
            .joinToString(separator = "")
        val epsilonString = gammaString.map { if (it == '1') '0' else '1' }
            .joinToString(separator = "")

        return gammaString.toInt(2) * epsilonString.toInt(2)
    }

    private fun solvePart2(filename: String): Int {
        val lines = Files.readAllLines(Path.of(filename), Charsets.UTF_8)

        val oxygenRating = findRating(lines, false)
        val co2Rating = findRating(lines, true)

        return oxygenRating * co2Rating
    }

    private tailrec fun findRating(lines: List<String>, invert: Boolean, i: Int = 0): Int {
        if (lines.size == 1) {
            return lines[0].toInt(2)
        }

        val (leadingOnes, leadingZeroes) = lines.partition { it[i] == '1' }
        val mostCommonBit = if (leadingOnes.size >= leadingZeroes.size) '1' else '0'

        val remainingLines = lines.filter { line ->
            val matches = (line[i] == mostCommonBit)
            if (invert) !matches else matches
        }
        return findRating(remainingLines, invert, i + 1)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val filename = "input3.txt"
        val solution1 = solvePart1(filename)
        println(solution1)
        val solution2 = solvePart2(filename)
        println(solution2)
    }
}