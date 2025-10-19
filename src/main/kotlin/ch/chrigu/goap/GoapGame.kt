package ch.chrigu.goap

import ch.chrigu.goap.animations.Animations
import ch.chrigu.goap.entities.Agent
import ch.chrigu.goap.entities.WorldObject
import ch.chrigu.goap.textures.GameTexture
import ch.chrigu.goap.world.GameState
import ch.chrigu.goap.world.WorldSetup
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.ScreenUtils

class GoapGame : ApplicationAdapter() {
    private lateinit var spriteBatch: SpriteBatch
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var camera: OrthographicCamera
    private lateinit var font: BitmapFont
    private val player: Agent
        get() = GameState.agents.first { it.isPlayer }

    private enum class LoadingState { LOADING, FINISHED }

    private var loadingState = LoadingState.LOADING

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
        val animations = Animations()
        WorldSetup(animations).setupWorld()
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
