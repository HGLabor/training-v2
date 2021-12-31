package de.hglabor.training.challenge

import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.regions.Region
import de.hglabor.utils.kutils.we
import de.hglabor.utils.kutils.world
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.World

private val defaultPos = listOf(Location(null, 4.0, 64.0, 6.0), Location(null, 10.0, 65.0, 12.0))

@Serializable
sealed class CuboidChallenge : Challenge() {
    override val world: World = world("world")!!
    val cuboidRegion get() = region as CuboidRegion
    // Default region
    override val region: Region by lazy { CuboidRegion(world.we(), defaultPos[0].we(), defaultPos[1].we()) }
}