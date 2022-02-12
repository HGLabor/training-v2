@file:Suppress("CanBeParameter")

package de.hglabor.training.challenge

import com.sk89q.worldedit.math.Vector2
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.regions.CylinderRegion
import com.sk89q.worldedit.regions.Region
import de.hglabor.training.events.updateChallengeIfSurvival
import de.hglabor.training.main.PREFIX
import de.hglabor.training.mechanics.checkSoupMechanic
import de.hglabor.training.renewInv
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
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
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
            if (player?.challenge == this) it.callback()
        }
    }

    fun Player.fail() {
        sendMessage("$PREFIX ${KColors.RED}You failed ${this@Challenge.displayName}")
        teleport((if (bedSpawnLocation?.world == world) bedSpawnLocation else null) ?: world.spawnLocation)
        updateChallengeIfSurvival()
        renewInv()
    }

    open fun saveToConfig() {}

    @Transient open val hunger = false
    @Transient open val warpItems = true
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