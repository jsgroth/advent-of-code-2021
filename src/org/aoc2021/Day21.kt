package org.aoc2021

import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.max

object Day21 {
    data class Key(val p1Pos: Int, val p1Score: Int, val p2Pos: Int, val p2Score: Int, val isP1Turn: Boolean)

    private fun solvePart1(lines: List<String>): Int {
        var (p1Pos, p2Pos) = parseStartingPositions(lines)

        var die = 1
        var rolls = 0
        var p1Score = 0
        var p2Score = 0
        while (true) {
            (1..3).forEach { _ ->
                p1Pos += die
                die = (die % 100) + 1
                rolls++
            }

            p1Pos = ((p1Pos - 1) % 10) + 1
            p1Score += p1Pos
            if (p1Score >= 1000) {
                return rolls * p2Score
            }

            (1..3).forEach { _ ->
                p2Pos += die
                die = (die % 100) + 1
                rolls++
            }

            p2Pos = ((p2Pos - 1) % 10) + 1
            p2Score += p2Pos
            if (p2Score >= 1000) {
                return rolls * p1Score
            }
        }
    }

    private fun solvePart2(lines: List<String>): Long {
        val (start1, start2) = parseStartingPositions(lines)

        val (p1Wins, p2Wins) = computeWinningUniverses(Key(start1, 0, start2, 0, true))
        return max(p1Wins, p2Wins)
    }

    private fun computeWinningUniverses(
        key: Key,
        memoizedResults: MutableMap<Key, Pair<Long, Long>> = mutableMapOf(),
    ): Pair<Long, Long> {
        memoizedResults[key]?.let { return it }

        if (key.p1Score >= 21) {
            return 1L to 0L
        }
        if (key.p2Score >= 21) {
            return 0L to 1L
        }

        var p1WinSum = 0L
        var p2WinSum = 0L

        for (i in 1..3) {
            for (j in 1..3) {
                for (k in 1..3) {
                    val (newP1Pos, newP1Score) = if (key.isP1Turn) {
                        computeNewPosScore(key.p1Pos, key.p1Score, i, j, k)
                    } else {
                        key.p1Pos to key.p1Score
                    }
                    val (newP2Pos, newP2Score) = if (!key.isP1Turn) {
                        computeNewPosScore(key.p2Pos, key.p2Score, i, j, k)
                    } else {
                        key.p2Pos to key.p2Score
                    }
                    val newKey = Key(newP1Pos, newP1Score, newP2Pos, newP2Score, !key.isP1Turn)
                    val (p1Wins, p2Wins) = computeWinningUniverses(newKey, memoizedResults)
                    p1WinSum += p1Wins
                    p2WinSum += p2Wins
                }
            }
        }

        memoizedResults[key] = p1WinSum to p2WinSum
        return p1WinSum to p2WinSum
    }

    private fun computeNewPosScore(pos: Int, score: Int, i: Int, j: Int, k: Int): Pair<Int, Int> {
        val newPos = ((pos + i + j + k - 1) % 10) + 1
        return newPos to (score + newPos)
    }

    private fun parseStartingPositions(lines: List<String>): Pair<Int, Int> {
        return lines[0].split(" ").last().toInt() to lines[1].split(" ").last().toInt()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val lines = Files.readAllLines(Path.of("input21.txt"), Charsets.UTF_8)
        val solution1 = solvePart1(lines)
        println(solution1)
        val solution2 = solvePart2(lines)
        println(solution2)
    }
}