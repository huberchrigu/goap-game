package ch.chrigu.goap.actions

import ch.chrigu.goap.entities.WorldObject

class HealAction : CollectObjectAction<WorldObject.Health>("Heal", 2f, { !isHealthy }, { copy(isHealthy = true) }, WorldObject.Health::class.java)
