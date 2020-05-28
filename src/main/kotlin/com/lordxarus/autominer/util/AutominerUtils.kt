package com.lordxarus.autominer.util

import com.lordxarus.autominer.AutominerPlugin
import com.lordxarus.autominer.npc.AutominerModel
import com.lordxarus.autominer.npc.AutominerTrait
import com.sk89q.worldedit.Vector
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.ApplicableRegionSet
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import me.mrCookieSlime.QuickSell.Shop
import net.citizensnpcs.api.npc.NPC
import net.md_5.bungee.api.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.plugin.java.JavaPlugin


val plugin by lazy { JavaPlugin.getPlugin(AutominerPlugin::class.java)!! }

//TODO
val debug = false

fun getWorldGuardRegions(loc: Location): ApplicableRegionSet {
    val container = plugin.server.pluginManager.getPlugin("WorldGuard") as WorldGuardPlugin
    val regions = container.regionContainer.get(loc.world)
    return regions!!.getApplicableRegions(Vector(loc.x, loc.y, loc.z))
}

fun getMineRegions(loc: Location): List<ProtectedRegion> {
    return getWorldGuardRegions(loc).regions.filter { it.id.contains("-am-") }
}

fun getShopFromWorldGuard(loc: Location): Shop? {

    val regions = getMineRegions(loc)
    if (regions.isNotEmpty()) {
        return Shop.getShop(regions[0].id.replace("-am-", ""))
    }
    return null
}

fun isLocInAnyShop(loc: Location) = getShopFromWorldGuard(loc) != null


fun getTotalPrice(model: AutominerModel, shop: Shop): Double {
    var priceTotal = 0.0
    model.mined.forEach { priceTotal += shop.prices.getPrice(it) }
    return priceTotal * 20
}

fun getModel(player: Player): AutominerModel {
    return plugin.npcs[player]!!.getTrait(AutominerTrait::class.java).autominerModel
}

fun getShop(npc: NPC, player: Player): Shop {
    return if (npc.isSpawned && isLocInAnyShop(npc.entity.location)) {
        getShopFromWorldGuard(npc.entity.location)!!
    } else {
        Shop.getHighestShop(player)
    }
}


fun setItemNameLore(item: ItemStack, name: String, lore: ArrayList<String>): ItemStack {
    val meta = item.itemMeta
    meta.displayName = name
    meta.lore = lore
    setItemFlags(meta)
    item.itemMeta = meta
    return item
}

fun addToItemLore(item: ItemStack, lore: ArrayList<String>): ItemStack {
    val meta = item.itemMeta
    val combLore = meta.lore
    combLore.addAll(lore)
    meta.lore = combLore
    setItemFlags(meta)
    item.itemMeta = meta
    return item
}

fun addPermissionLore(item: ItemStack, player: Player, permission: String) {
    var text = "${ChatColor.GRAY}Can Use:${ChatColor.RESET} "

    text = if (player.hasPermission(permission)) {
        text.plus("${ChatColor.GREEN}Yes")
    } else {
        text.plus("${ChatColor.RED}No")
    }

    addToItemLore(item, arrayListOf(
            "",
            text
    ))
}

fun setItemFlags(meta: ItemMeta) {
    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS)
}

fun setHeadOwner(item: ItemStack, owner: String) {
    val meta = item.itemMeta as SkullMeta
    meta.owner = owner
    item.itemMeta = meta
}

fun addCommas(d: Double): String {
    val num = d.toInt()
    val out = StringBuilder(num.toString())
    if (countDigits(num) > 3) {
        println(countDigits(num))
        for (i in countDigits(num) downTo 0) {
            println("Index: $i")
            println("Rem: ${i.rem(3)}")
            val cand = ((countDigits(num) - i))
            if ((cand.rem(3)) == 0 && cand != 0) {
                println("asdf")
                out.insert(i, ",")
            }
        }
    }
    println(out.toString())
    return out.toString()
}

fun countDigits(num: Int): Int {
    var i = num
    var count = 0
    while(i != 0) {
        i /= 10
        count++
    }
    println("Counts: $count")
    println("Number: $num")
    return count
}