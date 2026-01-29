package com.sophia.farm.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import ktx.ashley.optionalPropertyFor
import com.badlogic.gdx.utils.Pool

class CanScareAggressiveAnimal : Component, Pool.Poolable {

    override fun reset() {
    }

    companion object {
        val Entity.canScareAggressiveAnimal by optionalPropertyFor<CanScareAggressiveAnimal>()
    }
}
