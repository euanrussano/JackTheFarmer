package com.sophia.farm.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import ktx.ashley.optionalPropertyFor
import com.badlogic.gdx.utils.Pool

class Health : Component, Pool.Poolable {

    var health = 10
    var maxHealth = 10

    override fun reset() {
        health = 10
        maxHealth = 10
    }

    companion object {
        val Entity.health by optionalPropertyFor<Health>()
    }
}
