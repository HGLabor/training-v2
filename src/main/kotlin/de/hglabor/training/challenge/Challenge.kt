package de.hglabor.training.challenge

import com.sk89q.worldedit.regions.Region
import de.hglabor.training.events.ChallengeEnterEvent
import de.hglabor.training.events.ChallengeLeaveEvent
import de.hglabor.training.events.updateChallengeIfSurvival
import de.hglabor.training.main.PREFIX
import de.hglabor.training.renewInv
import de.hglabor.utils.kutils.reflectMethod
import de.hglabor.utils.kutils.serialization.ChatColorSerializer
import de.hglabor.utils.kutils.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
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

@Serializable
sealed class Challenge {
    abstract val name: String
    abstract val world: World
    @Serializable(with = ChatColorSerializer::class)
    abstract val color: ChatColor

    abstract val region: Region

    val players = HashSet<@Serializable(with = UUIDSerializer::class) UUID>()
    inline fun players(forEach: Player.() -> Unit) {
        players.forEach { forEach(Bukkit.getPlayer(it)!!) }
    }
    open fun start() {}
    open fun stop() {}

    fun restart() { stop(); start() }

    open val displayName get() = name
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
        teleport((if (bedSpawnLocation?.world == world) bedSpawnLocation else null) ?: world.spawnLocation)
        updateChallengeIfSurvival()
        renewInv()
    }

    open fun saveToConfig() {}

    @Transient open val hunger = false
    @Transient open val warpItems = true
}

fun challengeListener() {
    listen<ChallengeEnterEvent> { with(it) {
        challenge.enter(player)
    }}

    listen<ChallengeLeaveEvent> { with(it) {
        challenge.leave(player)
    }}
}