package cz.stepes.donationbar.listener

import cz.stepes.donationbar.PluginKoinComponent
import cz.stepes.donationbar.manager.BarManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.koin.core.component.inject

class PlayerListener : PluginKoinComponent, Listener {

    private val bossBarManager: BarManager by inject()

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) = bossBarManager.addPlayer(event.player)

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) = bossBarManager.removePlayer(event.player)
}