package com.sophia.farm.map

import com.sophia.farm.map.TileType
import kotlin.random.Random
import kotlin.random.nextInt

class RandomMapGenerator(val random: Random): MapGenerator {
    override fun generate(width: Int, height: Int): GeneratedMap {
        val tiles = Array(width){ Array(height){ TileType.GROUND } }

        // surround with trees
        for (x in 0 until width){
            tiles[x][0] =  TileType.TREE
            tiles[x][height-1] =  TileType.TREE
        }

        for (y in 0 until height){
            tiles[0][y] =  TileType.TREE
            tiles[height-1][y] =  TileType.TREE
        }

        // throw a bunch of trees
        for (i in 0 until (width*height*0.3f).toInt()){
            val x = random.nextInt(width)
            val y = random.nextInt(height)
            tiles[x][y] = TileType.TREE
        }

        // find all valid spots to place the player
        val validSpots = mutableListOf<Pair<Int, Int>>()
        for (x in tiles.indices){
            for (y in tiles[x].indices){
                val tile = tiles[x][y]
                if (tile == TileType.GROUND){
                    validSpots.add(x to y)
                }
            }
        }

        return GeneratedMap(tiles, validSpots.random(random))
    }

}
