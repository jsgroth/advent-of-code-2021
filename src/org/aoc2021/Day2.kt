package org.aoc2021

import java.nio.file.Files
import java.nio.file.Path

object Day2 {
    private fun solvePart1(filename: String): Int {
        val (distance, depth) = Files.readAllLines(Path.of(filename), Charsets.UTF_8)
            .fold(0 to 0) { (distance, depth), line ->
                val (direction, value) = line.split(" ").let { it[0] to it[1].toInt() }
                when (direction) {
                    "forward" -> (distance + value) to depth
                    "up" -> distance to (depth - value)
                    "down" -> distance to (depth + value)
                    else -> throw IllegalArgumentException("invalid line: $line")
                }
            }

        return distance * depth
    }

    private fun solvePart2(filename: String): Int {
        val (distance, depth, _) = Files.readAllLines(Path.of(filename), Charsets.UTF_8)
            .fold(Triple(0, 0, 0)) { (distance, depth, aim), line ->
                val (direction, value) = line.split(" ").let { it[0] to it[1].toInt() }
                when (direction) {
                    "forward" -> Triple(distance + value, depth + aim * value, aim)
                    "up" -> Triple(distance, depth, aim - value)
                    "down" -> Triple(distance, depth, aim + value)
                    else -> throw IllegalArgumentException("invalid line: $line")
                }
            }

        return distance * depth
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val filename = "input2.txt"
        val solution1 = solvePart1(filename)
        println(solution1)
        val solution2 = solvePart2(filename)
        println(solution2)
    }
}