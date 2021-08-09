package de.hglabor.training.events

import de.hglabor.training.challenge.Challenge
import de.hglabor.training.challenge.challenge
import de.hglabor.training.challenge.challenges
import de.hglabor.training.challenge.notInChallenge
import de.hglabor.training.utils.extensions.inRegion
import net.axay.kspigot.event.listen
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent


fun regionListener() {
    listen<PlayerMoveEvent> { event -> with(event) {
        if (to?.distanceSquared(from) != 0.0) player.updateChallengeIfSurvival()
    }}
}

fun Player.updateChallengeIfSurvival() = if (gameMode == GameMode.SURVIVAL) updateChallenge() else Unit

fun Player.updateChallenge() {
    val pChallenge = challenge
    if (!(this inRegion pChallenge)) challenge = null
    challenges.forEach {
        if (this notInChallenge it && this inRegion it) {
            challenge = it
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