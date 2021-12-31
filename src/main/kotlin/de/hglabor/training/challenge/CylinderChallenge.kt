package de.hglabor.training.challenge

import com.sk89q.worldedit.regions.CylinderRegion
import com.sk89q.worldedit.regions.Region
import de.hglabor.training.serialization.CylinderRegionSerializer
import de.hglabor.training.serialization.WorldSerializer
import de.hglabor.utils.kutils.*
import kotlinx.serialization.Serializable
import org.bukkit.Material

private val DEFAULT_CENTER = world("mlg")!!.spawnLocation
private const val DEFAULT_RADIUS = 30

@Serializable
sealed class CylinderChallenge(
    private val floor: Material = Material.GOLD_BLOCK,
    private val wall: Material = Material.IRON_BLOCK,
    private val ceiling: Material = Material.BARRIER,
    private val bottomY: Int = 0,
    private val topY: Int = 255,
) :
    Challenge() {
    @Serializable(with = WorldSerializer::class)
    final override val world = world("mlg")!!
    val cylinderRegion get() = region as CylinderRegion

    @Serializable(with = CylinderRegionSerializer::class)
    override val region: Region = CylinderRegion(world.we(), DEFAULT_CENTER.we(), DEFAULT_RADIUS.vector2(), bottomY, topY)

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