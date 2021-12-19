package org.aoc2021

import java.nio.file.Files
import java.nio.file.Path

object Day13 {
    data class Point(val x: Int, val y: Int)

    data class Fold(val direction: String, val coordinate: Int)

    private fun solvePart1(lines: List<String>): Int {
        val (points, folds) = parseLines(lines)

        val grid = generateGrid(points)

        val folded = applyFold(grid, folds[0])

        return folded.sumOf { column -> column.count { it } }
    }

    private fun solvePart2(lines: List<String>) {
        val (points, folds) = parseLines(lines)

        val grid = generateGrid(points)

        val folded = folds.fold(grid) { newGrid, fold -> applyFold(newGrid, fold) }

        printGrid(folded)
    }

    private fun parseLines(lines: List<String>): Pair<List<Point>, List<Fold>> {
        val points = mutableListOf<Point>()
        val folds = mutableListOf<Fold>()

        lines.filter(String::isNotBlank).forEach { line ->
            if (line.startsWith("fold along ")) {
                val (direction, coordinate) = line.substringAfter("fold along ")
                    .split("=")
                    .let { it[0] to it[1].toInt() }

                folds.add(Fold(direction, coordinate))
            } else {
                val (x, y) = line.split(",").map(String::toInt)
                points.add(Point(x, y))
            }
        }

        return points.toList() to folds.toList()
    }

    private fun generateGrid(points: List<Point>): Array<Array<Boolean>> {
        val maxX = points.maxOf(Point::x) + 1
        val maxY = points.maxOf(Point::y) + 1

        val grid = Array(maxX) { Array(maxY) { false } }

        points.forEach { (x, y) -> grid[x][y] = true }

        return grid
    }

    private fun applyFold(grid: Array<Array<Boolean>>, fold: Fold) = when (fold.direction) {
        "x" -> applyHorizontalFold(grid, fold.coordinate)
        "y" -> applyVerticalFold(grid, fold.coordinate)
        else -> throw IllegalArgumentException(fold.direction)
    }

    private fun applyHorizontalFold(grid: Array<Array<Boolean>>, x: Int): Array<Array<Boolean>> {
        if (x != (grid.size - 1) / 2) throw IllegalArgumentException("$x ${grid.size}")

        val newGrid = Array(x) { Array(grid[0].size) { false } }

        for (i in 0 until x) {
            for (j in grid[0].indices) {
                newGrid[i][j] = grid[i][j]
            }
        }

        for (i in x+1 until grid.size) {
            for (j in grid[0].indices) {
                if (grid[i][j]) {
                    newGrid[grid.size - 1 - i][j] = true
                }
            }
        }

        return newGrid
    }

    private fun applyVerticalFold(grid: Array<Array<Boolean>>, y: Int): Array<Array<Boolean>> {
        if (y != (grid[0].size - 1) / 2) throw IllegalArgumentException("$y ${grid[0].size}")

        val newGrid = Array(grid.size) { Array(y) { false } }

        for (i in grid.indices) {
            for (j in 0 until y) {
                newGrid[i][j] = grid[i][j]
            }
        }

        for (i in grid.indices) {
            for (j in y+1 until grid[0].size) {
                if (grid[i][j]) {
                    newGrid[i][grid[0].size - 1 - j] = true
                }
            }
        }

        return newGrid
    }

    private fun printGrid(grid: Array<Array<Boolean>>) {
        for (j in grid[0].indices) {
            for (i in grid.indices) {
                print(if (grid[i][j]) "#" else " ")
            }
            println()
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val lines = Files.readAllLines(Path.of("input13.txt"), Charsets.UTF_8)
        val solution1 = solvePart1(lines)
        println(solution1)
        solvePart2(lines)
    }
}