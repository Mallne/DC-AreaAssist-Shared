package cloud.mallne.dicentra.areaassist.model.geokit

import cloud.mallne.dicentra.areaassist.extensions.CalculationExtensions.dot
import cloud.mallne.dicentra.areaassist.extensions.CalculationExtensions.magnitudeSquared
import cloud.mallne.dicentra.areaassist.extensions.CalculationExtensions.normalize
import cloud.mallne.dicentra.areaassist.extensions.CalculationExtensions.times
import cloud.mallne.dicentra.areaassist.extensions.CalculationExtensions.toDegrees
import cloud.mallne.dicentra.areaassist.extensions.CalculationExtensions.toRadians
import cloud.mallne.dicentra.areaassist.units.*
import cloud.mallne.dicentra.areaassist.units.Angle.Companion.degrees
import cloud.mallne.dicentra.areaassist.units.Angle.Companion.radians
import cloud.mallne.dicentra.areaassist.units.Length.Companion.meters
import kotlin.math.*


object GeokitMeasurement {
    /**
     * Takes two [GeokitPosition]s and finds the geographic bearing between them.
     */
    fun bearing(point1: GeokitPosition, point2: GeokitPosition): Measure<Angle> {
        val lon1: Double = point1.longitude.toRadians()
        val lon2: Double = point2.longitude.toRadians()
        val lat1: Double = point1.latitude.toRadians()
        val lat2: Double = point2.latitude.toRadians()
        val value1 = sin(lon2 - lon1) * cos(lat2)
        val value2 = cos(lat1) * sin(lat2) - (sin(lat1) * cos(lat2) * cos(lon2 - lon1))

        return atan2(value1, value2) * radians
    }

    /**
     * Calculates the distance between two points in degress, radians, miles, or kilometers. This
     * uses the Haversine formula to account for global curvature.
     */
    fun distance(
        point1: GeokitPosition, point2: GeokitPosition,
    ): Measure<Length> {
        val difLat: Double = (point2.latitude - point1.latitude).toRadians()
        val difLon: Double = (point2.longitude - point1.longitude).toRadians()
        val lat1: Double = point1.latitude.toRadians()
        val lat2: Double = point2.latitude.toRadians()

        val value = sin(difLat / 2).pow(2.0) + sin(difLon / 2).pow(2.0) * cos(lat1) * cos(lat2)

        return GeokitConversion.angleToLength(
            (2 * atan2(sqrt(value), sqrt(1 - value))) * radians
        )
    }

    /**
     * Takes a [List] of [GeokitPosition] and measures its length in the specified units.
     */
    fun length(coords: List<GeokitPosition>): Measure<Length> {
        var travelled = 0.0 * meters
        var prevCoords: GeokitPosition = coords.first()
        var curCoords: GeokitPosition
        for (i in 1..<coords.size) {
            curCoords = coords[i]
            travelled += distance(prevCoords, curCoords)
            prevCoords = curCoords
        }
        return travelled
    }

    /**
     * Takes a Point and calculates the location of a destination point given a distance in
     * degrees, radians, miles, or kilometers; and bearing in degrees. This uses the Haversine
     * formula to account for global curvature.
     */
    fun destination(
        point: GeokitPosition,
        distance: Measure<Length>,
        bearing: Measure<Angle>
    ): GeokitPosition {
        val longitude1: Double = point.longitude.toRadians()
        val latitude1: Double = point.latitude.toRadians()
        val bearingRad: Double = bearing `in` radians

        val radians: Double = GeokitConversion.lengthToAngle(distance) `in` radians

        val latitude2 = asin(
            sin(latitude1) * cos(radians)
                    + cos(latitude1) * sin(radians) * cos(bearingRad)
        )
        val longitude2 = longitude1 + atan2(
            (sin(bearingRad) * sin(radians) * cos(latitude1)),
            cos(radians) - sin(latitude1) * sin(latitude2)
        )

        return GeokitPosition(latitude2.toDegrees(), longitude2.toDegrees())
    }

    fun destination(
        origin: GeokitPosition,
        direction: DoubleArray
    ): GeokitPosition {
        return GeokitPosition(origin.latitude + direction[1], origin.longitude + direction[0])
    }

    /**
     * Takes a list of points and returns a point at a specified distance along the line.
     */
    fun along(coords: List<GeokitPosition>, distance: Measure<Length>): GeokitPosition {
        var travelled = 0.0 * meters
        for (i in coords.indices) {
            if (distance >= travelled && i == coords.size - 1) {
                break
            } else if (travelled >= distance) {
                val overshot = distance - travelled
                if (overshot.amount == 0.0) {
                    return coords[i]
                } else {
                    val direction = (bearing(coords[i], coords[i - 1]) - 180 * degrees)
                    return destination(coords[i], overshot, direction)
                }
            } else {
                travelled += distance(coords[i], coords[i + 1])
            }
        }

        return coords[coords.size - 1]
    }

    fun bbox(resultCoords: List<GeokitPosition>): GeokitBounds {
        var minLon = GeokitConstants.MAX_LONGITUDE
        var minLat = GeokitConstants.MAX_LATITUDE
        var maxLon = GeokitConstants.MIN_LONGITUDE
        var maxLat = GeokitConstants.MIN_LATITUDE

        for (point in resultCoords) {
            val lat = point.latitude
            val lon = point.longitude
            minLat = min(minLat, lat)
            minLon = min(minLon, lon)
            maxLat = max(maxLat, lat)
            maxLon = max(maxLon, lon)
        }
        return GeokitBounds(maxLat, maxLon, minLat, minLon)
    }

    fun center(bounds: GeokitBounds): GeokitPosition {
        val latCenter = (bounds.north + bounds.south) / 2.0
        val longCenter = (bounds.east + bounds.west) / 2.0
        return GeokitPosition(latCenter, longCenter)
    }

    fun vectorOf(from: GeokitPosition, to: GeokitPosition): GeokitVector {
        return GeokitVector(origin = from, direction = directionalVectorOf(from, to))
    }

    fun directionalVectorOf(from: GeokitPosition, to: GeokitPosition): DoubleArray {
        return doubleArrayOf(to.longitude - from.longitude, to.latitude - from.latitude)
    }

    /**
     * Calculates the closest point on a line segment (defined by a start point and a direction vector)
     * to an external point P, and the shortest vector from that closest point to P.
     *
     * @param pointP The external point.
     * @param segment The starting point of the line segment. And The direction vector defining the segment from A. Its magnitude is the segment's length.
     * @return the Resulting Intersection Vector
     */
    fun intersectionVector(
        pointP: GeokitPosition,
        segment: GeokitVector
    ): GeokitVector {

        val pointOrigin = segment.origin
        val vectorOriginDestination = segment.direction

        val squaredLengthVecOD = vectorOriginDestination.magnitudeSquared()

        // Handle the case where the segment is just a point (A == B)
        if (squaredLengthVecOD == 0.0) {
            // The segment is effectively a single point A.
            // The shortest vector is simply the vector from P to A.
            return pointP - pointOrigin
        }

        // Calculate the vector from the segment's start point (A) to the point P
        val vectorOriginP = pointP - pointOrigin

        // Calculate the scalar projection 't' of vectorAP onto vectorAB.
        // This 't' value indicates where the projection of P falls along the infinite line
        // passing through A and B.
        // t = (AP · AB) / |AB|^2
        val t = (vectorOriginP.direction dot vectorOriginDestination) / squaredLengthVecOD

        return when {
            t < 0.0 -> {
                // The projection of P falls outside the segment on the side of point A.
                // The closest point on the segment to P is A.
                // The shortest vector is the vector from P to A.
                // This is the same as vectorAP calculated earlier, but going from P to A is -(A-P) = P-A
                pointOrigin - pointP // Vector from P to A
            }

            t > 1.0 -> {
                // The projection of P falls outside the segment on the side of point B.
                // The closest point on the segment to P is B.
                // Point B = Point A + Vector AB
                val pointB = pointOrigin + vectorOriginDestination
                // The shortest vector is the vector from P to B.
                pointB - pointP // Vector from P to B
            }

            else -> {
                // The projection of P falls onto the segment [A, B].
                // The closest point on the segment is the projection point Q.
                // Q = A + t * vectorAB
                val closestPointOnSegment = pointOrigin + t * vectorOriginDestination
                // The shortest vector is the vector from P to Q.
                closestPointOnSegment - pointP // Vector from P to Q
            }
        }
    }

    /**
     * Calculates the closest point on a line segment (defined by a start point and a direction vector)
     * to an external point P, and the shortest vector from that closest point to P.
     *
     * @param pointP The external point.
     * @param shape The starting point of the line segment. And The direction vector defining the segment from A. Its magnitude is the segment's length.
     * @return the Resulting Intersection Vector
     */
    fun intersectionVector(
        pointP: GeokitPosition,
        shape: GeokitShape
    ): GeokitVector? {
        val vectors = shape.asVectors()
        var currentBest: Pair<Double, GeokitVector?> = Double.POSITIVE_INFINITY to null
        for (vector in vectors) {
            val vc = intersectionVector(pointP, vector)
            if (vc.fastLength() < currentBest.first) {
                currentBest = (vc.fastLength() to vc)
            }
        }
        return currentBest.second
    }

    /**
     * Determines if a point lies inside a polygon using the ray casting algorithm.
     * The algorithm works by casting a ray from the point to the right and counting intersections with polygon edges.
     * An odd number of intersections means the point is inside the polygon.
     *
     * @param position The point to check
     * @param shape The polygon represented as a list of vertices
     * @return true if the point lies inside the polygon, false otherwise
     */
    fun isPointInsidePolygon(position: GeokitPosition, shape: GeokitShape): Boolean {
        val vertexCount = shape.size
        var isInside = false

        for (i in 0 until vertexCount) {
            val currentVertex = shape[i]
            val nextVertex = shape[(i + 1) % vertexCount]

            val x1 = currentVertex.longitude
            val y1 = currentVertex.latitude
            val x2 = nextVertex.longitude
            val y2 = nextVertex.latitude

            if ((y1 > position.latitude) != (y2 > position.latitude) &&
                position.longitude < (x2 - x1) * (position.latitude - y1) / (y2 - y1) + x1
            ) {
                isInside = !isInside
            }
        }

        return isInside
    }

    /**
     * Finds the closest intersection point between a ray starting from the given position
     * in the specified direction and the given shape.
     *
     * @param position Starting point of the ray
     * @param lookingAt Direction angle in which to look for intersection
     * @param shape The shape to test for intersection
     * @return The closest intersection vector or null if no intersection found
     */
    fun intersectionVectorInDirection(
        position: GeokitPosition,
        lookingAt: Measure<Angle>,
        shape: GeokitShape
    ): GeokitVector? {
        // Convert compass bearing to mathematical angle (clockwise from north to counterclockwise from east)
        val lookingAtRadians = (lookingAt).normalize() `in` radians

        // Calculate direction vector components
        // Note: We swap sine and cosine and adjust signs to account for latitude/longitude orientation
        val directionVector = doubleArrayOf(
            sin(lookingAtRadians),  // longitude component (X)
            cos(lookingAtRadians)   // latitude component (Y)
        ).normalize()

        var closestIntersection: GeokitVector? = null
        var minDistance = Double.POSITIVE_INFINITY

        for (i in 0 until shape.size) {
            val start = shape[i]
            val end = shape[(i + 1) % shape.size]

            val x1 = position.longitude
            val y1 = position.latitude
            val x2 = x1 + directionVector[0]
            val y2 = y1 + directionVector[1]

            val x3 = start.longitude
            val y3 = start.latitude
            val x4 = end.longitude
            val y4 = end.latitude

            val denominator = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4)
            if (abs(denominator) < 1e-10) continue

            val t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / denominator
            val u = -((x1 - x2) * (y1 - y3) - (y1 - y2) * (x1 - x3)) / denominator

            if (t >= 0 && u >= 0 && u <= 1) {
                val intersectionX = x1 + t * (x2 - x1)
                val intersectionY = y1 + t * (y2 - y1)
                val distance = sqrt((intersectionX - x1).pow(2) + (intersectionY - y1).pow(2))

                if (distance < minDistance) {
                    minDistance = distance
                    closestIntersection = GeokitVector(
                        position,
                        GeokitPosition(latitude = intersectionY, longitude = intersectionX)
                    )
                }
            }
        }

        return closestIntersection
    }
}