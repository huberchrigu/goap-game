package ch.chrigu.goap.actions

import ch.chrigu.goap.entities.WorldObject

class ConsumeStaminaAction : CollectObjectAction<WorldObject.Stamina>("ConsumeStamina", 2f, { !isRested }, { copy(isRested = true) }, WorldObject.Stamina::class.java)