package ch.chrigu.goap.actions

import ch.chrigu.goap.entities.Agent

// A fallback action when no other plan can be made.
class WanderAction : MoveToAction("Wander", 10f, { true }, { this }) {
    override fun perform(agent: Agent): Boolean {
        if (target == null) {
            target = agent.targetPosition
        }
        if (target == null) return true // No wander position
        return super.perform(agent)
    }
}
