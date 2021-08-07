package de.hglabor.training.challenge

import com.sk89q.worldedit.regions.CylinderRegion
import de.hglabor.training.main.Manager
import de.hglabor.training.utils.extensions.cylinder
import de.hglabor.training.utils.extensions.vector2
import de.hglabor.training.utils.extensions.we
import de.hglabor.training.utils.extensions.world
import net.axay.kspigot.extensions.broadcast
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World

private val DEFAULT_CENTER = world("mlg")!!.spawnLocation
private const val DEFAULT_RADIUS = 30

private fun center(name: String): Location? { with(Manager.config) {
    val path = "challenge.$name.region.center"
    addDefault(path, DEFAULT_CENTER)
    Manager.saveConfig()
    return getLocation(path)
}}

private fun radius(name: String): Int { with(Manager.config) {
    val path = "challenge.$name.region.radius"
    addDefault(path, DEFAULT_RADIUS)
    Manager.saveConfig()
    return getInt(path)
}}

open class CylinderChallenge(name: String, world: World = world("mlg")!!) :
    Challenge(name, world, CylinderRegion(
        world.we(),
        center(name)!!.we(),
        radius(name).vector2(),
        50, 90
    )) {
    val cylinderRegion get() = region as CylinderRegion

    override fun start() {
        broadcast("start cylinder region")
        cylinder(world, cylinderRegion, Material.DIAMOND_BLOCK, filled = false, firstAir = true)
    }
}