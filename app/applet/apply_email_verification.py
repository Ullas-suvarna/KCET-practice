auth_file = 'app/src/main/java/com/example/ui/screens/AuthScreen.kt'
with open(auth_file, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Update accountRecord branch in login
target1 = """                                        if (authSuccess) {
                                            // Sync password to Firestore if it changed
                                            if (storedPassword != passwordInput.trim()) {
                                                FirebaseService.updateUserPasswordInFirestore(loginId, passwordInput.trim())
                                            }

                                            // Re-check profile after sign in
                                            val signedInUid = FirebaseService.auth.currentUser?.uid ?: storedUid"""

replacement1 = """                                        if (authSuccess) {
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

                                            // Re-check profile after sign in
                                            val signedInUidCheck = FirebaseService.auth.currentUser?.uid ?: storedUid"""

assert target1 in content, "target1 not found"
content = content.replace(target1, replacement1, 1)

# Also replace signedInUid reference in the next lines if needed:
# The next line in original was:
# val userProfileDoc = if (signedInUid.isNotBlank()) ...
# Since we renamed to signedInUidCheck or kept signedInUid:
# Let's check: signedInUid is defined right before it, so let's keep signedInUid!
content = content.replace("val signedInUidCheck = FirebaseService.auth.currentUser?.uid ?: storedUid", "")

# 2. Update direct fallback login branch
target2 = """                                    if (loginId.contains("@")) {
                                        FirebaseService.auth.signInWithEmailAndPassword(loginId, passwordInput.trim())
                                            .addOnSuccessListener {
                                                coroutineScope.launch {
                                                    val uid = FirebaseService.auth.currentUser?.uid ?: "user"
                                                    val userProfileDoc = FirebaseService.getUserProfileOnce(uid)"""

replacement2 = """                                    if (loginId.contains("@")) {
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
                                                    FirebaseService.updateUserEmailVerified(uid, loginId)
                                                    val userProfileDoc = FirebaseService.getUserProfileOnce(uid)"""

assert target2 in content, "target2 not found"
content = content.replace(target2, replacement2, 1)

with open(auth_file, 'w', encoding='utf-8') as f:
    f.write(content)

print("Both login checks successfully updated in AuthScreen.kt!")
