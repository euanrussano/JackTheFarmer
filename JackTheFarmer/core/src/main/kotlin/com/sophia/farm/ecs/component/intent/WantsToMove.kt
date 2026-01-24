package com.sophia.farm.ecs.component.intent

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import com.sophia.farm.Direction
import ktx.ashley.optionalPropertyFor

class WantsToMove : Component, Pool.Poolable {
    var direction = Direction.RIGHT

    override fun reset() {
        direction = Direction.RIGHT
    }

    companion object {
        val Entity.wantsToMove by optionalPropertyFor<WantsToMove>()
    }
}
