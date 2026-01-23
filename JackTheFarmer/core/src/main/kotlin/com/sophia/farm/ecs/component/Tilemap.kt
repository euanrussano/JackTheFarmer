package com.sophia.farm.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import com.sophia.farm.TileType
import ktx.ashley.optionalPropertyFor

class Tilemap : Component, Pool.Poolable {

    var tiles: Array<Array<TileType>> = emptyArray() // tiles[x][y]

    // computed properties
    val width get() = tiles.size
    val height get() = tiles[0].size

    override fun reset() {
        tiles = emptyArray()
    }

    companion object {
        val Entity.tilemap by optionalPropertyFor<Tilemap>()
    }
}
