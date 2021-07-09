package de.hglabor.training.challenge

import com.sk89q.worldedit.regions.Region
import de.hglabor.training.events.ChallengeEnterEvent
import de.hglabor.training.events.ChallengeLeaveEvent
import de.hglabor.training.utils.renewInv
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.actionBar
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

abstract class Challenge(val name: String, val region: Region) {
    val players = HashSet<UUID>()
    inline fun players(forEach: (Player) -> Unit) {
        players.forEach { forEach(Bukkit.getPlayer(it)!!) }
    }
    open fun start() {}
    open fun stop() {}

    internal fun enter(player: Player) {
        players.add(player.uniqueId)
        player.renewInv()
        player.actionBar("${KColors.GREEN}You entered $name")
        onEnter(player)
    }
    protected open fun onEnter(player: Player) {}

    internal fun leave(player: Player) {
        players.remove(player.uniqueId)
        player.renewInv()
        player.actionBar("${KColors.RED}You left $name")
        onLeave(player)
    }

    override fun toString(): String = "$name Challenge [center={${region.center.x},${region.center.y},${region.center.z}}]"

    protected open fun onLeave(player: Player) {}
}

fun challengeListener() {
    listen<ChallengeEnterEvent> { with(it) {
        challenge.enter(player)
    }}

    listen<ChallengeLeaveEvent> { with(it) {
        challenge.leave(player)
    }}
}