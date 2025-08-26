package ch.chrigu.goap.actions

import ch.chrigu.goap.entities.Agent
import com.badlogic.gdx.Gdx

class AttackEnemyAction : MoveToAction(
    "AttackEnemy", 1f, { hasWeapon && enemyVisible }, { copy(enemyDead = true) }
) {  // Simplification: we assume the attack will eventually kill the enemy

    private var shootTimer = 0f

    override fun validate(agent: Agent): Boolean {
        return agent.targetEnemy != null && agent.currentWeapon != null && agent.currentWeapon!!.ammo > 0 && agent.health > 40
    }

    override fun perform(agent: Agent): Boolean {
        val enemy = agent.targetEnemy ?: return true // Enemy is gone
        if (enemy.isDead) return true

        target = enemy.position
        val distance = agent.position.dst(enemy.position)

        if (distance > 150f) { // Move closer if too far
            return super.perform(agent)
        } else { // In range, stop moving and shoot
            agent.targetPosition = null
            shootTimer += Gdx.graphics.deltaTime
            if (shootTimer > 1f) { // Shoot every 1 second
                shootTimer = 0f
                agent.shoot(enemy.position)
                if (agent.currentWeapon == null || agent.currentWeapon!!.ammo <= 0) {
                    return true // Out of ammo, action failed/finished
                }
            }
            return false // Still attacking
        }
    }
}
