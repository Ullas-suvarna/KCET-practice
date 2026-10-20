package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Terms & Conditions Dialog
 */
@Composable
fun TermsAndConditionsDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = null,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Terms & Conditions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEEF2FF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Last Updated: January 2026 • Version 1.0.0\nDeveloped & Maintained by Gen Z EduTech",
                        fontSize = 11.sp,
                        color = Color(0xFF4338CA),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                LegalSectionItem(
                    title = "1. Acceptance of Terms",
                    content = "By creating an account or accessing the KCET Gen Z platform, developed and maintained by Gen Z EduTech, you agree to comply with and be bound by these Terms & Conditions."
                )

                LegalSectionItem(
                    title = "2. Educational Use Only",
                    content = "All mock test series, previous years' question papers (PYQs), rank predictors, syllabus guides, and study resources are intended exclusively for student practice and educational self-assessment."
                )

                LegalSectionItem(
                    title = "3. Independent Platform Disclaimer",
                    content = "KCET Gen Z is an independent educational platform and is NOT affiliated with, endorsed by, or associated with the Karnataka Examinations Authority (KEA) or the Government of Karnataka."
                )

                LegalSectionItem(
                    title = "4. Account Integrity & Security",
                    content = "Users are responsible for keeping their login credentials secure. Shared or unauthorized account distribution is strictly monitored to prevent abuse."
                )

                LegalSectionItem(
                    title = "5. Intellectual Property Rights",
                    content = "All platform design, branding, content organization, software code, and learning materials belong strictly to KCET Gen Z and Gen Z EduTech."
                )

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalDivider(color = Color(0xFFE2E8F0))

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "© 2026 KCET Gen Z. All Rights Reserved.",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("I Understand & Agree")
            }
        }
    )
}

/**
 * Privacy Policy Dialog
 */
@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PrivacyTip,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Privacy Policy",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFECFDF5),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔒 Your data privacy is our top priority.\nGen Z EduTech complies with modern mobile security standards.",
                        fontSize = 11.sp,
                        color = Color(0xFF047857),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                LegalSectionItem(
                    title = "1. Information We Collect",
                    content = "We collect basic user details (Full Name, Email Address, Mobile Phone Number, Target Stream, and KCET Target Rank) to create your student account and personalize test analytics."
                )

                LegalSectionItem(
                    title = "2. How We Use Your Data",
                    content = "Your data is strictly utilized to authenticate your logins, generate test performance reports, facilitate 1-on-1 senior mentorship bookings, and issue reward badges."
                )

                LegalSectionItem(
                    title = "3. Data Security & Storage",
                    content = "All user credentials and records are protected using Google Firebase Cloud Encryption. Passwords and sensitive data are encrypted and never stored in plain text."
                )

                LegalSectionItem(
                    title = "4. No Third-Party Selling",
                    content = "Gen Z EduTech does NOT sell, rent, or trade your personal information to third-party advertisers or telemarketers."
                )

                LegalSectionItem(
                    title = "5. Account & Data Control",
                    content = "You can update your profile information or request complete account data deletion at any time by contacting our helpline at KcetGenZservice@gmail.com."
                )

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalDivider(color = Color(0xFFE2E8F0))

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Developed & Maintained by Gen Z EduTech.",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Close Privacy Policy")
            }
        }
    )
}

/**
 * Contact Support & Help Dialog
 */
@Composable
fun ContactSupportDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = null,
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Need Help? Contact Support",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Our support team at Gen Z EduTech is available to assist you with login issues, test access, payment verification, and KCET preparation queries.",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Official Email Card
                SupportContactCard(
                    icon = Icons.Default.AlternateEmail,
                    title = "Helpline Email",
                    value = "KcetGenZservice@gmail.com",
                    actionLabel = "Send Email",
                    iconTint = Color(0xFF6366F1),
                    onAction = {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:KcetGenZservice@gmail.com"))
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Email", "KcetGenZservice@gmail.com"))
                            Toast.makeText(context, "Copied KcetGenZservice@gmail.com to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Senior Live Mentorship
                SupportContactCard(
                    icon = Icons.Default.AlternateEmail,
                    title = "Senior Live Mentorship",
                    value = "KcetGenZservice@gmail.com",
                    actionLabel = "Email Support",
                    iconTint = Color(0xFFE11D48),
                    onAction = {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:KcetGenZservice@gmail.com"))
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Contact support at KcetGenZservice@gmail.com", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "💡 Frequently Asked Questions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Forgot Password: Use the 'Forgot Password?' link on the Sign In page.\n" +
                                   "• 1-on-1 Sessions: Book senior slots via Live Senior Chat in the side menu.\n" +
                                   "• Premium Pass: Unlock unlimited tests & video solutions via Profile settings.",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Close Help")
            }
        }
    )
}

/**
 * About KCET Gen Z Dialog
 */
@Composable
fun AboutAppDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "About KCET Gen Z",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("GenZ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "KCET Gen Z ⭐",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Text(
                    text = "Version 1.0.0 (Production Build)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6366F1)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "KCET Gen Z is Karnataka's premier AI-powered entrance test preparation & counselling guidance app designed specifically for Engineering, Medical (NEET), and Agriculture aspirants.",
                    fontSize = 12.sp,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🚀 Key Capabilities:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E293B))
                        Text(text = "• Chapterwise PYQs with Detailed Video Solutions", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text(text = "• Real-Time Rank vs Marks Predictor Engine", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text(text = "• AI Doubt Solver with Step-by-Step Explanations", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text(text = "• 1-on-1 Personal Senior Student Mentorship", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text(text = "• KEA College Cutoff & Seat Matrix Analyzer", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Developed & Maintained by Gen Z EduTech",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Lead Developer & Founder: Ullas U Suvarna",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "© 2026 KCET Gen Z. All Rights Reserved.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Got It")
            }
        }
    )
}

/**
 * Government & KEA Disclaimer Dialog
 */
@Composable
fun DisclaimerDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Official Disclaimer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF3C7),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠️ Independent Educational Platform Statement",
                        fontSize = 12.sp,
                        color = Color(0xFF92400E),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "KCET Gen Z is an independent educational prep app developed and maintained by Gen Z EduTech.\n\n" +
                           "• This application is NOT affiliated with, authorized by, endorsed by, or in any way officially connected with the Karnataka Examinations Authority (KEA), the Government of Karnataka, or any official examination board.\n\n" +
                           "• All official notifications, examination timetables, admit cards, and official seat allotment details should be verified on the official KEA website: kea.kar.nic.in.\n\n" +
                           "• Rank prediction models and cutoff estimations are computed using historical statistical data for educational guidance purposes only.",
                    fontSize = 12.sp,
                    color = Color(0xFF334155),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                HorizontalDivider(color = Color(0xFFE2E8F0))

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "© 2026 KCET Gen Z • Developed & Maintained by Gen Z EduTech",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Acknowledge")
            }
        }
    )
}

@Composable
private fun LegalSectionItem(
    title: String,
    content: String
) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            fontSize = 12.sp,
            color = Color(0xFF475569),
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun SupportContactCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    actionLabel: String,
    iconTint: Color,
    onAction: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    shape = CircleShape,
                    color = iconTint.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = title, fontSize = 11.sp, color = Color(0xFF64748B))
                    Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                }
            }
            OutlinedButton(
                onClick = onAction,
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(text = actionLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
