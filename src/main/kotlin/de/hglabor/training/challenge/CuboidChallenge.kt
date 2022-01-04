package de.hglabor.training.challenge

import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.regions.Region
import de.hglabor.training.serialization.CuboidRegionSerializer
import de.hglabor.training.serialization.WorldSerializer
import de.hglabor.utils.kutils.we
import de.hglabor.utils.kutils.world
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.World

private val defaultPos = listOf(Location(null, 4.0, 64.0, 6.0), Location(null, 10.0, 65.0, 12.0))

@Serializable
sealed class CuboidChallenge : Challenge() {
    @Serializable(with = WorldSerializer::class)
    final override val world: World = world("world")!!
    val cuboidRegion get() = region as CuboidRegion
    // Default region
    @Serializable(with = CuboidRegionSerializer::class)
    override val region: Region = CuboidRegion(world.we(), defaultPos[0].we(), defaultPos[1].we())
}