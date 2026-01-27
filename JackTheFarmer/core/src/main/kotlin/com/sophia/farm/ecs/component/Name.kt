package com.sophia.farm.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import ktx.ashley.optionalPropertyFor
import com.badlogic.gdx.utils.Pool

class Name : Component, Pool.Poolable {

    var text = "<Unnamed>"
    override fun reset() {
        text = "<Unnamed>"
    }

    companion object {
        val Entity.name by optionalPropertyFor<Name>()
    }
}
