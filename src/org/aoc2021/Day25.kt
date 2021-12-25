package org.aoc2021

import java.nio.file.Files
import java.nio.file.Path

object Day25 {
    private fun solve(lines: List<String>): Int {
        var grid = parseInput(lines)

        var turns = 0
        while (true) {
            turns++
            val prevGrid = grid
            grid = simulateTurn(grid)
            if (prevGrid == grid) {
                return turns
            }
        }
    }

    private fun simulateTurn(grid: List<List<Char>>): List<List<Char>> {
        return moveDown(moveRight(grid))
    }

    private fun moveRight(grid: List<List<Char>>): List<List<Char>> {
        val newGrid = Array(grid.size) { Array(grid[0].size) { 'X' } }
        for (i in grid.indices) {
            for (j in grid[0].indices) {
                if (grid[i][j] == '>') {
                    val nj = (j + 1) % grid[0].size
                    if (grid[i][nj] == '.') {
                        newGrid[i][j] = '.'
                        newGrid[i][nj] = '>'
                    } else if (newGrid[i][j] == 'X') {
                        newGrid[i][j] = '>'
                    }
                } else if (newGrid[i][j] == 'X') {
                    newGrid[i][j] = grid[i][j]
                }
            }
        }
        return newGrid.map(Array<Char>::toList)
    }

    private fun moveDown(grid: List<List<Char>>): List<List<Char>> {
        val newGrid = Array(grid.size) { Array(grid[0].size) { 'X' } }
        for (i in grid.indices) {
            for (j in grid[0].indices) {
                if (grid[i][j] == 'v') {
                    val ni = (i + 1) % grid.size
                    if (grid[ni][j] == '.') {
                        newGrid[i][j] = '.'
                        newGrid[ni][j] = 'v'
                    } else if (newGrid[i][j] == 'X') {
                        newGrid[i][j] = 'v'
                    }
                } else if (newGrid[i][j] == 'X') {
                    newGrid[i][j] = grid[i][j]
                }
            }
        }
        return newGrid.map(Array<Char>::toList)
    }

    private fun parseInput(lines: List<String>): List<List<Char>> {
        return lines.map { line -> line.toCharArray().toList() }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val lines = Files.readAllLines(Path.of("input25.txt"), Charsets.UTF_8)
        val solution = solve(lines)
        println(solution)
    }
}