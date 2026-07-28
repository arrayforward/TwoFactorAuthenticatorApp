package com.otp.authenticator.data

import java.util.UUID

data class OtpAccount(
    val id: String = UUID.randomUUID().toString(),
    val issuer: String,
    val label: String,
    val secret: String,     // Base32 encoded
    val algorithm: String = "SHA1",
    val digits: Int = 6,
    val period: Int = 30
) {
    val displayName: String
        get() = if (issuer.isNotBlank()) "$issuer ($label)" else label

    val issuerOrLabel: String
        get() = issuer.ifBlank { label }

    val otpAuthUrl: String
        get() {
            val encodedIssuer = java.net.URLEncoder.encode(issuer, "UTF-8")
            val encodedLabel = java.net.URLEncoder.encode(label, "UTF-8")
            return "otpauth://totp/$encodedIssuer:$encodedLabel?secret=$secret&issuer=$encodedIssuer&algorithm=$algorithm&digits=$digits&period=$period"
        }
}
