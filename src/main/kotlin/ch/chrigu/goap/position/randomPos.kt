package ch.chrigu.goap.position

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import kotlin.random.Random

fun randomPos() = Vector2(
    Random.Default.nextFloat() * (Gdx.graphics.width - 50) + 25,
    Random.Default.nextFloat() * (Gdx.graphics.height - 50) + 25
)
