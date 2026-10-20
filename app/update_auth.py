import os

auth_screen_path = 'app/src/main/java/com/example/ui/screens/AuthScreen.kt'
with open(auth_screen_path, 'r', encoding='utf-8') as f:
    text = f.read()

# 1. Update Registration Logic
old_reg = """                                isLoading = true
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
                                    }"""

new_reg = """                                isLoading = true
                                FirebaseService.auth.createUserWithEmailAndPassword(derivedAuthEmail, passwordInput.trim())
                                    .addOnSuccessListener { authResult ->
                                        val createdUser = authResult.user
                                        val uid = createdUser?.uid ?: System.currentTimeMillis().toString()
                                        coroutineScope.launch {
                                            var verificationSent = false
                                            if (emailClean.contains("@") && createdUser != null) {
                                                try {
                                                    with(FirebaseService) {
                                                        val res = createdUser.sendEmailVerification().awaitResult()
                                                        verificationSent = res.isSuccess
                                                    }
                                                } catch (_: Exception) {}
                                            }

                                            FirebaseService.saveUserRecord(
                                                uid = uid,
                                                email = emailClean,
                                                phone = phoneClean,
                                                password = passwordInput.trim(),
                                                name = nameInput.trim(),
                                                signInMethod = if (emailClean.isNotBlank()) "Email & Password" else "Phone",
                                                emailVerified = false
                                            )

                                            // Sign out user immediately so they cannot enter without email verification
                                            try {
                                                FirebaseService.auth.signOut()
                                            } catch (_: Exception) {}

                                            isLoading = false

                                            if (emailClean.contains("@")) {
                                                pendingVerificationEmail = emailClean
                                                pendingVerificationPassword = passwordInput.trim()
                                                showEmailVerificationSentDialog = true
                                                authSuccessMessage = "Verification email sent to $emailClean! Please click the verification link in your inbox before logging in."
                                                Toast.makeText(context, "Verification link sent to $emailClean! Please verify your email.", Toast.LENGTH_LONG).show()
                                                isRegistering = false
                                                passwordInput = ""
                                            } else {
                                                authSuccessMessage = "Registration Successful! Account created for $phoneClean. Please log in now."
                                                Toast.makeText(context, "Registered successfully! Please log in now.", Toast.LENGTH_LONG).show()
                                                isRegistering = false
                                                passwordInput = ""
                                            }
                                        }
                                    }
                                    .addOnFailureListener { ex ->
                                        val msg = ex.localizedMessage ?: "Unknown registration error"
                                        coroutineScope.launch {
                                            isLoading = false
                                            if (msg.contains("already in use") || msg.contains("collision") || msg.contains("exists")) {
                                                authErrorMessage = "Registration Failed: An account with this email ($emailClean) already exists. If your email is unverified, please sign in or click Verify Email to verify."
                                                Toast.makeText(context, "Account already exists. Please verify or sign in.", Toast.LENGTH_LONG).show()
                                                if (emailClean.contains("@")) {
                                                    unverifiedEmailAddress = emailClean
                                                    unverifiedUserPassword = passwordInput.trim()
                                                    showEmailNotVerifiedDialog = true
                                                }
                                            } else {
                                                authErrorMessage = "Registration Failed: $msg"
                                                Toast.makeText(context, "Registration error: $msg", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }"""

if old_reg in text:
    text = text.replace(old_reg, new_reg, 1)
    print("1. Registration logic updated.")
else:
    print("1. Registration logic target not matched!")

# 2. Update Login Check - accountRecord branch
old_login_record = """                                        if (authSuccess) {
                                            // Sync password to Firestore if it changed
                                            if (storedPassword != passwordInput.trim()) {
                                                FirebaseService.updateUserPasswordInFirestore(loginId, passwordInput.trim())
                                            }

                                            // Re-check profile after sign in
                                            val signedInUid = FirebaseService.auth.currentUser?.uid ?: storedUid"""

new_login_record = """                                        if (authSuccess) {
                                            val currentUser = FirebaseService.auth.currentUser
                                            try {
                                                with(FirebaseService) {
                                                    currentUser?.reload()?.awaitResult()
                                                }
                                            } catch (_: Exception) {}

                                            // Check email verification if the account has an email address
                                            val isEmailAccount = loginId.contains("@") || (accountRecord["email"]?.isNotBlank() == true) || (storedAuthEmail.contains("@") && !storedAuthEmail.startsWith("phone_"))
                                            val emailToVerify = if (loginId.contains("@")) loginId else (accountRecord["email"]?.ifBlank { null } ?: storedAuthEmail)

                                            if (isEmailAccount && currentUser != null && !currentUser.isEmailVerified) {
                                                // User has not verified their email yet! Block login as required.
                                                unverifiedEmailAddress = emailToVerify
                                                unverifiedUserPassword = passwordInput.trim()
                                                showEmailNotVerifiedDialog = true

                                                try {
                                                    FirebaseService.auth.signOut()
                                                } catch (_: Exception) {}

                                                isLoading = false
                                                authErrorMessage = "Email Not Verified: Please verify your email ($emailToVerify) before logging in. Check your inbox and spam folder for the link."
                                                Toast.makeText(context, "⚠️ Email not verified! Please check your email inbox.", Toast.LENGTH_LONG).show()
                                                return@launch
                                            }

                                            // Sync verified status
                                            val signedInUid = currentUser?.uid ?: storedUid
                                            if (isEmailAccount) {
                                                FirebaseService.updateUserEmailVerified(signedInUid, loginId)
                                            }

                                            // Sync password to Firestore if it changed
                                            if (storedPassword != passwordInput.trim()) {
                                                FirebaseService.updateUserPasswordInFirestore(loginId, passwordInput.trim())
                                            }

                                            // Re-check profile after sign in"""

if old_login_record in text:
    text = text.replace(old_login_record, new_login_record, 1)
    print("2. Login accountRecord check updated.")
else:
    print("2. Login accountRecord check target not matched!")

# 3. Update Login Check - fallback loginId.contains("@")
old_login_direct = """                                    if (loginId.contains("@")) {
                                        FirebaseService.auth.signInWithEmailAndPassword(loginId, passwordInput.trim())
                                            .addOnSuccessListener {
                                                coroutineScope.launch {
                                                    val uid = FirebaseService.auth.currentUser?.uid ?: "user\""""

new_login_direct = """                                    if (loginId.contains("@")) {
                                        FirebaseService.auth.signInWithEmailAndPassword(loginId, passwordInput.trim())
                                            .addOnSuccessListener {
                                                coroutineScope.launch {
                                                    val currentUser = FirebaseService.auth.currentUser
                                                    try {
                                                        with(FirebaseService) {
                                                            currentUser?.reload()?.awaitResult()
                                                        }
                                                    } catch (_: Exception) {}

                                                    if (currentUser != null && !currentUser.isEmailVerified) {
                                                        // Email not verified! Block login as requested
                                                        unverifiedEmailAddress = loginId
                                                        unverifiedUserPassword = passwordInput.trim()
                                                        showEmailNotVerifiedDialog = true

                                                        try {
                                                            FirebaseService.auth.signOut()
                                                        } catch (_: Exception) {}

                                                        isLoading = false
                                                        authErrorMessage = "Email Not Verified: Please verify your email ($loginId) before logging in. Check your inbox and spam folder."
                                                        Toast.makeText(context, "⚠️ Email not verified! Please verify before logging in.", Toast.LENGTH_LONG).show()
                                                        return@launch
                                                    }

                                                    val uid = currentUser?.uid ?: "user"
                                                    FirebaseService.updateUserEmailVerified(uid, loginId)"""

if old_login_direct in text:
    text = text.replace(old_login_direct, new_login_direct, 1)
    print("3. Login direct fallback check updated.")
else:
    print("3. Login direct fallback check target not matched!")

# 4. Add the two dialogs if not present
if "showEmailVerificationSentDialog" in text and "Step 1 of 2: Verification Required" not in text:
    old_dialog_anchor = """    if (showDisclaimerDialog) {
        DisclaimerDialog(onDismiss = { showDisclaimerDialog = false })
    }"""

    new_dialogs = """    if (showDisclaimerDialog) {
        DisclaimerDialog(onDismiss = { showDisclaimerDialog = false })
    }

    // -------------------------------------------------------------
    // EMAIL VERIFICATION SENT DIALOG (Post-registration)
    // -------------------------------------------------------------
    if (showEmailVerificationSentDialog) {
        AlertDialog(
            onDismissRequest = {
                showEmailVerificationSentDialog = false
                isRegistering = false
                emailInput = pendingVerificationEmail
                passwordInput = ""
            },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEFF6FF),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Verification Sent",
                            tint = Color(0xFF1E66F5),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Verify Your Email Address",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Step 1 of 2: Verification Required",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = Color(0xFF1E66F5),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = pendingVerificationEmail,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }

                    Text(
                        text = "A Firebase email verification link has been sent to your registered email address.",
                        fontSize = 13.sp,
                        color = Color(0xFF334155),
                        lineHeight = 17.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "⚠️ Action Required Before Login:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = "1. Open your email inbox (check Spam/Junk if needed).\\n2. Click the verification link to verify your email.\\n3. Only after verification, login will succeed!",
                                fontSize = 12.sp,
                                color = Color(0xFF78350F),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    if (verificationStatusCheckMessage != null) {
                        Text(
                            text = verificationStatusCheckMessage!!,
                            fontSize = 12.sp,
                            color = if (verificationStatusCheckMessage!!.startsWith("✅")) Color(0xFF16A34A) else Color(0xFFDC2626),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Resend Link Button
                    OutlinedButton(
                        enabled = !isResendingVerification,
                        onClick = {
                            if (pendingVerificationEmail.isNotBlank() && pendingVerificationPassword.isNotBlank()) {
                                isResendingVerification = true
                                verificationStatusCheckMessage = null
                                coroutineScope.launch {
                                    val res = FirebaseService.resendEmailVerification(pendingVerificationEmail, pendingVerificationPassword)
                                    isResendingVerification = false
                                    if (res.isSuccess) {
                                        verificationStatusCheckMessage = "✅ Verification email resent! Please check your inbox."
                                        Toast.makeText(context, "Verification link resent! Check inbox.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        verificationStatusCheckMessage = "❌ Resend error: ${res.exceptionOrNull()?.localizedMessage}"
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Credentials required to resend verification.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isResendingVerification) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Resending...")
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Resend Verification Link", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEmailVerificationSentDialog = false
                        isRegistering = false
                        emailInput = pendingVerificationEmail
                        passwordInput = ""
                        authSuccessMessage = "Verification email sent to $pendingVerificationEmail. Click the link in your email, then enter your password to sign in!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0256FF)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Proceed to Sign In")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEmailVerificationSentDialog = false
                        isRegistering = false
                        emailInput = pendingVerificationEmail
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }

    // -------------------------------------------------------------
    // EMAIL NOT VERIFIED DIALOG (Shown on login block or verify tap)
    // -------------------------------------------------------------
    if (showEmailNotVerifiedDialog) {
        var verifyCheckLoading by remember { mutableStateOf(false) }
        var resendPassInput by remember { mutableStateOf(unverifiedUserPassword.ifBlank { passwordInput }) }
        var resendEmailInput by remember { mutableStateOf(unverifiedEmailAddress.ifBlank { if (emailInput.contains("@")) emailInput else "" }) }
        var localFeedback by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showEmailNotVerifiedDialog = false },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFEF3C7),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email Not Verified",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Email Verification Required",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF92400E),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Registration Pending Verification",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Your account has not been verified yet. Only after verifying your email address will registration be complete and login allowed.",
                        fontSize = 13.sp,
                        color = Color(0xFF334155),
                        lineHeight = 17.sp
                    )

                    OutlinedTextField(
                        value = resendEmailInput,
                        onValueChange = { resendEmailInput = it },
                        label = { Text("Registered Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = resendPassInput,
                        onValueChange = { resendPassInput = it },
                        label = { Text("Account Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    if (localFeedback != null) {
                        Text(
                            text = localFeedback!!,
                            fontSize = 12.sp,
                            color = if (localFeedback!!.startsWith("✅")) Color(0xFF16A34A) else Color(0xFFDC2626),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "💡 Steps to activate your account:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "• Check your inbox and spam folder for the Firebase link.\\n• Click the link to verify your email.\\n• Then tap 'Check Verification & Login' below.",
                                fontSize = 11.sp,
                                color = Color(0xFF475569),
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // Resend button
                    Button(
                        enabled = !isResendingVerification && !verifyCheckLoading,
                        onClick = {
                            val cleanMail = resendEmailInput.trim().lowercase()
                            if (cleanMail.isBlank() || resendPassInput.trim().isBlank()) {
                                localFeedback = "❌ Please enter your email and password to resend link."
                                return@Button
                            }
                            isResendingVerification = true
                            localFeedback = null
                            coroutineScope.launch {
                                val res = FirebaseService.resendEmailVerification(cleanMail, resendPassInput.trim())
                                isResendingVerification = false
                                if (res.isSuccess) {
                                    localFeedback = "✅ Fresh verification email sent to $cleanMail! Check your inbox."
                                    Toast.makeText(context, "Verification link resent to $cleanMail!", Toast.LENGTH_SHORT).show()
                                } else {
                                    localFeedback = "❌ Could not resend: ${res.exceptionOrNull()?.localizedMessage}"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                    ) {
                        if (isResendingVerification) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sending Email...", color = Color.White)
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Resend Verification Email", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !verifyCheckLoading && !isResendingVerification,
                    onClick = {
                        val cleanMail = resendEmailInput.trim().lowercase()
                        val pass = resendPassInput.trim()
                        if (cleanMail.isBlank() || pass.isBlank()) {
                            localFeedback = "❌ Please enter email and password to check verification."
                            return@Button
                        }
                        verifyCheckLoading = true
                        localFeedback = null
                        coroutineScope.launch {
                            val signInRes = FirebaseService.auth.signInWithEmailAndPassword(cleanMail, pass).awaitResult()
                            if (signInRes.isFailure) {
                                verifyCheckLoading = false
                                localFeedback = "❌ Sign-in failed: ${signInRes.exceptionOrNull()?.localizedMessage}"
                                return@launch
                            }
                            val currentUser = FirebaseService.auth.currentUser
                            try {
                                with(FirebaseService) {
                                    currentUser?.reload()?.awaitResult()
                                }
                            } catch (_: Exception) {}

                            if (currentUser?.isEmailVerified == true) {
                                verifyCheckLoading = false
                                val uid = currentUser.uid
                                FirebaseService.updateUserEmailVerified(uid, cleanMail)
                                val accountRecord = FirebaseService.getUserAccountRecord(cleanMail)
                                val storedName = accountRecord?.get("name")?.ifBlank { null } ?: "KCET Student"
                                Toast.makeText(context, "🎉 Email verified! Welcome to KCET Gen Z!", Toast.LENGTH_SHORT).show()
                                showEmailNotVerifiedDialog = false
                                onLoginSuccess(cleanMail, storedName, "Email & Password")
                            } else {
                                verifyCheckLoading = false
                                try { FirebaseService.auth.signOut() } catch (_: Exception) {}
                                localFeedback = "❌ Email is still not verified! Please open the link in your email inbox first."
                                Toast.makeText(context, "Email not verified yet. Please click the link in your email.", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0256FF)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (verifyCheckLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Checking...")
                    } else {
                        Text("Check & Sign In")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmailNotVerifiedDialog = false }) {
                    Text("Close")
                }
            }
        )
    }"""

    if old_dialog_anchor in text:
        text = text.replace(old_dialog_anchor, new_dialogs, 1)
        print("4. Dialogs added.")
    else:
        print("4. Dialog anchor not matched!")

with open(auth_screen_path, 'w', encoding='utf-8') as f:
    f.write(text)
print("Finished applying updates to AuthScreen.kt")
