package de.hglabor.training.mechanics

import de.hglabor.training.utils.extensions.stack
import net.axay.kspigot.extensions.bukkit.feed
import net.axay.kspigot.extensions.bukkit.heal
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

val soups = listOf(Material.MUSHROOM_STEW, Material.SUSPICIOUS_STEW)
val bowl = Material.BOWL
const val healAmount = 7
const val feedAmount = 6

fun PlayerInteractEvent.checkSoupMechanic(): Boolean {
    return if (action.name.contains("right_click", ignoreCase = true) && hand != EquipmentSlot.OFF_HAND && material in soups) {
        with(player) {
            var used = false
            // Heal
            if (health != healthScale) {
                if (health + healAmount > healthScale) heal() else health += healAmount
                used = true
            }
            // Feed
            else if (foodLevel != 20) {
                if (foodLevel + feedAmount > 20) feed() else foodLevel + feedAmount
                used = true
            }
            if (used) inventory.setItem(inventory.heldItemSlot, bowl.stack())
            used
        }
    } else false
}