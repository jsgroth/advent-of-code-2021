package org.aoc2021

import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object Day23 {
    private const val hallwayLength = 11

    private val amphipodChars = setOf('A', 'B', 'C', 'D')
    private val roomIndices = setOf(2, 4, 6, 8)

    private val secondRow = listOf(Amphipod.DESERT, Amphipod.COPPER, Amphipod.BRONZE, Amphipod.AMBER)
    private val thirdRow = listOf(Amphipod.DESERT, Amphipod.BRONZE, Amphipod.AMBER, Amphipod.COPPER)

    enum class Amphipod(val energyPerStep: Int) {
        AMBER(1),
        BRONZE(10),
        COPPER(100),
        DESERT(1000),
    }

    private val winState = Amphipod.values().toList()

    sealed interface HallwayPosition

    data class Space(val occupant: Amphipod?) : HallwayPosition

    data class Room(val occupants: List<Amphipod?>) : HallwayPosition {
        fun withNewOccupant(occupant: Amphipod): Room {
            if (occupants.all { it == null }) {
                return Room(occupants.drop(1).plus(occupant))
            }
            val firstNullIndex = firstOpenIndex()
            return Room(
                occupants.take(firstNullIndex).plus(occupant).plus(occupants.drop(firstNullIndex + 1))
            )
        }

        fun withoutTopOccupant(): Room {
            val firstNonNullIndex = firstOccupiedIndex()
            return Room(
                occupants.take(firstNonNullIndex).plus(null).plus(occupants.drop(firstNonNullIndex + 1))
            )
        }

        fun firstOpenIndex(): Int {
            return occupants.indexOfLast { it == null }
        }

        fun firstOccupiedIndex(): Int {
            return occupants.indexOfFirst { it != null }
        }
    }

    data class State(val distance: Int, val hallway: List<HallwayPosition>)

    private fun solve(lines: List<String>, expandMiddle: Boolean): Int {
        val hallway = parseInput(lines, expandMiddle)

        val pq = PriorityQueue<State> { a, b ->
            a.distance.compareTo(b.distance)
        }

        pq.add(State(0, hallway))

        val stateToMinDistance = mutableMapOf<List<HallwayPosition>, Int>()

        while (!pq.isEmpty()) {
            val state = pq.remove()

            if (state.distance >= (stateToMinDistance[state.hallway] ?: Integer.MAX_VALUE)) {
                continue
            }

            stateToMinDistance[state.hallway] = state.distance

            if (isWinningState(state.hallway)) {
                return state.distance
            }

            generatePossibleMoves(state.hallway).forEach { (moveDistance, moveState) ->
                val newDistance = state.distance + moveDistance
                if (newDistance < (stateToMinDistance[moveState] ?: Integer.MAX_VALUE)) {
                    pq.add(State(newDistance, moveState))
                }
            }
        }

        throw IllegalArgumentException("no winning solution found")
    }

    private fun isWinningState(state: List<HallwayPosition>): Boolean {
        return state.filterIsInstance<Room>().zip(winState).all { (room, targetAmphipod) ->
            room.occupants.all { it == targetAmphipod }
        }
    }

    private fun generatePossibleMoves(hallway: List<HallwayPosition>): List<Pair<Int, List<HallwayPosition>>> {
        val moves = mutableListOf<Pair<Int, List<HallwayPosition>>>()

        hallway.forEachIndexed { i, position ->
            when (position) {
                is Space -> moves.addAll(generateSpaceMoves(hallway, i, position))
                is Room -> moves.addAll(generateRoomMoves(hallway, i, position))
            }
        }

        return moves.toList()
    }

    private fun generateSpaceMoves(
        hallway: List<HallwayPosition>,
        i: Int,
        space: Space,
    ): List<Pair<Int, List<HallwayPosition>>> {
        if (space.occupant == null) {
            return listOf()
        }

        val targetIndex = 2 * winState.indexOf(space.occupant) + 2
        val targetRoom = hallway[targetIndex] as Room
        if (canMoveToRoom(hallway, i, targetIndex)) {
            val distance = space.occupant.energyPerStep * (abs(i - targetIndex) + 1 + targetRoom.firstOpenIndex())
            val newState = hallway.map { position ->
                if (position === space) {
                    Space(null)
                } else if (position === targetRoom) {
                    targetRoom.withNewOccupant(space.occupant)
                } else {
                    position
                }
            }
            return listOf(distance to newState)
        }

        return listOf()
    }

    private fun generateRoomMoves(
        hallway: List<HallwayPosition>,
        i: Int,
        room: Room,
    ): List<Pair<Int, List<HallwayPosition>>> {
        val targetAmphipod = winState[(i - 2) / 2]
        if (room.occupants.all { it == null || it == targetAmphipod }) {
            return listOf()
        }

        val firstOccupiedIndex = room.firstOccupiedIndex()
        val first = room.occupants[firstOccupiedIndex]!!

        if (first != targetAmphipod) {
            val firstTargetIndex = 2 * winState.indexOf(first) + 2
            val firstTargetRoom = hallway[firstTargetIndex] as Room
            if (canMoveToRoom(hallway, i, firstTargetIndex)) {
                val steps = abs(i - firstTargetIndex) + 2 + firstOccupiedIndex + firstTargetRoom.firstOpenIndex()
                val distance = first.energyPerStep * steps
                val newState = hallway.map { position ->
                    if (position === room) {
                        room.withoutTopOccupant()
                    } else if (position === firstTargetRoom) {
                        firstTargetRoom.withNewOccupant(first)
                    } else {
                        position
                    }
                }
                return listOf(distance to newState)
            }
        }

        val moves = mutableListOf<Pair<Int, List<HallwayPosition>>>()
        for (j in hallway.indices) {
            val space = hallway[j]
            if (space !is Space) {
                continue
            }

            if (canMoveToSpace(hallway, i, j)) {
                val distance = first.energyPerStep * (abs(i - j) + 1 + room.firstOccupiedIndex())
                val newState = hallway.map { position ->
                    if (position === room) {
                        room.withoutTopOccupant()
                    } else if (position === space) {
                        Space(first)
                    } else {
                        position
                    }
                }
                moves.add(distance to newState)
            }
        }
        return moves.toList()
    }

    private fun canMoveToSpace(
        hallway: List<HallwayPosition>,
        i: Int,
        targetIndex: Int,
    ): Boolean {
        val space = hallway[targetIndex] as Space
        if (space.occupant != null) {
            return false
        }
        return canMoveToPosition(hallway, i, targetIndex)
    }

    private fun canMoveToRoom(
        hallway: List<HallwayPosition>,
        i: Int,
        targetIndex: Int,
    ): Boolean {
        val targetAmphipod = winState[(targetIndex - 2) / 2]
        val targetRoom = hallway[targetIndex] as Room
        if (!targetRoom.occupants.all { it == null || it == targetAmphipod }) {
            return false
        }

        return canMoveToPosition(hallway, i, targetIndex)
    }

    private fun canMoveToPosition(
        hallway: List<HallwayPosition>,
        i: Int,
        targetIndex: Int,
    ): Boolean {
        val start = min(i, targetIndex)
        val end = max(i, targetIndex)
        return ((start + 1) until end).all { j ->
            val position = hallway[j]
            position !is Space || position.occupant == null
        }
    }

    private fun parseInput(lines: List<String>, expandMiddle: Boolean): List<HallwayPosition> {
        val frontOccupants = lines[2].filter(amphipodChars::contains).toCharArray()
        val backOccupants = lines[3].filter(amphipodChars::contains).toCharArray()

        val roomOccupants = frontOccupants.zip(backOccupants)
        var occupantsIndex = 0
        val result = mutableListOf<HallwayPosition>()
        for (i in 0 until hallwayLength) {
            if (roomIndices.contains(i)) {
                val (frontOccupant, backOccupant) = roomOccupants[occupantsIndex]
                val room = if (expandMiddle) {
                    Room(listOf(
                        charToAmphipod(frontOccupant),
                        secondRow[(i - 2) / 2],
                        thirdRow[(i - 2) / 2],
                        charToAmphipod(backOccupant),
                    ))
                } else {
                    Room(listOf(charToAmphipod(frontOccupant), charToAmphipod(backOccupant)))
                }
                result.add(room)

                occupantsIndex++
            } else {
                result.add(Space(occupant = null))
            }
        }

        return result.toList()
    }

    private fun charToAmphipod(c: Char): Amphipod = when (c) {
        'A' -> Amphipod.AMBER
        'B' -> Amphipod.BRONZE
        'C' -> Amphipod.COPPER
        'D' -> Amphipod.DESERT
        else -> throw IllegalArgumentException("$c")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val lines = Files.readAllLines(Path.of("input23.txt"), Charsets.UTF_8)
        val solution1 = solve(lines, false)
        println(solution1)
        val solution2 = solve(lines, true)
        println(solution2)
    }
}