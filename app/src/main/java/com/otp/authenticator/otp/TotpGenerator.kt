package com.otp.authenticator.otp

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

object TotpGenerator {

    private const val DIGITS = 6
    private const val TIME_STEP = 30L
    private const val ALGORITHM = "HmacSHA1"

    fun generate(secret: ByteArray, timeMillis: Long = System.currentTimeMillis()): String {
        val counter = timeMillis / 1000 / TIME_STEP
        val counterBytes = ByteArray(8)
        for (i in 0 until 8) {
            counterBytes[7 - i] = ((counter shr (i * 8)) and 0xff).toByte()
        }
        val hash = hmacSha1(secret, counterBytes)
        val offset = hash.last().toInt() and 0x0f
        val binary = ((hash[offset].toInt() and 0x7f) shl 24) or
                ((hash[offset + 1].toInt() and 0xff) shl 16) or
                ((hash[offset + 2].toInt() and 0xff) shl 8) or
                (hash[offset + 3].toInt() and 0xff)
        val otp = binary % 10.0.pow(DIGITS).toInt()
        return otp.toString().padStart(DIGITS, '0')
    }

    fun remainingSeconds(timeMillis: Long = System.currentTimeMillis()): Int {
        return (TIME_STEP - (timeMillis / 1000 % TIME_STEP)).toInt()
    }

    fun progressFraction(timeMillis: Long = System.currentTimeMillis()): Float {
        val elapsed = (timeMillis / 1000 % TIME_STEP).toFloat()
        return elapsed / TIME_STEP.toFloat()
    }

    private fun hmacSha1(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance(ALGORITHM)
        mac.init(SecretKeySpec(key, ALGORITHM))
        return mac.doFinal(data)
    }

    private val BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    fun decodeBase32(encoded: String): ByteArray {
        var input = encoded.uppercase().trim().replace("=", "")
        input = input.filter { it in BASE32_ALPHABET }

        val result = mutableListOf<Byte>()
        var buffer = 0
        var bitsLeft = 0

        for (c in input) {
            val value = BASE32_ALPHABET.indexOf(c)
            if (value < 0) continue
            buffer = (buffer shl 5) or value
            bitsLeft += 5
            if (bitsLeft >= 8) {
                bitsLeft -= 8
                result.add((buffer shr bitsLeft).toByte())
            }
        }

        return result.toByteArray()
    }
}
