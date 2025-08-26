package ch.chrigu.goap

/**
 * Represents the state of the world or the conditions of an action.
 * It's a map of state names (e.g., "hasWeapon") to their boolean values.
 */
data class WorldState(
    val hasWeapon: Boolean,
    val isHealthy: Boolean,
    val isRested: Boolean,
    val isFed: Boolean,
    val enemyVisible: Boolean,
    val enemyDead: Boolean
)

typealias Preconditions = WorldState.() -> Boolean
typealias Effects = WorldState.() -> WorldState
