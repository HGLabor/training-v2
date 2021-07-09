package de.hglabor.training.main

import de.hglabor.training.challenge.CuboidChallenge
import de.hglabor.training.challenge.challengeListener
import de.hglabor.training.challenge.challenges
import de.hglabor.training.challenge.registerChallenges
import de.hglabor.training.commands.commands
import de.hglabor.training.config.Config
import de.hglabor.training.events.processPlayerMove
import de.hglabor.training.events.regionListener
import de.hglabor.training.utils.itemsListener
import net.axay.kspigot.event.listen
import net.axay.kspigot.main.KSpigot
import org.bukkit.event.player.PlayerJoinEvent

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
        registerChallenges(CuboidChallenge("test"))
        commands()
        challenges.forEach { it.start() }

        listen<PlayerJoinEvent> {
            processPlayerMove(it.player)
        }
    }

    override fun shutdown() {
        challenges.forEach { it.stop() }
    }

}


