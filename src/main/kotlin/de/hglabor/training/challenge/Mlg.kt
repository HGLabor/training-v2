package de.hglabor.training.challenge

import com.sk89q.worldedit.math.Vector2
import de.hglabor.training.events.updateChallengeIfSurvival
import de.hglabor.training.serialization.ChatColorSerializer
import de.hglabor.utils.kutils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.axay.kspigot.event.listen
import net.axay.kspigot.runnables.taskRunLater
import net.md_5.bungee.api.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerMoveEvent

@SerialName("mlg")
@Serializable
sealed class Mlg(
    override val name: String,
    @Serializable(with = ChatColorSerializer::class) override val color: ChatColor,
    private val platformMaterial: Material = Material.SMOOTH_QUARTZ,
    private val platformHeights: List<Int> = listOf(25, 50, 100, 150, 200, 250),
    private val platformRadius: Double = 10.0,
    private val warpEntityType: EntityType = EntityType.PIG
) : CylinderChallenge() {
    private val bottomArea = cylinderRegion.minimumY + 5

    init {
        challengePlayerEvent<EntityDamageEvent> {
            cancel()
            if (entity.location.y <= bottomArea && damage > 0.0) (entity as Player).fail()
        }
        challengePlayerEvent<PlayerMoveEvent> {
            if (from.y > bottomArea-1 && to.y <= bottomArea-1) taskRunLater(10) {
                // Check if player still lives
                if (player inChallenge this@Mlg && player.location.y <= bottomArea) player.complete()
            }
        }
    }

    override fun Player.tpSpawn() {
        // If the bed spawn location is in this mlg, teleport the player there.
        // Otherwise, teleport the player to the spawn location of this mlg.
        teleport(if(bedSpawnLocation?.inRegion(this@Mlg) == true) bedSpawnLocation!! else spawn)
    }

    abstract val mlgItems: List<Material>
    override val displayName get() = "$name Mlg"
    override fun allowRespawnLocation(location: Location) = location.y > 10 && !location.blockBelow.isEmpty

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
                it.player.apply { teleport(spawn); updateChallengeIfSurvival() }
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

    override fun renewInv(player: Player) {
        if (mlgItems.isEmpty() || mlgItems.size > 6) return
        player.setItems(when (mlgItems.size) {
            1 -> 4
            2, 3 -> 3
            4 -> 2
            else -> 1
        }, mlgItems)
    }

    private fun Player.setItems(startSlot: Int, items: List<Material>) {
        for (slot in items.indices) inventory.setItem(slot+startSlot, items[slot].stack())
    }

    private fun platformRegion(y: Int) = cylinderRegion.clone().apply {
        radius = Vector2.at(platformRadius, platformRadius)
        minimumY = y
        maximumY = y
    }
}