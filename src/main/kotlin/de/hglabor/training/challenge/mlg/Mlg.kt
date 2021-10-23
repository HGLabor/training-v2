package de.hglabor.training.challenge.mlg

import de.hglabor.training.challenge.CylinderChallenge
import de.hglabor.training.utils.extensions.world

class Mlg(name: String, val platformHeights: List<Int> = listOf()) : CylinderChallenge(name, world("mlg")!!) {
    override val displayName: String get() = "$name Mlg"

    override fun start() {
        super.start()
        // TODO Platforms

    }
}