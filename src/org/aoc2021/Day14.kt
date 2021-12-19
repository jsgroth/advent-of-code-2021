package org.aoc2021

import java.nio.file.Files
import java.nio.file.Path

object Day14 {
    private const val iterationsPart1 = 10
    private const val iterationsPart2 = 40

    private fun solve(lines: List<String>, iterations: Int): Long {
        val start = lines[0]

        val rules = lines.drop(2).map { it.split(" -> ") }.associate { it[0] to it[1] }

        val startMap = start.windowed(2).groupBy { it }.mapValues { (_, values) -> values.size.toLong() }

        val processed = (1..iterations).fold(startMap) { polymer, _ ->
            doIteration(polymer, rules)
        }

        val countsByChar = processed.entries.flatMap { (pair, count) -> listOf( pair[0] to count, pair[1] to count ) }
            .groupBy(Pair<Char, Long>::first, Pair<Char, Long>::second)
            .mapValues { (_, counts) -> counts.sum() }
            .mapValues { (c, count) ->
                if (c == start.first() || c == start.last()) {
                    if (start.first() == start.last()) {
                        (count + 2) / 2
                    } else {
                        (count + 1) / 2
                    }
                } else {
                    count / 2
                }
            }

        return countsByChar.values.maxOrNull()!! - countsByChar.values.minOrNull()!!
    }

    private fun doIteration(polymer: Map<String, Long>, rules: Map<String, String>): Map<String, Long> {
        val newMap = mutableMapOf<String, Long>()
        polymer.forEach { (pair, count) ->
            val rule = rules[pair]
            if (rule != null) {
                newMap.merge(pair[0] + rule, count) { a, b -> a + b }
                newMap.merge(rule + pair[1], count) { a, b -> a + b }
            } else {
                newMap.merge(pair, count) { a, b -> a + b }
            }
        }
        return newMap.toMap()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val lines = Files.readAllLines(Path.of("input14.txt"), Charsets.UTF_8)
        val solution1 = solve(lines, iterationsPart1)
        println(solution1)
        val solution2 = solve(lines, iterationsPart2)
        println(solution2)
    }
}