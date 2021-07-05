package de.hglabor.training.challenge

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

infix fun Challenge?.addPlayer(player: Player) = this?.players?.add(player.uniqueId) ?: false

/** when this is null the player isn't inside any challenge */
var Player.challenge: Challenge?
    get() = challenges.getForPlayer(this)
    set(value) { challenge?.players?.add(uniqueId) }