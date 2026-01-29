package com.sophia.farm.ecs.component.event

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import ktx.ashley.optionalPropertyFor
import com.badlogic.gdx.utils.Pool

class ClickedOnWorld : Component, Pool.Poolable {

    var x = 0
    var y = 0
    override fun reset() {
        x = 0
        y = 0
    }

    companion object {
        val Entity.clickedOnWorld by optionalPropertyFor<ClickedOnWorld>()
    }
}
