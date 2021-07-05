package de.hglabor.training.utils

import net.axay.kspigot.chat.KColors
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

fun namedItem(material: Material, name: String): ItemStack {
    return itemStack(material) {
        meta { this.name = name }
    }
}

val WARPS = namedItem(Material.NETHER_STAR, "${KColors.GOLD}${KColors.BOLD}Warps")
val HUB = namedItem(Material.HEART_OF_THE_SEA, "${KColors.GOLD}${KColors.BOLD}Hub")
val RESPAWN_ANCHOR = namedItem(Material.RESPAWN_ANCHOR,"${KColors.GREEN}Left click = new spawn ${KColors.RESET}| ${KColors.YELLOW}Right click = reset")
val SETTINGS = namedItem(Material.COMPARATOR,"${KColors.GRAY}${KColors.BOLD}Settings")

val WARP_ITEMS = listOf(WARPS, HUB, RESPAWN_ANCHOR, SETTINGS)

fun Player.renewInv() {
    clearInv()
    with(inventory) {
        setItem(0, WARPS)
        setItem(7, HUB)
        setItem(8, RESPAWN_ANCHOR)
        setItem(17, SETTINGS)
    }
}

fun ItemStack?.isWarpItem() = WARP_ITEMS.any { this?.isSimilar(it) ?: return@any false }

fun itemsListener() {
    // TODO listen inv click etc
}