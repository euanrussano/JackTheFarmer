package com.sophia.farm.ecs.factory

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.Viewport
import com.sophia.farm.ecs.system.AggressiveBehaviourSystem
import com.sophia.farm.ecs.system.intent.AnimalSteeringSystem
import com.sophia.farm.ecs.system.ClearEventsSystem
import com.sophia.farm.ecs.system.CollisionSystem
import com.sophia.farm.ecs.system.intent.KeyboardInputSystem
import com.sophia.farm.ecs.system.MovementSystem
import com.sophia.farm.ecs.system.ScareAggressiveAnimalAwaySystem
import com.sophia.farm.ecs.system.rendering.ShapeRenderingSystem
import com.sophia.farm.ecs.system.rendering.TilemapRenderingSystem
import com.sophia.farm.ecs.system.VisibilitySystem
import com.sophia.farm.ecs.system.intent.MouseInputSystem
import com.sophia.farm.ecs.system.rendering.CameraFollowSystem
import com.sophia.farm.ecs.system.rendering.HUDSystem
import com.sophia.farm.map.DungeonMapGenerator
import kotlin.random.Random

class WorldBuilder(
    private val random: Random,
    private val shapeRenderer: ShapeRenderer,
    private val viewport: Viewport,
) {

    fun build(engine: PooledEngine) {
        buildWorld(engine)
        installSystems(engine)
    }

    private fun buildWorld(engine: PooledEngine) {
        val mapGenerator = DungeonMapGenerator(random)
        val generatedMap = mapGenerator.generate(20, 20)

        EntityFactory.tilemap(engine, generatedMap.tiles)
        EntityFactory.player(
            engine,
            generatedMap.playerSpawn.first,
            generatedMap.playerSpawn.second
        )

        val spawnPoints = generatedMap.spawnPoints.toMutableList()
        repeat(5) {
            if (spawnPoints.isEmpty()) return
            val (x, y) = spawnPoints.removeAt(random.nextInt(spawnPoints.size))
            EntityFactory.randomAnimal(engine, x, y, random)
        }
    }

    private fun installSystems(engine: PooledEngine) {
        engine.addSystem(MouseInputSystem(viewport))
        engine.addSystem(KeyboardInputSystem())
        engine.addSystem(AnimalSteeringSystem())
        engine.addSystem(CollisionSystem())
        engine.addSystem(ScareAggressiveAnimalAwaySystem())
        engine.addSystem(AggressiveBehaviourSystem())
        engine.addSystem(MovementSystem())
        engine.addSystem(VisibilitySystem())
        engine.addSystem(CameraFollowSystem(viewport.camera))
        engine.addSystem(TilemapRenderingSystem(shapeRenderer))
        engine.addSystem(ShapeRenderingSystem(shapeRenderer))
        engine.addSystem(HUDSystem(viewport))
        engine.addSystem(ClearEventsSystem())
    }
}

