package com.example.data

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SavedDoubtChat(
    val id: String = "chat_${System.currentTimeMillis()}_${(100..999).random()}",
    val questionText: String,
    val aiAnswerText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val formattedTime: String = "",
    val subjectTag: String = "KCET Doubt",
    val hasImage: Boolean = false
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("questionText", questionText)
            put("aiAnswerText", aiAnswerText)
            put("timestamp", timestamp)
            put("formattedTime", formattedTime)
            put("subjectTag", subjectTag)
            put("hasImage", hasImage)
        }
    }

    companion object {
        fun fromJsonObject(json: JSONObject): SavedDoubtChat {
            return SavedDoubtChat(
                id = json.optString("id", "chat_${System.currentTimeMillis()}"),
                questionText = json.optString("questionText", ""),
                aiAnswerText = json.optString("aiAnswerText", ""),
                timestamp = json.optLong("timestamp", System.currentTimeMillis()),
                formattedTime = json.optString("formattedTime", "Recently"),
                subjectTag = json.optString("subjectTag", "KCET Guru"),
                hasImage = json.optBoolean("hasImage", false)
            )
        }
    }
}

object RecentDoubtChatManager {

    private const val TAG = "RecentDoubtChatManager"
    private const val PREFS_NAME = "kcet_ai_doubt_history_prefs"
    private const val KEY_SAVED_CHATS = "saved_recent_ai_chats_v1"
    private const val KEY_ACTIVE_CHAT_MESSAGES = "active_doubt_messages_v1"
    const val MAX_RECENT_CHATS = 50

    /**
     * Loads permanently stored active chat messages for the current session.
     */
    fun getActiveChatMessages(context: Context, userId: String): List<DoubtMessage> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rawJson = prefs.getString("${KEY_ACTIVE_CHAT_MESSAGES}_$userId", null) ?: return emptyList()

        return try {
            val jsonArray = JSONArray(rawJson)
            val list = mutableListOf<DoubtMessage>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(DoubtMessage.fromJsonObject(obj))
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing active doubt messages: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Saves active chat messages stream permanently so app refreshes preserve state.
     */
    fun saveActiveChatMessages(context: Context, messages: List<DoubtMessage>, userId: String) {
        try {
            val jsonArray = JSONArray()
            messages.filter { !it.isThinking }.takeLast(50).forEach { msg ->
                jsonArray.put(msg.toJsonObject())
            }
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString("${KEY_ACTIVE_CHAT_MESSAGES}_$userId", jsonArray.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save active chat messages: ${e.message}", e)
        }
    }

    /**
     * Loads permanently stored recent chats (maximum 50 items, newest first).
     */
    fun getRecentChats(context: Context, userId: String): List<SavedDoubtChat> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rawJson = prefs.getString("${KEY_SAVED_CHATS}_$userId", null) ?: return emptyList()

        return try {
            val jsonArray = JSONArray(rawJson)
            val list = mutableListOf<SavedDoubtChat>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(SavedDoubtChat.fromJsonObject(obj))
            }
            list.take(MAX_RECENT_CHATS)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing saved recent chats: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Permanently stores a new doubt Q&A session.
     * Enforces the 5-chat maximum limit: when the 6th chat is added, the oldest chat is automatically deleted.
     */
    fun saveNewChat(
        context: Context,
        question: String,
        answer: String,
        userId: String,
        customSubjectTag: String? = null,
        hasImage: Boolean = false
    ): List<SavedDoubtChat> {
        if (question.isBlank() && answer.isBlank()) return getRecentChats(context, userId)

        val existingList = getRecentChats(context, userId).toMutableList()

        val timeStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
        val detectedTag = customSubjectTag ?: detectSubjectTag(question, answer)

        val newChat = SavedDoubtChat(
            id = "chat_${System.currentTimeMillis()}_${(100..999).random()}",
            questionText = question.trim(),
            aiAnswerText = answer.trim(),
            timestamp = System.currentTimeMillis(),
            formattedTime = timeStr,
            subjectTag = detectedTag,
            hasImage = hasImage
        )

        // Remove any identical previous question to avoid duplicate entries and promote to top
        existingList.removeAll { it.questionText.trim().equals(question.trim(), ignoreCase = true) }

        // Insert newest chat at the beginning
        existingList.add(0, newChat)

        // AUTOMATIC EXPIRY: Strictly keep only the 5 most recent chats (6th and older automatically discarded)
        val trimmedList = existingList.take(MAX_RECENT_CHATS)

        saveToPrefs(context, trimmedList, userId)
        return trimmedList
    }

    /**
     * Deletes a specific chat item.
     */
    fun deleteChat(context: Context, chatId: String, userId: String): List<SavedDoubtChat> {
        val list = getRecentChats(context, userId).filter { it.id != chatId }
        saveToPrefs(context, list, userId)
        return list
    }

    /**
     * Clears all permanently saved recent chats.
     */
    fun clearAllChats(context: Context, userId: String): List<SavedDoubtChat> {
        saveToPrefs(context, emptyList(), userId)
        return emptyList()
    }

    private fun saveToPrefs(context: Context, list: List<SavedDoubtChat>, userId: String) {
        try {
            val jsonArray = JSONArray()
            list.take(MAX_RECENT_CHATS).forEach { chat ->
                jsonArray.put(chat.toJsonObject())
            }
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString("${KEY_SAVED_CHATS}_$userId", jsonArray.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save recent chats to SharedPreferences: ${e.message}", e)
        }
    }

    fun detectSubjectTag(question: String, answer: String): String {
        val combined = "$question $answer".lowercase()

        return when {
            combined.contains("circuit") || combined.contains("ohm") || combined.contains("ray optics") ||
                    combined.contains("snell") || combined.contains("velocity") || combined.contains("force") ||
                    combined.contains("gravitation") || combined.contains("physics") || combined.contains("lens") ||
                    combined.contains("capacitance") || combined.contains("voltage") || combined.contains("resistance") ||
                    combined.contains("carnot") || combined.contains("photoelectric") || combined.contains("de-broglie") -> "⚛️ Physics"

            combined.contains("markovnikov") || combined.contains("acid") || combined.contains("reaction") ||
                    combined.contains("chemistry") || combined.contains("organic") || combined.contains("polymer") ||
                    combined.contains("molar") || combined.contains("equilibrium") || combined.contains("hcl") ||
                    combined.contains("naoh") || combined.contains("benzene") || combined.contains("terylene") ||
                    combined.contains("vsepr") || combined.contains("hybridization") -> "🧪 Chemistry"

            combined.contains("integral") || combined.contains("derivative") || combined.contains("matrix") ||
                    combined.contains("determinant") || combined.contains("limit") || combined.contains("math") ||
                    combined.contains("calculus") || combined.contains("vector") || combined.contains("trigonometry") ||
                    combined.contains("plane") || combined.contains("probability") || combined.contains("permutation") ||
                    combined.contains("dx") || combined.contains("dy/dx") -> "📐 Mathematics"

            combined.contains("botany") || combined.contains("zoology") || combined.contains("dna") ||
                    combined.contains("cell") || combined.contains("biology") || combined.contains("photosynthesis") ||
                    combined.contains("genetics") || combined.contains("ecology") || combined.contains("hormone") -> "🌱 Biology"

            combined.contains("counselling") || combined.contains("kea") || combined.contains("cutoff") ||
                    combined.contains("seat") || combined.contains("allotment") || combined.contains("option entry") ||
                    combined.contains("document verification") || combined.contains("rank") -> "🏛️ KEA Counselling"

            else -> "🤖 KCET Guru"
        }
    }
}
