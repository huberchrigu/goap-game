package ch.chrigu.goap.actions

import ch.chrigu.goap.entities.Agent

class FleeAction : MoveToAction("Flee", 1f, { enemyVisible }, { copy(enemyVisible = false) }) { // We hope to achieve this

    override fun validate(agent: Agent): Boolean {
        val enemyWeapon = agent.targetEnemy?.currentWeapon
        return agent.health <= 40 && enemyWeapon != null && enemyWeapon.ammo > 0
    }

    override fun perform(agent: Agent): Boolean {
        val enemy = agent.targetEnemy ?: return true // Enemy is gone, we're safe
        agent.isFleeing = true

        if (target == null) {
            // Calculate a position away from the enemy
            val fleeVector = agent.position.cpy().sub(enemy.position).nor()
            target = agent.position.cpy().add(fleeVector.scl(200f)) // Flee 200 units away
        }

        // If we ran far enough away that the enemy is no longer visible, the action is a success.
        if (agent.position.dst(enemy.position) > 250f) {
            agent.isFleeing = false
            agent.targetEnemy = null
            return true
        }

        return super.perform(agent)
    }
}
