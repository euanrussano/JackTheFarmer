package com.sophia.farm.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Pool
import ktx.ashley.optionalPropertyFor

class Shape : Component, Pool.Poolable {

    var type = ShapeType.CIRCLE
    var color = Color.RED

    override fun reset() {
        type = ShapeType.CIRCLE
        color = Color.RED
    }

    companion object {
        val Entity.shape by optionalPropertyFor<Shape>()
    }

    enum class ShapeType {
        RECTANGLE,
        CIRCLE
    }
}
