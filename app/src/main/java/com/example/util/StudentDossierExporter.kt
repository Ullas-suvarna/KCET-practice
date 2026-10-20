package com.example.util

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.AdminTestResultItem
import com.example.data.UserProfile
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object StudentDossierExporter {

    private fun filterStudentTests(allTests: List<AdminTestResultItem>, student: UserProfile): List<AdminTestResultItem> {
        val cleanUid = student.uid.lowercase().trim()
        val cleanEmail = student.email.lowercase().trim()
        val cleanStudentId = student.formattedStudentId.lowercase().trim()
        val cleanName = student.name.lowercase().trim()

        return allTests.filter { test ->
            val tUserId = test.userId.lowercase().trim()
            val tEmail = test.userEmail.lowercase().trim()
            val tStudentId = test.studentId.lowercase().trim()
            val tName = test.userName.lowercase().trim()

            (tUserId.isNotBlank() && (tUserId == cleanUid || (cleanEmail.isNotBlank() && tUserId == cleanEmail) || (cleanStudentId.isNotBlank() && tUserId == cleanStudentId))) ||
            (tEmail.isNotBlank() && cleanEmail.isNotBlank() && tEmail == cleanEmail) ||
            (tStudentId.isNotBlank() && cleanStudentId.isNotBlank() && tStudentId == cleanStudentId) ||
            (tName.isNotBlank() && cleanName.isNotBlank() && tName == cleanName)
        }
    }

    /**
     * Generates a printer-optimized HTML document displaying complete student personal info,
     * IDs, and passwords clearly one-by-one.
     */
    fun generateDossierHtml(
        students: List<UserProfile>,
        testResults: List<AdminTestResultItem>
    ): String {
        val dateStr = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.US).format(Date())
        val sb = StringBuilder()

        sb.append("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>KCET Gen Z - Student Directory & Credentials Dossier</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                        margin: 0;
                        padding: 20px;
                        color: #0f172a;
                        background-color: #f8fafc;
                        font-size: 13px;
                        line-height: 1.5;
                    }
                    @media print {
                        body {
                            background-color: #ffffff;
                            padding: 0;
                            font-size: 12px;
                        }
                        .no-print { display: none !important; }
                        .student-card {
                            page-break-inside: avoid;
                            border: 1px solid #cbd5e1 !important;
                            box-shadow: none !important;
                            margin-bottom: 20px !important;
                        }
                    }
                    .header-box {
                        background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%);
                        color: #ffffff;
                        padding: 24px;
                        border-radius: 12px;
                        margin-bottom: 24px;
                        text-align: center;
                    }
                    .header-title {
                        font-size: 22px;
                        font-weight: 800;
                        margin: 0 0 6px 0;
                        letter-spacing: 0.5px;
                    }
                    .header-subtitle {
                        font-size: 13px;
                        color: #c7d2fe;
                        margin: 0 0 10px 0;
                    }
                    .header-meta {
                        display: flex;
                        justify-content: center;
                        gap: 16px;
                        font-size: 11px;
                        color: #e0e7ff;
                        font-weight: 600;
                    }
                    .student-card {
                        background: #ffffff;
                        border: 1.5px solid #e2e8f0;
                        border-radius: 12px;
                        padding: 18px;
                        margin-bottom: 18px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.04);
                    }
                    .card-header {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        border-bottom: 2px solid #f1f5f9;
                        padding-bottom: 12px;
                        margin-bottom: 12px;
                    }
                    .student-index {
                        background-color: #4f46e5;
                        color: #ffffff;
                        font-size: 12px;
                        font-weight: 800;
                        padding: 4px 10px;
                        border-radius: 6px;
                        display: inline-block;
                        margin-right: 8px;
                    }
                    .student-name {
                        font-size: 17px;
                        font-weight: 800;
                        color: #0f172a;
                    }
                    .badges-row {
                        display: flex;
                        gap: 6px;
                    }
                    .badge {
                        font-size: 10px;
                        font-weight: 700;
                        padding: 3px 8px;
                        border-radius: 6px;
                        text-transform: uppercase;
                    }
                    .badge-premium { background-color: #fef3c7; color: #92400e; border: 1px solid #fde68a; }
                    .badge-free { background-color: #f1f5f9; color: #475569; border: 1px solid #e2e8f0; }
                    .badge-active { background-color: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
                    .badge-blocked { background-color: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }
                    
                    .credential-box {
                        background-color: #eff6ff;
                        border: 1.5px dashed #3b82f6;
                        border-radius: 8px;
                        padding: 12px 14px;
                        margin: 12px 0;
                        display: flex;
                        flex-wrap: wrap;
                        gap: 16px;
                        justify-content: space-between;
                    }
                    .cred-item {
                        flex: 1;
                        min-width: 140px;
                    }
                    .cred-label {
                        font-size: 10px;
                        font-weight: 700;
                        color: #1e40af;
                        text-transform: uppercase;
                        margin-bottom: 2px;
                    }
                    .cred-value {
                        font-size: 14px;
                        font-weight: 800;
                        color: #1e3a8a;
                        font-family: "Courier New", Courier, monospace;
                    }
                    .password-highlight {
                        background-color: #fef08a;
                        color: #854d0e;
                        padding: 2px 6px;
                        border-radius: 4px;
                        border: 1px solid #facc15;
                    }

                    .info-grid {
                        display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
                        gap: 10px;
                        margin: 10px 0;
                        background-color: #f8fafc;
                        padding: 12px;
                        border-radius: 8px;
                    }
                    .info-cell {
                        display: flex;
                        flex-direction: column;
                    }
                    .info-cell-label {
                        font-size: 10px;
                        color: #64748b;
                        font-weight: 600;
                        text-transform: uppercase;
                    }
                    .info-cell-value {
                        font-size: 12px;
                        font-weight: 700;
                        color: #334155;
                    }

                    .tests-summary {
                        background-color: #faf5ff;
                        border: 1px solid #e9d5ff;
                        border-radius: 8px;
                        padding: 10px 14px;
                        margin-top: 10px;
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                    }
                    .tests-stat {
                        font-size: 11px;
                        font-weight: 700;
                        color: #6b21a8;
                    }
                    .footer-note {
                        text-align: center;
                        font-size: 11px;
                        color: #94a3b8;
                        margin-top: 30px;
                        padding-top: 16px;
                        border-top: 1px solid #e2e8f0;
                    }
                </style>
            </head>
            <body>
                <div class="header-box">
                    <div class="header-title">🎓 KCET Gen Z — Student Directory & Credentials</div>
                    <div class="header-subtitle">Official Student Master Records, Personal Profiles, IDs & Passwords</div>
                    <div class="header-meta">
                        <span>📅 Generated: $dateStr</span>
                        <span>👥 Total Registered Students: ${students.size}</span>
                        <span>🔒 Confidential Administrative Record</span>
                    </div>
                </div>
        """.trimIndent())

        students.forEachIndexed { index, student ->
            val tests = filterStudentTests(testResults, student)
            val avgPct = if (tests.isNotEmpty()) "%.1f%%".format(tests.map { it.percentage }.average()) else "N/A"
            val bestScore = if (tests.isNotEmpty()) "${tests.maxOf { it.score }} / 180" else "0 / 180"
            val effectivePassword = student.displayPassword

            sb.append("""
                <div class="student-card">
                    <div class="card-header">
                        <div>
                            <span class="student-index">#${index + 1}</span>
                            <span class="student-name">${escapeHtml(student.name)}</span>
                        </div>
                        <div class="badges-row">
                            <span class="badge ${if (student.isPremium) "badge-premium" else "badge-free"}">
                                ${if (student.isPremium) "👑 Premium Pro" else "🎓 Free Tier"}
                            </span>
                            <span class="badge ${if (student.isBlocked) "badge-blocked" else "badge-active"}">
                                ${if (student.isBlocked) "🚫 Blocked" else if (student.isForceLoggedOut) "🚪 Logged Out" else "🟢 Active"}
                            </span>
                        </div>
                    </div>

                    <!-- CREDENTIALS BOX (ID & PASSWORD) -->
                    <div class="credential-box">
                        <div class="cred-item">
                            <div class="cred-label">🆔 Student ID</div>
                            <div class="cred-value">${escapeHtml(student.formattedStudentId)}</div>
                        </div>
                        <div class="cred-item">
                            <div class="cred-label">📧 Login Email / Mobile</div>
                            <div class="cred-value">${escapeHtml(if (student.email.isNotBlank()) student.email else "Not Set")}</div>
                        </div>
                        <div class="cred-item">
                            <div class="cred-label">🔑 Password</div>
                            <div class="cred-value password-highlight">${escapeHtml(effectivePassword)}</div>
                        </div>
                        <div class="cred-item">
                            <div class="cred-label">🔐 Auth Method</div>
                            <div class="cred-value" style="font-size:12px;">${escapeHtml(student.signInMethod)}</div>
                        </div>
                    </div>

                    <!-- PERSONAL & ACADEMIC INFO -->
                    <div class="info-grid">
                        <div class="info-cell">
                            <span class="info-cell-label">🎯 Target KCET Rank</span>
                            <span class="info-cell-value" style="color:#4f46e5;">${escapeHtml(student.kcetTargetRank)}</span>
                        </div>
                        <div class="info-cell">
                            <span class="info-cell-label">🎓 Target Stream / Branch</span>
                            <span class="info-cell-value">${escapeHtml(student.targetStream)}</span>
                        </div>
                        <div class="info-cell">
                            <span class="info-cell-label">📅 Registration Date</span>
                            <span class="info-cell-value">${escapeHtml(student.registrationDate)}</span>
                        </div>
                        <div class="info-cell">
                            <span class="info-cell-label">🔑 System UID</span>
                            <span class="info-cell-value" style="font-family:monospace; font-size:11px;">${escapeHtml(student.uid)}</span>
                        </div>
                    </div>

                    <!-- ACADEMIC TEST PERFORMANCE -->
                    <div class="tests-summary">
                        <span class="tests-stat">📊 Tests Taken: ${tests.size}</span>
                        <span class="tests-stat">🏆 Best Score: $bestScore</span>
                        <span class="tests-stat">📈 Avg Percentage: $avgPct</span>
                    </div>
                </div>
            """.trimIndent())
        }

        sb.append("""
                <div class="footer-note">
                    KCET Gen Z CBT Platform • Confidential Administrative Export • Total Records: ${students.size}
                </div>
            </body>
            </html>
        """.trimIndent())

        return sb.toString()
    }

    /**
     * Generates a structured plain-text dossier formatted student-by-student with complete details.
     */
    fun generateDossierPlainText(
        students: List<UserProfile>,
        testResults: List<AdminTestResultItem>
    ): String {
        val dateStr = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.US).format(Date())
        val sb = StringBuilder()

        sb.append("================================================================================\n")
        sb.append("               KCET GEN Z - STUDENT DIRECTORY & CREDENTIALS DOSSIER             \n")
        sb.append("================================================================================\n")
        sb.append("Generated On   : $dateStr\n")
        sb.append("Total Students : ${students.size}\n")
        sb.append("Document Type  : Confidential Administrative Student Credentials Record\n")
        sb.append("================================================================================\n\n")

        students.forEachIndexed { index, student ->
            val tests = filterStudentTests(testResults, student)
            val avgPct = if (tests.isNotEmpty()) "%.1f%%".format(tests.map { it.percentage }.average()) else "N/A"
            val bestScore = if (tests.isNotEmpty()) "${tests.maxOf { it.score }} / 180" else "0 / 180"
            val effectivePassword = student.displayPassword
            val statusStr = if (student.isBlocked) "BLOCKED (Reason: ${student.blockReason.ifBlank { "Admin Action" }})" else if (student.isForceLoggedOut) "FORCE LOGGED OUT" else "ACTIVE"
            val planStr = if (student.isPremium) "👑 PREMIUM PRO" else "🎓 FREE"

            sb.append("--------------------------------------------------------------------------------\n")
            sb.append("STUDENT #${index + 1} : ${student.name.uppercase()}\n")
            sb.append("--------------------------------------------------------------------------------\n")
            sb.append("  [ CREDENTIALS & LOGIN ]\n")
            sb.append("  • Student ID     : ${student.formattedStudentId}\n")
            sb.append("  • Login Email    : ${if (student.email.isNotBlank()) student.email else "Not provided"}\n")
            sb.append("  • Password       : $effectivePassword\n")
            sb.append("  • Sign-in Method : ${student.signInMethod}\n")
            sb.append("  • User UID       : ${student.uid}\n\n")

            sb.append("  [ PERSONAL & ACADEMIC INFORMATION ]\n")
            sb.append("  • Full Name      : ${student.name}\n")
            sb.append("  • Target Rank    : ${student.kcetTargetRank}\n")
            sb.append("  • Target Stream  : ${student.targetStream}\n")
            sb.append("  • Joined Date    : ${student.registrationDate}\n")
            sb.append("  • Plan Status    : $planStr\n")
            sb.append("  • Account Status : $statusStr\n\n")

            sb.append("  [ TEST & PERFORMANCE STATS ]\n")
            sb.append("  • Tests Attempted: ${tests.size}\n")
            sb.append("  • Highest Score  : $bestScore\n")
            sb.append("  • Average Pct    : $avgPct\n")

            if (tests.isNotEmpty()) {
                sb.append("  • Test History   :\n")
                tests.take(5).forEachIndexed { tIdx, t ->
                    val tDate = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(t.timestamp))
                    sb.append("     ${tIdx + 1}. ${t.testName} -> Score: ${t.score}/${t.totalQuestions} (${t.percentage}%) [$tDate]\n")
                }
            }
            sb.append("--------------------------------------------------------------------------------\n\n")
        }

        sb.append("================================================================================\n")
        sb.append("               END OF KCET STUDENT DIRECTORY DOSSIER (${students.size} STUDENTS) \n")
        sb.append("================================================================================\n")

        return sb.toString()
    }

    /**
     * Generates a standard CSV spreadsheet for Excel / Google Sheets.
     */
    fun generateDossierCsv(
        students: List<UserProfile>,
        testResults: List<AdminTestResultItem>
    ): String {
        val sb = StringBuilder()
        sb.append("Index,Student_ID,Full_Name,Login_Email,Password,Sign_In_Method,Target_Rank,Target_Stream,Plan,Status,Registration_Date,Total_Tests,Best_Score,Avg_Percentage,UID\n")

        students.forEachIndexed { index, student ->
            val tests = filterStudentTests(testResults, student)
            val avgPct = if (tests.isNotEmpty()) "%.1f".format(tests.map { it.percentage }.average()) else "0"
            val bestScore = if (tests.isNotEmpty()) "${tests.maxOf { it.score }}" else "0"
            val effectivePassword = student.displayPassword
            val statusStr = if (student.isBlocked) "Blocked" else if (student.isForceLoggedOut) "Logged Out" else "Active"
            val planStr = if (student.isPremium) "Premium Pro" else "Free"

            sb.append("${index + 1},")
            sb.append("\"${escapeCsv(student.formattedStudentId)}\",")
            sb.append("\"${escapeCsv(student.name)}\",")
            sb.append("\"${escapeCsv(student.email)}\",")
            sb.append("\"${escapeCsv(effectivePassword)}\",")
            sb.append("\"${escapeCsv(student.signInMethod)}\",")
            sb.append("\"${escapeCsv(student.kcetTargetRank)}\",")
            sb.append("\"${escapeCsv(student.targetStream)}\",")
            sb.append("\"${escapeCsv(planStr)}\",")
            sb.append("\"${escapeCsv(statusStr)}\",")
            sb.append("\"${escapeCsv(student.registrationDate)}\",")
            sb.append("${tests.size},")
            sb.append("$bestScore,")
            sb.append("$avgPct,")
            sb.append("\"${escapeCsv(student.uid)}\"\n")
        }

        return sb.toString()
    }

    /**
     * Triggers the Android Print Spooler with HTML content to allow printing or saving as PDF.
     */
    fun printDossier(context: Context, htmlContent: String, jobName: String = "KCET_Student_Directory_Dossier") {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager == null) {
                Toast.makeText(context, "Printing service unavailable on this device", Toast.LENGTH_SHORT).show()
                return
            }

            val webView = WebView(context)
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    try {
                        val printAdapter = webView.createPrintDocumentAdapter(jobName)
                        printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error starting print: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        } catch (e: Exception) {
            Toast.makeText(context, "Print error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Exports dossier content to cache and launches the Android share/download sheet.
     */
    fun shareOrDownloadDossier(
        context: Context,
        content: String,
        fileName: String,
        mimeType: String = "text/plain"
    ) {
        try {
            val cacheFile = File(context.cacheDir, fileName)
            cacheFile.writeText(content)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "KCET Students Complete Directory & Credentials")
                putExtra(Intent.EXTRA_TEXT, "Attached is the complete KCET student directory, personal info, IDs, and passwords.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(sendIntent, "Download / Export Student Dossier")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            // Fallback plain text intent
            try {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, content)
                }
                val chooser = Intent.createChooser(intent, "Download / Export Student Dossier")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (ex: Exception) {
                Toast.makeText(context, "Export error: ${ex.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Copies text to Android clipboard.
     */
    fun copyToClipboard(context: Context, text: String, label: String = "Student Dossier") {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "✓ $label copied to clipboard!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to copy: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    private fun escapeCsv(text: String): String {
        return text.replace("\"", "\"\"")
    }
}
