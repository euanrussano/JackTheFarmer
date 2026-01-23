package com.sophia.farm

import com.badlogic.gdx.Input.Keys
import ktx.app.KtxInputAdapter

class Keyboard: KtxInputAdapter {
    val keyUp = Keys.UP
    val keyDown = Keys.DOWN
    val keyLeft = Keys.LEFT
    val keyRight = Keys.RIGHT

    val isUpHeld get() = keyUp in pressedKeys
    val isDownHeld get() = keyDown in pressedKeys
    val isLeftHeld get() = keyLeft in pressedKeys
    val isRightHeld get() = keyRight in pressedKeys

    private val pressedKeys = mutableSetOf<Int>()

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
