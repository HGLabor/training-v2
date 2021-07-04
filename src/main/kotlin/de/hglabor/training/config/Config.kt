package de.hglabor.training.config

import de.hglabor.training.main.PLUGIN
import org.bukkit.Bukkit
import org.bukkit.ChatColor

val PREFIX: String = "${ChatColor.DARK_GRAY}[${ChatColor.AQUA}Training${ChatColor.DARK_GRAY}]${ChatColor.WHITE}"
val MAX_PLAYERS: Int = Bukkit.getServer().maxPlayers

enum class Config(private val path: String, value: Any) {

    ;

    private val configValue: Any get() = PLUGIN.config.get(this.path) ?: this.mValue
    private var mValue: Any = value

    companion object {
        fun load() {
            values().forEach { PLUGIN.config.addDefault(it.path, it.configValue) }
            PLUGIN.config.options().copyDefaults(true)
            PLUGIN.saveConfig()
        }
        fun reload() { PLUGIN.reloadConfig() }
    }

    fun set(value: Int) {
        mValue = value
        PLUGIN.config.set(this.path, value)
        PLUGIN.saveConfig()
    }

    fun getInt(): Int = this.configValue as Int
    fun getBoolean(): Boolean = this.configValue as Boolean
    fun getString(): String = this.configValue as String
    @Suppress("UNCHECKED_CAST") fun getStringList(): ArrayList<String> = this.configValue as ArrayList<String>? ?: ArrayList()
}