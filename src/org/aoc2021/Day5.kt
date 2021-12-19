package org.aoc2021

import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

object Day5 {
    data class Vent(val x1: Int, val y1: Int, val x2: Int, val y2: Int)

    private fun solvePart1(filename: String): Int {
        val vents = Files.readAllLines(Path.of(filename), Charsets.UTF_8)
            .map(Day5::parseLine)

        val counts = emptyCountsGrid(vents)

        vents.forEach { vent ->
            if (vent.x1 == vent.x2) {
                val start = min(vent.y1, vent.y2)
                val end = max(vent.y1, vent.y2)
                for (j in start..end) {
                    counts[vent.x1][j]++
                }
            }

            if (vent.y1 == vent.y2) {
                val start = min(vent.x1, vent.x2)
                val end = max(vent.x1, vent.x2)
                for (i in start..end) {
                    counts[i][vent.y1]++
                }
            }
        }

        return counts.sumOf { column ->
            column.count { it >= 2 }
        }
    }

    private fun solvePart2(filename: String): Int {
        val vents = Files.readAllLines(Path.of(filename), Charsets.UTF_8)
            .map(Day5::parseLine)

        val counts = emptyCountsGrid(vents)

        vents.forEach { vent ->
            val dx = (vent.x2 - vent.x1).sign
            val dy = (vent.y2 - vent.y1).sign

            val length = max(abs(vent.x2 - vent.x1), abs(vent.y2 - vent.y1))

            for (i in 0..length) {
                counts[vent.x1 + dx * i][vent.y1 + dy * i]++
            }
        }

        return counts.sumOf { column ->
            column.count { it >= 2 }
        }
    }

    private fun emptyCountsGrid(vents: List<Vent>): Array<Array<Int>> {
        val maxX = vents.maxOf { max(it.x1, it.x2) } + 1
        val maxY = vents.maxOf { max(it.y1, it.y2) } + 1

        return Array(maxX) { Array(maxY) { 0 } }
    }

    private fun parseLine(line: String): Vent {
        val (p1, p2) = line.split(" -> ")
        val (x1, y1) = p1.split(",").map(String::toInt)
        val (x2, y2) = p2.split(",").map(String::toInt)
        return Vent(x1, y1, x2, y2)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val filename = "input5.txt"
        val solution1 = solvePart1(filename)
        println(solution1)
        val solution2 = solvePart2(filename)
        println(solution2)
    }
}