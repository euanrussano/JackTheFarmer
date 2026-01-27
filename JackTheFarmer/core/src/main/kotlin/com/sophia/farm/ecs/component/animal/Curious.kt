package com.sophia.farm.ecs.component.animal

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.tagFor

class Curious : Component, Pool.Poolable {

    override fun reset() {
    }

    companion object {
        val Entity.isCurious by tagFor<Curious>()
    }
}
