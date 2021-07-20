package de.hglabor.training.challenge.mlg

import de.hglabor.training.challenge.CylinderChallenge
import org.bukkit.World

class Mlg(name: String, world: World) : CylinderChallenge(name, world) {
    override val displayName: String get() = "$name Mlg"
}