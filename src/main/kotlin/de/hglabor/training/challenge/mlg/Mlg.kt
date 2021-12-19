package de.hglabor.training.challenge.mlg

import com.sk89q.worldedit.math.Vector2
import de.hglabor.training.challenge.CylinderChallenge
import de.hglabor.utils.kutils.*
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Pig
import kotlin.reflect.KClass

class Mlg(
    name: String,
    private val platformMaterial: Material = Material.SMOOTH_QUARTZ,
    private val platformHeights: List<Int> = listOf(25, 50, 100, 150, 200, 250),
    private val platformRadius: Double = 10.0,
    val warpEntity: KClass<out Entity> = Pig::class
) : CylinderChallenge(name, world("mlg")!!) {
    override val displayName: String get() = "$name Mlg"

    val spawn get() = cylinderRegion.center.location()

    override fun start() {
        super.start()
        // Platforms
        worldEdit.editSession(world) {
            platformHeights.forEach {
                cylinder(platformRegion(it), platformMaterial, height = 1)
            }
        }
    }

    private fun platformRegion(y: Int) = cylinderRegion.clone().apply {
        radius = Vector2.at(platformRadius, platformRadius)
        minimumY = y
        maximumY = y
    }
}