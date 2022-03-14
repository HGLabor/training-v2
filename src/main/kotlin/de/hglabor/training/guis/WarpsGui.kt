package de.hglabor.training.guis

import de.hglabor.training.challenge.AimTraining
import de.hglabor.training.challenge.challenge
import de.hglabor.training.events.updateChallenge
import de.hglabor.utils.kutils.cancel
import de.hglabor.utils.kutils.world
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.gui.GUIType
import net.axay.kspigot.gui.Slots
import net.axay.kspigot.gui.elements.GUIRectSpaceCompound
import net.axay.kspigot.gui.kSpigotGUI
import net.axay.kspigot.gui.openGUI
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

val DAMAGER = itemStack(Material.STONE_SWORD) {
    meta { name = "${KColors.ORANGERED}Damager" }
}
val MLG = itemStack(Material.WATER_BUCKET) {
    meta { name = "${KColors.AQUA}Mlg" }
}
val AIM_TRAINING = itemStack(Material.BOW) {
    meta { name = "${KColors.WHITE}Aim Training" }
}

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
                when(element) {
                     DAMAGER -> clickEvent.player.teleport(world("world")!!.spawnLocation)
                     MLG -> clickEvent.player.teleport(world("mlg")!!.spawnLocation)
                     AIM_TRAINING -> if (clickEvent.player.challenge !is AimTraining) clickEvent.player.teleport(Location(world("world"), 44.5, 64.0, -40.5, -90f, -20f))
                }
                clickEvent.player.updateChallenge()
            }
        )

        compound.addContent(listOf(DAMAGER, MLG, AIM_TRAINING))
    }
})