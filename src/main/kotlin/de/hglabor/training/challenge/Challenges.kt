@file:Suppress("CanBeParameter", "SERIALIZER_TYPE_INCOMPATIBLE")

package de.hglabor.training.challenge

import com.sk89q.worldedit.math.Vector2
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.regions.CylinderRegion
import com.sk89q.worldedit.regions.Region
import de.hglabor.training.WARP_ITEMS
import de.hglabor.training.defaultInv
import de.hglabor.training.events.updateChallengeIfSurvival
import de.hglabor.training.main.PREFIX
import de.hglabor.training.mechanics.checkSoupMechanic
import de.hglabor.training.serialization.*
import de.hglabor.utils.kutils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.actionBar
import net.axay.kspigot.extensions.geometry.add
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import net.axay.kspigot.runnables.taskRunLater
import net.md_5.bungee.api.ChatColor
import org.bukkit.*
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.player.*
import java.util.*

// All in 1 file bc of kotlinx.serialization <3
@Serializable
sealed class Challenge {
    abstract val name: String
    @Serializable(with = WorldSerializer::class)
    abstract val world: World
    @Serializable(with = ChatColorSerializer::class)
    abstract val color: ChatColor

    abstract val region: Region

    @Transient
    val players = HashSet<@Serializable(with = UUIDSerializer::class) UUID>()
    inline fun players(forEach: Player.() -> Unit) {
        players.forEach { forEach(Bukkit.getPlayer(it)!!) }
    }
    open fun start() {}
    open fun stop() {}

    fun restart() { stop(); start() }

    open val displayName get() = name
    internal fun enter(player: Player) {
        players.add(player.uniqueId)
        player.renewInv()
        player.actionBar("${KColors.GREEN}You entered $displayName")
        onEnter(player)
    }
    protected open fun onEnter(player: Player) {}

    internal fun leave(player: Player) {
        players.remove(player.uniqueId)
        player.renewInv()
        player.actionBar("${KColors.RED}You left $displayName")
        onLeave(player)
    }

    override fun toString(): String = "$displayName[center={${region.center.x},${region.center.y},${region.center.z}}]"

    protected open fun onLeave(player: Player) {}

    /**
     * Executes the given [callback] if the player of the
     * [Event] is in this challenge.
     */
    inline fun <reified T : Event> challengePlayerEvent(crossinline callback: T.() -> Unit) {
        listen<T> {
            // Try to reflect directly
            var player = it.reflectMethod<Player>("getPlayer")
            // Try to get from entity event
            if (player == null) if ((it as EntityEvent).entity is Player) player = (it as EntityEvent).entity as Player
            if (player?.challenge == this) {
                it.callback()
            }
        }
    }

    fun Player.fail() {
        sendMessage("$PREFIX ${KColors.RED}You failed ${this@Challenge.displayName}")
        tpSpawn()
        updateChallengeIfSurvival()
        renewInv()
        playSound(Sound.BLOCK_GLASS_BREAK)
    }

    fun Player.complete() {
        sendMessage("$PREFIX ${KColors.GREEN}You completed ${this@Challenge.displayName}")
        tpSpawn()
        updateChallengeIfSurvival()
        renewInv()
        playSound(Sound.ENTITY_ARROW_HIT_PLAYER)
    }

    @JvmName("playerRenewInv")
    fun Player.renewInv() = renewInv(this)
    open fun renewInv(player: Player) = player.defaultInv()
    open fun Player.tpSpawn() {
        teleport((if (bedSpawnLocation?.world == world) bedSpawnLocation else null) ?: world.spawnLocation)
    }
    open fun saveToConfig() {}

    @Transient open val hunger = false
    @Transient open val warpItems = true
    /** @return true if setting the respawn location to this location is allowed */
    open fun allowRespawnLocation(location: Location) = false
}

@Serializable
sealed class CuboidChallenge : Challenge() {
    @Transient private val defaultPos = listOf(Location(null, 4.0, 64.0, 6.0), Location(null, 10.0, 65.0, 12.0))

    @Serializable(with = WorldSerializer::class)
    final override val world: World = world("world")!!
    val cuboidRegion get() = region as CuboidRegion
    // Default region
    @Serializable(with = CuboidRegionSerializer::class)
    override val region: Region = CuboidRegion(world.we(), defaultPos[0].we(), defaultPos[1].we())
}


@Serializable
sealed class CylinderChallenge(
    private val floor: Material = Material.GOLD_BLOCK,
    private val wall: Material = Material.IRON_BLOCK,
    private val ceiling: Material = Material.BARRIER,
    private val bottomY: Int = 0,
    private val topY: Int = 255,
) :
    Challenge() {
    @Transient private val defaultCenter = world("mlg")!!.spawnLocation
    @Transient private val defaultRadius = 30

    @Serializable(with = WorldSerializer::class)
    final override val world = world("mlg")!!
    val cylinderRegion get() = region as CylinderRegion

    @Serializable(with = CylinderRegionSerializer::class)
    override val region: Region = CylinderRegion(world.we(), defaultCenter.we(), defaultRadius.vector2(), bottomY, topY)

    override fun start() {
        worldEdit.editSession(world) {
            // Wall
            cylinder(cylinderRegion.floor, wall, filled = false, firstAir = true, height = 255)
            // Floor
            cylinder(cylinderRegion.floor, floor, height = 1)
            // Ceiling
            cylinder(cylinderRegion.ceiling, ceiling, height = 1)
        }
    }
}

private const val DEFAULT_PERIOD = 10L
private const val DEFAULT_DAMAGE = 4.0


@SerialName("damager")
@Serializable
class Damager(
    override val name: String,
    @Serializable(with = ChatColorSerializer::class) override val color: ChatColor = KColors.WHITE,
    private val period: Long = DEFAULT_PERIOD,
    private val damage: Double = DEFAULT_DAMAGE
) : CuboidChallenge() {
    @Transient private var task: KSpigotRunnable? = null
    @Transient private var hologram: Hologram? = null
    override val displayName get() = "$name Damager"

    init {
        challengePlayerEvent<PlayerInteractEvent> {
            // TODO increase soups eaten - check if finished
            if (checkSoupMechanic()) Unit
        }
        challengePlayerEvent<PlayerDropItemEvent> {
            taskRunLater(20L) {
                if (!(itemDrop.location inRegion this@Damager)) itemDrop.remove()
            }
        }
    }

    override fun start() {
        super.start()

        val holoLoc = cuboidRegion.center.location().clone().add(0, 2, 0)
        hologram = hologram(holoLoc, "$color$displayName", "Damage: ${KColors.GOLD}${damage/2} ${KColors.RED}\u2764", "Period: ${KColors.GOLD}$period", world = world)
        task = task(period = period) {
            if(it.isCancelled) return@task
            players {
                if (health - damage <= 0.0) fail()
                else damage(damage)
            }
        }
    }

    override fun onEnter(player: Player) {
        player.saturation = 0F
        player.maximumNoDamageTicks = 0 // Do this so periods < 20 work (e.g. impossible damager)
        with(player.inventory) {
            setItem(0, Material.STONE_SWORD.stack())
            for (i in 1..35) setItem(i, Material.MUSHROOM_STEW.stack())
            setItem(13, Material.BOWL.stack(64))
            // TODO cocoa recraft
            setItem(14, Material.RED_MUSHROOM.stack(64))
            setItem(15, Material.BROWN_MUSHROOM.stack(64))
        }
    }

    override fun onLeave(player: Player) {
        player.maximumNoDamageTicks = 20
    }

    override fun stop() {
        super.stop()
        task?.cancel()
        hologram?.remove()
    }

    override val hunger = true
    override val warpItems = false
}


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


@SerialName("block_mlg")
@Serializable
class BlockMlg : Mlg("block", KColors.WHITE) {
    init {
        challengePlayerEvent<BlockPlaceEvent> {
            if (block.location.y > 10 || WARP_ITEMS.any { block.type == it.type }) cancel()
            // Remove block after 0.5 second
            else block.removeAfter(10)
        }
        challengePlayerEvent<PlayerBucketEmptyEvent> {
            if (block.location.y > 10 || WARP_ITEMS.any { block.type == it.type }) cancel()
            // Remove block after 0.5 second
            else block.removeAfter(10)
        }
    }

    override val mlgItems = listOf(Material.WATER_BUCKET, Material.COBWEB, Material.SLIME_BLOCK, Material.SCAFFOLDING, Material.TWISTING_VINES, Material.HONEY_BLOCK)
}