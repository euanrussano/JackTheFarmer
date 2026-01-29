package com.sophia.farm.ecs.system.intent

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.utils.viewport.Viewport
import com.sophia.farm.controller.Mouse
import com.sophia.farm.ecs.component.Player
import com.sophia.farm.ecs.component.event.ClickedOnWorld
import ktx.ashley.allOf
import ktx.ashley.configureEntity
import ktx.ashley.with
import ktx.math.vec2
import kotlin.math.round

class MouseInputSystem(val viewport: Viewport): IteratingSystem(
    allOf(
        Player::class
    ).get()
) {
    val mouse = Mouse()
    val touch = vec2()

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        val im = Gdx.input.inputProcessor as? InputMultiplexer?: InputMultiplexer()
        im.addProcessor(mouse)
        Gdx.input.inputProcessor = im
    }

    override fun removedFromEngine(engine: Engine) {
        super.removedFromEngine(engine)
        (Gdx.input.inputProcessor as? InputMultiplexer)?.removeProcessor(mouse)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        if (!mouse.justClicked) return
        viewport.unproject(touch.set(mouse.x.toFloat(), mouse.y.toFloat()))
        val x = round(touch.x).toInt()
        val y = round(touch.y).toInt()

        engine.configureEntity(entity){
            with<ClickedOnWorld>{
                this.x = x
                this.y = y
            }
        }
    }

}
