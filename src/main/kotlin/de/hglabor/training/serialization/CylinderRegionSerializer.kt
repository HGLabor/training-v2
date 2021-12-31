package de.hglabor.training.serialization

import com.sk89q.worldedit.regions.CylinderRegion
import de.hglabor.utils.kutils.location
import de.hglabor.utils.kutils.vector2
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

object CylinderRegionSerializer : KSerializer<CylinderRegion> {
    override val descriptor = PrimitiveSerialDescriptor("CylinderRegion", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): CylinderRegion {
        val data = Json.decodeFromString<CylinderRegionData>(decoder.decodeString())
        return CylinderRegion(
            world(data.world)!!.we(),
            data.center.we(),
            data.radius.vector2(),
            data.minY,
            data.maxY,
        )
    }

    override fun serialize(encoder: Encoder, value: CylinderRegion) {
        encoder.encodeString(Json.encodeToString(CylinderRegionData(value.world!!.name, value.center.location(), value.radius.x, value.minimumY, value.maximumY)))
    }
}

@Serializable
private data class CylinderRegionData(val world: String, @Serializable(with = LocationSerializer::class) val center: Location, val radius: Double, val minY: Int, val maxY: Int)