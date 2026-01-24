package com.sophia.farm.controller

import com.badlogic.gdx.Input
import ktx.app.KtxInputAdapter

class Keyboard(
    private val bindings: Map<Action, Set<Int>>
): KtxInputAdapter {

    private val pressedKeys = mutableSetOf<Int>()

    fun isHeld(action: Action): Boolean =
        pressedKeys.any { it in (bindings[action] ?: emptySet()) }

    fun clearAllKeys(){
        pressedKeys.clear()
    }

    override fun keyDown(keycode: Int): Boolean {
        pressedKeys += keycode
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        pressedKeys -= keycode
        return true
    }


}
