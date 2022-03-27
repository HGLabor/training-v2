@file:Suppress("CanBeParameter")

package de.hglabor.training.challenge

import com.sk89q.worldedit.math.Vector2
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.regions.CylinderRegion
import com.sk89q.worldedit.regions.Region
import de.hglabor.training.WARP_ITEMS
import de.hglabor.training.defaultInv
import de.hglabor.training.events.updateChallengeIfSurvival
import de.hglabor.training.main.Manager
import de.hglabor.training.main.PREFIX
import de.hglabor.training.main.json
import de.hglabor.training.mechanics.checkSoupMechanic
import de.hglabor.training.serialization.*
import de.hglabor.utils.kutils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.actionBar
import net.axay.kspigot.extensions.bukkit.title
import net.axay.kspigot.extensions.geometry.blockLoc
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import net.axay.kspigot.runnables.taskRunLater
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.ChatColor
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import org.bukkit.*
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer
import org.bukkit.entity.Chicken
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

// All in 1 file bc of kotlinx.serialization <3
@Suppress("EqualsOrHashCode")
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
            if (player == null) player = it.reflectMethod<Player>("getWhoClicked")
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
    open fun saveToConfig() {
        Manager.configFile.writeText(json.encodeToString(challenges))
    }

    @Transient open val hunger = false
    @Transient open val allowDrop = true
    @Transient open val warpItems = true
    /** @return true if setting the respawn location to this location is allowed */
    open fun allowRespawnLocation(location: Location) = false
    /** @return true if interacting is allowed */
    open fun allowInteraction(event: PlayerInteractEvent) = false

    override fun equals(other: Any?) = other is Challenge && other.name.equals(name, true)
}

@Serializable
sealed class CuboidChallenge : Challenge() {
    @Transient private val defaultPos = listOf(Location(null, 4.0, 64.0, 6.0), Location(null, 10.0, 65.0, 12.0))

    @Serializable(with = WorldSerializer::class)
    final override val world: World = world("world")!!
    val cuboidRegion get() = region as CuboidRegion
    // Default region
    @Serializable(with = CuboidRegionSerializer::class) @Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
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

    @Serializable(with = CylinderRegionSerializer::class) @Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
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

        val holoLoc = cuboidRegion.center.location().addY(if(this.name.equals("lava", true)) 7 else 0)
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
        player.maximumNoDamageTicks = period.toInt() // Do this so periods < 20 work (e.g. impossible damager)
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
    @Transient private val bottomArea = cylinderRegion.minimumY + 5

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
    override fun allowInteraction(event: PlayerInteractEvent) = true

    val spawn get() = cylinderRegion.center.location().apply {
        this.world = this@Mlg.world
        this.y = 101.0
    }

    @Transient lateinit var warpEntity: Entity
    override fun start() {
        super.start()
        warpEntity = world.spawnEntity(world.spawnLocation, warpEntityType).apply {
            statueAttributes()
               @Suppress("DEPRECATION")
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

@SerialName("aim_training")
@Serializable
class AimTraining : CuboidChallenge() {
    override val allowDrop = false
    override fun allowInteraction(event: PlayerInteractEvent) = true

    init {
        listen<ProjectileHitEvent> { with(it) {
            if (entity.shooter !is Player || hitEntity == null) {
                return@listen
            }
            val player = entity.shooter as Player
            if (hitEntity?.uniqueId == player.uniqueId || player notInChallenge this@AimTraining) {
                return@listen
            }

            if (chickens[player]?.location?.blockLoc == hitEntity!!.location.blockLoc) {
                chickens[player]?.remove()
                spawnChicken(player)
            }
        }}
    }

    @Transient private var locations = listOf(
        Location(world, 65.7, 74.75, -47.1),
        Location(world, 65.7, 76.6, -41.1),
        Location(world, 65.7, 80.73, -28.95),
        Location(world, 65.7, 70.98, -37.1),
        Location(world, 65.7, 84.1, -38.1),
    )
    @Transient private var hologram: Hologram? = null
    @Transient private var task: KSpigotRunnable? = null
    @Transient var chickens = hashMapOf<Player, Chicken>()

    override val name = "aimtraining"
    override val displayName = "Aim Training"
    override val color: ChatColor get() = KColors.DEEPPINK

    override fun start() {
        super.start()

        val holoLoc = cuboidRegion.center.location().addY(2)
        hologram = hologram(holoLoc, "$color$displayName", "Duration: ${KColors.GOLD}64 shots", world = world)
        task = task(
            period = 20
        ) {
            for (player in chickens.keys) {
                for (otherPlayers in net.axay.kspigot.extensions.onlinePlayers.stream().filter { it != player }.toList()) {
                    (otherPlayers as CraftPlayer).handle.connection.send(ClientboundRemoveEntitiesPacket(chickens[player]?.entityId ?: continue))
                }
            }
        }
    }

    override fun onEnter(player: Player) {
        player.saturation = 0F
        with(player.inventory) {
            setItem(4, Material.BOW.stack())
            setItem(9, Material.ARROW.stack(64))
            heldItemSlot = 4
        }
        spawnChicken(player)
    }

    private fun spawnChicken(player: Player) {
        val chicken = world.spawn(locations.random(), Chicken::class.java)
        chicken.statueAttributes()
        chicken.isGlowing = true
        chicken.customName(Component.text("chicken man"))

        for (otherPlayer in net.axay.kspigot.extensions.onlinePlayers.filter { it != player }) {
            (otherPlayer as CraftPlayer).handle.connection.send(ClientboundRemoveEntitiesPacket(chicken.entityId))
        }
        player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 0f)
        chickens[player] = chicken
    }

    override fun onLeave(player: Player) {
        chickens[player]?.remove()
        chickens.remove(player)
    }

    override fun stop() {
        super.stop()
        hologram?.remove()
        task?.cancel()
        chickens.values.forEach(Chicken::remove)
        chickens.clear()
    }

}

@SerialName("crafting")
@Serializable
class CraftingChallenge : CuboidChallenge() {
    override val allowDrop = false
    override fun allowInteraction(event: PlayerInteractEvent) = event.player.data?.startTime != null
    override val warpItems = false

    @Transient val craftingUtils = CraftingUtils()

    init {
        challengePlayerEvent<InventoryClickEvent> {
            if (whoClicked !is Player) return@challengePlayerEvent

            val playerData = (whoClicked as Player).data ?: return@challengePlayerEvent
            if (playerData.startTime == null) cancel()
        }
        challengePlayerEvent<CraftItemEvent> {
            val player = whoClicked as Player
            val data = player.data ?: return@challengePlayerEvent

            if (currentItem?.type == data.item) {
                val timeNeededMillis = System.currentTimeMillis() - (data.startTime ?: return@challengePlayerEvent)
                val timeNeededFormat = timeNeededMillis.milliseconds.toComponents { seconds, nanoseconds ->
                    "$seconds.${nanoseconds.toString().substring(0..2)}s"
                }
                player.sendMessage("$PREFIX ${KColors.GREEN}You crafted " +
                        "${KColors.GOLD}${currentItem?.type} ${KColors.GREEN}in ${KColors.WHITE}$timeNeededFormat${KColors.GREEN}.")
                player.playSound(Sound.ENTITY_PLAYER_LEVELUP, pitch = 0)
                player.data = CraftingData()
            }
        }
    }

    @Transient private var hologram: Hologram? = null
    @Transient private val tasks = HashMap<UUID, KSpigotRunnable?>()
    @Transient private val playerDatas = HashMap<UUID, CraftingData>()
    private inner class CraftingData(
        val item: Material? = null,
        var startTime: Long? = null
    )
    private var Player.data: CraftingData?
        get() = playerDatas[uniqueId]
        set(value) {
            playerDatas[uniqueId] = value!!
        }

    override val name = "crafting"
    override val displayName = "Crafting"
    override val color: ChatColor get() = KColors.SADDLEBROWN

    override fun start() {
        super.start()

        val holoLoc = cuboidRegion.center.location().addY(2)
        hologram = hologram(holoLoc, "$color$displayName", world = world)
    }

    @Transient private val duration: Int = 20
    override fun onEnter(player: Player) {
        var seconds: Long = 0

        tasks[player.uniqueId] = task(period = 20L) {
            if (it.isCancelled) return@task
            when (seconds % duration) {
                0L -> {
                    // Preparation
                    player.closeAndClearInv()
                    val item = craftingUtils.randomItem()
                    player.data = CraftingData(item)

                    player.playSound(Sound.ENTITY_EVOKER_CAST_SPELL, pitch = 0)
                    player.sendMessage("$PREFIX ${KColors.YELLOW}Next item: ${KColors.GOLD}${item.name}")
                    player.title("§6Crafting", "§cCraft (a) §b${item.name}", 20, 40, 20)
                    repeat(9) { i ->
                        player.inventory.setItem(i, namedItem(item, "${KColors.AQUA}${item.name}"))
                    }
                }
                5L -> {
                    // Crafting
                    val data = player.data ?: return@task

                    player.closeAndClearInv()
                    player.playSound(Sound.BLOCK_BEEHIVE_ENTER, pitch = 0)
                    data.item?.ingredients?.addToInv(player)
                    data.startTime = System.currentTimeMillis()
                }
            }
            player.data?.item?.let { item ->
                player.actionBar("Current Item: ${KColors.GOLD}${item.name}")
            }
            seconds++
        }
    }

    override fun onLeave(player: Player) {
        tasks[player.uniqueId]?.cancel()
        player.data = CraftingData()
    }

    override fun stop() {
        super.stop()
        hologram?.remove()
        tasks.values.forEach { it?.cancel() }
    }

}