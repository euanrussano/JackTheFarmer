package com.sophia.farm.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.optionalPropertyFor

class FieldOfView : Component, Pool.Poolable {

    var radius: Int = 5
    val revealedPoints = mutableListOf<Pair<Int, Int>>()
    val visiblePoints = mutableListOf<Pair<Int, Int>>()

    override fun reset() {
        revealedPoints.clear()
        visiblePoints.clear()
    }

    companion object {
        val Entity.fieldOfView by optionalPropertyFor<FieldOfView>()
    }
}
