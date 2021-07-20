package de.hglabor.training.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import de.hglabor.training.challenge.CuboidChallenge
import de.hglabor.training.challenge.CylinderChallenge
import de.hglabor.training.challenge.challenge
import de.hglabor.training.challenge.challengeNames
import de.hglabor.training.config.PREFIX
import de.hglabor.training.events.updateChallenge
import de.hglabor.training.utils.extensions.bv2
import de.hglabor.training.utils.extensions.onlinePlayers
import de.hglabor.training.utils.extensions.we
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.commands.*
import net.axay.kspigot.extensions.bukkit.actionBar

fun commands() {
    command("training") {
        literal("challenge") {
            simpleExecutes {
                it.source.player.actionBar("${KColors.RED}Specify a challenge")
            }
            argument("name", StringArgumentType.string()) {
                simpleSuggests { challengeNames }
                literal("region") {
                    literal("set") {
                        arrayOf("pos1", "pos2").forEach { pos ->
                            literal(pos) {
                                simpleExecutes {
                                    val challenge = challenge(it.getArgument("name"))
                                    val player = it.source.player
                                    if (challenge !is CuboidChallenge) {
                                        if (challenge != null) player.sendMessage("$PREFIX ${KColors.RED}Challenge region is not cuboid.")
                                        else player.sendMessage("$PREFIX ${KColors.RED}Challenge not found.")
                                        return@simpleExecutes
                                    }
                                    if (pos == "pos1") challenge.cuboidRegion.pos1 = player.location.we()
                                    else if (pos == "pos2") challenge.cuboidRegion.pos2 = player.location.we()
                                    player.sendMessage("$PREFIX ${KColors.GREEN}Set $pos of challenge ${KColors.GRAY}${challenge.name} ${KColors.GREEN}to your current position.")
                                    onlinePlayers { updateChallenge() }
                                    challenge.restart()
                                }
                            }
                        }
                        // TODO center, radius
                        literal("center") {
                            simpleExecutes {
                                val challenge = challenge(it.getArgument("name"))
                                val player = it.source.player
                                if (challenge !is CylinderChallenge) {
                                    if (challenge != null) player.sendMessage("$PREFIX ${KColors.RED}Challenge region is not cylinder.")
                                    else player.sendMessage("$PREFIX ${KColors.RED}Challenge not found.")
                                    return@simpleExecutes
                                }
                                challenge.cylinderRegion.setCenter(player.location.bv2())
                                player.sendMessage("$PREFIX ${KColors.GREEN}Set center of challenge ${KColors.GRAY}${challenge.name} ${KColors.GREEN}to your current position.")
                                onlinePlayers { updateChallenge() }
                            }
                        }
                        literal("radius") {
                            argument("radius", IntegerArgumentType.integer(1)) {
                                
                            }
                        }
                    }
                }
            }
        }
    }
}