package de.hglabor.training.guis

import de.hglabor.training.events.updateChallengeIfSurvival
import de.hglabor.utils.kutils.cancel
import de.hglabor.utils.kutils.namedItem
import de.hglabor.utils.kutils.world
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.gui.GUIType
import net.axay.kspigot.gui.Slots
import net.axay.kspigot.gui.elements.GUIRectSpaceCompound
import net.axay.kspigot.gui.kSpigotGUI
import net.axay.kspigot.gui.openGUI
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

val DAMAGER = namedItem(Material.STONE_SWORD, "${KColors.ORANGERED}Damager")
val MLG = namedItem(Material.WATER_BUCKET, "${KColors.AQUA}Mlg")
val AIM_TRAINING = namedItem(Material.BOW, "${KColors.WHITE}Aim Training")
val CRAFTING = namedItem(Material.CRAFTING_TABLE, "${KColors.SADDLEBROWN}Crafting")
val PARKOUR = namedItem(Material.DIAMOND_BOOTS, "${KColors.BLUE}Jump And Run")

fun Player.openWarpsGUI() = openGUI(kSpigotGUI(GUIType.THREE_BY_NINE) {
    title = "${KColors.AQUA}Warps"

    page(1) {
        placeholder(Slots.Border, ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE))

        lateinit var compound: GUIRectSpaceCompound<*, ItemStack>
        compound = createRectCompound(
            Slots.RowTwoSlotTwo,
            Slots.RowTwoSlotEight,
            iconGenerator = { it },

            onClick = { clickEvent, element ->
                clickEvent.bukkitEvent.cancel()
                // TODO configurable
                when(element) {
                     DAMAGER -> clickEvent.player.teleport(world("world")!!.spawnLocation)
                     MLG -> clickEvent.player.teleport(world("mlg")!!.spawnLocation)
                     AIM_TRAINING -> {
                         clickEvent.player.teleport(Location(world("world"), 44.5, 64.0, -40.5, -90f, -30f))
                         clickEvent.player.inventory.heldItemSlot = 4
                     }
                     CRAFTING -> {
                         clickEvent.player.teleport(Location(world("world"), 27.5, 64.0, -0.5, -90f, 20f))
                     }
                     PARKOUR -> {
                         clickEvent.player.teleport(Location(world("world"), -10.5, 64.0, -20.5, -180f, 0f))
                     }
                }
                clickEvent.player.updateChallengeIfSurvival()
            }
        )

        compound.addContent(listOf(DAMAGER, /*MLG,*/ AIM_TRAINING, CRAFTING, PARKOUR))
    }
})