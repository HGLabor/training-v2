package de.hglabor.training.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

abstract class TrainingEvent : Event() {
    companion object {
        val handlers = HandlerList()
    }

    override fun getHandlers(): HandlerList = TrainingEvent.handlers
}