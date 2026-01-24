package com.sophia.farm.map

import com.sophia.farm.map.TileType

interface MapGenerator {
    fun generate(width: Int, height: Int): GeneratedMap
}
