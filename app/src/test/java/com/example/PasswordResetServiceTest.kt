package com.example

import com.example.data.EmailValidationResult
import com.example.data.PasswordResetBackendResult
import com.example.data.PasswordResetService
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PasswordResetServiceTest {

    private fun createMockClient(
        handler: (Request, String?) -> Response
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

    private fun jsonResponse(request: Request, code: Int, json: String): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code in 200..299) "OK" else "Error")
            .body(json.toResponseBody("application/json".toMediaType()))
            .build()
    }

    @Test
    fun `validateEmail rejects empty and invalid formats and normalizes valid emails`() {
        assertTrue(PasswordResetService.validateEmail("   ") is EmailValidationResult.Invalid)
        assertTrue(PasswordResetService.validateEmail("student") is EmailValidationResult.Invalid)
        assertTrue(PasswordResetService.validateEmail("student@invalid") is EmailValidationResult.Invalid)

        val valid = PasswordResetService.validateEmail("  Student@Example.COM ")
        assertTrue(valid is EmailValidationResult.Valid)
        assertEquals("student@example.com", (valid as EmailValidationResult.Valid).normalizedEmail)
    }

    @Test
    fun `scenario A - EmailJS success posts JSON and returns friendly message`() = runTest {
        var capturedMethod = ""
        var capturedUrl = ""
        var capturedContentType = ""
        var capturedBody = ""

        val client = createMockClient { request, body ->
            capturedMethod = request.method
            capturedUrl = request.url.toString()
            capturedContentType = request.header("Content-Type").orEmpty()
            capturedBody = body.orEmpty()
            jsonResponse(
                request,
                200,
                """{"success":true,"provider":"emailjs"}"""
            )
        }

        val service = PasswordResetService(httpClient = client)
        val result = service.sendResetEmail("Student@Example.com")

        assertEquals("POST", capturedMethod)
        assertEquals("https://kcet-password-reset.vercel.app/api/send-reset-email", capturedUrl)
        assertTrue(capturedContentType.contains("application/json"))
        assertTrue(capturedBody.contains("\"email\":\"student@example.com\""))

        assertTrue(result is PasswordResetBackendResult.EmailJsSent)
        val sent = result as PasswordResetBackendResult.EmailJsSent
        assertEquals("emailjs", sent.provider)
        assertEquals(
            PasswordResetService.MSG_EMAILJS_SUCCESS,
            sent.message
        )
    }

    @Test
    fun `scenario B - fallbackToFirebase triggers FirebaseFallback result`() = runTest {
        val client = createMockClient { request, _ ->
            jsonResponse(
                request,
                200,
                """{"success":false,"fallbackToFirebase":true,"provider":"firebase"}"""
            )
        }

        val service = PasswordResetService(httpClient = client)
        val result = service.sendResetEmail("student@example.com")

        assertTrue(result is PasswordResetBackendResult.FirebaseFallback)
        assertEquals("firebase", (result as PasswordResetBackendResult.FirebaseFallback).provider)
    }

    @Test
    fun `scenario C - deliveryUncertain returns wait message without fallback`() = runTest {
        val client = createMockClient { request, _ ->
            jsonResponse(
                request,
                200,
                """{"success":false,"deliveryUncertain":true}"""
            )
        }

        val service = PasswordResetService(httpClient = client)
        val result = service.sendResetEmail("student@example.com")

        assertTrue(result is PasswordResetBackendResult.DeliveryUncertain)
        assertEquals(
            "Your request is being processed. Please wait a few minutes before requesting another reset email.",
            (result as PasswordResetBackendResult.DeliveryUncertain).message
        )
    }

    @Test
    fun `handles HTTP error and network failures cleanly`() = runTest {
        val httpErrorClient = createMockClient { request, _ ->
            jsonResponse(request, 429, """{"error":"Too many requests. Please wait."}""")
        }
        val networkClient = OkHttpClient.Builder()
            .addInterceptor { throw IOException("No network") }
            .build()

        val httpRes = PasswordResetService(httpClient = httpErrorClient)
            .sendResetEmail("student@example.com")
        assertTrue(httpRes is PasswordResetBackendResult.Error)
        assertEquals(429, (httpRes as PasswordResetBackendResult.Error).httpCode)
        assertEquals("Too many requests. Please wait.", httpRes.message)

        val netRes = PasswordResetService(httpClient = networkClient)
            .sendResetEmail("student@example.com")
        assertTrue(netRes is PasswordResetBackendResult.Error)
        assertTrue((netRes as PasswordResetBackendResult.Error).isNetworkError)
    }
}
