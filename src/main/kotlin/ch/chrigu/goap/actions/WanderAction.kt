package ch.chrigu.goap.actions

import ch.chrigu.goap.entities.Agent
import ch.chrigu.goap.position.randomPos

// A fallback action when no other plan can be made.
class WanderAction : MoveToAction("Wander", 10f, { true }, { this }) {
    override fun perform(agent: Agent): Boolean {
        if (target == null) {
            target = randomPos()
        }
        return super.perform(agent)
    }
}
