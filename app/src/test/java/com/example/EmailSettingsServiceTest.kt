package com.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.data.EmailDeliverySettings
import com.example.data.EmailSettingsApiResult
import com.example.data.EmailSettingsService
import com.example.ui.screens.EmailDeliverySettingsSection
import com.example.viewmodel.EmailDeliveryUiState
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class EmailSettingsServiceTest {

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
    fun `fetchEmailSettings sends Bearer token and parses ENABLED response`() = runTest {
        var capturedMethod = ""
        var capturedAuthHeader = ""
        var capturedContentType = ""

        val client = createMockClient { request, _ ->
            capturedMethod = request.method
            capturedAuthHeader = request.header("Authorization").orEmpty()
            capturedContentType = request.header("Content-Type").orEmpty()
            jsonResponse(
                request,
                200,
                """
                {
                  "activeProvider": "emailjs",
                  "emailjsStatus": "ENABLED",
                  "customEmailEnabled": true,
                  "lastFailureReason": null,
                  "lastUpdatedTime": "2026-04-01T10:15:30.000Z"
                }
                """.trimIndent()
            )
        }

        val service = EmailSettingsService(
            endpointUrl = EmailSettingsService.DEFAULT_ENDPOINT_URL,
            httpClient = client,
            tokenProvider = { EmailSettingsApiResult.Success("test-firebase-id-token") }
        )

        val result = service.fetchEmailSettings()
        assertTrue(result is EmailSettingsApiResult.Success)
        val settings = (result as EmailSettingsApiResult.Success).data

        assertEquals("GET", capturedMethod)
        assertEquals("Bearer test-firebase-id-token", capturedAuthHeader)
        assertTrue(capturedContentType.contains("application/json"))
        assertEquals("emailjs", settings.activeProvider)
        assertEquals("ENABLED", settings.emailjsStatus)
        assertTrue(settings.customEmailEnabled)
        assertNull(settings.lastFailureReason)
        assertTrue(settings.lastUpdatedTime.isNotBlank() && settings.lastUpdatedTime != "Never updated")
    }

    @Test
    fun `updateEmailSettings posts customEmailEnabled true and false cleanly`() = runTest {
        val capturedBodies = mutableListOf<String>()
        var currentEnabled = false

        val client = createMockClient { request, body ->
            if (request.method == "POST") {
                capturedBodies.add(body.orEmpty())
                currentEnabled = body.orEmpty().contains("true")
            }
            val provider = if (currentEnabled) "emailjs" else "firebase"
            val status = if (currentEnabled) "ENABLED" else "DISABLED"
            jsonResponse(
                request,
                200,
                """
                {
                  "ok": true,
                  "settings": {
                    "activeProvider": "$provider",
                    "emailjsStatus": "$status",
                    "customEmailEnabled": $currentEnabled,
                    "lastFailureReason": "",
                    "updatedAt": 1775038530000
                  }
                }
                """.trimIndent()
            )
        }

        val service = EmailSettingsService(
            httpClient = client,
            tokenProvider = { EmailSettingsApiResult.Success("admin-token") }
        )

        // Enable EmailJS
        val enableRes = service.updateEmailSettings(true)
        assertTrue(enableRes is EmailSettingsApiResult.Success)
        val enabledSettings = (enableRes as EmailSettingsApiResult.Success).data
        assertTrue(enabledSettings.customEmailEnabled)
        assertEquals("ENABLED", enabledSettings.emailjsStatus)
        assertEquals("emailjs", enabledSettings.activeProvider)
        assertTrue(capturedBodies[0].contains("\"customEmailEnabled\":true"))

        // Disable EmailJS
        val disableRes = service.updateEmailSettings(false)
        assertTrue(disableRes is EmailSettingsApiResult.Success)
        val disabledSettings = (disableRes as EmailSettingsApiResult.Success).data
        assertFalse(disabledSettings.customEmailEnabled)
        assertEquals("DISABLED", disabledSettings.emailjsStatus)
        assertEquals("firebase", disabledSettings.activeProvider)
        assertTrue(capturedBodies[1].contains("\"customEmailEnabled\":false"))
    }

    @Test
    fun `maps HTTP 401 403 and network errors accurately`() = runTest {
        val unauthorizedClient = createMockClient { req, _ ->
            jsonResponse(req, 401, """{"error":"Invalid ID token"}""")
        }
        val forbiddenClient = createMockClient { req, _ ->
            jsonResponse(req, 403, """{"error":"Forbidden"}""")
        }
        val networkErrorClient = OkHttpClient.Builder()
            .addInterceptor { throw IOException("Connection timed out") }
            .build()

        val res401 = EmailSettingsService(
            httpClient = unauthorizedClient,
            tokenProvider = { EmailSettingsApiResult.Success("bad-token") }
        ).fetchEmailSettings()
        assertTrue(res401 is EmailSettingsApiResult.Error)
        assertEquals(401, (res401 as EmailSettingsApiResult.Error).httpCode)
        assertTrue(res401.message.startsWith("Administrator authentication is missing or invalid."))

        val res403 = EmailSettingsService(
            httpClient = forbiddenClient,
            tokenProvider = { EmailSettingsApiResult.Success("non-admin-token") }
        ).updateEmailSettings(true)
        assertTrue(res403 is EmailSettingsApiResult.Error)
        assertEquals(403, (res403 as EmailSettingsApiResult.Error).httpCode)
        assertTrue(res403.message.startsWith("The signed-in account is not authorized."))

        val resNet = EmailSettingsService(
            httpClient = networkErrorClient,
            tokenProvider = { EmailSettingsApiResult.Success("admin-token") }
        ).fetchEmailSettings()
        assertTrue(resNet is EmailSettingsApiResult.Error)
        assertTrue(resNet.let { (it as EmailSettingsApiResult.Error).isNetworkError })
        assertEquals("Unable to connect to the server. Try again.", (resNet as EmailSettingsApiResult.Error).message)
    }

    @Test
    fun `EmailDeliverySettingsSection displays fields prevents duplicate clicks and triggers actions`() {
        var refreshCount = 0
        var disableClicks = 0

        composeRule.setContent {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                EmailDeliverySettingsSection(
                    uiState = EmailDeliveryUiState(
                        isLoading = false,
                        isUpdating = false,
                        settings = EmailDeliverySettings(
                            activeProvider = "emailjs",
                            emailjsStatus = "ENABLED",
                            customEmailEnabled = true,
                            lastFailureReason = null,
                            lastUpdatedTime = "01 Apr 2026, 03:45:30 PM IST"
                        ),
                        firebaseAuthEmail = "ullassuvarna65@gmail.com",
                        isFirebaseEmailVerified = true,
                        isFirebaseSignedIn = true
                    ),
                    onRefresh = { refreshCount++ },
                    onEnableEmailJs = {},
                    onDisableEmailJs = { disableClicks++ },
                    onDismissFeedback = {}
                )
            }
        }

        // LaunchedEffect(Unit) automatically triggers initial refresh
        assertEquals(1, refreshCount)

        composeRule.onNodeWithTag("emailjs_status_value").assertTextEquals("ENABLED")
        composeRule.onNodeWithTag("active_provider_value").assertTextEquals("emailjs")
        composeRule.onNodeWithTag("custom_email_enabled_value").assertTextEquals("true")
        composeRule.onNodeWithTag("last_failure_reason_value").assertTextEquals("None")
        composeRule.onNodeWithTag("last_updated_time_value").assertTextEquals("01 Apr 2026, 03:45:30 PM IST")

        // Enable button disabled when already ENABLED; Disable & Refresh enabled
        composeRule.onNodeWithTag("enable_emailjs_button").performScrollTo().assertIsNotEnabled()
        composeRule.onNodeWithTag("disable_emailjs_button").performScrollTo().assertIsEnabled()
        composeRule.onNodeWithTag("refresh_email_status_button").performScrollTo().assertIsEnabled()

        composeRule.onNodeWithTag("disable_emailjs_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(1, disableClicks)

        composeRule.onNodeWithText("Email Delivery Settings").assertExists()
    }

    @Test
    fun `EmailDeliverySettingsSection disables all action buttons during API request`() {
        composeRule.setContent {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                EmailDeliverySettingsSection(
                    uiState = EmailDeliveryUiState(
                        isLoading = false,
                        isUpdating = true,
                        settings = EmailDeliverySettings(
                            activeProvider = "firebase",
                            emailjsStatus = "DISABLED",
                            customEmailEnabled = false,
                            lastFailureReason = "SMTP timeout",
                            lastUpdatedTime = "01 Apr 2026, 04:00:00 PM IST"
                        ),
                        errorMessage = "Temporary warning"
                    ),
                    onRefresh = {},
                    onEnableEmailJs = {},
                    onDisableEmailJs = {},
                    onDismissFeedback = {}
                )
            }
        }

        composeRule.onNodeWithTag("emailjs_status_value").assertTextEquals("DISABLED")
        composeRule.onNodeWithTag("active_provider_value").assertTextEquals("firebase")
        composeRule.onNodeWithTag("custom_email_enabled_value").assertTextEquals("false")
        composeRule.onNodeWithTag("last_failure_reason_value").assertTextEquals("SMTP timeout")

        composeRule.onNodeWithTag("enable_emailjs_button").performScrollTo().assertIsNotEnabled()
        composeRule.onNodeWithTag("disable_emailjs_button").performScrollTo().assertIsNotEnabled()
        composeRule.onNodeWithTag("refresh_email_status_button").performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun `fetch and update customVerificationEmailEnabled remain independent from customEmailEnabled`() = runTest {
        val capturedPosts = mutableListOf<String>()
        var currentCustomEmail = true
        var currentCustomVerification = false

        val client = createMockClient { request, body ->
            if (request.method == "POST") {
                val payload = body.orEmpty()
                capturedPosts.add(payload)
                if (payload.contains("customVerificationEmailEnabled")) {
                    currentCustomVerification = payload.contains("true")
                }
                if (payload.contains("\"customEmailEnabled\"")) {
                    currentCustomEmail = payload.contains("true")
                }
            }
            jsonResponse(
                request,
                200,
                """
                {
                  "ok": true,
                  "settings": {
                    "activeProvider": "emailjs",
                    "emailjsStatus": "ENABLED",
                    "customEmailEnabled": $currentCustomEmail,
                    "customVerificationEmailEnabled": $currentCustomVerification,
                    "lastFailureReason": null,
                    "updatedAt": 1775038530000
                  }
                }
                """.trimIndent()
            )
        }

        val service = EmailSettingsService(
            httpClient = client,
            tokenProvider = { EmailSettingsApiResult.Success("admin-token") }
        )

        val initial = service.fetchEmailSettings() as EmailSettingsApiResult.Success
        assertTrue(initial.data.customEmailEnabled)
        assertFalse(initial.data.customVerificationEmailEnabled)

        val enabledRes = service.updateCustomVerificationEmailSetting(true) as EmailSettingsApiResult.Success
        assertTrue(enabledRes.data.customVerificationEmailEnabled)
        assertTrue(enabledRes.data.customEmailEnabled)
        assertTrue(capturedPosts[0].contains("\"customVerificationEmailEnabled\":true"))
        assertFalse(capturedPosts[0].contains("customEmailEnabled"))

        val disabledRes = service.updateCustomVerificationEmailSetting(false) as EmailSettingsApiResult.Success
        assertFalse(disabledRes.data.customVerificationEmailEnabled)
        assertTrue(disabledRes.data.customEmailEnabled)
        assertTrue(capturedPosts[1].contains("\"customVerificationEmailEnabled\":false"))
        assertFalse(capturedPosts[1].contains("customEmailEnabled"))
    }

    @Test
    fun `EmailDeliverySettingsSection renders and triggers Custom Email Verification buttons independently`() {
        var enableVerificationClicks = 0

        composeRule.setContent {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                EmailDeliverySettingsSection(
                    uiState = EmailDeliveryUiState(
                        isLoading = false,
                        isUpdating = false,
                        settings = EmailDeliverySettings(
                            activeProvider = "emailjs",
                            emailjsStatus = "ENABLED",
                            customEmailEnabled = true,
                            customVerificationEmailEnabled = false,
                            lastFailureReason = null,
                            lastUpdatedTime = "01 Apr 2026, 04:15:00 PM IST"
                        ),
                        firebaseAuthEmail = "ullassuvarna65@gmail.com",
                        isFirebaseEmailVerified = true,
                        isFirebaseSignedIn = true
                    ),
                    onRefresh = {},
                    onEnableEmailJs = {},
                    onDisableEmailJs = {},
                    onEnableCustomVerification = { enableVerificationClicks++ },
                    onDisableCustomVerification = {},
                    onDismissFeedback = {}
                )
            }
        }

        composeRule.onNodeWithTag("custom_verification_enabled_value").performScrollTo().assertTextEquals("false")
        composeRule.onNodeWithTag("custom_verification_status_badge").performScrollTo().assertExists()
        composeRule.onNodeWithTag("enable_custom_verification_button").performScrollTo().assertIsEnabled()
        composeRule.onNodeWithTag("disable_custom_verification_button").performScrollTo().assertIsNotEnabled()

        composeRule.onNodeWithTag("enable_custom_verification_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(1, enableVerificationClicks)
    }
}
