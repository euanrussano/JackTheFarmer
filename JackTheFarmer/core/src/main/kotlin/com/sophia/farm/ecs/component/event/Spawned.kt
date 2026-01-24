package com.sophia.farm.ecs.component.event

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.optionalPropertyFor

class Spawned : Component, Pool.Poolable {

    // TODO fields
    override fun reset() {
    }

    companion object {
        val Entity.spawned by optionalPropertyFor<Spawned>()
    }
}
