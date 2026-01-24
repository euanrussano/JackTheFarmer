package com.sophia.farm.map

import com.badlogic.gdx.math.Rectangle
import com.sophia.farm.map.TileType
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class DungeonMapGenerator(val random: Random): MapGenerator {
    private class Room(val x: Int, val y: Int, val width: Int, val height: Int){
        val center = x + width/2 to y + height/2
        fun applyToMap(tiles: Array<Array<TileType>>){
            for (dx in 0 .. width){
                for (dy in 0 .. height){
                    tiles[x+dx][y+dy] = TileType.GROUND
                }
            }
        }
    }

    private class Corridor(val startX: Int, val startY: Int, val endX: Int, val endY: Int){
        fun applyToMap(tiles: Array<Array<TileType>>){
            if (startX == endX) {
                // vertical corridor
                for (y in min(startY, endY)..max(startY, endY)) {
                    tiles[startX][y] = TileType.GROUND
                }
            } else {
                // horizontal corridor
                for (x in min(startX, endX)..max(startX, endX)) {
                    tiles[x][startY] = TileType.GROUND
                }
            }
        }
    }

    override fun generate(width: Int, height: Int): GeneratedMap {
        val tiles = Array(width){ Array(height){ TileType.TREE } }

        val rooms = listOf(
            Room(2, 1, 3, 3),
            Room(13, 1, 3,3),
            Room(3, 11, 3, 3),
            Room(11, 14, 3,3)
        )

        for ((i, room) in rooms.withIndex()) {
            room.applyToMap(tiles)
            var prevRoom= rooms.last()
            if (i > 0){
                prevRoom = rooms[i-1]
            }
            val (x1, y1) = room.center
            val (x2, y2) = prevRoom.center

            if (random.nextBoolean()) {
                // vertical then horizontal
                Corridor(x1, y1, x1, y2).applyToMap(tiles)
                Corridor(x1, y2, x2, y2).applyToMap(tiles)
            } else {
                // horizontal then vertical
                Corridor(x1, y1, x2, y1).applyToMap(tiles)
                Corridor(x2, y1, x2, y2).applyToMap(tiles)
            }
        }

        // place the player in the center of the first room
        val room = rooms.first()
        val playerSpawn = room.center

        val spawnPoints = mutableListOf<Pair<Int, Int>>()
        for (room in rooms.drop(1)){
            spawnPoints.add(room.center)
        }

        return GeneratedMap(tiles, playerSpawn, spawnPoints)
    }


}
