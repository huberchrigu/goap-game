package ch.chrigu.goap

import ch.chrigu.goap.actions.*
import ch.chrigu.goap.entities.Agent
import ch.chrigu.goap.entities.WorldObject
import ch.chrigu.goap.position.randomPos
import ch.chrigu.goap.textures.GameTexture
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ScreenUtils

class GoapGame : ApplicationAdapter() {
    private lateinit var spriteBatch: SpriteBatch
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var camera: OrthographicCamera
    private lateinit var font: BitmapFont
    private lateinit var player: Agent

    private enum class LoadingState { LOADING, FINISHED }

    private var loadingState = LoadingState.LOADING

    // --- Animation objects ---
    private lateinit var playerWalkAnimation: Animation<TextureRegion>
    private lateinit var playerIdleAnimation: Animation<TextureRegion>
    private lateinit var playerFleeAnimation: Animation<TextureRegion>
    private lateinit var npcWalkAnimation: Animation<TextureRegion>
    private lateinit var npcIdleAnimation: Animation<TextureRegion>
    private lateinit var npcFleeAnimation: Animation<TextureRegion>

    override fun create() {
        camera = OrthographicCamera().apply {
            setToOrtho(false, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        }
        spriteBatch = SpriteBatch()
        shapeRenderer = ShapeRenderer()
        font = BitmapFont()
        font.color = Color.WHITE

        downloadAssets()
    }

    override fun render() {
        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1f)
        camera.update()
        spriteBatch.projectionMatrix = camera.combined
        shapeRenderer.projectionMatrix = camera.combined

        when (loadingState) {
            LoadingState.LOADING -> {
                val textures = GameTexture.entries
                val loaded = textures.count { it.texture != null }
                if (loaded == textures.size) {
                    onAssetsLoaded()
                    loadingState = LoadingState.FINISHED
                } else {
                    spriteBatch.begin()
                    font.draw(spriteBatch, "Loading Assets... (${loaded} / ${textures.size})", 10f, 30f)
                    spriteBatch.end()
                }
            }

            LoadingState.FINISHED -> {
                val delta = Gdx.graphics.deltaTime
                updateGame(delta)
                drawGame()
            }
        }
    }

    override fun dispose() {
        spriteBatch.dispose()
        shapeRenderer.dispose()
        font.dispose()
        GameTexture.entries.forEach { it.texture?.dispose() }
        GameState.agents.clear()
        GameState.worldObjects.clear()
        GameState.projectiles.clear()
    }

    private fun downloadAssets() {
        Gdx.app.log("AssetDownloader", "Starting asset download...")
        GameTexture.entries.forEach { it.load() }
    }

    private fun onAssetsLoaded() {
        Gdx.app.log("GoapGame", "All assets loaded. Initializing game world.")
        playerIdleAnimation = Animation(0f, TextureRegion(GameTexture.PLAYER_IDLE.texture))
        playerWalkAnimation = Animation(0.25f, Array<TextureRegion>().apply {
            add(TextureRegion(GameTexture.PLAYER_WALK1.texture))
            add(TextureRegion(GameTexture.PLAYER_WALK2.texture))
        })
        playerFleeAnimation = Animation(0.1f, TextureRegion(GameTexture.PLAYER_FLEE.texture))
        npcIdleAnimation = Animation(0f, TextureRegion(GameTexture.NPC_IDLE.texture))
        npcWalkAnimation = Animation(0.25f, Array<TextureRegion>().apply {
            add(TextureRegion(GameTexture.NPC_WALK1.texture))
            add(TextureRegion(GameTexture.NPC_WALK2.texture))
        })
        npcFleeAnimation = Animation(0.1f, TextureRegion(GameTexture.NPC_FLEE.texture))

        setupWorld()
    }

    private fun setupWorld() {
        val availableActions = setOf(PickupWeaponAction(), EatFoodAction(), HealAction(), RestAction(), AttackEnemyAction(), WanderAction(), FleeAction())
        val goals = setOf(
            GoapGoal("Flee", { agent -> if (agent.targetEnemy != null && agent.health <= 40) 100f else 0f }, { !enemyVisible }),
            GoapGoal("Kill Enemy", { agent -> if (agent.targetEnemy != null) 90f else 0f }, { enemyDead }),
            GoapGoal("Get Weapon", { agent -> if (agent.currentWeapon == null) 50f else 0f }, { hasWeapon }),
            GoapGoal("Stay Healthy", { agent -> (100f - agent.health) / 100f * 80f }, { isHealthy }),
            GoapGoal("Stay Fed", { agent -> (100f - agent.food) / 100f * 60f }, { isFed }),
            GoapGoal("Stay Rested", { agent -> (100f - agent.stamina) / 100f * 40f }, { isRested })
        )
        player = Agent(initialPosition = Vector2(100f, 100f), isPlayer = true).apply {
            idleAnimation = playerIdleAnimation
            walkAnimation = playerWalkAnimation
            fleeAnimation = playerFleeAnimation
        }
        GameState.agents.add(player)
        repeat(5) {
            val npc = Agent(initialPosition = randomPos()).apply {
                this.availableActions = availableActions
                this.goals = goals
                idleAnimation = npcIdleAnimation
                walkAnimation = npcWalkAnimation
                fleeAnimation = npcFleeAnimation
            }
            GameState.agents.add(npc)
        }
        repeat(10) { GameState.spawn(WorldObject.Food()) }
        repeat(5) { GameState.spawn(WorldObject.Health()) }
        repeat(5) { GameState.spawn(WorldObject.Stamina()) }
        repeat(3) { GameState.spawn(WorldObject.Weapon()) }
    }

    private fun updateGame(delta: Float) {
        handlePlayerInput(delta)
        GameState.agents.forEach { it.update(delta, GameState) }
        updateProjectiles(delta)
        checkCollisions()
    }

    private fun drawGame() {
        drawWorldObjectsAndAgents()
        drawProjectilesAndStatus()
    }

    private fun drawWorldObjectsAndAgents() {
        spriteBatch.begin()
        GameState.worldObjects.forEach { obj ->
            val texture = obj.texture.texture!!
            spriteBatch.draw(texture, obj.position.x - texture.width / 2, obj.position.y - texture.height / 2)
        }
        GameState.agents.forEach { agent ->
            if (!agent.isDead) {
                agent.drawSprite(spriteBatch)
            }
        }
        spriteBatch.end()
    }

    private fun drawProjectilesAndStatus() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        GameState.projectiles.forEach {
            shapeRenderer.color = Color.YELLOW
            shapeRenderer.circle(it.position.x, it.position.y, 3f)
        }
        GameState.agents.forEach { agent ->
            if (!agent.isDead) {
                agent.drawStatusBars(shapeRenderer, spriteBatch, font)
            }
        }
        shapeRenderer.end()
    }

    private fun updateProjectiles(delta: Float) {
        GameState.projectiles.removeIf { p ->
            p.position.mulAdd(p.velocity, delta)
            p.lifeTime -= delta
            p.lifeTime <= 0
        }
    }

    private fun checkCollisions() {
        GameState.projectiles.removeIf { p ->
            val agent = GameState.agents
                .firstOrNull { it.collides(p) }
            if (agent == null)
                false
            else {
                agent.takeDamage(25f)
                true
            }
        }
        GameState.worldObjects.removeIf { o ->
            val agent = GameState.agents
                .firstOrNull { it.collides(o) }
            if (agent == null)
                false
            else {
                when (o) {
                    is WorldObject.Weapon -> agent.setWeapon(o)
                    is WorldObject.Health -> agent.heal(o.value)
                    is WorldObject.Food -> agent.eat(o.value)
                    is WorldObject.Stamina -> agent.gainStamina(o.value)
                }
                true
            }
        }
    }

    private fun handlePlayerInput(delta: Float) {
        val moveDirection = Vector2.Zero.cpy()
        if (Gdx.input.isKeyPressed(Input.Keys.W)) moveDirection.y = 1f
        if (Gdx.input.isKeyPressed(Input.Keys.S)) moveDirection.y = -1f
        if (Gdx.input.isKeyPressed(Input.Keys.A)) moveDirection.x = -1f
        if (Gdx.input.isKeyPressed(Input.Keys.D)) moveDirection.x = 1f
        if (moveDirection.len2() > 0) {
            player.moveTo(moveDirection.nor())
        }

        // Shooting
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && player.currentWeapon != null && player.currentWeapon!!.ammo > 0) {
            val mousePos3D = Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            camera.unproject(mousePos3D)
            val target = Vector2(mousePos3D.x, mousePos3D.y)
            player.shoot(target)
        }
    }
}
