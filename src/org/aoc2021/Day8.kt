package org.aoc2021

import java.nio.file.Files
import java.nio.file.Path

object Day8 {
    private fun solvePart1(lines: List<String>): Int {
        return lines.sumOf { line ->
            val outputDigits = line.split(" | ")[1].split(" ")
            outputDigits.count { setOf(2, 3, 4, 7).contains(it.length) }
        }
    }

    private fun solvePart2(lines: List<String>): Int {
        return lines.sumOf { line ->
            val (referenceDigits, outputDigits) = line.split(" | ").let {
                it[0].split(" ") to it[1].split(" ")
            }
            solveLine(referenceDigits, outputDigits)
        }
    }

    private fun solveLine(referenceDigits: List<String>, outputDigits: List<String>): Int {
        val digitToChars = Array(10) { setOf<Char>() }

        val referenceSets = referenceDigits.map(String::toSet)

        digitToChars[1] = referenceSets.single { it.size == 2 }
        digitToChars[4] = referenceSets.single { it.size == 4 }
        digitToChars[7] = referenceSets.single { it.size == 3 }
        digitToChars[8] = referenceSets.single { it.size == 7 }

        digitToChars[3] = referenceSets.single { referenceSet ->
            referenceSet.size == 5 && referenceSet.containsAll(digitToChars[1])
        }

        digitToChars[2] = referenceSets.single { referenceSet ->
            referenceSet.size == 5 && referenceSet.intersect(digitToChars[4]).size == 2
        }
        digitToChars[5] = referenceSets.single { referenceSet ->
            referenceSet.size == 5 && referenceSet.intersect(digitToChars[4]).size == 3 &&
                    referenceSet != digitToChars[3]
        }

        digitToChars[6] = referenceSets.single { referenceSet ->
            referenceSet.size == 6 && referenceSet.intersect(digitToChars[1]).size == 1
        }

        digitToChars[9] = referenceSets.single { referenceSet ->
            referenceSet.size == 6 && referenceSet.containsAll(digitToChars[4])
        }

        digitToChars[0] = referenceSets.single { referenceSet ->
            referenceSet.size == 6 && referenceSet != digitToChars[6] && referenceSet != digitToChars[9]
        }

        val setToDigit = digitToChars.mapIndexed { index, chars -> chars to index}.toMap()

        return outputDigits.fold(0) { sum, outputDigit ->
            10 * sum + setToDigit[outputDigit.toSet()]!!
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val lines = Files.readAllLines(Path.of("input8.txt"), Charsets.UTF_8)
        val solution1 = solvePart1(lines)
        println(solution1)
        val solution2 = solvePart2(lines)
        println(solution2)
    }
}