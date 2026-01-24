package com.sophia.farm.ecs.factory

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.sophia.farm.map.TileType
import com.sophia.farm.ecs.component.Player
import com.sophia.farm.ecs.component.Position
import com.sophia.farm.ecs.component.Shape
import com.sophia.farm.ecs.component.Size
import com.sophia.farm.ecs.component.Tilemap
import ktx.ashley.entity
import ktx.ashley.with

object EntityFactory {

    fun tilemap(engine: Engine, tiles: Array<Array<TileType>>): Entity{
        return engine.entity {
            with<Tilemap>{
                this.tiles = tiles
            }
        }
    }

    fun player(engine: Engine, x: Int, y: Int): Entity{
        return engine.entity {
            with<Position>{
                this.x = x
                this.y = y
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
    }

}
