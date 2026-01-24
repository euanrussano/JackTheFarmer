package com.sophia.farm.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Rectangle
import com.sophia.farm.ecs.component.Position
import com.sophia.farm.ecs.component.Position.Companion.position
import com.sophia.farm.ecs.component.Size
import com.sophia.farm.ecs.component.Size.Companion.size
import com.sophia.farm.ecs.component.Tilemap
import com.sophia.farm.ecs.component.Tilemap.Companion.tilemap
import com.sophia.farm.ecs.component.event.Moved
import com.sophia.farm.ecs.component.intent.WantsToMove
import com.sophia.farm.ecs.component.intent.WantsToMove.Companion.wantsToMove
import com.sophia.farm.map.TileType
import ktx.ashley.allOf
import ktx.ashley.configureEntity
import ktx.ashley.remove
import ktx.ashley.with

class MovementSystem: IteratingSystem(
    allOf(
        Position::class,
        WantsToMove::class
    ).get()
) {
    val rect1 = Rectangle()
    val rect2 = Rectangle()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val wantsToMove = entity.wantsToMove!!
        val position = entity.position!!
        val size = entity.size!!

        val width = size.width
        val height = size.height

        // calculate potential position before moving for validation
        val newX = position.x + wantsToMove.direction.dx
        val newY = position.y + wantsToMove.direction.dy

        rect1.set(newX-width/2f, newY-height/2f, width.toFloat(), height.toFloat())

        // clean intent
        entity.remove<WantsToMove>()

        // check if position is valid on map
        val tilemapEntity = engine.getEntitiesFor(allOf(Tilemap::class).get()).first()
        val tilemap = tilemapEntity.tilemap!!
        val isWalkable = tilemap.tiles.getOrNull(newX)?.getOrNull(newY) == TileType.GROUND

        // check if does not overlap other entities
        var overlapsOther = false
        for (other in engine.getEntitiesFor(allOf(Position::class, Size::class).get())) {
            if (other == entity) continue

            val otherPosition = other.position!!
            val otherSize = other.size!!

            val otherWidth = otherSize.width
            val otherHeight = otherSize.height
            rect2.set(otherPosition.x-otherWidth/2f, otherPosition.y-otherHeight/2f, otherWidth.toFloat(), otherHeight.toFloat())

            if (rect1.overlaps(rect2)){
                overlapsOther = true
                break
            }
        }
        if (isWalkable && !overlapsOther){
            position.x = newX
            position.y = newY
            engine.configureEntity(entity){
                with<Moved>()
            }
        }
    }
}
