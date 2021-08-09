package de.hglabor.training.utils.extensions

import com.sk89q.worldedit.EditSession
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.math.BlockVector2
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.math.Vector2
import com.sk89q.worldedit.math.Vector3
import com.sk89q.worldedit.regions.CylinderRegion
import com.sk89q.worldedit.world.block.BlockState
import de.hglabor.training.challenge.Challenge
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player


fun World.we() = BukkitWorld(this)
fun Location.we(): BlockVector3 = BlockVector3.at(x, y, z)
fun Location.bv2(): BlockVector2 = BlockVector2.at(x, z)
fun Vector3.bukkit(): Location = Location(null, x, y, z)
fun BlockVector3.bukkit(): Location = Location(null, x.toDouble(), y.toDouble(), z.toDouble())
// Radius
fun Number.vector2(): Vector2 = Vector2.at(this.toDouble(), this.toDouble())
fun Material.defaultPattern(): BlockState = BukkitAdapter.asBlockState(stack())

infix fun Player.inRegion(challenge: Challenge?) = challenge?.world == world && challenge.region.contains(location.we())

val worldEdit: WorldEdit get() = WorldEdit.getInstance()

/** Create a new `EditSession` and then flush the queue after running the `block` */
@Suppress("DEPRECATION")
inline fun WorldEdit.editSession(world: World, maxBlocks: Int = -1, block: EditSession.() -> Unit) = editSessionFactory.getEditSession(world.we(), maxBlocks).apply(block).flushQueue()

fun EditSession.cylinder(region: CylinderRegion, material: Material, filled: Boolean = true, firstAir: Boolean = false, height: Int = region.height) {
    if (firstAir) makeCylinder(region.center.toBlockPoint(), Material.AIR.defaultPattern(), region.radius.x, region.radius.z, height, true)
    makeCylinder(region.center.toBlockPoint(), material.defaultPattern(), region.radius.x, region.radius.z, height, filled)
}

val CylinderRegion.floor get() = clone().apply { maximumY = minimumY }
val CylinderRegion.ceiling get() = clone().apply { minimumY = maximumY }