package com.sophia.farm.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.optionalPropertyFor

class Size : Component, Pool.Poolable {

    var width = 0
    var height = 0

    override fun reset() {
        width = 0
        height = 0
    }

    companion object {
        val Entity.size by optionalPropertyFor<Size>()
    }
}
