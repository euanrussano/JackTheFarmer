# 2.5. Wild animals everywhere!

## Short refactoring before starting

- Move the engine setup and world building to a new class `WorldBuilder`

```kotlin
package com.sophia.farm.ecs.factory

... (imports)

class WorldBuilder(
    private val random: Random,
    private val shapeRenderer: ShapeRenderer
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
            EntityFactory.bunny(engine, x, y)
        }
    }

    private fun installSystems(engine: PooledEngine) {
        engine.addSystem(KeyboardInputSystem())
        engine.addSystem(MovementSystem())
        engine.addSystem(VisibilitySystem())
        engine.addSystem(TilemapRenderingSystem(shapeRenderer))
        engine.addSystem(ShapeRenderingSystem(shapeRenderer))
        engine.addSystem(ClearEventsSystem())
    }
}


```

Then the `FirstScreen` class will look like this

```kotlin
package com.sophia.farm.screen

... (imports)

class FirstScreen(val game: JackTheFarmer) : KtxScreen {
    ... (code omitted)
    val worldBuilder = WorldBuilder(random, shapeRenderer)

    override fun show() {
        worldBuilder.build(engine)
    }
    ... (code omitted)
}

```

## Making different animals

- We already have `bunny`, let's move it to a more generic `animal`

```kotlin
class EntityFactory 
... (code omitted)
fun animal(engine: Engine, x: Int, y: Int, shapeType: Shape.ShapeType, color: Color): Entity{
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
                this.type = shapeType
                this.color = color
            }
            with<Spawned>()
        }
    }
```

Now `bunny` becomes:

```kotlin
class EntityFactory 
... (code omitted)
fun bunny(engine: Engine, x: Int, y: Int): Entity{
        return animal(engine, x, y, Shape.ShapeType.CIRCLE, Color.YELLOW)
    }
```

We can now create a `fox`:

```kotlin
class EntityFactory 
... (code omitted)
fun fox(engine: Engine, x: Int, y: Int): Entity{
        return animal(engine, x, y, Shape.ShapeType.CIRCLE, Color.BROWN)
    }
```

Let's also create a function `randomAnimal` that will create a random animal in the `EntityFactory`:

```kotlin
fun randomAnimal(engine: Engine, x: Int, y: Int, random: Random): Entity{
        if (random.nextBoolean()){
            return bunny(engine, x, y)
        } else {
            return fox(engine, x, y)
        }
    }
```

To use this function, we will call the `randomAnimal` function in the the `WorldBuilder`, instead of bunny:

```kotlin
class WorldBuilder(
    private val random: Random,
    private val shapeRenderer: ShapeRenderer
) {
    .... (code omitted)
    fun buildWorld(engine: PooledEngine) {
        ... (code omitted)
        val spawnPoints = generatedMap.spawnPoints.toMutableList()
        repeat(5) {
            if (spawnPoints.isEmpty()) return
            val (x, y) = spawnPoints.removeAt(random.nextInt(spawnPoints.size))
            EntityFactory.randomAnimal(engine, x, y, random)
        }
    }
        
}
```
You can now run the project and notice that we have mixed animals, both `bunny` and `fox` in the world.

Let's properly tag every animal in the ECS world, so we can filter them later. For that, create a component `Animal`. We will also create two "animal behaviors": `Curious` and `Timid`. I want to place them in a subpackage `com.sophia.farm.ecs.component.animal` so we can easily distinguish them.

```kotlin
package com.sophia.farm.ecs.component.animal

... (imports)

class Animal : Component, Pool.Poolable {
    
    override fun reset() {
    }

    companion object {
        val Entity.isAnimal by tagFor<Animal>()
    }
}
```

```kotlin
package com.sophia.farm.ecs.component.animal

... (imports)

class Timid : Component, Pool.Poolable {

    override fun reset() {
    }

    companion object {
        val Entity.isTimid by tagFor<Timid>()
    }
}

```

```kotlin
package com.sophia.farm.ecs.component.animal

... (imports)

class Curious : Component, Pool.Poolable {

    override fun reset() {
    }

    companion object {
        val Entity.isCurious by tagFor<Curious>()
    }
}

```

At this point, it is also interesting to have names for the entities, so we can better identify them with logs and debugging. So let's create a component `Name`:

```kotlin
package com.sophia.farm.ecs.component

... (imports)

class Name : Component, Pool.Poolable {

    var text = "<Unnamed>"
    override fun reset() {
        text = "<Unnamed>"
    }

    companion object {
        val Entity.name by optionalPropertyFor<Name>()
    }
}

```

Notice that I created `Name` in the `com.sophia.farm.ecs.component` package. We shouldn't put this in the `animal` subpackage, since we can have other entities that are not animals with names. Such as Jack.

With that in place, we can make the `bunny` and `fox` be `Animal, Curious`  and `Animal, Timid`, respectively. We just need to make a bit of adjustments to the proper functions in the  `EntityFactory`:

```kotlin
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
            with<Animal>()
            if (isCurious) with<Curious>()
            if (isTimid) with<Timid>()
            with<Spawned>()
        }
    }
```

Notice that we make any `animal`  by default not curious (isCurious = false) and not timid (isTimid = false). Then,add the `Timid` or `Curious` depending on the animal.

Also add the `Name` component in the `player` function in `EntityFactory`. Then we name the hero as "Jack". Or whatever name you want.

## Making them move

Now that we have the `Timid` and `Curious` animals, we can make them move.

You may have already noticed that we want different kinds of behavior for the `Timid` and `Curious` animals. I think it would be interesting to have some animals (`Curious`) that move in the direction of the player (they are curious to know who is this guy), and some that move in opposite direction (`Timid`), in order to avoid the player. Also this attraction/repelling effect should not be applied between animals, but only to other entities that are not animals.

The first thing to do is to create a system `TimidAnimalSystem` that will tell the movement intent of the `Timid` animals. As already mentioned, we will see if there is any other entity in the field of view which is not an animal, and if so, we will move in the opposite direction of that entity.


## Timid Animals: Deciding Where to Move

Timid animals behave in a very simple way:

> **If they see something that is not an animal, they try to move away from it.**

To do this, we need to transform *what the animal sees* into a *movement intent*. This can be done with a bit of very simple vector math.

---

## 1. What information do we already have?

For each timid animal, we assume the following data is available:

* Its **current position**

  ```
  A = (ax, ay)
  ```

* Its **field of view**, which gives us a set of visible tiles or entities:

  ```
  visibleEntities = { E1, E2, E3, ... }
  ```

* Each visible entity has a position:

  ```
  Ei = (ex, ey)
  ```

We also assume we can identify whether a visible entity is an animal or not.

---

## 2. Selecting the threatening entities

Timid animals only care about **non-animal entities** (for example, the player).

So the first step is filtering:

> From all visible entities, keep only those that are **not animals**.

If no such entity exists, the timid animal does nothing this turn.

---

## 3. Direction *towards* a threat

For each threatening entity, we compute a **direction vector** from the animal to that entity.

For one threat at position `(ex, ey)`:

```
directionToThreat = (ex - ax, ey - ay)
```

This vector points **towards** the danger.

---

## 4. Direction *away* from a threat

Since the animal is timid, it wants to move in the **opposite direction**.

So we simply invert the vector:

```
escapeVector = (ax - ex, ay - ey)
```

This vector points **away** from the threat.

---

## 5. Multiple threats: combining directions

Often, more than one threatening entity may be visible.

We handle this by **adding all escape vectors together**:

```
escapeSum = Σ (ax - ex, ay - ey)
```

Intuitively:

* Each threat “pushes” the animal away from itself
* The final direction is the sum of all these pushes

This is similar to basic steering behaviors used in many games.

---

## 6. Normalizing the escape direction

The resulting vector may be long or short depending on distances and number of threats.

We only care about the **direction**, not the magnitude.

So we normalize it:

```
length = sqrt(dx² + dy²)

normalizedEscape = (dx / length, dy / length)
```

If the length is zero (perfect symmetry), the animal does not move.

---

## 7. Converting direction into grid movement

Because the game uses a **grid**, we convert the continuous direction into a discrete move.

For example:

* If `|dx| > |dy|`, move horizontally
* Otherwise, move vertically

And choose the sign:

```
dx > 0 → move right
dx < 0 → move left
dy > 0 → move up
dy < 0 → move down
```

This gives a movement intent such as:

* `(1, 0)`
* `(-1, 0)`
* `(0, 1)`
* `(0, -1)`

---

## 8. Emitting the movement intent

Finally, the system does **not move the animal directly**.

Instead, it emits an intent:

```
WantsToMove(dx, dy)
```

This keeps responsibilities clean:

* `TimidAnimalSystem` decides *what the animal wants*
* `MovementSystem` decides *whether the move is possible*

---

## Summary

The timid animal algorithm can be summarized as:

1. Look at all visible entities
2. Keep only non-animal entities
3. For each one, compute a vector pointing away
4. Sum all escape vectors
5. Normalize the result
6. Convert it to a grid direction
7. Emit a movement intent

This approach is:

* simple
* deterministic
* easy to extend later (fear radius, panic, freezing, etc.)

And most importantly, it makes timid animals feel **naturally reactive**, without any complex AI logic.


## Final code
Add a system `TimidAnimalSystem` that will model intent of `Animal, Timid` entities. The code is:

```kotlin
package com.sophia.farm.ecs.system

... (imports)

class TimidAnimalSystem: IntervalIteratingSystem(
    allOf(
        Animal::class,
        Timid::class,
        FieldOfView::class,
        Position::class
    ).get(),
    1f
) {
    override fun processEntity(entity: Entity) {
        val position = entity.position!!
        val fieldOfView = entity.fieldOfView!!

        val ax = position.x.toFloat()
        val ay = position.y.toFloat()

        val notAnimalEntities = engine.getEntitiesFor(allOf(Position::class).exclude(Animal::class).get())
        val notAnimalEntitiesInFieldOfView = notAnimalEntities.filter {
            val otherPosition = it.position!!
            otherPosition.x to otherPosition.y in fieldOfView.visiblePoints
        }
        val espaceVector= vec2()
        for (otherEntity in notAnimalEntitiesInFieldOfView){
            val otherPosition = otherEntity.position!!
            val ex = otherPosition.x.toFloat()
            val ey = otherPosition.y.toFloat()
            espaceVector.add(ax - ex, ay - ey)
        }
        espaceVector.nor()

        if (espaceVector.len() == 0f) return

        val dx = espaceVector.x
        val dy = espaceVector.y
        var direction: Direction
        if (abs(dx) > abs(dy)){
            if (dx > 0){
                direction = Direction.RIGHT
            } else {
                direction = Direction.LEFT
            }
        } else {
            if (dy > 0){
                direction = Direction.UP
            } else {
                direction = Direction.DOWN
            }
        }

        engine.configureEntity(entity){
            with<WantsToMove>{
                this.direction = direction
            }
        }

    }

}

```

Now add this system to the engine in `WorldBuilder`:

```kotlin
    engine.addSystem(KeyboardInputSystem())
    engine.addSystem(TimidAnimalSystem())
    engine.addSystem(MovementSystem())
    engine.addSystem(VisibilitySystem())
    ... (other systems)
        
```

Notice that the order of the systems here is very important. Since this is a system that creates an intent, we need to make sure that it is executed **before** the `MovementSystem` or other systems that update the simulation.

Now if you run the game, you will see the timid animals moving away from the player when you try to get close to them.

Now let's move on to the next step. To implement the `Curious` behavior, you may notice that everything is basically the same, except that the `Timid` animals move in the opposite direction of the `Curious` ones.

Because of that, we would have 2 options:
- Create a new system and copy the code from the `TimidAnimalSystem` to the `CuriousAnimalSystem`
- Refactor the `TimidAnimalSystem` to make it a `AnimalSteeringSystem` that will move a `Animal` that can be either `Timid` or `Curious`

Sometimes there is not a clear better option, but in this case it is clear that the best option is to refactor the `TimidAnimalSystem` to be a `AnimalSteeringSystem`. This is because we never want to repeat code, because it makes it very hard to mantain.

```kotlin
package com.sophia.farm.ecs.system

... (imports)

class AnimalSteeringSystem: IntervalIteratingSystem(
    allOf(
        Animal::class,
        FieldOfView::class,
        Position::class
    ).oneOf(
        Timid::class,
        Curious::class
    ).get(),
    1f
) {

    val tempVec = vec2()
    val escapeVec = vec2()
    override fun processEntity(entity: Entity) {
        val position = entity.position!!
        val fieldOfView = entity.fieldOfView!!

        val ax = position.x.toFloat()
        val ay = position.y.toFloat()

        val notAnimalEntities = engine.getEntitiesFor(allOf(Position::class).exclude(Animal::class).get())
        val notAnimalEntitiesInFieldOfView = notAnimalEntities.filter {
            val otherPosition = it.position!!
            otherPosition.x to otherPosition.y in fieldOfView.visiblePoints
        }
        escapeVec.setZero()
        for (otherEntity in notAnimalEntitiesInFieldOfView){
            val otherPosition = otherEntity.position!!
            val ex = otherPosition.x.toFloat()
            val ey = otherPosition.y.toFloat()
            if (entity.isCurious) {
                escapeVec.add(ex - ax, ey - ay)
            } else if (entity.isTimid) {
                escapeVec.add(ax - ex, ay - ey)
            }
        }
        escapeVec.nor()

        if (escapeVec.len() == 0f) return

        val dx = escapeVec.x
        val dy = escapeVec.y
        var direction: Direction
        if (abs(dx) > abs(dy)){
            if (dx > 0){
                direction = Direction.RIGHT
            } else {
                direction = Direction.LEFT
            }
        } else {
            if (dy > 0){
                direction = Direction.UP
            } else {
                direction = Direction.DOWN
            }
        }

        engine.configureEntity(entity){
            with<WantsToMove>{
                this.direction = direction
            }
        }



    }

}

```

Now replace the `TimidAnimalSystem` with the `AnimalSteeringSystem` in the `WorldBuilder`:

```kotlin
    engine.addSystem(KeyboardInputSystem())
    engine.addSystem(AnimalSteeringSystem())
    engine.addSystem(MovementSystem())
    engine.addSystem(VisibilitySystem())
    ... (other systems)
        
```

And if you run the project now, you will see that the `Curious` and `Timid` animals move, but the first in the direction of the player, and the second in the opposite direction of the player. This is a very simple system, but it is already very useful in a game. And it makes things very dynamic.


