package com.sophia.farm.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.sophia.farm.ecs.component.FieldOfView
import com.sophia.farm.ecs.component.FieldOfView.Companion.fieldOfView
import com.sophia.farm.ecs.component.Player
import com.sophia.farm.ecs.component.Position
import com.sophia.farm.ecs.component.Position.Companion.position
import com.sophia.farm.ecs.component.Shape
import com.sophia.farm.ecs.component.Shape.Companion.shape
import com.sophia.farm.ecs.component.Size
import com.sophia.farm.ecs.component.Size.Companion.size
import ktx.ashley.allOf
import kotlin.math.max

class ShapeRenderingSystem(
    val shapeRenderer: ShapeRenderer
): IteratingSystem(
    allOf(
        Position::class,
        Size::class,
        Shape::class
    ).get()
) {

    override fun update(deltaTime: Float) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        super.update(deltaTime)
        shapeRenderer.end()
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val player = engine.getEntitiesFor(allOf(Player::class, FieldOfView::class).get()).first()
        val fieldOfView = player.fieldOfView!!
        val visiblePoints = fieldOfView.visiblePoints

        val position = entity.position!!

        if (position.x to position.y !in visiblePoints) return

        val x = position.x.toFloat()
        val y = position.y.toFloat()


        val size = entity.size!!
        val shape = entity.shape!!



        val width = size.width.toFloat()
        val height = size.height.toFloat()

        shapeRenderer.color = shape.color

        when (shape.type){
            Shape.ShapeType.RECTANGLE -> shapeRenderer.rect(x-width/2, y-height/2, width, height)
            Shape.ShapeType.CIRCLE -> {
                val radius = max(width, height)/2f
                shapeRenderer.circle(x, y, radius, 20)
            }
        }

    }

}
