# 2.2. Jack Moves around the Abandoned Farm (basic movement)

**Story behind:**

*Jack's grandfather used to have a farm, but he disappeared somehow. Now Jack is the only one left to take care of the farm.*

A farmer without a farm is completely pointles, so in this chapter will put together a basic map representing jack's farm, draw it, and let Jack walk around a bit. We will continue from where we stopped in **Chapter 2.1**.

## Defining the Map

A very popular approach used to define maps in programming is using the concept of 2D Tile-based maps. In this approach, the map is partitioned in a rectangular grid of tiles, where each tile represents a particular type of terrain. For example, a tile might represent a grassland, a mountain, a forest, a lake, etc.

![Map example](images/02_02_tilemap_concept.png)

To start simple, we will allow 2 types of tiles: ground and tree. That will be enough to get start, even though we already envision more complex maps in the future. But for good programming practice, we should follow the principle:

  *Keep the code as simple as possible*

We create an enum class called `TileType` that will represent the different types of tiles in our map in its own file  e.g `src/main/kotlin/com/sophia/farm/TileType.kt`:

```kotlin
package com.sophia.farm

enum class TileType {
    GROUND,
    TREE
}
```

We also need a `Component` that tells us that the Entity is a Tiled map. For that we create a new class called `TileMap` in its own file e.g `src/main/kotlin/com/sophia/farm/ecs/component/TileMap.kt`:

```kotlin
package com.sophia.farm.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import com.sophia.farm.TileType
import ktx.ashley.optionalPropertyFor

class Tilemap : Component, Pool.Poolable {

    var tiles: Array<Array<TileType>> = emptyArray() // tiles[x][y]

    // computed properties
    val width get() = tiles.size
    val height get() = tiles[0].size

    override fun reset() {
        tiles = emptyArray()
    }

    companion object {
        val Entity.tilemap by optionalPropertyFor<Tilemap>()
    }
}
```

In this class, we also add the `width` and `height` properties, which will be used to access the dimensions of the map. These are called **computed properties** because they are calculated from the values of other properties, on demand. In this case, the `width` is the number of columns in the map, and the `height` is the number of rows. If the map changes, it don't need to manually update those computed properties, they will be updated automatically (they are recalculated on every access).


Now that we have the enum and the component, we can create the map. For now we can just do it directly in the `FirstScreen.kt` class. Later, to keep things organized we will move it to a better place.

```kotlin
val random = Random(1)

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
```
We instantiate an object from `Random(seed)` kotlin class to be able to generate (pseudo) random numbers and fix a seed so the map is always the same. The actual number of the seed doesn't really matter, as long as you fix it, the random numbers will always be generated in the same order.

So our first map is just like a matrix of `TileType`, with the width and height of the map. We can now access the map from anywhere in the game, and draw it using the `TileType` to determine the color of each tile. As you can see, I chose to make it surrounded by trees (so I don't need to worry about player falling outside the map). I also threw a bunch of trees randomly to make the map more interesting (for each tile, there is a 30% chance of being a tree).

That's all great, but what is the size of each tile? We never defined it explicitly. For the sake of simplicity we can imagine that each tile is 1x1 meter. In our current setup, we have the ratio:

 - 1 tile = 1 meter
 - 1 meter = 1 pixel
  
But that would show each tile super small. So we will rescale the world units to make each meter more than just 1 pixel.

LibGdx helps us to do that easily, by defining a `Viewport` class that will hold the number of world units shown in the screen. There are different kinds of `Viewport`s (e.g `FitViewport`, `ExtendViewport`,`ScreenViewport`, etc). Each one serves a different purpose when rendering your game. We will use a `ExtendViewport`.


The `ExtendViewport` preserves the world's aspect ratio by expanding it in one direction to fill the viewport, without introducing black bars. It first scales the world to fit within the viewport, and then stretches the shorter dimension to match the viewport's size.

![Viewport example](images/02_02_viewport.png)

Let's define the viewport then, and additionally we also want to pull out the `ShapeRenderer`that we defined before inside the `ShapeRenderingSystem` to the `FirstScreen.kt` class, so it can be reused by the system that renders the map:

```kotlin
val shapeRenderer = ShapeRenderer()
val viewport = ExtendViewport(20f, 20f)
```

So now we have much more control of the correspondence between world units and screen units. Independent of the size of the screen, we will consistently show 20x20 units of the world. This will be automatically rescaled when   the screen changes size. To make sure that happens, we need to override the `resize` method in the `FirstScreen` class:

```kotlin
override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
        viewport.camera.position.set(viewport.worldWidth/2f-0.5f, viewport.worldHeight/2f-0.5f, 0f)
    }
```
The `viewport.update` method is used to update the viewport to match the new screen size, and the `viewport.camera.position` is used to change the position of the camera to where we want, in this case to center the camera in the middle of the viewport.

To make sure that the `ShapeRenderer` draws the world taking into account the viewport, we need to add the following line of code inside the `render` method of the `FirstScreen` class:

```kotlin
shapeRenderer.projectionMatrix = viewport.camera.combined
```

We also need to reposition and resize our main hero Jack accordingly:

```kotlin
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
    }
```

Now let's define the system to render the `Tilemap` entity:

```kotlin
package com.sophia.farm.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.sophia.farm.TileType
import com.sophia.farm.ecs.component.Tilemap
import com.sophia.farm.ecs.component.Tilemap.Companion.tilemap
import ktx.ashley.allOf

class TilemapRenderingSystem(
    val shapeRenderer: ShapeRenderer
): IteratingSystem(
    allOf(
        Tilemap::class
    ).get()
) {

    override fun update(deltaTime: Float) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        super.update(deltaTime)
        shapeRenderer.end()
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val tilemap = entity.tilemap!!

        val tiles = tilemap.tiles
        shapeRenderer.color = Color.GREEN
        for (x in tiles.indices){
            for (y in tiles[0].indices){
                val tile = tiles[x][y]
                when(tile){
                    TileType.GROUND -> shapeRenderer.circle(x.toFloat(), y.toFloat(), 0.05f, 20)
                    TileType.TREE -> shapeRenderer.rect(x-0.45f, y-0.45f, 0.9f, 0.9f)
                }

            }
        }
    }

}

```

Now let's attach the system to the engine in the `FirstScreen.kt` class:

```kotlin
engine.addSystem(TilemapRenderingSystem(shapeRenderer))
```

With that you should be able to see the map in the screen, and the main character Jack too, as you can see in the screenshot below.

![jack](images/02_02_screenshot_final.png)

But we still need to implement the movement of the main character. We will do it now.

## Movement

To be able to control Jack and move around the map, we need to somehow **tag** it with a component that will tell the engine: "Hey this is the player that should move around the map". We will use the `Player` component to do that. We will create it in the `src/main/kotlin/com/sophia/farm/ecs/component/Player.kt` file:

```kotlin
package com.sophia.farm.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import ktx.ashley.optionalPropertyFor
import ktx.ashley.tagFor

class Player : Component {

    companion object {
        val Entity.player by optionalPropertyFor<Player>()
        val Entity.isPlayer by tagFor<Player>()
    }
}

```

Since I think this component will work basically as a tag, I also added a helper method `isPlayer` to the `Entity` class, which will be used to check if the entity is a player.

Now we need a way to know that the input (keyboard, mouse, gamepad) has been used to control the player. For that we will create a new class called `Keyboard` which will respond to the keyboard input and will be used to move the player. We will create it in the `src/main/kotlin/com/sophia/farm/Keyboard.kt` file:

```kotlin
package com.sophia.farm

import com.badlogic.gdx.Input.Keys
import ktx.app.KtxInputAdapter

class Keyboard: KtxInputAdapter {
    val keyUp = Keys.UP
    val keyDown = Keys.DOWN
    val keyLeft = Keys.LEFT
    val keyRight = Keys.RIGHT

    val isUpHeld get() = keyUp in pressedKeys
    val isDownHeld get() = keyDown in pressedKeys
    val isLeftHeld get() = keyLeft in pressedKeys
    val isRightHeld get() = keyRight in pressedKeys

    private val pressedKeys = mutableSetOf<Int>()

    fun clearAllKeys(){
        pressedKeys.clear()
    }

    override fun keyDown(keycode: Int): Boolean {
        pressedKeys += keycode
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        pressedKeys -= keycode
        return true
    }


}
```

To be able to listen to keyboard input events, we need to use the `KtxInputAdapter` which is a LibGDX/KTX class that will handle the input events. There are other equivalent classes for this one (`InputProcessor`, `InputAdapter`) but the `KtxInputAdapter` is the one that is easier to use, since we just need to implement the methods that we want to listen to.

Now let's use the `Keyboard` in our `KeyboardInputSystem` that will map inputs to entity action:

```kotlin
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
```

This system iterates over entities with both `Player` and `Position` components. When it is added to the engine, we make sure that the `Keyboard` is attached as an input processor to listen to any input events from the device.

Then in the `processEntity` method, we check for each input and transform that to a translation of position from the entity, if the new position is walkable on the tilemap.

The way we retrieve whatever entity from the engine that we may need is by using the method `engine.getEntitiesFor()` and inside the parenthesis we define the collections of components (also called Family in Ashley) that we want to get from the engine.So to get the entity that has the `Tilemap` component, I just need to retrieve entities from `allOf(Tilemap::class).get()` family. 

An attentive reader will notice that the `KeyboardInputSystem` is actually dealing with multiple responsibilities. First it checks that the input to move is given, then it makes sure that it does not overlap a tree, and then applies the move. For the next chapter, we will apply some refactoring and split the system into smaller systems that will be responsible for different tasks. But for now this is good.

You can test the game now and see that you can move the player around being obstructed by trees.








