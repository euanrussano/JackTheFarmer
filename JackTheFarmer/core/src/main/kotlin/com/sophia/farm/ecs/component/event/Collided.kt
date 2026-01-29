package com.sophia.farm.ecs.component.event

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import ktx.ashley.optionalPropertyFor
import com.badlogic.gdx.utils.Pool

class Collided : Component, Pool.Poolable {

    var other: Entity? = null

    override fun reset() {
        other = null
    }

    companion object {
        val Entity.collided by optionalPropertyFor<Collided>()
    }
}
