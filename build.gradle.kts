@file:Suppress("PropertyName")

group = "de.hglabor"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.5.10"
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
    // Paper
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://repo.codemc.io/repository/maven-public/")
    // FAWE
    maven("https://mvn.intellectualsites.com/content/repositories/releases/")
    // CloudNet
    maven("https://repo.cloudnetservice.eu/repository/releases/")
}

dependencies {
    implementation(kotlin("reflect"))
    // CraftBukkit
    compileOnly("org.bukkit", "craftbukkit", "1.17-R0.1-SNAPSHOT")
    // PAPER
    compileOnly("io.papermc.paper:paper-api:1.17-R0.1-SNAPSHOT")
    // FAWE
    compileOnly("com.intellectualsites.fawe:FAWE-Bukkit:1.16-637")
    // KSPIGOT
    implementation("net.axay:kspigot:1.17.2")
    // HGLabor Utils
    implementation("de.hglabor:hglabor-utils:0.0.6")
    // CloudNet
    compileOnly("de.dytanic.cloudnet", "cloudnet-bridge", "3.3.0-RELEASE")
}

tasks {
    compileJava {
        options.release.set(16)
        options.encoding = "UTF-8"
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "16"
    }
}