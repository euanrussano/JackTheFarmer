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
    val mapWidth = 20
    val mapHeight = 20

    val mapGenerator = DungeonMapGenerator(random)

    override fun show() {
        val generatedMap = mapGenerator.generate(mapWidth, mapHeight)

        val tilemap = EntityFactory.tilemap(engine, generatedMap.tiles)
        val jack = EntityFactory.player(engine, generatedMap.playerSpawn.first,generatedMap.playerSpawn.second)

        // spawn some bunnies
        val spawnPointsAvailable = generatedMap.spawnPoints.toMutableList()
        for (i in 0 .. 4){
            if (spawnPointsAvailable.isEmpty()) break
            val idx = random.nextInt(spawnPointsAvailable.size)
            val (x, y) = spawnPointsAvailable.removeAt(idx)
            EntityFactory.bunny(engine, x, y)
        }

        engine.addSystem(KeyboardInputSystem())
        engine.addSystem(MovementSystem())
        engine.addSystem(VisibilitySystem())
        engine.addSystem(TilemapRenderingSystem(shapeRenderer))
        engine.addSystem(ShapeRenderingSystem(shapeRenderer))
        engine.addSystem(ClearEventsSystem())
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
