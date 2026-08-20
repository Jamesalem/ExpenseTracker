package com.example.expensetracker.data.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityUtilTest {

    @Test
    fun testPinHashingAndVerification() {
        val pin = "1234"
        val salt = SecurityUtil.generateSalt()
        val hash = SecurityUtil.hashPin(pin, salt)

        assertNotEquals(pin, hash)
        assertTrue(hash.startsWith("$salt$"))

        // Verification success
        assertTrue(SecurityUtil.verifyPin("1234", hash))

        // Verification fail
        assertFalse(SecurityUtil.verifyPin("0000", hash))
        assertFalse(SecurityUtil.verifyPin("1235", hash))
    }

    @Test
    fun testLegacyPlaintextPinFallback() {
        val legacyPin = "5678"
        assertTrue(SecurityUtil.verifyPin("5678", legacyPin))
        assertFalse(SecurityUtil.verifyPin("1234", legacyPin))
    }
}
