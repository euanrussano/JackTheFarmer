package com.sophia.farm.ecs.system.intent

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import com.sophia.farm.Direction
import com.sophia.farm.ecs.component.FieldOfView
import com.sophia.farm.ecs.component.FieldOfView.Companion.fieldOfView
import com.sophia.farm.ecs.component.Position
import com.sophia.farm.ecs.component.Position.Companion.position
import com.sophia.farm.ecs.component.animal.Animal
import com.sophia.farm.ecs.component.animal.Curious
import com.sophia.farm.ecs.component.animal.Curious.Companion.isCurious
import com.sophia.farm.ecs.component.animal.Timid
import com.sophia.farm.ecs.component.animal.Timid.Companion.isTimid
import com.sophia.farm.ecs.component.intent.WantsToMove
import ktx.ashley.allOf
import ktx.ashley.configureEntity
import ktx.ashley.exclude
import ktx.ashley.oneOf
import ktx.ashley.with
import ktx.math.vec2
import kotlin.math.abs

class AnimalSteeringSystem: IntervalIteratingSystem(
    allOf(
        Animal::class,
        FieldOfView::class,
        Position::class
    ).oneOf(
        Timid::class,
        Curious::class
    ).get(),
    1f
) {

    val tempVec = vec2()
    val escapeVec = vec2()
    override fun processEntity(entity: Entity) {
        val position = entity.position!!
        val fieldOfView = entity.fieldOfView!!

        val ax = position.x.toFloat()
        val ay = position.y.toFloat()

        val notAnimalEntities = engine.getEntitiesFor(allOf(Position::class).exclude(Animal::class).get())
        val notAnimalEntitiesInFieldOfView = notAnimalEntities.filter {
            val otherPosition = it.position!!
            otherPosition.x to otherPosition.y in fieldOfView.visiblePoints
        }
        escapeVec.setZero()
        for (otherEntity in notAnimalEntitiesInFieldOfView){
            val otherPosition = otherEntity.position!!
            val ex = otherPosition.x.toFloat()
            val ey = otherPosition.y.toFloat()
            if (entity.isCurious) {
                escapeVec.add(ex - ax, ey - ay)
            } else if (entity.isTimid) {
                escapeVec.add(ax - ex, ay - ey)
            }
        }
        escapeVec.nor()

        if (escapeVec.len() == 0f) return

        val dx = escapeVec.x
        val dy = escapeVec.y
        var direction: Direction
        if (abs(dx) > abs(dy)){
            if (dx > 0){
                direction = Direction.RIGHT
            } else {
                direction = Direction.LEFT
            }
        } else {
            if (dy > 0){
                direction = Direction.UP
            } else {
                direction = Direction.DOWN
            }
        }

        engine.configureEntity(entity){
            with<WantsToMove>{
                this.direction = direction
            }
        }



    }

}
