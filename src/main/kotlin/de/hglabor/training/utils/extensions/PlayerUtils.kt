package de.hglabor.training.utils.extensions

import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

inline fun onlinePlayers(block: Player.() -> Unit) = net.axay.kspigot.extensions.onlinePlayers.forEach(block)

fun Entity.isCreative(): Boolean = this is Player && this.gameMode == GameMode.CREATIVE

fun Cancellable.cancel() { this.isCancelled = true }

fun Player.clearInv() {
    inventory.clear()
    setItemOnCursor(null)
}

fun Player.noMove(seconds: Int) {
    this.addPotionEffect(PotionEffect(PotionEffectType.SLOW, seconds*20, 6, true, false))
    this.addPotionEffect(PotionEffect(PotionEffectType.JUMP, seconds*20, 250, true, false))
}

fun Player.closeAndClearInv() {
    this.closeInventory()
    this.clearInv()
}

fun Player.addToInv(items: List<ItemStack>)   { items.forEach { this.inventory.addItem(it) } }
fun Player.playPlingSound(pitch: Number = 1) = this.playSound(this.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1F, pitch.toFloat())
fun Player.playSound(sound: Sound, pitch: Number = 1, volume: Number = 1, location: Location = this.location) = playSound(location, sound, volume.toFloat(), pitch.toFloat())

fun HumanEntity.survival() { this.gameMode = GameMode.SURVIVAL }
fun HumanEntity.spectator() { this.gameMode = GameMode.SPECTATOR }
