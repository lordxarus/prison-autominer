package com.lordxarus.autominer.npc

import org.bukkit.block.Block
import java.util.*

class DefaultBlockBreaker(private val model: AutominerModel): BlockBreaker(model) {

    val region = model.region

    override fun narrowScan(): ArrayList<Block> {
        return scan(model.narrowScanRange)
    }

    fun getNextBlock() {

    }
}