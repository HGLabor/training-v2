package de.hglabor.training.main

import de.hglabor.training.challenge.CuboidChallenge
import de.hglabor.training.challenge.challengeListener
import de.hglabor.training.challenge.challenges
import de.hglabor.training.challenge.registerChallenges
import de.hglabor.training.config.Config
import de.hglabor.training.events.regionListener
import de.hglabor.training.utils.itemsListener
import net.axay.kspigot.main.KSpigot

val Manager by lazy { InternalMainClass.INSTANCE }

class InternalMainClass : KSpigot() {
    companion object {
        lateinit var INSTANCE: InternalMainClass; private set
    }

    override fun load() {
        INSTANCE = this
    }

    override fun startup() {
        Config.load()
        itemsListener()
        challengeListener()
        regionListener()
        registerChallenges(CuboidChallenge("test"))
        challenges.forEach { it.start() }
    }

    override fun shutdown() {
        challenges.forEach { it.stop() }
    }

}


