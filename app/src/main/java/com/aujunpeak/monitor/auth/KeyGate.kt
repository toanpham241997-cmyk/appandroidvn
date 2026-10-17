package com.aujunpeak.monitor.auth

import java.security.MessageDigest

/**
 * Demo key only. This offline check can be bypassed by reverse-engineering an APK.
 * For commercial key licenses, replace with HTTPS backend verification and an expiring session.
 */
object KeyGate {
    // SHA-256 for demo key AUJUN-2026 (see README for generation instructions).
    private const val DEMO_KEY_SHA256 = "6f3123702c3421f91a9673a578f27ef7e4b4d9ed3fdbb10d0c1c03ca6adeab18"

    fun isValid(key: String): Boolean {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(key.trim().uppercase().toByteArray(Charsets.UTF_8))
        val expected = hexToBytes(DEMO_KEY_SHA256)
        return MessageDigest.isEqual(digest, expected)
    }

    private fun hexToBytes(hex: String): ByteArray = ByteArray(hex.length / 2) { index ->
        hex.substring(index * 2, index * 2 + 2).toInt(16).toByte()
    }
}
