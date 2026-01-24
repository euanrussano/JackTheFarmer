package com.sophia.farm.map

import com.sophia.farm.map.TileType

class GeneratedMap(
    val tiles: Array<Array<TileType>>,
    val playerSpawn: Pair<Int, Int>,
    val spawnPoints: List<Pair<Int, Int>> = listOf()
)
