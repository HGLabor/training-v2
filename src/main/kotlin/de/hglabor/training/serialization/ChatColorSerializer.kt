package de.hglabor.training.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.md_5.bungee.api.ChatColor

object ChatColorSerializer : KSerializer<ChatColor> {
    override val descriptor = PrimitiveSerialDescriptor("ChatColor", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ChatColor = ChatColor.of(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: ChatColor) = encoder.encodeString(value.name)
}