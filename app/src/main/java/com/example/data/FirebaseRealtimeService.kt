package com.example.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume


data class OneToOneMessage(
    val messageId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val sentBySenior: Boolean = false,
    val isRead: Boolean = false
)

data class SeniorMentorProfile(
    val name: String = "",
    val bio: String = "",
    val specialty: String = "",
    val telegramOrInstagram: String = "",
    val password: String = "",
    val lastUpdated: Long = System.currentTimeMillis(),
    val email: String = "",
    val phone: String = "",
    val qualification: String = "",
    val experience: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "Active"
)

data class SeniorChatMessage(
    val messageId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val sentBySenior: Boolean = false,
    val avatarUrl: String? = null
)

data class UserPresence(
    val uid: String = "",
    val userName: String = "",
    val online: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val role: String = "Student"
)

data class LiveXpData(
    val uid: String = "",
    val userName: String = "",
    val xp: Int = 0,
    val rank: Int = 1,
    val targetRank: String = "Under 1000",
    val level: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
)

object FirebaseRealtimeService {
    private const val TAG = "FirebaseRealtimeService"
    const val RTDB_URL = "https://kcet-app-c0457-default-rtdb.asia-southeast1.firebasedatabase.app"

    val database: FirebaseDatabase by lazy {
        try {
            FirebaseDatabase.getInstance(RTDB_URL)
        } catch (e: Exception) {
            FirebaseDatabase.getInstance()
        }
    }

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private fun DataSnapshot.asSafeString(default: String = ""): String {
        return try {
            value?.toString() ?: default
        } catch (_: Exception) {
            default
        }
    }

    private fun DataSnapshot.asSafeStringOrNull(): String? {
        return try {
            value?.toString()
        } catch (_: Exception) {
            null
        }
    }

    private fun DataSnapshot.asSafeLong(default: Long = 0L): Long {
        return try {
            when (val raw = value) {
                is Number -> raw.toLong()
                is String -> raw.trim().toLongOrNull() ?: default
                else -> default
            }
        } catch (_: Exception) {
            default
        }
    }

    private fun DataSnapshot.asSafeBoolean(): Boolean? {
        return try {
            when (val raw = value) {
                is Boolean -> raw
                is Number -> raw.toInt() == 1
                is String -> when (raw.trim().lowercase()) {
                    "true", "1", "yes" -> true
                    "false", "0", "no" -> false
                    else -> null
                }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    // ==========================================
    // 1. LEADERBOARD (Node: /leaderboard)
    // Rule: .read: auth != null, .write: false
    // ==========================================
    fun observeLeaderboard(): Flow<List<LeaderboardEntry>> = callbackFlow {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            Log.d(TAG, "Unauthenticated user, returning empty leaderboard")
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val leaderboardRef = database.getReference("leaderboard")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val remoteEntries = mutableListOf<LeaderboardEntry>()
                for (child in snapshot.children) {
                    try {
                        val uidKey = child.key ?: ""
                        val name = child.child("name").getValue(String::class.java) ?: "KCET Aspirant"
                        val kcetTargetRank = child.child("kcetTargetRank").getValue(String::class.java)
                            ?: child.child("targetRank").getValue(String::class.java) ?: "Under 1000"
                        val points = child.child("points").getValue(Long::class.java)?.toInt()
                            ?: child.child("xp").getValue(Long::class.java)?.toInt() ?: 0
                        val accuracy = child.child("accuracy").getValue(Double::class.java)?.toFloat() ?: 85.0f
                        val testsCount = child.child("testsCount").getValue(Long::class.java)?.toInt() ?: 10
                        val rank = child.child("rank").getValue(Long::class.java)?.toInt() ?: 0
                        val avatarInitials = child.child("avatarInitials").getValue(String::class.java)
                            ?: if (name.length >= 2) name.substring(0, 2).uppercase() else "KC"
                        val isCurrentUser = (uidKey == currentUid)

                        remoteEntries.add(
                            LeaderboardEntry(
                                rank = rank,
                                name = name,
                                kcetTargetRank = kcetTargetRank,
                                points = points,
                                accuracy = accuracy,
                                testsCount = testsCount,
                                avatarInitials = avatarInitials,
                                isCurrentUser = isCurrentUser
                            )
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "Error parsing leaderboard entry: ${e.message}")
                    }
                }

                val sorted = remoteEntries.sortedWith(
                    compareByDescending<LeaderboardEntry> { it.points }
                        .thenByDescending { it.accuracy }
                        .thenByDescending { it.testsCount }
                ).mapIndexed { index, entry ->
                    entry.copy(
                        rank = index + 1,
                        isCurrentUser = entry.isCurrentUser || (entry.name.trim().lowercase() == currentUid.trim().lowercase())
                    )
                }

                trySend(sorted)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Leaderboard observe cancelled: ${error.message}")
            }
        }

        leaderboardRef.addValueEventListener(listener)
        awaitClose { leaderboardRef.removeEventListener(listener) }
    }

    // ==========================================
    // 2. LIVE SENIOR CHAT (Node: /seniorChats/$chatId/messages/$messageId)
    // Rule:
    // .validate: newData.hasChildren(['senderId', 'message', 'timestamp'])
    // .write: auth != null && newData.child('senderId').val() === auth.uid
    // ==========================================
    fun observeSeniorChatMessages(chatId: String): Flow<List<SeniorChatMessage>> = callbackFlow {
        val cleanChatId = chatId.replace(".", "_").replace("#", "_").replace("$", "_").replace("[", "_").replace("]", "_")
        val messagesRef = database.getReference("seniorChats/$cleanChatId/messages")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messagesList = mutableListOf<SeniorChatMessage>()
                for (child in snapshot.children) {
                    try {
                        val msgId = child.key ?: ""
                        val senderId = child.child("senderId").getValue(String::class.java) ?: ""
                        val senderName = child.child("senderName").getValue(String::class.java) ?: "User"
                        val message = child.child("message").getValue(String::class.java) ?: ""
                        val timestamp = child.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()
                        val sentBySeniorVal = child.child("sentBySenior").getValue(Boolean::class.java) 
                            ?: child.child("isSenior").getValue(Boolean::class.java) 
                            ?: false
                        val avatarUrl = child.child("avatarUrl").getValue(String::class.java)

                        if (message.isNotBlank()) {
                            messagesList.add(
                                SeniorChatMessage(
                                    messageId = msgId,
                                    senderId = senderId,
                                    senderName = senderName,
                                    message = message,
                                    timestamp = timestamp,
                                    sentBySenior = sentBySeniorVal,
                                    avatarUrl = avatarUrl
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error parsing senior chat message: ${e.message}")
                    }
                }
                messagesList.sortBy { it.timestamp }
                trySend(messagesList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Senior chat observe cancelled: ${error.message}")
            }
        }

        messagesRef.addValueEventListener(listener)
        awaitClose { messagesRef.removeEventListener(listener) }
    }

    suspend fun sendSeniorChatMessage(
        chatId: String,
        senderId: String,
        senderName: String,
        messageText: String,
        isSenior: Boolean = false,
        avatarUrl: String? = null
    ): Boolean = suspendCancellableCoroutine { continuation ->
        try {
            val cleanChatId = chatId.replace(".", "_").replace("#", "_").replace("$", "_").replace("[", "_").replace("]", "_")
            val messagesRef = database.getReference("seniorChats/$cleanChatId/messages")
            val newMessageRef = messagesRef.push()

            // CRITICAL: Must satisfy newData.hasChildren(['senderId', 'message', 'timestamp'])
            // and newData.child('senderId').val() === auth.uid
            val effectiveSenderId = auth.currentUser?.uid ?: senderId

            val messageMap = mapOf<String, Any?>(
                "senderId" to effectiveSenderId,
                "message" to messageText,
                "timestamp" to System.currentTimeMillis(),
                "senderName" to senderName,
                "sentBySenior" to isSenior,
                "avatarUrl" to avatarUrl
            )

            newMessageRef.setValue(messageMap).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Senior chat message sent successfully to Realtime DB: ${newMessageRef.key}")
                    if (continuation.isActive) continuation.resume(true)
                } else {
                    Log.e(TAG, "Failed to send senior chat message: ${task.exception?.message}")
                    if (continuation.isActive) continuation.resume(false)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending senior chat message: ${e.message}")
            if (continuation.isActive) continuation.resume(false)
        }
    }

    // ==========================================
    // 3. NOTIFICATIONS (Node: /notifications/$uid)
    // Rule: .read: auth != null && auth.uid === $uid, .write: false
    // ==========================================
    fun observeStudentNotifications(uid: String): Flow<List<StudentNotification>> = callbackFlow {
        if (uid.isBlank() || auth.currentUser == null) {
            Log.d(TAG, "Unauthenticated user or blank uid, skipping student notifications observe")
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val notifRef = database.getReference("notifications/$uid")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<StudentNotification>()
                for (child in snapshot.children) {
                    try {
                        val id = child.child("id").getValue(String::class.java) ?: child.key ?: ""
                        val title = child.child("title").getValue(String::class.java) ?: "Notification"
                        val message = child.child("message").getValue(String::class.java) ?: ""
                        val timestamp = child.child("timestamp").getValue(String::class.java) ?: "Recently"
                        val type = child.child("type").getValue(String::class.java) ?: "GENERAL"
                        val isRead = child.child("isRead").getValue(Boolean::class.java) ?: false
                        val createdAtMillis = child.child("createdAtMillis").getValue(Long::class.java) ?: System.currentTimeMillis()

                        list.add(
                            StudentNotification(
                                id = id,
                                title = title,
                                message = message,
                                timestamp = timestamp,
                                type = type,
                                isRead = isRead,
                                createdAtMillis = createdAtMillis
                            )
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "Error parsing notification: ${e.message}")
                    }
                }
                list.sortByDescending { it.createdAtMillis }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Notifications observe cancelled: ${error.message}")
            }
        }

        notifRef.addValueEventListener(listener)
        awaitClose { notifRef.removeEventListener(listener) }
    }

    // ==========================================
    // 4. ONLINE STATUS / PRESENCE (Node: /presence/$uid)
    // Rule:
    // .read: auth != null
    // .write: auth != null && auth.uid === $uid
    // .validate: newData.hasChildren(['online', 'lastSeen'])
    // ==========================================
    fun setupPresenceTracking(userName: String = "Student", role: String = "Student") {
        val currentUser = auth.currentUser ?: return
        val uid = currentUser.uid

        val connectedRef = database.getReference(".info/connected")
        val userPresenceRef = database.getReference("presence/$uid")

        val onlineMap = mapOf<String, Any>(
            "online" to true,
            "lastSeen" to ServerValue.TIMESTAMP,
            "userName" to userName.ifBlank { "Student" },
            "role" to role
        )
        // Immediately write to ensure presence node appears in Realtime DB console
        userPresenceRef.setValue(onlineMap)

        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val connected = snapshot.asSafeBoolean() ?: false
                    if (connected) {
                        val offlineMap = mapOf<String, Any>(
                            "online" to false,
                            "lastSeen" to ServerValue.TIMESTAMP,
                            "userName" to userName.ifBlank { "Student" },
                            "role" to role
                        )
                        userPresenceRef.onDisconnect().setValue(offlineMap)
                        userPresenceRef.setValue(onlineMap)
                        Log.d(TAG, "User presence connected set to online for UID: $uid")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error handling connected state: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Connected info listener cancelled: ${error.message}")
            }
        })
    }

    fun observeUserPresence(uid: String): Flow<UserPresence> = callbackFlow {
        if (uid.isBlank() || auth.currentUser == null) {
            Log.d(TAG, "Unauthenticated user or blank uid, skipping user presence observe")
            trySend(UserPresence())
            close()
            return@callbackFlow
        }

        val presenceRef = try {
            database.getReference("presence/$uid")
        } catch (e: Exception) {
            trySend(UserPresence())
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val online = snapshot.child("online").asSafeBoolean() ?: false
                    val lastSeen = snapshot.child("lastSeen").asSafeLong(System.currentTimeMillis())
                    val name = snapshot.child("userName").asSafeString("Student")
                    val role = snapshot.child("role").asSafeString("Student")

                    trySend(UserPresence(uid = uid, userName = name, online = online, lastSeen = lastSeen, role = role))
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing user presence: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "User presence observe cancelled: ${error.message}")
            }
        }

        try {
            presenceRef.addValueEventListener(listener)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to attach presence listener: ${e.message}")
        }
        awaitClose { try { presenceRef.removeEventListener(listener) } catch (_: Exception) {} }
    }

    fun observeAllOnlineUsers(): Flow<List<UserPresence>> = callbackFlow {
        if (auth.currentUser == null) {
            Log.d(TAG, "Unauthenticated user, skipping all online users observe")
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val presenceRef = try {
            database.getReference("presence")
        } catch (e: Exception) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val activeList = mutableListOf<UserPresence>()
                    for (child in snapshot.children) {
                        val userUid = child.key ?: ""
                        val online = child.child("online").asSafeBoolean() ?: false
                        val lastSeen = child.child("lastSeen").asSafeLong(System.currentTimeMillis())
                        val name = child.child("userName").asSafeString("Student")
                        val role = child.child("role").asSafeString("Student")

                        if (online) {
                            activeList.add(UserPresence(uid = userUid, userName = name, online = true, lastSeen = lastSeen, role = role))
                        }
                    }
                    trySend(activeList)
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing online users: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "All presence observe cancelled: ${error.message}")
            }
        }

        try {
            presenceRef.addValueEventListener(listener)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to attach all presence listener: ${e.message}")
        }
        awaitClose { try { presenceRef.removeEventListener(listener) } catch (_: Exception) {} }
    }

    // ==========================================
    // 5. LIVE XP & RANK (Node: /liveXP/$uid)
    // Rule: .read: auth != null, .write: false
    // ==========================================
    fun observeLiveXP(uid: String): Flow<LiveXpData> = callbackFlow {
        if (uid.isBlank() || auth.currentUser == null) {
            Log.d(TAG, "Unauthenticated user or blank uid, skipping live XP observe")
            trySend(LiveXpData())
            close()
            return@callbackFlow
        }

        val xpRef = try {
            database.getReference("liveXP/$uid")
        } catch (e: Exception) {
            trySend(LiveXpData())
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val name = snapshot.child("userName").asSafeString("Student")
                    val xp = snapshot.child("xp").asSafeLong(0L).toInt()
                    val rank = snapshot.child("rank").asSafeLong(1L).toInt()
                    val targetRank = snapshot.child("targetRank").asSafeString("Under 1000")
                    val level = snapshot.child("level").asSafeLong(1L).toInt()
                    val updatedAt = snapshot.child("updatedAt").asSafeLong(System.currentTimeMillis())

                    trySend(
                        LiveXpData(
                            uid = uid,
                            userName = name,
                            xp = xp,
                            rank = rank,
                            targetRank = targetRank,
                            level = level,
                            updatedAt = updatedAt
                        )
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing live XP: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Live XP observe cancelled: ${error.message}")
            }
        }

        try {
            xpRef.addValueEventListener(listener)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to attach live XP listener: ${e.message}")
        }
        awaitClose { try { xpRef.removeEventListener(listener) } catch (_: Exception) {} }
    }

    fun observeAllLiveXP(): Flow<Map<String, LiveXpData>> = callbackFlow {
        if (auth.currentUser == null) {
            Log.d(TAG, "Unauthenticated user, skipping all live XP observe")
            trySend(emptyMap())
            close()
            return@callbackFlow
        }

        val xpRef = try {
            database.getReference("liveXP")
        } catch (e: Exception) {
            trySend(emptyMap())
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val map = mutableMapOf<String, LiveXpData>()
                    for (child in snapshot.children) {
                        val userUid = child.key ?: ""
                        val name = child.child("userName").asSafeString("Student")
                        val xp = child.child("xp").asSafeLong(0L).toInt()
                        val rank = child.child("rank").asSafeLong(1L).toInt()
                        val targetRank = child.child("targetRank").asSafeString("Under 1000")
                        val level = child.child("level").asSafeLong(1L).toInt()
                        val updatedAt = child.child("updatedAt").asSafeLong(System.currentTimeMillis())

                        map[userUid] = LiveXpData(
                            uid = userUid,
                            userName = name,
                            xp = xp,
                            rank = rank,
                            targetRank = targetRank,
                            level = level,
                            updatedAt = updatedAt
                        )
                    }
                    trySend(map)
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing all live XP: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "All Live XP observe cancelled: ${error.message}")
            }
        }

        try {
            xpRef.addValueEventListener(listener)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to attach all live XP listener: ${e.message}")
        }
        awaitClose { try { xpRef.removeEventListener(listener) } catch (_: Exception) {} }
    }

    fun updateUserLiveXP(uid: String, userName: String, xp: Int, rank: Int, targetRank: String) {
        try {
            if (uid.isBlank()) return
            val xpRef = database.getReference("liveXP").child(uid)
            val data = mapOf(
                "userName" to userName,
                "xp" to xp,
                "rank" to rank,
                "targetRank" to targetRank,
                "level" to (xp / 100).coerceAtLeast(1),
                "updatedAt" to System.currentTimeMillis()
            )
            xpRef.updateChildren(data)

            val lbRef = database.getReference("leaderboard").child(uid)
            lbRef.updateChildren(mapOf(
                "name" to userName,
                "points" to xp,
                "xp" to xp,
                "rank" to rank,
                "targetRank" to targetRank
            ))
        } catch (e: Exception) {
            Log.w(TAG, "Error updating LiveXP in RTDB: ${e.message}")
        }
    }

    fun generateTestData(onComplete: (Boolean) -> Unit) {
        try {
            val batchUpdates = mutableMapOf<String, Any>()

            // 1. Leaderboard Mock Data
            batchUpdates["leaderboard/user_1"] = mapOf(
                "name" to "Ananya Rao",
                "targetRank" to "Under 100",
                "points" to 1450,
                "xp" to 1450,
                "accuracy" to 95.2,
                "testsCount" to 18,
                "rank" to 1,
                "avatarInitials" to "AR"
            )
            batchUpdates["leaderboard/user_2"] = mapOf(
                "name" to "Rohan Kulkarni",
                "targetRank" to "Under 250",
                "points" to 1320,
                "xp" to 1320,
                "accuracy" to 91.0,
                "testsCount" to 15,
                "rank" to 2,
                "avatarInitials" to "RK"
            )
            batchUpdates["leaderboard/user_3"] = mapOf(
                "name" to "Deepika Bhat",
                "targetRank" to "Under 350",
                "points" to 1180,
                "xp" to 1180,
                "accuracy" to 88.5,
                "testsCount" to 14,
                "rank" to 3,
                "avatarInitials" to "DB"
            )
            batchUpdates["leaderboard/user_4"] = mapOf(
                "name" to "Preetham Shetty",
                "targetRank" to "Under 500",
                "points" to 1020,
                "xp" to 1020,
                "accuracy" to 84.0,
                "testsCount" to 12,
                "rank" to 4,
                "avatarInitials" to "PS"
            )
            batchUpdates["leaderboard/user_5"] = mapOf(
                "name" to "Suhas Patil",
                "targetRank" to "Under 1200",
                "points" to 420,
                "xp" to 420,
                "accuracy" to 78.0,
                "testsCount" to 6,
                "rank" to 5,
                "avatarInitials" to "SP"
            )

            // 2. Presence Mock Data
            batchUpdates["presence/user_1"] = mapOf(
                "online" to true,
                "lastSeen" to System.currentTimeMillis(),
                "userName" to "Ananya Rao (Senior)",
                "role" to "Senior"
            )
            batchUpdates["presence/user_2"] = mapOf(
                "online" to true,
                "lastSeen" to System.currentTimeMillis(),
                "userName" to "Rohan Kulkarni (Senior)",
                "role" to "Senior"
            )
            batchUpdates["presence/user_3"] = mapOf(
                "online" to false,
                "lastSeen" to System.currentTimeMillis() - 3600000,
                "userName" to "Deepika Bhat",
                "role" to "Student"
            )

            // 3. Live XP Mock Data
            batchUpdates["liveXP/user_1"] = mapOf(
                "userName" to "Ananya Rao",
                "xp" to 1450,
                "rank" to 1,
                "targetRank" to "Under 100",
                "level" to 15,
                "updatedAt" to System.currentTimeMillis()
            )
            batchUpdates["liveXP/user_2"] = mapOf(
                "userName" to "Rohan Kulkarni",
                "xp" to 1320,
                "rank" to 2,
                "targetRank" to "Under 250",
                "level" to 12,
                "updatedAt" to System.currentTimeMillis()
            )
            batchUpdates["liveXP/user_3"] = mapOf(
                "userName" to "Deepika Bhat",
                "xp" to 1180,
                "rank" to 3,
                "targetRank" to "Under 350",
                "level" to 10,
                "updatedAt" to System.currentTimeMillis()
            )

            // 4. Sample Chat Messages
            batchUpdates["seniorChats/senior_chat_global/messages/msg_1"] = mapOf(
                "senderId" to "user_1",
                "senderName" to "Ananya Rao (Senior)",
                "message" to "Hey everyone! Congratulations on starting your KCET preparation. Feel free to ask any physics and math questions here!",
                "timestamp" to System.currentTimeMillis() - 120000,
                "sentBySenior" to true
            )
            batchUpdates["seniorChats/senior_chat_global/messages/msg_2"] = mapOf(
                "senderId" to "user_2",
                "senderName" to "Rohan Kulkarni (Senior)",
                "message" to "Hi aspirants! I scored Rank 182 last year. Consistent mock tests are the absolute key to success.",
                "timestamp" to System.currentTimeMillis() - 60000,
                "sentBySenior" to true
            )

            database.reference.updateChildren(batchUpdates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Test data generated successfully.")
                    onComplete(true)
                } else {
                    Log.e(TAG, "Failed to generate test data: ${task.exception?.message}")
                    onComplete(false)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating test data: ${e.message}")
            onComplete(false)
        }
    }

    // ==========================================
    // 5. 1-to-1 CHATS (Node: /oneToOneChats/$bookingId/messages)
    // ==========================================
    fun observeOneToOneChat(bookingId: String): Flow<List<OneToOneMessage>> = callbackFlow {
        val chatRef = database.getReference("oneToOneChats").child(bookingId).child("messages")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<OneToOneMessage>()
                for (child in snapshot.children) {
                    try {
                        val msg = child.getValue(OneToOneMessage::class.java)
                        if (msg != null) {
                            messages.add(msg.copy(messageId = child.key ?: ""))
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error parsing 1:1 message: ${e.message}")
                    }
                }
                trySend(messages.sortedBy { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        chatRef.addValueEventListener(listener)
        awaitClose { chatRef.removeEventListener(listener) }
    }

    suspend fun sendOneToOneMessage(
        bookingId: String,
        senderId: String,
        senderName: String,
        message: String,
        isSenior: Boolean
    ): Boolean = suspendCancellableCoroutine { continuation ->
        val chatRef = database.getReference("oneToOneChats").child(bookingId).child("messages")
        val newMsgRef = chatRef.push()
        val msg = OneToOneMessage(
            messageId = newMsgRef.key ?: "",
            senderId = senderId,
            senderName = senderName,
            message = message,
            timestamp = System.currentTimeMillis(),
            sentBySenior = isSenior
        )
        newMsgRef.setValue(msg).addOnCompleteListener { task ->
            if (continuation.isActive) {
                continuation.resume(task.isSuccessful)
            }
        }
    }

    fun markOneToOneMessagesAsRead(bookingId: String, currentUserId: String) {
        if (bookingId.isBlank()) return
        val chatRef = database.getReference("oneToOneChats").child(bookingId).child("messages")
        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updates = mutableMapOf<String, Any>()
                for (child in snapshot.children) {
                    val senderId = child.child("senderId").getValue(String::class.java) ?: ""
                    val isRead = child.child("isRead").getValue(Boolean::class.java) ?: false
                    if (senderId.isNotBlank() && senderId != currentUserId && !isRead) {
                        updates["${child.key}/isRead"] = true
                    }
                }
                if (updates.isNotEmpty()) {
                    chatRef.updateChildren(updates)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun deleteOneToOneMessage(bookingId: String, messageId: String) {
        val msgRef = database.getReference("oneToOneChats").child(bookingId).child("messages").child(messageId)
        msgRef.removeValue()
    }

    fun clearOneToOneChat(bookingId: String) {
        val chatRef = database.getReference("oneToOneChats").child(bookingId)
        chatRef.removeValue()
    }

    fun setChatActiveState(bookingId: String, userId: String, active: Boolean) {
        val activeRef = database.getReference("oneToOneChats").child(bookingId).child("activeUsers").child(userId)
        if (active) {
            activeRef.setValue(true)
            activeRef.onDisconnect().removeValue()
        } else {
            activeRef.removeValue()
        }
    }

    fun observeActiveChatUsers(bookingId: String): Flow<List<String>> = callbackFlow {
        if (auth.currentUser == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val activeRef = database.getReference("oneToOneChats").child(bookingId).child("activeUsers")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<String>()
                for (child in snapshot.children) {
                    if (child.getValue(Boolean::class.java) == true) {
                        list.add(child.key ?: "")
                    }
                }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        activeRef.addValueEventListener(listener)
        awaitClose { activeRef.removeEventListener(listener) }
    }

    // ==========================================
    // 6. SENIOR MENTOR GLOBAL PROFILE (Node: /seniorMentorProfile)
    // ==========================================
    fun observeSeniorMentorProfile(): Flow<SeniorMentorProfile?> = callbackFlow {
        var dbListener: ValueEventListener? = null
        val profileRef = database.getReference("seniorMentorProfile")

        fun attachDatabaseListener() {
            if (dbListener != null) return
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val profile = snapshot.getValue(SeniorMentorProfile::class.java)
                    trySend(profile)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Observe mentor profile info (${error.code}): ${error.message}")
                    dbListener?.let { profileRef.removeEventListener(it) }
                    dbListener = null
                }
            }
            dbListener = listener
            profileRef.addValueEventListener(listener)
        }

        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                attachDatabaseListener()
            } else {
                firebaseAuth.signInAnonymously().addOnSuccessListener {
                    attachDatabaseListener()
                }
            }
        }

        auth.addAuthStateListener(authStateListener)

        if (auth.currentUser != null) {
            attachDatabaseListener()
        } else {
            auth.signInAnonymously().addOnSuccessListener {
                attachDatabaseListener()
            }.addOnFailureListener {
                attachDatabaseListener()
            }
        }

        awaitClose {
            auth.removeAuthStateListener(authStateListener)
            dbListener?.let { profileRef.removeEventListener(it) }
        }
    }

    suspend fun updateSeniorMentorProfile(profile: SeniorMentorProfile): Boolean = suspendCancellableCoroutine { continuation ->
        val mentorId = profile.name.trim().lowercase().replace(Regex("[^a-z0-9_]"), "_").ifBlank { "mentor_" + System.currentTimeMillis() }
        val mentorsRef = database.getReference("registeredMentors").child(mentorId)
        val profileRef = database.getReference("seniorMentorProfile")

        // Also save to Firestore
        CoroutineScope(Dispatchers.IO).launch {
            try { FirebaseService.saveSeniorMentorToFirestore(profile) } catch (_: Exception) {}
        }

        profileRef.setValue(profile).addOnCompleteListener { task1 ->
            mentorsRef.setValue(profile).addOnCompleteListener { task2 ->
                if (continuation.isActive) {
                    continuation.resume(task2.isSuccessful)
                }
            }
        }
    }

    private val deletedMentorKeys = java.util.Collections.synchronizedSet(mutableSetOf<String>())

    fun markMentorDeletedLocally(name: String) {
        deletedMentorKeys.add(name.trim().lowercase())
    }

    fun isMentorDeleted(name: String): Boolean {
        return deletedMentorKeys.contains(name.trim().lowercase())
    }

    private fun parseSeniorMentorFromSnapshot(snapshot: DataSnapshot): SeniorMentorProfile? {
        if (!snapshot.exists()) return null
        try {
            val pojo = snapshot.getValue(SeniorMentorProfile::class.java)
            if (pojo != null && pojo.name.isNotBlank()) {
                return pojo
            }
        } catch (_: Exception) {}

        try {
            val map = snapshot.value as? Map<*, *> ?: return null
            val name = (map["name"] ?: map["mentorName"] ?: map["username"])?.toString()?.trim() ?: ""
            if (name.isBlank()) return null
            return SeniorMentorProfile(
                name = name,
                bio = (map["bio"] ?: map["description"] ?: map["about"])?.toString() ?: "",
                specialty = (map["specialty"] ?: map["specialization"] ?: map["subject"])?.toString() ?: "",
                qualification = (map["qualification"] ?: map["degree"])?.toString() ?: "",
                experience = (map["experience"] ?: map["exp"])?.toString() ?: "",
                telegramOrInstagram = (map["telegramOrInstagram"] ?: map["socials"] ?: map["instagram"] ?: map["telegram"])?.toString() ?: "",
                email = (map["email"] ?: map["userEmail"])?.toString() ?: "",
                phone = (map["phone"] ?: map["contact"] ?: map["mobile"])?.toString() ?: "",
                status = map["status"]?.toString() ?: "Active",
                password = map["password"]?.toString() ?: ""
            )
        } catch (_: Exception) {
            return null
        }
    }

    fun observeAllSeniorMentors(): Flow<List<SeniorMentorProfile>> = callbackFlow {
        var rtdbMentors = emptyList<SeniorMentorProfile>()
        var singleProfile: SeniorMentorProfile? = null
        var fsMentors = emptyList<SeniorMentorProfile>()

        fun emitMerged() {
            val combinedMap = LinkedHashMap<String, SeniorMentorProfile>()

            // 1. Start with default verified senior mentors
            FirebaseService.defaultVerifiedSeniorMentors.forEach { prof ->
                if (!isMentorDeleted(prof.name)) {
                    val key = prof.email.lowercase().trim().ifBlank { prof.name.lowercase().trim() }
                    combinedMap[key] = prof
                }
            }

            // 2. Overlay singleProfile if present
            singleProfile?.let { prof ->
                if (prof.name.isNotBlank() && !isMentorDeleted(prof.name)) {
                    val key = prof.email.lowercase().trim().ifBlank { prof.name.lowercase().trim() }
                    combinedMap[key] = prof
                }
            }

            // 3. Overlay RTDB registeredMentors
            rtdbMentors.forEach { prof ->
                if (prof.name.isNotBlank() && !isMentorDeleted(prof.name)) {
                    val key = prof.email.lowercase().trim().ifBlank { prof.name.lowercase().trim() }
                    combinedMap[key] = prof
                }
            }

            // 4. Overlay Firestore mentors
            fsMentors.forEach { prof ->
                if (prof.name.isNotBlank() && !isMentorDeleted(prof.name)) {
                    val key = prof.email.lowercase().trim().ifBlank { prof.name.lowercase().trim() }
                    combinedMap[key] = prof
                }
            }

            val list = combinedMap.values.toList()
            trySend(list)
        }

        val mentorsRef = database.getReference("registeredMentors")
        val mentorsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val list = mutableListOf<SeniorMentorProfile>()
                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            parseSeniorMentorFromSnapshot(child)?.let { list.add(it) }
                        }
                        if (list.isEmpty()) {
                            parseSeniorMentorFromSnapshot(snapshot)?.let { list.add(it) }
                        }
                    }
                    rtdbMentors = list
                    emitMerged()
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing mentors snapshot: ${e.message}")
                    emitMerged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Observe mentors notice (${error.code}): ${error.message}")
                emitMerged()
            }
        }

        val profileRef = database.getReference("seniorMentorProfile")
        val profileListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    singleProfile = parseSeniorMentorFromSnapshot(snapshot)
                    emitMerged()
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing senior mentor profile: ${e.message}")
                    emitMerged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Observe profile notice (${error.code}): ${error.message}")
                emitMerged()
            }
        }

        fun attachDatabaseListeners() {
            try {
                mentorsRef.addValueEventListener(mentorsListener)
                profileRef.addValueEventListener(profileListener)
            } catch (e: Exception) {
                Log.w(TAG, "Failed attaching mentor listeners: ${e.message}")
            }
        }

        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            try {
                if (firebaseAuth.currentUser != null) {
                    attachDatabaseListeners()
                } else {
                    firebaseAuth.signInAnonymously().addOnSuccessListener {
                        attachDatabaseListeners()
                    }.addOnFailureListener {
                        emitMerged()
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Auth state listener error in mentors flow: ${e.message}")
                emitMerged()
            }
        }

        try {
            auth.addAuthStateListener(authStateListener)
            if (auth.currentUser != null) {
                attachDatabaseListeners()
            } else {
                auth.signInAnonymously().addOnSuccessListener {
                    attachDatabaseListeners()
                }.addOnFailureListener {
                    emitMerged()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Initial mentor listener setup error: ${e.message}")
            emitMerged()
        }

        val fsJob = launch(Dispatchers.IO) {
            try {
                FirebaseService.observeAllSeniorMentorsFromFirestore().collect { list ->
                    fsMentors = list
                    emitMerged()
                }
            } catch (_: Exception) {}
        }

        awaitClose {
            try { auth.removeAuthStateListener(authStateListener) } catch (_: Exception) {}
            try { mentorsRef.removeEventListener(mentorsListener) } catch (_: Exception) {}
            try { profileRef.removeEventListener(profileListener) } catch (_: Exception) {}
            fsJob.cancel()
        }
    }

    suspend fun registerSeniorMentor(profile: SeniorMentorProfile, passwordPlain: String = ""): String? = suspendCancellableCoroutine { continuation ->
        val email = profile.email.trim()

        // Also save to Firestore
        CoroutineScope(Dispatchers.IO).launch {
            try { FirebaseService.saveSeniorMentorToFirestore(profile) } catch (_: Exception) {}
        }

        if (email.isNotBlank() && passwordPlain.isNotBlank()) {
            auth.createUserWithEmailAndPassword(email, passwordPlain.trim())
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        val mentorId = profile.name.trim().lowercase().replace(Regex("[^a-z0-9_]"), "_").ifBlank { "mentor_" + System.currentTimeMillis() }
                        val mentorsRef = database.getReference("registeredMentors").child(mentorId)
                        val mentorProfileWithPassword = profile.copy(password = passwordPlain.ifBlank { profile.password })
                        mentorsRef.setValue(mentorProfileWithPassword).addOnCompleteListener { dbTask ->
                            if (continuation.isActive) {
                                continuation.resume(if (dbTask.isSuccessful) null else (dbTask.exception?.message ?: "Saved to Firestore"))
                            }
                        }
                    } else {
                        Log.w(TAG, "Firebase Auth register info: ${authTask.exception?.message}")
                        val mentorId = profile.name.trim().lowercase().replace(Regex("[^a-z0-9_]"), "_").ifBlank { "mentor_" + System.currentTimeMillis() }
                        val mentorsRef = database.getReference("registeredMentors").child(mentorId)
                        val mentorProfileWithPassword = profile.copy(password = passwordPlain.ifBlank { profile.password })
                        mentorsRef.setValue(mentorProfileWithPassword).addOnCompleteListener { dbTask ->
                            if (continuation.isActive) {
                                continuation.resume(null) // Resume with null as fallback since DB is updated
                            }
                        }
                    }
                }
        } else {
            val mentorId = profile.name.trim().lowercase().replace(Regex("[^a-z0-9_]"), "_").ifBlank { "mentor_" + System.currentTimeMillis() }
            val mentorsRef = database.getReference("registeredMentors").child(mentorId)
            mentorsRef.setValue(profile).addOnCompleteListener { task ->
                if (continuation.isActive) {
                    continuation.resume(if (task.isSuccessful) null else null)
                }
            }
        }
    }

    suspend fun deleteSeniorMentor(mentorName: String): Boolean = suspendCancellableCoroutine { continuation ->
        val mentorId = mentorName.trim().lowercase().replace(Regex("[^a-z0-9_]"), "_").ifBlank { "mentor_" }
        markMentorDeletedLocally(mentorName)
        val mentorsRef = database.getReference("registeredMentors").child(mentorId)

        CoroutineScope(Dispatchers.IO).launch {
            val fsResult = try { FirebaseService.deleteSeniorMentorFromFirestore(mentorName) } catch (e: Exception) { false }
            
            mentorsRef.removeValue().addOnCompleteListener { task ->
                if (continuation.isActive) {
                    continuation.resume(task.isSuccessful || fsResult)
                }
            }
        }
    }

    suspend fun updateSeniorMentorStatus(mentorName: String, newStatus: String): Boolean = suspendCancellableCoroutine { continuation ->
        val mentorId = mentorName.trim().lowercase().replace(Regex("[^a-z0-9_]"), "_").ifBlank { "mentor_" }
        val statusRef = database.getReference("registeredMentors").child(mentorId).child("status")

        CoroutineScope(Dispatchers.IO).launch {
            try { FirebaseService.updateSeniorMentorStatusInFirestore(mentorName, newStatus) } catch (_: Exception) {}
        }

        statusRef.setValue(newStatus).addOnCompleteListener { task ->
            if (continuation.isActive) {
                continuation.resume(true)
            }
        }
    }

    fun updateStudentProfile(profile: UserProfile) {
        try {
            val uid = profile.uid.ifBlank { auth.currentUser?.uid ?: "" }
            if (uid.isNotBlank()) {
                val lbRef = database.getReference("leaderboard").child(uid)
                lbRef.child("name").setValue(profile.name)
                lbRef.child("kcetTargetRank").setValue(profile.kcetTargetRank)

                val presenceRef = database.getReference("presence").child(uid)
                presenceRef.child("userName").setValue(profile.name)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error updating student profile in RTDB: ${e.message}")
        }
    }

    fun deleteStudentData(uid: String, email: String = "", name: String = "") {
        try {
            if (uid.isNotBlank()) {
                database.getReference("leaderboard").child(uid).removeValue()
                database.getReference("presence").child(uid).removeValue()
                database.getReference("liveXP").child(uid).removeValue()
            }
            if (email.isNotBlank()) {
                val safeEmail = email.trim().lowercase().replace(".", "_").replace("@", "_")
                database.getReference("leaderboard").child(safeEmail).removeValue()
                database.getReference("liveXP").child(safeEmail).removeValue()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error removing student data in RTDB: ${e.message}")
        }
    }

    fun sendAdminAlert(notification: StudentNotification) {
        try {
            val adminNotifRef = database.getReference("adminNotifications").child(notification.id)
            val data = mapOf(
                "id" to notification.id,
                "title" to notification.title,
                "message" to notification.message,
                "timestamp" to notification.timestamp,
                "type" to notification.type,
                "targetUserId" to (notification.targetUserId ?: "ADMIN"),
                "createdAtMillis" to notification.createdAtMillis
            )
            adminNotifRef.setValue(data)
        } catch (e: Exception) {
            Log.w(TAG, "Error sending admin alert in RTDB: ${e.message}")
        }
    }

    fun observeAdminPinFromRTDB(): Flow<String?> = callbackFlow {
        val ref = try {
            database.getReference("admin_config").child("pin")
        } catch (e: Exception) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val pinVal = snapshot.asSafeStringOrNull()?.trim()
                    trySend(pinVal)
                } catch (e: Exception) {
                    Log.w(TAG, "Error reading admin PIN from RTDB: ${e.message}")
                    trySend(null)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                trySend(null)
            }
        }
        try {
            ref.addValueEventListener(listener)
        } catch (e: Exception) {
            Log.w(TAG, "Failed attaching admin PIN listener: ${e.message}")
            trySend(null)
        }
        awaitClose { try { ref.removeEventListener(listener) } catch (_: Exception) {} }
    }

    fun deleteNotificationPermanently(id: String) {
        try {
            database.getReference("adminNotifications").child(id).removeValue()
            database.getReference("globallyDeletedNotifications").child(id).setValue(true)
        } catch (e: Exception) {
            Log.w(TAG, "Error deleting notification from RTDB: ${e.message}")
        }
    }

    fun observeGloballyDeletedNotificationIds(): Flow<Set<String>> = callbackFlow {
        val ref = try {
            database.getReference("globallyDeletedNotifications")
        } catch (e: Exception) {
            trySend(emptySet())
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val set = mutableSetOf<String>()
                    for (child in snapshot.children) {
                        val key = child.key
                        if (!key.isNullOrBlank()) {
                            set.add(key)
                        }
                    }
                    trySend(set)
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing globally deleted notification IDs: ${e.message}")
                    trySend(emptySet())
                }
            }
            override fun onCancelled(error: DatabaseError) {
                trySend(emptySet())
            }
        }
        try {
            ref.addValueEventListener(listener)
        } catch (e: Exception) {
            Log.w(TAG, "Failed attaching deleted notification IDs listener: ${e.message}")
            trySend(emptySet())
        }
        awaitClose { try { ref.removeEventListener(listener) } catch (_: Exception) {} }
    }

    fun saveAdminPricingToRTDB(liveChatPrice: Int, subscriptionPrice: Int) {
        try {
            val map = mapOf(
                "liveChatPrice" to liveChatPrice,
                "subscriptionPrice" to subscriptionPrice,
                "updatedAtMillis" to System.currentTimeMillis()
            )
            database.getReference("admin_config").child("pricing").setValue(map)
        } catch (e: Exception) {
            Log.w(TAG, "Error saving pricing to RTDB: ${e.message}")
        }
    }

    fun observeAdminPricingFromRTDB(): Flow<Map<String, Any>?> = callbackFlow {
        val ref = try {
            database.getReference("admin_config").child("pricing")
        } catch (e: Exception) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    val value = snapshot.value as? Map<String, Any>
                    trySend(value)
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing admin pricing from RTDB: ${e.message}")
                    trySend(null)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                trySend(null)
            }
        }
        try {
            ref.addValueEventListener(listener)
        } catch (e: Exception) {
            Log.w(TAG, "Failed attaching admin pricing listener: ${e.message}")
            trySend(null)
        }
        awaitClose { try { ref.removeEventListener(listener) } catch (_: Exception) {} }
    }

    private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitSafe(): T? {
        return withTimeoutOrNull(4500L) {
            suspendCancellableCoroutine { cont ->
                addOnSuccessListener { result ->
                    if (cont.isActive) cont.resume(result)
                }
                addOnFailureListener {
                    if (cont.isActive) cont.resume(null)
                }
            }
        }
    }

    suspend fun seedSystemSecurityKeyIfMissing(
        initialKey: String = SystemControlConfig.DEFAULT_SYSTEM_SECURITY_KEY
    ) {
        try {
            val adminConfigRef = database.getReference("admin_config")
            val snapshot = adminConfigRef.get().awaitSafe()
            val existingSecKey = snapshot?.child("securityKey")?.value?.toString()?.trim()
            if (existingSecKey.isNullOrBlank()) {
                val existingPin = snapshot?.child("pin")?.value?.toString()?.trim()
                val keyToSeed = if (!existingPin.isNullOrBlank()) existingPin else initialKey
                val updates = mutableMapOf<String, Any>(
                    "securityKey" to keyToSeed,
                    "securityKeyUpdatedAtMillis" to System.currentTimeMillis()
                )
                if (existingPin.isNullOrBlank()) {
                    updates["pin"] = keyToSeed
                }
                adminConfigRef.updateChildren(updates).awaitSafe()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error seeding RTDB system security key: ${e.message}")
        }
    }

    data class RtdbSecurityKeySnapshot(
        val securityKey: String? = null,
        val pin: String? = null,
        val updatedAtMillis: Long = 0L
    )

    fun observeSystemSecurityKeyFromRTDB(): Flow<RtdbSecurityKeySnapshot?> = callbackFlow {
        val ref = try {
            database.getReference("admin_config")
        } catch (e: Exception) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val secKey = snapshot.child("securityKey").asSafeStringOrNull()?.trim()
                    val pin = snapshot.child("pin").asSafeStringOrNull()?.trim()
                    val updatedAt = snapshot.child("securityKeyUpdatedAtMillis").asSafeLong(0L)
                    trySend(RtdbSecurityKeySnapshot(securityKey = secKey, pin = pin, updatedAtMillis = updatedAt))
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing RTDB security key: ${e.message}")
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(null)
            }
        }
        try {
            ref.addValueEventListener(listener)
        } catch (e: Exception) {
            Log.w(TAG, "Failed attaching security key listener: ${e.message}")
            trySend(null)
        }
        awaitClose { try { ref.removeEventListener(listener) } catch (_: Exception) {} }
    }

    suspend fun getSystemSecurityKeyOnceFromRTDB(): RtdbSecurityKeySnapshot? {
        return try {
            val snapshot = database.getReference("admin_config").get().awaitSafe() ?: return null
            if (!snapshot.exists()) return null
            val secKey = snapshot.child("securityKey").asSafeStringOrNull()?.trim()
            val pin = snapshot.child("pin").asSafeStringOrNull()?.trim()
            val updatedAt = snapshot.child("securityKeyUpdatedAtMillis").asSafeLong(0L)
            RtdbSecurityKeySnapshot(securityKey = secKey, pin = pin, updatedAtMillis = updatedAt)
        } catch (e: Exception) {
            Log.w(TAG, "Error reading RTDB security key: ${e.message}")
            null
        }
    }

    suspend fun saveSystemShutdownToRTDB(
        isShutdown: Boolean,
        updatedBy: String,
        updatedAtMillis: Long = System.currentTimeMillis()
    ) {
        try {
            val adminConfigRef = database.getReference("admin_config")
            val updates = mapOf<String, Any>(
                "systemShutdown" to isShutdown,
                "systemStatus" to mapOf(
                    "isShutdown" to isShutdown,
                    "updatedAtMillis" to updatedAtMillis,
                    "updatedBy" to updatedBy
                )
            )
            adminConfigRef.updateChildren(updates).awaitSafe()
        } catch (e: Exception) {
            Log.w(TAG, "Error saving system shutdown to RTDB: ${e.message}")
        }
    }

    fun observeSystemShutdownFromRTDB(): Flow<SystemControlConfig.SystemShutdownStatus?> = callbackFlow {
        val ref = try {
            database.getReference("admin_config")
        } catch (e: Exception) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (!snapshot.exists()) {
                        trySend(null)
                        return
                    }
                    val statusNode = snapshot.child("systemStatus")
                    val boolDirect = snapshot.child("systemShutdown").asSafeBoolean()
                    val statusBool = statusNode.child("isShutdown").asSafeBoolean()
                    val updatedAt = statusNode.child("updatedAtMillis").asSafeLong(0L)
                    val updatedBy = statusNode.child("updatedBy").asSafeString("")
                    val effectiveBool = statusBool ?: boolDirect
                    if (effectiveBool != null) {
                        trySend(
                            SystemControlConfig.SystemShutdownStatus(
                                isShutdown = effectiveBool,
                                updatedAtMillis = updatedAt,
                                updatedBy = updatedBy
                            )
                        )
                    } else {
                        trySend(null)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing RTDB system shutdown status: ${e.message}")
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(null)
            }
        }
        try {
            ref.addValueEventListener(listener)
        } catch (e: Exception) {
            Log.w(TAG, "Failed attaching RTDB system shutdown listener: ${e.message}")
            trySend(null)
        }
        awaitClose { try { ref.removeEventListener(listener) } catch (_: Exception) {} }
    }

    suspend fun saveLeaderboardResetAtToRTDB(resetAtMillis: Long) {
        try {
            val nextResetAt = resetAtMillis + SystemControlConfig.LEADERBOARD_CYCLE_DURATION_MS
            val updates = mapOf<String, Any>(
                "leaderboardResetAt" to resetAtMillis,
                "leaderboardCycleStartAt" to resetAtMillis,
                "leaderboardNextResetAt" to nextResetAt
            )
            database.getReference("admin_config")
                .updateChildren(updates)
                .awaitSafe()
        } catch (e: Exception) {
            Log.w(TAG, "Error saving leaderboardResetAt to RTDB: ${e.message}")
        }
    }

    suspend fun saveLeaderboardCycleToRTDB(cycle: SystemControlConfig.LeaderboardCycleStatus) {
        try {
            val updates = mutableMapOf<String, Any>(
                "leaderboardCycleStartAt" to cycle.effectiveCycleStartAtMillis,
                "leaderboardNextResetAt" to cycle.effectiveNextResetAtMillis,
                "isPaused" to cycle.isPaused,
                "pausedAtMillis" to cycle.pausedAtMillis,
                "pausedRemainingMillis" to cycle.pausedRemainingMillis
            )
            if (cycle.lastResetAtMillis > 0L) {
                updates["leaderboardResetAt"] = cycle.lastResetAtMillis
            }
            database.getReference("admin_config")
                .updateChildren(updates)
                .awaitSafe()
        } catch (e: Exception) {
            Log.w(TAG, "Error saving leaderboard cycle to RTDB: ${e.message}")
        }
    }

    suspend fun getLeaderboardCycleOnceFromRTDB(): SystemControlConfig.LeaderboardCycleStatus? {
        return try {
            val snapshot = database.getReference("admin_config").get().awaitSafe() ?: return null
            if (!snapshot.exists()) return null
            val resetAt = snapshot.child("leaderboardResetAt").asSafeLong(0L)
            val cycleStart = snapshot.child("leaderboardCycleStartAt").asSafeLong(0L)
            val nextReset = snapshot.child("leaderboardNextResetAt").asSafeLong(0L)
            val isPaused = snapshot.child("isPaused").value?.toString()?.toBoolean() ?: false
            val pausedAt = snapshot.child("pausedAtMillis").asSafeLong(0L)
            val pausedRem = snapshot.child("pausedRemainingMillis").asSafeLong(0L)
            if (resetAt <= 0L && cycleStart <= 0L && nextReset <= 0L) return null
            SystemControlConfig.LeaderboardCycleStatus(
                lastResetAtMillis = resetAt,
                cycleStartAtMillis = if (cycleStart > 0L) cycleStart else resetAt,
                nextResetAtMillis = if (nextReset > 0L) nextReset else (if (resetAt > 0L) resetAt + SystemControlConfig.LEADERBOARD_CYCLE_DURATION_MS else 0L),
                isPaused = isPaused,
                pausedAtMillis = pausedAt,
                pausedRemainingMillis = pausedRem
            )
        } catch (e: Exception) {
            Log.w(TAG, "Error reading RTDB leaderboard cycle: ${e.message}")
            null
        }
    }

    fun observeLeaderboardCycleFromRTDB(): Flow<SystemControlConfig.LeaderboardCycleStatus?> = callbackFlow {
        val ref = try {
            database.getReference("admin_config")
        } catch (e: Exception) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (!snapshot.exists()) {
                        trySend(null)
                        return
                    }
                    val resetAt = snapshot.child("leaderboardResetAt").asSafeLong(0L)
                    val cycleStart = snapshot.child("leaderboardCycleStartAt").asSafeLong(0L)
                    val nextReset = snapshot.child("leaderboardNextResetAt").asSafeLong(0L)
                    val isPaused = snapshot.child("isPaused").value?.toString()?.toBoolean() ?: false
                    val pausedAt = snapshot.child("pausedAtMillis").asSafeLong(0L)
                    val pausedRem = snapshot.child("pausedRemainingMillis").asSafeLong(0L)
                    if (resetAt <= 0L && cycleStart <= 0L && nextReset <= 0L) {
                        trySend(null)
                        return
                    }
                    trySend(
                        SystemControlConfig.LeaderboardCycleStatus(
                            lastResetAtMillis = resetAt,
                            cycleStartAtMillis = if (cycleStart > 0L) cycleStart else resetAt,
                            nextResetAtMillis = if (nextReset > 0L) nextReset else (if (resetAt > 0L) resetAt + SystemControlConfig.LEADERBOARD_CYCLE_DURATION_MS else 0L),
                            isPaused = isPaused,
                            pausedAtMillis = pausedAt,
                            pausedRemainingMillis = pausedRem
                        )
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing leaderboard cycle from RTDB: ${e.message}")
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(null)
            }
        }
        try {
            ref.addValueEventListener(listener)
        } catch (e: Exception) {
            Log.w(TAG, "Failed attaching leaderboard cycle listener: ${e.message}")
            trySend(null)
        }
        awaitClose { try { ref.removeEventListener(listener) } catch (_: Exception) {} }
    }

    fun observeLeaderboardResetAtFromRTDB(): Flow<Long> = callbackFlow {
        val ref = try {
            database.getReference("admin_config").child("leaderboardResetAt")
        } catch (e: Exception) {
            trySend(0L)
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val ts = snapshot.asSafeLong(0L)
                    trySend(ts)
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing leaderboardResetAt from RTDB: ${e.message}")
                    trySend(0L)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(0L)
            }
        }
        try {
            ref.addValueEventListener(listener)
        } catch (e: Exception) {
            Log.w(TAG, "Failed attaching leaderboardResetAt listener: ${e.message}")
            trySend(0L)
        }
        awaitClose { try { ref.removeEventListener(listener) } catch (_: Exception) {} }
    }

    suspend fun resetAllLeaderboardXpInRTDB(resetAtMillis: Long = System.currentTimeMillis()): Boolean {
        return try {
            val batchUpdates = mutableMapOf<String, Any>()

            val lbSnapshot = database.getReference("leaderboard").get().awaitSafe()
            if (lbSnapshot != null && lbSnapshot.exists()) {
                for (child in lbSnapshot.children) {
                    val key = child.key ?: continue
                    batchUpdates["leaderboard/$key/points"] = 0
                    batchUpdates["leaderboard/$key/xp"] = 0
                    batchUpdates["leaderboard/$key/rank"] = 0
                }
            }

            val xpSnapshot = database.getReference("liveXP").get().awaitSafe()
            if (xpSnapshot != null && xpSnapshot.exists()) {
                for (child in xpSnapshot.children) {
                    val key = child.key ?: continue
                    batchUpdates["liveXP/$key/xp"] = 0
                    batchUpdates["liveXP/$key/points"] = 0
                    batchUpdates["liveXP/$key/level"] = 1
                    batchUpdates["liveXP/$key/rank"] = 0
                    batchUpdates["liveXP/$key/updatedAt"] = resetAtMillis
                }
            }

            batchUpdates["admin_config/leaderboardResetAt"] = resetAtMillis
            batchUpdates["admin_config/leaderboardCycleStartAt"] = resetAtMillis
            batchUpdates["admin_config/leaderboardNextResetAt"] =
                resetAtMillis + SystemControlConfig.LEADERBOARD_CYCLE_DURATION_MS

            if (batchUpdates.isNotEmpty()) {
                database.reference.updateChildren(batchUpdates).awaitSafe()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting leaderboard XP in RTDB: ${e.message}")
            false
        }
    }

    // -------------------------------------------------------------
    // APP-WIDE MAINTENANCE MODE & SERVER TIME OFFSET (RTDB)
    // -------------------------------------------------------------
    fun observeServerTimeOffsetMs(): Flow<Long> = callbackFlow {
        val offsetRef = try {
            database.getReference(".info/serverTimeOffset")
        } catch (e: Exception) {
            trySend(0L)
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val offset = snapshot.asSafeLong(0L)
                    trySend(offset)
                } catch (e: Exception) {
                    trySend(0L)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(0L)
            }
        }
        try {
            offsetRef.addValueEventListener(listener)
        } catch (e: Exception) {
            Log.w(TAG, "Failed attaching server time offset listener: ${e.message}")
            trySend(0L)
        }
        awaitClose { try { offsetRef.removeEventListener(listener) } catch (_: Exception) {} }
    }

    fun observeMaintenanceConfigFromRTDB(): Flow<MaintenanceConfig?> = callbackFlow {
        val ref = try {
            database.getReference("admin_config").child("maintenance")
        } catch (e: Exception) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (!snapshot.exists()) {
                        trySend(null)
                        return
                    }
                    val enabled = snapshot.child("maintenanceEnabled").asSafeBoolean() ?: false
                    val startTime = snapshot.child("maintenanceStartTime").asSafeLong(0L)
                    val endTime = snapshot.child("maintenanceEndTime").asSafeLong(0L)
                    val updatedAt = snapshot.child("updatedAt").asSafeLong(0L)
                    val updatedBy = snapshot.child("updatedBy").asSafeString("")

                    trySend(
                        MaintenanceConfig(
                            maintenanceEnabled = enabled,
                            maintenanceStartTime = startTime,
                            maintenanceEndTime = endTime,
                            updatedAt = updatedAt,
                            updatedBy = updatedBy
                        )
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing RTDB maintenance config: ${e.message}")
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(null)
            }
        }
        try {
            ref.addValueEventListener(listener)
        } catch (e: Exception) {
            Log.w(TAG, "Failed attaching RTDB maintenance listener: ${e.message}")
            trySend(null)
        }
        awaitClose { try { ref.removeEventListener(listener) } catch (_: Exception) {} }
    }

    suspend fun saveMaintenanceConfigToRTDB(config: MaintenanceConfig): Boolean {
        return try {
            val map = mapOf<String, Any>(
                "maintenanceEnabled" to config.maintenanceEnabled,
                "maintenanceStartTime" to config.maintenanceStartTime,
                "maintenanceEndTime" to config.maintenanceEndTime,
                "updatedAt" to config.updatedAt,
                "updatedBy" to config.updatedBy
            )
            database.getReference("admin_config")
                .child("maintenance")
                .setValue(map)
                .awaitSafe() != null
            true
        } catch (e: Exception) {
            Log.w(TAG, "Error saving maintenance config to RTDB: ${e.message}")
            false
        }
    }
}
