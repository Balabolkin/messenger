package com.eltex.messengerapp.util

import com.eltex.messengerapp.BuildConfig

/**
 * Returns a secure HTTPS URL for the given path.
 * If the URL is relative, appends the BASE_HOST using HTTPS.
 * If the URL is absolute and starts with http://, replaces it with https://.
 */
fun toSecureUrl(url: String?): String? {
    if (url == null) return null
    val trimmed = url.trim()
    val fullUrl = if (trimmed.startsWith("http")) {
        trimmed
    } else {
        // Ensure starting slash is handled correctly
        val path = if (trimmed.startsWith("/")) trimmed else "/$trimmed"
        "https://${BuildConfig.BASE_HOST}$path"
    }
    return if (fullUrl.startsWith("http://")) {
        fullUrl.replaceFirst("http://", "https://")
    } else {
        fullUrl
    }
}
