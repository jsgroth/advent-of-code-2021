package org.aoc2021

import java.nio.file.Files
import java.nio.file.Path

object Day11 {
    private const val iterations = 100

    private fun solvePart1(lines: List<String>): Int {
        val energies = parseLines(lines)

        return (1..iterations).fold(0) { totalFlashes, _ ->
            totalFlashes + simulateIteration(energies)
        }
    }

    private fun solvePart2(lines: List<String>): Int {
        val energies = parseLines(lines)

        val totalSize = energies.size * energies[0].size

        var iteration = 1
        while (true) {
            val flashes = simulateIteration(energies)

            if (flashes == totalSize) {
                return iteration
            }
            iteration++
        }
    }

    private fun parseLines(lines: List<String>): Array<Array<Int>> {
        val energies = Array(lines.size) { Array(lines[0].length) { 0 } }

        lines.forEachIndexed { i, line ->
            line.forEachIndexed { j, c ->
                energies[i][j] = c.digitToInt()
            }
        }

        return energies
    }

    private fun simulateIteration(energies: Array<Array<Int>>): Int {
        for (i in energies.indices) {
            for (j in energies[0].indices) {
                energies[i][j]++
            }
        }

        var flashes = 0
        while (true) {
            var flashed = false

            for (i in energies.indices) {
                for (j in energies[0].indices) {
                    if (energies[i][j] > 9) {
                        flash(energies, i, j)
                        flashed = true
                        flashes++
                    }
                }
            }

            if (!flashed) {
                break
            }
        }

        return flashes
    }

    private fun flash(energies: Array<Array<Int>>, i: Int, j: Int) {
        energies[i][j] = 0

        (-1..1).forEach { di ->
            (-1..1).forEach { dj ->
                if (i + di >= 0 &&
                    i + di < energies.size &&
                    j + dj >= 0 &&
                    j + dj < energies[0].size &&
                    energies[i + di][j + dj] > 0
                ) {
                    energies[i + di][j + dj]++
                }
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val lines = Files.readAllLines(Path.of("input11.txt"), Charsets.UTF_8)
        val solution1 = solvePart1(lines)
        println(solution1)
        val solution2 = solvePart2(lines)
        println(solution2)
    }
}