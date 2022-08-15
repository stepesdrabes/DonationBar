package cz.stepes.donationbar.manager

import cz.stepes.donationbar.DonationBarPlugin
import cz.stepes.donationbar.PluginKoinComponent
import cz.stepes.donationbar.util.extension.colorify
import org.bukkit.Sound
import org.koin.core.component.inject

class ConfigManager : PluginKoinComponent {

    private val plugin: DonationBarPlugin by inject()

    fun message(path: String, withPrefix: Boolean = true) = ((if (withPrefix) plugin.config.getString("prefix") + " " else "") + plugin.config.getString(path)).colorify()

    fun sound(path: String): Sound {
        plugin.config.getString("sounds.$path")?.let { return Sound.valueOf(it) }
        return Sound.ENTITY_ITEM_BREAK
    }
}