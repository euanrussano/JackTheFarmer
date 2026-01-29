package com.sophia.farm.ecs.component.animal

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import ktx.ashley.optionalPropertyFor
import com.badlogic.gdx.utils.Pool

class Aggressive : Component, Pool.Poolable {

    override fun reset() {
    }

    companion object {
        val Entity.aggressive by optionalPropertyFor<Aggressive>()
    }
}
