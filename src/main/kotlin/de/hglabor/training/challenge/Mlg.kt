package de.hglabor.training.challenge

import com.sk89q.worldedit.math.Vector2
import de.hglabor.training.events.updateChallengeIfSurvival
import de.hglabor.training.serialization.ChatColorSerializer
import de.hglabor.utils.kutils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.axay.kspigot.event.listen
import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.event.player.PlayerInteractAtEntityEvent

@SerialName("mlg")
@Serializable
class Mlg(
    override val name: String,
    @Serializable(with = ChatColorSerializer::class) override val color: ChatColor,
    private val platformMaterial: Material = Material.SMOOTH_QUARTZ,
    private val platformHeights: List<Int> = listOf(25, 50, 100, 150, 200, 250),
    private val platformRadius: Double = 10.0,
    private val warpEntityType: EntityType = EntityType.PIG
) : CylinderChallenge() {
    override val displayName: String get() = "$name Mlg"

    val spawn get() = cylinderRegion.center.location().apply {
        this.world = this@Mlg.world
        this.y = 101.0
    }
    @Transient lateinit var warpEntity: Entity
    override fun start() {
        super.start()
        warpEntity = world.spawnEntity(world.spawnLocation, warpEntityType).apply {
            statueAttributes()
            customName = "$color$displayName"
        }
        listen<PlayerInteractAtEntityEvent> {
            if (it.rightClicked == warpEntity) {
                it.cancel()
                it.player.teleport(spawn)
                it.player.updateChallengeIfSurvival()
            }
        }

        // Platforms
        worldEdit.editSession(world) {
            platformHeights.forEach {
                cylinder(platformRegion(it), platformMaterial, height = 1)
            }
        }
    }

    override fun stop() {
        super.stop()
        warpEntity.remove()
    }

    private fun platformRegion(y: Int) = cylinderRegion.clone().apply {
        radius = Vector2.at(platformRadius, platformRadius)
        minimumY = y
        maximumY = y
    }
}