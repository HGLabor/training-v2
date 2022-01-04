package de.hglabor.training.serialization

import com.sk89q.worldedit.regions.CuboidRegion
import de.hglabor.utils.kutils.location
import de.hglabor.utils.kutils.we
import de.hglabor.utils.kutils.world
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import net.axay.kspigot.serialization.LocationSerializer
import org.bukkit.Location

object CuboidRegionSerializer : KSerializer<CuboidRegion> {
    override val descriptor = PrimitiveSerialDescriptor("CuboidRegion", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): CuboidRegion {
        val data = Json.decodeFromString<CuboidRegionData>(decoder.decodeString())
        return CuboidRegion(
            world(data.world)!!.we(),
            data.pos1.we(),
            data.pos2.we(),
        )
    }

    override fun serialize(encoder: Encoder, value: CuboidRegion) {
        encoder.encodeString(Json.encodeToString(CuboidRegionData(value.world!!.name, value.pos1.location(), value.pos2.location())))
    }
}

@Serializable
private data class CuboidRegionData(val world: String, @Serializable(with = LocationSerializer::class) val pos1: Location, @Serializable(with = LocationSerializer::class) val pos2: Location)