package com.sophia.farm

import com.sophia.farm.screen.FirstScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync

class JackTheFarmer : KtxGame<KtxScreen>() {
    override fun create() {
        KtxAsync.initiate()

        addScreen(FirstScreen(this))
        setScreen<FirstScreen>()
    }
}

