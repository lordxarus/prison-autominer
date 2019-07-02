package com.lordxarus.autominer

import com.lordxarus.autominer.util.setItemNameLore
import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object Items {

    val timeTokenSmall = setItemNameLore(
            ItemStack(Material.PRISMARINE_SHARD),
            "${ChatColor.RESET}${ChatColor.DARK_GREEN}Small AutoMiner Token",
            arrayListOf(
                    "${ChatColor.GRAY}Adds 3 minutes of mining time.",
                    "${ChatColor.GREEN}To use: ${ChatColor.GRAY}Right click the air."
            ))

    val timeTokenMedium = setItemNameLore(
            ItemStack(Material.PRISMARINE_SHARD),
            "${ChatColor.RESET}${ChatColor.DARK_GREEN}Medium AutoMiner Token",
            arrayListOf(
                    "${ChatColor.GRAY}Adds 10 minutes of mining time.",
                    "${ChatColor.GREEN}To use: ${ChatColor.GRAY}Right click the air."
            ))

    val timeTokenBig = setItemNameLore(
            ItemStack(Material.PRISMARINE_SHARD),
            "${ChatColor.RESET}${ChatColor.DARK_GREEN}Large AutoMiner Token",
            arrayListOf(
                    "${ChatColor.GRAY}Adds 25 minutes of mining time.",
                    "${ChatColor.GREEN}To use: ${ChatColor.GRAY}Right click the air."
            ))

    val tokens = arrayListOf(timeTokenSmall.itemMeta.displayName, timeTokenMedium.itemMeta.displayName, timeTokenBig.itemMeta.displayName)
}