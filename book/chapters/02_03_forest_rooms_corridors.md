# Making Forest Clearings

In this, we will make a new map where Jack can explore. It will be a forest with some clearings. A clearing is basically a "room". These "rooms" are connected through narrow corridors, creating a kind of dungeon-style map. This will give us a good start to add forest animals later on that can move around and interact with Jack.

## Cleaning up

Before developing new code, it is always recommended to check the current state of the codebase, and see if it is necessary to do some cleaning by:
- Removing unused imports
- Removing unused variables
- Removing unused functions

Besides removing unused stuff, the cleaning also involves the process of *refactoring*. Refactoring means that you take a block of code and extract it to a function, or to a class. In this way, you can make the code more readable and maintainable. Otherwise they will be a big mess.

We don't have much of unused code to remove, but we do have a few blocks of code that need refactoring.

One thing that we can extract is the process of map generation. Since we already envision a few different types of maps, we can create a new interface called `MapGenerator` that will allow us to generate different maps. And by using an interface we allow other classes (called client classes) to use a concrete `MapGenerator` implementation without knowing the implementation details. This makes our code really flexible, extensible and powerful.

Create a new package `map` and add the following file `src/main/kotlin/com/sophia/farm/map/MapGenerator.kt`:

```kotlin
package com.sophia.farm.map

import com.sophia.farm.TileType

interface MapGenerator {
    fun generate(width: Int, height: Int): Array<Array<TileType>>
}
```

The interface as you can see is very simple. It has a single method called `generate` which takes the width and height of the map and returns an array of `TileType` which will be used to create the map.

Now let's create a new class called `RandomMapGenerator` in its own file `src/main/kotlin/com/sophia/farm/map/RandomMapGenerator.kt`. Then we can move all the code that was previously written in the `FirstScreen` to create the map into this class:

```kotlin
package com.sophia.farm.map

import com.sophia.farm.TileType
import kotlin.random.Random
import kotlin.random.nextInt

class RandomMapGenerator(val random: Random): MapGenerator {
    override fun generate(width: Int, height: Int): Array<Array<TileType>> {
        val tiles = Array(width){ Array(height){ TileType.GROUND } }

        // surround with trees
        for (x in 0 until width){
            tiles[x][0] =  TileType.TREE
            tiles[x][height-1] =  TileType.TREE
        }

        for (y in 0 until height){
            tiles[0][y] =  TileType.TREE
            tiles[height-1][y] =  TileType.TREE
        }

        // throw a bunch of trees
        for (i in 0 until (width*height*0.3f).toInt()){
            val x = random.nextInt(width)
            val y = random.nextInt(height)
            tiles[x][y] = TileType.TREE
        }

        return tiles
    }

}

```

I took the freedom to make some changes in the structure of the code, so that it is easier to read. It becomes a bit bigger in length but it is easier to understand, which is normally a good thing. We just want to optimize the code and sacrifice readability if it is really necessary.

Another part that we can pull out of the FirstScreen is the creation of entities. We can move that code to a new class called `EntityFactory` in its own file `src/main/kotlin/com/sophia/farm/ecs/factory/EntityFactory.kt`:

```kotlin
package com.sophia.farm.ecs.factory

...(imports)

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

```

Then the FirstScreen will look like this:

```kotlin
package com.sophia.farm.screen

...(imports)

class FirstScreen(val game: JackTheFarmer) : KtxScreen {
    val random = Random(1)
    val shapeRenderer = ShapeRenderer()
    val viewport = ExtendViewport(20f, 20f)
    val engine = PooledEngine()
    val mapWidth = 20
    val mapHeight = 20

    override fun show() {
        val tiles = RandomMapGenerator(random).generate(mapWidth, mapHeight)

        val tilemap = EntityFactory.tilemap(engine, tiles)

        val jack = EntityFactory.player(engine, 5,5)

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
```

## Making the Forest Clearing Map

We will work now on developing an instance of `MapGenerator` that generates a map for the forest clearing. The map will be very much similar to a dungeon map, with corridors and rooms. Each room is a forest clearing, and the corridors are used to connect the rooms/ clearings.


### Creating rectangular rooms

Start by defining the `Room` class. The `Room` class will have the following properties:
- `x`: the x coordinate of the top-left corner of the room
- `y`: the y coordinate of the top-left corner of the room
- `width`: the width of the room
- `height`: the height of the room

We also add a helper method `applyToMap` that will apply the room to the 2D array of tiles.

```kotlin
private class Room(val x: Int, val y: Int, val width: Int, val height: Int){
    val center = x + width/2 to y + height/2
    fun applyToMap(tiles: Array<Array<TileType>>){
        for (dx in 0 .. width){
            for (dy in 0 .. height){
                tiles[x+dx][y+dy] = TileType.GROUND
            }
        }
    }
}
```

### Making the corridors

Similar to the `Room` class, we define a `Corridor` class to create the connections between the rooms. In practice, we could even reuse the `Room` class to make the corridors. However, for the sake of good practice and readability, we will create a new class for the corridors. The `Corridor` class will have the following properties:
- `startX`: the x coordinate of the start of the corridor
- `startY`: the y coordinate of the start of the corridor
- `endX`: the x coordinate of the end of the corridor
- `endY`: the y coordinate of the end of the corridor

We also add a helper method `applyToMap` that will apply the corridor to the 2D array of tiles.

```kotlin
private class Corridor(val startX: Int, val startY: Int, val endX: Int, val endY: Int){
        fun applyToMap(tiles: Array<Array<TileType>>){
            if (startX == endX) {
                // vertical corridor
                for (y in min(startY, endY)..max(startY, endY)) {
                    tiles[startX][y] = TileType.GROUND
                }
            } else {
                // horizontal corridor
                for (x in min(startX, endX)..max(startX, endX)) {
                    tiles[x][startY] = TileType.GROUND
                }
            }
        }
    }
```

### A Simple Forest With 4 Clearings

Now that we have the `Room` and `Corridor` classes, we can define a simple map generator that generates a forest with 4 clearings. Let's create a new package map, put the `Room` and `Corridor` classes and add the following file `src/main/kotlin/com/sophia/farm/map/DungeonMapGenerator.kt`:


```kotlin
package com.sophia.farm.map

... (imports)

class DungeonMapGenerator(val random: Random): MapGenerator {
    
    override fun generate(width: Int, height: Int): Array<Array<TileType>> {
        val tiles = Array(width){ Array(height){ TileType.TREE } }

        val rooms = listOf(
            Room(2, 1, 3, 3),
            Room(13, 1, 3,3),
            Room(3, 11, 3, 3),
            Room(11, 14, 3,3)
        )

        for (room in rooms){
            room.applyToMap(tiles)
        }

        return tiles
    }
}
```

If you run the code now, you will see the map with 4 clearings or rooms. They are all disconnected and the player is placed in the middle of the trees, which makes no sense. We will correct that next.


#### Joining the Clearings

Now that we have 4 clearings, we need to connect them together. We will do that by creating corridors between the rooms. First, remove this piece of code:

```kotlin
for (room in rooms){
            room.applyToMap(tiles)
}
```

Then Let's add the following code to the `DungeonMapGenerator` class:


```kotlin
    fun generate(width: Int, height: Int): Array<Array<TileType>> {
        ...
        val rooms  = ...

        for ((i, room) in rooms.withIndex()) {
            room.applyToMap(tiles)
            var prevRoom= rooms.last()
            if (i > 0){
                prevRoom = rooms[i-1]
            }
            val (x1, y1) = room.center
            val (x2, y2) = prevRoom.center

            if (random.nextBoolean()) {
                // vertical then horizontal
                Corridor(x1, y1, x1, y2).applyToMap(tiles)
                Corridor(x1, y2, x2, y2).applyToMap(tiles)
            } else {
                // horizontal then vertical
                Corridor(x1, y1, x2, y1).applyToMap(tiles)
                Corridor(x2, y1, x2, y2).applyToMap(tiles)
            }
        }

        return tiles
    }
```

In this code, we go through each room and create a corridor between it and the previous room. We also connect the last room to the first room. To make things nicer, we use `random.nextBoolean()` to decide which direction to connect the corridors. This way, the corridors are L shaped and in different directions. Notice that, in theory, the corridors are connected to the centers of the rooms.

If you run the code now, you will see a nice set of corridors and rooms, with all rooms properly connected to another. But we still have the issue of the player placement which we need to fix next.

#### Placing the player

Placing the player randomly in the map is a very bad decision, since it may spawn right above a tree, wall or roof. To fix this, we will create a new class `GeneratedMap` that will hold the map and the player spawn location. This will be the new output of the `generate` method in `MapGenerator` class.

```kotlin
class GeneratedMap(
    val tiles: Array<Array<TileType>>,
    val playerSpawn: Pair<Int, Int>
)
```

Now, we will change the `generate` method in the `MapGenerator` class to return the `GeneratedMap` instead of the 2D array of tiles.

```kotlin
interface MapGenerator {
    fun generate(width: Int, height: Int): GeneratedMap
}
```

Then, we will add the following code to the `RandomMapGenerator` class:

```kotlin
class RandomMapGenerator
...
// throw a bunch of trees
        for (i in 0 until (width*height*0.3f).toInt()){
            val x = random.nextInt(width)
            val y = random.nextInt(height)
            tiles[x][y] = TileType.TREE
        }

        // find all valid spots to place the player
        val validSpots = mutableListOf<Pair<Int, Int>>()
        for (x in tiles.indices){
            for (y in tiles[x].indices){
                val tile = tiles[x][y]
                if (tile == TileType.GROUND){
                    validSpots.add(x to y)
                }
            }
        }

        return GeneratedMap(tiles, validSpots.random(random))
```

If this, we "filter" all the valid tiles in the map and choose one of them randomly. 

Then, we will add the following code to the `DungeonMapGenerator` class:

```kotlin
class DungeonMapGenerator
...

override fun generate 
// rooms, corridors, etc

// place the player in the center of the first room
        val room = rooms.first()
        val playerSpawn = room.center

        return GeneratedMap(tiles, playerSpawn)
}

```

For this map, the player will always be spawned in the center of the first room. Remember, these rules may change in the future but we want to build towards a robust architecture and meaningful code.

If you run the code now, you will see the map with the player spawned in the center of the first room. That's it for the map generation for now. So let's move to improve a bit the player input.

#### Making it possible to move with WASD or arrow keys

So far the player can only move up, down, left and right. We hardcoded the key bindings in the `Keyboard` class. But we want to make it possible to move with WASD or arrow keys. There are different ways to achieve this. Some approaches makes things even more messy. But we will follow one to try to keep the code clean. Let's start by creating a `Action` enum class, that will represent the different actions that the player can perform, and that are bound to specific keys:

```kotlin
package com.sophia.farm.controller

enum class Action {
    MOVE_UP,
    MOVE_DOWN,
    MOVE_LEFT,
    MOVE_RIGHT
}
```

Then, we will create a `KeyboardPresets` class that will hold the key bindings for each action. With this presets, we don't need to manually write the key bindings in the `Keyboard` class. But still keep the flexibility that we will add to it.

```kotlin
package com.sophia.farm.controller

import com.badlogic.gdx.Input.Keys

object KeyboardPresets {

    fun arrowsAndWasd(): Map<Action, Set<Int>> = mapOf(
        Action.MOVE_UP to setOf(Keys.UP, Keys.W),
        Action.MOVE_DOWN to setOf(Keys.DOWN, Keys.S),
        Action.MOVE_LEFT to setOf(Keys.LEFT, Keys.A),
        Action.MOVE_RIGHT to setOf(Keys.RIGHT, Keys.D)
    )
}
```

Now we can modify the Keyboard class as follows.

```kotlin
package com.sophia.farm.controller

import com.badlogic.gdx.Input
import ktx.app.KtxInputAdapter

class Keyboard(
    private val bindings: Map<Action, Set<Int>>
): KtxInputAdapter {

    private val pressedKeys = mutableSetOf<Int>()

    fun isHeld(action: Action): Boolean =
        pressedKeys.any { it in (bindings[action] ?: emptySet()) }

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
We moved all the keybindings to a very flexible `bindings` map. This way, we can add new keybindings to the `Keyboard` class without modifying the code.

Now modify how the `Keyboard` is used in the `KeyboardInputSystem`. It will use the KeyboardPresets to get the key bindings for each action.

```kotlin
package com.sophia.farm.ecs.system

... (imports)

class KeyboardInputSystem

    .... 

    private val keyboard = Keyboard(KeyboardPresets.arrowsAndWasd())

    ....

    override fun processEntity(entity: Entity, deltaTime: Float) {
        
        ....(code continues)

        if (keyboard.isHeld(Action.MOVE_UP)){
            newY++
        } else if (keyboard.isHeld(Action.MOVE_DOWN)){
            newY--
        } else if (keyboard.isHeld(Action.MOVE_LEFT)){
            newX--
        } else if (keyboard.isHeld(Action.MOVE_RIGHT)){
            newX++
        }

        ...(code continues)
    }

}

```

You can run now the project and you will see as the image below. Try moving the player with WASD or arrow keys. Both should work fine, and the trees should obstruct the player movement.

![jack](images/02_03_screenshot_final.png)

