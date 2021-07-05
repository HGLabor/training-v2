package de.hglabor.training.utils

import net.axay.kspigot.extensions.geometry.add
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

fun Entity.isCreative(): Boolean = this is Player && this.gameMode == GameMode.CREATIVE

fun Cancellable.cancel() { this.isCancelled = true }

fun Player.clearInv() {
    this.inventory.clear()
}

fun Player.noMove(seconds: Int) {
    this.addPotionEffect(PotionEffect(PotionEffectType.SLOW, seconds*20, 6, true, false))
    this.addPotionEffect(PotionEffect(PotionEffectType.JUMP, seconds*20, 250, true, false))
}

fun Player.closeAndClearInv() {
    this.closeInventory()
    this.clearInv()
}

fun List<ItemStack>.addToInv(player: Player) { player.addToInv(this) }

fun Player.addToInv(items: List<ItemStack>)   { items.forEach { this.inventory.addItem(it) } }
fun Player.playPlingSound(pitch: Number = 1) = this.playSound(this.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1F, pitch.toFloat())
fun Player.playSound(sound: Sound, pitch: Number = 1, volume: Number = 1, location: Location = this.location) = playSound(location, sound, volume.toFloat(), pitch.toFloat())

fun HumanEntity.survival() { this.gameMode = GameMode.SURVIVAL }
fun HumanEntity.spectator() { this.gameMode = GameMode.SPECTATOR }

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

fun World.trainingGameRules(): World {
    time = 6000
    setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
    setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
    setGameRule(GameRule.DO_WEATHER_CYCLE, false)
    setGameRule(GameRule.DO_MOB_SPAWNING, false)
    setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)
    setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
    return this
}

// Easy string colors

fun String.col(vararg colorNames: String): String {
    var prefix = ""
    colorNames.forEach { prefix += colorFromName(it) }
    return prefix + this + ChatColor.RESET.toString() + ChatColor.WHITE.toString()
}

fun colorFromName(name: String): ChatColor = ChatColor.valueOf(name.uppercase())

fun Location.addY(y: Number) = this.clone().add(0, y, 0)

fun Set<Block>.scanFor(material: Material): Set<Block> {
    val blocks = HashSet<Block>()
    this.forEach {
        if (it.type == material) blocks.add(it)
    }
    return blocks
}

@JvmName("blocksBetweenExtension")
fun World.blocksBetween(x1: Int, x2: Int, z1: Int, z2: Int, y1: Int = 50, y2: Int = 150) = blocksBetween(this, x1, x2, z1, z2, y1, y2)

fun blocksBetween(world: World, x1: Int, x2: Int, z1: Int, z2: Int, y1: Int = 50, y2: Int = 150): Set<Block> {
    val blocks = java.util.HashSet<Block>()
    for (x in x1..x2) {
        for (y in y1..y2) {
            for (z in z1..z2) {
                blocks.add(world.getBlockAt(x, y, z))
            }
        }
    }
    return blocks
}

fun MutableList<ItemStack>.add(material: Material, amount: Int = 1) = add(ItemStack(material, amount))
fun MutableList<ItemStack>.addAll(vararg items: Any) {
    items.forEach {
        if (it is Material) add(it)
        else if (it is ItemStack) add(it)
    }
}