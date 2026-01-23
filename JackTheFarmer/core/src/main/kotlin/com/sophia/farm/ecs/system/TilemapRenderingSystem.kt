package com.sophia.farm.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.sophia.farm.TileType
import com.sophia.farm.ecs.component.Tilemap
import com.sophia.farm.ecs.component.Tilemap.Companion.tilemap
import ktx.ashley.allOf

class TilemapRenderingSystem(
    val shapeRenderer: ShapeRenderer
): IteratingSystem(
    allOf(
        Tilemap::class
    ).get()
) {

    override fun update(deltaTime: Float) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        super.update(deltaTime)
        shapeRenderer.end()
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val tilemap = entity.tilemap!!

        val tiles = tilemap.tiles
        shapeRenderer.color = Color.GREEN
        for (x in tiles.indices){
            for (y in tiles[0].indices){
                val tile = tiles[x][y]
                when(tile){
                    TileType.GROUND -> shapeRenderer.circle(x.toFloat(), y.toFloat(), 0.05f, 20)
                    TileType.TREE -> shapeRenderer.rect(x-0.45f, y-0.45f, 0.9f, 0.9f)
                }

            }
        }
    }

}
