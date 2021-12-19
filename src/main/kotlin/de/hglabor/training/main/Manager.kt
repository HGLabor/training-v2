package de.hglabor.training.main

import de.hglabor.training.challenge.challenge
import de.hglabor.training.challenge.challengeListener
import de.hglabor.training.challenge.challenges
import de.hglabor.training.challenge.damager.Damager
import de.hglabor.training.challenge.mlg.Mlg
import de.hglabor.training.challenge.registerChallenges
import de.hglabor.training.commands.commands
import de.hglabor.training.config.Config
import de.hglabor.training.events.regionListener
import de.hglabor.training.events.updateChallenge
import de.hglabor.training.events.updateChallengeIfSurvival
import de.hglabor.training.itemsListener
import de.hglabor.training.renewInv
import de.hglabor.utils.kutils.cancel
import de.hglabor.utils.kutils.isCreative
import de.hglabor.utils.kutils.trainingGameRules
import de.hglabor.utils.kutils.world
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.event.listen
import net.axay.kspigot.main.KSpigot
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.*

val Manager by lazy { InternalMainClass.INSTANCE }

class InternalMainClass : KSpigot() {
    companion object {
        lateinit var INSTANCE: InternalMainClass; private set
    }

    override fun load() {
        INSTANCE = this
    }

    override fun startup() {
        Bukkit.createWorld(WorldCreator("mlg"))?.trainingGameRules() ?: throw RuntimeException("mlg World could not be created.")
        world("world")!!.trainingGameRules()

        Config.load()

        itemsListener()
        challengeListener()
        regionListener()
        registerChallenges(
            Damager("noob", KColors.AQUA, 20, 4.0),
            Damager("easy", KColors.GREEN, 10, 4.0),
            Damager("medium", KColors.ORANGE, 10, 5.0),
            Damager("hard", KColors.RED, 10, 7.0),
            Damager("impossible", KColors.BLACK, 1, 1.0),
            // TODO Crap Damager
            Mlg("test") // TODO MLGs
        )
        commands()
        challenges.forEach { it.start() }

        config.options().copyDefaults(true)

        listen<EntityDamageByEntityEvent> { it.cancel() }
        listen<PlayerAttemptPickupItemEvent> { if (it.player.challenge == null && it.player.gameMode == GameMode.SURVIVAL) it.cancel() }

        listen<PlayerJoinEvent> { with(it) {
            @Suppress("DEPRECATION")
            joinMessage = null
            player.renewInv()
            player.teleport(player.location.world!!.spawnLocation)
            player.updateChallengeIfSurvival()
        }}

        listen<PlayerQuitEvent> { with(it) {
            @Suppress("DEPRECATION")
            quitMessage = null
            player.challenge = null
        }}

        listen<FoodLevelChangeEvent> { with(it) {
            if ((entity as Player).challenge?.hunger != true) cancel()
        }}

        listen<BlockBreakEvent> { with (it) {
            if (!player.isCreative()) cancel()
        }}

        listen<BlockPlaceEvent> { with (it) {
            if (!player.isCreative()) cancel()
        }}

        listen<PlayerInteractEvent> { with (it) {
            if (!player.isCreative()) cancel()
        }}

        listen<PlayerGameModeChangeEvent> { with(it) {
            when (newGameMode) {
                // Update player challenge if in survival
                GameMode.SURVIVAL -> player.updateChallenge()
                // Leave challenge if in not in survival
                else -> player.challenge = null
            }
        }}
    }

    override fun shutdown() {
        challenges.forEach { it.stop() }
    }

}


