package com.lordxarus.autominer.npc

import net.citizensnpcs.api.trait.Trait


class AutominerTrait: Trait("automine") {

    val autominerModel by lazy { AutominerModel(npc) }

    override fun onSpawn() {
        autominerModel.reset()
        autominerModel.onSpawn()
    }

    override fun onDespawn() {
        autominerModel.run = false
    }

    override fun run() {
        if (npc.isSpawned) {
            autominerModel.run()
        }
    }

}

