package org.aoc2021

import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.abs
import kotlin.system.measureNanoTime

typealias Vector = List<Int>
typealias Matrix = List<List<Int>>

private fun Vector.vectorAdd(vector: Vector): Vector {
    return this.mapIndexed { i, value ->
        value + vector[i]
    }
}

private fun Vector.vectorSubtract(vector: Vector): Vector {
    return this.mapIndexed { i, value ->
        value - vector[i]
    }
}

private fun Vector.matrixMultiply(matrix: Matrix): Vector {
    return this.indices.map { i ->
        matrix.indices.sumOf { j ->
            this[j] * matrix[j][i]
        }
    }
}

private fun Matrix.multiply(matrix: Matrix): Matrix {
    return this.map { vector -> vector.matrixMultiply(matrix) }
}

object Day19 {
    data class ScannerLocation(val coordinates: Vector, val rotation: Matrix)

    private val identityMatrix = listOf(
        listOf(1, 0, 0),
        listOf(0, 1, 0),
        listOf(0, 0, 1),
    )

    private val xRotationMatrix = listOf(
        listOf(1, 0, 0),
        listOf(0, 0, -1),
        listOf(0, 1, 0),
    )

    private val yRotationMatrix = listOf(
        listOf(0, 0, -1),
        listOf(0, 1, 0),
        listOf(1, 0, 0),
    )

    private val zRotationMatrix = listOf(
        listOf(0, 1, 0),
        listOf(-1, 0, 0),
        listOf(0, 0, 1),
    )

    private val all3DRotationMatrices = generateRotationMatrices()

    private fun solve(lines: List<String>): Pair<Int, Int> {
        val scanners = parseLines(lines)

        val scannerLocations = findScanners(scanners)

        val solution1 = countBeacons(scanners, scannerLocations)

        val solution2 = scannerLocations.values.maxOf { a ->
            scannerLocations.values.filter { it !== a }.maxOf { b ->
                abs(a.coordinates[0] - b.coordinates[0]) + abs(a.coordinates[1] - b.coordinates[1]) +
                        abs(a.coordinates[2] - b.coordinates[2])
            }
        }

        return solution1 to solution2
    }

    private fun parseLines(lines: List<String>): List<List<Vector>> {
        if (lines.isEmpty()) {
            return listOf()
        }

        val nextScanner = lines.drop(1)
            .takeWhile(String::isNotBlank)
            .map { line -> line.split(",").map(String::toInt).let { (a, b, c) -> listOf(a, b, c) } }

        return listOf(nextScanner).plus(parseLines(lines.dropWhile(String::isNotBlank).drop(1)))
    }

    private fun findScanners(scanners: List<List<Vector>>): Map<Int, ScannerLocation> {
        val scannerLocations = mutableMapOf(
            0 to ScannerLocation(listOf(0, 0, 0), identityMatrix),
        )

        while (scannerLocations.size < scanners.size) {
            println("located ${scannerLocations.size} / ${scanners.size} scanners")

            val (foundScannerIds, unknownScannerIds) =
                scanners.indices.partition { scannerLocations.containsKey(it) }

            foundScannerIds.forEach { foundScannerId ->
                val foundScannerLocation = scannerLocations[foundScannerId]!!
                unknownScannerIds.forEach { unknownScannerId ->
                    val foundBeaconLocations = scanners[foundScannerId].map { beaconLocation ->
                        beaconLocation.matrixMultiply(foundScannerLocation.rotation)
                    }
                    val unknownBeaconLocations = scanners[unknownScannerId]

                    checkForOverlap(
                        foundScannerLocation, foundBeaconLocations, unknownBeaconLocations
                    )?.let { newScannerLocation ->
                        scannerLocations[unknownScannerId] = newScannerLocation
                    }
                }
            }
        }

        return scannerLocations.toMap()
    }

    private fun checkForOverlap(
        scannerLocation: ScannerLocation,
        beaconLocations: List<Vector>,
        otherBeaconLocations: List<Vector>,
    ): ScannerLocation? {
        beaconLocations.forEach { beaconLocation ->
            val beaconDistances = computeDistances(beaconLocation, beaconLocations)

            all3DRotationMatrices.forEach { rotationMatrix ->
                val rotatedOtherLocations = otherBeaconLocations.map { it.matrixMultiply(rotationMatrix) }

                rotatedOtherLocations.forEach { otherBeaconLocation ->
                    val otherBeaconDistances = computeDistances(otherBeaconLocation, rotatedOtherLocations)

                    if (beaconDistances.toSet().intersect(otherBeaconDistances.toSet()).size >= 11) {
                        val otherScannerCoordinates =
                            scannerLocation.coordinates.vectorAdd(beaconLocation).vectorSubtract(otherBeaconLocation)
                        return ScannerLocation(otherScannerCoordinates, rotationMatrix)
                    }
                }
            }
        }
        return null
    }

    private fun computeDistances(beacon: Vector, beacons: List<Vector>): List<Vector> {
        return beacons.filter { it !== beacon }.map { it.vectorSubtract(beacon) }
    }

    private fun countBeacons(scanners: List<List<Vector>>, scannerLocations: Map<Int, ScannerLocation>): Int {
        val allBeaconLocations = mutableSetOf<Vector>()
        scanners.forEachIndexed { i, beaconLocations ->
            val scannerLocation = scannerLocations[i]!!

            allBeaconLocations.addAll(beaconLocations.map { beaconLocation ->
                beaconLocation.matrixMultiply(scannerLocation.rotation).vectorAdd(scannerLocation.coordinates)
            })
        }
        return allBeaconLocations.size
    }

    private fun generateRotationMatrices(): List<Matrix> {
        val result = mutableListOf<Matrix>()

        // +z
        var currentMatrix = identityMatrix
        result.addAll(generateRotationMatricesForAxis(currentMatrix, zRotationMatrix))

        // -z
        currentMatrix = currentMatrix.multiply(yRotationMatrix).multiply(yRotationMatrix)
        result.addAll(generateRotationMatricesForAxis(currentMatrix, zRotationMatrix))

        // +y
        currentMatrix = currentMatrix.multiply(xRotationMatrix)
        result.addAll(generateRotationMatricesForAxis(currentMatrix, yRotationMatrix))

        // -y
        currentMatrix = currentMatrix.multiply(xRotationMatrix).multiply(xRotationMatrix)
        result.addAll(generateRotationMatricesForAxis(currentMatrix, yRotationMatrix))

        // +x
        currentMatrix = currentMatrix.multiply(zRotationMatrix)
        result.addAll(generateRotationMatricesForAxis(currentMatrix, xRotationMatrix))

        // -x
        currentMatrix = currentMatrix.multiply(zRotationMatrix).multiply(zRotationMatrix)
        result.addAll(generateRotationMatricesForAxis(currentMatrix, xRotationMatrix))

        return result.toList()
    }

    private fun generateRotationMatricesForAxis(matrix: Matrix, rotation: Matrix): List<Matrix> {
        val result = mutableListOf<Matrix>()
        var current = matrix
        (1..4).forEach { _ ->
            result.add(current)
            current = current.multiply(rotation)
        }
        return result.toList()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val lines = Files.readAllLines(Path.of("input19.txt"), Charsets.UTF_8)
        val (solution1, solution2) = solve(lines)
        println(solution1)
        println(solution2)
    }
}