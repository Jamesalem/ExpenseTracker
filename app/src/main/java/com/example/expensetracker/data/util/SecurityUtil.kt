package com.example.expensetracker.data.util

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.Base64

/**
 * Cryptographic utility for securing PINs and sensitive local data.
 * Protects against plaintext exposure using SHA-256 with salted key derivation.
 */
object SecurityUtil {

    private const val SALT_BYTES = 16

    /**
     * Generates a secure random 16-byte salt encoded as Base64.
     */
    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_BYTES)
        random.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    /**
     * Hashes a PIN with a salt using SHA-256.
     * Output format: "SALT_HEX:HASH_HEX" or "SALT_B64$HASH_B64"
     */
    fun hashPin(pin: String, salt: String = generateSalt()): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(salt.toByteArray(Charsets.UTF_8))
            val hash = digest.digest(pin.toByteArray(Charsets.UTF_8))
            val hashB64 = Base64.getEncoder().encodeToString(hash)
            "$salt$$hashB64"
        } catch (e: NoSuchAlgorithmException) {
            // Fallback
            "$salt$${pin.hashCode()}"
        }
    }

    /**
     * Verifies if an entered plaintext PIN matches the stored salted hash.
     * Supports backward compatibility with legacy 4-digit plaintext PINs.
     */
    fun verifyPin(enteredPin: String, storedHashOrPlain: String?): Boolean {
        if (storedHashOrPlain.isNullOrBlank()) return true
        if (enteredPin.length != 4) return false

        // Check if stored format is "SALT$HASH"
        return if (storedHashOrPlain.contains("$")) {
            val parts = storedHashOrPlain.split("$")
            if (parts.size == 2) {
                val salt = parts[0]
                val expectedHash = parts[1]
                val computed = hashPin(enteredPin, salt)
                computed == storedHashOrPlain
            } else {
                false
            }
        } else {
            // Legacy plaintext comparison
            enteredPin == storedHashOrPlain
        }
    }
}
