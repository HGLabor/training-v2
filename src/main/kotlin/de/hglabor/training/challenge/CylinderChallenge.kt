package de.hglabor.training.challenge

import com.sk89q.worldedit.regions.CylinderRegion
import de.hglabor.training.main.Manager
import de.hglabor.training.utils.extensions.world
import org.bukkit.World

private fun center(name: String) { with(Manager.config) {
    val path = "challenge.$name.region.center"
    //addDefault(path, )
}}

open class CylinderChallenge(name: String, world: World = world("mlg")!!) :
    Challenge(name, world, CylinderRegion(

    )) {
    val cylinderRegion get() = region as CylinderRegion
}