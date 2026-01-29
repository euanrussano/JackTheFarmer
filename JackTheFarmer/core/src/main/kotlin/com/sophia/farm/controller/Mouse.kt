package com.sophia.farm.controller

import ktx.app.KtxInputAdapter

class Mouse: KtxInputAdapter {

    var x: Int = 0
    var y: Int = 0
    private var clickedThisFrame: Boolean = false
    val justClicked: Boolean
        get() {
            val result = clickedThisFrame
            clickedThisFrame = false
            return result
        }


    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        x = screenX
        y = screenY
        clickedThisFrame = true
        return true
    }


}
