package de.hglabor.training.challenge.damager

import de.hglabor.training.challenge.CuboidChallenge
import net.axay.kspigot.extensions.broadcast
import org.bukkit.event.player.PlayerInteractEvent

class Damager(name: String) : CuboidChallenge("$name Damager") {
    init {
        challengePlayerEvent<PlayerInteractEvent> {
            broadcast("interacted in $name")
        }
    }
}