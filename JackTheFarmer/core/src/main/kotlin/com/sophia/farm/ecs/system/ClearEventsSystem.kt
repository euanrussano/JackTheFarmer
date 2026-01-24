package com.sophia.farm.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.sophia.farm.ecs.component.event.Moved
import com.sophia.farm.ecs.component.event.Spawned
import ktx.ashley.allOf
import ktx.ashley.oneOf
import ktx.ashley.remove

class ClearEventsSystem: IteratingSystem(
    oneOf(
        Moved::class,
        Spawned::class
    ).get()
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity.remove<Moved>()
        entity.remove<Spawned>()
    }
}
