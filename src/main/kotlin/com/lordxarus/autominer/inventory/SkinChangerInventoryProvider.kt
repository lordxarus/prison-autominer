package com.lordxarus.autominer.inventory

import com.lordxarus.autominer.npc.AutominerModel
import com.lordxarus.autominer.npc.AutominerTrait
import com.lordxarus.autominer.util.*
import fr.minuskube.inv.ClickableItem
import fr.minuskube.inv.content.InventoryContents
import fr.minuskube.inv.content.InventoryProvider
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

class SkinChangerInventoryProvider: InventoryProvider {

    val providerOf by lazy { plugin.skinChangerInventory }

    override fun update(player: Player?, contents: InventoryContents) {
        val model by lazy { plugin.npcs[player]?.getTrait(AutominerTrait::class.java)?.autominerModel!! }


    }

    override fun init(player: Player, contents: InventoryContents) {
        val model by lazy { plugin.npcs[player]?.getTrait(AutominerTrait::class.java)?.autominerModel!! }

        contents.fill(ClickableItem.empty(ItemStack(Material.STAINED_GLASS_PANE, 1, 11.toShort())))

        contents.set(1, 1, createHeadClickable(player, model, "Miner"))
        contents.set(1, 2, createHeadClickable(player, model, "BeaverXSwordz"))
        contents.set(1, 3, createHeadClickable(player, model, "boreddevforhire"))
        contents.set(1, 4, createHeadClickable(player, model, "CinemaXSwordz"))
        contents.set(1, 6, createHeadClickable(player, model, "GeXyd"))
        contents.set(1, 5, createHeadClickable(player, model, "iAbide"))
        contents.set(1, 6, createHeadClickable(player, model, "ItsAndromenda"))
        contents.set(1, 7, createHeadClickable(player, model, "Maxyo"))
        contents.set(2, 1, createHeadClickable(player, model, "MiniLaddd"))
        contents.set(2, 2, createHeadClickable(player, model, "Notch"))
        contents.set(2, 3, createHeadClickable(player, model, "Poptartersauce"))
        contents.set(2, 4, createHeadClickable(player, model, "sub2pewdiepie212"))



        contents.set(3, 4, createBackButtonClickable(player))

    }

    fun createHeadClickable(player: Player, model: AutominerModel, name: String): ClickableItem {
        val itemWithMeta = ItemStack(Material.SKULL_ITEM, 1, 3.toShort()).also {
            val meta = it.itemMeta.clone() as SkullMeta
            setItemFlags(meta)
            setHeadOwner(it, name)
            setItemNameLore(it, "${ChatColor.RESET}${ChatColor.YELLOW}$name", arrayListOf(
                    "",
                    "${ChatColor.AQUA}>> Click to choose!"
            ))
            addPermissionLore(it, player, "${Permissions.skinChange}.${name.toLowerCase()}")
            it.addUnsafeEnchantment(Enchantment.DIG_SPEED, 1)
        }

        return ClickableItem.of(itemWithMeta) {
            if (player.hasPermission("${Permissions.skinChange}.${name.toLowerCase()}")) {
                plugin.npcs[player]!!.name = name
                player.sendMessage("${plugin.messages.changeSkin}$name.")
            } else {
                player.sendMessage(plugin.messages.noPermission)
            }
        }
    }

    fun createBackButtonClickable(player: Player): ClickableItem {
        return ClickableItem.of(setItemNameLore(ItemStack(Material.SPRUCE_DOOR_ITEM), "${ChatColor.RESET}${ChatColor.GRAY}⤺ Back ⤺", arrayListOf())) {
            providerOf.parent.get().open(player)
        }
    }
}