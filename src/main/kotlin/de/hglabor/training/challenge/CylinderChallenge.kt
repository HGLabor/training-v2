package de.hglabor.training.challenge

import com.sk89q.worldedit.regions.CylinderRegion
import com.sk89q.worldedit.regions.Region
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
    world: World,
    private val floor: Material = Material.GOLD_BLOCK,
    private val wall: Material = Material.IRON_BLOCK,
    private val ceiling: Material = Material.BARRIER,
    private val bottomY: Int = 0,
    private val topY: Int = 255,
) :
    Challenge(name, world) {
    val cylinderRegion get() = region as CylinderRegion

    override lateinit var region: Region

    override fun start() {
        // Get region from config
        region = CylinderRegion(world.we(), center(name)!!.we(), radius(name).vector2(), bottomY, topY)

        worldEdit.editSession(world) {
            // Wall
            cylinder(cylinderRegion.floor, wall, filled = false, firstAir = true, height = 255)
            // Floor
            cylinder(cylinderRegion.floor, floor, height = 1)
            // Ceiling
            cylinder(cylinderRegion.ceiling, ceiling, height = 1)
        }
    }

    override fun saveToConfig() = with(Manager.config) {
        // TODO Save region to config
        set("challenge.$name.region.radius", cylinderRegion.radius.x.toInt())
        set("challenge.$name.region.center", cylinderRegion.center.bukkit())
    }
}