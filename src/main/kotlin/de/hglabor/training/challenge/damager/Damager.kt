package de.hglabor.training.challenge.damager

import de.hglabor.training.challenge.CuboidChallenge
import de.hglabor.training.utils.extensions.Hologram
import de.hglabor.training.utils.extensions.bukkit
import de.hglabor.training.utils.extensions.hologram
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.extensions.geometry.add
import net.axay.kspigot.extensions.geometry.subtract
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player

class Damager(name: String, private val period: Long = 20L, private var damage: Double = 2.0) : CuboidChallenge("$name Damager") {
    var task: KSpigotRunnable? = null
    var hologram: Hologram? = null
    override fun start() {
        val holoLoc = cuboidRegion.center.bukkit().clone().add(0, 2, 0)
        hologram = hologram(holoLoc, "$color$name", "Damage: ${KColors.GOLD}${damage/2} ${KColors.RED}\u2764", "Period: ${KColors.GOLD}$period", world = world)
        task = task(period = period) {
            if(it.isCancelled) return@task
            players { damage(damage) }
        }
    }

    override fun onEnter(player: Player) {
        player.saturation = 0F
    }

    override fun stop() {
        task?.cancel()
        hologram?.remove()
    }

    override val hunger = true
    override val warpItems = false
}