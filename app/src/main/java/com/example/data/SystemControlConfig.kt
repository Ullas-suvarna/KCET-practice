package com.example.data

object SystemControlConfig {
    const val DEFAULT_SYSTEM_SECURITY_KEY = "17082006"

    data class RemoteSecurityKeySnapshot(
        val securityKey: String? = null,
        val pin: String? = null,
        val updatedAtMillis: Long = 0L
    )

    data class SystemShutdownStatus(
        val isShutdown: Boolean = false,
        val updatedAtMillis: Long = 0L,
        val updatedBy: String = ""
    )

    /**
     * Resolves the single authoritative security key from Firebase Firestore and Realtime Database.
     *
     * Rules:
     * - Initial default key is "17082006" only when neither Firestore nor RTDB has a stored key.
     * - If a key is updated in Firebase (either Firestore `admin_config/security` or RTDB `admin_config`),
     *   ONLY the updated key is returned (never falling back to "17082006" if Firebase holds a newer/custom key).
     */
    fun resolveEffectiveSecurityKey(
        firestoreSnapshot: RemoteSecurityKeySnapshot?,
        rtdbSecurityKey: String?,
        rtdbPin: String? = null,
        rtdbUpdatedAtMillis: Long = 0L
    ): String {
        val fsSecKey = firestoreSnapshot?.securityKey?.trim()?.takeIf { it.isNotEmpty() }
        val fsPin = firestoreSnapshot?.pin?.trim()?.takeIf { it.isNotEmpty() }
        val fsResolved = when {
            fsSecKey != null && fsSecKey != DEFAULT_SYSTEM_SECURITY_KEY -> fsSecKey
            fsPin != null && fsPin != DEFAULT_SYSTEM_SECURITY_KEY -> fsPin
            else -> fsSecKey ?: fsPin
        }

        val rtdbSec = rtdbSecurityKey?.trim()?.takeIf { it.isNotEmpty() }
        val rtdbP = rtdbPin?.trim()?.takeIf { it.isNotEmpty() }
        val rtdbResolved = when {
            rtdbSec != null && rtdbSec != DEFAULT_SYSTEM_SECURITY_KEY -> rtdbSec
            rtdbP != null && rtdbP != DEFAULT_SYSTEM_SECURITY_KEY -> rtdbP
            else -> rtdbSec ?: rtdbP
        }

        val fsTs = firestoreSnapshot?.updatedAtMillis ?: 0L

        return when {
            fsResolved != null && rtdbResolved != null -> {
                if (fsResolved == rtdbResolved) {
                    fsResolved
                } else if (fsTs > 0L && rtdbUpdatedAtMillis > 0L) {
                    if (rtdbUpdatedAtMillis > fsTs) rtdbResolved else fsResolved
                } else if (rtdbResolved != DEFAULT_SYSTEM_SECURITY_KEY && fsResolved == DEFAULT_SYSTEM_SECURITY_KEY) {
                    rtdbResolved
                } else if (fsResolved != DEFAULT_SYSTEM_SECURITY_KEY && rtdbResolved == DEFAULT_SYSTEM_SECURITY_KEY) {
                    fsResolved
                } else {
                    fsResolved
                }
            }
            fsResolved != null -> fsResolved
            rtdbResolved != null -> rtdbResolved
            else -> DEFAULT_SYSTEM_SECURITY_KEY
        }
    }

    /**
     * Strictly validates the entered security key against the resolved Firebase key.
     * Rejects empty strings and rejects the default key whenever Firebase holds an updated key.
     */
    fun verifySecurityKey(enteredKey: String, effectiveFirebaseKey: String): Boolean {
        val cleanEntered = enteredKey.trim()
        val cleanTarget = effectiveFirebaseKey.trim().ifBlank { DEFAULT_SYSTEM_SECURITY_KEY }
        return cleanEntered.isNotEmpty() && cleanEntered == cleanTarget
    }

    fun resolveSystemShutdownStatus(
        firestoreStatus: SystemShutdownStatus?,
        rtdbStatus: SystemShutdownStatus?
    ): Boolean {
        return when {
            firestoreStatus != null && rtdbStatus != null -> {
                if (rtdbStatus.updatedAtMillis > firestoreStatus.updatedAtMillis) {
                    rtdbStatus.isShutdown
                } else {
                    firestoreStatus.isShutdown
                }
            }
            firestoreStatus != null -> firestoreStatus.isShutdown
            rtdbStatus != null -> rtdbStatus.isShutdown
            else -> false
        }
    }
}
