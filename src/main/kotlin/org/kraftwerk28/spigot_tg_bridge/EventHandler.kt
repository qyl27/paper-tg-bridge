package org.kraftwerk28.spigot_tg_bridge

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.advancement.Advancement
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.awt.TextComponent

class EventHandler(
    private val plugin: Plugin,
    private val config: Configuration,
    private val tgBot: TgBot,
) : Listener {

    @EventHandler
    fun onPlayerChat(event: AsyncChatEvent) {
        if (!config.logFromMCtoTG) return
        event.run {
            var name = ""
            plugin.chat?.let {
                name += it.getPlayerPrefix(player) + " "
            }
            name += PlainTextComponentSerializer.plainText().serialize(player.displayName())
            sendMessage(PlainTextComponentSerializer.plainText().serialize(message()), name)
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (!config.logJoinLeave) return
        var name = ""
        plugin.chat?.let {
            name += it.getPlayerPrefix(event.player) + " "
        }
        name += PlainTextComponentSerializer.plainText().serialize(event.player.displayName())
        val text = config.joinString.replace("%username%", name)
        sendMessage(text)
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        if (!config.logJoinLeave) return
        var name = ""
        plugin.chat?.let {
            name += it.getPlayerPrefix(event.player) + " "
        }
        name += PlainTextComponentSerializer.plainText().serialize(event.player.displayName())
        val text = config.leaveString.replace("%username%", name)
        sendMessage(text)
    }

    @EventHandler
    fun onPlayerDied(event: PlayerDeathEvent) {
        if (!config.logDeath) return
        event.deathMessage()?.let { it ->
            val username = PlainTextComponentSerializer.plainText().serialize(event.player.displayName())
            var name = ""
            plugin.chat?.let {
                name += it.getPlayerPrefix(event.player) + " "
            }
            name += username
            val text = PlainTextComponentSerializer.plainText().serialize(it).replace(username, "<b>$name</b>")
            sendMessage(text)
        }
    }

    @EventHandler
    fun onPlayerAsleep(event: PlayerBedEnterEvent) {
        if (!config.logPlayerAsleep) return
        if (event.bedEnterResult != PlayerBedEnterEvent.BedEnterResult.OK)
            return
        var name = ""
        plugin.chat?.let {
            name += it.getPlayerPrefix(event.player) + " "
        }
        name += PlainTextComponentSerializer.plainText().serialize(event.player.displayName())
        val text = config.asleepString.replace("%username%", name)
        sendMessage(text)
    }

    @EventHandler
    fun onPlayerAdvancementDone(event: PlayerAdvancementDoneEvent) {
        if(!config.logPlayerAdvancement) return
        plugin.server.logger.info("PlayerAdvancementDoneEvent")
        var name = ""
        plugin.chat?.let {
            name += it.getPlayerPrefix(event.player) + " "
        }
        name += PlainTextComponentSerializer.plainText().serialize(event.player.displayName())
        val text = config.advancementString
            .replace("%username%", name)
            .replace("%advancement%", PlainTextComponentSerializer.plainText().serialize(event.advancement.display!!.title()))
        sendMessage(text)
    }

    private fun sendMessage(text: String, username: String? = null) {
        plugin.launch {
            tgBot.sendMessageToTelegram(text, username)
        }
    }
}
