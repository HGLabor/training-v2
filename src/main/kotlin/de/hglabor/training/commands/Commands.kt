package de.hglabor.training.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import de.hglabor.training.challenge.Challenge
import de.hglabor.training.challenge.CuboidChallenge
import de.hglabor.training.challenge.CylinderChallenge
import de.hglabor.training.challenge.challenges
import de.hglabor.training.config.Config
import de.hglabor.training.config.PREFIX
import de.hglabor.training.events.updateChallengeIfSurvival
import de.hglabor.training.main.Manager
import de.hglabor.training.utils.extensions.*
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.commands.*
import net.axay.kspigot.extensions.bukkit.actionBar
import org.bukkit.entity.Player

fun commands() {
    fun Player.sendTrainingVersion() = sendMessage("$PREFIX This server is running ${"Training-v2".col("green")} (${Manager.description.version.col("green")})")
    command("training") {
        simpleExecutes { it.source.player.sendTrainingVersion() }
        literal("version") { simpleExecutes { it.source.player.sendTrainingVersion() } }
        literal("reload") {
            simpleExecutes {
                Config.reload()
                it.source.player.sendMessage("$PREFIX Reloaded config")
                challenges.forEach(Challenge::restart)
            }
        }
        literal("challenge") {
            simpleExecutes {
                it.source.player.actionBar("${KColors.RED}Specify a challenge")
            }
            challenges.forEach { challenge ->
                literal(challenge.name) {
                    literal("region") {
                        literal("set") {
                            if (challenge is CuboidChallenge) {
                                arrayOf("pos1", "pos2").forEach { pos ->
                                    literal(pos) {
                                        simpleExecutes {
                                            val player = it.source.player
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
                                    simpleExecutes {
                                        val player = it.source.player
                                        challenge.cylinderRegion.setCenter(player.location.bv2())
                                        player.sendMessage("$PREFIX ${KColors.GREEN}Set center of challenge ${KColors.GRAY}${challenge.name} ${KColors.GREEN}to your current position.")
                                        challenge.saveToConfig()
                                        challenge.restart()
                                        onlinePlayers { updateChallengeIfSurvival() }
                                    }
                                }
                                literal("radius") {
                                    argument("radius", IntegerArgumentType.integer(1)) {
                                        simpleExecutes {
                                            val player = it.source.player
                                            val radius = it.getArgument<Int>("radius")
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