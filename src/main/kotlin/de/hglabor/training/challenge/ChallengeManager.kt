package de.hglabor.training.challenge

import de.hglabor.training.events.ChallengeEnterEvent
import de.hglabor.training.events.ChallengeLeaveEvent
import de.hglabor.utils.kutils.call
import de.hglabor.utils.kutils.we
import net.axay.kspigot.event.listen
import org.bukkit.Location
import org.bukkit.entity.Player

var challenges = HashSet<Challenge>()

private fun HashSet<Challenge>.getForPlayer(player: Player): Challenge? = firstOrNull { player.uniqueId in it.players }

fun challenge(name: String): Challenge? = challenges.firstOrNull { it.name.equals(name, true) }

val challengeNames: List<String> get() = run {
    val names = ArrayList<String>()
    challenges.forEach { names.add(it.name) }
    return names
}

fun Challenge?.addPlayer(player: Player) = this?.players?.add(player.uniqueId) ?: false
infix fun Challenge?.contains(player: Player) = this?.players?.contains(player.uniqueId) ?: false
infix fun Player?.inChallenge(challenge: Challenge?) = this?.challenge == challenge
infix fun Player?.notInChallenge(challenge: Challenge?) = !inChallenge(challenge)

infix fun Player.inRegion(challenge: Challenge?) = location inRegion challenge
infix fun Location.inRegion(challenge: Challenge?) = challenge?.world != null && challenge.world == world && challenge.region.contains(we())

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

fun challengeListener() {
    listen<ChallengeEnterEvent> { with(it) {
        challenge.enter(player)
    }}

    listen<ChallengeLeaveEvent> { with(it) {
        challenge.leave(player)
    }}
}