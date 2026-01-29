package com.sophia.farm.ecs.component.animal

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import ktx.ashley.optionalPropertyFor
import com.badlogic.gdx.utils.Pool

class Scared : Component, Pool.Poolable {

    var factor = 0f

    override fun reset() {
        factor = 0f
    }

    companion object {
        val Entity.scared by optionalPropertyFor<Scared>()
    }
}
