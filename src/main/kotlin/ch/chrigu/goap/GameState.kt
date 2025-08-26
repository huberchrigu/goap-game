package ch.chrigu.goap

import ch.chrigu.goap.entities.Agent
import ch.chrigu.goap.entities.Projectile
import ch.chrigu.goap.entities.WorldObject
import com.badlogic.gdx.math.Vector2

/**
 * Singleton object to hold the entire game state.
 */
object GameState {
    val agents = mutableListOf<Agent>()
    val worldObjects = mutableListOf<WorldObject>()
    val projectiles = mutableListOf<Projectile>()

    inline fun <reified T : WorldObject> findNearestObjectOfType(position: Vector2): T? {
        return findNearestObjectOfType(position, T::class.java)
    }

    fun <T : WorldObject> findNearestObjectOfType(position: Vector2, clazz: Class<T>): T? {
        return worldObjects
            .filterIsInstance(clazz)
            .minByOrNull { it.position.dst(position) }
    }

    fun spawn(vararg worldObject: WorldObject?) {
        worldObject.filterNotNull().forEach { worldObjects.add(it) }
    }
}
