package ch.chrigu.goap.entities

import com.badlogic.gdx.math.Vector2
import java.util.UUID

/**
 * Represents a projectile fired by an agent.
 */
data class Projectile(
    val ownerId: UUID,
    val position: Vector2,
    val velocity: Vector2,
    var lifeTime: Float = 2f // seconds
)
