package de.hglabor.training.events

import de.hglabor.training.challenge.Challenge
import de.hglabor.training.challenge.challenges
import net.axay.kspigot.event.listen
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.player.PlayerMoveEvent


fun regionListener() {
    listen<PlayerMoveEvent> { event -> with(event) {
        challenges.forEach {

        }
    }}
}

class ChallengeEnterEvent(val player: Player, val challenge: Challenge) : TrainingEvent() {
    companion object {
        @Suppress("unused")
        @JvmStatic fun getHandlerList() = handlers
    }
}
class ChallengeLeaveEvent(val player: Player, val challenge: Challenge) : TrainingEvent() {
    companion object {
        @Suppress("unused")
        @JvmStatic fun getHandlerList() = handlers
    }
}