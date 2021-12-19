package org.aoc2021

import java.nio.file.Files
import java.nio.file.Path

object Day18 {
    data class TreeNode(val left: TreeNode? = null, val right: TreeNode? = null, val value: Int? = null)

    private fun solvePart1(lines: List<String>): Int {
        val snailfishNumbers = lines.map(Day18::parseSnailfishNumber)
        val snailfishSum = snailfishNumbers.reduce(Day18::addSnailfishNumbers)
        return magnitude(snailfishSum)
    }

    private fun solvePart2(lines: List<String>): Int {
        val snailfishNumbers = lines.map(Day18::parseSnailfishNumber)

        return snailfishNumbers.maxOf { snailfishNumber ->
            snailfishNumbers.filter { it !== snailfishNumber }.maxOf { otherNumber ->
                magnitude(addSnailfishNumbers(snailfishNumber, otherNumber))
            }
        }
    }

    private fun parseSnailfishNumber(line: String): TreeNode {
        return parseNode(line, 0, false).first
    }

    private fun parseNode(line: String, i: Int, parsingRight: Boolean): Pair<TreeNode, Int> {
        return if (line[i] == '[') {
            val (left, j) = parseNode(line, i + 1, false)
            val (right, k) = parseNode(line, j + 1, true)
            TreeNode(left = left, right = right) to k + 1
        } else {
            val endChar = if (parsingRight) ']' else ','
            val end = line.indexOf(endChar, startIndex = i)
            val value = line.substring(i, end).toInt()
            TreeNode(value = value) to end
        }
    }

    private fun addSnailfishNumbers(first: TreeNode, second: TreeNode): TreeNode {
        return reduceSnailfishNumber(TreeNode(left = first, right = second))
    }

    private tailrec fun reduceSnailfishNumber(number: TreeNode): TreeNode {
        findNodeToExplode(number)?.let { nodeToExplode ->
            return reduceSnailfishNumber(explode(number, nodeToExplode))
        }

        findNodeToSplit(number)?.let { nodeToSplit ->
            return reduceSnailfishNumber(split(number, nodeToSplit))
        }

        return number
    }

    private fun findNodeToExplode(number: TreeNode, depth: Int = 0): TreeNode? {
        if (depth == 4 && number.left != null && number.right != null) {
            return number
        }
        if (number.left == null || number.right == null) {
            return null
        }

        findNodeToExplode(number.left, depth + 1)?.let { return it }
        return findNodeToExplode(number.right, depth + 1)
    }

    private fun explode(number: TreeNode, nodeToExplode: TreeNode): TreeNode {
        val allNodes = traverse(number)

        val nodeToExplodeIndex = allNodes.indexOfFirst { it === nodeToExplode }
        val lastValueBefore = allNodes.subList(0, nodeToExplodeIndex - 1).findLast { it.value != null }
        val firstValueAfter = allNodes.subList(nodeToExplodeIndex + 2, allNodes.size).find { it.value != null }

        return doExplode(number, nodeToExplode, lastValueBefore, firstValueAfter)
    }

    private fun traverse(number: TreeNode): List<TreeNode> {
        return if (number.left == null || number.right == null) {
            listOf(number)
        } else {
            traverse(number.left).plus(number).plus(traverse(number.right))
        }
    }

    private fun doExplode(
        number: TreeNode,
        nodeToExplode: TreeNode,
        lastValueBefore: TreeNode?,
        firstValueAfter: TreeNode?,
    ): TreeNode {
        if (number === nodeToExplode) {
            return TreeNode(value = 0)
        }
        if (number === lastValueBefore) {
            return TreeNode(value = number.value!! + nodeToExplode.left!!.value!!)
        }
        if (number === firstValueAfter) {
            return TreeNode(value = number.value!! + nodeToExplode.right!!.value!!)
        }
        if (number.left == null || number.right == null) {
            return number
        }
        return TreeNode(
            left = doExplode(number.left, nodeToExplode, lastValueBefore, firstValueAfter),
            right = doExplode(number.right, nodeToExplode, lastValueBefore, firstValueAfter),
        )
    }

    private fun findNodeToSplit(number: TreeNode): TreeNode? {
        if (number.value != null && number.value >= 10) {
            return number
        }
        if (number.left == null || number.right == null) {
            return null
        }

        findNodeToSplit(number.left)?.let { return it }
        return findNodeToSplit(number.right)
    }

    private fun split(number: TreeNode, nodeToSplit: TreeNode): TreeNode {
        if (number === nodeToSplit) {
            return TreeNode(
                left = TreeNode(value = number.value!! / 2),
                right = TreeNode(value = number.value / 2 + (number.value % 2)),
            )
        }
        if (number.left == null || number.right == null) {
            return number
        }

        return TreeNode(
            left = split(number.left, nodeToSplit),
            right = split(number.right, nodeToSplit),
        )
    }

    private fun magnitude(number: TreeNode): Int {
        if (number.left == null || number.right == null) {
            return number.value!!
        }
        return 3 * magnitude(number.left) + 2 * magnitude(number.right)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val lines = Files.readAllLines(Path.of("input18.txt"), Charsets.UTF_8)
        val solution1 = solvePart1(lines)
        println(solution1)
        val solution2 = solvePart2(lines)
        println(solution2)
    }
}