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
        if (auth.currentUser == null) {
            Log.d(TAG, "Unauthenticated user, skipping senior chat messages observe")
            trySend(emptyList())
            close()
            return@callbackFlow
        }

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
                val connected = snapshot.getValue(Boolean::class.java) ?: false
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

        val presenceRef = database.getReference("presence/$uid")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val online = snapshot.child("online").getValue(Boolean::class.java) ?: false
                val lastSeen = snapshot.child("lastSeen").getValue(Long::class.java) ?: System.currentTimeMillis()
                val name = snapshot.child("userName").getValue(String::class.java) ?: "Student"
                val role = snapshot.child("role").getValue(String::class.java) ?: "Student"

                trySend(UserPresence(uid = uid, userName = name, online = online, lastSeen = lastSeen, role = role))
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "User presence observe cancelled: ${error.message}")
            }
        }

        presenceRef.addValueEventListener(listener)
        awaitClose { presenceRef.removeEventListener(listener) }
    }

    fun observeAllOnlineUsers(): Flow<List<UserPresence>> = callbackFlow {
        if (auth.currentUser == null) {
            Log.d(TAG, "Unauthenticated user, skipping all online users observe")
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val presenceRef = database.getReference("presence")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val activeList = mutableListOf<UserPresence>()
                for (child in snapshot.children) {
                    val userUid = child.key ?: ""
                    val online = child.child("online").getValue(Boolean::class.java) ?: false
                    val lastSeen = child.child("lastSeen").getValue(Long::class.java) ?: System.currentTimeMillis()
                    val name = child.child("userName").getValue(String::class.java) ?: "Student"
                    val role = child.child("role").getValue(String::class.java) ?: "Student"

                    if (online) {
                        activeList.add(UserPresence(uid = userUid, userName = name, online = true, lastSeen = lastSeen, role = role))
                    }
                }
                trySend(activeList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "All presence observe cancelled: ${error.message}")
            }
        }

        presenceRef.addValueEventListener(listener)
        awaitClose { presenceRef.removeEventListener(listener) }
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

        val xpRef = database.getReference("liveXP/$uid")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("userName").getValue(String::class.java) ?: "Student"
                val xp = snapshot.child("xp").getValue(Long::class.java)?.toInt() ?: 0
                val rank = snapshot.child("rank").getValue(Long::class.java)?.toInt() ?: 1
                val targetRank = snapshot.child("targetRank").getValue(String::class.java) ?: "Under 1000"
                val level = snapshot.child("level").getValue(Long::class.java)?.toInt() ?: 1
                val updatedAt = snapshot.child("updatedAt").getValue(Long::class.java) ?: System.currentTimeMillis()

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
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Live XP observe cancelled: ${error.message}")
            }
        }

        xpRef.addValueEventListener(listener)
        awaitClose { xpRef.removeEventListener(listener) }
    }

    fun observeAllLiveXP(): Flow<Map<String, LiveXpData>> = callbackFlow {
        if (auth.currentUser == null) {
            Log.d(TAG, "Unauthenticated user, skipping all live XP observe")
            trySend(emptyMap())
            close()
            return@callbackFlow
        }

        val xpRef = database.getReference("liveXP")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val map = mutableMapOf<String, LiveXpData>()
                for (child in snapshot.children) {
                    val userUid = child.key ?: ""
                    val name = child.child("userName").getValue(String::class.java) ?: "Student"
                    val xp = child.child("xp").getValue(Long::class.java)?.toInt() ?: 0
                    val rank = child.child("rank").getValue(Long::class.java)?.toInt() ?: 1
                    val targetRank = child.child("targetRank").getValue(String::class.java) ?: "Under 1000"
                    val level = child.child("level").getValue(Long::class.java)?.toInt() ?: 1
                    val updatedAt = child.child("updatedAt").getValue(Long::class.java) ?: System.currentTimeMillis()

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
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "All Live XP observe cancelled: ${error.message}")
            }
        }

        xpRef.addValueEventListener(listener)
        awaitClose { xpRef.removeEventListener(listener) }
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
        if (auth.currentUser == null) {
            Log.d(TAG, "Unauthenticated user, skipping 1-to-1 chat observe")
            trySend(emptyList())
            close()
            return@callbackFlow
        }

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
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Observe mentors notice (${error.code}): ${error.message}")
                emitMerged()
            }
        }

        val profileRef = database.getReference("seniorMentorProfile")
        val profileListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                singleProfile = parseSeniorMentorFromSnapshot(snapshot)
                emitMerged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Observe profile notice (${error.code}): ${error.message}")
                emitMerged()
            }
        }

        fun attachDatabaseListeners() {
            mentorsRef.addValueEventListener(mentorsListener)
            profileRef.addValueEventListener(profileListener)
        }

        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                attachDatabaseListeners()
            } else {
                firebaseAuth.signInAnonymously().addOnSuccessListener {
                    attachDatabaseListeners()
                }.addOnFailureListener {
                    emitMerged()
                }
            }
        }

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

        val fsJob = launch(Dispatchers.IO) {
            try {
                FirebaseService.observeAllSeniorMentorsFromFirestore().collect { list ->
                    fsMentors = list
                    emitMerged()
                }
            } catch (_: Exception) {}
        }

        awaitClose {
            auth.removeAuthStateListener(authStateListener)
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
            try { FirebaseService.deleteSeniorMentorFromFirestore(mentorName) } catch (_: Exception) {}
        }

        mentorsRef.removeValue().addOnCompleteListener { task ->
            if (continuation.isActive) {
                continuation.resume(true)
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
        val ref = database.getReference("admin_config").child("pin")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pinVal = snapshot.getValue(String::class.java) ?: snapshot.value?.toString()
                trySend(pinVal)
            }
            override fun onCancelled(error: DatabaseError) {
                trySend(null)
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
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
        val ref = database.getReference("globallyDeletedNotifications")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val set = mutableSetOf<String>()
                for (child in snapshot.children) {
                    val key = child.key
                    if (!key.isNullOrBlank()) {
                        set.add(key)
                    }
                }
                trySend(set)
            }
            override fun onCancelled(error: DatabaseError) {
                trySend(emptySet())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
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
        val ref = database.getReference("admin_config").child("pricing")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.value as? Map<String, Any>
                trySend(value)
            }
            override fun onCancelled(error: DatabaseError) {
                trySend(null)
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}
