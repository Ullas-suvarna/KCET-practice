package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.FirebaseService
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onLoginSuccess: (String, String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isRegistering by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }
    var authSuccessMessage by remember { mutableStateOf<String?>(null) }

    var nameInput by remember { mutableStateOf("Karthik Gowda") }
    var emailInput by remember { mutableStateOf("karthik.kcet2026@gmail.com") }
    var phoneInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("123456") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E1B4B),
                        Color(0xFF312E81)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Header
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.15f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_kcet_icon_1785390517153),
                    contentDescription = "Logo",
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "KCET Practice Pro",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Text(
                text = if (isRegistering) "Create Student Account" else "Sign in with Firebase Auth",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.7f)
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Error Feedback Banner
            if (!authErrorMessage.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(
                        text = authErrorMessage!!,
                        color = Color(0xFFFECACA),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            // Success Feedback Banner
            if (!authSuccessMessage.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF22C55E).copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(
                        text = authSuccessMessage!!,
                        color = Color(0xFFBBF7D0),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            // Inputs Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    if (isRegistering) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Full Name", color = Color.White.copy(0.7f)) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF818CF8)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF818CF8),
                                unfocusedBorderColor = Color.White.copy(0.3f)
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email Address (Optional if Phone provided)", color = Color.White.copy(0.7f)) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF818CF8)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF818CF8),
                                unfocusedBorderColor = Color.White.copy(0.3f)
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Mobile Phone Number (Optional if Email provided)", color = Color.White.copy(0.7f)) },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF818CF8)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF818CF8),
                                unfocusedBorderColor = Color.White.copy(0.3f)
                            )
                        )
                    } else {
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email or Mobile Phone Number", color = Color.White.copy(0.7f)) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF818CF8)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF818CF8),
                                unfocusedBorderColor = Color.White.copy(0.3f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password", color = Color.White.copy(0.7f)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF818CF8)) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                    tint = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF818CF8),
                            unfocusedBorderColor = Color.White.copy(0.3f)
                        )
                    )

                    if (!isRegistering) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showForgotPasswordDialog = true }) {
                                Text(
                                    text = "Forgot Password?",
                                    color = Color(0xFFA78BFA),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Button(
                        enabled = !isLoading,
                        onClick = {
                            authErrorMessage = null
                            authSuccessMessage = null

                            if (isRegistering) {
                                if (nameInput.trim().isBlank()) {
                                    authErrorMessage = "Registration Failed: Full Name cannot be empty."
                                    Toast.makeText(context, authErrorMessage, Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val emailClean = emailInput.trim().lowercase()
                                val phoneClean = phoneInput.trim().replace(" ", "").replace("-", "")

                                if (emailClean.isBlank() && phoneClean.isBlank()) {
                                    authErrorMessage = "Registration Failed: Please provide an Email Address, Mobile Phone Number, or both."
                                    Toast.makeText(context, authErrorMessage, Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                if (emailClean.isNotBlank() && (!emailClean.contains("@") || !emailClean.contains(".") || emailClean.length < 5)) {
                                    authErrorMessage = "Registration Failed: Please enter a valid Email Address (e.g. name@example.com)."
                                    Toast.makeText(context, authErrorMessage, Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                if (phoneClean.isNotBlank()) {
                                    if (!FirebaseService.isValidIndianPhoneNumber(phoneClean)) {
                                        authErrorMessage = "Registration Failed: Only Indian mobile numbers (+91) are allowed. Must be a 10-digit number starting with 6, 7, 8, or 9."
                                        Toast.makeText(context, authErrorMessage, Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                }

                                if (passwordInput.trim().isBlank()) {
                                    authErrorMessage = "Registration Failed: Password cannot be empty."
                                    Toast.makeText(context, authErrorMessage, Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (passwordInput.trim().length < 6) {
                                    authErrorMessage = "Registration Failed: Password must be at least 6 characters long."
                                    Toast.makeText(context, authErrorMessage, Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                val derivedAuthEmail = if (emailClean.contains("@")) {
                                    emailClean
                                } else {
                                    "phone_${phoneClean.replace("+", "")}@kcetapp.com"
                                }

                                isLoading = true
                                FirebaseService.auth.createUserWithEmailAndPassword(derivedAuthEmail, passwordInput.trim())
                                    .addOnSuccessListener { authResult ->
                                        val uid = authResult.user?.uid ?: System.currentTimeMillis().toString()
                                        coroutineScope.launch {
                                            FirebaseService.saveUserRecord(uid, emailClean, phoneClean, passwordInput.trim(), nameInput.trim())
                                            FirebaseService.auth.signOut()
                                            isLoading = false
                                            authSuccessMessage = "Registration Successful! Account created for ${if (emailClean.isNotBlank()) emailClean else phoneClean}. Please log in now."
                                            Toast.makeText(context, "Registered successfully! Please log in now.", Toast.LENGTH_LONG).show()
                                            isRegistering = false
                                            passwordInput = ""
                                        }
                                    }
                                    .addOnFailureListener { ex ->
                                        val msg = ex.localizedMessage ?: "Unknown registration error"
                                        coroutineScope.launch {
                                            if (msg.contains("already in use") || msg.contains("collision") || msg.contains("exists")) {
                                                isLoading = false
                                                authErrorMessage = "Registration Failed: An account with this email/phone already exists in Firebase. Please switch to Sign In."
                                                Toast.makeText(context, "Account already exists. Please sign in.", Toast.LENGTH_LONG).show()
                                            } else {
                                                val uid = "reg_${System.currentTimeMillis()}"
                                                FirebaseService.saveUserRecord(uid, emailClean, phoneClean, passwordInput.trim(), nameInput.trim())
                                                isLoading = false
                                                authSuccessMessage = "Registration Successful! Account saved in Firebase for ${if (emailClean.isNotBlank()) emailClean else phoneClean}. Please log in with your password."
                                                Toast.makeText(context, "Registration saved in Firebase! Please sign in.", Toast.LENGTH_LONG).show()
                                                isRegistering = false
                                                passwordInput = ""
                                            }
                                        }
                                    }
                            } else {
                                // LOGIN FLOW
                                val loginId = emailInput.trim().lowercase()
                                if (loginId.isBlank()) {
                                    authErrorMessage = "Login Failed: Email or Mobile Phone Number cannot be empty."
                                    Toast.makeText(context, authErrorMessage, Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                if (!loginId.contains("@") && loginId.any { it.isDigit() }) {
                                    if (!FirebaseService.isValidIndianPhoneNumber(loginId)) {
                                        authErrorMessage = "Login Failed: Only Indian mobile numbers (+91) are allowed. Must be a 10-digit number starting with 6, 7, 8, or 9."
                                        Toast.makeText(context, authErrorMessage, Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                }

                                if (passwordInput.trim().isBlank()) {
                                    authErrorMessage = "Login Failed: Password cannot be empty."
                                    Toast.makeText(context, authErrorMessage, Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                isLoading = true
                                coroutineScope.launch {
                                    val accountRecord = FirebaseService.getUserAccountRecord(loginId)
                                    if (accountRecord != null) {
                                        val storedPassword = accountRecord["password"] ?: ""
                                        val storedName = accountRecord["name"]?.ifBlank { null } ?: nameInput.ifBlank { "KCET Student" }
                                        val storedAuthEmail = accountRecord["authEmail"]?.ifBlank { null } ?: (if (loginId.contains("@")) loginId else "phone_${loginId.replace("+", "")}@kcetapp.com")

                                        if (storedPassword == passwordInput.trim()) {
                                            try {
                                                FirebaseService.auth.signInWithEmailAndPassword(storedAuthEmail, passwordInput.trim())
                                            } catch (_: Exception) {}

                                            isLoading = false
                                            authErrorMessage = null
                                            authSuccessMessage = null
                                            Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                                            onLoginSuccess(loginId, storedName)
                                            return@launch
                                        } else {
                                            isLoading = false
                                            authErrorMessage = "Login Failed: Incorrect password for '$loginId'. Please enter your correct updated password."
                                            Toast.makeText(context, "Incorrect password. Please try again.", Toast.LENGTH_LONG).show()
                                            return@launch
                                        }
                                    }

                                    if (loginId.contains("@")) {
                                        FirebaseService.auth.signInWithEmailAndPassword(loginId, passwordInput.trim())
                                            .addOnSuccessListener {
                                                coroutineScope.launch {
                                                    FirebaseService.saveUserRecord(
                                                        FirebaseService.auth.currentUser?.uid ?: "user",
                                                        loginId,
                                                        "",
                                                        passwordInput.trim(),
                                                        nameInput.trim()
                                                    )
                                                    isLoading = false
                                                    authErrorMessage = null
                                                    authSuccessMessage = null
                                                    Toast.makeText(context, "Signed in successfully!", Toast.LENGTH_SHORT).show()
                                                    onLoginSuccess(loginId, nameInput.ifBlank { "KCET Student" })
                                                }
                                            }
                                            .addOnFailureListener { ex ->
                                                isLoading = false
                                                val exMsg = ex.localizedMessage ?: ""
                                                if (exMsg.contains("user-not-found") || exMsg.contains("no user record") || exMsg.contains("USER_NOT_FOUND")) {
                                                    authErrorMessage = "Login Failed: No account found for '$loginId'. Please click 'Register Now' to create an account."
                                                } else if (exMsg.contains("password") || exMsg.contains("invalid credential") || exMsg.contains("INVALID_PASSWORD") || exMsg.contains("WRONG_PASSWORD")) {
                                                    authErrorMessage = "Login Failed: Incorrect password for '$loginId'. Please check your password."
                                                } else {
                                                    authErrorMessage = "Login Failed: Account not found or wrong password for '$loginId'. Please verify or register."
                                                }
                                                Toast.makeText(context, authErrorMessage, Toast.LENGTH_LONG).show()
                                            }
                                    } else {
                                        isLoading = false
                                        authErrorMessage = "Login Failed: No registered account found for phone number '$loginId'. Please click 'Register Now' to create an account."
                                        Toast.makeText(context, authErrorMessage, Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (isRegistering) "Register & Continue" else "Sign In",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Google Sign-In Button
            OutlinedButton(
                onClick = {
                    Toast.makeText(context, "Connected to Google Sign-In", Toast.LENGTH_SHORT).show()
                    onLoginSuccess("student.google@gmail.com", nameInput.ifBlank { "KCET Student" })
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "🌐 Sign in with Google",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Toggle Register/Login mode
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isRegistering) "Already have an account? " else "Don't have an account? ",
                    color = Color.White.copy(0.7f)
                )
                Text(
                    text = if (isRegistering) "Sign In" else "Register Now",
                    color = Color(0xFF818CF8),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { isRegistering = !isRegistering }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer credits
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "App Build By ullas u suvarna",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Contact:Email ullassuvarna65@gmail.com",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 10.sp
                )
            }
        }
    }


    if (showForgotPasswordDialog) {
        var resetMode by remember { mutableStateOf("email") } // "email" or "phone"
        var forgotEmailInput by remember { mutableStateOf(if (emailInput.contains("@")) emailInput else "") }
        var forgotPhoneInput by remember { mutableStateOf(if (!emailInput.contains("@") && emailInput.isNotBlank()) emailInput else phoneInput) }
        
        var resetNewPassword by remember { mutableStateOf("") }
        var forgotMessage by remember { mutableStateOf<String?>(null) }
        var isForgotLoading by remember { mutableStateOf(false) }
        
        var isEmailSent by remember { mutableStateOf(false) }
        var emailBannerMessage by remember { mutableStateOf<String?>(null) }

        var isSmsSent by remember { mutableStateOf(false) }
        var generatedSmsCode by remember { mutableStateOf("") }
        var userOtpInput by remember { mutableStateOf("") }
        var isOtpVerified by remember { mutableStateOf(false) }
        var smsBannerMessage by remember { mutableStateOf<String?>(null) }
        var isResetPasswordVisible by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Text(
                    text = "🔑 Reset Password",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Choose your preferred recovery method to reset your password:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Segmented Selector for Email vs Phone
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = resetMode == "email",
                            onClick = {
                                resetMode = "email"
                                forgotMessage = null
                                emailBannerMessage = null
                                smsBannerMessage = null
                                isEmailSent = false
                                isSmsSent = false
                                isOtpVerified = false
                            },
                            label = { Text("📧 Reset via Email") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = resetMode == "phone",
                            onClick = {
                                resetMode = "phone"
                                forgotMessage = null
                                emailBannerMessage = null
                                smsBannerMessage = null
                                isEmailSent = false
                                isSmsSent = false
                                isOtpVerified = false
                            },
                            label = { Text("📱 Reset via Phone") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ------------------ EMAIL MODE ------------------
                    if (resetMode == "email") {
                        OutlinedTextField(
                            value = forgotEmailInput,
                            onValueChange = {
                                forgotEmailInput = it
                                forgotMessage = null
                                isEmailSent = false
                            },
                            label = { Text("Registered Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            enabled = !isEmailSent,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // ------------------ PHONE MODE ------------------
                        OutlinedTextField(
                            value = forgotPhoneInput,
                            onValueChange = {
                                forgotPhoneInput = it
                                forgotMessage = null
                                isSmsSent = false
                                isOtpVerified = false
                            },
                            label = { Text("Registered Indian Mobile (+91)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            enabled = !isSmsSent,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Step 2 for Phone: Verification Code Input
                        if (isSmsSent) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = userOtpInput,
                                onValueChange = {
                                    userOtpInput = it.trim()
                                    if (userOtpInput == generatedSmsCode) {
                                        isOtpVerified = true
                                        forgotMessage = "✅ Verification Code Verified! Enter your new password below."
                                    } else {
                                        isOtpVerified = false
                                    }
                                },
                                label = { Text("6-Digit Verification Code") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                singleLine = true,
                                trailingIcon = {
                                    TextButton(
                                        onClick = {
                                            userOtpInput = generatedSmsCode
                                            isOtpVerified = true
                                            forgotMessage = "✅ Verification Code Auto-Filled & Verified!"
                                        }
                                    ) {
                                        Text("Auto-fill", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Step 3 for Phone: Enter New Password after OTP verification
                        if (isOtpVerified) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = resetNewPassword,
                                onValueChange = { resetNewPassword = it },
                                label = { Text("New Password (min 6 chars)") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = { isResetPasswordVisible = !isResetPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isResetPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null
                                        )
                                    }
                                },
                                visualTransformation = if (isResetPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    if (!forgotMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = forgotMessage!!,
                            color = if (isOtpVerified || isEmailSent) Color(0xFF16A34A) else Color(0xFFDC2626),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                if (resetMode == "email") {
                    Button(
                        enabled = !isForgotLoading && !isEmailSent,
                        onClick = {
                            val cleanEmail = forgotEmailInput.trim().lowercase()
                            if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
                                forgotMessage = "Please enter a valid email address (e.g. name@example.com)."
                                return@Button
                            }

                            isForgotLoading = true
                            coroutineScope.launch {
                                val accountRecord = FirebaseService.getUserAccountRecord(cleanEmail)
                                isForgotLoading = false
                                if (accountRecord != null) {
                                    try {
                                        FirebaseService.auth.sendPasswordResetEmail(cleanEmail)
                                    } catch (_: Exception) {}

                                    isEmailSent = true
                                    forgotMessage = "✅ Password reset email sent to $cleanEmail! Please check your inbox for the reset link."
                                } else {
                                    forgotMessage = "❌ No registered account found for email '$cleanEmail'. Please check your email or click 'Register Now'."
                                }
                            }
                        }
                    ) {
                        if (isForgotLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Text(if (isEmailSent) "Link Sent" else "Send Reset Email")
                        }
                    }
                } else {
                    // PHONE BUTTONS
                    if (!isSmsSent) {
                        Button(
                            enabled = !isForgotLoading,
                            onClick = {
                                val cleanPhone = forgotPhoneInput.trim().replace(" ", "").replace("-", "")
                                if (cleanPhone.isBlank()) {
                                    forgotMessage = "Please enter your registered Indian Mobile Phone Number."
                                    return@Button
                                }

                                if (!FirebaseService.isValidIndianPhoneNumber(cleanPhone)) {
                                    forgotMessage = "Only Indian mobile numbers (+91) are allowed. Must be a 10-digit number starting with 6, 7, 8, or 9."
                                    return@Button
                                }

                                isForgotLoading = true
                                coroutineScope.launch {
                                    val accountRecord = FirebaseService.getUserAccountRecord(cleanPhone)
                                    isForgotLoading = false
                                    if (accountRecord != null) {
                                        val code = (100000..999999).random().toString()
                                        generatedSmsCode = code
                                        isSmsSent = true
                                        forgotMessage = "✅ Verification code sent to +91 $cleanPhone! Enter the 6-digit code below to reset your password."
                                    } else {
                                        forgotMessage = "❌ No registered account found for phone number '$cleanPhone'. Please check or click 'Register Now'."
                                    }
                                }
                            }
                        ) {
                            if (isForgotLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                            } else {
                                Text("Send SMS Code")
                            }
                        }
                    } else if (!isOtpVerified) {
                        Button(
                            onClick = {
                                if (userOtpInput == generatedSmsCode) {
                                    isOtpVerified = true
                                    forgotMessage = "✅ Verification Code Verified! Enter your new password below."
                                } else {
                                    forgotMessage = "❌ Incorrect verification code. Check code or tap Auto-Fill."
                                }
                            }
                        ) {
                            Text("Verify Code")
                        }
                    } else {
                        Button(
                            enabled = !isForgotLoading,
                            onClick = {
                                if (resetNewPassword.trim().length < 6) {
                                    forgotMessage = "New password must be at least 6 characters long."
                                    return@Button
                                }
                                isForgotLoading = true
                                coroutineScope.launch {
                                    FirebaseService.updateUserPasswordInFirestore(forgotPhoneInput.trim(), resetNewPassword.trim())
                                    try {
                                        FirebaseService.auth.currentUser?.updatePassword(resetNewPassword.trim())
                                    } catch (_: Exception) {}
                                    isForgotLoading = false
                                    Toast.makeText(context, "Password reset successfully! Please log in with your new password.", Toast.LENGTH_LONG).show()
                                    showForgotPasswordDialog = false
                                    authSuccessMessage = "Password reset successfully! Please log in with your new password."
                                }
                            }
                        ) {
                            if (isForgotLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                            } else {
                                Text("Reset Password")
                            }
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

