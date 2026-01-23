package com.sophia.farm.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import ktx.ashley.optionalPropertyFor
import ktx.ashley.tagFor

class Player : Component {

    companion object {
        val Entity.player by optionalPropertyFor<Player>()
        val Entity.isPlayer by tagFor<Player>()
    }
}
