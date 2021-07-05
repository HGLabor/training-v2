package de.hglabor.training.challenge

import com.sk89q.worldedit.regions.Region
import de.hglabor.training.events.ChallengeEnterEvent
import de.hglabor.training.events.ChallengeLeaveEvent
import de.hglabor.training.utils.renewInv
import net.axay.kspigot.commands.command
import net.axay.kspigot.commands.literal
import net.axay.kspigot.event.listen
import net.axay.kspigot.ipaddress.checkIP
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.HashSet

abstract class Challenge(private val name: String, val region: Region) {
    val players = HashSet<UUID>()
    inline fun players(forEach: (Player) -> Unit) {
        players.forEach { forEach(Bukkit.getPlayer(it)!!) }
    }
    open fun start() {}
    open fun stop() {}

    fun enter(player: Player) {
        players.add(player.uniqueId)
        player.renewInv()
        player.sendMessage("You entered $name")
        onEnter(player)
    }
    open fun onEnter(player: Player) {}

    fun leave(player: Player) {
        players.remove(player.uniqueId)
        player.renewInv()
        player.sendMessage("You left $name")
        onLeave(player)
    }
    open fun onLeave(player: Player) {}
}

fun challengeListener() {
    listen<ChallengeEnterEvent> {
        it.player.checkIP()
    }

    listen<ChallengeLeaveEvent> {

    }
}