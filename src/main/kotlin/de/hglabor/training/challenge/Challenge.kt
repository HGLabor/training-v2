package de.hglabor.training.challenge

import com.sk89q.worldedit.regions.Region
import de.hglabor.training.config.PREFIX
import de.hglabor.training.events.ChallengeEnterEvent
import de.hglabor.training.events.ChallengeLeaveEvent
import de.hglabor.training.utils.extensions.reflectMethod
import de.hglabor.training.utils.renewInv
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.actionBar
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityEvent
import java.util.*

abstract class Challenge(val name: String, val world: World, val color: ChatColor = KColors.WHITE) {
    abstract var region: Region

    val players = HashSet<UUID>()
    inline fun players(forEach: Player.() -> Unit) {
        players.forEach { forEach(Bukkit.getPlayer(it)!!) }
    }
    // Get from config
    open fun start() {}
    // Don't save to config -> don't override values that were changed in the config
    open fun stop() {}

    fun restart() { stop(); start() }

    open val displayName = name
    internal fun enter(player: Player) {
        players.add(player.uniqueId)
        player.renewInv()
        player.actionBar("${KColors.GREEN}You entered $displayName")
        onEnter(player)
    }
    protected open fun onEnter(player: Player) {}

    internal fun leave(player: Player) {
        players.remove(player.uniqueId)
        player.renewInv()
        player.actionBar("${KColors.RED}You left $displayName")
        onLeave(player)
    }

    override fun toString(): String = "$displayName[center={${region.center.x},${region.center.y},${region.center.z}}]"

    protected open fun onLeave(player: Player) {}

    /**
     * Executes the given [callback] if the player of the
     * [Event] is in this challenge.
     */
    inline fun <reified T : Event> challengePlayerEvent(crossinline callback: T.() -> Unit) {
        listen<T> {
            // Try to reflect directly
            var player = it.reflectMethod<Player>("getPlayer")
            // Try to get from entity event
            if (player == null) if ((it as EntityEvent).entity is Player) player = (it as EntityEvent).entity as Player
            if (player?.challenge == this) it.callback()
        }
    }

    fun Player.fail() {
        sendMessage("$PREFIX ${KColors.RED}You failed ${this@Challenge.displayName}")
        renewInv()
        teleport(bedSpawnLocation ?: return)
    }

    open fun saveToConfig() {}

    open val hunger = false
    open val warpItems = true
}

fun challengeListener() {
    listen<ChallengeEnterEvent> { with(it) {
        challenge.enter(player)
    }}

    listen<ChallengeLeaveEvent> { with(it) {
        challenge.leave(player)
    }}
}