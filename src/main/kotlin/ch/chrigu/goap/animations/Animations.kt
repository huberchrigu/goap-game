package ch.chrigu.goap.animations

import ch.chrigu.goap.textures.GameTexture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion

class Animations {
    val playerIdleAnimation = Animation(0f, TextureRegion(GameTexture.PLAYER_IDLE.texture))
    val playerWalkAnimation = Animation(
        0.25f,
        TextureRegion(GameTexture.PLAYER_WALK1.texture),
        TextureRegion(GameTexture.PLAYER_WALK2.texture)
    )
    val playerFleeAnimation = Animation(0.1f, TextureRegion(GameTexture.PLAYER_FLEE.texture))
    val npcIdleAnimation = Animation(0f, TextureRegion(GameTexture.NPC_IDLE.texture))
    val npcWalkAnimation = Animation(
        0.25f,
        TextureRegion(GameTexture.NPC_WALK1.texture),
        TextureRegion(GameTexture.NPC_WALK2.texture)
    )
    val npcFleeAnimation = Animation(0.1f, TextureRegion(GameTexture.NPC_FLEE.texture))
}