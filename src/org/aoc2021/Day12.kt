package org.aoc2021

import java.nio.file.Files
import java.nio.file.Path

object Day12 {
    private fun solvePart1(lines: List<String>): Int {
        val points = parseLines(lines)

        return search("start", points)
    }

    private fun solvePart2(lines: List<String>): Int {
        val points = parseLines(lines)

        return searchPart2("start", points)
    }

    private fun parseLines(lines: List<String>): Map<String, List<String>> {
        return lines.flatMap { line ->
            val (b, e) = line.split("-")
            listOf(b to e, e to b)
        }
            .groupBy(Pair<String, String>::first, Pair<String, String>::second)
    }

    private fun search(p: String, points: Map<String, List<String>>, visitedSmallCaves: Set<String> = setOf()): Int {
        return points[p]!!.filter { edge ->
            edge != "start" && !visitedSmallCaves.contains(edge)
        }
            .sumOf { edge ->
                if (edge == "end") {
                    1
                } else {
                    val newVisitedCaves = if (edge.all(Char::isLowerCase)) {
                        visitedSmallCaves.plus(edge)
                    } else {
                        visitedSmallCaves
                    }
                    search(edge, points, newVisitedCaves)
                }
            }
    }

    private fun searchPart2(
        p: String,
        points: Map<String, List<String>>,
        visitedSmallCaves: Set<String> = setOf(),
        visitedTwice: Boolean = false,
    ): Int {
        return points[p]!!.filter { it != "start" }
            .sumOf { edge ->
                if (edge == "end") {
                    1
                } else if (visitedSmallCaves.contains(edge) && visitedTwice) {
                    0
                } else {
                    val newVisitedTwice = if (visitedSmallCaves.contains(edge)) true else visitedTwice

                    val newVisitedCaves = if (edge.all(Char::isLowerCase)) {
                        visitedSmallCaves.plus(edge)
                    } else {
                        visitedSmallCaves
                    }
                    searchPart2(edge, points, newVisitedCaves, newVisitedTwice)
                }
            }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val lines = Files.readAllLines(Path.of("input12.txt"), Charsets.UTF_8)
        val solution1 = solvePart1(lines)
        println(solution1)
        val solution2 = solvePart2(lines)
        println(solution2)
    }
}