package com.example

import com.example.data.SystemControlConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SystemControlAndResetTest {

    @Test
    fun `resolveEffectiveSecurityKey defaults to 17082006 when Firebase has no key`() {
        val resolved = SystemControlConfig.resolveEffectiveSecurityKey(
            firestoreSnapshot = null,
            rtdbSecurityKey = null,
            rtdbPin = null
        )
        assertEquals("17082006", resolved)
        assertTrue(SystemControlConfig.verifySecurityKey("17082006", resolved))
    }

    @Test
    fun `updated key in Firestore rejects default 17082006 and allows only updated key`() {
        val resolved = SystemControlConfig.resolveEffectiveSecurityKey(
            firestoreSnapshot = SystemControlConfig.RemoteSecurityKeySnapshot(
                securityKey = "99887766",
                pin = "17082006",
                updatedAtMillis = 1000L
            ),
            rtdbSecurityKey = "17082006",
            rtdbPin = "17082006",
            rtdbUpdatedAtMillis = 500L
        )
        assertEquals("99887766", resolved)
        assertFalse(SystemControlConfig.verifySecurityKey("17082006", resolved))
        assertTrue(SystemControlConfig.verifySecurityKey("99887766", resolved))
        assertFalse(SystemControlConfig.verifySecurityKey("", resolved))
        assertFalse(SystemControlConfig.verifySecurityKey("   ", resolved))
    }

    @Test
    fun `updated key in RTDB rejects default 17082006 and allows only updated key`() {
        val resolved = SystemControlConfig.resolveEffectiveSecurityKey(
            firestoreSnapshot = SystemControlConfig.RemoteSecurityKeySnapshot(
                securityKey = "17082006",
                pin = "17082006",
                updatedAtMillis = 500L
            ),
            rtdbSecurityKey = "77665544",
            rtdbPin = "17082006",
            rtdbUpdatedAtMillis = 2000L
        )
        assertEquals("77665544", resolved)
        assertFalse(SystemControlConfig.verifySecurityKey("17082006", resolved))
        assertTrue(SystemControlConfig.verifySecurityKey("77665544", resolved))
    }

    @Test
    fun `updated pin in Firestore rejects default 17082006 when securityKey is still default`() {
        val resolved = SystemControlConfig.resolveEffectiveSecurityKey(
            firestoreSnapshot = SystemControlConfig.RemoteSecurityKeySnapshot(
                securityKey = "17082006",
                pin = "20270817"
            ),
            rtdbSecurityKey = "17082006",
            rtdbPin = "17082006"
        )
        assertEquals("20270817", resolved)
        assertFalse(SystemControlConfig.verifySecurityKey("17082006", resolved))
        assertTrue(SystemControlConfig.verifySecurityKey("20270817", resolved))
    }

    @Test
    fun `resolveSystemShutdownStatus uses newest timestamp across Firestore and RTDB`() {
        assertTrue(
            SystemControlConfig.resolveSystemShutdownStatus(
                firestoreStatus = SystemControlConfig.SystemShutdownStatus(isShutdown = true, updatedAtMillis = 2000L),
                rtdbStatus = SystemControlConfig.SystemShutdownStatus(isShutdown = false, updatedAtMillis = 1000L)
            )
        )
        assertFalse(
            SystemControlConfig.resolveSystemShutdownStatus(
                firestoreStatus = SystemControlConfig.SystemShutdownStatus(isShutdown = true, updatedAtMillis = 1000L),
                rtdbStatus = SystemControlConfig.SystemShutdownStatus(isShutdown = false, updatedAtMillis = 3000L)
            )
        )
        assertFalse(
            SystemControlConfig.resolveSystemShutdownStatus(
                firestoreStatus = null,
                rtdbStatus = null
            )
        )
    }

    @Test
    fun `system shutdown screen and admin panel classes load cleanly`() {
        val shutdownScreenCls = Class.forName("com.example.ui.screens.SystemShutdownScreenKt")
        val adminScreenCls = Class.forName("com.example.ui.screens.AdminPanelScreenKt")
        assertNotNull(shutdownScreenCls)
        assertNotNull(adminScreenCls)
        for (m in adminScreenCls.declaredMethods) {
            assertTrue(
                "Method ${m.name} has ${m.parameterCount} params, must be <= 255",
                m.parameterCount <= 255
            )
        }
    }
}
