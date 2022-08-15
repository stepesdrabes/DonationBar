package cz.stepes.donationbar

import cz.stepes.donationbar.command.DonationBarCommand
import cz.stepes.donationbar.listener.PlayerListener
import cz.stepes.donationbar.manager.BarManager
import cz.stepes.donationbar.manager.ConfigManager
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.KoinApplication
import org.koin.core.component.inject
import org.koin.dsl.koinApplication
import org.koin.dsl.module

lateinit var koinApp: KoinApplication

class DonationBarPlugin : PluginKoinComponent, JavaPlugin() {

    private val instance: DonationBarPlugin = this

    private val barManager: BarManager by inject()

    override fun onEnable() {
        koinApp = koinApplication {
            modules(
                module {
                    single { instance }
                    single { ConfigManager() }
                    single { BarManager() }
                }
            )
        }

        saveDefaultConfig()

        try {
            barManager.initialize()
        } catch (exception: Exception) {
            logger.severe("There was an error while creating a donation bar: " + exception.message)
            pluginLoader.disablePlugin(this)
            return
        }

        Bukkit.getPluginManager().registerEvents(PlayerListener(), this)

        DonationBarCommand().let {
            getCommand("donationbar")?.setExecutor(it)
            getCommand("donationbar")?.setTabCompleter(it)
        }

        Bukkit.getOnlinePlayers().forEach { barManager.addPlayer(it) }

        logger.info("Plugin has been successfully enabled!")
    }

    override fun onDisable() {
        barManager.cleanUp()

        logger.info("Plugin has been disabled!")
    }
}