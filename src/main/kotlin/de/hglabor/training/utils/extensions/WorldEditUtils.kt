package de.hglabor.training.utils.extensions

import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.math.Vector3
import de.hglabor.training.challenge.Challenge
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

fun World.we() = BukkitWorld(this)
fun Location.we(): BlockVector3 = BlockVector3.at(x, y, z)
fun Vector3.bukkit(): Location = Location(null, x, y, z)
fun BlockVector3.bukkit(): Location = Location(null, x.toDouble(), y.toDouble(), z.toDouble())

infix fun Player.inRegion(challenge: Challenge?) = challenge?.region?.contains(location.we()) ?: false