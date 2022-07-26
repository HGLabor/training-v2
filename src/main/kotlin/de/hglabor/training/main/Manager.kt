package de.hglabor.training.main

import de.hglabor.training.challenge.*
import de.hglabor.training.commands.commands
import de.hglabor.training.events.mainListener
import de.hglabor.training.events.regionListener
import de.hglabor.training.itemsListener
import de.hglabor.utils.kutils.trainingGameRules
import de.hglabor.utils.kutils.world
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.axay.kspigot.main.KSpigot
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.WorldCreator
import java.io.File

val Manager by lazy { InternalMainClass.INSTANCE }
val PREFIX: String = "${ChatColor.DARK_GRAY}[${ChatColor.AQUA}Training${ChatColor.DARK_GRAY}]${ChatColor.WHITE}"

val json = Json {
    prettyPrint = true
    encodeDefaults = true
    @Suppress("EXPERIMENTAL_API_USAGE")
    prettyPrintIndent = "  "
    ignoreUnknownKeys = true
}

class InternalMainClass : KSpigot() {
    companion object {
        lateinit var INSTANCE: InternalMainClass; private set
    }

    val configFile by lazy {
        dataFolder.mkdir()
        File(dataFolder.path + "/challenges.json")
    }

    override fun load() {
        INSTANCE = this
    }

    override fun startup() {
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord")
        Bukkit.createWorld(WorldCreator("mlg"))?.trainingGameRules() ?: throw RuntimeException("mlg World could not be created.")
        world("world")!!.trainingGameRules()

        itemsListener()
        challengeListener()
        regionListener()

        val defaultChallenges = listOf(
            Damager("Noob", net.md_5.bungee.api.ChatColor.AQUA, 20, 4.0),
            Damager("Easy", net.md_5.bungee.api.ChatColor.GREEN, 10, 4.0),
            Damager("Medium", net.md_5.bungee.api.ChatColor.YELLOW/* TODO orange */, 10, 5.0),
            Damager("Hard", net.md_5.bungee.api.ChatColor.RED, 10, 7.0),
            Damager("Impossible", net.md_5.bungee.api.ChatColor.BLACK, 1, 1.0),
            Damager("Lava", net.md_5.bungee.api.ChatColor.GOLD, 20, 0.0),
            AimTraining(),
            CraftingChallenge(), // TODO config stuff
            ParkourChallenge(), // TODO config stuff
            // TODO Crap Damager
            //BlockMlg() // TODO MLGs
        )

        // Json deserialize
        if (!configFile.exists()) {
            logger.warning("No existing config file, creating one")
            configFile.createNewFile()
            // Register default challenges
            challenges += defaultChallenges
        }
        // Get challenges from json config
        else {
            challenges = json.decodeFromString(configFile.readText())
            defaultChallenges.forEach {
                // Add new challenges
                if (challenges.none { filterChallenge -> filterChallenge == it }) challenges += it
            }
        }

        commands()
        challenges.forEach { it.start() }

        config.options().copyDefaults(true)

        mainListener()
    }

    override fun shutdown() {
        challenges.forEach { it.stop() }

        // Serialize challenges
        configFile.writeText(json.encodeToString(challenges))
    }

}
