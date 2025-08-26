plugins {
    kotlin("jvm") version "2.2.0"
    application
}

group = "ch.chrigu.goap"
version = "1.0.0"

repositories {
    mavenCentral()
}

val gdxVersion = "1.13.5"
val kotlinVersion = "2.2.0"

application {
    mainClass.set("com.example.goap.DesktopLauncherKt")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("com.badlogicgames.gdx:gdx:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
    runtimeOnly("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
}

tasks.jar {
    manifest.attributes["Main-Class"] = application.mainClass.get()
    val dependencies = configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    }
    from(dependencies)
    from(sourceSets.main.get().resources)
}
