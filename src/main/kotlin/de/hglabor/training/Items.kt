package de.hglabor.training

import de.hglabor.training.challenge.challenge
import de.hglabor.training.events.updateChallengeIfSurvival
import de.hglabor.training.guis.openWarpsGUI
import de.hglabor.training.utils.sendSimpleMessage
import de.hglabor.utils.kutils.*
import eu.cloudnetservice.driver.CloudNetDriver
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.chat.literalText
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.feedSaturate
import net.axay.kspigot.extensions.bukkit.sendToServer
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

val WARPS = namedItem(Material.NETHER_STAR, literalText("Warps") { color = KColors.AQUA; bold = true })
val HUB = namedItem(Material.HEART_OF_THE_SEA, literalText("Hub") { color = KColors.GOLD; bold = true })
val RESPAWN_ANCHOR = namedItem(Material.RESPAWN_ANCHOR, literalText {
    text("Left click = new spawn ") { color = KColors.GREEN }
    text("|")
    text(" Right click = reset") { color = KColors.YELLOW }
})
val SETTINGS = namedItem(Material.COMPARATOR, literalText("Settings") { color = KColors.GRAY; bold = true })

val WARP_ITEMS =           listOf(WARPS, HUB, RESPAWN_ANCHOR, SETTINGS)
val WARP_ITEM_LOCATIONS =  listOf(0,     7,   8,              17)

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
        if (whoClicked !is Player) return@listen

        if ((whoClicked as Player).challenge?.warpItems != false && hotbarButton in WARP_ITEM_LOCATIONS) {
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
                HUB -> if (isRightClick) {
                    val lobbys = CloudNetDriver.instance<CloudNetDriver>().cloudServiceProvider().servicesByGroup("lobby")
                    sendToServer(lobbys.random().name())
                }
                RESPAWN_ANCHOR -> {
                    if (isRightClick) {
                        setBedSpawnLocation(location.world.spawnLocation, true)
                        sendSimpleMessage("Reset respawn location", KColors.YELLOW)
                    }
                    else if (isLeftClick) {
                        updateChallengeIfSurvival()
                        if (challenge?.allowRespawnLocation(location) != false) {
                            setBedSpawnLocation(location, true)
                            sendSimpleMessage("Updated respawn location", KColors.GREEN)
                        }
                        else sendSimpleMessage("Here you can't update your respawn location.", KColors.RED)
                    }
                }
            }
        }
    }}
    listen<PlayerDropItemEvent> { with(it) {
        if (itemDrop.itemStack.isWarpItem() || ((player.challenge == null || !player.challenge!!.allowDrop) && player.gameMode == GameMode.SURVIVAL)) cancel()
    }}
}
