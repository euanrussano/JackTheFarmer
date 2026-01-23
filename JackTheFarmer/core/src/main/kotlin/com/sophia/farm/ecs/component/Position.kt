package com.sophia.farm.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.optionalPropertyFor

class Position : Component, Pool.Poolable {

    var x = 0
    var y = 0

    override fun reset() {
        x = 0
        y = 0
    }

    companion object {
        val Entity.position by optionalPropertyFor<Position>()
    }
}
