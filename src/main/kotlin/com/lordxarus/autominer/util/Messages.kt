package com.lordxarus.autominer.util

import org.bukkit.ChatColor
import org.bukkit.configuration.file.FileConfiguration

class Messages(val config: FileConfiguration) {

    val noPermission = trans(config.getString("messages.no-permission"))
    val noImpl = trans(config.getString("messages.no-impl"))
    val sell = trans(config.getString("messages.sell"))
    val noRegion = trans(config.getString("messages.no-region"))
    val changeSkin = trans(config.getString("messages.change-skin"))
    val outOfTime = trans(config.getString("messages.out-of-time"))
    val tokenApplied = trans(config.getString("messages.token-applied"))


    private fun trans(perm: String) = ChatColor.translateAlternateColorCodes('&', perm)

}