package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object GeminiDailyQuestionService {

    private const val PREFS_NAME = "kcet_daily_practice_prefs"
    private const val KEY_LAST_WINDOW_ID = "last_24h_window_id"
    private const val KEY_CACHED_QUESTIONS_JSON = "cached_daily_questions_json"

    private const val KEY_LAST_MOCK_WINDOW_ID = "last_24h_mock_window_id"
    private const val KEY_CACHED_MOCK_QUESTIONS_JSON = "cached_daily_mock_questions_json"
    private const val KEY_LAST_GENERATED_DATE = "last_generated_date_str"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Calculates the current 24-hour window ID (e.g. "2026-08-07_01:00").
     * The 24-hour cycle runs from 01:00 AM today to 12:59 AM next day.
     */
    fun getCurrent24hWindowInfo(): DailyWindowInfo {
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)

        val windowStart = Calendar.getInstance().apply {
            if (currentHour < 1) {
                // Between 00:00 and 00:59 -> Window started at 01:00 AM yesterday
                add(Calendar.DAY_OF_MONTH, -1)
            }
            set(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val windowEnd = (windowStart.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MILLISECOND, -1) // 12:59:59.999 AM next day
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val windowId = "DPP_${sdf.format(windowStart.time)}_01AM"

        val msRemaining = (windowEnd.timeInMillis - now.timeInMillis).coerceAtLeast(0L)

        val hoursLeft = msRemaining / (1000 * 60 * 60)
        val minsLeft = (msRemaining % (1000 * 60 * 60)) / (1000 * 60)
        val secsLeft = (msRemaining % (1000 * 60)) / 1000

        val countdownStr = String.format(Locale.getDefault(), "%02dh %02dm %02ds", hoursLeft, minsLeft, secsLeft)

        return DailyWindowInfo(
            windowId = windowId,
            windowStartDate = windowStart.time,
            windowEndDate = windowEnd.time,
            millisUntilReset = msRemaining,
            formattedCountdown = countdownStr
        )
    }

    data class DailyWindowInfo(
        val windowId: String,
        val windowStartDate: Date,
        val windowEndDate: Date,
        val millisUntilReset: Long,
        val formattedCountdown: String
    )

    /**
     * Checks if the 24-hour window has reset, and fetches or generates new questions if needed.
     */
    suspend fun getOrGenerateDailyQuestions(
        context: Context,
        forceRefresh: Boolean = false
    ): Pair<List<Question>, DailyWindowInfo> = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val windowInfo = getCurrent24hWindowInfo()
        val savedWindowId = prefs.getString(KEY_LAST_WINDOW_ID, "") ?: ""
        val cachedJson = prefs.getString(KEY_CACHED_QUESTIONS_JSON, "") ?: ""

        val isNewWindow = savedWindowId != windowInfo.windowId

        if (!forceRefresh && !isNewWindow && cachedJson.isNotBlank()) {
            val parsedList = parseQuestionsFromJson(cachedJson, windowInfo.windowId)
            if (parsedList.isNotEmpty()) {
                return@withContext Pair(parsedList, windowInfo)
            }
        }

        // Generate fresh questions via Gemini AI or Fallback
        val aiGeneratedQuestions = fetchDailyQuestionsFromGemini(windowInfo.windowId)

        val finalQuestions = if (aiGeneratedQuestions.isNotEmpty()) {
            aiGeneratedQuestions
        } else {
            generateSeededDailyQuestions(windowInfo.windowId)
        }

        // Cache the newly generated questions
        saveQuestionsToCache(prefs, windowInfo.windowId, finalQuestions)

        return@withContext Pair(finalQuestions, windowInfo)
    }

    /**
     * Checks if the 24-hour window has reset, and fetches or generates a full 60-question KCET Mock Test if needed.
     */
    suspend fun getOrGenerateDailyFullMockTest(
        context: Context,
        forceRefresh: Boolean = false
    ): Pair<List<Question>, DailyWindowInfo> = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val windowInfo = getCurrent24hWindowInfo()
        val savedWindowId = prefs.getString(KEY_LAST_MOCK_WINDOW_ID, "") ?: ""
        val cachedJson = prefs.getString(KEY_CACHED_MOCK_QUESTIONS_JSON, "") ?: ""

        val isNewWindow = savedWindowId != windowInfo.windowId

        if (!forceRefresh && !isNewWindow && cachedJson.isNotBlank()) {
            val parsedList = parseQuestionsFromJson(cachedJson, windowInfo.windowId)
            if (parsedList.size >= 30) {
                return@withContext Pair(parsedList, windowInfo)
            }
        }

        // Try AI generation for full 60 questions
        val aiGeneratedQuestions = fetchDailyFullMockFromGemini(windowInfo.windowId)

        val finalQuestions = if (aiGeneratedQuestions.size >= 30) {
            aiGeneratedQuestions
        } else {
            generateSeeded60MockQuestions(windowInfo.windowId)
        }

        // Cache full mock questions
        saveMockQuestionsToCache(prefs, windowInfo.windowId, finalQuestions)

        return@withContext Pair(finalQuestions, windowInfo)
    }

    private suspend fun fetchDailyFullMockFromGemini(windowId: String): List<Question> = withContext(Dispatchers.IO) {
        val candidateKeys = GeminiDoubtService.getCandidateKeys("GEMINI_DAILY_PRACTICE_KEY")
        val dateTag = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())

        val systemPrompt = """
            You are KCET Full Mock Test Engine. Generate exactly 60 fresh, standard KCET multiple-choice questions for today's Full Mock Test ($dateTag).
            Subject distribution: 20 Physics, 20 Chemistry, 20 Mathematics.
            
            OUTPUT FORMAT REQUIREMENTS:
            Respond STRICTLY with a clean JSON array of 60 question objects. No extra text or markdown wrappers.
            Each object must contain:
            - "subject": "PHYSICS", "CHEMISTRY", or "MATHEMATICS"
            - "questionText": string
            - "optionA": string
            - "optionB": string
            - "optionC": string
            - "optionD": string
            - "correctOption": "A", "B", "C", or "D"
            - "explanation": step-by-step solution string
        """.trimIndent()

        val prompt = "Generate today's 60 KCET Full Mock Test Questions (20 Physics, 20 Chemistry, 20 Math) with step-by-step solutions."

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", systemPrompt))
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.7)
            })
        }

        for (apiKey in candidateKeys) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-pro-preview:generateContent?key=$apiKey"
                val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder().url(url).header("x-goog-api-key", apiKey).post(body).build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val respBodyStr = response.body?.string() ?: ""
                    val parsedQuestions = parseGeminiResponse(respBodyStr, windowId)
                    if (parsedQuestions.size >= 20) {
                        return@withContext parsedQuestions
                    }
                }
            } catch (e: Exception) {
                // Try next
            }
        }

        return@withContext emptyList()
    }

    private suspend fun fetchDailyQuestionsFromGemini(windowId: String): List<Question> = withContext(Dispatchers.IO) {
        val candidateKeys = GeminiDoubtService.getCandidateKeys("GEMINI_DAILY_PRACTICE_KEY")

        val dateTag = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())

        val systemPrompt = """
            You are KCET Daily Question Engine. Generate exactly 15 fresh, high-quality KCET multiple-choice questions for today's Daily Practice Set ($dateTag).
            Subject distribution: 5 Physics, 5 Chemistry, 5 Mathematics.
            
            OUTPUT FORMAT REQUIREMENTS:
            Respond STRICTLY with a clean JSON array of 15 question objects. No extra text, markdown wrappers, or markdown code blocks.
            Each object must contain:
            - "subject": "PHYSICS", "CHEMISTRY", or "MATHEMATICS"
            - "questionText": string
            - "optionA": string
            - "optionB": string
            - "optionC": string
            - "optionD": string
            - "correctOption": "A", "B", "C", or "D"
            - "explanation": step-by-step solution string
        """.trimIndent()

        val prompt = "Generate today's 15 KCET Practice Questions for Physics, Chemistry, and Mathematics with step-by-step solutions."

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", systemPrompt))
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.7)
            })
        }

        for (apiKey in candidateKeys) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-pro-preview:generateContent?key=$apiKey"
                val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder().url(url).header("x-goog-api-key", apiKey).post(body).build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val respBodyStr = response.body?.string() ?: ""
                    val parsedQuestions = parseGeminiResponse(respBodyStr, windowId)
                    if (parsedQuestions.size >= 10) {
                        return@withContext parsedQuestions
                    }
                }
            } catch (e: Exception) {
                // Try next candidate key
            }
        }

        return@withContext emptyList()
    }

    private fun parseGeminiResponse(jsonString: String, windowId: String): List<Question> {
        val resultList = mutableListOf<Question>()
        try {
            val root = JSONObject(jsonString)
            val candidates = root.optJSONArray("candidates") ?: return emptyList()
            val candidate = candidates.optJSONObject(0) ?: return emptyList()
            val content = candidate.optJSONObject("content") ?: return emptyList()
            val parts = content.optJSONArray("parts") ?: return emptyList()
            val rawText = parts.optJSONObject(0)?.optString("text", "") ?: ""

            val cleanJson = rawText.replace("```json", "").replace("```", "").trim()
            val jsonArray = JSONArray(cleanJson)

            val baseId = 900000 + (windowId.hashCode() % 50000).let { if (it < 0) -it else it }

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val subjStr = obj.optString("subject", "PHYSICS").uppercase()
                val subj = when {
                    subjStr.contains("CHEM") -> Subject.CHEMISTRY
                    subjStr.contains("MATH") -> Subject.MATHEMATICS
                    subjStr.contains("BIO") -> Subject.BIOLOGY
                    else -> Subject.PHYSICS
                }

                val correctOptStr = obj.optString("correctOption", "A").uppercase()
                val correctOpt = when (correctOptStr) {
                    "B" -> Option.B
                    "C" -> Option.C
                    "D" -> Option.D
                    else -> Option.A
                }

                resultList.add(
                    Question(
                        id = baseId + i + 1,
                        subject = subj,
                        questionText = obj.optString("questionText", "Daily Question ${i + 1}"),
                        optionA = obj.optString("optionA", "Option A"),
                        optionB = obj.optString("optionB", "Option B"),
                        optionC = obj.optString("optionC", "Option C"),
                        optionD = obj.optString("optionD", "Option D"),
                        correctOption = correctOpt,
                        explanation = obj.optString("explanation", "Step-by-step solution provided by AI."),
                        testSetId = "ts_daily_ai",
                        isPremium = false
                    )
                )
            }
        } catch (e: Exception) {
            // Error parsing
        }
        return resultList
    }

    private fun parseQuestionsFromJson(jsonStr: String, windowId: String): List<Question> {
        val list = mutableListOf<Question>()
        try {
            val arr = JSONArray(jsonStr)
            val baseId = 900000 + (windowId.hashCode() % 50000).let { if (it < 0) -it else it }
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val subjStr = obj.optString("subject", "PHYSICS")
                val subj = try { Subject.valueOf(subjStr) } catch (e: Exception) { Subject.PHYSICS }
                val optStr = obj.optString("correctOption", "A")
                val correctOpt = try { Option.valueOf(optStr) } catch (e: Exception) { Option.A }

                list.add(
                    Question(
                        id = baseId + i + 1,
                        subject = subj,
                        questionText = obj.getString("questionText"),
                        optionA = obj.getString("optionA"),
                        optionB = obj.getString("optionB"),
                        optionC = obj.getString("optionC"),
                        optionD = obj.getString("optionD"),
                        correctOption = correctOpt,
                        explanation = obj.getString("explanation"),
                        testSetId = "ts_daily_ai",
                        isPremium = false
                    )
                )
            }
        } catch (e: Exception) {
            // Fallback
        }
        return list
    }

    private fun saveQuestionsToCache(prefs: SharedPreferences, windowId: String, questions: List<Question>) {
        try {
            val arr = JSONArray()
            questions.forEach { q ->
                val obj = JSONObject().apply {
                    put("subject", q.subject.name)
                    put("questionText", q.questionText)
                    put("optionA", q.optionA)
                    put("optionB", q.optionB)
                    put("optionC", q.optionC)
                    put("optionD", q.optionD)
                    put("correctOption", q.correctOption.name)
                    put("explanation", q.explanation)
                }
                arr.put(obj)
            }
            prefs.edit()
                .putString(KEY_LAST_WINDOW_ID, windowId)
                .putString(KEY_CACHED_QUESTIONS_JSON, arr.toString())
                .apply()
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun saveMockQuestionsToCache(prefs: SharedPreferences, windowId: String, questions: List<Question>) {
        try {
            val arr = JSONArray()
            questions.forEach { q ->
                val obj = JSONObject().apply {
                    put("subject", q.subject.name)
                    put("questionText", q.questionText)
                    put("optionA", q.optionA)
                    put("optionB", q.optionB)
                    put("optionC", q.optionC)
                    put("optionD", q.optionD)
                    put("correctOption", q.correctOption.name)
                    put("explanation", q.explanation)
                }
                arr.put(obj)
            }
            prefs.edit()
                .putString(KEY_LAST_MOCK_WINDOW_ID, windowId)
                .putString(KEY_CACHED_MOCK_QUESTIONS_JSON, arr.toString())
                .apply()
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun generateSeeded60MockQuestions(windowId: String): List<Question> {
        val seed = windowId.hashCode()
        val baseId = 800000 + (seed % 20000).let { if (it < 0) -it else it }

        val pool = SampleData.sampleQuestions
        val shuffledPool = pool.shuffled(java.util.Random(seed.toLong()))
        val list = mutableListOf<Question>()

        for (i in 0 until 60) {
            val sampleQ = shuffledPool[i % shuffledPool.size]
            list.add(
                sampleQ.copy(
                    id = baseId + i + 1,
                    testSetId = "ts_daily_mock"
                )
            )
        }
        return list
    }

    private fun generateSeededDailyQuestions(windowId: String): List<Question> {
        val seed = windowId.hashCode()
        val baseId = 950000 + (seed % 10000).let { if (it < 0) -it else it }

        val pool = SampleData.sampleQuestions.ifEmpty {
            listOf(
                Question(
                    id = 1,
                    subject = Subject.PHYSICS,
                    questionText = "A particle moves in a circle of radius R with constant speed v. What is its centripetal acceleration?",
                    optionA = "Zero",
                    optionB = "v² / R towards center",
                    optionC = "v / R away from center",
                    optionD = "v² / R tangentially",
                    correctOption = Option.B,
                    explanation = "In uniform circular motion, centripetal acceleration is v²/R directed toward the center."
                )
            )
        }

        val shuffledPool = pool.shuffled(java.util.Random(seed.toLong()))
        val list = mutableListOf<Question>()

        for (i in 0 until 15) {
            val sampleQ = shuffledPool[i % shuffledPool.size]
            list.add(
                sampleQ.copy(
                    id = baseId + i + 1,
                    questionText = "🤖 [Daily AI Practice Q${i + 1}] ${sampleQ.questionText}",
                    testSetId = "ts_daily_ai"
                )
            )
        }
        return list
    }
}
