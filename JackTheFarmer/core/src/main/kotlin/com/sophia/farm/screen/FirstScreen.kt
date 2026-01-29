package com.sophia.farm.screen

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.sophia.farm.JackTheFarmer
import ktx.app.KtxScreen
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.sophia.farm.ecs.factory.WorldBuilder
import com.sophia.farm.ecs.system.rendering.HUDSystem
import ktx.ashley.get
import ktx.ashley.getSystem
import kotlin.random.Random

class FirstScreen(val game: JackTheFarmer) : KtxScreen {
    val random = Random(1)
    val shapeRenderer = ShapeRenderer()
    val viewport = ExtendViewport(20f, 20f)
    val engine = PooledEngine()
    val worldBuilder = WorldBuilder(random, shapeRenderer, viewport)

    override fun show() {
        worldBuilder.build(engine)
    }


    override fun render(delta: Float) {
        shapeRenderer.projectionMatrix = viewport.camera.combined

        engine.update(delta)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
        engine[HUDSystem::class]?.stage?.viewport?.update(width, height)
    }
}
