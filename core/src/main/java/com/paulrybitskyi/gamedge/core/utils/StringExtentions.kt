package com.paulrybitskyi.gamedge.core.utils

fun String.getAccessTokenFromURL(): String {
    val startIdentifier = "access_token"
    val endIdentifier = "&scope"

    val startIndex = this.indexOf(startIdentifier) + startIdentifier.length + 1
    val lastIndex = this.indexOf(endIdentifier)

    return this.substring(startIndex, lastIndex)
}
