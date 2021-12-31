package de.hglabor.training.challenge

import com.sk89q.worldedit.math.Vector2
import de.hglabor.training.events.updateChallengeIfSurvival
import de.hglabor.utils.kutils.*
import de.hglabor.utils.kutils.serialization.ChatColorSerializer
import kotlinx.serialization.Serializable
import net.axay.kspigot.event.listen
import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.event.player.PlayerInteractAtEntityEvent

@Serializable
class Mlg(
    override val name: String,
    @Serializable(with = ChatColorSerializer::class) override val color: ChatColor,
    override val world: World = world("mlg")!!,
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

    lateinit var warpEntity: Entity
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

    private fun platformRegion(y: Int) = cylinderRegion.clone().apply {
        radius = Vector2.at(platformRadius, platformRadius)
        minimumY = y
        maximumY = y
    }
}