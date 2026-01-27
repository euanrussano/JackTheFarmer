package com.sophia.farm.screen

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.sophia.farm.JackTheFarmer
import com.sophia.farm.ecs.component.Position
import com.sophia.farm.ecs.component.Shape
import com.sophia.farm.ecs.component.Size
import com.sophia.farm.ecs.system.ShapeRenderingSystem
import ktx.app.KtxScreen
import ktx.ashley.entity
import ktx.ashley.with
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.sophia.farm.map.TileType
import com.sophia.farm.ecs.component.Player
import com.sophia.farm.ecs.component.Tilemap
import com.sophia.farm.ecs.factory.EntityFactory
import com.sophia.farm.ecs.factory.WorldBuilder
import com.sophia.farm.ecs.system.ClearEventsSystem
import com.sophia.farm.ecs.system.KeyboardInputSystem
import com.sophia.farm.ecs.system.MovementSystem
import com.sophia.farm.ecs.system.TilemapRenderingSystem
import com.sophia.farm.ecs.system.VisibilitySystem
import com.sophia.farm.map.DungeonMapGenerator
import com.sophia.farm.map.RandomMapGenerator
import kotlin.random.Random

class FirstScreen(val game: JackTheFarmer) : KtxScreen {
    val random = Random(1)
    val shapeRenderer = ShapeRenderer()
    val viewport = ExtendViewport(20f, 20f)
    val engine = PooledEngine()
    val worldBuilder = WorldBuilder(random, shapeRenderer)

    override fun show() {
        worldBuilder.build(engine)
    }


    override fun render(delta: Float) {
        shapeRenderer.projectionMatrix = viewport.camera.combined

        engine.update(delta)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
        viewport.camera.position.set(viewport.worldWidth/2f-0.5f, viewport.worldHeight/2f-0.5f, 0f)
    }
}
