package de.hglabor.training.challenge

import de.hglabor.training.WARP_ITEMS
import de.hglabor.utils.kutils.cancel
import de.hglabor.utils.kutils.removeAfter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.axay.kspigot.chat.KColors
import org.bukkit.Material
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent

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