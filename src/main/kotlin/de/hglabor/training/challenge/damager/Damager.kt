package de.hglabor.training.challenge.damager

import de.hglabor.training.challenge.CuboidChallenge
import de.hglabor.training.mechanics.checkSoupMechanic
import de.hglabor.training.utils.extensions.*
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.extensions.geometry.add
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import net.axay.kspigot.runnables.taskRunLater
import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent

private const val DEFAULT_PERIOD = 10L
private const val DEFAULT_DAMAGE = 4.0

class Damager(name: String, color: ChatColor = KColors.WHITE, private val period: Long = DEFAULT_PERIOD, private val damage: Double = DEFAULT_DAMAGE) : CuboidChallenge(name, color = color) {
    private var task: KSpigotRunnable? = null
    private var hologram: Hologram? = null
    override val displayName get() = "$name Damager"

    init {
        challengePlayerEvent<PlayerInteractEvent> {
            // TODO increase soups eaten - check if finished
            if (checkSoupMechanic()) Unit
        }
        challengePlayerEvent<PlayerDropItemEvent> {
            taskRunLater(20L) {
                if (!(itemDrop.location inRegion this@Damager)) itemDrop.remove()
            }
        }
    }

    override fun start() {
        super.start()

        val holoLoc = cuboidRegion.center.bukkit().clone().add(0, 2, 0)
        hologram = hologram(holoLoc, "$color$displayName", "Damage: ${KColors.GOLD}${damage/2} ${KColors.RED}\u2764", "Period: ${KColors.GOLD}$period", world = world)
        task = task(period = period) {
            if(it.isCancelled) return@task
            players {
                if (health - damage == 0.0) fail()
                damage(damage)
            }
        }
    }

    override fun onEnter(player: Player) {
        player.saturation = 0F
        with(player.inventory) {
            setItem(0, Material.STONE_SWORD.stack())
            for (i in 1..35) setItem(i, Material.MUSHROOM_STEW.stack())
            setItem(13, Material.BOWL.stack(64))
            // TODO cocoa recraft
            setItem(14, Material.RED_MUSHROOM.stack(64))
            setItem(15, Material.BROWN_MUSHROOM.stack(64))
        }
    }

    override fun stop() {
        super.stop()
        task?.cancel()
        hologram?.remove()
    }

    override val hunger = true
    override val warpItems = false
}