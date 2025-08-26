package ch.chrigu.goap.actions

import ch.chrigu.goap.entities.WorldObject

class EatFoodAction : CollectObjectAction<WorldObject.Food>("EatFood", 2f, { !isFed }, { copy(isFed = true) }, WorldObject.Food::class.java)
