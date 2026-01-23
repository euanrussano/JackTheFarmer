# 2.1. Creating Jack the Farmer

In this chapter will introduce the concept of Entity-Component-System (ECS) and contrast it with the classifcal Object-Oriented Design using Inheritance.  This design will form the basis for the rest of this tutorial series.

If you have experience with game development, you may be familiar with an object-oriented design (a common approach, even in the original Python libtcod tutorial that inspired this one). While there is nothing inherently wrong with an object-oriented design, it can become cumbersome when you begin to expand your game beyond your initial design ideas.

Let's give a simple example. Suppose we want to create a dog, then since we already envision other types of entities we can start the hierarchy:

```
class BaseEntity
    class Dog
```

So far no problems, but suppose that we can have 2 types of dogs, one that is friendly and the other is wild.  Then we might have:

```
class BaseEntity
    class Dog
        class FriendlyDog
        class WildDog
```

Still not too bad maybe, but then you realize that a wild Dog may or may not bark.  So we might have:

```
class BaseEntity
    class Dog
        class FriendlyDog
        class WildDog
            class BarkingWildDog
            class NotBarkingWildDog
```

I think you already realize how unwieldy this is.  You need the Dog class to describe common behaviour for all dogs (barking, walking, etc), but then you need to describe the behaviour for each type of dog.  It's all just getting out of hand. And is clear that such style of coding is restrictive and it requires defining new classes for every new kind of behavious that you may want to add. So how is component-based design better than that?

The idea is that a base entity can be modified by composing it with different components.  In the case of NotBarkingWildDog, you could have:

```
entity:{
    temperament: wild
    species: dog
    barks: false
}
```

The components are the behaviours of the entity, and they can be added or removed from the entity.  This is much more flexible and allows you to add new behaviours to existing entities. In this way the entire hierarchy of entities can be defined in a single class. And the final result is a much more flexible, data-driven and extensible system.

## Cleaning up the initial code

After you created the initial project skeleon using gdx-liftoff tool, we want to do a bit or reorganization to start the project in a clean state.

Make sure that in the module `core` you have the following files (that should be the initial state of the project):

* `src/main/kotlin/com/sophia/farm/JackTheFarmer.kt`

The class `JackTheFarmer` is the main entry point of the project. We wil start by cleaning up the code. Delete any other class in the file that is not inside `JackTheFarmer` class and just leave the `create()`  method with the addition and setting of the screen `FirstScreen`. The resulting code should look like this:

```kotlin
package com.sophia.farm

import com.sophia.farm.screen.FirstScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync

class JackTheFarmer : KtxGame<KtxScreen>() {
    override fun create() {
        KtxAsync.initiate()

        addScreen(FirstScreen(this))
        setScreen<FirstScreen>()
    }
}
```

This code means that, when the `create` method is called by the main application, it will add the first screen to the game and set it as the current screen. Now let's define the first screen. Create a new package `screen` and add the following file `src/main/kotlin/com/sophia/farm/screen/FirstScreen.kt`:

```kotlin
package com.sophia.farm.screen

import com.sophia.farm.JackTheFarmer
import ktx.app.KtxScreen

class FirstScreen(val game: JackTheFarmer) : KtxScreen {
}

```

Now we have a class that is a screen for the game. It has a reference to the game and we can use it to add entities to the game. For now it is empty so we want to add the our first entity which is main hero, Jack himself. 


## Definiing a position component

We want to have a very simple demo where we have an entity that can move around the screen. For that we need to define components in our ECS. The first component will be the position of the entity. This will be a simple X and Y coordinate on the screen.

```kotlin
package com.sophia.farm.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.optionalPropertyFor

class Position : Component, Pool.Poolable {

    var x = 0
    var y = 0

    override fun reset() {
        x = 0
        y = 0
    }

    companion object {
        val Entity.position by optionalPropertyFor<Position>()
    }
}

```

We are using Ashley framework which already has an entity component system implemented, with some basic classes so we don't need to start from scratch.  

There is a already a lot of code here for a simple position component, so I want to go over it and explain each part to make it clear. For future components, most of the code will be the same, except that we will use different properties.

Let's start with this first line of the class:

```kotlin
class Position : Component, Pool.Poolable
``` 

so this tells us that the name of the class is `Position` and that it implements Ashley's `Component` interface and `Pool.Poolable` interface.

The `Component` interface here is essential for the ECS to work, as Ashley needs to understand that this class is a `Component` and that it can be added to an entity. Now the `Poolable` interface is just a helper interface which assists us to keep compenents that are removed clean, by resetting them to the default values.

This resettings happens when we remove an entity from the Engine (if the entity dies or is no more necessary), then Ashley will call the `Poolable.reset()` method to reset the component to the default values.

Now let's look at the next line of the class:

```kotlin
var x = 0
var y = 0
```

Here is the main point of the `Position` component which is to hold the 2 coordinates of the entity that basically defines for us where this entity is located in the world (and in our screen - more on that later).

Now let's look at the next part of the class:

```kotlin
override fun reset() {
    x = 0
    y = 0
}
```

As already mentioned, this is a helper method to reset the component to the default values, when the entity (and by consequence, the component) is no more necessary.

Now let's look at the next part of the class:

```kotlin
companion object {
    val Entity.position by optionalPropertyFor<Position>()
}
```

This part is a helper code that comes from the KTX framework (Kotlin for LibGDX), and helps us to access the position of the entity simply by typing `entity.position` rather then the annoying explicit Component mapper usage which would be something like:

```kotlin
val positionMapper = mapperFor<Position>()
entity.position = positionMapper[entity]
``` 

## Define a Size component

There is no point in having an entity that has a location in the map but has no physical size (width, height, etc). So let's create a new component that will hold the size of the entity. AS you will see, most of the code repeats with slight changes, the important things are the `width` and `height` properties ( we don't need to worry about the 3D dimension since we are 2D).

```kotlin
package com.sophia.farm.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.optionalPropertyFor

class Size : Component, Pool.Poolable {

    var width = 0
    var height = 0

    override fun reset() {
        width = 0
        height = 0
    }

    companion object {
        val Entity.size by optionalPropertyFor<Size>()
    }
}
```

## Define the Shape for rendering

For rendering our entity in the screen, we will need a component that tells exactly how it should be drawn. To start in a simple way, we just want to show Jack as a simple red circle, positioned according the `Position` component, and size according to the `Size` component. So let's create a new component for this, called `Shape`:

```kotlin
package com.sophia.farm.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Pool
import ktx.ashley.optionalPropertyFor

class Shape : Component, Pool.Poolable {

    var type = ShapeType.CIRCLE
    var color = Color.RED

    override fun reset() {
        type = ShapeType.CIRCLE
        color = Color.RED
    }

    companion object {
        val Entity.shape by optionalPropertyFor<Shape>()
    }

    enum class ShapeType {
        RECTANGLE,
        CIRCLE
    }
}

```

The overall structure of this class is similar to what we have already seen. Now the biggest difference lies in the defintion of an inner class: `ShapeType`. This is a helper class that holds all the possible shapes that can be drawn. For now we have `RECTANGLE` and `CIRCLE`, but we can add more shapes in the future.

## Putting all data to work: creating an entity for Jack

With all the components defined, we are ready to create an entity for Jack. Using LibGDX with KTX makes it very convenient to create the entity and have it directly attached to an engine without having to worry about it later. 

We will create an `Engine` first. The `Engine` is the main class of Ashley that holds all the entities with their components, and the systems that operates on them and makes all the animations, physics, etc running. We can create an instance of the `Engine` directly in the `FirstScreen` like this:

```kotlin
val engine = PooledEngine()
```

First I mentioned `Engine` but why am I creating a `PooledEngine`. According the Ashley documentation, the `PooledEngine` is just a special type of `Engine` that performs better by storing entities and components for reusability, thus avoiding creating new objects when needed.

FOllowing this, we will create an `Entity` for Jack:

```kotlin
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
```

LibGDX with KTX provides a DSL (Domain specific language) structure that helps us to conveniently define new entities using the syntax above. We create the entity with `engine.entity{}` and then just add components to it using the `with<>()` syntax. We can add as many components as we want, and the engine will take care of creating the entity and storing it in the engine.

Now we have the entity created, and we can access its components using the `jack.position`, `jack.size`, and `jack.shape` properties, as defined in the `Position`, `Size`, and `Shape` classes.

## Making things work by creating the Rendering System

With all this structure in place, it is not time to define a System that will gather the entitiy's position, size and shape and actually draw it in the screen. For that we will define a new class called `ShapeRenderingSystem`. This class has a lot of code, but don't worry as we will go over it step by step.

```kotlin
package com.sophia.farm.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.sophia.farm.ecs.component.Position
import com.sophia.farm.ecs.component.Position.Companion.position
import com.sophia.farm.ecs.component.Shape
import com.sophia.farm.ecs.component.Shape.Companion.shape
import com.sophia.farm.ecs.component.Size
import com.sophia.farm.ecs.component.Size.Companion.size
import ktx.ashley.allOf
import kotlin.math.max

class ShapeRenderingSystem: IteratingSystem(
    allOf(
        Position::class,
        Size::class,
        Shape::class
    ).get()
) {
    val shapeRenderer = ShapeRenderer()

    override fun update(deltaTime: Float) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        super.update(deltaTime)
        shapeRenderer.end()
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val position = entity.position!!
        val size = entity.size!!
        val shape = entity.shape!!

        val x = position.x.toFloat()
        val y = position.y.toFloat()

        val width = size.width.toFloat()
        val height = size.height.toFloat()

        shapeRenderer.color = shape.color

        when (shape.type){
            Shape.ShapeType.RECTANGLE -> shapeRenderer.rect(x-width/2, y-height/2, width, height)
            Shape.ShapeType.CIRCLE -> {
                val radius = max(width, height)/2f
                shapeRenderer.circle(x, y, radius)
            }
        }

    }

}
```

Let's start with the first part of the class, in its header:

```kotlin
class ShapeRenderingSystem: IteratingSystem(
    allOf(
        Position::class,
        Size::class,
        Shape::class
    ).get()
)
```

The `ShapeRenderingSystem` inherits from an `IteratingSystem`. This system is a built-in in Ashley which means that the system will iterate over all entities that have a predefined set of components. This set of components is defined in the `allOf()` function, and the `Position`, `Size`, and `Shape` classes are the components that we defined before.

The `allOf()` command is a builder function that tell the system to only iterate over entities that have **ALL** the components defined in the list. There is also the command `oneOf()` which iterates over entities that have **ANY** of the components defined in the list.

To better understand, consider a simple example of a system that iterates over all entities that have the `Position` and `Size` components, and **at least** one of the `Dog` or `Cat`:

```kotlin
class CatOrDogSystem: IteratingSystem(
    allOf(
        Position::class,
        Size::class
    ).oneOf(
        Dog::class,
        Cat::class
    .)get()
)
```

This system iterates over all entities that must have `Position` and `Size` components, and at least one of the `Dog` or `Cat` components. So if the entity is composed by:

```
Position
Size
Dog
```

The system will iterate over it. If the entity is composed by:

```
Position
Size
Cat
```

The system will iterate over it. If the entity is composed by:

```
Position
Size
Dog
Cat
```

The system will iterate over it. If the entity is composed by:

```
Position
Size
```

The system will not iterate over it.

Continuing on our class, we use LibGDX `ShapeRenderer` which is a class that can draw shapes in the screen:

```kotlin
val shapeRenderer = ShapeRenderer()
```

The it use it to process each entity in the `processEntity` function.

Even though normally you wouldn't override the `update` method in an `IteratingSystem`, in this system is better to do it because the `ShapeRenderer` needs to be started (begin) and ended (end) in every frame.

Now inside the `processEntity` method, we retrieve the relevant components of the entity using the helper accessors we defined previously: `position`, `size`, and `shape`:

```kotlin
val position = entity.position!!
val size = entity.size!!
val shape = entity.shape!!
```

We use the `!!` symbol to tell the compiler that we are sure that the components are not null, so we don't need to check if they are null.

Now we have all the information we need to draw the entity in the screen, and we can draw it using the `ShapeRenderer`:

```kotlin
shapeRenderer.color = shape.color

when (shape.type){
    Shape.ShapeType.RECTANGLE -> shapeRenderer.rect(x-width/2, y-height/2, width, height)
    Shape.ShapeType.CIRCLE -> {
        val radius = max(width, height)/2f
        shapeRenderer.circle(x, y, radius)
    }
}
```

To use this system with the engine, we need to add it to the engine using the following code:

```kotlin
engine.addSystem(ShapeRenderingSystem())
```

Lastly, to run the engine (which will call the attached systems to process the entities), we call the `update` method in the `render`method of the `FirstScreen` class.

```kotlin
override fun render(delta: Float) {
    engine.update(delta)
}
```

Now we can see the results in the screen.

![jack](images/02_01_screenshot_section02_01.png)


Well that was a long chapter but we need to provide a really solid base to build upon. The good thing is that you have gone further than my beginners and have a basic understanding of ECS, LibGDX, Kotlin and Ashley. So next chapter we will see how to make the game interactive, by moving Jack around with the keyboard.










