package de.hglabor.training.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import de.hglabor.training.challenge.*
import de.hglabor.training.events.updateChallengeIfSurvival
import de.hglabor.training.main.Manager
import de.hglabor.training.main.PREFIX
import de.hglabor.utils.kutils.*
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.commands.argument
import net.axay.kspigot.commands.command
import net.axay.kspigot.commands.literal
import net.axay.kspigot.commands.runs
import org.bukkit.entity.Player

fun commands() {
    fun Player.sendTrainingVersion() = sendMessage("$PREFIX This server is running ${"Training-v2".col("green")} (${Manager.description.version.col("green")})")
    command("training") {
        runs { player.sendTrainingVersion() }
        literal("version") { runs { player.sendTrainingVersion() } }
        literal("reload") {
            runs {
                 Manager.reloadConfig()
                 player.sendMessage("$PREFIX Reloaded config")
                 challenges.forEach(Challenge::restart)
            }
        }
        literal("challenge") {
            literal("debug") {
                runs {
                    player.sendMessage("$PREFIX ${KColors.GRAY}You are in ${KColors.WHITE}${player.challenge}${KColors.GRAY}.")
                }
            }
            challenges.forEach { challenge ->
                literal(challenge.name) {
                    if (challenge is Mlg) {
                        literal("warpentity") {
                            runs {
                                // Teleports the warp entity for this mlg to the players current position
                                challenge.warpEntity.teleport(player.location)
                                player.sendMessage("$PREFIX ${KColors.GREEN}Teleported warpentity of ${KColors.WHITE}${challenge.displayName} ${KColors.GREEN}to your position.")
                            }
                        }
                    }
                    literal("region") {
                        literal("set") {
                            if (challenge is CuboidChallenge) {
                                arrayOf("pos1", "pos2").forEach { pos ->
                                    literal(pos) {
                                        runs {
                                            if (pos == "pos1") challenge.cuboidRegion.pos1 = player.location.we()
                                            else if (pos == "pos2") challenge.cuboidRegion.pos2 =
                                                player.location.we()
                                            player.sendMessage("$PREFIX ${KColors.GREEN}Set $pos of challenge ${KColors.GRAY}${challenge.name} ${KColors.GREEN}to your current position.")
                                            onlinePlayers { updateChallengeIfSurvival() }
                                            challenge.saveToConfig()
                                            challenge.restart()
                                        }
                                    }
                                }
                            }
                            else if (challenge is CylinderChallenge) {
                                literal("center") {
                                    runs {
                                        challenge.cylinderRegion.setCenter(player.location.blockVector2())
                                        player.sendMessage("$PREFIX ${KColors.GREEN}Set center of challenge ${KColors.GRAY}${challenge.name} ${KColors.GREEN}to your current position.")
                                        challenge.saveToConfig()
                                        challenge.restart()
                                        onlinePlayers { updateChallengeIfSurvival() }
                                    }
                                }
                                literal("radius") {
                                    argument("radius", IntegerArgumentType.integer(1)) {
                                        runs {
                                            val radius = getArgument<Int>("radius")
                                            challenge.cylinderRegion.radius = radius.vector2()
                                            player.sendMessage("$PREFIX ${KColors.GREEN}Set radius of challenge ${KColors.GRAY}${challenge.name} ${KColors.GREEN}to ${KColors.WHITE}$radius.")
                                            challenge.saveToConfig()
                                            challenge.restart()
                                            onlinePlayers { updateChallengeIfSurvival() }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}