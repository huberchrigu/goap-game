package ch.chrigu.goap

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

fun main() {
    val config = Lwjgl3ApplicationConfiguration().apply {
        setTitle("GOAP Network Demo")
        setWindowedMode(1280, 720)
        useVsync(true)
        // We can't set a window icon from a network resource easily, so we'll skip it.
    }
    Lwjgl3Application(GoapGame(), config)
}
