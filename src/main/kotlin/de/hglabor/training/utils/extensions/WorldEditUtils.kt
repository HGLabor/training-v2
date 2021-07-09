package de.hglabor.training.utils.extensions

import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.math.BlockVector3
import de.hglabor.training.challenge.Challenge
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

fun World.we() = BukkitWorld(this)
fun Location.we(): BlockVector3 = BlockVector3.at(x, y, z)

infix fun Player.inRegion(challenge: Challenge?) = challenge?.region?.contains(location.we()) ?: false