package de.hglabor.training.challenge

import com.sk89q.worldedit.regions.Region
import de.hglabor.training.utils.clearInv
import de.hglabor.training.utils.closeAndClearInv
import de.hglabor.training.utils.renewInv
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.ArrayList

abstract class Challenge(val name: String, val region: Region) {
    val players = ArrayList<UUID>()
    inline fun players(forEach: (Player) -> Unit) {
        players.forEach { forEach(Bukkit.getPlayer(it)!!) }
    }
    open fun start() {}
    open fun stop() {}

    fun enter(player: Player) {
        players.add(player.uniqueId)
        player.renewInv()
        onEnter(player)
    }
    open fun onEnter(player: Player) {}

    fun leave(player: Player) {
        players.remove(player.uniqueId)
        player.renewInv()
        onLeave(player)
    }
    open fun onLeave(player: Player) {}
}