package cz.stepes.donationbar.manager

import com.google.gson.GsonBuilder
import cz.stepes.donationbar.DonationBarPlugin
import cz.stepes.donationbar.PluginKoinComponent
import cz.stepes.donationbar.data.remote.FundraisingEventResponse
import cz.stepes.donationbar.data.remote.HttpRoutes
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarFlag
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import org.koin.core.component.inject
import java.lang.Runnable
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.math.min

class BarManager : PluginKoinComponent {

    private val plugin: DonationBarPlugin by inject()
    private val configManager: ConfigManager by inject()

    private var goal: Int = 0
    private var amountRaised: Int = 0

    private lateinit var barKey: NamespacedKey
    private lateinit var donationBar: BossBar
    private var updateTask: BukkitTask? = null

    fun initialize() {
        barKey = NamespacedKey(plugin, "DONATION_BAR_KEY")
        donationBar = Bukkit.createBossBar(
            barKey,
            createBarTitle(),
            BarColor.valueOf(configManager.message("settings.barColor", withPrefix = false)),
            BarStyle.valueOf(configManager.message("settings.barStyle", withPrefix = false)),
        )

        donationBar.removeFlag(BarFlag.CREATE_FOG)
        donationBar.removeFlag(BarFlag.DARKEN_SKY)
        donationBar.removeFlag(BarFlag.PLAY_BOSS_MUSIC)

        val updatePeriod = plugin.config.getInt("settings.updatePeriodSeconds") * 20L
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            if (!plugin.config.getBoolean("settings.barEnabled")) return@Runnable
            updateBar()
        }, updatePeriod, updatePeriod)
    }

    fun addPlayer(player: Player) {
        if (!plugin.config.getBoolean("settings.barEnabled")) return
        if (player.hasPermission("donationbar.hide")) return
        donationBar.addPlayer(player)
    }

    fun removePlayer(player: Player) = donationBar.removePlayer(player)

    fun reloadBar() {
        donationBar.color = BarColor.valueOf(configManager.message("settings.barColor", withPrefix = false))
        donationBar.style = BarStyle.valueOf(configManager.message("settings.barStyle", withPrefix = false))
    }

    private fun updateBar() {
        CoroutineScope(Dispatchers.IO).launch { getData() }

        donationBar.progress = try {
            val num = amountRaised.toDouble() / goal.toDouble()
            if (num.isNaN()) throw Exception()
            min(num, 1.0)
        } catch (_: Exception) {
            0.0
        }

        donationBar.setTitle(createBarTitle())

        Bukkit.getOnlinePlayers().forEach { if (it.hasPermission("donationbar.hide")) donationBar.removePlayer(it) }
    }

    private fun getData() {
        var goalTotal = 0
        var raisedTotal = 0

        plugin.config.getStringList("settings.tiltifyFundraisingEventIDs").forEach { url ->
            try {
                val client = HttpClient.newBuilder().build()

                val request = HttpRequest.newBuilder()
                    .uri(URI.create("${HttpRoutes.FUNDRAISING_EVENT}/$url"))
                    .setHeader("Authorization", "Bearer ${configManager.message("settings.tiltifyApiKey", withPrefix = false)}")
                    .build()

                val response = client.send(request, HttpResponse.BodyHandlers.ofString())

                GsonBuilder().create().fromJson(response.body(), FundraisingEventResponse::class.java)?.let {
                    goalTotal += it.fundraisingEventData.goal.toInt()
                    raisedTotal += it.fundraisingEventData.amountRaised.toInt()
                }
            } catch (exception: Exception) {
                plugin.logger.severe("Error while loading donations data: $exception")
            }
        }

        goal = goalTotal
        amountRaised = raisedTotal
    }

    fun toggleBar(enabled: Boolean) {
        when (enabled) {
            true -> Bukkit.getOnlinePlayers().forEach { donationBar.addPlayer(it) }
            false -> donationBar.removeAll()
        }

        plugin.config.set("settings.barEnabled", enabled)
        plugin.saveConfig()
    }

    private fun createBarTitle() = configManager.message("donationBar", withPrefix = false)
        .replace("%raised%", amountRaised.toString())
        .replace("%goal%", goal.toString())

    fun cleanUp() {
        updateTask?.cancel()
        updateTask = null

        donationBar.removeAll()
    }
}