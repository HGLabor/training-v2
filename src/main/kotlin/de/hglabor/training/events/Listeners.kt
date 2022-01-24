package de.hglabor.training.events

import de.hglabor.training.challenge.Mlg
import de.hglabor.training.challenge.challenge
import de.hglabor.training.defaultInv
import de.hglabor.utils.kutils.cancel
import de.hglabor.utils.kutils.isCreative
import net.axay.kspigot.event.listen
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.*

fun mainListener() {
    joinQuitListener()

    cancelEvent<EntityDamageByEntityEvent>()
    cancelEventWhen<PlayerAttemptPickupItemEvent> { player.challenge == null && player.gameMode == GameMode.SURVIVAL }
    cancelEventWhen<FoodLevelChangeEvent> { (entity as Player).challenge?.hunger != true }
    cancelEventWhen<BlockBreakEvent> { !player.isCreative() }
    cancelEventWhen<BlockPlaceEvent> { !player.isCreative() && player.challenge !is Mlg }
    cancelEventWhen<PlayerInteractEvent> { !player.isCreative() && player.challenge !is Mlg }
    cancelEventWhen<PlayerBucketEmptyEvent> { !player.isCreative() && player.challenge !is Mlg }

    listen<PlayerGameModeChangeEvent> { with(it) {
        when (newGameMode) {
            // Update player challenge if in survival
            GameMode.SURVIVAL -> player.updateChallenge()
            // Leave challenge if in not in survival
            else -> player.challenge = null
        }
    }}
}

// Remove join and quit messages & handle join stuff
private fun joinQuitListener() {
    listen<PlayerJoinEvent> { with(it) {
        @Suppress("DEPRECATION")
        joinMessage = null
        player.defaultInv()
        player.teleport(player.location.world!!.spawnLocation)
        player.updateChallengeIfSurvival()
    }}

    listen<PlayerQuitEvent> { with(it) {
        @Suppress("DEPRECATION")
        quitMessage = null
        player.challenge = null
    }}
}

inline fun <reified T : org.bukkit.event.Event> cancelEvent(priority: EventPriority = EventPriority.NORMAL) = cancelEventWhen<T>(priority) { true }
inline fun <reified T : org.bukkit.event.Event> cancelEventWhen(priority: EventPriority = EventPriority.NORMAL,
                                                                crossinline condition: T.() -> Boolean) = listen<T>(priority) {
    if (it.condition() && it is Cancellable) it.cancel()
}