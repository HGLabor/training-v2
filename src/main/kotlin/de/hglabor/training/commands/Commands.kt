package de.hglabor.training.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import de.hglabor.training.challenge.*
import de.hglabor.training.events.updateChallengeIfSurvival
import de.hglabor.training.main.Manager
import de.hglabor.training.main.PREFIX
import de.hglabor.training.main.json
import de.hglabor.utils.kutils.*
import kotlinx.serialization.encodeToString
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.chat.literalText
import net.axay.kspigot.commands.*
import org.bukkit.entity.Player

fun commands() {
    fun Player.sendTrainingVersion() = sendMessage("$PREFIX This server is running ${"Training-v2".col("green")} (${Manager.description.version.col("green")})")
    command("training") {
        requiresPermission("hglabor.training.admintools")
        runs { player.sendTrainingVersion() }
        literal("version") { runs { player.sendTrainingVersion() } }
        literal("config") {
            literal("reload") {
                runs {
                    player.sendMessage("$PREFIX Reloaded config")
                    challenges.forEach(Challenge::restart)
                }
            }
            literal("save") {
                runs {
                    Manager.configFile.writeText(json.encodeToString(challenges))
                    player.sendMessage("$PREFIX Saved config.")
                }
            }
        }
        literal("challenge") {
            literal("debug") {
                runs {
                    player.sendMessage(literalText(PREFIX) {
                        text(" You are in ") { color = KColors.GRAY }
                        text(player.challenge?.toString() ?: "no challenge")
                        text(".") { color = KColors.GRAY }
                    })
                }
            }
            challenges.forEach { challenge ->
                literal(challenge.name) {
                    if (challenge is Mlg) {
                        literal("warpentity") {
                            runs {
                                // Teleports the warp entity for this mlg to the players current position
                                challenge.warpEntity.teleport(player.location)
                                player.sendMessage(literalText(PREFIX) {
                                    text(" Teleported warpentity of ") { color = KColors.GREEN }
                                    text(challenge.displayName) { color = KColors.WHITE }
                                    text(" to your position.") { color = KColors.GREEN }
                                })
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
                                            else if (pos == "pos2") challenge.cuboidRegion.pos2 = player.location.we()
                                            player.sendMessage(literalText(PREFIX) {
                                                text(" Set $pos of challenge ") { color = KColors.GREEN }
                                                text(challenge.name) { color = KColors.GRAY }
                                                text(" to your current position") { color = KColors.GREEN }
                                            })
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
                                        player.sendMessage(literalText(PREFIX) {
                                            text(" Set center of challenge ") { color = KColors.GREEN }
                                            text(challenge.name) { color = KColors.GRAY }
                                            text(" to your current position") { color = KColors.GREEN }
                                        })
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
                                            player.sendMessage(literalText(PREFIX) {
                                                text(" Set radius of challenge ") { color = KColors.GREEN }
                                                text(challenge.name) { color = KColors.GRAY }
                                                text(" to ") { color = KColors.GREEN }
                                                text(radius.toString()) { color = KColors.WHITE }
                                                text(".") { color = KColors.GREEN }
                                            })
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