package ch.chrigu.goap.world

import ch.chrigu.goap.animations.Animations
import ch.chrigu.goap.actions.*
import ch.chrigu.goap.entities.Agent
import ch.chrigu.goap.entities.WorldObject
import ch.chrigu.goap.goals.GoapGoal
import ch.chrigu.goap.position.randomPos
import com.badlogic.gdx.math.Vector2

class WorldSetup(private val animations: Animations) {
    fun setupWorld() {
        val availableActions = setOf(PickupWeaponAction(), EatFoodAction(), ConsumeStaminaAction(), HealAction(), RestAction(), AttackEnemyAction(), WanderAction(), FleeAction())
        val goals = setOf(
            GoapGoal("Flee", { agent -> if (agent.targetEnemy != null && agent.health <= 40) 100f else 0f }, { !enemyVisible }),
            GoapGoal("Kill Enemy", { agent -> if (agent.targetEnemy != null) 90f else 0f }, { enemyDead }),
            GoapGoal("Get Weapon", { agent -> if (agent.currentWeapon == null) 50f else 0f }, { hasWeapon }),
            GoapGoal("Stay Healthy", { agent -> (100f - agent.health) / 100f * 80f }, { isHealthy }),
            GoapGoal("Stay Fed", { agent -> (100f - agent.food) / 100f * 60f }, { isFed }),
            GoapGoal("Stay Rested", { agent -> (100f - agent.stamina) / 100f * 40f }, { isRested })
        )
        val player = Agent(initialPosition = Vector2(100f, 100f), isPlayer = true).apply {
            idleAnimation = animations.playerIdleAnimation
            walkAnimation = animations.playerWalkAnimation
            fleeAnimation = animations.playerFleeAnimation
        }
        GameState.agents.add(player)
        repeat(5) {
            val npc = Agent(initialPosition = randomPos()).apply {
                this.availableActions = availableActions
                this.goals = goals
                idleAnimation = animations.npcIdleAnimation
                walkAnimation = animations.npcWalkAnimation
                fleeAnimation = animations.npcFleeAnimation
            }
            GameState.agents.add(npc)
        }
        repeat(10) { GameState.spawn(WorldObject.Food()) }
        repeat(5) { GameState.spawn(WorldObject.Health()) }
        repeat(5) { GameState.spawn(WorldObject.Stamina()) }
        repeat(3) { GameState.spawn(WorldObject.Weapon()) }
    }
}
