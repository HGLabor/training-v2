package de.hglabor.training.challenge

import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import de.hglabor.training.utils.extensions.we
import de.hglabor.training.utils.extensions.world

class CuboidChallenge(name: String) :
    Challenge(name, CuboidRegion(world("world").we(), BlockVector3.at(4, 64,6), BlockVector3.at(10, 64, 12)))