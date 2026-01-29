package com.sophia.farm.ecs.system.rendering

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.badlogic.gdx.scenes.scene2d.ui.Value
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.Viewport
import com.sophia.farm.ecs.component.Diary.Companion.diary
import com.sophia.farm.ecs.component.Health.Companion.health
import com.sophia.farm.ecs.component.Name
import com.sophia.farm.ecs.component.Name.Companion.name
import com.sophia.farm.ecs.component.Player
import com.sophia.farm.ecs.component.Position
import com.sophia.farm.ecs.component.Position.Companion.position
import com.sophia.farm.ecs.component.event.ClickedOnWorld.Companion.clickedOnWorld
import ktx.actors.txt
import ktx.ashley.allOf
import ktx.math.vec2
import ktx.scene2d.actors
import ktx.scene2d.label
import ktx.scene2d.progressBar
import ktx.scene2d.scene2d
import ktx.scene2d.table
import kotlin.let
import kotlin.math.min

class HUDSystem(
    val worldViewport: Viewport
): IteratingSystem(
    allOf(
        Player::class
    ).get()
) {
    val playerHealthBar: ProgressBar
    val diaryLabels: List<Label>
    val stage = Stage().apply {
        actors {
            table {
                setFillParent(true)
                defaults().pad(10f)
                table {
                    it.grow()
                }
                row()
                // bottom panel
                table {
                    it.growX().minHeight(Value.percentHeight(0.25f, parent))
                    defaults().pad(5f)
                    background = skin.getDrawable("window")
                    table {
                        it.growX()
                        this.defaults().align(Align.left).growX()
                        label("Health: ")
                        playerHealthBar = progressBar(0f, 1f, 0.1f) {
                            it.minWidth(Value.percentWidth(0.5f, parent))
                            it.minHeight(Value.percentHeight(0.1f, parent))
                        }
                    }
                    row()
                    table {
                        it.growX()
                        this.defaults().align(Align.left).growX()
                        diaryLabels = buildList {
                            for (i in 0 until 5){
                                add(label(" "))
                                row()
                            }
                        }

                    }
                }
            }
        }
    }

    val infoLabel = scene2d.label("").apply {
        this@HUDSystem.stage.addActor(this)
        this.isVisible = false
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)

        stage.viewport.apply()
        stage.act()
        stage.draw()
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val health = entity.health!!
        val diary = entity.diary!!

        playerHealthBar.value = health.health.toFloat() / health.maxHealth

        val n = min(diaryLabels.size, diary.messages.size)
        diaryLabels.forEach {
            it.txt = " "
        }
        // show last messages more on top
        val reversedMessages = diary.messages.reversed()
        for (i in 0 until n){
            diaryLabels[i].txt = reversedMessages[i]
        }

        entity.clickedOnWorld?.let  { clicked ->
            diary.messages.add("Clicked on world at (${clicked.x}, ${clicked.y})")

            engine.getEntitiesFor(allOf(Position::class, Name::class).get()).firstOrNull {
                it.position!!.x == clicked.x && it.position!!.y == clicked.y
            }?.let {
                infoLabel.txt = it.name?.text?: ""
                infoLabel.isVisible = true
                infoLabel.color.a = 1f
                infoLabel.actions.clear()
                infoLabel.addAction(Actions.sequence(Actions.delay(1f),Actions.fadeOut(1f)))

                val coords = vec2()
                worldViewport.project(coords.set(clicked.x.toFloat(), clicked.y.toFloat()))
                coords.y = Gdx.graphics.height - coords.y
                stage.viewport.unproject(coords)
                infoLabel.setPosition(coords.x, coords.y)
            }
        }
    }
}
