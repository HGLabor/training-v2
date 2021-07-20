package de.hglabor.training.config

import de.hglabor.training.main.Manager
import net.axay.kspigot.extensions.server
import org.bukkit.ChatColor
import org.bukkit.Location

val PREFIX: String = "${ChatColor.DARK_GRAY}[${ChatColor.AQUA}Training${ChatColor.DARK_GRAY}]${ChatColor.WHITE}"
val MAX_PLAYERS: Int = server.maxPlayers

enum class Config(private val path: String, value: Any) {

    ;

    private val configValue: Any get() = Manager.config.get(this.path) ?: this.mValue
    private var mValue: Any = value

    companion object {
        fun load() {
            values().forEach { Manager.config.addDefault(it.path, it.configValue) }
            Manager.config.options().copyDefaults(true)
            Manager.saveConfig()
        }
        fun reload() { Manager.reloadConfig() }
    }

    fun set(value: Int) {
        mValue = value
        Manager.config.set(this.path, value)
        Manager.saveConfig()
    }

    fun getInt(): Int = this.configValue as Int
    fun getBoolean(): Boolean = this.configValue as Boolean
    fun getString(): String = this.configValue as String
    @Suppress("UNCHECKED_CAST") fun getStringList(): ArrayList<String> = this.configValue as ArrayList<String>? ?: ArrayList()
    @Suppress("UNCHECKED_CAST") fun getLocation()= this.configValue as Location
}