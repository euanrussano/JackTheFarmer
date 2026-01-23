package com.sophia.farm.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.sophia.farm.Keyboard
import com.sophia.farm.TileType
import com.sophia.farm.ecs.component.Player
import com.sophia.farm.ecs.component.Position
import com.sophia.farm.ecs.component.Position.Companion.position
import com.sophia.farm.ecs.component.Tilemap
import com.sophia.farm.ecs.component.Tilemap.Companion.tilemap
import ktx.app.KtxInputAdapter
import ktx.ashley.allOf

class KeyboardInputSystem: IteratingSystem(
    allOf(
        Player::class,
        Position::class
    ).get()
){

    val keyboard = Keyboard()

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        Gdx.input.inputProcessor = keyboard
    }

    override fun removedFromEngine(engine: Engine) {
        super.removedFromEngine(engine)
        Gdx.input.inputProcessor = keyboard
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val position = entity.position!!

        var newX = position.x
        var newY = position.y
        if (keyboard.isUpHeld){
            newY++
        } else if (keyboard.isDownHeld){
            newY--
        } else if (keyboard.isLeftHeld){
            newX--
        } else if (keyboard.isRightHeld){
            newX++
        }

        val tilemapEntity = engine.getEntitiesFor(allOf(Tilemap::class).get()).first()
        val tilemap = tilemapEntity.tilemap!!
        val isWalkable = tilemap.tiles.getOrNull(newX)?.getOrNull(newY) == TileType.GROUND
        if (isWalkable){
            position.x = newX
            position.y = newY
        }

        keyboard.clearAllKeys()
    }

}
