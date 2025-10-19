package ch.chrigu.goap.actions

import ch.chrigu.goap.world.Effects
import ch.chrigu.goap.world.GameState
import ch.chrigu.goap.world.Preconditions
import ch.chrigu.goap.entities.Agent
import ch.chrigu.goap.entities.WorldObject

abstract class CollectObjectAction<T : WorldObject>(name: String, cost: Float, preconditions: Preconditions, effects: Effects, private val clazz: Class<T>) :
    MoveToAction(name, cost, preconditions, effects) {
    private var worldObject: T? = null

    override fun validate(agent: Agent) = GameState.findNearestObjectOfType(agent.position, clazz)
        ?.also { worldObject = it }
        ?.let { true } ?: false

    override fun perform(agent: Agent): Boolean {
        if (worldObject == null) return true
        if (!GameState.worldObjects.contains(worldObject!!)) return true
        if (target == null) target = worldObject!!.position

        if (super.perform(agent)) {
            worldObject = null
            return true
        }
        return false
    }
}
