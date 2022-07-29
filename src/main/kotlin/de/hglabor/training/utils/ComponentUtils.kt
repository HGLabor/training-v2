package de.hglabor.training.utils

import de.hglabor.training.main.PREFIX
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.chat.literalText
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player

fun Player.sendSimpleMessage(msg: String, mColor: TextColor = KColors.GRAY) = sendMessage(literalText(PREFIX) {
    text(" $msg") { color = mColor }
})