package de.hglabor.training.main

import de.hglabor.training.challenge.challengeListener
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
    }

    override fun shutdown() {}
}


