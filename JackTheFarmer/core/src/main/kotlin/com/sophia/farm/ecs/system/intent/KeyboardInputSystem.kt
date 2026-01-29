package com.sophia.farm.ecs.system.intent

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.sophia.farm.Direction
import com.sophia.farm.controller.Action
import com.sophia.farm.controller.Keyboard
import com.sophia.farm.controller.KeyboardPresets
import com.sophia.farm.ecs.component.Player
import com.sophia.farm.ecs.component.Position
import com.sophia.farm.ecs.component.intent.WantsToMove
import ktx.ashley.allOf
import ktx.ashley.configureEntity
import ktx.ashley.with

class KeyboardInputSystem: IteratingSystem(
    allOf(
        Player::class,
        Position::class
    ).get()
){

    val keyboard = Keyboard(KeyboardPresets.arrowsAndWasd())

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        val im = Gdx.input.inputProcessor as? InputMultiplexer?: InputMultiplexer()
        im.addProcessor(keyboard)
        Gdx.input.inputProcessor = im
    }

    override fun removedFromEngine(engine: Engine) {
        super.removedFromEngine(engine)
        (Gdx.input.inputProcessor as? InputMultiplexer)?.removeProcessor(keyboard)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        var direction: Direction? = null

        if (keyboard.isHeld(Action.MOVE_UP)){
            direction = Direction.UP
        } else if (keyboard.isHeld(Action.MOVE_DOWN)){
            direction = Direction.DOWN
        } else if (keyboard.isHeld(Action.MOVE_LEFT)){
            direction = Direction.LEFT
        } else if (keyboard.isHeld(Action.MOVE_RIGHT)){
            direction = Direction.RIGHT
        }

        if (direction != null){
            engine.configureEntity(entity){
                with<WantsToMove>{
                    this.direction = direction
                }
            }
        }

        keyboard.clearAllKeys()
    }

}
