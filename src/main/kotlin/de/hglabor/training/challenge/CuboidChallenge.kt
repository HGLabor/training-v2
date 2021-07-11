package de.hglabor.training.challenge

import com.sk89q.worldedit.regions.CuboidRegion
import de.hglabor.training.main.Manager
import de.hglabor.training.utils.extensions.bukkit
import de.hglabor.training.utils.extensions.we
import de.hglabor.training.utils.extensions.world
import org.bukkit.Location
import org.bukkit.World

private val defaultPos = listOf(Location(null, 4.0, 64.0, 6.0), Location(null, 10.0, 65.0, 12.0))

private fun pos(name: String, number: Int): Location? { with(Manager.config) {
    val path = "challenge.$name.region.pos$number"
    addDefault(path, defaultPos[number-1])
    Manager.saveConfig()
    return getLocation(path)
}}

open class CuboidChallenge(name: String, protected val world: World = world("world")!!) :
    Challenge(name, CuboidRegion(
        world.we(),
        pos(name, 1)!!.we(),
        pos(name, 2)!!.we(),
    )) {
    init {
        Manager.saveConfig()
    }
    val cuboidRegion get() = region as CuboidRegion

    override fun stop() { with(Manager.config) {
        for (pos in 1..2) {
            val path = "challenge.${this@CuboidChallenge.name}.region.pos$pos"
            val location = (if (pos == 1) cuboidRegion.pos1.bukkit() else cuboidRegion.pos2.bukkit())
            if (getLocation(path) != location) set(path, location)
        }
        Manager.saveConfig()
    }}
}