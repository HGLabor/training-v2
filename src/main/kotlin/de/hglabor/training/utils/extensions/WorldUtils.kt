package de.hglabor.training.utils.extensions

import net.axay.kspigot.extensions.geometry.add
import org.bukkit.*
import org.bukkit.block.Block

fun world(worldName: String) = Bukkit.getWorld(worldName)

@JvmName("blocksBetweenExtension")
fun World.blocksBetween(x1: Int, x2: Int, z1: Int, z2: Int, y1: Int = 50, y2: Int = 150) = blocksBetween(this, x1, x2, z1, z2, y1, y2)

fun blocksBetween(world: World, x1: Int, x2: Int, z1: Int, z2: Int, y1: Int = 50, y2: Int = 150): Set<Block> {
    val blocks = java.util.HashSet<Block>()
    for (x in x1..x2) {
        for (y in y1..y2) {
            for (z in z1..z2) {
                blocks.add(world.getBlockAt(x, y, z))
            }
        }
    }
    return blocks
}

fun World.trainingGameRules(): World {
    time = 6000
    setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
    setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
    setGameRule(GameRule.DO_WEATHER_CYCLE, false)
    setGameRule(GameRule.DO_MOB_SPAWNING, false)
    setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)
    setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
    return this
}

fun Location.addY(y: Number) = this.clone().add(0, y, 0)

fun Set<Block>.scanFor(material: Material): Set<Block> {
    val blocks = HashSet<Block>()
    this.forEach {
        if (it.type == material) blocks.add(it)
    }
    return blocks
}