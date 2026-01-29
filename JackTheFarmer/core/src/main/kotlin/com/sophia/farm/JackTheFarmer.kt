package com.sophia.farm

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.sophia.farm.screen.FirstScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.assets.toInternalFile
import ktx.async.KtxAsync
import ktx.scene2d.Scene2DSkin

class JackTheFarmer : KtxGame<KtxScreen>() {
    override fun create() {
        KtxAsync.initiate()
        Scene2DSkin.defaultSkin = Skin("ui/uiskin.json".toInternalFile())

        addScreen(FirstScreen(this))
        setScreen<FirstScreen>()
    }
}

