package org.aoc2021

import java.nio.file.Files
import java.nio.file.Path

object Day9 {
    data class Point(val x: Int, val y: Int)

    private fun solvePart1(lines: List<String>): Int {
        val heights = lines.map { line ->
            line.map(Char::digitToInt)
        }
        val lowPoints = findLowPoints(heights)
        return lowPoints.sumOf { p ->
            1 + heights[p.x][p.y]
        }
    }

    private fun solvePart2(lines: List<String>): Int {
        val heights = lines.map { line ->
            line.map(Char::digitToInt)
        }
        val lowPoints = findLowPoints(heights)

        val basinMarkers = findBasins(heights, lowPoints)

        return basinMarkers.flatten()
            .filter { it != 0 }
            .groupBy { it }
            .map { (_, values) -> values.size }
            .sortedDescending()
            .take(3)
            .reduce { a, b -> a * b }
    }

    private fun findLowPoints(heights: List<List<Int>>): List<Point> {
        val lowPoints = mutableListOf<Point>()
        for (i in heights.indices) {
            for (j in heights[0].indices) {
                if (
                    (i == 0 || heights[i][j] < heights[i-1][j]) &&
                    (i == heights.size - 1 || heights[i][j] < heights[i+1][j]) &&
                    (j == 0 || heights[i][j] < heights[i][j-1]) &&
                    (j == heights[0].size - 1 || heights[i][j] < heights[i][j+1])
                ) {
                    lowPoints.add(Point(i, j))
                }
            }
        }
        return lowPoints.toList()
    }

    private fun findBasins(heights: List<List<Int>>, lowPoints: List<Point>): Array<Array<Int>> {
        val basinMarkers = Array(heights.size) { Array(heights[0].size) { 0 } }

        lowPoints.forEachIndexed { index, p ->
            val marker = index + 1
            fill(p, marker, heights, basinMarkers)
        }

        return basinMarkers
    }

    private fun fill(p: Point, marker: Int, heights: List<List<Int>>, basinMarkers: Array<Array<Int>>) {
        val (x, y) = p

        basinMarkers[x][y] = marker

        listOf(
            -1 to 0,
            1 to 0,
            0 to -1,
            0 to 1,
        ).forEach { (dx, dy) ->
            val i = x + dx
            val j = y + dy

            if (
                i >= 0 && i < heights.size &&
                j >= 0 && j < heights[0].size &&
                heights[i][j] != 9 && basinMarkers[i][j] == 0
            ) {
                fill(Point(i, j), marker, heights, basinMarkers)
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val lines = Files.readAllLines(Path.of("input9.txt"), Charsets.UTF_8)
        val solution1 = solvePart1(lines)
        println(solution1)
        val solution2 = solvePart2(lines)
        println(solution2)
    }
}