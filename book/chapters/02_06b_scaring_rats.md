# 2.6b. Scaring animals away (basic interaction, continued)

In the previous section, we introduced **aggressive animals** that can damage the player on contact.
However, Jack is not completely helpless.

In this section, we add a simple but expressive interaction:

> **When Jack bumps into an aggressive animal, he can scare it away.**

This interaction introduces:

* player-specific abilities
* accumulating effects over time
* behavior changes triggered by repeated interactions

All without breaking ECS principles.

---

## Design idea

The behavior we want is:

* Each collision between Jack and an aggressive animal increases a **scaring factor**
* Each collision adds **+0.5**
* When the animal is scared enough (`scaringFactor ≥ 1.0`), it runs away
* Scaring should only affect **aggressive animals**
* Non-aggressive animals should *not* be scared by Jack

This keeps interactions intentional and avoids accidental side effects.

---

## 1. Player capability as a trait

First, we model the ability to scare animals as a **player capability**, not as a hard-coded rule.

### `CanScareAggressiveAnimal` component

```kotlin
package com.sophia.farm.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.optionalPropertyFor
import com.badlogic.ashley.core.Entity

class CanScareAggressiveAnimal : Component, Pool.Poolable {

    override fun reset() {
        // no state
    }

    companion object {
        val Entity.canScareAggressiveAnimal by optionalPropertyFor<CanScareAggressiveAnimal>()
    }
}
```

This component simply answers the question:

> “Is this entity allowed to scare aggressive animals?”

---

## 2. Add the trait to the player

In `EntityFactory`, we attach this capability to Jack.

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
        with<CanScareAggressiveAnimal>()
        with<Spawned>()
    }
}
```

By modeling this as a component, we keep the door open for:

* other characters that can scare animals
* upgrades or abilities later on

---

## 3. Modeling fear explicitly: `Scared` component

To track how frightened an animal is, we introduce a new component.

```kotlin
package com.sophia.farm.ecs.component.animal

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.optionalPropertyFor
import com.badlogic.ashley.core.Entity

class Scared : Component, Pool.Poolable {

    var factor = 0f

    override fun reset() {
        factor = 0f
    }

    companion object {
        val Entity.scared by optionalPropertyFor<Scared>()
    }
}
```

This component:

* stores accumulated fear
* can be reused later for fleeing, freezing, or panic behaviors

---

## 4. ScareAggressiveAnimalAwaySystem

Now we implement the system that **interprets collisions** and applies fear.

### Responsibilities of this system

* React only to collisions involving an entity that can scare
* Affect only aggressive animals
* Accumulate fear
* Remove the animal once it is scared enough

---

### Implementation

```kotlin
package com.sophia.farm.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.sophia.farm.ecs.component.CanScareAggressiveAnimal
import com.sophia.farm.ecs.component.Name.Companion.name
import com.sophia.farm.ecs.component.animal.Aggressive
import com.sophia.farm.ecs.component.animal.Scared
import com.sophia.farm.ecs.component.animal.Scared.Companion.scared
import com.sophia.farm.ecs.component.animal.Animal.Companion.isAnimal
import com.sophia.farm.ecs.component.animal.Aggressive.Companion.isAggressive
import com.sophia.farm.ecs.component.event.Collided
import com.sophia.farm.ecs.component.event.Collided.Companion.collided
import ktx.ashley.allOf
import ktx.ashley.configureEntity

class ScareAggressiveAnimalAwaySystem : IteratingSystem(
    allOf(
        Collided::class,
        CanScareAggressiveAnimal::class
    ).get()
) {

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val other = entity.collided!!.other ?: return

        // Only animals
        if (!other.isAnimal) return

        // Only aggressive animals
        if (!other.isAggressive) return

        val scared = other.scared
        if (scared != null) {
            scared.factor += 0.5f
        } else {
            engine.configureEntity(other) {
                with<Scared> { factor = 0.5f }
            }
        }

        val factor = other.scared!!.factor
        println("${entity.name} tries to scare a ${other.name}. scaring: $factor/1.0")

        if (factor >= 1f) {
            println("${entity.name} scared a ${other.name} away!")
            engine.removeEntity(other)
        }
    }
}
```

---

## 5. Wiring the system

The order matters, because we want:

1. collisions detected
2. scaring applied
3. aggression handled

So in `WorldBuilder`:

```kotlin
engine.addSystem(CollisionSystem())
engine.addSystem(ScareAggressiveAnimalAwaySystem())
engine.addSystem(AggressiveBehaviourSystem())
```

This ensures:

* scaring can happen before another attack
* behavior remains deterministic

---

## Result

Running the game and bumping into a rat now produces:

```
Jack tries to scare a rat. scaring: 0.5/1.0
Jack scared a rat away!
```

This confirms that:

* scaring accumulates across collisions
* only aggressive animals are affected
* animals are removed once sufficiently scared

---

## Why this design works

This interaction remains clean because:

* collision systems still report **facts**
* behavior systems interpret meaning
* traits define *capabilities*, not logic
* fear is explicit, inspectable state

Most importantly, nothing here is hard-coded to:

* Jack
* rats
* combat

Everything is data-driven.

---

## What this enables next

With this foundation, we can easily add:

* timid animals fleeing instead of disappearing
* cooldowns on scaring
* animals becoming aggressive *after* being scared
* non-lethal crowd-control mechanics

But for now, we already have a **complete interaction loop**:
movement → collision → behavior → consequence.

This is exactly the kind of system that scales well as the game grows.
