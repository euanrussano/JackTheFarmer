package com.sophia.farm.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import ktx.ashley.optionalPropertyFor
import com.badlogic.gdx.utils.Pool

class Diary : Component, Pool.Poolable {

    var messages = mutableListOf<String>()

    override fun reset() {
        messages.clear()
    }

    companion object {
        val Entity.diary by optionalPropertyFor<Diary>()
    }
}
