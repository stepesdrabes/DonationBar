package cz.stepes.donationbar.util.extension

import net.md_5.bungee.api.ChatColor
import java.util.regex.Matcher
import java.util.regex.Pattern

private val HEX_PATTERN = Pattern.compile("#[a-fA-F\\d]{6}")

fun String.colorify(): String = ChatColor.translateAlternateColorCodes('&', replaceHexColors())

private fun String.replaceHexColors(): String {
    var text = this
    var match: Matcher = HEX_PATTERN.matcher(text)

    while (match.find()) {
        val color = text.substring(match.start(), match.end())
        text = text.replace(color, ChatColor.of(color).toString())
        match = HEX_PATTERN.matcher(text)
    }

    return text
}