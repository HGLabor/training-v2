package de.hglabor.training.challenge

import com.sk89q.worldedit.regions.CylinderRegion
import de.hglabor.training.main.Manager
import de.hglabor.training.utils.extensions.*
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

open class CylinderChallenge(
    name: String,
    world: World = world("mlg")!!,
    private val floor: Material = Material.GOLD_BLOCK,
    private val wall: Material = Material.IRON_BLOCK,
    private val ceiling: Material = Material.BARRIER,
    bottomY: Int = 0,
    topY: Int = 255,
) :
    Challenge(name, world, CylinderRegion(
        world.we(),
        center(name)!!.we(),
        radius(name).vector2(),
        bottomY, topY
    )) {
    val cylinderRegion get() = region as CylinderRegion

    override fun start() {
        worldEdit.editSession(world) {
            // Wall
            cylinder(cylinderRegion.floor, wall, filled = false, firstAir = true, height = 255)
            // Floor
            cylinder(cylinderRegion.floor, floor, height = 1)
            // Ceiling
            cylinder(cylinderRegion.ceiling, ceiling, height = 1)
        }
    }
}