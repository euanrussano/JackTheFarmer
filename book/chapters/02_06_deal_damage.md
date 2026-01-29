# 2.6. Being bitten by a rat and scaring them away (basic interaction)

In this section we implement the first *real interaction* in the game:
**an aggressive animal damaging the player on contact**.

To achieve this, we will:

* model aggression as a trait
* introduce health
* separate collision detection from behavior
* trigger attacks as *intent*, not as a side effect of collision

Let’s go step by step, with all the code in one place.

---

## 1. Aggressive component

Aggression is a **trait**, not behavior.
The component answers only one question:

> “Is this animal allowed to attack?”

```kotlin
package com.sophia.farm.ecs.component.animal

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.optionalPropertyFor
import com.badlogic.ashley.core.Entity

class Aggressive : Component, Pool.Poolable {

    override fun reset() {
        // no state to reset
    }

    companion object {
        val Entity.aggressive by optionalPropertyFor<Aggressive>()
    }
}
```

---

## 2. Health component

Health is generic and reusable.
Anything that can take damage can have `Health`.

```kotlin
package com.sophia.farm.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.optionalPropertyFor
import com.badlogic.ashley.core.Entity

class Health : Component, Pool.Poolable {

    var health = 10
    var maxHealth = 10

    override fun reset() {
        health = 10
        maxHealth = 10
    }

    companion object {
        val Entity.health by optionalPropertyFor<Health>()
    }
}
```

---

## 3. Improving Name for debugging

Overriding `toString()` makes logs readable and meaningful.

```kotlin
package com.sophia.farm.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.optionalPropertyFor
import com.badlogic.ashley.core.Entity

class Name : Component, Pool.Poolable {

    var text = "<Unnamed>"

    override fun reset() {
        text = "<Unnamed>"
    }

    override fun toString(): String = text

    companion object {
        val Entity.name by optionalPropertyFor<Name>()
    }
}
```

---

## 4. Creating a Rat in `EntityFactory`

A rat is simply an animal with **Curious + Aggressive** traits.

```kotlin
fun rat(engine: Engine, x: Int, y: Int): Entity {
    return animal(
        engine,
        name = "Rat",
        x = x,
        y = y,
        shapeType = Shape.ShapeType.CIRCLE,
        color = Color.LIGHT_GRAY,
        isCurious = true,
        isAggressive = true
    )
}
```

---

## 5. Generic animal factory

Traits are attached compositionally.

```kotlin
fun animal(
    engine: Engine,
    name: String,
    x: Int,
    y: Int,
    shapeType: Shape.ShapeType,
    color: Color,
    isCurious: Boolean = false,
    isTimid: Boolean = false,
    isAggressive: Boolean = false
): Entity {
    return engine.entity {
        with<Name> { text = name }
        with<Position> {
            this.x = x
            this.y = y
        }
        with<Size> {
            width = 1
            height = 1
        }
        with<Shape> {
            type = shapeType
            this.color = color
        }
        with<FieldOfView>()
        with<Animal>()
        if (isCurious) with<Curious>()
        if (isTimid) with<Timid>()
        if (isAggressive) with<Aggressive>()
        with<Spawned>()
    }
}
```

---

## 6. Adding health to the player

The player is just another entity with health.

```kotlin
fun player(engine: Engine, x: Int, y: Int): Entity {
    return engine.entity {
        with<Name> { text = "Jack" }
        with<Position> {
            this.x = x
            this.y = y
        }
        with<Size> {
            width = 1
            height = 1
        }
        with<Shape> {
            type = Shape.ShapeType.CIRCLE
            color = Color.RED
        }
        with<Player>()
        with<FieldOfView>()
        with<Health>()
        with<Spawned>()
    }
}
```

---

## 7. Collided event component

Collision is a **fact**, not a decision.

```kotlin
package com.sophia.farm.ecs.component.event

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.optionalPropertyFor

class Collided : Component, Pool.Poolable {

    var other: Entity? = null

    override fun reset() {
        other = null
    }

    companion object {
        val Entity.collided by optionalPropertyFor<Collided>()
    }
}
```

---

## 8. CollisionSystem (fact-only detection)

This system **detects** collisions and emits `Collided`.
It does **not** decide what collisions mean.

```kotlin
class CollisionSystem : IteratingSystem(
    allOf(WantsToMove::class, Position::class, Size::class).get()
) {
    private val rect1 = Rectangle()
    private val rect2 = Rectangle()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val move = entity.wantsToMove!!
        val pos = entity.position!!
        val size = entity.size!!

        val newX = pos.x + move.direction.dx
        val newY = pos.y + move.direction.dy

        rect1.set(
            newX - size.width / 2f,
            newY - size.height / 2f,
            size.width.toFloat(),
            size.height.toFloat()
        )

        for (other in engine.getEntitiesFor(allOf(Position::class, Size::class).get())) {
            if (other == entity) continue

            val op = other.position!!
            val os = other.size!!

            rect2.set(
                op.x - os.width / 2f,
                op.y - os.height / 2f,
                os.width.toFloat(),
                os.height.toFloat()
            )

            if (rect1.overlaps(rect2)) {
                entity.remove<WantsToMove>()
                engine.configureEntity(entity) {
                    with<Collided> { this.other = other }
                }
                return
            }
        }
    }
}
```

---

## 9. Clean MovementSystem

Movement only applies valid moves and emits `Moved`.

```kotlin
class MovementSystem : IteratingSystem(
    allOf(Position::class, WantsToMove::class).get()
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val move = entity.wantsToMove!!
        val pos = entity.position!!

        val newX = pos.x + move.direction.dx
        val newY = pos.y + move.direction.dy

        entity.remove<WantsToMove>()

        val tilemap = engine
            .getEntitiesFor(allOf(Tilemap::class).get())
            .first()
            .tilemap!!

        if (tilemap.tiles.getOrNull(newX)?.getOrNull(newY) == TileType.GROUND) {
            pos.x = newX
            pos.y = newY
            engine.configureEntity(entity) { with<Moved>() }
        }
    }
}
```

---

## 10. AggressiveBehaviourSystem

This system **interprets collisions** and applies damage.

```kotlin
class AggressiveBehaviourSystem : IteratingSystem(
    allOf(Animal::class, Aggressive::class, Collided::class).get()
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val other = entity.collided!!.other ?: return
        val health = other.health ?: return

        val damage = 1
        health.health -= damage

        println("${entity.name} attacks ${other.name} for $damage damage")
    }
}
```

---

## 11. Wiring everything together

System order reflects the gameplay pipeline:

```kotlin
engine.addSystem(KeyboardInputSystem())
engine.addSystem(AnimalSteeringSystem())
engine.addSystem(CollisionSystem())
engine.addSystem(AggressiveBehaviourSystem())
engine.addSystem(MovementSystem())
engine.addSystem(VisibilitySystem())
engine.addSystem(TilemapRenderingSystem(shapeRenderer))
engine.addSystem(ShapeRenderingSystem(shapeRenderer))
engine.addSystem(ClearEventsSystem())
```

---

## Result

Running the game and walking into a rat now produces:

```
Rat attacks Jack for 1 damage
Rat attacks Jack for 1 damage
```

This confirms the full interaction loop:

* perception → movement intent
* collision detection → factual event
* behavior interpretation → attack
* consequence → health reduction

At this point, we have **true gameplay interaction**, built in a clean ECS-friendly way that will scale naturally in the next sections.


