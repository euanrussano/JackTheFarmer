package com.sophia.farm.ecs.system.rendering

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.Camera
import com.sophia.farm.ecs.component.CameraFollow
import com.sophia.farm.ecs.component.Position
import com.sophia.farm.ecs.component.Position.Companion.position
import ktx.ashley.allOf
import ktx.graphics.lerpTo
import ktx.math.vec2

class CameraFollowSystem(
    private val camera: Camera
): IteratingSystem(
    allOf(
        CameraFollow::class,
        Position::class
    ).get()
) {
    val target= vec2()
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val position = entity.position!!

        target.set(position.x.toFloat(), position.y.toFloat())

        camera.lerpTo(target, 0.1f)
        camera.update()
    }
}
