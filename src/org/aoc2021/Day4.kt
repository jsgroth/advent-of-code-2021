package org.aoc2021

import java.nio.file.Files
import java.nio.file.Path

class Board(boardLines: List<String>) {
    companion object {
        const val boardSize = 5
    }

    private val board: List<List<Int>>
    private val chosenNumbers: Array<Array<Boolean>> = Array(boardSize) { Array(boardSize) { false } }
    private val numberToPosition: Map<Int, Pair<Int, Int>>

    init {
        board = boardLines.drop(1).map { line ->
            line.split(Regex(" +")).filter(String::isNotBlank).map(String::toInt)
        }
        numberToPosition = board.flatMapIndexed { i, row ->
            row.mapIndexed { j, number -> number to (i to j) }
        }.toMap()
    }

    fun processNumber(number: Int) {
        numberToPosition[number]?.let { (i, j) ->
            chosenNumbers[i][j] = true
        }
    }

    private fun checkRows(): Boolean {
        return chosenNumbers.any { row ->
            row.all { it }
        }
    }

    private fun checkColumns(): Boolean {
        return (0 until boardSize).any { j ->
            (0 until boardSize).all { i ->
                chosenNumbers[i][j]
            }
        }
    }

    fun checkWin() = checkRows() || checkColumns()

    fun sumUnmarkedNumbers(): Int {
        return board.mapIndexed { i, row ->
            row.filterIndexed { j, _ -> !chosenNumbers[i][j] }.sum()
        }.sum()
    }
}

object Day4 {
    private fun solvePart1(filename: String): Int {
        val lines = Files.readAllLines(Path.of(filename), Charsets.UTF_8)

        val chosenNumbers = lines[0].split(",").map(String::toInt)
        val boards = parseBoards(lines)

        chosenNumbers.forEach { chosenNumber ->
            boards.forEach { board ->
                board.processNumber(chosenNumber)
                if (board.checkWin()) {
                    return chosenNumber * board.sumUnmarkedNumbers()
                }
            }
        }

        throw IllegalArgumentException("no winning boards found")
    }

    private fun solvePart2(filename: String): Int {
        val lines = Files.readAllLines(Path.of(filename), Charsets.UTF_8)

        val chosenNumbers = lines[0].split(",").map(String::toInt)
        val boards = parseBoards(lines)

        return findSolution(chosenNumbers, boards)
    }

    private fun parseBoards(lines: List<String>): List<Board> {
        return lines.drop(1).chunked(Board.boardSize + 1, ::Board)
    }

    private tailrec fun findSolution(chosenNumbers: List<Int>, boards: List<Board>): Int {
        val chosenNumber = chosenNumbers[0]

        boards.forEach { board ->
            board.processNumber(chosenNumber)
        }

        if (boards.size == 1 && boards[0].checkWin()) {
            return chosenNumber * boards[0].sumUnmarkedNumbers()
        }

        val remainingBoards = boards.filterNot(Board::checkWin)
        return findSolution(chosenNumbers.drop(1), remainingBoards)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val filename = "input4.txt"
        val solution1 = solvePart1(filename)
        println(solution1)
        val solution2 = solvePart2(filename)
        println(solution2)
    }
}