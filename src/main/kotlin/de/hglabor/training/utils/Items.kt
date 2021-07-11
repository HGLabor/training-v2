package de.hglabor.training.utils

import de.hglabor.training.challenge.challenge
import de.hglabor.training.utils.extensions.cancel
import de.hglabor.training.utils.extensions.clearInv
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.feedSaturate
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

fun namedItem(material: Material, name: String): ItemStack {
    return itemStack(material) {
        meta { this.name = name }
    }
}

val WARPS = namedItem(Material.NETHER_STAR, "${KColors.AQUA}${KColors.BOLD}Warps")
val HUB = namedItem(Material.HEART_OF_THE_SEA, "${KColors.GOLD}${KColors.BOLD}Hub")
val RESPAWN_ANCHOR = namedItem(Material.RESPAWN_ANCHOR,"${KColors.GREEN}Left click = new spawn ${KColors.RESET}| ${KColors.YELLOW}Right click = reset")
val SETTINGS = namedItem(Material.COMPARATOR,"${KColors.GRAY}${KColors.BOLD}Settings")

val WARP_ITEMS = listOf(WARPS, HUB, RESPAWN_ANCHOR, SETTINGS)
val LOCATIONS =  listOf(0,     7,   8,              17)

fun Player.renewInv() {
    clearInv()
    feedSaturate()
    health = healthScale
    if (challenge?.warpItems != false) LOCATIONS.forEachIndexed { index, location -> inventory.setItem(location, WARP_ITEMS[index]) }
}

fun ItemStack?.isWarpItem() = WARP_ITEMS.any { this?.isSimilar(it) ?: return@any false }

fun itemsListener() {
    listen<InventoryClickEvent> { with(it) {
        if (hotbarButton in LOCATIONS) {
            cancel()
            return@listen
        }
        if (currentItem.isWarpItem()) {
            cancel()
            if (isRightClick && currentItem == SETTINGS) {
                whoClicked.sendMessage("settings")
                // TODO open settings
            }
        }
    }}
    listen<PlayerInteractEvent> { with (it) {
        if (!item.isWarpItem()) return@listen
        cancel()
        if (it is PlayerInteractAtEntityEvent || hand == EquipmentSlot.OFF_HAND) return@listen
        when (item) {
            WARPS -> if (isRightClick) player.sendMessage("warps") // TODO open warps gui
            HUB -> if (isRightClick) player.sendMessage("hub") // TODO send player to lobby
            RESPAWN_ANCHOR -> {
                // TODO set/reset respawn point
                if (isRightClick) player.sendMessage("respawn anchor right click")
                else if (isLeftClick) player.sendMessage("respawn anchor left click")
            }
        }
    }}
    listen<PlayerDropItemEvent> { with(it) {
        if (itemDrop.itemStack.isWarpItem()) cancel()
    }}
}

private val PlayerInteractEvent.isRightClick get() = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK
private val PlayerInteractEvent.isLeftClick get() = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK