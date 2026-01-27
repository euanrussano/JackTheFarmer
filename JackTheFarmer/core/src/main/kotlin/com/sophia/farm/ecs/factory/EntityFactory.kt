package com.sophia.farm.ecs.factory

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.sophia.farm.ecs.component.FieldOfView
import com.sophia.farm.ecs.component.Name
import com.sophia.farm.map.TileType
import com.sophia.farm.ecs.component.Player
import com.sophia.farm.ecs.component.Position
import com.sophia.farm.ecs.component.Shape
import com.sophia.farm.ecs.component.Size
import com.sophia.farm.ecs.component.Tilemap
import com.sophia.farm.ecs.component.animal.Animal
import com.sophia.farm.ecs.component.animal.Curious
import com.sophia.farm.ecs.component.animal.Timid
import com.sophia.farm.ecs.component.event.Spawned
import com.sophia.farm.screen.FirstScreen
import ktx.ashley.entity
import ktx.ashley.with
import kotlin.random.Random

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
            with<Name>{
                text = "Jack"
            }
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
            with<FieldOfView>()
            with<Spawned>()
        }
    }

    fun fox(engine: Engine, x: Int, y: Int): Entity{
        return animal(engine, "Fox", x, y, Shape.ShapeType.CIRCLE, Color.BROWN, isCurious=true)
    }

    fun bunny(engine: Engine, x: Int, y: Int): Entity{
        return animal(engine, "Bunny", x, y, Shape.ShapeType.CIRCLE, Color.YELLOW, isTimid=true)
    }

    fun animal(engine: Engine, name: String, x: Int, y: Int, shapeType: Shape.ShapeType, color: Color, isCurious: Boolean=false, isTimid: Boolean=false): Entity{
        return engine.entity {
            with<Name>{
                this.text = name
            }
            with<Position>{
                this.x = x
                this.y = y
            }
            with<Size>{
                this.width = 1
                this.height = 1
            }
            with<Shape>{
                this.type = shapeType
                this.color = color
            }
            with<FieldOfView>()
            with<Animal>()
            if (isCurious) with<Curious>()
            if (isTimid) with<Timid>()
            with<Spawned>()
        }
    }

    fun randomAnimal(engine: Engine, x: Int, y: Int, random: Random): Entity{
        if (random.nextBoolean()){
            return bunny(engine, x, y)
        } else {
            return fox(engine, x, y)
        }
    }





}
