package ch.chrigu.goap.actions

import ch.chrigu.goap.entities.WorldObject

class RestAction : CollectObjectAction<WorldObject.Stamina>("Rest", 3f, { !isRested }, { copy(isRested = true) }, WorldObject.Stamina::class.java)
