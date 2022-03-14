package de.hglabor.training

import de.dytanic.cloudnet.driver.CloudNetDriver
import de.dytanic.cloudnet.ext.bridge.player.IPlayerManager
import de.hglabor.training.challenge.challenge
import de.hglabor.training.guis.openWarpsGUI
import de.hglabor.training.main.PREFIX
import de.hglabor.utils.kutils.*
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.feedSaturate
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

val WARPS = namedItem(Material.NETHER_STAR, "${KColors.AQUA}${KColors.BOLD}Warps")
val HUB = namedItem(Material.HEART_OF_THE_SEA, "${KColors.GOLD}${KColors.BOLD}Hub")
val RESPAWN_ANCHOR = namedItem(Material.RESPAWN_ANCHOR,"${KColors.GREEN}Left click = new spawn ${KColors.RESET}| ${KColors.YELLOW}Right click = reset")
val SETTINGS = namedItem(Material.COMPARATOR,"${KColors.GRAY}${KColors.BOLD}Settings")

val WARP_ITEMS =           listOf(WARPS, HUB, RESPAWN_ANCHOR, SETTINGS)
val WARP_ITEM_LOCATIONS =  listOf(0,     7,   8,              17)

private val playerManager = CloudNetDriver.getInstance().servicesRegistry.getFirstService(IPlayerManager::class.java)

fun Player.defaultInv() {
    closeAndClearInv()
    feedSaturate()
    fireTicks = 0
    health = healthScale
    if (challenge?.warpItems != false) WARP_ITEM_LOCATIONS.forEachIndexed { index, location -> inventory.setItem(location, WARP_ITEMS[index]) }
}

fun ItemStack?.isWarpItem() = WARP_ITEMS.any { this?.isSimilar(it) ?: return@any false }

fun itemsListener() {
    listen<InventoryClickEvent> { with(it) {
        if (hotbarButton in WARP_ITEM_LOCATIONS) {
            cancel()
            return@listen
        }
        if (currentItem.isWarpItem()) {
            cancel()
            if (isRightClick && currentItem == SETTINGS) {
                whoClicked.sendMessage("nö")
                // TODO open settings
            }
        }
    }}
    listen<PlayerInteractEvent> { with (it) {
        if (!item.isWarpItem()) return@listen
        cancel()
        if (hand == EquipmentSlot.OFF_HAND) return@listen
        with (player) {
            when (item) {
                WARPS -> if (isRightClick) openWarpsGUI()
                HUB -> if (isRightClick) playerManager.getPlayerExecutor(player?.uniqueId ?: return@listen).connectToFallback()
                RESPAWN_ANCHOR -> {
                    if (isRightClick) {
                        bedSpawnLocation = player?.location?.world?.spawnLocation
                        sendMessage("$PREFIX ${KColors.YELLOW}Reset respawn location.")
                    }
                    else if (isLeftClick) {
                        if (challenge?.allowRespawnLocation(location) != false) {
                            setBedSpawnLocation(location, true)
                            sendMessage("$PREFIX ${KColors.GREEN}Updated respawn location.")
                        }
                        else sendMessage("$PREFIX ${KColors.RED}Here you can't update your respawn location.")
                    }
                }
            }
        }
    }}
    listen<PlayerDropItemEvent> { with(it) {
        if (itemDrop.itemStack.isWarpItem() || (player.challenge == null && player.gameMode == GameMode.SURVIVAL)) cancel()
    }}
}
