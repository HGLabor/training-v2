package de.hglabor.training.events

import de.hglabor.training.challenge.Challenge
import de.hglabor.training.challenge.challenge
import de.hglabor.training.challenge.challenges
import de.hglabor.training.challenge.notInChallenge
import de.hglabor.training.utils.extensions.inRegion
import de.hglabor.training.utils.extensions.isCreative
import net.axay.kspigot.event.listen
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent


fun regionListener() {
    listen<PlayerMoveEvent> { event -> with(event) {
        if (!(player.isCreative()) && to?.distanceSquared(from) != 0.0) updatePlayerChallenge(player)
    }}
}

fun updatePlayerChallenge(player: Player) {
    val pChallenge = player.challenge
    if (!(player inRegion pChallenge)) player.challenge = null
    challenges.forEach {
        if (player notInChallenge it && player inRegion it) {
            player.challenge = it
        }
    }
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