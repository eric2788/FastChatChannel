package com.ericlam.mc.fastchatchannel

import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.plugin.java.JavaPlugin
import java.text.MessageFormat
import java.util.regex.Pattern

class FCChannel : JavaPlugin(), Listener {

    private var prefix = '#'
    private val chatPattern
        get() = Pattern.compile("^$prefix(?<channel>\\w+)\\s+\\S", Pattern.UNICODE_CHARACTER_CLASS)
    private val channelPattern
        get() = Regex("^$prefix\\p{L}+\\s+", RegexOption.IGNORE_CASE)

    override fun onEnable() {
        saveDefaultConfig().also { reloadConfig() }
        server.pluginManager.registerEvents(this, this)
    }


    override fun reloadConfig() {
        super.reloadConfig().also { prefix = config.getString("prefix")?.takeIf { it.isNotBlank() }?.get(0) ?: prefix }
    }

    @EventHandler
    fun onPlayerChat(e: AsyncPlayerChatEvent) {
        val matcher = chatPattern.matcher(e.message)
        if (!matcher.find()) return
        val channel = matcher.group("channel") ?: return
        if (!e.player.hasPermission("channel.$channel")) return
        if (channel == "reload") reloadConfig().also { e.player.sendMessage("${ChatColor.GREEN} Reloaded") }
        e.isCancelled = true
        val message = channelPattern.replace(e.message, "")
        val player = e.player.displayName
        val format = config.getString("format") ?: "[$channel][$player] $message"
        val chat = ChatColor.translateAlternateColorCodes('&', MessageFormat.format(format, channel, player, message))
        server.onlinePlayers.filter { it.hasPermission("channel.$channel") }.forEach { it.sendMessage(chat) }
    }
}