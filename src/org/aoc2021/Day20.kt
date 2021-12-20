package org.aoc2021

import java.nio.file.Files
import java.nio.file.Path

object Day20 {
    private const val iterationsPart1 = 2
    private const val iterationsPart2 = 50

    data class Image(
        val lit: Set<Pair<Int, Int>>,
        val outOfBoundsLit: Boolean,
    )

    private fun solve(lines: List<String>, iterations: Int): Int {
        val (algorithm, image) = parseInput(lines)

        val processed = (1..iterations).fold(image) { prevImage, _ ->
            processImage(algorithm, prevImage)
        }

        return processed.lit.size
    }

    private fun processImage(algorithm: String, image: Image): Image {
        val newImage = mutableSetOf<Pair<Int, Int>>()

        val minX = image.lit.minOf { it.first }
        val maxX = image.lit.maxOf { it.first }
        val minY = image.lit.minOf { it.second }
        val maxY = image.lit.maxOf { it.second }

        for (i in (minX - 1)..(maxX + 1)) {
            for (j in (minY - 1)..(maxY + 1)) {
                val algorithmIndex = computeAlgorithmIndex(image, i, j, minX, maxX, minY, maxY)
                if (algorithm[algorithmIndex] == '#') {
                    newImage.add(i to j)
                }
            }
        }

        val newOutOfBoundsLit = if (image.outOfBoundsLit) (algorithm.last() == '#') else (algorithm.first() == '#')

        return Image(newImage.toSet(), newOutOfBoundsLit)
    }

    private fun computeAlgorithmIndex(image: Image, i: Int, j: Int, minX: Int, maxX: Int, minY: Int, maxY: Int): Int {
        var algorithmIndex = 0
        for (dx in -1..1) {
            for (dy in -1..1) {
                algorithmIndex *= 2
                if (image.lit.contains(i + dx to j + dy)) {
                    algorithmIndex++
                } else if (i + dx < minX || i + dx > maxX || j + dy < minY || j + dy > maxY) {
                    if (image.outOfBoundsLit) {
                        algorithmIndex++
                    }
                }
            }
        }
        return algorithmIndex
    }

    private fun parseInput(lines: List<String>): Pair<String, Image> {
        val algorithm = lines[0]

        val litPixels = lines.drop(2).flatMapIndexed { i, line ->
            line.mapIndexed { j, c ->
                if (c == '#') (i to j) else null
            }.filterNotNull()
        }.toSet()

        return algorithm to Image(litPixels, false)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val lines = Files.readAllLines(Path.of("input20.txt"), Charsets.UTF_8)
        val solution1 = solve(lines, iterationsPart1)
        println(solution1)
        val solution2 = solve(lines, iterationsPart2)
        println(solution2)
    }
}