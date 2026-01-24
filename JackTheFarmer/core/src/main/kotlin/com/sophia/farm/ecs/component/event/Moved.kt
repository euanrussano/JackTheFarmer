package com.sophia.farm.ecs.component.event

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import ktx.ashley.optionalPropertyFor
import ktx.ashley.tagFor

class Moved : Component {

    companion object {
        val Entity.hasMoved by tagFor<Moved>()
    }
}
