package org.aoc2021

import java.nio.file.Files
import java.nio.file.Path

object Day1 {
    private const val windowSize = 3

    private fun solvePart1(filename: String): Int {
        val (increases, _) = Files.readAllLines(Path.of(filename), Charsets.UTF_8)
            .map(String::toInt)
            .fold(0 to Integer.MAX_VALUE) { (increases, lastValue), value ->
                if (value > lastValue) (increases + 1 to value) else (increases to value)
            }
        return increases
    }

    private fun solvePart2(filename: String): Int {
        val (increases, _) = Files.readAllLines(Path.of(filename), Charsets.UTF_8)
            .map(String::toInt)
            .windowed(windowSize)
            .fold(0 to Integer.MAX_VALUE) { (increases, lastSum), window ->
                val windowSum = window.sum()
                if (windowSum > lastSum) (increases + 1 to windowSum) else (increases to windowSum)
            }
        return increases
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val filename = "input1.txt"
        val solution1 = solvePart1(filename)
        println(solution1)
        val solution2 = solvePart2(filename)
        println(solution2)
    }
}