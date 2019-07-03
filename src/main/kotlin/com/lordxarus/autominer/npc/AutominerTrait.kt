package com.lordxarus.autominer.npc

import com.lordxarus.autominer.util.getModel
import com.lordxarus.autominer.util.plugin
import net.citizensnpcs.api.event.NPCLeftClickEvent
import net.citizensnpcs.api.event.NPCRightClickEvent
import net.citizensnpcs.api.trait.Trait
import org.bukkit.event.EventHandler


class AutominerTrait : Trait("automine") {

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

    @EventHandler
    fun onLeftClick(e: NPCLeftClickEvent) {
        if (e.npc.isSpawned) {
            if (e.npc == npc && e.clicker.player == autominerModel.player) {
                getModel(e.clicker.player).npc.despawn()
            }
        }
    }

    @EventHandler
    fun onRightClick(e: NPCRightClickEvent) {
        if (e.npc.isSpawned) {
            if (e.npc == npc && e.clicker.player == autominerModel.player) {
                plugin.inventory.open(e.clicker)
            }
        }
    }
}

