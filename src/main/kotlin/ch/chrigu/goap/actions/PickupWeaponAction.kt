package ch.chrigu.goap.actions

import ch.chrigu.goap.entities.WorldObject

class PickupWeaponAction : CollectObjectAction<WorldObject.Weapon>("PickupWeapon", 1f, { !hasWeapon }, { copy(hasWeapon = true) }, WorldObject.Weapon::class.java)
