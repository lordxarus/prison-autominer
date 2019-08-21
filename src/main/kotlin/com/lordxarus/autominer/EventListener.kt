package com.lordxarus.autominer

import com.lordxarus.autominer.npc.AutominerTrait
import com.lordxarus.autominer.util.getModel
import com.lordxarus.autominer.util.plugin
import net.citizensnpcs.api.CitizensAPI
import net.md_5.bungee.api.ChatColor.DARK_PURPLE
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import java.time.Duration

class EventListener : Listener {

    val skinChangePlayers = arrayListOf<Player>()


    // todo
    @EventHandler
    fun onChat(e: PlayerChatEvent) {
        val player = e.player
        val message = e.message
        if (skinChangePlayers.contains(player) && e.message.isNotBlank()) {
            e.isCancelled = true
            skinChangePlayers.remove(player)
            val npc = plugin.npcs[player]!!
            if (!npc.isSpawned) {
                npc.name = message
            } else {
                val loc = npc.entity.location
                npc.despawn()
                npc.name = message
                npc.spawn(loc)
            }

            player.sendMessage("${DARK_PURPLE}Autominer name set to $RED\"$message\" ")
        }

    }


    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player
        if (!plugin.npcs.containsKey(player)) {
            val npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "Miner")
            plugin.npcs[player] = npc
            npc.addTrait(AutominerTrait())
            getModel(player).loadConfig()
        }
    }

    @EventHandler
    fun onDisconnect(e: PlayerQuitEvent) {
        val model = getModel(e.player)
        model.saveConfig()
        model.npc.destroy()
    }

    @EventHandler
    fun onRightClick(e: PlayerInteractEvent) {
        val player = e.player
        if (e.action == Action.RIGHT_CLICK_AIR && e.item.hasItemMeta()) {
            if (Items.tokens.contains(e.item.itemMeta.displayName)) {
                val model = getModel(player)

                var timeAdded = 0
                when(e.item.itemMeta.displayName) {
                    Items.timeTokenSmall.itemMeta.displayName ->
                        timeAdded += 180000
                    Items.timeTokenMedium.itemMeta.displayName ->
                        timeAdded += 600000
                    Items.timeTokenBig.itemMeta.displayName ->
                        timeAdded += 1500000
                }

                if (player.itemInHand.amount > 1) {
                    player.itemInHand.amount -= 1
                } else {
                    player.itemInHand = ItemStack(Material.AIR)
                }
                player.updateInventory()

                model.timeLeft += timeAdded
                player.sendMessage("${plugin.messages.tokenApplied}${Duration.ofSeconds(model.timeLeft / 1000L).toString().toLowerCase().replace("pt", "")}")

            }

        }
    }

}