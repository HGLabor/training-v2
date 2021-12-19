package de.hglabor.training.challenge

import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.regions.Region
import de.hglabor.training.main.Manager
import de.hglabor.utils.kutils.location
import de.hglabor.utils.kutils.we
import de.hglabor.utils.kutils.world
import net.axay.kspigot.chat.KColors
import net.md_5.bungee.api.ChatColor
import org.bukkit.Location
import org.bukkit.World

private val defaultPos = listOf(Location(null, 4.0, 64.0, 6.0), Location(null, 10.0, 65.0, 12.0))

private fun pos(name: String, number: Int): Location? = with(Manager.config) {
    val path = "challenge.$name.region.pos$number"
    addDefault(path, defaultPos[number-1])
    Manager.saveConfig()
    getLocation(path)
}

open class CuboidChallenge(name: String, world: World = world("world")!!, color: ChatColor = KColors.WHITE) :
    Challenge(name, world, color) {
    val cuboidRegion get() = region as CuboidRegion
    override lateinit var region: Region

    override fun start() {
        // Get from config
        region = CuboidRegion(world.we(), pos(name, 1)!!.we(), pos(name, 2)!!.we(),)
    }

    override fun saveToConfig() = with(Manager.config) {
        for (pos in 1..2) {
            val path = "challenge.${this@CuboidChallenge.name}.region.pos$pos"
            val location = (if (pos == 1) cuboidRegion.pos1.location() else cuboidRegion.pos2.location())
            if (getLocation(path) != location) set(path, location)
        }
    }
}