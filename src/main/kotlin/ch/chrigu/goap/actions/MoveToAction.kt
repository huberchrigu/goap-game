package ch.chrigu.goap.actions

import ch.chrigu.goap.world.Effects
import ch.chrigu.goap.world.Preconditions
import ch.chrigu.goap.entities.Agent
import com.badlogic.gdx.math.Vector2

// A simple action for moving towards a target
abstract class MoveToAction(name: String, cost: Float, preconditions: Preconditions, effects: Effects) : GoapAction(name, cost, preconditions, effects) {
    protected var target: Vector2? = null

    override fun perform(agent: Agent): Boolean {
        if (target == null) return true // No target, so action is "done"

        val direction = target!!.cpy().sub(agent.position).nor()
        agent.moveTo(direction)

        // Arrived at destination
        if (agent.position.dst(target!!) < 5f) {
            target = null
            return true
        }
        return false
    }

    override fun reset() {
        target = null
    }
}
