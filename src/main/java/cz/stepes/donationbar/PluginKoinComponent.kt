package cz.stepes.donationbar

import org.koin.core.component.KoinComponent

interface PluginKoinComponent : KoinComponent {

    override fun getKoin() = koinApp.koin
}