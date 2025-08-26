package ch.chrigu.goap.entities

import ch.chrigu.goap.GameState
import ch.chrigu.goap.GoapGoal
import ch.chrigu.goap.GoapPlanner
import ch.chrigu.goap.WorldState
import ch.chrigu.goap.actions.GoapAction
import ch.chrigu.goap.actions.WanderAction
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import java.util.*
import kotlin.math.min

/**
 * Represents a character in the game, either NPC or Player.
 */
class Agent(
    val id: UUID = UUID.randomUUID(),
    initialPosition: Vector2,
    val isPlayer: Boolean = false
) {
    private val size = 32f
    private val radius = size / 2

    // --- Agent Stats ---
    var position = initialPosition
        private set
    var health = 100f
        private set
    var stamina = 100f
        private set
    var food = 100f
        private set
    var currentWeapon: WorldObject.Weapon? = null
        private set
    var targetEnemy: Agent? = null
    var targetPosition: Vector2? = null

    // --- GOAP State ---
    var currentPlan: Queue<GoapAction> = LinkedList()
    var currentAction: GoapAction? = null
    val goapPlanner = GoapPlanner()
    var availableActions: Set<GoapAction> = emptySet()
    var goals: Set<GoapGoal> = emptySet()

    // --- Animation & Visual State ---
    lateinit var walkAnimation: Animation<TextureRegion>
    lateinit var idleAnimation: Animation<TextureRegion>
    lateinit var fleeAnimation: Animation<TextureRegion>
    private var animationStateTime = 0f
    private var isMoving = false
    var isFleeing = false // Public so FleeAction can set it
    private var isHurt = false
    private var facingRight = true
    private val lastPosition = initialPosition.cpy()

    val isDead: Boolean get() = health <= 0

    private val baseSpeed = 75f
    val speed: Float
        get() = when {
            isFleeing -> baseSpeed * 1.2f // Speed boost when fleeing
            stamina < 20f -> baseSpeed * 0.5f
            else -> baseSpeed
        }

    fun collides(worldObject: WorldObject) = collides(worldObject.position)
    fun collides(position: Vector2) = !isDead && this.position.dst(position) < radius
    fun collides(projectile: Projectile) = id != projectile.ownerId && collides(projectile.position)

    fun update(delta: Float, gameState: GameState) {
        if (isDead) return

        // --- Update Animation & Visual State ---
        animationStateTime += delta
        val dx = position.x - lastPosition.x
        isMoving = (dx != 0f || (position.y - lastPosition.y) != 0f)
        if (dx > 0) facingRight = true
        if (dx < 0) facingRight = false
        lastPosition.set(position)
        isHurt = health <= 40

        // --- Apply world effects ---
        food -= 2f * delta
        if (food < 0) food = 0f

        if (food <= 0) {
            takeDamage(2f * delta)
            stamina -= 5f * delta
        }
        if (stamina < 0) stamina = 0f

        if (isPlayer) return // Player is controlled manually

        // --- GOAP Logic for NPCs ---
        if (currentAction?.perform(this) != false) { // if action is done or null
            currentAction = null
            isFleeing = false // Ensure fleeing stops if action is done
            if (currentPlan.isNotEmpty()) {
                currentAction = currentPlan.poll()
            } else {
                findNewPlan(gameState)
            }
        }
    }

    fun moveTo(direction: Vector2) {
        position.mulAdd(direction, speed * Gdx.graphics.deltaTime)
        if (position.x < radius) position.x = radius
        if (position.y < radius) position.y = radius
        if (position.x > Gdx.graphics.width - radius) position.x = Gdx.graphics.width - radius
        if (position.y > Gdx.graphics.height - radius) position.y = Gdx.graphics.height - radius
    }

    fun createWorldState(gameState: GameState): WorldState {
        targetEnemy = gameState.agents.firstOrNull { !it.isDead && it.id != this.id && it.position.dst(this.position) < 200f }

        return WorldState(
            hasWeapon = (currentWeapon != null && currentWeapon!!.ammo > 0),
            isHealthy = (health > 80f),
            isRested = (stamina > 80f),
            isFed = (food > 80f),
            enemyVisible = (targetEnemy != null),
            enemyDead = (targetEnemy?.isDead ?: true)
        )
    }

    fun drawSprite(batch: SpriteBatch) {
        val currentAnimation = when {
            isFleeing -> fleeAnimation
            isMoving -> walkAnimation
            else -> idleAnimation
        }
        val currentFrame = currentAnimation.getKeyFrame(animationStateTime, true)

        // Flip texture based on direction
        val flipX = (facingRight && currentFrame.isFlipX) || (!facingRight && !currentFrame.isFlipX)
        if (flipX) {
            currentFrame.flip(true, false)
        }

        // Apply hurt tint
        batch.color = if (isHurt && !isFleeing) Color.RED else Color.WHITE
        batch.draw(currentFrame, position.x - currentFrame.regionWidth / 2, position.y - currentFrame.regionHeight / 2)
        batch.color = Color.WHITE // Reset color
    }

    fun drawStatusBars(shapeRenderer: ShapeRenderer, spriteBatch: SpriteBatch, font: BitmapFont) {
        drawBar(shapeRenderer, health, 2, Color.FIREBRICK, Color.GREEN)
        drawBar(shapeRenderer, food, 1, Color.GRAY, Color.LIME)
        drawBar(shapeRenderer, stamina, 0, Color.GRAY, Color.BLUE)
        if (currentWeapon != null && currentWeapon!!.ammo > 0) {
            spriteBatch.begin()
            font.draw(spriteBatch, currentWeapon!!.ammo.toString(), position.x + 17, position.y)
            spriteBatch.end()
        }
    }

    fun setWeapon(weapon: WorldObject.Weapon) {
        currentWeapon = weapon
    }

    fun shoot(target: Vector2) {
        require(currentWeapon != null && currentWeapon!!.ammo > 0)
        currentWeapon!!.ammo--
        if (currentWeapon!!.ammo == 0) currentWeapon = null
        val direction = target.sub(position).nor()
        val projectile = Projectile(
            ownerId = id,
            position = position.cpy(),
            velocity = direction.scl(300f)
        )
        GameState.projectiles.add(projectile)
    }

    fun takeDamage(value: Float) {
        health -= value
        if (health <= 0) {
            health = 0f
            spawnOnDeath()
        }
    }

    fun heal(value: Float) {
        health += value
        health = min(100f, health)
    }

    fun eat(value: Float) {
        food += value
        food = min(100f, food)
    }

    fun gainStamina(value: Float) {
        stamina += value
        stamina = min(100f, stamina)
    }

    private fun spawnOnDeath() {
        GameState.spawn(WorldObject.Stamina(value = stamina), currentWeapon)
    }

    private fun findNewPlan(gameState: GameState) {
        val worldState = createWorldState(gameState)
        val bestGoal = goals.maxByOrNull { it.priority(this) }

        if (bestGoal != null) {
            println("Agent ${id.toString().take(4)} chose goal: ${bestGoal.name} with priority ${bestGoal.priority(this)}")
            currentPlan = goapPlanner.plan(this, availableActions, worldState, bestGoal) ?: LinkedList()
            if (currentPlan.isEmpty()) {
                targetPosition = Vector2(
                    position.x + (-100..100).random(),
                    position.y + (-100..100).random()
                )
                currentAction = WanderAction()
            }
        }
    }

    private fun drawBar(shapeRenderer: ShapeRenderer, value: Float, yIndex: Int, bg: Color, fg: Color) {
        shapeRenderer.color = bg
        shapeRenderer.rect(position.x - 16, position.y + 15 + 7 * yIndex, size, 5f)
        shapeRenderer.color = fg
        shapeRenderer.rect(position.x - 16, position.y + 15 + 7 * yIndex, size * (value / 100f), 5f)
    }
}
