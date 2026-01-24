# 2.4. Field of View (or Jack can't see everything) 

So far, we have already some nice maps: a map of the abandoned farm, and a map of forest. However isn't it strange that Jack can see everything? We want to add a bit more or exploration by implementing a "field of view". Using this, Jack will only be able to see what he has already discovered.

![Field of View](images/02_04_field_of_view.png)

# The algorithm behind the field of view

![Field of View Algorithm](images/02_04_field_of_view_idea.png)

So far, Jack has been walking through the farm and the forest with an almost *super-human vision*: every tile, every tree, every wall is visible from the very beginning. To make exploration more interesting (and a bit more realistic), we introduce the concept of **Field of View (FoV)**.

The idea is simple: at any moment, Jack can only *see* what is close enough **and** not blocked by obstacles. Everything else must be discovered step by step.

---

### How the Field of View Works (Conceptually)

We calculate the field of view in two phases: **range** and **visibility**.

---

### 1. Collecting Points Inside the View Radius

First, we define a **view radius** `R`. This represents how far Jack can see.

If Jack is at position `(px, py)`, we collect all map tiles `(x, y)` such that the distance to Jack is within this radius.

Using simple math, a tile is a *candidate* if:

```
(x - px)² + (y - py)² ≤ R²
```

This gives us a circular area around Jack.
All tiles inside this circle are added to a temporary set called:

```
candidateVisiblePoints
```

At this stage, we are *not* considering walls, trees, or other obstacles yet — only distance.

---

### 2. Ray Casting: Can Jack Actually See This Tile?

Now comes the important part: **line of sight**.



For each point in `candidateVisiblePoints`, we cast a **ray** from Jack’s position `(px, py)` to the target tile `(x, y)`.

You can think of this ray as asking the question:

> “If Jack looks directly at this tile, does something block his view?”

Along this ray, we step through the grid (using a simple line algorithm such as Bresenham) and check each intermediate tile:

* If the ray passes through a **solid tile**
  (wall, tree, rock, building, or any entity marked as blocking vision),
  then the view is obstructed.
* In that case, the target tile is **removed** from the visible set.
* If the ray reaches the target tile without obstruction, the tile is visible.

After this step, we obtain the final set:

```
visiblePoints
```

These are the tiles Jack can currently see *right now*.

---

### 3. Remembering the World: Revealed Points

Seeing something once should leave a memory.

For that, we maintain another set:

```
revealedPoints
```

Every time we compute the field of view:

```
revealedPoints = revealedPoints ∪ visiblePoints
```

This means:

* **Visible tiles**:
  Show the map *and* all entities (trees, animals, items, etc.).
* **Revealed but not visible tiles**:
  Show only the terrain (ground, walls, paths), but **hide entities**.
* **Never revealed tiles**:
  Remain completely hidden.

This creates the classic “fog of war” effect:

* The world feels persistent.
* Exploration feels meaningful.
* Jack never forgets where he has been — he just can’t see everything at once.

---

### Summary

In short, the field of view algorithm follows these steps:

1. Collect all tiles inside a radius using distance math.
2. For each tile, cast a ray from the player to check line of sight.
3. Keep only the tiles whose rays are not blocked.
4. Store all seen tiles forever in `revealedPoints`.

With this system in place, the abandoned farm and the forest finally feel like places to **discover**, not just places to **look at**.

## Field of View component

With the theory out of the way, let’s create the `FieldOfView` component. This component will have a `radius` and two sets of points: `revealedPoints` and `visiblePoints`.
The `radius` is the maximum distance from the player that the field of view will cover. The `visiblePoints` are the tiles that entity can currently see, while the `revealedPoints` are the tiles that have been seen at least once. The difference between them in practice is that revealed tiles will only show map tiles, while visible tiles will also show entities.

```kotlin
package com.sophia.farm.ecs.component

...(imports)

class FieldOfView : Component, Pool.Poolable {

    var radius: Int = 5
    val revealedPoints = mutableListOf<Pair<Int, Int>>()
    val visiblePoints = mutableListOf<Pair<Int, Int>>()

    override fun reset() {
        revealedPoints.clear()
        visiblePoints.clear()
    }

    companion object {
        val Entity.fieldOfView by optionalPropertyFor<FieldOfView>()
    }
}

```

Now make sure you add the `FieldOfView` component to the player entity.


```kotlin
package com.sophia.farm.ecs.factory

... (imports)

object EntityFactory {

    ... (code omitted)

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
            with<FieldOfView>()
        }
    }

}

```

## The Visibility System

With the `FieldOfView` component in place, we can now create the `VisibilitySystem` system. This system will calculate the visible and revealed points for each entity with a `FieldOfView` component. We want to iterate over all entities with a `FieldOfView` component and a `Position` component, so we use the `IteratingSystem` base class.

I didn't add much comments in this class, but its code is backed by the theory above, so feel free to revisit it.

```kotlin
package com.sophia.farm.ecs.system

...(imports)

class VisibilitySystem(
    val useRadius: Boolean = false // make field circular (true) or squared (false)
): IteratingSystem(
    allOf(
        FieldOfView::class,
        Position::class
    ).get()
) {

    val start = vec2()
    val end = vec2()
    val circle = Circle()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val fieldOfView = entity.fieldOfView!!
        val position = entity.position!!
        val x = position.x
        val y = position.y

        // consider the map bounds
        val tilemapEntity = engine.getEntitiesFor(allOf(Tilemap::class).get()).first()
        val tilemap = tilemapEntity.tilemap!!
        val width = tilemap.width
        val height = tilemap.height

        val candidateVisiblePoints = mutableListOf<Pair<Int, Int>>()
        for (dx in -fieldOfView.radius .. fieldOfView.radius){
            for (dy in -fieldOfView.radius .. fieldOfView.radius){
                val px = x + dx
                val py = y + dy
                if (useRadius){
                    val distance = (px - x)*(px - x) + (py - y)*(py - y)
                    if (distance > fieldOfView.radius*fieldOfView.radius) continue
                }
                if (px !in 0 until width || py !in 0 until height) continue
                candidateVisiblePoints.add(px to py)
            }
        }

        // line of sight
        val visiblePoints = mutableListOf<Pair<Int, Int>>()
        visiblePoints.addAll(candidateVisiblePoints)
        val iter = visiblePoints.iterator()
        start.set(x.toFloat(), y.toFloat())
        while(iter.hasNext()){
            val (px, py) = iter.next()
            end.set(px.toFloat(), py.toFloat())
            val isBlocking = candidateVisiblePoints.any {
                val (xi, yi) = it
                if (xi to yi == px to py) return@any false
                if (xi to yi == x to y) return@any false
                if (tilemap.tiles[xi][yi] != TileType.TREE) return@any false
                circle.set(xi.toFloat(), yi.toFloat(), 0.5f)
                return@any Intersector.intersectSegmentCircle(start, end, circle, null)
            }
            if (isBlocking){
                iter.remove()
            }
        }

        fieldOfView.visiblePoints.clear()
        fieldOfView.visiblePoints.addAll(visiblePoints)

    }
}

```

## Adapting the rendering to consider the field of view

There is no real effect of the field of view, if the entity can still see the whole map. So we need to modify the `TilemapRenderingSystem` to only render the tiles that are visible to the player.

Let's go ahead and modify the `TilemapRenderingSystem`. we will need to query the player entity and its `FieldOfView` component, so we can check which tiles are visible to the player.

```kotlin
package com.sophia.farm.ecs.system

...(imports)

class TilemapRenderingSystem ....

    ... (code here ommitted)

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val player = engine.getEntitiesFor(allOf(Player::class, FieldOfView::class).get()).first()
        val fieldOfView = player.fieldOfView!!
        val visiblePoints = fieldOfView.visiblePoints

        val tilemap = entity.tilemap!!

        val tiles = tilemap.tiles
        shapeRenderer.color = Color.GREEN
        for (x in tiles.indices){
            for (y in tiles[0].indices){
                if (x to y !in visiblePoints) continue
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

## Adding memory: The Revealed Tiles

Besides the `visiblePoints` set, we also need to keep track of the tiles that are already revealed to the player. That requires a very minimum change to the `VisibilitySystem`

```kotlin
class VisibilitySystem ...
...

override fun processEntity(entity: Entity, deltaTime: Float) {
    
    ....
    fieldOfView.visiblePoints.clear()
    fieldOfView.visiblePoints.addAll(visiblePoints)

    // Simply add all visible points to the revealed points
    fieldOfView.revealedPoints.addAll(fieldOfView.visiblePoints)
}
```

Now also change the `TilemapRenderingSystem`, so that it also renders the tiles that are already revealed to the player. We make them grayed out, so that it is clear that they are already known to the player, but are outside of the current view radius.

```kotlin
class TilemapRenderingSystem ...

override fun processEntity(entity: Entity, deltaTime: Float) {
    val player = engine.getEntitiesFor(allOf(Player::class, FieldOfView::class).get()).first()
    val fieldOfView = player.fieldOfView!!
    val visiblePoints = fieldOfView.visiblePoints
    val revealedPoints = fieldOfView.revealedPoints

    ...

    for (x in tiles.indices){
            for (y in tiles[0].indices){
                if (x to y !in revealedPoints) continue
                if (x to y in visiblePoints){
                    shapeRenderer.color = Color.GREEN
                } else {
                    shapeRenderer.color = Color.GRAY
                }
                val tile = tiles[x][y]
                when(tile){
                    TileType.GROUND -> shapeRenderer.circle(x.toFloat(), y.toFloat(), 0.05f, 20)
                    TileType.TREE -> shapeRenderer.rect(x-0.45f, y-0.45f, 0.9f, 0.9f)
                }

        }
    }


}
```

![Field of view](images/02_04_screenshot_view.png)


### Viewing other entities only if they are visible

At this point we can already add some life to our world by adding some new entities. For example, let's add a new entity called a **bunny**. Go ahead and create a new `bunny` entity in the `EntityFactory` as follows:

```kotlin
package com.sophia.farm.ecs.factory

... (imports)

object EntityFactory {

   ....(code omitted)

    fun bunny(engine: Engine, x: Int, y: Int): Entity{
        return engine.entity {
            with<Position>{
                this.x = x
                this.y = y
            }
            with<Size>{
                this.width = 1
                this.height = 1
            }
            with<Shape>{
                type = Shape.ShapeType.CIRCLE
                color = Color.YELLOW
            }
        }
    }

}

```

As you can see, using ECS made it super simple to create a new kind of entity just by composing it with different components. 

Where will bunnies spawn? What makes more sense is that the map generator knows which tiles are available for bunnies to spawn in. So let's modify the `GeneratedMap` class, and add a new field called `spawnPoints`. This will be a list of coordinates where entities (besides player) can spawn.

```kotlin
package com.sophia.farm.map

import com.sophia.farm.map.TileType

class GeneratedMap(
    val tiles: Array<Array<TileType>>,
    val playerSpawn: Pair<Int, Int>,
    val spawnPoints: List<Pair<Int, Int>> = listOf()
)

```

Now let's modify the `DungeonMapGenerator` to add the `spawnPoints`.

```kotlin
package com.sophia.farm.map

... (imports)
class DungeonMapGenerator(val random: Random): MapGenerator {
    ... (code omitted)

    override fun generate(width: Int, height: Int): GeneratedMap {
        
        ... (code omitted)

        // place the player in the center of the first room
        val room = rooms.first()
        val playerSpawn = room.center

        val spawnPoints = mutableListOf<Pair<Int, Int>>()
        for (room in rooms.drop(1)){
            spawnPoints.add(room.center)
        }

        return GeneratedMap(tiles, playerSpawn, spawnPoints)
    }


}

```

It is nice to notice that you won't need to change the `RandomMapGenerator` class for now, since the `GeneratedMap` class sets a default value for `spawnPoints`, which is an empty list.

Now let's add the bunnies to the map from the `FirstScreen` class.

```kotlin
package com.sophia.farm.screen

... (imports)

class FirstScreen(val game: JackTheFarmer) : KtxScreen {
    ... (code omitted)

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

        ... (code omitted)
    }

}

```

You should be able to run the project now. You will see that, even though the bunnies are outside the field of view radius, they are being rendered. We need to correct that.

![Field of view](images/02_04_screenshot_visible_entities.png)

To hide bunnies that are outside the field of view, we need to modify the `ShapeRenderingSystem`. Similar to what we did before, we query the player entity and its `FieldOfView` component, so we can check which tiles are visible to the player. If the entity is outside the field of view, we can skip rendering it.

```kotlin
package com.sophia.farm.ecs.system

...(imports)

class ShapeRenderingSystem(
    val shapeRenderer: ShapeRenderer
)... (code here ommitted)

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val player = engine.getEntitiesFor(allOf(Player::class, FieldOfView::class).get()).first()
        val fieldOfView = player.fieldOfView!!
        val visiblePoints = fieldOfView.visiblePoints

        val position = entity.position!!

        if (position.x to position.y !in visiblePoints) return

        ... (code here ommitted)
    }

}

```

Run the project again and you will see that the bunnies are now hidden. Good work!

![Field of view](images/02_04_screenshot_corrected_entities.png)

## Recalculating the field of view only when the player moves

So far the Visibility system is working "perfectly". However we have a problem: the field of view is being recalculated at every frame, even if the player doesn't move. This is a waste of processing power. Normally we don't need to worry very much about that so early, but since this is relatively a heavy operation, it is better to do it only when the player moves.

To be able to update the field of view only when the player moves, we need to take some steps

### The Moved Event component

The first thing we will do is to add an event that will be triggered when the player moves. So exactly after the entity changes its position, we will add to it a component `Moved`. This component does not represent exactly a property of the entity, but it is a signal that the entity has moved. Some call it an event, others call it a flag, or a signal. I prefer to call it an event.

So let's go ahead and create the `Moved` component in a sub-package of `com.sophia.farm.ecs.component` called `event`

```kotlin
package com.sophia.farm.ecs.component.event

... (imports)

class Moved : Component {
    companion object {
        val Entity.hasMoved by tagFor<Moved>()
    }
}
```


### The Spawned Event component

Now we also want to keep track of when the entity is spawned. This is necessary because the update of the field of view needs to be triggered not only when the entity moves, but also when the entity is spawned.

Again this is just an Event-like component, so create it in the same package as the `Moved` component.

```kotlin
package com.sophia.farm.ecs.component.event

... (imports)

class Spawned : Component, Pool.Poolable {
    override fun reset() {
    }
    companion object {
        val Entity.spawned by optionalPropertyFor<Spawned>()
    }
}

```

To make things consistent, go to the `EntityFactory` class and make sure every entity that we create has a `Spawned` component. This component will be removed by a clean up system at the end of an engine cycle.

```kotlin
fun player(engine: Engine, x: Int, y: Int): Entity{
        return engine.entity {
            .... (code omitted)
            with<FieldOfView>()
            with<Spawned>()
        }
    }
```

Do the same as above for the `bunny` entity.

### The WantsToMove component

Now we want to define another component, which will represent the *intent* of the entity to move. This is not the same as the `Moved` component. This component is not an event (something that happened), but it is represents an intent of the entity. We create it because we want to lighten the responsibility of the `KeyboardInputSystem` and make it only responsible for issuing the intent to move. The `MovementSystem` will take care of actually moving the entity.

```kotlin
package com.sophia.farm.ecs.component.intent

... (imports)

class WantsToMove : Component, Pool.Poolable {
    var direction = Direction.RIGHT

    override fun reset() {
        direction = Direction.RIGHT
    }

    companion object {
        val Entity.wantsToMove by optionalPropertyFor<WantsToMove>()
    }
}

```

As you can see we just have a direction that we want to move in. We will use this direction to calculate the next position of the entity. So also define a `Direction` enum class, I created it in the root package of the project:

```kotlin
package com.sophia.farm

enum class Direction(val dx: Int, val dy: Int) {
    UP(0, 1),
    DOWN(0, -1),
    LEFT(-1, 0),
    RIGHT(1, 0)
}

```
Besides the explicit entries (UP, DOWN, etc) we also have the properties `dx` and `dy` that will be used to calculate the next position. This will save us some lines of code later on.


### Refactoring movement to its own system

At this moment, we already have the infrastructure necessary to separate the movement logic from the input logic. So let's go ahead and create a new system called `MovementSystem` in the `com.sophia.farm.ecs.system` package.

```kotlin
package com.sophia.farm.ecs.system

... (imports)

class MovementSystem: IteratingSystem(
    allOf(
        Position::class,
        WantsToMove::class
    ).get()
) {
    val rect1 = Rectangle()
    val rect2 = Rectangle()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val wantsToMove = entity.wantsToMove!!
        val position = entity.position!!
        val size = entity.size!!

        val width = size.width
        val height = size.height

        // calculate potential position before moving for validation
        val newX = position.x + wantsToMove.direction.dx
        val newY = position.y + wantsToMove.direction.dy

        rect1.set(newX-width/2f, newY-height/2f, width.toFloat(), height.toFloat())

        // clean intent
        entity.remove<WantsToMove>()

        // check if position is valid on map
        val tilemapEntity = engine.getEntitiesFor(allOf(Tilemap::class).get()).first()
        val tilemap = tilemapEntity.tilemap!!
        val isWalkable = tilemap.tiles.getOrNull(newX)?.getOrNull(newY) == TileType.GROUND

        // check if does not overlap other entities
        var overlapsOther = false
        for (other in engine.getEntitiesFor(allOf(Position::class, Size::class).get())) {
            if (other == entity) continue

            val otherPosition = other.position!!
            val otherSize = other.size!!

            val otherWidth = otherSize.width
            val otherHeight = otherSize.height
            rect2.set(otherPosition.x-otherWidth/2f, otherPosition.y-otherHeight/2f, otherWidth.toFloat(), otherHeight.toFloat())

            if (rect1.overlaps(rect2)){
                overlapsOther = true
                break
            }
        }
        if (isWalkable && !overlapsOther){
            position.x = newX
            position.y = newY
            engine.configureEntity(entity){
                with<Moved>()
            }
        }
    }
}

```

This class is much longer than our original code in the `KeyboardInputSystem`, but this happens because we are being much more careful here when calculating movement, not only taking into consideration the map tiles, but also the position of other entities in the world.


Since the whole purpose of this class was to clean up the `KeyboardInputSystem`, let's check the code there. 

```kotlin
package com.sophia.farm.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.sophia.farm.Direction
import com.sophia.farm.controller.Action
import com.sophia.farm.controller.Keyboard
import com.sophia.farm.controller.KeyboardPresets
import com.sophia.farm.ecs.component.Player
import com.sophia.farm.ecs.component.Position
import com.sophia.farm.ecs.component.intent.WantsToMove
import ktx.ashley.allOf
import ktx.ashley.configureEntity
import ktx.ashley.with

class KeyboardInputSystem: IteratingSystem(
    allOf(
        Player::class,
        Position::class
    ).get()
){

    val keyboard = Keyboard(KeyboardPresets.arrowsAndWasd())

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        Gdx.input.inputProcessor = keyboard
    }

    override fun removedFromEngine(engine: Engine) {
        super.removedFromEngine(engine)
        Gdx.input.inputProcessor = keyboard
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        var direction: Direction? = null

        if (keyboard.isHeld(Action.MOVE_UP)){
            direction = Direction.UP
        } else if (keyboard.isHeld(Action.MOVE_DOWN)){
            direction = Direction.DOWN
        } else if (keyboard.isHeld(Action.MOVE_LEFT)){
            direction = Direction.LEFT
        } else if (keyboard.isHeld(Action.MOVE_RIGHT)){
            direction = Direction.RIGHT
        }

        if (direction != null){
            engine.configureEntity(entity){
                with<WantsToMove>{
                    this.direction = direction
                }
            }
        }

        keyboard.clearAllKeys()
    }

}

```

As you can see, the `KeyboardInputSystem` is not much shorter than the original, but now its responsability is much more clear and isolated. It just queries for inputs and creates intents (for now, just the `WantsToMove` intent) to be processed by the other systems in the engine.

### Update the Visibility System to update only when necessary

Now that we have the `Moved` and `Spawned` evens, we can update the `VisibilitySystem` to update only when the player moved or spawned. 

```kotlin
package com.sophia.farm.ecs.system

... (imports)

class VisibilitySystem(
    val useRadius: Boolean = false // make field circular (true) or squared (false)
): IteratingSystem(
    allOf(
        FieldOfView::class,
        Position::class
    ).oneOf(
        Moved::class,
        Spawned::class
    ).get()
) {

    val start = vec2()
    val end = vec2()
    val circle = Circle()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        if (entity.isPlayer){
            Gdx.app.log("VisibilitySystem", "Updating player field of view")
        }
        val fieldOfView = entity.fieldOfView!!
        val position = entity.position!!
        val x = position.x
        val y = position.y

        // consider the map bounds
        val tilemapEntity = engine.getEntitiesFor(allOf(Tilemap::class).get()).first()
        val tilemap = tilemapEntity.tilemap!!
        val width = tilemap.width
        val height = tilemap.height

        val candidateVisiblePoints = mutableListOf<Pair<Int, Int>>()
        for (dx in -fieldOfView.radius .. fieldOfView.radius){
            for (dy in -fieldOfView.radius .. fieldOfView.radius){
                val px = x + dx
                val py = y + dy
                if (useRadius){
                    val distance = (px - x)*(px - x) + (py - y)*(py - y)
                    if (distance > fieldOfView.radius*fieldOfView.radius) continue
                }
                if (px !in 0 until width || py !in 0 until height) continue
                candidateVisiblePoints.add(px to py)
            }
        }

        // line of sight
        val visiblePoints = mutableListOf<Pair<Int, Int>>()
        visiblePoints.addAll(candidateVisiblePoints)
        val iter = visiblePoints.iterator()
        start.set(x.toFloat(), y.toFloat())
        while(iter.hasNext()){
            val (px, py) = iter.next()
            end.set(px.toFloat(), py.toFloat())
            val isBlocking = candidateVisiblePoints.any {
                val (xi, yi) = it
                if (xi to yi == px to py) return@any false
                if (xi to yi == x to y) return@any false
                if (tilemap.tiles[xi][yi] != TileType.TREE) return@any false
                circle.set(xi.toFloat(), yi.toFloat(), 0.5f)
                return@any Intersector.intersectSegmentCircle(start, end, circle, null)
            }
            if (isBlocking){
                iter.remove()
            }
        }

        fieldOfView.visiblePoints.clear()
        fieldOfView.visiblePoints.addAll(visiblePoints)

        fieldOfView.revealedPoints.addAll(fieldOfView.visiblePoints)

    }
}

```

The main changes were:

* The `VisibilitySystem` now only queries entities that has been moved or spawned, thus avoiding unnecessary updates.
* The `useRadius` flag is now passed to the `VisibilitySystem` so we can make it flexible if we want to make the field of view circular or squared.

### Add a system to clear events triggered by the systems

As a last step for this chapter, we need to clean up events emitted along the engine cycle of update by the systems. One possible approach, which may not be the best one but works for now, is to add a system to clear events triggered by the systems. 

For that, let's create a new `ClearEventsSystem` that will clear all events emitted by the systems.

```kotlin
package com.sophia.farm.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.sophia.farm.ecs.component.event.Moved
import com.sophia.farm.ecs.component.event.Spawned
import ktx.ashley.allOf
import ktx.ashley.oneOf
import ktx.ashley.remove

class ClearEventsSystem: IteratingSystem(
    oneOf(
        Moved::class,
        Spawned::class
    ).get()
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity.remove<Moved>()
        entity.remove<Spawned>()
    }
}

```
