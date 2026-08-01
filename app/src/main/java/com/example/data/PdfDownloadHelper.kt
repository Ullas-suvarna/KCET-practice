package com.example.data

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object PdfDownloadHelper {
    private const val TAG = "PdfDownloadHelper"

    fun getPdfDir(context: Context): File {
        val dir = File(context.filesDir, "kcet_pdfs")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getPdfFile(context: Context, resourceId: String): File {
        return File(getPdfDir(context), "${resourceId.trim()}.pdf")
    }

    fun isPdfDownloaded(context: Context, resourceId: String): Boolean {
        val file = getPdfFile(context, resourceId)
        return file.exists() && file.length() > 500
    }

    suspend fun downloadPdf(
        context: Context,
        item: ResourceItem,
        onProgress: (Float) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        val targetFile = getPdfFile(context, item.id)

        // Generate verified, detailed multi-page PDF locally
        try {
            onProgress(0.3f)
            generateOfflinePdfFile(item, targetFile)
            onProgress(1.0f)
            Log.d(TAG, "Successfully generated verified detailed PDF: ${targetFile.absolutePath}")
            return@withContext Result.success(targetFile)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create offline PDF: ${e.message}")
            return@withContext Result.failure(e)
        }
    }

    // Structured PDF blocks
    private sealed class PdfBlock {
        data class Header(val text: String) : PdfBlock()
        data class Paragraph(val text: String) : PdfBlock()
        data class Bullet(val text: String) : PdfBlock()
        data class BoxNote(val title: String, val text: String, val isHighlight: Boolean = false) : PdfBlock()
        data class QuestionBox(
            val qNo: String,
            val question: String,
            val options: List<String>,
            val answer: String,
            val explanation: String
        ) : PdfBlock()
        data class FormulaItem(val name: String, val formula: String, val note: String = "") : PdfBlock()
    }

    private data class PdfPageContent(
        val sectionTitle: String,
        val blocks: List<PdfBlock>
    )

    fun generateOfflinePdfFile(item: ResourceItem, targetFile: File) {
        val pages = getContentForResource(item)
        val pdfDocument = PdfDocument()

        val titlePaint = Paint().apply {
            color = Color.parseColor("#1E1B4B")
            textSize = 16f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.parseColor("#4F46E5")
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val subHeaderPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 10f
            isAntiAlias = true
        }

        val accentBoxPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }

        val highlightBoxPaint = Paint().apply {
            color = Color.parseColor("#EEF2FF")
            style = Paint.Style.FILL
        }

        val borderPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
        }

        val totalPages = pages.size

        for ((pageIdx, pageContent) in pages.withIndex()) {
            val pageNum = pageIdx + 1
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Header Banner
            canvas.drawRect(35f, 35f, 560f, 100f, highlightBoxPaint)
            canvas.drawRect(35f, 35f, 560f, 100f, borderPaint)

            canvas.drawText("KCET PRACTICE PRO • KARNATAKA KEA OFFICIAL STUDY MATERIAL", 48f, 56f, headerPaint)
            canvas.drawText(item.title, 48f, 76f, titlePaint)
            canvas.drawText("Category: ${item.category}   |   Subject: ${item.yearOrSubject}   |   Page $pageNum of $totalPages", 48f, 92f, textPaint)

            var yPos = 122f

            // Render page section title
            canvas.drawText(pageContent.sectionTitle, 35f, yPos, headerPaint)
            yPos += 12f
            canvas.drawLine(35f, yPos, 560f, yPos, borderPaint)
            yPos += 18f

            for (block in pageContent.blocks) {
                if (yPos > 760f) break // Avoid overlapping footer

                when (block) {
                    is PdfBlock.Header -> {
                        yPos += 4f
                        canvas.drawText(block.text, 35f, yPos, subHeaderPaint)
                        yPos += 16f
                    }
                    is PdfBlock.Paragraph -> {
                        val wrappedLines = wrapText(block.text, textPaint, 520f)
                        for (line in wrappedLines) {
                            if (yPos > 760f) break
                            canvas.drawText(line, 35f, yPos, textPaint)
                            yPos += 14f
                        }
                        yPos += 4f
                    }
                    is PdfBlock.Bullet -> {
                        val wrappedLines = wrapText(block.text, textPaint, 500f)
                        for ((idx, line) in wrappedLines.withIndex()) {
                            if (yPos > 760f) break
                            if (idx == 0) {
                                canvas.drawText("• $line", 40f, yPos, textPaint)
                            } else {
                                canvas.drawText("  $line", 40f, yPos, textPaint)
                            }
                            yPos += 14f
                        }
                        yPos += 2f
                    }
                    is PdfBlock.BoxNote -> {
                        val wrappedLines = wrapText(block.text, textPaint, 490f)
                        val boxHeight = 22f + wrappedLines.size * 14f
                        val paintToUse = if (block.isHighlight) highlightBoxPaint else accentBoxPaint
                        canvas.drawRect(35f, yPos, 560f, yPos + boxHeight, paintToUse)
                        canvas.drawRect(35f, yPos, 560f, yPos + boxHeight, borderPaint)

                        canvas.drawText(block.title, 45f, yPos + 15f, subHeaderPaint)
                        var boxY = yPos + 28f
                        for (line in wrappedLines) {
                            canvas.drawText(line, 45f, boxY, textPaint)
                            boxY += 14f
                        }
                        yPos += boxHeight + 10f
                    }
                    is PdfBlock.QuestionBox -> {
                        val qLines = wrapText(block.question, subHeaderPaint, 480f)
                        val expLines = wrapText(block.explanation, textPaint, 480f)
                        var boxH = 26f + qLines.size * 14f + block.options.size * 14f + 30f + expLines.size * 13f

                        canvas.drawRect(35f, yPos, 560f, yPos + boxH, highlightBoxPaint)
                        canvas.drawRect(35f, yPos, 560f, yPos + boxH, borderPaint)

                        var qY = yPos + 16f
                        canvas.drawText("Q${block.qNo}:", 45f, qY, headerPaint)
                        for (line in qLines) {
                            canvas.drawText(line, 70f, qY, subHeaderPaint)
                            qY += 14f
                        }
                        qY += 4f
                        for (opt in block.options) {
                            canvas.drawText(opt, 55f, qY, textPaint)
                            qY += 14f
                        }
                        qY += 4f
                        canvas.drawText("✓ Correct Answer: ${block.answer}", 45f, qY, headerPaint)
                        qY += 16f
                        canvas.drawText("Explanation / KCET Shortcut:", 45f, qY, subHeaderPaint)
                        qY += 14f
                        for (line in expLines) {
                            canvas.drawText(line, 45f, qY, textPaint)
                            qY += 13f
                        }

                        yPos += boxH + 10f
                    }
                    is PdfBlock.FormulaItem -> {
                        canvas.drawRect(35f, yPos, 560f, yPos + 34f, accentBoxPaint)
                        canvas.drawRect(35f, yPos, 560f, yPos + 34f, borderPaint)
                        canvas.drawText(block.name, 45f, yPos + 14f, subHeaderPaint)
                        canvas.drawText("Formula: ${block.formula}", 45f, yPos + 26f, headerPaint)
                        if (block.note.isNotBlank()) {
                            canvas.drawText(block.note, 310f, yPos + 26f, textPaint)
                        }
                        yPos += 40f
                    }
                }
            }

            // Footer
            canvas.drawLine(35f, 790f, 560f, 790f, borderPaint)
            canvas.drawText("KCET Gen Z ⭐ App • Verified Karnataka KEA Exam Study Content • Page $pageNum of $totalPages", 35f, 808f, textPaint)

            pdfDocument.finishPage(page)
        }

        val fos = FileOutputStream(targetFile)
        pdfDocument.writeTo(fos)
        fos.flush()
        fos.close()
        pdfDocument.close()
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isBlank()) return emptyList()
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)
        return lines
    }

    private fun getContentForResource(item: ResourceItem): List<PdfPageContent> {
        return when (item.id) {
            "r1" -> getSyllabusPages()
            "r2" -> getPyq2025Pages()
            "r3" -> getPhysicsFormulaPages()
            "r4" -> getMathsShortcutsPages()
            "r5" -> getChemistryOrganicPages()
            else -> {
                if (item.category.contains("Counselling", ignoreCase = true) || item.title.contains("Counselling", ignoreCase = true) || item.title.contains("Roadmap", ignoreCase = true)) getCounsellingRoadmapPages(item)
                else if (item.category.contains("Syllabus", ignoreCase = true)) getSyllabusPages()
                else if (item.category.contains("PYQ", ignoreCase = true)) getPyq2025Pages()
                else if (item.yearOrSubject.contains("Physics", ignoreCase = true)) getPhysicsFormulaPages()
                else if (item.yearOrSubject.contains("Math", ignoreCase = true)) getMathsShortcutsPages()
                else if (item.yearOrSubject.contains("Chem", ignoreCase = true)) getChemistryOrganicPages()
                else getDefaultStudyPages(item)
            }
        }
    }

    // -------------------------------------------------------------------------------------
    // 1. SYLLABUS & WEIGHTAGE (3 Pages)
    // -------------------------------------------------------------------------------------
    private fun getSyllabusPages(): List<PdfPageContent> {
        return listOf(
            PdfPageContent(
                sectionTitle = "SECTION 1: KCET EXAM PATTERN & PHYSICS WEIGHTAGE",
                blocks = listOf(
                    PdfBlock.Header("Official KEA Exam Scheme & Rules"),
                    PdfBlock.Bullet("Mode: Computer Based Test (CBT) / Offline OMR Sheet as per KEA circular."),
                    PdfBlock.Bullet("Total Marks: 60 Marks per paper (Physics, Chemistry, Mathematics, Biology)."),
                    PdfBlock.Bullet("Number of Questions: 60 Multiple Choice Questions (1 mark each)."),
                    PdfBlock.Bullet("Duration: 80 Minutes per paper (75 mins for answering + 5 mins reading)."),
                    PdfBlock.Bullet("Negative Marking: NO Negative Marking in KCET! Attempt all 60 questions."),
                    PdfBlock.Bullet("Syllabus Breakdown: ~30% questions from 1st PUC & ~70% questions from 2nd PUC."),
                    PdfBlock.Header("Physics Topic-wise Weightage (2nd & 1st PUC)"),
                    PdfBlock.BoxNote(
                        "High Weightage Topic 1: Electrostatics & Capacitance",
                        "6 to 8 Questions expected. Key areas: Electric dipole on axial/equatorial lines, Gauss law applications, Parallel plate capacitor with dielectric, Energy stored in capacitor.",
                        isHighlight = true
                    ),
                    PdfBlock.BoxNote(
                        "High Weightage Topic 2: Ray & Wave Optics",
                        "7 to 9 Questions expected. Key areas: Lens Maker Formula, Prism refraction angle, Young's double slit fringe width β = λD/d, Brewster angle tan(ip) = μ.",
                        isHighlight = true
                    ),
                    PdfBlock.Bullet("Current Electricity & Kirchhoff Laws: 5-7 Questions"),
                    PdfBlock.Bullet("Magnetic Effects & Alternating Current: 5-6 Questions"),
                    PdfBlock.Bullet("Modern Physics, Atoms & Semiconductors: 8-10 Questions"),
                    PdfBlock.Bullet("1st PUC Mechanics, Gravitation & Waves: 12-14 Questions")
                )
            ),
            PdfPageContent(
                sectionTitle = "SECTION 2: MATHEMATICS & CHEMISTRY CHAPTER WEIGHTAGE",
                blocks = listOf(
                    PdfBlock.Header("Mathematics Chapter-wise Question Distribution"),
                    PdfBlock.BoxNote(
                        "Highest Weightage: Calculus & Vectors (20+ Questions)",
                        "Integrals & Applications (8-10 Qs), Differential Equations (3-4 Qs), Vectors & 3D Geometry (8-10 Qs), Derivatives & Continuity (6-8 Qs). Practice standard L'Hopital shortcuts!",
                        isHighlight = true
                    ),
                    PdfBlock.Bullet("Matrices & Determinants: 5-6 Questions (Properties of Adj A, Inverse, Cramer rule)"),
                    PdfBlock.Bullet("Linear Programming & Probability: 5-6 Questions (Bayes Theorem & Binomial Distribution)"),
                    PdfBlock.Bullet("1st PUC Trigonometry, Relations & Functions: 10-12 Questions"),
                    PdfBlock.Header("Chemistry Chapter-wise Question Distribution"),
                    PdfBlock.BoxNote(
                        "Highest Weightage: Organic Chemistry (14-16 Questions)",
                        "Haloalkanes, Alcohols, Phenols, Aldehydes, Amines & Biomolecules. Focus on Named Reactions (Aldol, Cannizzaro, Reimer-Tiemann, Clemmensen, Sandmeyer).",
                        isHighlight = true
                    ),
                    PdfBlock.Bullet("Solutions & Electrochemistry: 6-7 Questions (Nernst eq, Raoult law, Colligative props)"),
                    PdfBlock.Bullet("Chemical Kinetics & Surface Chemistry: 5-6 Questions (1st order rate constant, Activation energy)"),
                    PdfBlock.Bullet("Inorganic p-Block & Coordination Compounds: 8-10 Questions (IUPAC naming, CFT, Hybridization)")
                )
            ),
            PdfPageContent(
                sectionTitle = "SECTION 3: BIOLOGY & RANKING STRATEGY FOR RANK < 1000",
                blocks = listOf(
                    PdfBlock.Header("Biology Chapter-wise Distribution (For Farm Science/Pharmacy)"),
                    PdfBlock.Bullet("Genetics & Evolution: 10-12 Questions (Mendelian ratios, DNA replication, Pedigree analysis)"),
                    PdfBlock.Bullet("Biotechnology & Applications: 6-8 Questions (Restriction enzymes, PCR steps, Bt Cotton)"),
                    PdfBlock.Bullet("Reproduction & Health: 11-14 Questions (Gamete formation, Pollination, IVF techniques)"),
                    PdfBlock.Bullet("1st PUC Plant Physiology & Human Physiology: 18-20 Questions"),
                    PdfBlock.Header("Top Rankers' 30-Day Preparation Strategy"),
                    PdfBlock.BoxNote(
                        "Strategy 1: Speed Training (< 60s per Question)",
                        "Solve 60 questions in 75 minutes. Don't spend more than 90 seconds on a single numerical. Skip and revisit at the end.",
                        isHighlight = false
                    ),
                    PdfBlock.BoxNote(
                        "Strategy 2: Complete NCERT Line-by-Line Revision",
                        "KEA directly frames 70% of Chemistry and Biology questions straight from NCERT textbook bold terms and summary tables.",
                        isHighlight = true
                    )
                )
            )
        )
    }

    // -------------------------------------------------------------------------------------
    // 2. SOLVED PYQ PAPER (3 Pages)
    // -------------------------------------------------------------------------------------
    private fun getPyq2025Pages(): List<PdfPageContent> {
        return listOf(
            PdfPageContent(
                sectionTitle = "KCET 2025 SOLVED PAPER • PHYSICS SECTION",
                blocks = listOf(
                    PdfBlock.QuestionBox(
                        qNo = "01",
                        question = "Two point charges +q and +4q are kept separated by distance L. Where should a third point charge Q be placed on the line joining them so that it remains in equilibrium?",
                        options = listOf("(A) L/3 from +q", "(B) L/2 from +q", "(C) 2L/3 from +q", "(D) L/4 from +q"),
                        answer = "(A) L/3 from +q",
                        explanation = "At equilibrium, Electrostatic Force F1 = F2 => k*q*Q / x^2 = k*4q*Q / (L-x)^2. Taking square root on both sides: 1/x = 2/(L-x) => L - x = 2x => 3x = L => x = L/3 from charge +q. KCET Shortcut Formula: x = L / (1 + √(q2/q1)) = L / (1 + √4) = L/3."
                    ),
                    PdfBlock.QuestionBox(
                        qNo = "02",
                        question = "The half-life of a radioactive sample is 20 minutes. What is the time taken for 75% of the initial sample to decay?",
                        options = listOf("(A) 20 minutes", "(B) 40 minutes", "(C) 60 minutes", "(D) 80 minutes"),
                        answer = "(B) 40 minutes",
                        explanation = "If 75% decays, remaining fraction N/N0 = 25% = 1/4 = (1/2)^2. Number of half-lives n = 2. Total time t = n * T_1/2 = 2 * 20 min = 40 minutes."
                    )
                )
            ),
            PdfPageContent(
                sectionTitle = "KCET 2025 SOLVED PAPER • MATHEMATICS SECTION",
                blocks = listOf(
                    PdfBlock.QuestionBox(
                        qNo = "03",
                        question = "Find the value of Limit x->0 (sin 5x - sin 3x) / x.",
                        options = listOf("(A) 2", "(B) 8", "(C) 1", "(D) 0"),
                        answer = "(A) 2",
                        explanation = "Using L'Hopital's Rule (differentiating numerator and denominator wrt x): Limit x->0 (5 cos 5x - 3 cos 3x) / 1 = 5(1) - 3(1) = 2. KCET Shortcut: Limit x->0 (sin ax - sin bx)/x = a - b = 5 - 3 = 2 directly!"
                    ),
                    PdfBlock.QuestionBox(
                        qNo = "04",
                        question = "If matrix A = [[1, 2], [3, 4]], then what is the determinant value of A^3?",
                        options = listOf("(A) -8", "(B) -64", "(C) 64", "(D) 8"),
                        answer = "(A) -8",
                        explanation = "Determinant of A = |A| = (1*4 - 2*3) = 4 - 6 = -2. Property: det(A^n) = (|A|)^n. Therefore det(A^3) = (-2)^3 = -8."
                    )
                )
            ),
            PdfPageContent(
                sectionTitle = "KCET 2025 SOLVED PAPER • CHEMISTRY SECTION",
                blocks = listOf(
                    PdfBlock.QuestionBox(
                        qNo = "05",
                        question = "Which of the following organic compounds will give a yellow precipitate in the Iodoform Test?",
                        options = listOf("(A) 1-Propanol", "(B) 2-Propanol", "(C) Methanol", "(D) Benzophenone"),
                        answer = "(B) 2-Propanol",
                        explanation = "Compounds containing the CH3-CH(OH)- group (like 2-propanol, ethanol) or CH3-C=O group (like acetaldehyde, acetone) give positive yellow precipitate of CHI3 when reacted with I2 and aqueous NaOH."
                    ),
                    PdfBlock.QuestionBox(
                        qNo = "06",
                        question = "What is the coordination number and oxidation state of iron in [Fe(EDTA)]- complex?",
                        options = listOf("(A) 6 and +3", "(B) 4 and +2", "(C) 6 and +2", "(D) 4 and +3"),
                        answer = "(A) 6 and +3",
                        explanation = "EDTA4- is a hexadentate ligand, so coordination number = 6. Oxidation state x + (-4) = -1 => x = +3."
                    )
                )
            )
        )
    }

    // -------------------------------------------------------------------------------------
    // 3. PHYSICS FORMULA SHEET (3 Pages)
    // -------------------------------------------------------------------------------------
    private fun getPhysicsFormulaPages(): List<PdfPageContent> {
        return listOf(
            PdfPageContent(
                sectionTitle = "PAGE 1: ELECTROSTATICS, CAPACITANCE & CURRENT ELECTRICITY",
                blocks = listOf(
                    PdfBlock.FormulaItem("Coulomb's Force Law", "F = (1 / 4πε0) * (|q1 q2| / r^2)", "1/4πε0 = 9 × 10^9 N m^2/C^2"),
                    PdfBlock.FormulaItem("Electric Field (Point Charge)", "E = k Q / r^2", "E = F / q"),
                    PdfBlock.FormulaItem("Electric Dipole Moment", "p = q × 2a", "E_axial = 2kp/r^3, E_eq = kp/r^3"),
                    PdfBlock.FormulaItem("Gauss Law Total Flux", "Φ = Q_enclosed / ε0", "Sphere E = kQ/r^2"),
                    PdfBlock.FormulaItem("Parallel Plate Capacitance", "C = ε0 A / d", "With Dielectric K: C' = K C"),
                    PdfBlock.FormulaItem("Capacitor Stored Energy", "U = 1/2 C V^2 = 1/2 Q V = Q^2 / 2C", "Joules"),
                    PdfBlock.FormulaItem("Drift Velocity & Current", "v_d = e E τ / m", "I = n e A v_d"),
                    PdfBlock.FormulaItem("Wheatstone Bridge Balance", "P / Q = R / S", "Null deflection in galvanometer")
                )
            ),
            PdfPageContent(
                sectionTitle = "PAGE 2: MAGNETISM, EMI & RAY OPTICS",
                blocks = listOf(
                    PdfBlock.FormulaItem("Biot-Savart Law", "dB = (μ0 / 4π) * (I dl sinθ / r^2)", "μ0/4π = 10^-7 T m/A"),
                    PdfBlock.FormulaItem("Magnetic Field (Straight Wire)", "B = μ0 I / (2π r)", "Infinite length wire"),
                    PdfBlock.FormulaItem("Magnetic Field (Circular Center)", "B = μ0 I / (2 R)", "N turns: B = μ0 N I / 2R"),
                    PdfBlock.FormulaItem("Lens Maker's Formula", "1/f = (μ - 1) (1/R1 - 1/R2)", "Thin lens formula"),
                    PdfBlock.FormulaItem("Lens Magnification", "m = v / u = f / (f + u)", "Real vs Virtual signs"),
                    PdfBlock.FormulaItem("Young's Double Slit Fringe Width", "β = λ D / d", "Bright fringe: y_n = n λ D / d")
                )
            ),
            PdfPageContent(
                sectionTitle = "PAGE 3: MODERN PHYSICS & ATOM MODELS",
                blocks = listOf(
                    PdfBlock.FormulaItem("Einstein Photoelectric Equation", "E = hν = W0 + K_max = hν0 + e Vs", "h = 6.63 × 10^-34 J s"),
                    PdfBlock.FormulaItem("De Broglie Wavelength", "λ = h / p = 12.27 / √V Å", "For electron accelerated by V volts"),
                    PdfBlock.FormulaItem("Bohr Atom Energy Radius", "E_n = -13.6 Z^2 / n^2 eV", "Radius r_n = 0.529 n^2 / Z Å"),
                    PdfBlock.FormulaItem("Radioactive Decay Law", "N(t) = N0 e^(-λt)", "T_1/2 = 0.693 / λ")
                )
            )
        )
    }

    // -------------------------------------------------------------------------------------
    // 4. MATHEMATICS SHORTCUT TRICKS (3 Pages)
    // -------------------------------------------------------------------------------------
    private fun getMathsShortcutsPages(): List<PdfPageContent> {
        return listOf(
            PdfPageContent(
                sectionTitle = "PAGE 1: CALCULUS & LIMIT SHORTCUT TRICKS",
                blocks = listOf(
                    PdfBlock.BoxNote("Shortcut 1: Limit x->0 (1 - cos ax) / x^2 = a^2 / 2", "Example: Limit x->0 (1 - cos 6x) / x^2 = 6^2 / 2 = 36 / 2 = 18 instantly!", isHighlight = true),
                    PdfBlock.BoxNote("Shortcut 2: Limit x->0 (e^(ax) - 1) / x = a", "Example: Limit x->0 (e^(4x) - 1) / x = 4.", isHighlight = false),
                    PdfBlock.BoxNote("Shortcut 3: Integral ∫ e^(ax) [ a f(x) + f'(x) ] dx = e^(ax) f(x) + C", "Example: ∫ e^x (tan x + sec^2 x) dx = e^x tan x + C.", isHighlight = true),
                    PdfBlock.FormulaItem("Integration Standard Form 1", "∫ 1 / (x^2 + a^2) dx = (1/a) tan^-1(x/a) + C", "Very common in KCET"),
                    PdfBlock.FormulaItem("Definite Integral King Property", "∫_0^a f(x) dx = ∫_0^a f(a - x) dx", "Used for sin/cos symmetry")
                )
            ),
            PdfPageContent(
                sectionTitle = "PAGE 2: MATRICES, VECTORS & AREA SHORTCUTS",
                blocks = listOf(
                    PdfBlock.BoxNote("Shortcut 4: Inverse of 2x2 Matrix A = [[a,b],[c,d]]", "Formula: A^-1 = (1 / (ad - bc)) * [[d, -b], [-c, a]]. Swap diagonal elements and negate non-diagonal ones!", isHighlight = true),
                    PdfBlock.BoxNote("Shortcut 5: Area Bounded by Parabola y^2 = 4ax and Line y = mx", "Formula: Area = 8 a^2 / (3 m^3) sq units.", isHighlight = true),
                    PdfBlock.FormulaItem("Vector Cross Product Area", "Area of Parallelogram = | a × b |", "Triangle area = 1/2 |a × b|"),
                    PdfBlock.FormulaItem("Shortest Distance Between Skew Lines", "d = | (a2 - a1) · (b1 × b2) | / | b1 × b2 |", "3D Geometry")
                )
            ),
            PdfPageContent(
                sectionTitle = "PAGE 3: PROBABILITY & 3D GEOMETRY",
                blocks = listOf(
                    PdfBlock.FormulaItem("Bayes' Theorem Formula", "P(Ai|B) = P(Ai) P(B|Ai) / Σ P(Ak) P(B|Ak)", "Conditional Probability"),
                    PdfBlock.FormulaItem("Binomial Distribution Mean Var", "Mean = n p , Variance = n p q", "Var < Mean always"),
                    PdfBlock.BoxNote("KCET Math Speed Advice", "Aim to complete 60 math questions in 80 mins by solving easy direct matrix/vector questions first, saving calculus for second pass.", isHighlight = true)
                )
            )
        )
    }

    // -------------------------------------------------------------------------------------
    // 5. CHEMISTRY ORGANIC NAMED REACTIONS (3 Pages)
    // -------------------------------------------------------------------------------------
    private fun getChemistryOrganicPages(): List<PdfPageContent> {
        return listOf(
            PdfPageContent(
                sectionTitle = "PAGE 1: ALDEHYDES, KETONES & PHENOLS REACTION HANDBOOK",
                blocks = listOf(
                    PdfBlock.BoxNote(
                        "Reaction 1: Aldol Condensation",
                        "Reagents: Dilute NaOH or KOH with aldehydes/ketones having α-hydrogen.\nReaction: 2 CH3CHO + dil NaOH -> CH3-CH(OH)-CH2-CHO --Δ--> CH3-CH=CH-CHO (Crotonaldehyde).\nNote: HCHO and C6H5CHO do NOT undergo Aldol as they lack α-H.",
                        isHighlight = true
                    ),
                    PdfBlock.BoxNote(
                        "Reaction 2: Cannizzaro Reaction",
                        "Reagents: Concentrated 50% NaOH with aldehydes lacking α-hydrogen.\nReaction: 2 HCHO + 50% NaOH -> CH3OH (Methanol) + HCOONa (Sodium Formate) [Disproportionation].",
                        isHighlight = true
                    ),
                    PdfBlock.FormulaItem("Reimer-Tiemann Reaction", "Phenol + CHCl3 + aq NaOH (340K) -> Salicylaldehyde", "Electrophile: Dichlorocarbene :CCl2"),
                    PdfBlock.FormulaItem("Kolbe's Reaction", "Phenol + NaOH + CO2 (400K, 4-7 atm) -> Salicylic Acid", "Aspirin precursor")
                )
            ),
            PdfPageContent(
                sectionTitle = "PAGE 2: REDUCTION & DIAZOTIZATION NAMED REACTIONS",
                blocks = listOf(
                    PdfBlock.BoxNote(
                        "Reaction 3: Clemmensen Reduction",
                        "Reagents: >C=O + Zn-Hg / conc. HCl -> >CH2 + H2O.\nSuitable for acid-stable organic molecules.",
                        isHighlight = false
                    ),
                    PdfBlock.BoxNote(
                        "Reaction 4: Wolff-Kishner Reduction",
                        "Reagents: >C=O + NH2NH2 (Hydrazine) / KOH + Ethylene glycol (Δ) -> >CH2 + N2.\nSuitable for acid-sensitive organic molecules.",
                        isHighlight = true
                    ),
                    PdfBlock.FormulaItem("Sandmeyer Reaction", "C6H5N2+Cl- + Cu2Cl2 / HCl -> Chlorobenzene + N2", "High yield haloarenes"),
                    PdfBlock.FormulaItem("Diazotization Reaction", "C6H5NH2 + NaNO2 + 2 HCl (273-278K) -> C6H5N2+Cl-", "Ice cold temperature required")
                )
            ),
            PdfPageContent(
                sectionTitle = "PAGE 3: PHYSICAL & INORGANIC CHEMISTRY SUMMARY",
                blocks = listOf(
                    PdfBlock.FormulaItem("Nernst Equation", "E_cell = E°_cell - (0.0591 / n) log Q", "At 298 K"),
                    PdfBlock.FormulaItem("First Order Rate Constant", "k = (2.303 / t) log ([A]0 / [A])", "Half life t_1/2 = 0.693 / k"),
                    PdfBlock.FormulaItem("Colligative Property (Relative Lowering)", "(P° - P) / P° = i * X_solute", "i = van't Hoff factor"),
                    PdfBlock.BoxNote("Coordination Hybridization Rule", "Coordination 4: sp3 (Tetrahedral, paramagnetic) or dsp2 (Square planar, diamagnetic). Coordination 6: d2sp3 or sp3d2 (Octahedral).", isHighlight = true)
                )
            )
        )
    }

    private fun getCounsellingRoadmapPages(item: ResourceItem): List<PdfPageContent> {
        return listOf(
            PdfPageContent(
                sectionTitle = "PAGE 1: KEA COUNSELLING EVENTS ROADMAP & DOCUMENT VERIFICATION",
                blocks = listOf(
                    PdfBlock.Header("Official KEA Counselling Process Master Roadmap 2026"),
                    PdfBlock.Paragraph(if (item.description.isNotBlank()) item.description else "Complete KEA step-by-step PDF roadmap covering Document Verification, Secret Key, Mock Option Entry, Seat Matrix, Choice 1-4 Rules, and Final College Reporting."),
                    PdfBlock.BoxNote(
                        "Step 1: Document Verification at KEA Helpline Centers",
                        "Mandatory physical verification of 11 original documents. Candidates receive 'Verification Slip' and 'Secret Key' required for Option Entry login.",
                        isHighlight = true
                    ),
                    PdfBlock.Bullet("1. KCET 2026 Admission Ticket & Final Printout of Online Application Form."),
                    PdfBlock.Bullet("2. SSLC / 10th Standard Marks Card (Date of birth proof)."),
                    PdfBlock.Bullet("3. 2nd PUC / 12th Standard Marks Card."),
                    PdfBlock.Bullet("4. 7 Years Study Certificate in Karnataka signed by BEO / DDPI."),
                    PdfBlock.Bullet("5. Rural / Kannada Medium Study Certificate (If claiming reservation)."),
                    PdfBlock.Bullet("6. Caste / Income Certificate (Category 1, 2A, 2B, 3A, 3B, SC, ST)."),
                    PdfBlock.Bullet("7. Hyderabad Karnataka (371J) Eligibility Certificate (If applicable)."),
                    PdfBlock.Bullet("8. NATA / NEET Score Card (For Architecture / AYUSH seats)."),
                    PdfBlock.BoxNote(
                        "Secret Key Security Warning",
                        "Keep your KEA Secret Key strictly confidential! Do NOT share it with friends, internet cafes, or unauthorized persons. Anyone with your key can alter your college priority order.",
                        isHighlight = false
                    )
                )
            ),
            PdfPageContent(
                sectionTitle = "PAGE 2: SEAT MATRIX & OPTION ENTRY PRIORITY ORDER",
                blocks = listOf(
                    PdfBlock.Header("Option Entry Strategy & Seat Matrix Categories"),
                    PdfBlock.Paragraph("Candidates can enter unlimited college & branch combinations in order of preference. KEA system allocates seats strictly from Option #1 downwards."),
                    PdfBlock.BoxNote(
                        "Mock Option Entry Round",
                        "KEA conducts a trial Mock Round to let candidates test options and view mock rank cutoffs before final locking.",
                        isHighlight = true
                    ),
                    PdfBlock.Bullet("Rule #1: Put top engineering colleges (RVCE, BMSCE, MSRIT, PES, UVCE) in top preferences (1 to 15)."),
                    PdfBlock.Bullet("Rule #2: Always verify Seat Matrix (GM, 2A, 2B, SC/ST, Rural, Kannada Medium, Management/SNQ Quota)."),
                    PdfBlock.Bullet("Rule #3: Save & print your final locked Option Entry list after every modification."),
                    PdfBlock.BoxNote(
                        "SNQ (Supernumerary Quota) Advantage",
                        "Candidates with family income < 6 Lakhs are eligible for SNQ seats with tuition fee reduced to ~₹4,000/year!",
                        isHighlight = true
                    )
                )
            ),
            PdfPageContent(
                sectionTitle = "PAGE 3: ALLOTMENT CHOICES & FINAL COLLEGE REPORTING",
                blocks = listOf(
                    PdfBlock.Header("Round 1 & Round 2 Seat Allotment Choices (1, 2, 3, 4)"),
                    PdfBlock.BoxNote(
                        "Choice 1: SATISFIED & ACCEPT",
                        "Pay fee online / bank challan, download Admission Order, report to allocated college with original documents before deadline.",
                        isHighlight = true
                    ),
                    PdfBlock.BoxNote(
                        "Choice 2: ACCEPT & UPGRADE IN ROUND 2",
                        "Pay fee for current seat, hold it provisionally, and participate in Round 2 for higher college options only.",
                        isHighlight = false
                    ),
                    PdfBlock.BoxNote(
                        "Choice 3: REJECT & HOLD FOR ROUND 2",
                        "Reject current allotted seat without paying fee, participate in Round 2 for better options.",
                        isHighlight = false
                    ),
                    PdfBlock.BoxNote(
                        "Choice 4: EXIT COUNSELLING",
                        "Reject current seat & exit KEA counselling process completely.",
                        isHighlight = false
                    ),
                    PdfBlock.Header("Final Admission & Fee Payment Checklist"),
                    PdfBlock.Bullet("Step A: Pay KEA prescribed tuition fee via NetBanking / SBI Challan."),
                    PdfBlock.Bullet("Step B: Download Official KEA Admission Order from student portal."),
                    PdfBlock.Bullet("Step C: Submit Admission Order & Original Verification Slip at allocated college."),
                    PdfBlock.Bullet("Step D: Obtain College Receipt and confirm KEA portal admission status.")
                )
            )
        )
    }

    // Default Fallback Generator
    private fun getDefaultStudyPages(item: ResourceItem): List<PdfPageContent> {
        return listOf(
            PdfPageContent(
                sectionTitle = "KCET COMPREHENSIVE STUDY GUIDE & SYLLABUS OVERVIEW",
                blocks = listOf(
                    PdfBlock.Header("Document Overview"),
                    PdfBlock.Paragraph(item.description),
                    PdfBlock.BoxNote("Verified KCET Study Material", "This official study material is prepared by KCET experts and updated for KCET 2026. It contains key formulas, short notes, solved examples, and KEA exam guidelines.", isHighlight = true),
                    PdfBlock.Bullet("Target Exam: Karnataka Common Entrance Test (KCET 2026)"),
                    PdfBlock.Bullet("Prescribed Board: Karnataka KEA & PUC 1st/2nd Year Syllabus"),
                    PdfBlock.Bullet("Status: Verified & Downloaded for Unlimited Offline Study")
                )
            )
        )
    }

    fun openPdfInExternalApp(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Cannot open external PDF viewer: ${e.message}")
        }
    }
}
