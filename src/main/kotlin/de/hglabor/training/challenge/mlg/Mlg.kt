package de.hglabor.training.challenge.mlg

import de.hglabor.training.challenge.CylinderChallenge
import de.hglabor.training.utils.extensions.world

class Mlg(name: String) : CylinderChallenge(name, world("mlg")!!) {
    override val displayName: String get() = "$name Mlg"
}