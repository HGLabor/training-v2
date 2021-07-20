package de.hglabor.training.main

import de.hglabor.training.challenge.*
import de.hglabor.training.challenge.damager.Damager
import de.hglabor.training.commands.commands
import de.hglabor.training.config.Config
import de.hglabor.training.events.regionListener
import de.hglabor.training.events.updateChallenge
import de.hglabor.training.utils.extensions.cancel
import de.hglabor.training.utils.extensions.isCreative
import de.hglabor.training.utils.itemsListener
import de.hglabor.training.utils.renewInv
import net.axay.kspigot.event.listen
import net.axay.kspigot.main.KSpigot
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

val Manager by lazy { InternalMainClass.INSTANCE }

class InternalMainClass : KSpigot() {
    companion object {
        lateinit var INSTANCE: InternalMainClass; private set
    }

    override fun load() {
        INSTANCE = this
    }

    override fun startup() {
        Config.load()
        itemsListener()
        challengeListener()
        regionListener()
        registerChallenges(Damager("easy"))
        commands()
        challenges.forEach { it.start() }

        config.options().copyDefaults(true)

        Bukkit.createWorld(WorldCreator("mlg"))

        listen<PlayerJoinEvent> { with(it) {
            joinMessage = null
            player.renewInv()
            player.teleport(player.location.world!!.spawnLocation)
            player.updateChallenge()
        }}

        listen<PlayerQuitEvent> { with(it) {
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


