package com.lordxarus.autominer.inventory

import com.lordxarus.autominer.npc.AutominerModel
import com.lordxarus.autominer.npc.AutominerTrait
import com.lordxarus.autominer.util.*
import fr.minuskube.inv.ClickableItem
import fr.minuskube.inv.content.InventoryContents
import fr.minuskube.inv.content.InventoryProvider
import fr.minuskube.inv.content.SlotPos
import net.md_5.bungee.api.ChatColor.*
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.time.Duration
import kotlin.math.roundToInt
import kotlin.random.Random


class AutominerInventoryProvider: InventoryProvider {

    val rand = Random
    val spawnButtonType = Material.DIAMOND_PICKAXE
    val spawnButtonSlotPos = SlotPos(1, 1)

    val sellBlocksButtonType = Material.DOUBLE_PLANT
    val sellBlocksButtonPos = SlotPos(1, 3)

    val earningsButtonType = Material.TRIPWIRE_HOOK
    val earningsButtonPos = SlotPos(1, 5)

    val skinChangerStack = ItemStack(Material.SKULL_ITEM, 1, 3.toShort())
    val skinChangerPos = SlotPos(1, 7)

    //todo
    val earningsClickable = ClickableItem.of(ItemStack(earningsButtonType).also {
        setItemNameLore(it, "${GOLD}Claim$DARK_GRAY", arrayListOf(
                "$DARK_GRAY―your crate key and token earnings!―",
                "",
                "$AQUA>> Click to view earnings!"
        ))

    }) { (it.whoClicked as Player).sendMessage(plugin.messages.noImpl) }



    override fun update(player: Player, contents: InventoryContents) {
        val model by lazy { plugin.npcs[player]?.getTrait(AutominerTrait::class.java)?.autominerModel!! }

        // Spawn button
        contents.set(spawnButtonSlotPos, createSpawnClickable(model, player, ItemStack(spawnButtonType)))

        // Sell button
        contents.set(sellBlocksButtonPos, createSellClickable(model, player, ItemStack(sellBlocksButtonType)))

        contents.set(earningsButtonPos, earningsClickable)

        // Skin changer
        contents.set(skinChangerPos, createSkinChangerClickable(model, player, skinChangerStack))

    }

    override fun init(player: Player, contents: InventoryContents) {
        val model by lazy { plugin.npcs[player]?.getTrait(AutominerTrait::class.java)?.autominerModel!! }

        // Spawn button
        contents.set(spawnButtonSlotPos, createSpawnClickable(model, player, ItemStack(spawnButtonType)))

        // Sell blocks button
        contents.set(sellBlocksButtonPos, createSellClickable(model, player, ItemStack(sellBlocksButtonType)))

        // Earnings button
        contents.set(earningsButtonPos, earningsClickable)

        // Skin changer
        contents.set(skinChangerPos, createSkinChangerClickable(model, player, skinChangerStack))

        contents.fill(ClickableItem.empty(ItemStack(Material.STAINED_GLASS_PANE, 1, 11.toShort())))

    }

    private fun createSellClickable(model: AutominerModel, player: Player, item: ItemStack): ClickableItem {

        val itemWithMeta = item.also {
            val meta = item.itemMeta
            setItemFlags(meta)
            meta.displayName = "$RESET${BLUE}Sell${DARK_GRAY}"
            val lore = arrayListOf(
                    "${DARK_GRAY}―off your mined blocks!―",
                    "",
                    "${GRAY}Blocks: $YELLOW${model.getTotalBlocksMined()}",
                    "${GRAY}Price: $YELLOW$${getTotalPrice(model, getShop(model.npc, player)).roundToInt()}",
                    "",
                    "$AQUA>> Click to claim!"
            )

            meta.lore = lore
            item.itemMeta = meta

            item.addUnsafeEnchantment(Enchantment.DIG_SPEED, 1)
        }

        return ClickableItem.of(itemWithMeta) { e ->
            if (e.isLeftClick && model.getTotalBlocksMined() != 0) {

                val shop = getShop(model.npc, player)

                player.sendMessage("${GREEN}Sold off ${model.getTotalBlocksMined()} blocks for: $${getTotalPrice(model, shop)}")
                model.sellAllItems()

                // todo
            }
        }
    }

    private fun createSpawnClickable(model: AutominerModel, player: Player, item: ItemStack): ClickableItem {
        val itemWithMeta = item.also {
            val meta = item.itemMeta
            setItemFlags(meta)

            val lore = arrayListOf(
                    "",
                    "${GRAY}Time left: $YELLOW${Duration.ofSeconds(model.timeLeft / 1000L).toString().toLowerCase().replace("pt", "")}",
                    ""
            )

            if (model.npc.isSpawned) {
                meta.displayName = "${DARK_RED}Hide Miner"
                lore.add("$AQUA>> Click to hide Miner!")
            } else {
                meta.displayName = "${DARK_RED}Summon Miner${GREEN}"
                lore.add("$AQUA>> Click to summon Miner!")
            }


            meta.lore = lore
            item.itemMeta = meta

            addPermissionLore(it, player, Permissions.spawnAutoMiner)

            item.addUnsafeEnchantment(Enchantment.DIG_SPEED, 1)

        }
        return ClickableItem.of(itemWithMeta) {
            if (player.hasPermission(Permissions.spawnAutoMiner)) {
                if (model.npc.isSpawned) {
                    model.npc.despawn()
                } else {
                    if (isLocInAnyShop(player.location)) {
                        if (model.timeLeft > 0) {
                            model.npc.spawn(player.location)
                        } else {
                            player.sendMessage(plugin.messages.outOfTime)
                        }
                    } else {
                        player.sendMessage(plugin.messages.noRegion)
                    }
                }
                player.closeInventory()
            } else {
                player.sendMessage(plugin.messages.noPermission)
            }
        }

    }

    fun createSkinChangerClickable(model: AutominerModel, player: Player, item: ItemStack): ClickableItem {
        val itemWithMeta = item.also {
            val meta = it.itemMeta.clone() as SkullMeta
            setItemFlags(meta)
            meta.owner = player.name
            meta.displayName = "${DARK_PURPLE}Change Your Miner's Skin!"

            val lore = arrayListOf(
                    "",
                    "$AQUA>> Click to choose!"
            )

            meta.lore = lore
            it.itemMeta = meta

            item.addUnsafeEnchantment(Enchantment.DIG_SPEED, 1)

        }

        return ClickableItem.of(itemWithMeta) {
            //plugin.listener.skinChangePlayers.add(player)
            //player.sendMessage("${GREEN}Please enter the username of the player you want your to miner to become.")
            plugin.skinChangerInventory.open(player)
        }
    }



}