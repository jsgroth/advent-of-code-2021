package org.aoc2021

import java.nio.file.Files
import java.nio.file.Path
import java.util.PriorityQueue

object Day15 {
    data class RiskPath(val currentCost: Int, val row: Int, val col: Int)

    private fun solve(lines: List<String>, shouldExpandGrid: Boolean = false): Int {
        val rawGrid = parseLines(lines)
        val grid = if (shouldExpandGrid) expandGrid(rawGrid) else rawGrid

        val pq = PriorityQueue<RiskPath> { a, b ->
            a.currentCost.compareTo(b.currentCost)
        }

        pq.add(RiskPath(0, 0, 0))

        val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)

        val minCosts = mutableMapOf<Pair<Int, Int>, Int>()
        minCosts[0 to 0] = 0

        while (true) {
            val top = pq.remove()!!

            if (top.row == grid.size - 1 && top.col == grid[0].size - 1) {
                return top.currentCost
            }

            directions.forEach { (dx, dy) ->
                val x = top.row + dx
                val y = top.col + dy
                if (x >= 0 && x < grid.size && y >= 0 && y < grid[0].size) {
                    val newCost = top.currentCost + grid[x][y]
                    if (newCost < (minCosts[x to y] ?: Integer.MAX_VALUE)) {
                        pq.add(RiskPath(newCost, x, y))
                        minCosts[x to y] = newCost
                    }
                }
            }
        }
    }

    private fun parseLines(lines: List<String>): List<List<Int>> {
        return lines.map { line ->
            line.map(Char::digitToInt)
        }
    }

    private fun expandGrid(grid: List<List<Int>>): List<List<Int>> {
        val newGrid = Array(grid.size * 5) { Array(grid[0].size * 5) { 0 } }

        for (i in grid.indices) {
            for (j in grid[0].indices) {
                for (k in 0 until 5) {
                    for (x in 0 until 5) {
                        val ni = i + grid.size * k
                        val nj = j + grid[0].size * x
                        newGrid[ni][nj] = grid[i][j] + k + x
                        while (newGrid[ni][nj] > 9) {
                            newGrid[ni][nj] -= 9
                        }
                    }
                }
            }
        }

        return newGrid.map(Array<Int>::toList)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val lines = Files.readAllLines(Path.of("input15.txt"), Charsets.UTF_8)
        val solution1 = solve(lines, shouldExpandGrid = false)
        println(solution1)
        val solution2 = solve(lines, shouldExpandGrid = true)
        println(solution2)
    }
}