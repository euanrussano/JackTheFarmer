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
import com.sophia.farm.TileType
import com.sophia.farm.ecs.component.Player
import com.sophia.farm.ecs.component.Tilemap
import com.sophia.farm.ecs.system.KeyboardInputSystem
import com.sophia.farm.ecs.system.TilemapRenderingSystem
import kotlin.random.Random

class FirstScreen(val game: JackTheFarmer) : KtxScreen {
    val random = Random(1)
    val shapeRenderer = ShapeRenderer()
    val viewport = ExtendViewport(20f, 20f)
    val engine = PooledEngine()
    val mapWidth = 20
    val mapHeight = 20

    val tiles = Array(mapWidth){x ->
        Array(mapHeight){y ->
            // surrounded by trees
            if (x in listOf(0, mapWidth-1)){
                return@Array TileType.TREE
            }
            if (y in listOf(0, mapHeight-1)){
                return@Array TileType.TREE
            }

            // throw a bunch of trees
            if (random.nextFloat() < 0.3f){
                return@Array TileType.TREE
            } else {
                return@Array TileType.GROUND
            }

        }

    }
    val tilemap = engine.entity {
        with<Tilemap>{
            this.tiles = this@FirstScreen.tiles
        }
    }

    val jack = engine.entity {
        with<Position>{
            x = 5
            y = 5
        }
        with<Size>{
            width = 1
            height = 1
        }
        with<Shape>{
            type = Shape.ShapeType.CIRCLE
            color = Color.RED
        }
        with<Player>()

    }


    override fun show() {
        engine.addSystem(KeyboardInputSystem())
        engine.addSystem(TilemapRenderingSystem(shapeRenderer))
        engine.addSystem(ShapeRenderingSystem(shapeRenderer))
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
