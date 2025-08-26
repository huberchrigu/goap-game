package ch.chrigu.goap.entities

import ch.chrigu.goap.position.randomPos
import ch.chrigu.goap.textures.GameTexture
import com.badlogic.gdx.math.Vector2

/**
 * Represents any object in the game world that isn't an agent.
 */
sealed class WorldObject(val position: Vector2, val texture: GameTexture) {
    class Food(position: Vector2 = randomPos(), val value: Float = 100f) : WorldObject(position, GameTexture.FOOD)
    class Health(position: Vector2 = randomPos(), val value: Float = 100f) : WorldObject(position, GameTexture.HEALTH)
    class Stamina(position: Vector2 = randomPos(), val value: Float = 100f) : WorldObject(position, GameTexture.STAMINA)
    class Weapon(position: Vector2 = randomPos(), var ammo: Int = 10) : WorldObject(position, GameTexture.WEAPON)
}
