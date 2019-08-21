package com.lordxarus.autominer.npc

import org.bukkit.Material
import org.bukkit.block.Block
import java.util.*

abstract class BlockBreaker(private val model: AutominerModel) {

   abstract fun narrowScan(): ArrayList<Block>

    fun wideScan(): ArrayList<Block> {
        return scan(model.wideScanRange)
    }

    fun scan(xZAxes: IntRange, yAxis: IntRange = -model.armReach..model.armReach): ArrayList<Block> {
        val result = ArrayList<Block>()
        for (i in xZAxes) {
            for (j in yAxis) {
                for (k in xZAxes) {
                    val block = model.npc.entity.location.block.getRelative(i, j, k)
                    if (model.region.contains(block.x, block.y, block.z) && block.type != Material.AIR) {
                        result.add(block)
                    }
                }
            }
        }
        return result
    }
}