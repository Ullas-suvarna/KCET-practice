package com.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.data.AdminEmailApiResult
import com.example.data.AdminEmailDeliveryStatus
import com.example.data.AdminEmailLogItem
import com.example.data.AdminEmailService
import com.example.data.AdminRegisteredStudent
import com.example.ui.screens.AdminEmailNotificationsSection
import com.example.viewmodel.AdminEmailNotificationUiState
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.SocketTimeoutException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class AdminEmailServiceTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun createMockClient(
        handler: (okhttp3.Request, String?) -> Response
    ): OkHttpClient {
        val interceptor = Interceptor { chain ->
            val request = chain.request()
            val bodyString = request.body?.let { body ->
                val buffer = Buffer()
                body.writeTo(buffer)
                buffer.readUtf8()
            }
            handler(request, bodyString)
        }
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    private fun jsonResponse(request: okhttp3.Request, code: Int, json: String): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code in 200..299) "OK" else "Error")
            .body(json.toResponseBody("application/json".toMediaType()))
            .build()
    }

    @Test
    fun `fetchAllStudents attaches Bearer token and follows nextPageToken pagination`() = runTest {
        val requestedUrls = mutableListOf<String>()
        val authHeaders = mutableListOf<String>()

        val client = createMockClient { request, _ ->
            requestedUrls.add(request.url.toString())
            authHeaders.add(request.header("Authorization").orEmpty())
            val pageToken = request.url.queryParameter("pageToken")
            if (pageToken == null) {
                jsonResponse(
                    request,
                    200,
                    """
                    {
                      "students": [
                        {"uid": "u1", "name": "Aarav Sharma", "email": "aarav@gmail.com", "studentId": "KCET2026-001"},
                        {"uid": "u2", "name": "Diya Nair", "email": "diya.nair@gmail.com", "studentId": "KCET2026-002"}
                      ],
                      "nextPageToken": "page_2_tok"
                    }
                    """.trimIndent()
                )
            } else {
                assertEquals("page_2_tok", pageToken)
                jsonResponse(
                    request,
                    200,
                    """
                    {
                      "students": [
                        {"uid": "u3", "name": "Kiran Rao", "email": "kiran@gmail.com", "studentId": "KCET2026-003"}
                      ],
                      "nextPageToken": null
                    }
                    """.trimIndent()
                )
            }
        }

        val service = AdminEmailService(
            httpClient = client,
            tokenProvider = { AdminEmailApiResult.Success("firebase-admin-jwt") }
        )

        val result = service.fetchAllStudents()
        assertTrue(result is AdminEmailApiResult.Success)
        val students = (result as AdminEmailApiResult.Success).data
        assertEquals(3, students.size)
        assertEquals(listOf("u1", "u2", "u3"), students.map { it.uid })
        assertEquals(2, requestedUrls.size)
        assertTrue(requestedUrls[1].contains("pageToken=page_2_tok"))
        assertTrue(authHeaders.all { it == "Bearer firebase-admin-jwt" })
    }

    @Test
    fun `filterStudents matches by student name, registered gmail, and studentId`() {
        val sample = listOf(
            AdminRegisteredStudent("u1", "Aarav Sharma", "aarav.sharma@gmail.com", "KCET-101"),
            AdminRegisteredStudent("u2", "Meera Kulkarni", "meera.k@gmail.com", "KCET-205"),
            AdminRegisteredStudent("u3", "Rohan Shetty", "rohan.s@gmail.com", "KCET-309")
        )

        assertEquals(listOf("u1"), AdminEmailService.filterStudents(sample, "aarav").map { it.uid })
        assertEquals(listOf("u2"), AdminEmailService.filterStudents(sample, "meera.k@gmail").map { it.uid })
        assertEquals(listOf("u3"), AdminEmailService.filterStudents(sample, "KCET-309").map { it.uid })
        assertEquals(3, AdminEmailService.filterStudents(sample, "   ").size)
    }

    @Test
    fun `sendStudentEmail sends only studentUid, subject, message with Bearer token`() = runTest {
        var capturedMethod = ""
        var capturedAuth = ""
        var capturedContentType = ""
        var capturedBody = ""

        val client = createMockClient { request, body ->
            capturedMethod = request.method
            capturedAuth = request.header("Authorization").orEmpty()
            capturedContentType = request.header("Content-Type").orEmpty()
            capturedBody = body.orEmpty()
            jsonResponse(
                request,
                200,
                """{"success": true, "recipientEmail": "aarav.sharma@gmail.com", "message": "Email sent"}"""
            )
        }

        val service = AdminEmailService(
            httpClient = client,
            tokenProvider = { AdminEmailApiResult.Success("admin-token-999") }
        )

        val multilineMsg = "Hello Aarav,\nYour KCET mock review is ready.\nRegards, Admin"
        val result = service.sendStudentEmail(
            studentUid = "uid_aarav_123",
            subject = "Mock Test Feedback",
            message = multilineMsg
        )

        assertTrue(result is AdminEmailApiResult.Success)
        val success = (result as AdminEmailApiResult.Success).data
        assertEquals("aarav.sharma@gmail.com", success.recipientEmail)

        assertEquals("POST", capturedMethod)
        assertEquals("Bearer admin-token-999", capturedAuth)
        assertTrue(capturedContentType.contains("application/json"))

        val sentJson = JSONObject(capturedBody)
        assertEquals("uid_aarav_123", sentJson.getString("studentUid"))
        assertEquals("Mock Test Feedback", sentJson.getString("subject"))
        assertEquals(multilineMsg, sentJson.getString("message"))
        assertEquals(3, sentJson.length())
        assertFalse(sentJson.has("email"))
        assertFalse(sentJson.has("recipientEmail"))
        assertFalse(sentJson.has("to"))
    }

    @Test
    fun `sendStudentEmail flags timeout uncertainty and blocks blank fields`() = runTest {
        var calls = 0
        val timeoutClient = createMockClient { _, _ ->
            calls++
            throw SocketTimeoutException("Read timed out")
        }

        val service = AdminEmailService(
            httpClient = timeoutClient,
            tokenProvider = { AdminEmailApiResult.Success("admin-token") }
        )

        val blankSubjectResult = service.sendStudentEmail("u1", "   ", "Valid message")
        assertTrue(blankSubjectResult is AdminEmailApiResult.Error)
        assertEquals(0, calls)

        val timeoutResult = service.sendStudentEmail("u1", "Alert", "Check status")
        assertTrue(timeoutResult is AdminEmailApiResult.Error)
        val error = timeoutResult as AdminEmailApiResult.Error
        assertTrue(error.isTimeoutUncertain)
        assertTrue(error.isNetworkError)
        assertTrue(error.message.contains("Email History", ignoreCase = true))
    }

    @Test
    fun `sendStudentEmail maps 401 and 403 auth errors without retrying`() = runTest {
        val client403 = createMockClient { request, _ ->
            jsonResponse(request, 403, """{"error": "Forbidden: not an admin"}""")
        }
        val service = AdminEmailService(
            httpClient = client403,
            tokenProvider = { AdminEmailApiResult.Success("non-admin-token") }
        )

        val result = service.sendStudentEmail("u1", "Hello", "World")
        assertTrue(result is AdminEmailApiResult.Error)
        val err = result as AdminEmailApiResult.Error
        assertEquals(403, err.httpCode)
        assertTrue(err.isAuthError)
    }

    @Test
    fun `fetchEmailLogs parses Submitted, Uncertain, and Failed entries`() = runTest {
        val client = createMockClient { request, _ ->
            jsonResponse(
                request,
                200,
                """
                {
                  "logs": [
                    {
                      "id": "log1",
                      "recipientName": "Aarav Sharma",
                      "recipientEmail": "aarav@gmail.com",
                      "subject": "Hall Ticket Reminder",
                      "status": "Submitted",
                      "timestamp": "2026-04-02T08:30:00.000Z"
                    },
                    {
                      "id": "log2",
                      "recipientName": "Diya Nair",
                      "recipientEmail": "diya@gmail.com",
                      "subject": "Schedule Change",
                      "status": "uncertain",
                      "timestamp": "2026-04-02T09:00:00.000Z",
                      "errorMessage": "Upstream gateway timed out"
                    },
                    {
                      "id": "log3",
                      "recipientName": "Kiran Rao",
                      "recipientEmail": "kiran@gmail.com",
                      "subject": "Fee Verification",
                      "status": "failed",
                      "timestamp": "2026-04-02T09:15:00.000Z",
                      "errorMessage": "EmailJS rejected template"
                    }
                  ]
                }
                """.trimIndent()
            )
        }

        val service = AdminEmailService(
            httpClient = client,
            tokenProvider = { AdminEmailApiResult.Success("token-logs") }
        )

        val result = service.fetchEmailLogs()
        assertTrue(result is AdminEmailApiResult.Success)
        val logs = (result as AdminEmailApiResult.Success).data
        assertEquals(3, logs.size)
        assertEquals(AdminEmailDeliveryStatus.SUBMITTED, logs[0].status)
        assertEquals(AdminEmailDeliveryStatus.UNCERTAIN, logs[1].status)
        assertEquals(AdminEmailDeliveryStatus.FAILED, logs[2].status)
        assertEquals("EmailJS rejected template", logs[2].errorDetail)
    }

    @Test
    fun `AdminEmailNotificationsSection supports search, selection, live preview, confirmation dialog, and history`() {
        val students = listOf(
            AdminRegisteredStudent("u1", "Aarav Sharma", "aarav.sharma@gmail.com", "KCET-101"),
            AdminRegisteredStudent("u2", "Meera Kulkarni", "meera.k@gmail.com", "KCET-205")
        )
        val logs = listOf(
            AdminEmailLogItem(
                id = "1",
                recipientName = "Aarav Sharma",
                recipientEmail = "aarav.sharma@gmail.com",
                subject = "Exam Reminder",
                status = AdminEmailDeliveryStatus.SUBMITTED,
                rawStatus = "Submitted",
                timestampFormatted = "02 Apr 2026, 02:00 PM"
            ),
            AdminEmailLogItem(
                id = "2",
                recipientName = "Meera Kulkarni",
                recipientEmail = "meera.k@gmail.com",
                subject = "Doubt Slot",
                status = AdminEmailDeliveryStatus.UNCERTAIN,
                rawStatus = "Uncertain",
                timestampFormatted = "02 Apr 2026, 02:05 PM"
            ),
            AdminEmailLogItem(
                id = "3",
                recipientName = "Rohan Shetty",
                recipientEmail = "rohan@gmail.com",
                subject = "Receipt",
                status = AdminEmailDeliveryStatus.FAILED,
                rawStatus = "Failed",
                timestampFormatted = "02 Apr 2026, 02:10 PM",
                errorDetail = "Invalid email template"
            )
        )

        var uiState by mutableStateOf(
            AdminEmailNotificationUiState(
                isFirebaseSignedIn = true,
                firebaseAuthEmail = "ullassuvarna65@gmail.com",
                isFirebaseEmailVerified = true,
                students = students,
                isStudentsLoaded = true,
                emailLogs = logs,
                isLogsLoaded = true
            )
        )
        var sendTriggeredCount = 0

        composeRule.setContent {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                AdminEmailNotificationsSection(
                    uiState = uiState,
                    onLoadStudents = {},
                    onSearchQueryChange = { q -> uiState = uiState.copy(searchQuery = q) },
                    onSelectStudent = { s -> uiState = uiState.copy(selectedStudent = s) },
                    onSubjectChange = { sub -> uiState = uiState.copy(subject = sub) },
                    onMessageChange = { msg -> uiState = uiState.copy(message = msg) },
                    onSendEmail = { sendTriggeredCount++ },
                    onLoadEmailLogs = {},
                    onDismissFeedback = {}
                )
            }
        }

        // 1. Search by student ID / Gmail and select student
        composeRule.onNodeWithTag("admin_email_student_search_input").performTextInput("KCET-205")
        composeRule.onNodeWithTag("student_option_u2").performScrollTo().performClick()

        composeRule.onNodeWithTag("selected_student_name").assertTextEquals("Meera Kulkarni")
        composeRule.onNodeWithTag("selected_student_readonly_email").assertTextEquals("meera.k@gmail.com")
        composeRule.onNodeWithTag("composer_readonly_recipient")
            .assertTextContains("Meera Kulkarni <meera.k@gmail.com>")

        // 2. Tapping Send with blank subject shows validation warning without triggering send
        composeRule.onNodeWithTag("admin_email_send_button").performScrollTo().performClick()
        composeRule.onNodeWithTag("composer_validation_error").assertIsDisplayed()
        assertEquals(0, sendTriggeredCount)

        // 3. Enter subject and multiline message; verify preview updates live
        composeRule.onNodeWithTag("admin_email_subject_input").performScrollTo().performTextInput("KCET Physics Doubt Slot")
        composeRule.onNodeWithTag("admin_email_message_input").performScrollTo().performTextInput("Line 1\nLine 2")

        composeRule.onNodeWithTag("preview_subject_text").assertTextEquals("KCET Physics Doubt Slot")
        composeRule.onNodeWithTag("preview_message_text").assertTextContains("Line 1\nLine 2")

        // 4. Open confirmation dialog -> cancel does not send -> confirm triggers send
        composeRule.onNodeWithTag("admin_email_send_button").performScrollTo().performClick()
        composeRule.onNodeWithTag("send_confirmation_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("confirm_dialog_recipient_email").assertTextContains("meera.k@gmail.com")
        composeRule.onNodeWithTag("confirm_dialog_subject").assertTextContains("KCET Physics Doubt Slot")
        assertEquals(0, sendTriggeredCount)

        composeRule.onNodeWithTag("confirm_send_confirm_button").performClick()
        assertEquals(1, sendTriggeredCount)

        // 5. While isSending = true, Send button must be disabled
        uiState = uiState.copy(isSending = true)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("admin_email_send_button").assertIsNotEnabled()

        // 6. Verify Email History status badges
        composeRule.onNodeWithTag("email_log_status_0").performScrollTo().assertTextContains("Submitted")
        composeRule.onNodeWithTag("email_log_status_1").performScrollTo().assertTextContains("Uncertain")
        composeRule.onNodeWithTag("email_log_status_2").performScrollTo().assertTextContains("Failed")
    }
}
