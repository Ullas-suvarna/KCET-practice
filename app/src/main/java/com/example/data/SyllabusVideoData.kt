package com.example.data

data class SyllabusChapter(
    val id: String,
    val subject: SubjectType,
    val classLevel: String, // "Class 11 (1st PUC)" or "Class 12 (2nd PUC)"
    val chapterName: String,
    val weightageMarks: String, // e.g. "3 - 4 Marks"
    val isHighYield: Boolean = false,
    val synopsisPoints: List<String>,
    val videos: List<SyllabusVideo>
)

enum class SubjectType(val displayName: String, val iconHexColor: String) {
    PHYSICS("Physics", "#4F46E5"),
    CHEMISTRY("Chemistry", "#059669"),
    MATHEMATICS("Mathematics", "#D97706"),
    BIOLOGY("Biology", "#DC2626")
}

enum class VideoCategory(val displayName: String) {
    ALL("All Videos"),
    CHAPTER_FULL("Chapter Synopsis & Q&A"),
    ONE_SHOT("One-Shot Revision"),
    MARATHON("Marathon & PYQs")
}

data class SyllabusVideo(
    val id: String,
    val title: String,
    val youtubeVideoId: String,
    val duration: String,
    val channelName: String,
    val category: VideoCategory,
    val viewsCount: String,
    val description: String,
    val topicsDiscussed: List<String>
)

object SyllabusVideoRepository {

    fun getSyllabusChapters(): List<SyllabusChapter> {
        return listOf(
            // ==================== MATHEMATICS ====================
            SyllabusChapter(
                id = "math_01",
                subject = SubjectType.MATHEMATICS,
                classLevel = "Class 12 (2nd PUC)",
                chapterName = "Calculus - Integrals & Applications",
                weightageMarks = "6 - 8 Marks",
                isHighYield = true,
                synopsisPoints = listOf(
                    "Standard indefinite integration rules and substitutions",
                    "Definite integrals properties and symmetry reduction",
                    "Area under simple curves, lines, parabolas, and circles",
                    "KCET Trick: ∫ e^x [f(x) + f'(x)] dx = e^x f(x) + C"
                ),
                videos = listOf(
                    SyllabusVideo(
                        id = "vid_math_01_oneshot",
                        title = "Integration & Area Under Curves - Complete One-Shot Revision for KCET",
                        youtubeVideoId = "3yL4qS3_3s0", // Representative educational video ID
                        duration = "1h 45m",
                        channelName = "Simplifying KCET Maths",
                        category = VideoCategory.ONE_SHOT,
                        viewsCount = "142K views",
                        description = "Complete single video coverage of Indefinite & Definite Integrals with 30 second KCET shortcut tricks.",
                        topicsDiscussed = listOf("Integration by parts shortcut", "Definite Integral properties", "Area bounded by curves")
                    ),
                    SyllabusVideo(
                        id = "vid_math_01_full",
                        title = "Integral Calculus - Full Chapter Synopsis & 50 Top KCET Questions",
                        youtubeVideoId = "9bZkp7q19f0",
                        duration = "2h 10m",
                        channelName = "KCET Master Academy",
                        category = VideoCategory.CHAPTER_FULL,
                        viewsCount = "98K views",
                        description = "Detailed line-by-line concept explanation of integration along with previous 10 years KCET solved MCQs.",
                        topicsDiscussed = listOf("Substitution Method", "Partial Fractions", "Definite Integral limits")
                    ),
                    SyllabusVideo(
                        id = "vid_math_01_marathon",
                        title = "Mathematics 500 PYQs Marathon - Calculus & Differential Equations",
                        youtubeVideoId = "dQw4w9WgXcQ",
                        duration = "3h 30m",
                        channelName = "Vedantu Kannada KCET",
                        category = VideoCategory.MARATHON,
                        viewsCount = "215K views",
                        description = "Non-stop live marathon solving 100+ calculus MCQs from 2015 to 2025 KCET papers.",
                        topicsDiscussed = listOf("Order & Degree of ODE", "Homogeneous Differential Equations", "KCET PYQ Speed Run")
                    )
                )
            ),
            SyllabusChapter(
                id = "math_02",
                subject = SubjectType.MATHEMATICS,
                classLevel = "Class 12 (2nd PUC)",
                chapterName = "Matrices & Determinants",
                weightageMarks = "4 - 5 Marks",
                isHighYield = true,
                synopsisPoints = listOf(
                    "Properties of Determinants and Adjoint of Matrix",
                    "Inverse of Matrix using A⁻¹ = (1 / |A|) adj(A)",
                    "System of linear equations consistency using Cramer's Rule",
                    "KCET Trick: |adj(A)| = |A|ⁿ⁻¹ for n×n matrix"
                ),
                videos = listOf(
                    SyllabusVideo(
                        id = "vid_math_02_oneshot",
                        title = "Matrices & Determinants One-Shot Crash Course | KCET Shortcuts",
                        youtubeVideoId = "kJQP7kiw5Fk",
                        duration = "1h 15m",
                        channelName = "Unacademy KCET Maths",
                        category = VideoCategory.ONE_SHOT,
                        viewsCount = "180K views",
                        description = "Master all determinant properties and 2x2 / 3x3 inverse shortcuts in 75 minutes.",
                        topicsDiscussed = listOf("Properties of Determinants", "Adjoint & Inverse Shortcut", "Solving Linear Equations")
                    ),
                    SyllabusVideo(
                        id = "vid_math_02_full",
                        title = "Matrices & Determinants - Synopsis + 40 MCQs Discussion",
                        youtubeVideoId = "fJ9rUzIMcZQ",
                        duration = "1h 50m",
                        channelName = "Physics Wallah Kannada",
                        category = VideoCategory.CHAPTER_FULL,
                        viewsCount = "112K views",
                        description = "Complete concept walkthrough with step-by-step NCERT + KCET previous year question solutions.",
                        topicsDiscussed = listOf("Types of Matrices", "Symmetric & Skew Symmetric", "Cramer's Rule")
                    )
                )
            ),
            // ==================== 1ST PUC (CLASS 11) MATHEMATICS ====================
            SyllabusChapter(
                id = "math_puc1_01",
                subject = SubjectType.MATHEMATICS,
                classLevel = "Class 11 (1st PUC)",
                chapterName = "Sets, Relations & Functions",
                weightageMarks = "4 - 5 Marks",
                isHighYield = true,
                synopsisPoints = listOf(
                    "Types of Sets, Subset, Power Set, and Venn Diagrams",
                    "Operations on Sets: Union, Intersection, Difference, and Complement",
                    "Cartesian product A × B and Number of relations = 2^(m×n)",
                    "Domain, Range, and Co-domain of Relations and Functions"
                ),
                videos = listOf(
                    SyllabusVideo(
                        id = "vid_math_puc1_01_oneshot",
                        title = "Sets, Relations & Functions 1st PUC One-Shot Complete Class for KCET",
                        youtubeVideoId = "kJQP7kiw5Fk",
                        duration = "1h 25m",
                        channelName = "Simplifying KCET Maths",
                        category = VideoCategory.ONE_SHOT,
                        viewsCount = "168K views",
                        description = "Complete 1st PUC Sets and Relations fast revision with Venn diagram shortcuts and KCET MCQs.",
                        topicsDiscussed = listOf("Venn Diagrams", "Number of Relations Formula", "Domain and Range")
                    ),
                    SyllabusVideo(
                        id = "vid_math_puc1_01_full",
                        title = "1st PUC Sets & Functions - Full Chapter Synopsis + 40 KCET Solved MCQs",
                        youtubeVideoId = "fJ9rUzIMcZQ",
                        duration = "1h 50m",
                        channelName = "KCET Master Academy",
                        category = VideoCategory.CHAPTER_FULL,
                        viewsCount = "115K views",
                        description = "In-depth chapter summary and step-by-step problem solving for Class 11 mathematics.",
                        topicsDiscussed = listOf("Power Sets", "De Morgan's Laws", "Composite Functions")
                    )
                )
            ),
            SyllabusChapter(
                id = "math_puc1_02",
                subject = SubjectType.MATHEMATICS,
                classLevel = "Class 11 (1st PUC)",
                chapterName = "Limits & Derivatives (1st PUC Calculus)",
                weightageMarks = "5 - 6 Marks",
                isHighYield = true,
                synopsisPoints = listOf(
                    "Standard Limit: lim (x->a) (xⁿ - aⁿ)/(x - a) = n·aⁿ⁻¹",
                    "Trigonometric Limits: lim (x->0) (sin x)/x = 1 and lim (x->0) (tan x)/x = 1",
                    "L'Hopital's Rule for 0/0 and ∞/∞ indeterminate forms in KCET",
                    "First Principle of Derivative and Product / Quotient Rule"
                ),
                videos = listOf(
                    SyllabusVideo(
                        id = "vid_math_puc1_02_oneshot",
                        title = "Limits & Derivatives Class 11 One-Shot Crash Course | KCET L'Hopital Trick",
                        youtubeVideoId = "L_LUpnjgPso",
                        duration = "1h 40m",
                        channelName = "Vedantu Kannada KCET",
                        category = VideoCategory.ONE_SHOT,
                        viewsCount = "205K views",
                        description = "Master all limit tricks, L'Hopital shortcuts, and basic differentiation rules for KCET.",
                        topicsDiscussed = listOf("L'Hopital Rule Shortcut", "Trigonometric Limits", "Derivative Product Rule")
                    ),
                    SyllabusVideo(
                        id = "vid_math_puc1_02_marathon",
                        title = "1st PUC Mathematics 200 PYQs Grand Marathon for KCET",
                        youtubeVideoId = "3yL4qS3_3s0",
                        duration = "3h 15m",
                        channelName = "Unacademy KCET Maths",
                        category = VideoCategory.MARATHON,
                        viewsCount = "240K views",
                        description = "Complete Class 11 Maths marathon covering Sets, Trigonometry, Permutations, and Limits.",
                        topicsDiscussed = listOf("Limits and Derivatives", "Conic Sections", "Permutations & Combinations")
                    )
                )
            ),
            SyllabusChapter(
                id = "math_03",
                subject = SubjectType.MATHEMATICS,
                classLevel = "Class 11 (1st PUC)",
                chapterName = "Trigonometric Functions & Equations",
                weightageMarks = "4 - 5 Marks",
                isHighYield = false,
                synopsisPoints = listOf(
                    "Compound angles, double angles, and half angle formulas",
                    "General solutions of sin θ = sin α, cos θ = cos α",
                    "Properties of triangles and sine/cosine rules",
                    "KCET Speed Trick: Angle substitution method for trigonometric identities"
                ),
                videos = listOf(
                    SyllabusVideo(
                        id = "vid_math_03_oneshot",
                        title = "Trigonometry Class 11 One-Shot Fast Revision for KCET 2026",
                        youtubeVideoId = "L_LUpnjgPso",
                        duration = "1h 30m",
                        channelName = "KCET Maths Express",
                        category = VideoCategory.ONE_SHOT,
                        viewsCount = "95K views",
                        description = "All trigonometric formulas, transformation formulas, and quick elimination techniques.",
                        topicsDiscussed = listOf("Compound Angles", "Transformation Formulas", "General Solutions")
                    )
                )
            ),

            // ==================== PHYSICS ====================
            SyllabusChapter(
                id = "phy_01",
                subject = SubjectType.PHYSICS,
                classLevel = "Class 12 (2nd PUC)",
                chapterName = "Current Electricity & Electrical Circuits",
                weightageMarks = "5 - 6 Marks",
                isHighYield = true,
                synopsisPoints = listOf(
                    "Ohm's Law, Drift velocity v_d = eEτ/m, and Mobility",
                    "Temperature dependence of resistance R_T = R_0(1 + αΔT)",
                    "Kirchhoff's Laws (KCL & KVL) for circuit solving",
                    "Wheatstone bridge balance condition R1/R2 = R3/R4 and Meter Bridge"
                ),
                videos = listOf(
                    SyllabusVideo(
                        id = "vid_phy_01_oneshot",
                        title = "Current Electricity One-Shot Complete Chapter for KCET",
                        youtubeVideoId = "YQHsXMglC9A",
                        duration = "1h 55m",
                        channelName = "Physics Wallah KCET",
                        category = VideoCategory.ONE_SHOT,
                        viewsCount = "230K views",
                        description = "Complete Current Electricity in one single lecture covering drift velocity, Kirchhoff laws, and bridge circuits.",
                        topicsDiscussed = listOf("Drift Velocity", "Kirchhoff's Laws", "Potentiometer & Meter Bridge")
                    ),
                    SyllabusVideo(
                        id = "vid_phy_01_full",
                        title = "Current Electricity - Detailed Synopsis & 45 Solved KCET PYQs",
                        youtubeVideoId = "3JZ_D3ELwOQ",
                        duration = "2h 15m",
                        channelName = "Vedantu KCET Physics",
                        category = VideoCategory.CHAPTER_FULL,
                        viewsCount = "125K views",
                        description = "Deep dive into circuit reduction, cell combinations in series/parallel, and power dissipation.",
                        topicsDiscussed = listOf("Equivalent Resistance", "Cells in Series/Parallel", "Heating Effect of Current")
                    ),
                    SyllabusVideo(
                        id = "vid_phy_01_marathon",
                        title = "KCET Physics Full Electrostatics & Electricity Marathon",
                        youtubeVideoId = "C0DPdy98e4c",
                        duration = "4h 00m",
                        channelName = "Simplifying KCET Physics",
                        category = VideoCategory.MARATHON,
                        viewsCount = "310K views",
                        description = "4 hour continuous marathon solving top 150 Physics MCQs from Electrostatics to Current Electricity.",
                        topicsDiscussed = listOf("Coulomb's Law", "Capacitors", "Electric Circuits", "KCET Speed Hacks")
                    )
                )
            ),
            SyllabusChapter(
                id = "phy_02",
                subject = SubjectType.PHYSICS,
                classLevel = "Class 12 (2nd PUC)",
                chapterName = "Ray Optics & Optical Instruments",
                weightageMarks = "5 - 6 Marks",
                isHighYield = true,
                synopsisPoints = listOf(
                    "Refraction through spherical surfaces and Lens Maker's Formula",
                    "Snell's Law μ₁ sin i = μ₂ sin r and Total Internal Reflection (TIR)",
                    "Prism dispersion and minimum deviation formula μ = sin((A+D_m)/2) / sin(A/2)",
                    "Compound Microscope & Astronomical Telescope magnifying power"
                ),
                videos = listOf(
                    SyllabusVideo(
                        id = "vid_phy_02_oneshot",
                        title = "Ray Optics One-Shot Revision | KCET Physics Formula & Tricks",
                        youtubeVideoId = "9bZkp7q19f0",
                        duration = "2h 05m",
                        channelName = "KCET Physics Guru",
                        category = VideoCategory.ONE_SHOT,
                        viewsCount = "190K views",
                        description = "Master ray diagrams, lens combinations, prism deviation, and optical instrument formulas in one video.",
                        topicsDiscussed = listOf("Refraction at Spherical Surfaces", "Prism Deviation", "Microscopes & Telescopes")
                    )
                )
            ),
            // ==================== 1ST PUC (CLASS 11) PHYSICS ====================
            SyllabusChapter(
                id = "phy_puc1_01",
                subject = SubjectType.PHYSICS,
                classLevel = "Class 11 (1st PUC)",
                chapterName = "Laws of Motion & Work, Energy, Power",
                weightageMarks = "5 - 6 Marks",
                isHighYield = true,
                synopsisPoints = listOf(
                    "Newton's Laws of Motion, Momentum Conservation, and Friction μ_s & μ_k",
                    "Work-Energy Theorem: W_net = ΔK = K_final - K_initial",
                    "Conservation of Mechanical Energy and Elastic vs Inelastic Collisions",
                    "KCET Friction Shortcut: Stopping distance s = v² / (2μg)"
                ),
                videos = listOf(
                    SyllabusVideo(
                        id = "vid_phy_puc1_01_oneshot",
                        title = "Laws of Motion & Work Energy Power 1st PUC One-Shot Complete Class",
                        youtubeVideoId = "YQHsXMglC9A",
                        duration = "2h 10m",
                        channelName = "Physics Wallah KCET",
                        category = VideoCategory.ONE_SHOT,
                        viewsCount = "220K views",
                        description = "Complete Class 11 mechanics revision: Pulley systems, friction, work-energy theorem, and collisions.",
                        topicsDiscussed = listOf("Pulley System Shortcuts", "Work-Energy Theorem", "Collision Coefficient of Restitution")
                    ),
                    SyllabusVideo(
                        id = "vid_phy_puc1_01_full",
                        title = "1st PUC Mechanics Synopsis + Top 45 KCET Solved MCQs",
                        youtubeVideoId = "3JZ_D3ELwOQ",
                        duration = "1h 45m",
                        channelName = "Simplifying KCET Physics",
                        category = VideoCategory.CHAPTER_FULL,
                        viewsCount = "135K views",
                        description = "Detailed concept breakdown of Newton's laws, bank angle on roads, and potential energy curves.",
                        topicsDiscussed = listOf("Free Body Diagrams", "Banking of Roads", "Conservative Forces")
                    )
                )
            ),
            SyllabusChapter(
                id = "phy_puc1_02",
                subject = SubjectType.PHYSICS,
                classLevel = "Class 11 (1st PUC)",
                chapterName = "Thermodynamics & Waves / Oscillations",
                weightageMarks = "4 - 5 Marks",
                isHighYield = false,
                synopsisPoints = listOf(
                    "First Law of Thermodynamics ΔQ = ΔU + W and Isothermal vs Adiabatic PV^γ = C",
                    "Carnot Engine Efficiency η = 1 - T₂/T₁",
                    "Simple Harmonic Motion (SHM) Time Period T = 2π √(m/k) and Pendulum T = 2π √(L/g)",
                    "Doppler Effect frequency shift formula f' = f ((v ± v_o) / (v ∓ v_s))"
                ),
                videos = listOf(
                    SyllabusVideo(
                        id = "vid_phy_puc1_02_oneshot",
                        title = "1st PUC Thermodynamics & Waves One-Shot Revision for KCET 2026",
                        youtubeVideoId = "C0DPdy98e4c",
                        duration = "1h 50m",
                        channelName = "Vedantu KCET Physics",
                        category = VideoCategory.ONE_SHOT,
                        viewsCount = "175K views",
                        description = "Quick formulas for Carnot engine, heat capacities C_p - C_v = R, and wave speed calculations.",
                        topicsDiscussed = listOf("First & Second Law Thermodynamics", "Carnot Engine", "SHM & Doppler Effect")
                    )
                )
            ),

            // ==================== CHEMISTRY ====================
            SyllabusChapter(
                id = "chem_01",
                subject = SubjectType.CHEMISTRY,
                classLevel = "Class 12 (2nd PUC)",
                chapterName = "Electrochemistry & Chemical Kinetics",
                weightageMarks = "6 - 7 Marks",
                isHighYield = true,
                synopsisPoints = listOf(
                    "Nernst Equation: E_cell = E°_cell - (0.0591/n) log Q at 298K",
                    "Kohlrausch's Law of independent migration of ions",
                    "Faraday's Laws of Electrolysis: W = Z · I · t",
                    "Order vs Molecularity of Reaction and First order integrated rate law k = (2.303/t) log(A₀/A)"
                ),
                videos = listOf(
                    SyllabusVideo(
                        id = "vid_chem_01_oneshot",
                        title = "Electrochemistry & Chemical Kinetics One-Shot Crash Course for KCET",
                        youtubeVideoId = "fJ9rUzIMcZQ",
                        duration = "2h 10m",
                        channelName = "Vedantu Kannada Chemistry",
                        category = VideoCategory.ONE_SHOT,
                        viewsCount = "210K views",
                        description = "Complete physical chemistry revision focusing on Nernst equation calculations and half-life formulas.",
                        topicsDiscussed = listOf("Nernst Equation", "Kohlrausch Law", "First Order Reaction Half Life")
                    ),
                    SyllabusVideo(
                        id = "vid_chem_01_full",
                        title = "Electrochemistry - Synopsis & Top 40 KCET Numerical MCQs",
                        youtubeVideoId = "3JZ_D3ELwOQ",
                        duration = "1h 40m",
                        channelName = "KCET Chem Master",
                        category = VideoCategory.CHAPTER_FULL,
                        viewsCount = "105K views",
                        description = "Line by line NCERT chemistry key points and numerical question tricks.",
                        topicsDiscussed = listOf("Standard Reduction Potential", "Conductance & Molar Conductivity", "Batteries & Fuel Cells")
                    )
                )
            ),
            SyllabusChapter(
                id = "chem_02",
                subject = SubjectType.CHEMISTRY,
                classLevel = "Class 12 (2nd PUC)",
                chapterName = "Organic Chemistry - Haloalkanes & Organic Mechanisms",
                weightageMarks = "5 - 6 Marks",
                isHighYield = true,
                synopsisPoints = listOf(
                    "SN1 vs SN2 mechanism, stereochemistry, and carbocation stability",
                    "Markovnikov & Anti-Markovnikov addition rule for unsymmetrical alkenes",
                    "Name reactions: Wurtz, Fittig, Swarts, Finkelstein, and Reimer-Tiemann",
                    "Acidic strength of phenols and basic strength of amines in aqueous medium"
                ),
                videos = listOf(
                    SyllabusVideo(
                        id = "vid_chem_02_oneshot",
                        title = "Complete Organic Chemistry Name Reactions & Mechanisms One-Shot | KCET",
                        youtubeVideoId = "L_LUpnjgPso",
                        duration = "2h 30m",
                        channelName = "Unacademy Organic Chem",
                        category = VideoCategory.ONE_SHOT,
                        viewsCount = "270K views",
                        description = "All 25 mandatory NCERT name reactions, SN1/SN2 tricks, and acidic/basic order short charts.",
                        topicsDiscussed = listOf("SN1 & SN2 Mechanisms", "NCERT Name Reactions", "Acidic & Basic Orders")
                    )
                )
            ),
            // ==================== 1ST PUC (CLASS 11) CHEMISTRY ====================
            SyllabusChapter(
                id = "chem_puc1_01",
                subject = SubjectType.CHEMISTRY,
                classLevel = "Class 11 (1st PUC)",
                chapterName = "Chemical Bonding, Structure & Thermodynamics",
                weightageMarks = "6 - 7 Marks",
                isHighYield = true,
                synopsisPoints = listOf(
                    "VSEPR Theory shapes and Hybridization (sp, sp², sp³, sp³d, sp³d²)",
                    "Molecular Orbital Theory (MOT): Bond Order = 1/2 (N_b - N_a)",
                    "Hess's Law of Constant Heat Summation and First/Second Law Thermodynamics",
                    "Gibbs Free Energy ΔG = ΔH - TΔS and Spontaneity criteria ΔG < 0"
                ),
                videos = listOf(
                    SyllabusVideo(
                        id = "vid_chem_puc1_01_oneshot",
                        title = "Chemical Bonding & Thermodynamics 1st PUC One-Shot for KCET",
                        youtubeVideoId = "fJ9rUzIMcZQ",
                        duration = "2h 15m",
                        channelName = "Vedantu Kannada Chemistry",
                        category = VideoCategory.ONE_SHOT,
                        viewsCount = "215K views",
                        description = "Master hybridization shortcuts, MOT bond order tricks, and ΔG calculation for KCET Chemistry.",
                        topicsDiscussed = listOf("Hybridization Trick", "MOT Bond Order", "Gibbs Free Energy Calculations")
                    ),
                    SyllabusVideo(
                        id = "vid_chem_puc1_01_full",
                        title = "1st PUC Chemical Bonding - Full NCERT Synopsis + 40 MCQs",
                        youtubeVideoId = "3JZ_D3ELwOQ",
                        duration = "1h 40m",
                        channelName = "KCET Chem Master",
                        category = VideoCategory.CHAPTER_FULL,
                        viewsCount = "120K views",
                        description = "Step-by-step molecular structure drawing and dipole moment analysis.",
                        topicsDiscussed = listOf("Dipole Moment", "VSEPR Shapes", "Hydrogen Bonding")
                    )
                )
            ),
            SyllabusChapter(
                id = "chem_puc1_02",
                subject = SubjectType.CHEMISTRY,
                classLevel = "Class 11 (1st PUC)",
                chapterName = "Basic Concepts of Organic Chemistry & Hydrocarbons",
                weightageMarks = "5 - 6 Marks",
                isHighYield = true,
                synopsisPoints = listOf(
                    "IUPAC Nomenclature rules, Inductive effect, Resonance (+R/-R), and Hyperconjugation",
                    "Stability order of Carbocations (3° > 2° > 1°) and Free Radicals",
                    "Alkanes (Wurtz Reaction), Alkenes (Ozonlysis & Markovnikov rule), and Alkynes",
                    "Aromaticity: Huckel's Rule (4n + 2) π electrons and Electrophilic Substitution"
                ),
                videos = listOf(
                    SyllabusVideo(
                        id = "vid_chem_puc1_02_oneshot",
                        title = "General Organic Chemistry (GOC) & Hydrocarbons 1st PUC One-Shot",
                        youtubeVideoId = "L_LUpnjgPso",
                        duration = "2h 25m",
                        channelName = "Unacademy Organic Chem",
                        category = VideoCategory.ONE_SHOT,
                        viewsCount = "260K views",
                        description = "Complete Class 11 Organic Chemistry crash course covering IUPAC, GOC effects, and Hydrocarbon reactions.",
                        topicsDiscussed = listOf("GOC Effects & Stability", "IUPAC Naming Shortcuts", "Ozonolysis Trick")
                    )
                )
            ),

            // ==================== BIOLOGY ====================
            SyllabusChapter(
                id = "bio_01",
                subject = SubjectType.BIOLOGY,
                classLevel = "Class 12 (2nd PUC)",
                chapterName = "Genetics & Evolution - Principles of Inheritance",
                weightageMarks = "7 - 8 Marks",
                isHighYield = true,
                synopsisPoints = listOf(
                    "Mendelian ratios: Monohybrid (3:1 / 1:2:1) and Dihybrid (9:3:3:1)",
                    "Incomplete dominance, Codominance (ABO blood grouping), and Pleiotropy",
                    "Chromosomal disorders: Down Syndrome (21 trisomy), Klinefelter (XXY), Turner (XO)",
                    "DNA Structure: Watson-Crick double helix, replication, transcription, and translation"
                ),
                videos = listOf(
                    SyllabusVideo(
                        id = "vid_bio_01_oneshot",
                        title = "Genetics & Molecular Basis of Inheritance One-Shot for KCET Biology",
                        youtubeVideoId = "YQHsXMglC9A",
                        duration = "2h 20m",
                        channelName = "KCET Biology Express",
                        category = VideoCategory.ONE_SHOT,
                        viewsCount = "195K views",
                        description = "Master genetics ratios, pedigree analysis, DNA replication, and Lac Operon in 140 minutes.",
                        topicsDiscussed = listOf("Mendelian Ratios & Pedigree Analysis", "Genetic Code & Lac Operon", "Chromosomal Disorders")
                    ),
                    SyllabusVideo(
                        id = "vid_bio_01_full",
                        title = "Principles of Inheritance - Full NCERT Line-by-Line & 60 KCET MCQs",
                        youtubeVideoId = "kJQP7kiw5Fk",
                        duration = "1h 50m",
                        channelName = "Physics Wallah Biology",
                        category = VideoCategory.CHAPTER_FULL,
                        viewsCount = "130K views",
                        description = "Complete NCERT biology diagram analysis and memory mnemonics for high score in KCET Biology.",
                        topicsDiscussed = listOf("Sex Determination", "Linkage & Recombination", "Mutations")
                    ),
                    SyllabusVideo(
                        id = "vid_bio_01_marathon",
                        title = "KCET Biology 300 MCQs Grand Marathon | Class 11 & 12 Full Syllabus",
                        youtubeVideoId = "3yL4qS3_3s0",
                        duration = "3h 45m",
                        channelName = "Vedantu Biology Kannada",
                        category = VideoCategory.MARATHON,
                        viewsCount = "290K views",
                        description = "Full syllabus grand marathon solving high yield NCERT exemplar and previous KCET biology questions.",
                        topicsDiscussed = listOf("Reproduction in Organisms", "Genetics", "Human Welfare & Biotechnology", "Ecology")
                    )
                )
            ),
            SyllabusChapter(
                id = "bio_02",
                subject = SubjectType.BIOLOGY,
                classLevel = "Class 12 (2nd PUC)",
                chapterName = "Biotechnology - Principles, Processes & Applications",
                weightageMarks = "5 - 6 Marks",
                isHighYield = true,
                synopsisPoints = listOf(
                    "Restriction enzymes (molecular scissors), EcoRI recognition site 5'-GAATTC-3'",
                    "Gel Electrophoresis, Plasmids (pBR322 selectable markers ampR, tetR)",
                    "Polymerase Chain Reaction (PCR) steps: Denaturation, Annealing, Extension",
                    "Bt Cotton (cryIAc, cryIIAb genes) and RNA interference (RNAi) mechanism"
                ),
                videos = listOf(
                    SyllabusVideo(
                        id = "vid_bio_02_oneshot",
                        title = "Biotechnology Principles & Applications One-Shot KCET Biology",
                        youtubeVideoId = "3JZ_D3ELwOQ",
                        duration = "1h 35m",
                        channelName = "KCET Bio Guru",
                        category = VideoCategory.ONE_SHOT,
                        viewsCount = "165K views",
                        description = "Complete biotechnology revision with restriction digestion, PCR cycles, and GMO applications.",
                        topicsDiscussed = listOf("Restriction Enzymes", "pBR322 Diagram Tricks", "PCR & Recombinant DNA")
                    )
                )
            ),
            // ==================== 1ST PUC (CLASS 11) BIOLOGY ====================
            SyllabusChapter(
                id = "bio_puc1_01",
                subject = SubjectType.BIOLOGY,
                classLevel = "Class 11 (1st PUC)",
                chapterName = "Cell Unit of Life & Biomolecules",
                weightageMarks = "6 - 7 Marks",
                isHighYield = true,
                synopsisPoints = listOf(
                    "Prokaryotic vs Eukaryotic Cell organelles: Endoplasmic Reticulum, Golgi, Mitochondria",
                    "Fluid Mosaic Model of Plasma Membrane by Singer & Nicolson",
                    "Biomolecules: Primary & Secondary metabolites, Amino Acids, Proteins, Carbohydrates",
                    "Enzyme Action: Lock and Key model, Km value, and Competitive vs Non-competitive inhibition"
                ),
                videos = listOf(
                    SyllabusVideo(
                        id = "vid_bio_puc1_01_oneshot",
                        title = "Cell Structure & Biomolecules 1st PUC One-Shot Complete Class for KCET",
                        youtubeVideoId = "YQHsXMglC9A",
                        duration = "2h 05m",
                        channelName = "KCET Biology Express",
                        category = VideoCategory.ONE_SHOT,
                        viewsCount = "210K views",
                        description = "Complete Class 11 Cell biology and Biomolecules revision with high-yield NCERT diagrams and mnemonics.",
                        topicsDiscussed = listOf("Organelle Functions", "Fluid Mosaic Model", "Enzyme Kinetics & Inhibitors")
                    ),
                    SyllabusVideo(
                        id = "vid_bio_puc1_01_full",
                        title = "1st PUC Cell & Biomolecules Synopsis + 50 Solved NCERT Exemplar Questions",
                        youtubeVideoId = "kJQP7kiw5Fk",
                        duration = "1h 45m",
                        channelName = "Physics Wallah Biology",
                        category = VideoCategory.CHAPTER_FULL,
                        viewsCount = "140K views",
                        description = "Line-by-line NCERT analysis of cell organelles and enzyme lock & key mechanism.",
                        topicsDiscussed = listOf("Nucleus & Chromatin", "Structure of Proteins", "Secondary Metabolites List")
                    )
                )
            ),
            SyllabusChapter(
                id = "bio_puc1_02",
                subject = SubjectType.BIOLOGY,
                classLevel = "Class 11 (1st PUC)",
                chapterName = "Plant Physiology & Human Physiology Basics",
                weightageMarks = "6 - 7 Marks",
                isHighYield = true,
                synopsisPoints = listOf(
                    "Photosynthesis: Light Reaction (Z-scheme) and Dark Reaction (C3 Calvin Cycle vs C4 Hatch-Slack)",
                    "Respiration in Plants: Glycolysis (EMP Pathway) yield = 2 ATP + 2 NADH, Krebs Cycle",
                    "Plant Growth Regulators: Auxins, Gibberellins, Cytokinins, Abscisic Acid, Ethylene",
                    "Human Physiology: Neural Control (Synapse conduction), Endocrine Hormones (Pituitary & Thyroid)"
                ),
                videos = listOf(
                    SyllabusVideo(
                        id = "vid_bio_puc1_02_oneshot",
                        title = "Plant & Human Physiology 1st PUC Grand One-Shot Revision KCET 2026",
                        youtubeVideoId = "3yL4qS3_3s0",
                        duration = "2h 40m",
                        channelName = "Vedantu Biology Kannada",
                        category = VideoCategory.ONE_SHOT,
                        viewsCount = "285K views",
                        description = "Master C3/C4 pathways, Plant Hormones, and Neural Synapse transmission in 160 minutes.",
                        topicsDiscussed = listOf("C3 vs C4 Photosynthesis", "Plant Growth Regulators", "Neural Transmission & Hormones")
                    )
                )
            )
        )
    }
}
