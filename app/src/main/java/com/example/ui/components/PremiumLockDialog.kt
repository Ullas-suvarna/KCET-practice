package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.ImageUtils
import com.example.data.PaymentReceipt

@Composable
fun PremiumLockDialog(
    subscriptionPrice: Int = 299,
    paymentReceipt: PaymentReceipt? = null,
    isPremium: Boolean = false,
    onSubmitReceipt: (transactionRef: String, receiptUri: String) -> Unit = { _, _ -> },
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit = {}
) {
    val context = LocalContext.current
    var transactionRefInput by remember { mutableStateOf("") }
    var receiptUriInput by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64 = ImageUtils.uriToBase64(context, uri)
            if (base64 != null) {
                receiptUriInput = base64
                Toast.makeText(context, "Payment receipt screenshot attached successfully!", Toast.LENGTH_SHORT).show()
            } else {
                receiptUriInput = uri.toString()
                Toast.makeText(context, "Payment receipt attached!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(horizontal = 10.dp, vertical = 16.dp)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF3C7),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B))
                ) {
                    Text(
                        text = "🔥 SPECIAL OFFER: ₹$subscriptionPrice (DISCOUNTED FROM ₹999)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isPremium) "⭐ Premium Unlocked" else "Unlock KCET Premium Membership",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Get access to Marks vs Rank Dashboard, 15+ Mock Tests, 10 Years Solved PYQs, Branch Explorer & College Recommendation",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                )

                // Premium Features Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7))
                            )
                        )
                        .border(1.5.dp, Color(0xFFF59E0B), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "👑 What's Included in Premium Pass:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        val featuresList = listOf(
                            "📊 KCET Marks vs Rank Dashboard & Instant Predictor",
                            "📝 15+ Full CBT Mock Tests & Real Exam Templates",
                            "📚 10+ Years Solved PYQ Question Papers with Step Solutions",
                            "🌿 Branch Explorer: 200+ Colleges & GM/2A/SC/ST Cutoffs",
                            "🏛️ College Recommendation & Rank Range",
                            "📖 Chapter-wise DPP Practice Sets & Formula eBooks",
                            "⚡ Priority AI Doubt Solver & Unlimited Access"
                        )

                        featuresList.forEach { feature ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• $feature",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF78350F)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // STATUS DISPLAY BOX IF RECEIPT ALREADY SUBMITTED
                if (isPremium) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF0FDF4))
                            .border(1.5.dp, Color(0xFF22C55E), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("⭐ Premium Unlocked Permanently!", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF15803D))
                            Text("Your account has lifetime access to all mock tests, PYQ papers & study notes.", fontSize = 12.sp, color = Color(0xFF166534), textAlign = TextAlign.Center)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                } else if (paymentReceipt?.status == "UNDER_VERIFICATION") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFEFCE8))
                            .border(1.5.dp, Color(0xFFEAB308), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.HourglassTop, contentDescription = null, tint = Color(0xFFCA8A04), modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("⏳ Payment is under verification", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFA16207))
                            Text("Please wait (1-6 hour) need to verification.", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF854D0E), textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Submitted Ref: ${paymentReceipt.transactionRef.ifBlank { "#REC-${paymentReceipt.receiptId.takeLast(6)}" }}", fontSize = 12.sp, color = Color(0xFF713F12))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                } else if (paymentReceipt?.status == "REJECTED") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFEF2F2))
                            .border(1.5.dp, Color(0xFFEF4444), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("❌ Payment Verification Failed", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFB91C1C))
                            Text("Reason: ${paymentReceipt.rejectionReason.ifBlank { "Invalid Transaction ID / Payment reference not found" }}", fontSize = 12.sp, color = Color(0xFF991B1B), textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Please re-check your payment and re-submit a valid receipt below.", fontSize = 11.sp, color = Color(0xFF7F1D1D))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // QR CODE PAYMENT DISPLAY
                if (!isPremium) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Scan QR Code to Pay ₹299", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                            Spacer(modifier = Modifier.height(10.dp))

                            // QR Code Image (Replaced with new QR code)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.92f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White)
                                    .border(1.5.dp, Color(0xFF6366F1).copy(alpha = 0.35f), RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.upi_payment_qr_1785749615690),
                                    contentDescription = "KCET Premium Payment QR Code",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(20.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Direct UPI Payment Button
                            Button(
                                onClick = {
                                    val upiUri = Uri.parse("upi://pay?pa=9480070645@fam&pn=KCET%20GenZ%20Premium&am=$subscriptionPrice&cu=INR&tn=KCET%20GenZ%20Premium%20Pass")
                                    val intent = Intent(Intent.ACTION_VIEW, upiUri)
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("UPI ID", "9480070645@fam")
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "UPI ID 9480070645@fam copied to clipboard! Open GPay / PhonePe to pay.", Toast.LENGTH_LONG).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                                shape = RoundedCornerShape(14.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 50.dp)
                            ) {
                                Icon(imageVector = Icons.Default.QrCode2, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("⚡ Pay Direct via UPI App (₹$subscriptionPrice)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Copy UPI Handle
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFEEF2FF))
                                    .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(12.dp))
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("UPI ID", "9480070645@fam")
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "UPI ID 9480070645@fam copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text("UPI ID: ", fontSize = 12.sp, color = Color(0xFF64748B))
                                Text("9480070645@fam", fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp, color = Color(0xFF4F46E5))
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy UPI ID",
                                    tint = Color(0xFF4F46E5),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // RECEIPT SUBMISSION FORM
                    if (paymentReceipt?.status != "UNDER_VERIFICATION") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(14.dp)
                        ) {
                            Text("Insert Paid Receipt Details", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Enter 12-digit UPI Ref / UTR number & attach receipt screenshot after paying ₹$subscriptionPrice:", fontSize = 11.sp, color = Color(0xFF64748B))

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = transactionRefInput,
                                onValueChange = { transactionRefInput = it },
                                label = { Text("Transaction ID / UTR Ref Number (e.g. 423819001234)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.2.dp, if (receiptUriInput.isNotBlank()) Color(0xFF16A34A) else Color(0xFF6366F1))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = if (receiptUriInput.isNotBlank()) Color(0xFF16A34A) else Color(0xFF4F46E5),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (receiptUriInput.isNotBlank()) "✅ Change Receipt Screenshot" else "Attach Payment Receipt Screenshot",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (receiptUriInput.isNotBlank()) Color(0xFF16A34A) else Color(0xFF4F46E5)
                                )
                            }

                            if (receiptUriInput.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                val previewBmp = remember(receiptUriInput) {
                                    ImageUtils.loadReceiptBitmap(context, receiptUriInput)
                                }
                                if (previewBmp != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White)
                                            .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        androidx.compose.foundation.Image(
                                            bitmap = previewBmp.asImageBitmap(),
                                            contentDescription = "Selected Receipt Preview",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (transactionRefInput.isBlank() && receiptUriInput.isBlank()) {
                                        Toast.makeText(context, "Please enter UTR Ref Number or attach receipt screenshot", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    onSubmitReceipt(transactionRefInput.trim(), receiptUriInput.trim())
                                    Toast.makeText(context, "Receipt submitted! Status: Payment under verification (1-6 hours)", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Submit Paid Receipt for Verification", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close", color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

