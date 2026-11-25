package com.example

import com.example.data.EmailVerificationBackendResult
import com.example.data.EmailVerificationService
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
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EmailVerificationServiceTest {

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
    fun `sendVerificationEmailWithToken posts Bearer token and parses EmailJS custom success`() = runTest {
        var capturedMethod = ""
        var capturedUrl = ""
        var capturedAuth = ""
        var capturedContentType = ""
        var capturedBody = ""

        val client = createMockClient { request, body ->
            capturedMethod = request.method
            capturedUrl = request.url.toString()
            capturedAuth = request.header("Authorization").orEmpty()
            capturedContentType = request.header("Content-Type").orEmpty()
            capturedBody = body.orEmpty()
            jsonResponse(request, 200, """{"success":true,"provider":"emailjs"}""")
        }

        val service = EmailVerificationService(
            endpointUrl = EmailVerificationService.DEFAULT_VERIFY_EMAIL_URL,
            httpClient = client
        )

        val result = service.sendVerificationEmailWithToken(idToken = "fresh-firebase-id-token")
        assertTrue(result is EmailVerificationBackendResult.CustomEmailSent)
        val sent = result as EmailVerificationBackendResult.CustomEmailSent
        assertEquals("emailjs", sent.provider)
        assertEquals("Verification email sent successfully. Please check your inbox.", sent.message)

        assertEquals("POST", capturedMethod)
        assertEquals("https://kcet-password-reset.vercel.app/api/send-verification-email", capturedUrl)
        assertEquals("Bearer fresh-firebase-id-token", capturedAuth)
        assertTrue(capturedContentType.contains("application/json"))
        assertEquals("{}", capturedBody)
    }

    @Test
    fun `parses fallbackToFirebase response cleanly`() = runTest {
        val client = createMockClient { req, _ ->
            jsonResponse(req, 200, """{"fallbackToFirebase":true,"provider":"firebase"}""")
        }

        val result = EmailVerificationService(httpClient = client)
            .sendVerificationEmailWithToken("token-123")

        assertTrue(result is EmailVerificationBackendResult.FirebaseFallback)
        assertEquals("firebase", (result as EmailVerificationBackendResult.FirebaseFallback).provider)
    }

    @Test
    fun `parses deliveryUncertain response without triggering fallback`() = runTest {
        val client = createMockClient { req, _ ->
            jsonResponse(req, 200, """{"deliveryUncertain":true}""")
        }

        val result = EmailVerificationService(httpClient = client)
            .sendVerificationEmailWithToken("token-123")

        assertTrue(result is EmailVerificationBackendResult.DeliveryUncertain)
        assertEquals(
            "We could not confirm email delivery. Please check your inbox and spam folder before trying again.",
            (result as EmailVerificationBackendResult.DeliveryUncertain).message
        )
    }

    @Test
    fun `parses alreadyVerified response cleanly and short circuits when already verified locally`() = runTest {
        var networkCalled = false
        val client = createMockClient { req, _ ->
            networkCalled = true
            jsonResponse(req, 200, """{"success":true,"alreadyVerified":true}""")
        }

        val service = EmailVerificationService(httpClient = client)

        val localAlreadyVerified = service.sendVerificationEmailWithToken("token-123", isAlreadyVerified = true)
        assertTrue(localAlreadyVerified is EmailVerificationBackendResult.AlreadyVerified)
        assertEquals("Your email is already verified.", (localAlreadyVerified as EmailVerificationBackendResult.AlreadyVerified).message)
        assertFalse(networkCalled)

        val backendAlreadyVerified = service.sendVerificationEmailWithToken("token-123", isAlreadyVerified = false)
        assertTrue(networkCalled)
        assertTrue(backendAlreadyVerified is EmailVerificationBackendResult.AlreadyVerified)
        assertEquals("Your email is already verified.", (backendAlreadyVerified as EmailVerificationBackendResult.AlreadyVerified).message)
    }

    @Test
    fun `rejects blank token maps HTTP and unknown 2xx responses without falling back`() = runTest {
        var calledWithBlankToken = false
        val blankClient = createMockClient { req, _ ->
            calledWithBlankToken = true
            jsonResponse(req, 200, """{"success":true}""")
        }
        val blankResult = EmailVerificationService(httpClient = blankClient)
            .sendVerificationEmailWithToken("")
        assertFalse(calledWithBlankToken)
        assertTrue(blankResult is EmailVerificationBackendResult.Error)
        assertTrue((blankResult as EmailVerificationBackendResult.Error).isAuthError)
        assertEquals(401, blankResult.httpCode)

        val err401 = EmailVerificationService.parseBackendResponse(401, """{"error":"Token expired"}""")
        assertTrue(err401 is EmailVerificationBackendResult.Error)
        assertTrue((err401 as EmailVerificationBackendResult.Error).isAuthError)
        assertEquals(401, err401.httpCode)

        val err403 = EmailVerificationService.parseBackendResponse(403, """{"error":"Forbidden"}""")
        assertTrue(err403 is EmailVerificationBackendResult.Error)
        assertTrue((err403 as EmailVerificationBackendResult.Error).isAuthError)
        assertEquals(403, err403.httpCode)

        val err429 = EmailVerificationService.parseBackendResponse(429, "")
        assertTrue(err429 is EmailVerificationBackendResult.Error)
        assertEquals(429, (err429 as EmailVerificationBackendResult.Error).httpCode)

        val err500 = EmailVerificationService.parseBackendResponse(500, "<html>Internal Server Error</html>")
        assertTrue(err500 is EmailVerificationBackendResult.Error)
        assertEquals(500, (err500 as EmailVerificationBackendResult.Error).httpCode)

        val invalidJson200 = EmailVerificationService.parseBackendResponse(200, "not json")
        assertTrue(invalidJson200 is EmailVerificationBackendResult.Error)
        assertTrue((invalidJson200 as EmailVerificationBackendResult.Error).message.contains("Invalid response"))

        val unknownJson200 = EmailVerificationService.parseBackendResponse(200, """{"status":"pending"}""")
        assertTrue(unknownJson200 is EmailVerificationBackendResult.Error)
        assertTrue((unknownJson200 as EmailVerificationBackendResult.Error).message.contains("Unexpected verification server response"))
    }

    @Test
    fun `preserves sanitized diagnostic details across network IOException types`() = runTest {
        val timeoutClient = OkHttpClient.Builder()
            .addInterceptor { throw SocketTimeoutException("Read timed out") }
            .build()
        val dnsClient = OkHttpClient.Builder()
            .addInterceptor { throw UnknownHostException("Unable to resolve host") }
            .build()
        val sslClient = OkHttpClient.Builder()
            .addInterceptor { throw SSLHandshakeException("Handshake aborted") }
            .build()
        val ioClient = OkHttpClient.Builder()
            .addInterceptor { throw IOException("Network unreachable") }
            .build()

        val timeoutErr = EmailVerificationService(httpClient = timeoutClient)
            .sendVerificationEmailWithToken("valid-token") as EmailVerificationBackendResult.Error
        assertTrue(timeoutErr.isNetworkError)
        assertTrue(timeoutErr.message.contains("timed out"))
        assertTrue(timeoutErr.message.contains("Read timed out"))

        val dnsErr = EmailVerificationService(httpClient = dnsClient)
            .sendVerificationEmailWithToken("valid-token") as EmailVerificationBackendResult.Error
        assertTrue(dnsErr.isNetworkError)
        assertTrue(dnsErr.message.contains("DNS lookup failed"))

        val sslErr = EmailVerificationService(httpClient = sslClient)
            .sendVerificationEmailWithToken("valid-token") as EmailVerificationBackendResult.Error
        assertTrue(sslErr.isNetworkError)
        assertTrue(sslErr.message.contains("SSL/TLS connection failed"))

        val ioErr = EmailVerificationService(httpClient = ioClient)
            .sendVerificationEmailWithToken("valid-token") as EmailVerificationBackendResult.Error
        assertTrue(ioErr.isNetworkError)
        assertTrue(ioErr.message.contains("IOException"))
    }

    @Test
    fun `sanitizeDiagnosticText redacts tokens action codes and passwords`() {
        val raw = "Failed with Authorization: Bearer eyJhbGciOi.abc.xyz oobCode=secretActionCode123 password=MySecret123"
        val sanitized = EmailVerificationService.sanitizeDiagnosticText(raw)
        assertFalse(sanitized.contains("eyJhbGciOi"))
        assertFalse(sanitized.contains("secretActionCode123"))
        assertFalse(sanitized.contains("MySecret123"))
        assertTrue(sanitized.contains("[REDACTED"))
    }

    @Test
    fun `resolveBackendResultWithFallback handles fallback on errors without duplicating deliveryUncertain`() = runTest {
        var fallbackCalls = 0

        val customOutcome = com.example.data.FirebaseService.resolveBackendResultWithFallback(
            result = EmailVerificationBackendResult.CustomEmailSent(EmailVerificationService.MSG_CUSTOM_VERIFICATION_SENT),
            targetEmail = "student@example.com",
            fallbackSender = {
                fallbackCalls++
                Result.success(Unit)
            }
        )
        assertTrue(customOutcome is com.example.data.FirebaseService.VerificationDispatchOutcome.CustomSent)
        assertEquals(0, fallbackCalls)

        val uncertainOutcome = com.example.data.FirebaseService.resolveBackendResultWithFallback(
            result = EmailVerificationBackendResult.DeliveryUncertain(EmailVerificationService.MSG_DELIVERY_UNCERTAIN),
            targetEmail = "student@example.com",
            fallbackSender = {
                fallbackCalls++
                Result.success(Unit)
            }
        )
        assertTrue(uncertainOutcome is com.example.data.FirebaseService.VerificationDispatchOutcome.DeliveryUncertain)
        assertEquals(0, fallbackCalls)

        val explicitFallbackOutcome = com.example.data.FirebaseService.resolveBackendResultWithFallback(
            result = EmailVerificationBackendResult.FirebaseFallback(),
            targetEmail = "student@example.com",
            fallbackSender = {
                fallbackCalls++
                Result.success(Unit)
            }
        )
        assertTrue(explicitFallbackOutcome is com.example.data.FirebaseService.VerificationDispatchOutcome.FallbackSent)
        assertEquals(1, fallbackCalls)

        val networkErrorFallbackOutcome = com.example.data.FirebaseService.resolveBackendResultWithFallback(
            result = EmailVerificationBackendResult.Error("Connection timed out", isNetworkError = true),
            targetEmail = "student@example.com",
            fallbackSender = {
                fallbackCalls++
                Result.success(Unit)
            }
        )
        assertTrue(networkErrorFallbackOutcome is com.example.data.FirebaseService.VerificationDispatchOutcome.FallbackSent)
        assertEquals(2, fallbackCalls)

        val bothFailedOutcome = com.example.data.FirebaseService.resolveBackendResultWithFallback(
            result = EmailVerificationBackendResult.Error("HTTP 500", httpCode = 500),
            targetEmail = "student@example.com",
            fallbackSender = {
                fallbackCalls++
                Result.failure(Exception("Firebase quota exceeded"))
            }
        )
        assertTrue(bothFailedOutcome is com.example.data.FirebaseService.VerificationDispatchOutcome.Error)
        assertEquals(3, fallbackCalls)
    }
}
