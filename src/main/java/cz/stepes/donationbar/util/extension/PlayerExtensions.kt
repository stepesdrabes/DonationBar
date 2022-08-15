package cz.stepes.donationbar.util.extension

import org.bukkit.Sound
import org.bukkit.entity.Player

fun Player.playSound(sound: Sound) = playSound(location, sound, 1.0f, 1.0f)