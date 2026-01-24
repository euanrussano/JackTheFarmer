package com.sophia.farm.controller

import com.badlogic.gdx.Input.Keys

object KeyboardPresets {

    fun arrowsAndWasd(): Map<Action, Set<Int>> = mapOf(
        Action.MOVE_UP to setOf(Keys.UP, Keys.W),
        Action.MOVE_DOWN to setOf(Keys.DOWN, Keys.S),
        Action.MOVE_LEFT to setOf(Keys.LEFT, Keys.A),
        Action.MOVE_RIGHT to setOf(Keys.RIGHT, Keys.D)
    )
}

