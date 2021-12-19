package org.aoc2021

import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.max

object Day17 {
    data class TargetArea(val xRange: IntRange, val yRange: IntRange)

    private fun solvePart1(lines: List<String>): Int {
        val targetArea = parseTargetArea(lines[0])

        var maxHeight = Integer.MIN_VALUE
        for (initialX in 0..200) {
            for (initialY in 0..500) {
                testVelocity(initialX, initialY, targetArea)?.let { maxHeight = max(maxHeight, it) }
            }
        }
        return maxHeight
    }

    private fun solvePart2(lines: List<String>): Int {
        val targetArea = parseTargetArea(lines[0])

        var count = 0
        for (initialX in 0..200) {
            for (initialY in -500..500) {
                testVelocity(initialX, initialY, targetArea)?.let { count++ }
            }
        }
        return count
    }

    private fun parseTargetArea(line: String): TargetArea {
        return line.substringAfter("target area: ").split(", ").let { (x, y) ->
            TargetArea(parseRange(x), parseRange(y))
        }
    }

    private fun parseRange(rangeString: String): IntRange {
        return rangeString.substring(2).split("..").map(String::toInt).let { it[0]..it[1] }
    }


    private fun testVelocity(initialX: Int, initialY: Int, targetArea: TargetArea): Int? {
        var x = 0
        var y = 0
        var xVelocity = initialX
        var yVelocity = initialY
        var maxY = 0
        while (x <= targetArea.xRange.last && y >= targetArea.yRange.first) {
            x += xVelocity
            y += yVelocity
            maxY = max(maxY, y)

            if (targetArea.xRange.contains(x) && targetArea.yRange.contains(y)) {
                return maxY
            }

            if (xVelocity > 0) {
                xVelocity--
            }
            if (xVelocity < 0) {
                xVelocity++
            }
            yVelocity--
        }
        return null
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val lines = Files.readAllLines(Path.of("input17.txt"), Charsets.UTF_8)
        val solution1 = solvePart1(lines)
        println(solution1)
        val solution2 = solvePart2(lines)
        println(solution2)
    }
}