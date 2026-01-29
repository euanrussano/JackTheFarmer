package com.sophia.farm.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.sophia.farm.ecs.component.CanScareAggressiveAnimal
import com.sophia.farm.ecs.component.Name.Companion.name
import com.sophia.farm.ecs.component.animal.Aggressive.Companion.isAggressive
import com.sophia.farm.ecs.component.animal.Animal.Companion.isAnimal
import com.sophia.farm.ecs.component.animal.Scared
import com.sophia.farm.ecs.component.animal.Scared.Companion.scared
import com.sophia.farm.ecs.component.event.Collided
import com.sophia.farm.ecs.component.event.Collided.Companion.collided
import ktx.ashley.allOf
import ktx.ashley.configureEntity
import ktx.ashley.with

class ScareAggressiveAnimalAwaySystem: IteratingSystem(
    allOf(
        Collided::class,
        CanScareAggressiveAnimal::class
    ).get()
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val collided = entity.collided!!

        val other = collided.other!!
        if (!other.isAnimal) return
        if (!other.isAggressive) return

        val scared = other.scared
        if (scared != null ){
            scared.factor += 0.5f
        } else {
            engine.configureEntity(other) {
                with<Scared>()
            }
        }

        val factor = other.scared?.factor!!
        println("${entity.name} tries to scare a ${other.name}. scaring: $factor/1.0")

        if ((other.scared?.factor ?: 0f) >= 1f) {
            println("${entity.name} scared a ${other.name} away!")
            engine.removeEntity(other)
        }
    }

}
