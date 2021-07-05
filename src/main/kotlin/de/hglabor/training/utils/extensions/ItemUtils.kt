package de.hglabor.training.utils.extensions

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

fun MutableList<ItemStack>.add(material: Material, amount: Int = 1) = add(ItemStack(material, amount))
fun MutableList<ItemStack>.addAll(vararg items: Any) {
    items.forEach {
        if (it is Material) add(it)
        else if (it is ItemStack) add(it)
    }
}

fun List<ItemStack>.addToInv(player: Player) { player.addToInv(this) }

fun Material.stack(amount: Int = 1): ItemStack = ItemStack(this, amount)
fun List<Material>.stack(): List<ItemStack> {
    val itemStacks = ArrayList<ItemStack>()
    forEach { itemStacks.add(it.stack()) }
    return itemStacks
}
fun Inventory.addAll(items: List<ItemStack>) = items.forEach { this.addItem(it) }
fun List<String>.materials(): List<Material> {
    val list = ArrayList<Material>()
    forEach {
        list.add(Material.getMaterial(it.uppercase()) ?: return@forEach)
    }
    return list
}