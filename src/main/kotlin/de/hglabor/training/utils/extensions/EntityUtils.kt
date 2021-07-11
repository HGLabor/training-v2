package de.hglabor.training.utils.extensions

import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity

fun Entity.statueAttributes() {
    isCustomNameVisible = true
    isPersistent = false
    isInvulnerable = true
    isSilent = true
    setGravity(false)
    if (this is LivingEntity) {
        setAI(false)
        removeWhenFarAway = false
    }
}