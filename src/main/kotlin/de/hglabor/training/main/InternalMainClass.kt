package de.hglabor.training.main

import de.hglabor.training.config.Config
import net.axay.kspigot.main.KSpigot

val PLUGIN by lazy { InternalMainClass.INSTANCE }

class InternalMainClass : KSpigot() {
    companion object {
        lateinit var INSTANCE: InternalMainClass; private set
    }

    override fun load() {
        INSTANCE = this
    }

    override fun startup() {
        Config.load()
    }

    override fun shutdown() {}
}


