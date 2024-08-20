package com.paulrybitskyi.gamedge.core.utils

import java.text.DecimalFormat

fun String.getAccessTokenFromURL(): String {
    val startIdentifier = "access_token"
    val endIdentifier = "&scope"

    val startIndex = this.indexOf(startIdentifier) + startIdentifier.length + 1
    val lastIndex = this.indexOf(endIdentifier)

    return this.substring(startIndex, lastIndex)
}

val decimalFormat = DecimalFormat("#.#")
fun Long.formattedCount(): String {
    return if (this < 10000) {
        this.toString()
    } else if (this < 1000000) {
        "${decimalFormat.format(this.div(1000))}K"
    } else if (this < 1000000000) {
        "${decimalFormat.format(this.div(1000000))}M"
    } else {
        "${decimalFormat.format(this.div(1000000000))}B"
    }
}
