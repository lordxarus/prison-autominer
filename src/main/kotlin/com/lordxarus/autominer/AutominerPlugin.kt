package com.lordxarus.autominer

import com.lordxarus.autominer.inventory.AutominerInventoryProvider
import com.lordxarus.autominer.inventory.SkinChangerInventoryProvider
import com.lordxarus.autominer.npc.AutominerTrait
import com.lordxarus.autominer.util.Messages
import com.lordxarus.autominer.util.Permissions
import com.lordxarus.autominer.util.getModel
import fr.minuskube.inv.InventoryManager
import fr.minuskube.inv.SmartInventory
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.NPC
import net.md_5.bungee.api.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.io.File


class AutominerPlugin : JavaPlugin(), CommandExecutor, Listener {

    val npcs = HashMap<Player, NPC>()
    val inventoryManager = InventoryManager(this)
    val listener = EventListener()

    val inventory by lazy {
        SmartInventory.builder()
                .id("mainMenu")
                .manager(inventoryManager)
                .provider(AutominerInventoryProvider())
                .size(3, 9)
                .title("${ChatColor.DARK_GRAY}${ChatColor.BOLD}AutoMiner Menu")
                .build()
    }

    val skinChangerInventory by lazy {
        SmartInventory.builder()
                .id("skinChangerMenu")
                .manager(inventoryManager)
                .provider(SkinChangerInventoryProvider())
                .size(4, 9)
                .title("${ChatColor.DARK_GRAY}${ChatColor.BOLD}Skin Changer!")
                .parent(inventory)
                .build()
    }

    val messages by lazy { Messages(config) }

    override fun onEnable() {
        if (!File("plugins/AutoMiner/config.yml").exists()) {
            saveDefaultConfig()
        }
        this.getCommand("am").executor = this
        CitizensAPI.getTraitFactory().registerTrait(net.citizensnpcs.api.trait.TraitInfo.create(AutominerTrait::class.java).withName("automine"))
        this.server.pluginManager.registerEvents(listener, this)
        inventoryManager.init()

        server.onlinePlayers.stream().forEach {
            println(it.name)
            if (!npcs.containsKey(it)) {
                val npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "Miner")
                npcs[it] = npc
                npc.addTrait(AutominerTrait())
                getModel(it).load()
            }
        }


    }

    override fun onDisable() {
        npcs.forEach { (t, u) ->
            val model = getModel(t)
            model.sellAllItems()
            model.save()
            (u.destroy())
        }
    }

    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>): Boolean {
        sender as Player
        if (label == "am") {
            if (args.isNotEmpty()) {
                if (args[0] == "token") {
                    if (sender.hasPermission(Permissions.autoMinerAdmin)) {
                        var item: ItemStack = Items.timeTokenSmall
                        if (args.size > 1) {
                            when(args[1]) {
                                "big" -> item = Items.timeTokenBig
                                "medium" -> item = Items.timeTokenMedium
                                "small" -> item = Items.timeTokenSmall

                            }
                        }
                        sender.inventory.setItem(sender.inventory.firstEmpty(), Items.timeTokenBig)
                        sender.updateInventory()

                    } else {
                        sender.sendMessage(messages.noPermission)
                    }
                }
            } else {
                if (npcs.containsKey(sender)) {
                    inventory.open(sender)
                }
            }
        }
        return true
    }



}
