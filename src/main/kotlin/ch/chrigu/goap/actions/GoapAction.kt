package ch.chrigu.goap.actions

import ch.chrigu.goap.world.Effects
import ch.chrigu.goap.world.Preconditions
import ch.chrigu.goap.entities.Agent

/**
 * The base class for any action an agent can perform.
 * Actions are the building blocks of a plan.
 *
 * @property name A descriptive name for the action (e.g., "PickupWeapon").
 * @property cost The cost of performing this action. The planner will try to find the cheapest plan.
 * @property preconditions The state of the world required *before* this action can be performed.
 * @property effects The changes to the world state that will happen *after* this action is completed.
 */
abstract class GoapAction(val name: String, val cost: Float, val preconditions: Preconditions, val effects: Effects) {
    /**
     * Checks if the action is still valid and can be performed.
     */
    open fun validate(agent: Agent): Boolean = true

    /**
     * The main logic of the action. This is called when the action is executed.
     * @return true if the action is complete, false if it's still in progress.
     */
    abstract fun perform(agent: Agent): Boolean

    /**
     * Resets any internal state of the action, so it can be used again.
     */
    open fun reset() {}
}
