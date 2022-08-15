package cz.stepes.donationbar.command

import cz.stepes.donationbar.DonationBarPlugin
import cz.stepes.donationbar.PluginKoinComponent
import cz.stepes.donationbar.manager.BarManager
import cz.stepes.donationbar.manager.ConfigManager
import cz.stepes.donationbar.util.extension.playSound
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.koin.core.component.inject

class DonationBarCommand : PluginKoinComponent, TabExecutor {

    private val plugin: DonationBarPlugin by inject()
    private val configManager: ConfigManager by inject()
    private val barManager: BarManager by inject()

    private val actions: List<String> = listOf("reload", "enable", "disable")

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(configManager.message("notPlayer"))
            return true
        }

        if (args.isEmpty() || !actions.contains(args[0].lowercase())) {
            sender.sendMessage(configManager.message("wrongUsage").replace("%usage%", command.usage))
            sender.playSound(configManager.sound("error"))
            return true
        }

        when (args[0].lowercase()) {
            "reload" -> reloadPlugin(sender)
            "enable" -> enableBar(sender)
            "disable" -> disableBar(sender)
        }

        return true
    }

    private fun reloadPlugin(player: Player) {
        plugin.reloadConfig()
        barManager.reloadBar()

        player.sendMessage(configManager.message("configReloaded"))
        player.playSound(configManager.sound("success"))
    }

    private fun enableBar(player: Player) {
        if (plugin.config.getBoolean("settings.barEnabled")) {
            player.sendMessage(configManager.message("barAlreadyEnabled"))
            player.playSound(configManager.sound("error"))
            return
        }

        barManager.toggleBar(true)

        player.sendMessage(configManager.message("barEnabled"))
        player.playSound(configManager.sound("success"))
    }

    private fun disableBar(player: Player) {
        if (!plugin.config.getBoolean("settings.barEnabled")) {
            player.sendMessage(configManager.message("barAlreadyDisabled"))
            player.playSound(configManager.sound("error"))
            return
        }

        barManager.toggleBar(false)

        player.sendMessage(configManager.message("barDisabled"))
        player.playSound(configManager.sound("success"))
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (args.size == 1) return actions
        return emptyList()
    }
}