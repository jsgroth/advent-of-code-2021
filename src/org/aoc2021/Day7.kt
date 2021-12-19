package org.aoc2021

import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.abs

object Day7 {
    private fun solvePart1(lines: List<String>): Int {
        val crabs = lines[0].split(",").map(String::toInt)

        val min = crabs.minOrNull()!!
        val max = crabs.maxOrNull()!!

        return (min..max).minOf { pos ->
            crabs.sumOf { crab -> abs(crab - pos) }
        }
    }

    private fun solvePart2(lines: List<String>): Int {
        val crabs = lines[0].split(",").map(String::toInt)

        val min = crabs.minOrNull()!!
        val max = crabs.maxOrNull()!!

        return (min..max).minOf { pos ->
            crabs.sumOf { crab ->
                val distance = abs(crab - pos)
                distance * (distance + 1) / 2
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val lines = Files.readAllLines(Path.of("input7.txt"), Charsets.UTF_8)
        val solution1 = solvePart1(lines)
        println(solution1)
        val solution2 = solvePart2(lines)
        println(solution2)
    }
}