package ch.chrigu.goap

import ch.chrigu.goap.actions.GoapAction
import ch.chrigu.goap.entities.Agent
import java.util.*

class GoapPlanner {

    /**
     * A node in the planning graph. It represents a state and the action that led to it.
     */
    private class Node(val parent: Node?, val cost: Float, val state: WorldState, val action: GoapAction?)

    /**
     * Creates a plan of actions to satisfy a goal.
     *
     * @param agent The agent requesting the plan.
     * @param availableActions The set of all possible actions the agent can perform.
     * @param worldState The current state of the world from the agent's perspective.
     * @param goal The goal to be achieved.
     * @return A queue of actions representing the plan, or null if no plan was found.
     */
    fun plan(agent: Agent, availableActions: Set<GoapAction>, worldState: WorldState, goal: GoapGoal): Queue<GoapAction>? {
        // Reset all actions so they can be used in planning
        availableActions.forEach { it.reset() }

        // Filter for actions that are valid in the current context
        val usableActions = availableActions.filter { it.validate(agent) }.toSet()

        val openList = PriorityQueue<Node>(compareBy { it.cost })
        val closedList = mutableSetOf<WorldState>()

        val startNode = Node(null, 0f, worldState, null)
        openList.add(startNode)

        while (openList.isNotEmpty()) {
            val currentNode = openList.poll()

            if (isGoalSatisfied(goal, currentNode.state)) {
                return reconstructPlan(currentNode)
            }

            closedList.add(currentNode.state)

            for (action in usableActions) {
                if (arePreconditionsMet(action.preconditions, currentNode.state)) {
                    val newState = applyEffects(currentNode.state, action.effects)
                    if (newState in closedList) continue

                    val newCost = currentNode.cost + action.cost
                    val newNode = Node(currentNode, newCost, newState, action)
                    openList.add(newNode)
                }
            }
        }

        return null // No plan found
    }

    private fun isGoalSatisfied(goal: GoapGoal, state: WorldState): Boolean {
        return goal.satisfies(state)
    }

    private fun arePreconditionsMet(preconditions: Preconditions, state: WorldState): Boolean {
        return preconditions(state)
    }

    private fun applyEffects(currentState: WorldState, effects: Effects): WorldState {
        return effects(currentState)
    }

    private fun reconstructPlan(goalNode: Node): Queue<GoapAction> {
        val plan = LinkedList<GoapAction>()
        var currentNode: Node? = goalNode
        while (currentNode?.action != null) {
            plan.addFirst(currentNode.action)
            currentNode = currentNode.parent
        }
        return plan
    }
}
