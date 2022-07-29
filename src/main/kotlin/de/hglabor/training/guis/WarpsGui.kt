package de.hglabor.training.guis

import de.hglabor.training.challenge.AimTraining
import de.hglabor.training.challenge.CraftingChallenge
import de.hglabor.training.challenge.challenge
import de.hglabor.training.events.updateChallengeIfSurvival
import de.hglabor.utils.kutils.cancel
import de.hglabor.utils.kutils.location
import de.hglabor.utils.kutils.namedItem
import de.hglabor.utils.kutils.world
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.chat.literalText
import net.axay.kspigot.gui.GUIType
import net.axay.kspigot.gui.Slots
import net.axay.kspigot.gui.elements.GUIRectSpaceCompound
import net.axay.kspigot.gui.kSpigotGUI
import net.axay.kspigot.gui.openGUI
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

val DAMAGER = namedItem(Material.STONE_SWORD, literalText("Damager") { color = KColors.ORANGERED })
val MLG = namedItem(Material.WATER_BUCKET, literalText("Mlg") { color = KColors.AQUA })
val AIM_TRAINING = namedItem(Material.BOW, literalText("Aim Training") { color = KColors.WHITE })
val CRAFTING = namedItem(Material.CRAFTING_TABLE, literalText("Crafting") { color = KColors.SADDLEBROWN })
val PARKOUR = namedItem(Material.DIAMOND_BOOTS, literalText("Jump And Run") { color = KColors.BLUE })

fun Player.openWarpsGUI() = openGUI(kSpigotGUI(GUIType.THREE_BY_NINE) {
    title = literalText("Warps") { color = KColors.AQUA }

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
                         clickEvent.player.teleport((challenge("aimtraining") as AimTraining).spawnLocation)
                         clickEvent.player.inventory.heldItemSlot = 4
                     }
                     CRAFTING -> {
                         clickEvent.player.teleport((challenge("crafting") as CraftingChallenge).spawnLocation)
                     }
                     PARKOUR -> {
                         val parkour = challenge("parkour")!!
                         clickEvent.player.teleport(parkour.region.center.location().apply { world = parkour.world })
                     }
                }
                clickEvent.player.updateChallengeIfSurvival()
            }
        )

        compound.addContent(listOf(DAMAGER, /*MLG,*/ AIM_TRAINING, CRAFTING, PARKOUR))
    }
})