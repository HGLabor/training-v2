group = "de.hglabor"
version = "0.0.1"
val kspigot = "1.18.0"
val kutils = "0.0.7"

plugins {
    kotlin("jvm") version "1.6.0"
    id("io.papermc.paperweight.userdev") version "1.3.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
    id("com.github.johnrengelman.shadow") version "7.1.1"
}

bukkit {
    main = "de.hglabor.training.main.InternalMainClass"
    website = "https://github.com/HGLabor/training-v2"
    version = project.version.toString()
    apiVersion = "1.18"
    libraries = listOf(
        "net.axay:kspigot:$kspigot",
        //"de.hglabor.utils:kutils:$kutils"
    )
    softDepend = listOf("WorldEdit")
}

repositories {
    mavenCentral()
    maven("https://repo.cloudnetservice.eu/repository/snapshots/") // CloudNet
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    paperDevBundle("1.18.1-R0.1-SNAPSHOT")
    compileOnly("net.axay:kspigot:$kspigot")
    implementation("de.hglabor.utils:kutils:$kutils")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.8")
    compileOnly("de.dytanic.cloudnet", "cloudnet-bridge", "3.4.0-SNAPSHOT")
    compileOnly(kotlin("stdlib-jdk8"))
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