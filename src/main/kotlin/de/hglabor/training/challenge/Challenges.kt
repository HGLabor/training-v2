package de.hglabor.training.challenge

import de.hglabor.training.events.ChallengeEnterEvent
import de.hglabor.training.events.ChallengeLeaveEvent
import de.hglabor.training.utils.extensions.call
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

val challenges = HashSet<Challenge>()

private fun HashSet<Challenge>.getForPlayer(player: Player): Challenge? {
    forEach {
        if (it.players.contains(player.uniqueId)) return it
    }
    return null
}

fun registerChallenges(vararg toRegister: Challenge) = challenges.addAll(toRegister)

fun Challenge?.addPlayer(player: Player) = this?.players?.add(player.uniqueId) ?: false
infix fun Challenge?.contains(player: Player) = this?.players?.contains(player.uniqueId) ?: false
infix fun Player?.inChallenge(challenge: Challenge?) = this?.challenge == challenge
infix fun Player?.notInChallenge(challenge: Challenge?) = !inChallenge(challenge)

/** when this is null the player isn't inside any challenge */
var Player.challenge: Challenge?
    get() = challenges.getForPlayer(this)
    set(value) {
        // Leave old challenge
        challenge?.let { ChallengeLeaveEvent(this, it).call() }
        // Add player to new challenge
        value?.players?.add(uniqueId)
        // Enter new challenge
        value?.let { ChallengeEnterEvent(this, it).call() }
    }