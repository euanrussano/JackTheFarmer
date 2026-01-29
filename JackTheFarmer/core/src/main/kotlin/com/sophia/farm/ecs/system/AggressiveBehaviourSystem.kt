package com.sophia.farm.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.systems.IteratingSystem
import com.sophia.farm.ecs.component.Diary.Companion.diary
import com.sophia.farm.ecs.component.Health.Companion.health
import com.sophia.farm.ecs.component.Name.Companion.name
import com.sophia.farm.ecs.component.animal.Aggressive
import com.sophia.farm.ecs.component.animal.Aggressive.Companion.aggressive
import com.sophia.farm.ecs.component.animal.Animal
import com.sophia.farm.ecs.component.event.Collided
import com.sophia.farm.ecs.component.event.Collided.Companion.collided
import ktx.ashley.allOf
import ktx.ashley.configureEntity

class AggressiveBehaviourSystem: IteratingSystem(
    allOf(
        Animal::class,
        Aggressive::class,
        Collided::class
    ).get()
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val collided = entity.collided!!

        val other = collided.other!!
        val otherHealth = other.health?: return

        val damage = 1
        otherHealth.health -= damage

        val msg = "${entity.name} attacks ${other.name} for $damage damage"

        entity.diary?.messages?.add(msg)
        other.diary?.messages?.add(msg)

    }
}
