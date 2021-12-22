package org.aoc2021

import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.max
import kotlin.math.min

object Day22 {
    data class Cube(val x: IntRange, val y: IntRange, val z: IntRange) {
        fun volume(): Long {
            return x.count().toLong() * y.count() * z.count()
        }

        fun intersect(other: Cube): Cube {
            if (x.first > other.x.last ||
                    x.last < other.x.first ||
                    y.first > other.y.last ||
                    y.last < other.y.first ||
                    z.first > other.z.last ||
                    z.last < other.z.first) {
                return Cube(IntRange.EMPTY, IntRange.EMPTY, IntRange.EMPTY)
            }

            val minX = max(x.first, other.x.first)
            val maxX = min(x.last, other.x.last)
            val minY = max(y.first, other.y.first)
            val maxY = min(y.last, other.y.last)
            val minZ = max(z.first, other.z.first)
            val maxZ = min(z.last, other.z.last)

            return Cube(minX..maxX, minY..maxY, minZ..maxZ)
        }
    }

    data class Step(val on: Boolean, val cube: Cube)

    private fun solve(lines: List<String>, shouldBound: Boolean): Long {
        val steps = parseSteps(lines).let { if (shouldBound) boundCubes(it) else it }
        val cubes = steps.map(Step::cube)

        var totalVolume = 0L
        steps.forEachIndexed { i, step ->
            val flippedVolume = computeFlippedVolume(steps.subList(0, i), step)
            if (step.on) {
                totalVolume += flippedVolume
                totalVolume += computeNonIntersectingVolume(cubes.subList(0, i), step.cube)
            } else {
                totalVolume -= flippedVolume
            }
        }

        return totalVolume
    }

    private fun parseSteps(lines: List<String>): List<Step> {
        return lines.map { line ->
            val (onString, rest) = line.split(" ")
            val (xRange, yRange, zRange) = rest.split(",").map { rangeString ->
                rangeString.substring(2).split("..").let { it[0].toInt()..it[1].toInt() }
            }
            Step(onString == "on", Cube(xRange, yRange, zRange))
        }
    }

    private fun boundCubes(steps: List<Step>): List<Step> {
        return steps.map { step ->
            val (x, y, z) = step.cube
            Step(step.on, Cube(
                max(x.first, -50)..min(x.last, 50),
                max(y.first, -50)..min(y.last, 50),
                max(z.first, -50)..min(z.last, 50),
            ))
        }
    }

    private fun computeFlippedVolume(previousSteps: List<Step>, step: Step): Long {
        val overlappingSteps = previousSteps.filter { it.cube.intersect(step.cube).volume() > 0 }

        var totalFlippedVolume = 0L
        overlappingSteps.indices.filter { j -> overlappingSteps[j].on != step.on }.forEach { j ->
            val otherStep = overlappingSteps[j]
            val laterCubes = overlappingSteps.subList(j + 1, overlappingSteps.size).map(Step::cube)

            val offOnIntersect = step.cube.intersect(otherStep.cube)

            totalFlippedVolume += computeNonIntersectingVolume(laterCubes, offOnIntersect)
        }

        return totalFlippedVolume
    }

    private fun computeNonIntersectingVolume(previousCubes: List<Cube>, cube: Cube): Long {
        val overlappingCubes = previousCubes.filter { it.intersect(cube).volume() > 0 }

        var newVolume = cube.volume()
        var sign = -1
        for (cubeCount in 1..overlappingCubes.size) {
            val cubeCombinations = combinations(overlappingCubes, cubeCount)

            cubeCombinations.forEach { cubeCombination ->
                newVolume += sign * cubeCombination.fold(cube) { a, b -> a.intersect(b) }.volume()
            }

            sign *= -1
        }
        return newVolume
    }

    private fun <T> combinations(list: List<T>, count: Int): List<List<T>> {
        if (count == 1) {
            return list.map { listOf(it) }
        }

        return list.flatMapIndexed { i, elem ->
            val subCombinations = combinations(list.subList(i + 1, list.size), count - 1)
            subCombinations.map { listOf(elem).plus(it) }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val lines = Files.readAllLines(Path.of("input22.txt"), Charsets.UTF_8)
        val solution1 = solve(lines, true)
        println(solution1)
        val solution2 = solve(lines, false)
        println(solution2)
    }
}