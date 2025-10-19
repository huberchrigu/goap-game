package ch.chrigu.goap.goals

import ch.chrigu.goap.world.WorldState
import ch.chrigu.goap.entities.Agent

/**
 * Represents a goal that an agent might want to achieve.
 * @property name A descriptive name for the goal (e.g., "Survive", "GetWeapon").
 * @property priority A function that calculates how important this goal is right now.
 * @property satisfies The world state the agent wants to achieve to satisfy this goal.
 */
data class GoapGoal(
    val name: String,
    val priority: (Agent) -> Float,
    val satisfies: WorldState.() -> Boolean
)