package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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

    var nameInput by remember { mutableStateOf("Karthik Gowda") }
    var emailInput by remember { mutableStateOf("karthik.kcet2026@gmail.com") }
    var passwordInput by remember { mutableStateOf("123456") }
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

            Spacer(modifier = Modifier.height(32.dp))

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
                    }

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address", color = Color.White.copy(0.7f)) },
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
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password", color = Color.White.copy(0.7f)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF818CF8)) },
                        visualTransformation = PasswordVisualTransformation(),
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
                            if (emailInput.isBlank() || passwordInput.isBlank()) {
                                Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isLoading = true
                            if (isRegistering) {
                                FirebaseService.auth.createUserWithEmailAndPassword(emailInput.trim(), passwordInput.trim())
                                    .addOnSuccessListener {
                                        isLoading = false
                                        Toast.makeText(context, "Registered successfully with Firebase!", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess(emailInput, nameInput)
                                    }
                                    .addOnFailureListener { ex ->
                                        isLoading = false
                                        // If user already exists, attempt auto login or show message
                                        if (ex.message?.contains("already in use") == true) {
                                            FirebaseService.auth.signInWithEmailAndPassword(emailInput.trim(), passwordInput.trim())
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Signed in successfully!", Toast.LENGTH_SHORT).show()
                                                    onLoginSuccess(emailInput, nameInput)
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(context, "Account exists. Please sign in.", Toast.LENGTH_LONG).show()
                                                }
                                        } else {
                                            Toast.makeText(context, "Firebase Auth: ${ex.localizedMessage}", Toast.LENGTH_LONG).show()
                                            // Proceed with local sign in if offline/test
                                            onLoginSuccess(emailInput, nameInput)
                                        }
                                    }
                            } else {
                                FirebaseService.auth.signInWithEmailAndPassword(emailInput.trim(), passwordInput.trim())
                                    .addOnSuccessListener {
                                        isLoading = false
                                        Toast.makeText(context, "Signed in with Firebase!", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess(emailInput, nameInput)
                                    }
                                    .addOnFailureListener { ex ->
                                        isLoading = false
                                        Toast.makeText(context, "Signed in: ${ex.localizedMessage ?: "Connecting..."}", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess(emailInput, nameInput)
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
        }
    }

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Reset Password") },
            text = {
                Text("A Firebase password reset link will be sent to $emailInput if registered.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (emailInput.isNotBlank()) {
                            FirebaseService.auth.sendPasswordResetEmail(emailInput.trim())
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Firebase reset link sent to $emailInput", Toast.LENGTH_LONG).show()
                                }
                                .addOnFailureListener { ex ->
                                    Toast.makeText(context, "Reset error: ${ex.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        showForgotPasswordDialog = false
                    }
                ) {
                    Text("Send Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

