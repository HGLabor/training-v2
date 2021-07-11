package de.hglabor.training.utils.extensions

import net.axay.kspigot.extensions.geometry.subtract
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.ArmorStand

class Hologram : ArrayList<ArmorStand>() {
    fun remove() {
        forEach { it.remove() }
    }
}

fun hologram(location: Location, vararg lines: String, world: World = location.world!!): Hologram {
    val hologram = Hologram()
    lines.forEachIndexed { index, text ->
        val armorStand = world.spawn(location.clone().subtract(0, index*0.25, 0), ArmorStand::class.java)
        armorStand.statueAttributes()
        armorStand.isMarker = true
        armorStand.isInvisible = true
        armorStand.customName = text
        hologram.add(armorStand)
    }
    return hologram
}