package org.aoc2021

import java.nio.file.Files
import java.nio.file.Path

object Day16 {
    interface Packet {
        val version: Int
        val typeId: Int

        fun sumSubPacketVersions(): Int

        fun evaluate(): Long
    }

    data class LiteralValuePacket(
        override val version: Int,
        override val typeId: Int,
        val value: Long,
    ) : Packet {
        override fun sumSubPacketVersions() = 0

        override fun evaluate() = value
    }

    data class OperatorPacket(
        override val version: Int,
        override val typeId: Int,
        val subPackets: List<Packet>,
    ) : Packet {
        override fun sumSubPacketVersions() = subPackets.sumOf { p ->
            p.version + p.sumSubPacketVersions()
        }

        override fun evaluate() = when (typeId) {
            0 -> subPackets.sumOf(Packet::evaluate)
            1 -> subPackets.fold(1L) { p, packet -> p * packet.evaluate() }
            2 -> subPackets.minOf(Packet::evaluate)
            3 -> subPackets.maxOf(Packet::evaluate)
            5 -> if (subPackets[0].evaluate() > subPackets[1].evaluate()) 1L else 0L
            6 -> if (subPackets[0].evaluate() < subPackets[1].evaluate()) 1L else 0L
            7 -> if (subPackets[0].evaluate() == subPackets[1].evaluate()) 1L else 0L
            else -> throw IllegalArgumentException("$typeId")
        }
    }

    private fun solvePart1(lines: List<String>): Int {
        val binary = hexToBinary(lines[0])

        val outerPacket = parsePacket(binary).first

        return outerPacket.version + outerPacket.sumSubPacketVersions()
    }

    private fun solvePart2(lines: List<String>): Long {
        val binary = hexToBinary(lines[0])

        val outerPacket = parsePacket(binary).first

        return outerPacket.evaluate()
    }

    private fun hexToBinary(line: String): String {
        return line.map { c ->
            val binary = c.toString().toInt(16).toString(2)
            if (binary.length == 4) {
                binary
            } else {
                "0".repeat(4 - (binary.length % 4)) + binary
            }
        }.joinToString(separator = "")
    }

    private fun parsePacket(binary: String, i: Int = 0): Pair<Packet, Int> {
        val version = binary.substring(i..i+2).toInt(2)
        val typeId = binary.substring(i+3..i+5).toInt(2)

        if (typeId == 4) {
            return parseLiteralValuePacket(binary, i, version, typeId)
        }
        return parseOperatorPacket(binary, i, version, typeId)
    }

    private fun parseLiteralValuePacket(binary: String, i: Int, version: Int, typeId: Int): Pair<LiteralValuePacket, Int> {
        var currentIndex = i + 6
        var currentValue = 0L
        while (true) {
            currentValue = 16 * currentValue + binary.substring(currentIndex+1..currentIndex+4).toInt(2)
            if (binary[currentIndex] == '0') {
                break
            }
            currentIndex += 5
        }

        val packet = LiteralValuePacket(version, typeId, currentValue)

        return packet to (currentIndex + 5)
    }

    private fun parseOperatorPacket(binary: String, i: Int, version: Int, typeId: Int): Pair<OperatorPacket, Int> {
        if (binary[i + 6] == '0') {
            val totalSubPacketLength = binary.substring(i+7..i+21).toInt(2)

            val subPackets = mutableListOf<Packet>()
            var currentIndex = i + 22
            while (currentIndex - (i + 22) != totalSubPacketLength) {
                val (packet, nextPacketIndex) = parsePacket(binary, currentIndex)
                subPackets.add(packet)
                currentIndex = nextPacketIndex
            }
            return OperatorPacket(version, typeId, subPackets.toList()) to currentIndex
        } else {
            val numSubPackets = binary.substring(i+7..i+17).toInt(2)

            val subPackets = mutableListOf<Packet>()
            var currentIndex = i + 18
            (1..numSubPackets).forEach { _ ->
                val (packet, nextPacketIndex) = parsePacket(binary, currentIndex)
                subPackets.add(packet)
                currentIndex = nextPacketIndex
            }
            return OperatorPacket(version, typeId, subPackets.toList()) to currentIndex
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val lines = Files.readAllLines(Path.of("input16.txt"), Charsets.UTF_8)
        val solution1 = solvePart1(lines)
        println(solution1)
        val solution2 = solvePart2(lines)
        println(solution2)
    }
}