package de.hglabor.training.utils.extensions

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

@Suppress("DEPRECATION")
fun cylinder(world: World, region: CylinderRegion, material: Material, filled: Boolean = true, firstAir: Boolean = false) {
    val session = worldEdit.editSessionFactory.getEditSession(world.we(), 1000000)
    if (firstAir) session.makeCylinder(region.center.toBlockPoint(), Material.AIR.defaultPattern(), region.radius.x, region.radius.z, region.height, true)
    session.makeCylinder(region.center.toBlockPoint(), material.defaultPattern(), region.radius.x, region.radius.z, region.height, filled)
    session.flushQueue()
}
