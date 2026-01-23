package com.sophia.farm.screen

import com.badlogic.ashley.core.PooledEngine
import com.sophia.farm.JackTheFarmer
import com.sophia.farm.ecs.component.Position
import com.sophia.farm.ecs.component.Shape
import com.sophia.farm.ecs.component.Size
import com.sophia.farm.ecs.system.ShapeRenderingSystem
import ktx.app.KtxScreen
import ktx.ashley.entity
import ktx.ashley.with
import com.badlogic.gdx.graphics.Color

class FirstScreen(val game: JackTheFarmer) : KtxScreen {
    val engine = PooledEngine()

    val jack = engine.entity {
        with<Position>{
            x = 320
            y = 240
        }
        with<Size>{
            width = 32
            height = 32
        }
        with<Shape>{
            type = Shape.ShapeType.CIRCLE
            color = Color.RED
        }
    }

    override fun show() {
        engine.addSystem(ShapeRenderingSystem())
    }


    override fun render(delta: Float) {
        engine.update(delta)
    }
}
