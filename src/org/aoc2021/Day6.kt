package org.aoc2021

import java.nio.file.Files
import java.nio.file.Path

object Day6 {
    private const val iterationsPart1 = 80
    private const val iterationsPart2 = 256

    private fun solve(lines: List<String>, iterations: Int): Long {
        var fishAgeToCount = lines[0].split(",")
            .groupBy(String::toInt)
            .mapValues { (_, values) -> values.size.toLong() }

        for (i in 0 until iterations) {
            val newMap = mutableMapOf<Int, Long>()

            fishAgeToCount.entries.forEach { (fish, count) ->
                if (fish == 0) {
                    newMap.merge(6, count) { a, b -> a + b }
                    newMap.merge(8, count) { a, b -> a + b }
                } else {
                    newMap.merge(fish - 1, count) { a, b -> a + b }
                }
            }

            fishAgeToCount = newMap.toMap()
        }

        return fishAgeToCount.values.sum()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val lines = Files.readAllLines(Path.of("input6.txt"), Charsets.UTF_8)
        val solution1 = solve(lines, iterationsPart1)
        println(solution1)
        val solution2 = solve(lines, iterationsPart2)
        println(solution2)
    }
}