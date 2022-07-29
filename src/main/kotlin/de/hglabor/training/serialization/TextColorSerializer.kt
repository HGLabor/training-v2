package de.hglabor.training.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.axay.kspigot.chat.KColors
import net.kyori.adventure.text.format.TextColor

object TextColorSerializer : KSerializer<TextColor> {
    override val descriptor = PrimitiveSerialDescriptor("TextColor", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): TextColor = TextColor.fromHexString(decoder.decodeString()) ?: KColors.WHITE

    override fun serialize(encoder: Encoder, value: TextColor) = encoder.encodeString(value.asHexString())
}