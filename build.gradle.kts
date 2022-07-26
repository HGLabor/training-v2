group = "de.hglabor"
version = "1.0.0-alpha"
val kspigot = "1.19.0"
val kutils = "0.0.20"
val kotlinxSerializationJson = "1.3.3"

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("io.papermc.paperweight.userdev") version "1.3.7"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

bukkit {
    main = "de.hglabor.training.main.InternalMainClass"
    website = "https://github.com/HGLabor/training-v2"
    version = project.version.toString()
    apiVersion = "1.19"
    softDepend = listOf("WorldEdit")
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.cloudnetservice.eu/repository/snapshots/") // CloudNet
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    paperDevBundle("1.19-R0.1-SNAPSHOT")
    implementation("net.axay:kspigot:$kspigot")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationJson")
    implementation("de.hglabor.utils:kutils:$kutils")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.10")
    compileOnly("de.dytanic.cloudnet", "cloudnet-bridge", "3.4.0-SNAPSHOT")
    compileOnly("de.dytanic.cloudnet", "cloudnet-wrapper-jvm", "3.4.0-SNAPSHOT")
}

tasks {
    build {
        dependsOn(reobfJar)
    }
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}