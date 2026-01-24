package com.sophia.farm.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Intersector
import com.sophia.farm.ecs.component.FieldOfView
import com.sophia.farm.ecs.component.FieldOfView.Companion.fieldOfView
import com.sophia.farm.ecs.component.Player.Companion.isPlayer
import com.sophia.farm.ecs.component.Position
import com.sophia.farm.ecs.component.Position.Companion.position
import com.sophia.farm.ecs.component.Tilemap
import com.sophia.farm.ecs.component.Tilemap.Companion.tilemap
import com.sophia.farm.ecs.component.event.Moved
import com.sophia.farm.ecs.component.event.Spawned
import com.sophia.farm.map.TileType
import ktx.ashley.allOf
import ktx.ashley.oneOf
import ktx.math.vec2

class VisibilitySystem(
    val useRadius: Boolean = false // make field circular (true) or squared (false)
): IteratingSystem(
    allOf(
        FieldOfView::class,
        Position::class
    ).oneOf(
        Moved::class,
        Spawned::class
    ).get()
) {

    val start = vec2()
    val end = vec2()
    val circle = Circle()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        if (entity.isPlayer){
            Gdx.app.log("VisibilitySystem", "Updating player field of view")
        }
        val fieldOfView = entity.fieldOfView!!
        val position = entity.position!!
        val x = position.x
        val y = position.y

        // consider the map bounds
        val tilemapEntity = engine.getEntitiesFor(allOf(Tilemap::class).get()).first()
        val tilemap = tilemapEntity.tilemap!!
        val width = tilemap.width
        val height = tilemap.height

        val candidateVisiblePoints = mutableListOf<Pair<Int, Int>>()
        for (dx in -fieldOfView.radius .. fieldOfView.radius){
            for (dy in -fieldOfView.radius .. fieldOfView.radius){
                val px = x + dx
                val py = y + dy
                if (useRadius){
                    val distance = (px - x)*(px - x) + (py - y)*(py - y)
                    if (distance > fieldOfView.radius*fieldOfView.radius) continue
                }
                if (px !in 0 until width || py !in 0 until height) continue
                candidateVisiblePoints.add(px to py)
            }
        }

        // line of sight
        val visiblePoints = mutableListOf<Pair<Int, Int>>()
        visiblePoints.addAll(candidateVisiblePoints)
        val iter = visiblePoints.iterator()
        start.set(x.toFloat(), y.toFloat())
        while(iter.hasNext()){
            val (px, py) = iter.next()
            end.set(px.toFloat(), py.toFloat())
            val isBlocking = candidateVisiblePoints.any {
                val (xi, yi) = it
                if (xi to yi == px to py) return@any false
                if (xi to yi == x to y) return@any false
                if (tilemap.tiles[xi][yi] != TileType.TREE) return@any false
                circle.set(xi.toFloat(), yi.toFloat(), 0.5f)
                return@any Intersector.intersectSegmentCircle(start, end, circle, null)
            }
            if (isBlocking){
                iter.remove()
            }
        }

        fieldOfView.visiblePoints.clear()
        fieldOfView.visiblePoints.addAll(visiblePoints)

        fieldOfView.revealedPoints.addAll(fieldOfView.visiblePoints)

    }
}
