package ch.chrigu.goap.textures

import ch.chrigu.goap.textures.TextConstants.BG_NPC
import ch.chrigu.goap.textures.TextConstants.BG_PLAYER
import ch.chrigu.goap.textures.TextConstants.FG_CHAR
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Net
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture

private const val url = "https://placehold.co/32x32"

enum class GameTexture(private val url: String?, private val fileName: String? = null) {
    PLAYER_IDLE("$url/$BG_PLAYER/$FG_CHAR.png?text=P"),
    PLAYER_WALK1("$url/$BG_PLAYER/$FG_CHAR.png?text=P_"),
    PLAYER_WALK2("$url/$BG_PLAYER/$FG_CHAR.png?text=_P"),
    PLAYER_FLEE("$url/$BG_PLAYER/$FG_CHAR.png?text=!!"),
    NPC_IDLE("$url/$BG_NPC/$FG_CHAR.png?text=N"),
    NPC_WALK1("$url/$BG_NPC/$FG_CHAR.png?text=N_"),
    NPC_WALK2("$url/$BG_NPC/$FG_CHAR.png?text=_N"),
    NPC_FLEE("$url/$BG_NPC/$FG_CHAR.png?text=!!"),
    FOOD(null, "food.png"),
    HEALTH(null, "health.png"),
    STAMINA(null, "stamina.png"),
    WEAPON(null, "weapon.png");

    var texture: Texture? = null

    fun load() {
        if (texture == null) {
            if (fileName == null) {
                val httpRequest = Net.HttpRequest(Net.HttpMethods.GET).also { it.url = url }
                Gdx.net.sendHttpRequest(httpRequest, LoadTextureRequest())
            } else {
                val data = javaClass.classLoader.getResourceAsStream(fileName)!!.readAllBytes()
                val original = Pixmap(data, 0, data.size)
                val scaled = Pixmap(32, 32, original.format)
                scaled.drawPixmap(original, 0, 0, original.width, original.height, 0, 0, 32, 32)
                texture = Texture(scaled)
            }
        }
    }

    private inner class LoadTextureRequest : Net.HttpResponseListener {
        override fun handleHttpResponse(httpResponse: Net.HttpResponse) {
            val bytes = httpResponse.result
            Gdx.app.postRunnable {
                try {
                    val pixmap = Pixmap(bytes, 0, bytes.size)
                    val texture = Texture(pixmap)
                    pixmap.dispose()
                    Gdx.app.log("AssetDownloader", "Successfully loaded $texture")
                    this@GameTexture.texture = texture
                } catch (e: Exception) {
                    Gdx.app.error("AssetDownloader", "Error creating texture for $this", e)
                    this@GameTexture.texture = Texture(Pixmap(32, 32, Pixmap.Format.RGB888).apply {
                        setColor(Color.MAGENTA)
                        fill()
                    })
                }
            }
        }

        override fun failed(t: Throwable) {
            Gdx.app.error("AssetDownloader", "Failed to download $this", t)
        }

        override fun cancelled() {
            Gdx.app.log("AssetDownloader", "Download cancelled for $this")
        }
    }
}

private object TextConstants {
    const val BG_PLAYER = "FFD700"
    const val BG_NPC = "00FFFF"
    const val FG_CHAR = "000000"
    const val FG_ITEMS = "FFFFFF"
}
