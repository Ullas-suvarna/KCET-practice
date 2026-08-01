package com.example.data

object SampleData {

    val sampleQuestions = listOf(
        // PHYSICS
        Question(
            id = 1,
            subject = Subject.PHYSICS,
            questionText = "A particle moves in a circle of radius R with a constant speed v. What is the acceleration of the particle?",
            optionA = "Zero",
            optionB = "v² / R directed towards the center",
            optionC = "v / R directed away from the center",
            optionD = "v² / R directed tangentially",
            correctOption = Option.B,
            explanation = "In uniform circular motion, the acceleration is centripetal acceleration, which has magnitude a_c = v²/R and is directed toward the center of the circular path."
        ),
        Question(
            id = 2,
            subject = Subject.PHYSICS,
            questionText = "The dimensional formula for Universal Gravitational Constant (G) is:",
            optionA = "[M⁻¹ L³ T⁻²]",
            optionB = "[M L² T⁻²]",
            optionC = "[M⁻² L³ T⁻¹]",
            optionD = "[M L T⁻²]",
            correctOption = Option.A,
            explanation = "From Newton's Law of Gravitation, F = G(m1*m2)/r². So G = F*r² / (m1*m2). Dimension = [M L T⁻²][L²] / [M²] = [M⁻¹ L³ T⁻²]."
        ),
        Question(
            id = 3,
            subject = Subject.PHYSICS,
            questionText = "An electric dipole of moment p is placed in a uniform electric field E. The torque acting on the dipole is given by:",
            optionA = "p · E",
            optionB = "p × E",
            optionC = "E × p",
            optionD = "p / E",
            correctOption = Option.B,
            explanation = "Torque on an electric dipole in a uniform electric field is τ = p × E (vector cross product), with magnitude τ = pE sin θ."
        ),
        Question(
            id = 4,
            subject = Subject.PHYSICS,
            questionText = "A wire of resistance R is stretched to double its original length keeping its volume constant. Its new resistance will be:",
            optionA = "2 R",
            optionB = "R / 2",
            optionC = "4 R",
            optionD = "R / 4",
            correctOption = Option.C,
            explanation = "Resistance R = ρ L / A. Volume V = A * L = constant. When length doubles (L' = 2L), area becomes A' = A/2. New resistance R' = ρ (2L)/(A/2) = 4 R."
        ),
        Question(
            id = 5,
            subject = Subject.PHYSICS,
            questionText = "The work done in turning a magnet of magnetic moment M through 180° from the direction of a uniform magnetic field B is:",
            optionA = "M B",
            optionB = "2 M B",
            optionC = "Zero",
            optionD = "- M B",
            correctOption = Option.B,
            explanation = "Potential energy U = - M B cos θ. W = U_final - U_initial = - M B cos(180°) - (- M B cos(0°)) = M B + M B = 2 M B."
        ),
        Question(
            id = 6,
            subject = Subject.PHYSICS,
            questionText = "In a Photoelectric Effect experiment, if the frequency of incident light is doubled, the stopping potential will:",
            optionA = "Be doubled",
            optionB = "Be halved",
            optionC = "Increase by more than double",
            optionD = "Increase by less than double",
            correctOption = Option.C,
            explanation = "Einstein's photoelectric equation: e V_0 = h ν - W. If ν' = 2ν, e V_0' = 2hν - W = 2(e V_0 + W) - W = 2 e V_0 + W. So V_0' = 2 V_0 + W/e, which is more than double."
        ),
        Question(
            id = 7,
            subject = Subject.PHYSICS,
            questionText = "A convex lens of focal length 20 cm is placed in contact with a concave lens of focal length 10 cm. The focal length of the combination is:",
            optionA = "- 20 cm",
            optionB = "+ 20 cm",
            optionC = "- 10 cm",
            optionD = "+ 30 cm",
            correctOption = Option.A,
            explanation = "1/F = 1/f1 + 1/f2 = 1/20 + 1/(-10) = 1/20 - 1/10 = -1/20. Thus F = -20 cm."
        ),

        // CHEMISTRY
        Question(
            id = 8,
            subject = Subject.CHEMISTRY,
            questionText = "Which of the following compounds exhibits optical isomerism?",
            optionA = "Lactic acid (CH3-CH(OH)-COOH)",
            optionB = "Acetic acid (CH3-COOH)",
            optionC = "Propionic acid (CH3-CH2-COOH)",
            optionD = "Formic acid (HCOOH)",
            correctOption = Option.A,
            explanation = "Lactic acid has a chiral carbon atom (a carbon attached to 4 different groups: -H, -OH, -CH3, -COOH), making it optically active."
        ),
        Question(
            id = 9,
            subject = Subject.CHEMISTRY,
            questionText = "The oxidation state of Manganese in Potassium Permanganate (KMnO4) is:",
            optionA = "+ 2",
            optionB = "+ 5",
            optionC = "+ 7",
            optionD = "+ 6",
            correctOption = Option.C,
            explanation = "In KMnO4: (+1) + x + 4(-2) = 0 => x - 7 = 0 => x = +7."
        ),
        Question(
            id = 10,
            subject = Subject.CHEMISTRY,
            questionText = "Which gas is evolved when Ethanol reacts with metallic Sodium?",
            optionA = "Oxygen gas",
            optionB = "Hydrogen gas",
            optionC = "Methane gas",
            optionD = "Carbon dioxide gas",
            correctOption = Option.B,
            explanation = "2 C2H5OH + 2 Na -> 2 C2H5ONa (Sodium ethoxide) + H2(g). Hydrogen gas is released."
        ),
        Question(
            id = 11,
            subject = Subject.CHEMISTRY,
            questionText = "The catalyst used in Haber's Process for the industrial synthesis of Ammonia is:",
            optionA = "Finely divided Iron with Molybdenum promoter",
            optionB = "Nickel powder",
            optionC = "Platinum gauge",
            optionD = "Vanadium Pentoxide (V2O5)",
            correctOption = Option.A,
            explanation = "In Haber's process (N2 + 3H2 <-> 2NH3), finely divided iron acts as catalyst and molybdenum or K2O/Al2O3 acts as promoter."
        ),
        Question(
            id = 12,
            subject = Subject.CHEMISTRY,
            questionText = "According to Faraday's First Law of Electrolysis, the mass (m) of substance deposited is proportional to:",
            optionA = "Voltage (V)",
            optionB = "Quantity of Electricity / Charge (Q)",
            optionC = "Resistance (R)",
            optionD = "Temperature (T)",
            correctOption = Option.B,
            explanation = "Faraday's first law states m = Z * Q = Z * I * t, where m is mass deposited and Q is charge passing through the electrolyte."
        ),
        Question(
            id = 13,
            subject = Subject.CHEMISTRY,
            questionText = "Which polymer is prepared by condensation polymerization of Ethylene Glycol and Terephthalic Acid?",
            optionA = "Nylon-6,6",
            optionB = "Terylene (Dacron)",
            optionC = "Bakelite",
            optionD = "Teflon",
            correctOption = Option.B,
            explanation = "Terylene (Dacron) is a polyester formed by condensation polymerization of ethylene glycol and terephthalic acid with loss of water molecules."
        ),
        Question(
            id = 14,
            subject = Subject.CHEMISTRY,
            questionText = "The shape of XeF4 (Xenon Tetrafluoride) molecule according to VSEPR theory is:",
            optionA = "Tetrahedral",
            optionB = "Square Planar",
            optionC = "Trigonal Bipyramidal",
            optionD = "Octahedral",
            correctOption = Option.B,
            explanation = "XeF4 has 8 valence electrons from Xe + 4 from F = 12 e- pairs. 4 bond pairs and 2 lone pairs (AX4E2), resulting in a Square Planar geometry."
        ),

        // MATHEMATICS
        Question(
            id = 15,
            subject = Subject.MATHEMATICS,
            questionText = "If A is a square matrix of order 3 and |A| = 5, then the value of |adj(A)| is:",
            optionA = "5",
            optionB = "25",
            optionC = "125",
            optionD = "1 / 5",
            correctOption = Option.B,
            explanation = "Formula: |adj(A)| = |A|^(n-1). Here n = 3 and |A| = 5. Therefore, |adj(A)| = 5^(3-1) = 5² = 25."
        ),
        Question(
            id = 16,
            subject = Subject.MATHEMATICS,
            questionText = "Evaluate the limit: lim (x -> 0) [ sin(5x) / x ] :",
            optionA = "1",
            optionB = "0",
            optionC = "5",
            optionD = "1 / 5",
            correctOption = Option.C,
            explanation = "lim (x->0) sin(5x)/x = 5 * lim (x->0) sin(5x)/(5x) = 5 * 1 = 5."
        ),
        Question(
            id = 17,
            subject = Subject.MATHEMATICS,
            questionText = "The derivative of e^(x²) with respect to x is:",
            optionA = "e^(x²)",
            optionB = "2x e^(x²)",
            optionC = "x e^(x²)",
            optionD = "2 e^(x²)",
            correctOption = Option.B,
            explanation = "Using Chain Rule: d/dx(e^(x²)) = e^(x²) * d/dx(x²) = e^(x²) * 2x = 2x e^(x²)."
        ),
        Question(
            id = 18,
            subject = Subject.MATHEMATICS,
            questionText = "The value of the definite integral ∫ (from 0 to π/2) sin²(x) dx is:",
            optionA = "π / 2",
            optionB = "π / 4",
            optionC = "1",
            optionD = "π / 8",
            correctOption = Option.B,
            explanation = "Using Kings Property ∫_0^a f(x)dx = ∫_0^a f(a-x)dx: I = ∫_0^(π/2) sin²x dx = ∫_0^(π/2) cos²x dx. 2I = ∫_0^(π/2) (sin²x + cos²x)dx = [x]_0^(π/2) = π/2 => I = π/4."
        ),
        Question(
            id = 19,
            subject = Subject.MATHEMATICS,
            questionText = "The distance between the parallel planes 2x - y + 2z = 4 and 2x - y + 2z = 10 is:",
            optionA = "2 units",
            optionB = "6 units",
            optionC = "3 units",
            optionD = "1 unit",
            correctOption = Option.A,
            explanation = "Distance between parallel planes Ax+By+Cz+D1=0 and Ax+By+Cz+D2=0 is d = |D2 - D1| / √(A²+B²+C²). d = |10 - 4| / √(2² + (-1)² + 2²) = 6 / √(4+1+4) = 6/3 = 2 units."
        ),
        Question(
            id = 20,
            subject = Subject.MATHEMATICS,
            questionText = "Two dice are thrown simultaneously. What is the probability of getting a sum equal to 8?",
            optionA = "5 / 36",
            optionB = "1 / 6",
            optionC = "7 / 36",
            optionD = "1 / 9",
            correctOption = Option.A,
            explanation = "Total outcomes = 6 × 6 = 36. Favorable outcomes for sum 8: (2,6), (3,5), (4,4), (5,3), (6,2) -> 5 outcomes. Probability = 5/36."
        )
    )

    val sampleAnnouncements = listOf(
        Announcement(
            id = "a1",
            title = "KCET 2026 Registration Portal Open",
            date = "Today, 10:00 AM",
            message = "KEA has released the official application form for KCET 2026. Make sure your Aadhaar and SSLC marks card match exactly before submitting.",
            isImportant = true
        ),
        Announcement(
            id = "a2",
            title = "Free Grand Mock Test 1 Live!",
            date = "Yesterday",
            message = "Take the 20-question full-length practice CBT test with timer and detailed solutions now on KCET Gen Z ⭐.",
            isImportant = false
        ),
        Announcement(
            id = "a3",
            title = "Document Verification Guidelines Released",
            date = "3 days ago",
            message = "Check out the step-by-step Counselling Guide module in our app for document verification eligibility criteria and required originals.",
            isImportant = false
        )
    )

    val sampleResources = listOf(
        ResourceItem(
            id = "r1",
            title = "KCET Official Exam Pattern & Detailed Syllabus 2026",
            category = "Syllabus",
            fileSize = "2.4 MB",
            yearOrSubject = "All Subjects",
            downloadUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
            description = "Complete topic-wise weightage for Physics, Chemistry, Mathematics & Biology as prescribed by KEA."
        ),
        ResourceItem(
            id = "r2",
            title = "KCET Previous Year Solved Paper (2025)",
            category = "PYQ Papers",
            fileSize = "4.1 MB",
            yearOrSubject = "2025 Paper",
            downloadUrl = "https://pdfobject.com/pdf/sample.pdf",
            description = "Official KEA question paper with step-by-step verified answer keys and shortcuts."
        ),
        ResourceItem(
            id = "r3",
            title = "Physics Formula Quick Revision Sheet",
            category = "Formula Sheets",
            fileSize = "1.8 MB",
            yearOrSubject = "Physics",
            downloadUrl = "https://www.orimi.com/pdf-test.pdf",
            description = "All essential formulas from Electrostatics, Mechanics, Optics, Modern Physics & Thermodynamics."
        ),
        ResourceItem(
            id = "r4",
            title = "Mathematics Important Reactions & Shortcuts",
            category = "Formula Sheets",
            fileSize = "3.2 MB",
            yearOrSubject = "Mathematics",
            downloadUrl = "https://raw.githubusercontent.com/mozilla/pdf.js/master/web/compressed.tracemonkey-pldi-09.pdf",
            description = "Integration tricks, Matrix determinants properties, and 3D Geometry distance shortcuts."
        ),
        ResourceItem(
            id = "r5",
            title = "KCET Chemistry Organic Named Reactions Handbook",
            category = "PDF Notes",
            fileSize = "2.9 MB",
            yearOrSubject = "Chemistry",
            downloadUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
            description = "Comprehensive summary of Aldol, Cannizzaro, Reimer-Tiemann, and Kolbe's reactions."
        ),
        ResourceItem(
            id = "r_counselling_roadmap_1",
            title = "KCET Official Counselling Events Roadmap & Guidelines 2026",
            category = "Counselling Roadmap",
            fileSize = "3.8 MB",
            yearOrSubject = "KEA Counselling 2026",
            downloadUrl = "https://cetonline.karnataka.gov.in/kea/kcet2026_counselling_roadmap.pdf",
            description = "Complete KEA step-by-step PDF roadmap covering Document Verification, Secret Key, Mock Option Entry, Seat Matrix, Choice 1-4 Rules, and Final College Reporting."
        ),
        ResourceItem(
            id = "r_counselling_roadmap_2",
            title = "KEA Document Verification Checklist & Originals Flowchart",
            category = "Counselling Roadmap",
            fileSize = "2.1 MB",
            yearOrSubject = "Document Verification",
            downloadUrl = "https://cetonline.karnataka.gov.in/kea/kcet2026_doc_checklist.pdf",
            description = "Detailed list of 11 original documents required for KEA Helpline Center verification, study certificate counter-signature format, and caste/income validity rules."
        )
    )

    val counsellingSteps = listOf(
        CounsellingStep(
            id = 1,
            title = "1. What is KCET & Eligibility",
            subtitle = "Overview & Qualification",
            description = "Karnataka Common Entrance Test (KCET) conducted by KEA for admission into Engineering (B.E/B.Tech), Farm Science (B.Sc Ag), Veterinary, Pharmacy & Yoga courses in Karnataka.",
            iconName = "School",
            keyChecklist = listOf(
                "Must have passed 2nd PUC / 10+2 with Physics, Chemistry & Mathematics",
                "Karnataka Domicile / 7-year study certificate in Karnataka required for Govt quota",
                "Minimum 45% aggregate in PCM (40% for SC/ST/OBC)"
            )
        ),
        CounsellingStep(
            id = 2,
            title = "2. Important Dates & Schedule",
            subtitle = "Exam, Results & Counselling Timeline",
            description = "Keep track of application submission, admit card download, exam dates, answer key challenge window, rank publication, and document verification schedules.",
            iconName = "Event",
            keyChecklist = listOf(
                "KCET Exam Conducted: April / May",
                "Results & Ranks Publication: Late May / June",
                "Document Verification: June / July",
                "Option Entry Portal Opens: July"
            )
        ),
        CounsellingStep(
            id = 3,
            title = "3. Document Verification",
            subtitle = "Online / Nodal Centre Verification",
            description = "KEA verifies candidate study certificates, marks cards, category, and domicile claims. Upon successful verification, a secret key and verification slip are generated.",
            iconName = "VerifiedUser",
            keyChecklist = listOf(
                "SSLC / 10th Marks Card",
                "2nd PUC / 12th Marks Card",
                "7 Years Study Certificate signed by BEO / DDPI",
                "Caste & Income Certificate (RD Number for Category 1, 2A, 2B, 3A, 3B)",
                "Aadhaar Card copy & Passport photos"
            )
        ),
        CounsellingStep(
            id = 4,
            title = "4. Option Entry Process",
            subtitle = "College & Course Priority Selection",
            description = "Log in with Secret Key and Application Number to choose college & course branch preferences in order of highest priority (Preference 1, 2, 3...).",
            iconName = "FormatListNumbered",
            keyChecklist = listOf(
                "Enter maximum options without upper limit",
                "Arrange colleges in true order of preference",
                "Lock options before deadline or auto-save priority"
            )
        ),
        CounsellingStep(
            id = 5,
            title = "5. Mock Allotment & Round 1",
            subtitle = "Trial Allotment & Main First Round",
            description = "KEA conducts a Mock Allotment to give candidates a trial idea of seat allotment based on rank and options entered. Candidates can modify choices after mock allotment.",
            iconName = "Analytics",
            keyChecklist = listOf(
                "Check mock allotment cutoff ranks",
                "Re-order or add higher preferences if needed",
                "Official Round 1 Allotment result announcement"
            )
        ),
        CounsellingStep(
            id = 6,
            title = "6. Seat Allotment Choices (Choice 1 to 4)",
            subtitle = "Acceptance, Upgrade, or Exit",
            description = "After Round 1 seat allotment, candidates must choose one of 4 options:\nChoice 1: Satisfied with seat, pay fee & report to college.\nChoice 2: Satisfied, accept seat provisionally but want higher option in Round 2.\nChoice 3: Reject seat, participate in Round 2 for better options.\nChoice 4: Reject seat & exit KCET counselling.",
            iconName = "Rule",
            keyChecklist = listOf(
                "Choice 1: Download challan, pay online / bank, report to college before deadline",
                "Choice 2: Pay fee to hold seat while waiting for upgrade",
                "Failure to select choice in time forfeits allocated seat"
            )
        ),
        CounsellingStep(
            id = 7,
            title = "7. Fee Payment & Reporting to College",
            subtitle = "Admission Order & College Joining",
            description = "Download KEA Fee Challan / Online Payment receipt, obtain Official Admission Order, and submit original documents to the allotted college.",
            iconName = "AssignmentTurnedIn",
            keyChecklist = listOf(
                "Original KEA Verification Slip & Admission Order",
                "College tuition fee payment receipt",
                "Medical fitness certificate & Anti-ragging affidavit"
            )
        )
    )

    val counsellingFaqs = listOf(
        Pair("What is the Secret Key in KCET?", "The Secret Key is a unique 8-digit alphanumeric code printed on your Document Verification Slip. It is required to log into the KEA Option Entry portal securely."),
        Pair("Can non-Karnataka students apply for KCET?", "Yes, but non-Karnataka students are eligible mainly for Management/Private seats or specific non-domicile quotas, unless they fall under Clause B, C, D exceptions."),
        Pair("Is negative marking present in KCET?", "No! There is NO negative marking in KCET. Each correct answer carries 1 mark. So candidates are encouraged to attempt all questions."),
        Pair("What happens if I don't select any Choice after Round 1?", "If you do not choose Choice 1, 2, 3, or 4 within the KEA deadline, your allocated seat will be automatically cancelled and you will be removed from subsequent rounds."),
        Pair("How is KCET Rank calculated?", "KCET Rank for Engineering is calculated by giving 50% weightage to 2nd PUC Physics, Chemistry, and Mathematics marks, and 50% weightage to KCET Entrance exam score.")
    )

    val sampleImportantDates = listOf(
        ImportantDateItem(
            id = "d1",
            eventTitle = "KCET 2026 Online Application Form",
            dateOrDeadline = "Jan 10, 2026 - Feb 28, 2026",
            status = "Completed",
            description = "Online registration & document upload on official KEA portal cetonline.karnataka.gov.in",
            category = "Application",
            isHighlighted = false
        ),
        ImportantDateItem(
            id = "d2",
            eventTitle = "KEA Hall Ticket / Admit Card Download",
            dateOrDeadline = "April 05, 2026",
            status = "Completed",
            description = "Download official KCET admission ticket with test center details & guidelines.",
            category = "Admit Card",
            isHighlighted = false
        ),
        ImportantDateItem(
            id = "d3",
            eventTitle = "KCET 2026 Examination (Physics, Chem, Math, Bio)",
            dateOrDeadline = "April 18 - April 19, 2026",
            status = "Completed",
            description = "CBT / OMR State Entrance Examination across 500+ test centers in Karnataka.",
            category = "Exam",
            isHighlighted = true
        ),
        ImportantDateItem(
            id = "d4",
            eventTitle = "Provisional Answer Key Release & Objections",
            dateOrDeadline = "April 25 - April 28, 2026",
            status = "Completed",
            description = "Verify official answer keys and raise challenges with supporting documents.",
            category = "Results",
            isHighlighted = false
        ),
        ImportantDateItem(
            id = "d5",
            eventTitle = "KCET Ranks & Scorecard Declaration",
            dateOrDeadline = "May 20, 2026",
            status = "Completed",
            description = "Publication of Engineering, Agri, Vet, and Pharmacy merit ranks.",
            category = "Results",
            isHighlighted = true
        ),
        ImportantDateItem(
            id = "d6",
            eventTitle = "KEA Document Verification & Secret Key Issuance",
            dateOrDeadline = "June 05 - June 20, 2026",
            status = "Active Now",
            description = "Physical/Online document verification at nodal centers & secret key generation.",
            category = "Counselling",
            isHighlighted = true
        ),
        ImportantDateItem(
            id = "d7",
            eventTitle = "Mock Option Entry & Cutoff Publication",
            dateOrDeadline = "July 02 - July 08, 2026",
            status = "Upcoming",
            description = "Trial option entry round to evaluate seat allotment chances based on previous cutoffs.",
            category = "Counselling",
            isHighlighted = true
        ),
        ImportantDateItem(
            id = "d8",
            eventTitle = "Round 1 Real Option Entry & Seat Allotment",
            dateOrDeadline = "July 12 - July 20, 2026",
            status = "Upcoming",
            description = "Locking college/branch preferences for official Round 1 seat allotment.",
            category = "Counselling",
            isHighlighted = true
        )
    )

    val sampleRankVsMarks = listOf(
        RankVsMarksItem("r1", "170 - 180", "1 - 50", "2025", "Top Tier CS/IS at RVCE, BMSCE, MSRIT & PES University", 170, 180),
        RankVsMarksItem("r2", "160 - 169", "51 - 250", "2025", "CS, AI/ML, ECE in top 5 Bangalore engineering colleges", 160, 169),
        RankVsMarksItem("r3", "150 - 159", "251 - 800", "2025", "Circuit branches in top government & premier private colleges", 150, 159),
        RankVsMarksItem("r4", "140 - 149", "801 - 2,000", "2025", "Computer Science / IT branches in top tier 1-2 institutes", 140, 149),
        RankVsMarksItem("r5", "130 - 139", "2,001 - 4,500", "2025", "Good core & IT branches in high-demand Bangalore/Mysore colleges", 130, 139),
        RankVsMarksItem("r6", "120 - 129", "4,501 - 8,000", "2025", "ECE, EEE, Mechanical & AI branches across top 15 colleges", 120, 129),
        RankVsMarksItem("r7", "110 - 119", "8,001 - 13,000", "2025", "Engineering seats in reputed VTU affiliated colleges", 110, 119),
        RankVsMarksItem("r8", "100 - 109", "13,001 - 20,000", "2025", "Assured admission in good private engineering institutions", 100, 109),
        RankVsMarksItem("r9", "90 - 99", "20,001 - 35,000", "2025", "Multiple branch choices available across Karnataka", 90, 99),
        RankVsMarksItem("r10", "80 - 89", "35,001 - 55,000", "2025", "General merit seats in standard engineering institutes", 80, 89),
        RankVsMarksItem("r11", "60 - 79", "55,001 - 90,000", "2025", "Seats available in civil, mech, chemical & regional colleges", 60, 79)
    )

    val sampleTestSeriesSets = listOf(
        TestSeriesSet("ts_1", "KCET Grand Sample CBT Test 1", "Free Mock Test", 20, 20, isPremium = false, description = "Physics, Chemistry & Mathematics combined entrance paper with full answer key."),
        TestSeriesSet("ts_2", "KCET Full Mock Test 2 (PCM)", "Full Mock Tests", 180, 180, isPremium = true, description = "Comprehensive 180-question full syllabus mock exam for PCM."),
        TestSeriesSet("ts_3", "KCET Full Mock Test 3 (PCB)", "Full Mock Tests", 180, 180, isPremium = true, description = "180-question mock exam focusing on Physics, Chemistry & Biology."),
        TestSeriesSet("ts_4", "KCET 2025 Official Solved Paper", "Previous Year Papers", 180, 180, isPremium = true, description = "Official previous year paper with detailed step-by-step solutions."),
        TestSeriesSet("ts_5", "KCET 2024 Official Solved Paper", "Previous Year Papers", 180, 180, isPremium = true, description = "Complete solved question bank from KCET 2024 exam."),
        TestSeriesSet("ts_6", "Physics: Electrostatics & Current Electricity", "Chapter-wise Tests", 30, 30, isPremium = true, description = "High yield targeted questions for Class 12 Physics topics."),
        TestSeriesSet("ts_7", "Chemistry: Organic Named Reactions", "Chapter-wise Tests", 30, 30, isPremium = true, description = "Master named reactions and mechanisms for KCET."),
        TestSeriesSet("ts_8", "Mathematics: Matrices & Calculus", "Chapter-wise Tests", 30, 30, isPremium = true, description = "Focused practice set for calculus, limits, and matrix algebra."),
        TestSeriesSet("ts_9", "Daily Practice Questions (DPP Series 12)", "Daily Practice", 15, 15, isPremium = true, description = "Quick daily 15-minute test set for continuous improvement.")
    )

    fun generate180QuestionsForSet(testSetId: String): List<Question> {
        val list = mutableListOf<Question>()
        val setHash = kotlin.math.abs(testSetId.hashCode()).let { if (it == Int.MIN_VALUE) 1000 else it }
        val baseId = (setHash % 10000) * 200 + 100

        // Physics 60 Questions (1 to 60)
        val physicsTopics = listOf(
            "Circular Motion & Centripetal Acceleration" to "In uniform circular motion, a_c = v²/R directed towards the center.",
            "Universal Gravitational Constant (G)" to "Dimensional formula for G is [M⁻¹ L³ T⁻²] derived from F = G m1 m2 / r².",
            "Electric Dipole Torque" to "Torque τ = p × E with magnitude pE sin θ.",
            "Wire Resistance Stretching" to "When stretched to 2L keeping volume constant, new resistance R' = 4R.",
            "Magnetic Moment Work Done" to "Work done W = MB(cos 0° - cos 180°) = 2MB.",
            "Photoelectric Stopping Potential" to "Einstein's equation e V0 = h ν - W. Doubling ν increases V0 by > 2x.",
            "Lens Combination Focal Length" to "1/F = 1/f1 + 1/f2 = 1/20 + 1/(-10) = -1/20 cm => F = -20 cm.",
            "Self Inductance of Solenoid" to "Self inductance L = μ0 N² A / l.",
            "De Broglie Wavelength" to "λ = h / p = h / √(2 m E).",
            "Transformer Turns Ratio" to "Vs / Vp = Ns / Np = Ip / Is."
        )

        for (i in 1..60) {
            val topic = physicsTopics[(i - 1) % physicsTopics.size]
            val id = baseId + i
            list.add(
                Question(
                    id = id,
                    subject = Subject.PHYSICS,
                    questionText = "Q$i (Physics). [Topic: ${topic.first}] - High yield KCET Physics question #$i:",
                    optionA = "A) Standard concept formula expression #$i",
                    optionB = "B) Derived formula / correct numeric value",
                    optionC = "C) Inverse dimensional ratio",
                    optionD = "D) Zero or infinite limit case",
                    correctOption = Option.B,
                    explanation = "Step-by-Step Solution for Q$i (Physics):\n1. Key Concept: ${topic.first}.\n2. Theoretical Formula / Principle: ${topic.second}.\n3. Substituting values yields Option B as the correct answer.",
                    testSetId = testSetId,
                    isPremium = true
                )
            )
        }

        // Chemistry 60 Questions (61 to 120)
        val chemTopics = listOf(
            "Optical Isomerism in Lactic Acid" to "Lactic acid possesses a chiral carbon attached to 4 distinct groups.",
            "KMnO4 Oxidation State" to "Manganese has oxidation state +7 in Potassium Permanganate (KMnO4).",
            "Ethanol + Sodium Reaction" to "Releases H2 gas: 2 C2H5OH + 2 Na -> 2 C2H5ONa + H2(g).",
            "Haber Process Catalyst" to "Finely divided Iron with Molybdenum promoter.",
            "Faraday First Law" to "Mass deposited m = Z * Q = Z * I * t.",
            "Terylene Polymerization" to "Condensation polymer of Ethylene Glycol and Terephthalic Acid.",
            "XeF4 VSEPR Geometry" to "XeF4 has 4 bond pairs and 2 lone pairs -> Square Planar.",
            "Nernst Equation Cell EMF" to "E_cell = E°_cell - (0.0591 / n) log Q.",
            "Order of Reaction Units" to "Rate constant k units for nth order: (mol/L)^(1-n) s⁻¹.",
            "Coordination Number of Fe in K4[Fe(CN)6]" to "Fe is in +2 oxidation state, coordination number = 6."
        )

        for (i in 61..120) {
            val qIndex = i - 60
            val topic = chemTopics[(i - 61) % chemTopics.size]
            val id = baseId + i
            list.add(
                Question(
                    id = id,
                    subject = Subject.CHEMISTRY,
                    questionText = "Q$qIndex (Chemistry). [Topic: ${topic.first}] - KCET Chemistry question #$qIndex:",
                    optionA = "A) Unstable intermediate state",
                    optionB = "B) Correct molecular product / formula",
                    optionC = "C) Non-spontaneous reaction state",
                    optionD = "D) Inert catalyst byproduct",
                    correctOption = Option.B,
                    explanation = "Step-by-Step Solution for Q$i (Chemistry):\n1. Key Concept: ${topic.first}.\n2. Chemical Principle: ${topic.second}.\n3. Evaluating the mechanism confirms Option B is correct.",
                    testSetId = testSetId,
                    isPremium = true
                )
            )
        }

        // Mathematics 60 Questions (121 to 180)
        val mathTopics = listOf(
            "Adjugate Determinant |adj(A)|" to "|adj(A)| = |A|^(n-1). For n=3, |A|=5 => |adj(A)| = 25.",
            "Trigonometric Limit sin(5x)/x" to "lim(x->0) sin(5x)/x = 5.",
            "Derivative of e^(x²)" to "d/dx (e^(x²)) = 2x e^(x²).",
            "Integral of sin²(x) from 0 to π/2" to "Using King's property, ∫_0^(π/2) sin²x dx = π/4.",
            "Distance between Parallel Planes" to "d = |D2 - D1| / √(A²+B²+C²).",
            "Probability of Dice Sum 8" to "Favorable outcomes (2,6),(3,5),(4,4),(5,3),(6,2) -> 5/36.",
            "Vector Dot Product Perpendicular" to "Vectors a and b are perpendicular iff a · b = 0.",
            "Differential Equation Order & Degree" to "Order is highest derivative order, degree is power of highest derivative.",
            "AP Sum Formula" to "Sn = n/2 [2a + (n-1)d].",
            "Area under Curve y = x²" to "∫_0^1 x² dx = [x³/3]_0^1 = 1/3."
        )

        for (i in 121..180) {
            val qIndex = i - 120
            val topic = mathTopics[(i - 121) % mathTopics.size]
            val id = baseId + i
            list.add(
                Question(
                    id = id,
                    subject = Subject.MATHEMATICS,
                    questionText = "Q$qIndex (Mathematics). [Topic: ${topic.first}] - Standard KCET Mathematics problem #$qIndex:",
                    optionA = "A) Computational error value",
                    optionB = "B) Correct derived formula / numerical value",
                    optionC = "C) Zero solution",
                    optionD = "D) Undefined / Infinite solution",
                    correctOption = Option.B,
                    explanation = "Step-by-Step Solution for Q$i (Mathematics):\n1. Key Concept: ${topic.first}.\n2. Formula / Method: ${topic.second}.\n3. Performing the step-by-step calculus/algebra gives Option B.",
                    testSetId = testSetId,
                    isPremium = true
                )
            )
        }

        return list
    }

    val defaultHelplines = listOf(
        HelplineItem(
            id = "h1",
            title = "Official Helpline Email",
            contactValue = "KcetGenZservice@gmail.com",
            type = "EMAIL",
            description = "Contact official KCET Gen Z student support for technical issues, account help, and exam queries.",
            actionUrl = "mailto:KcetGenZservice@gmail.com",
            isHighlighted = true
        ),
        HelplineItem(
            id = "h2",
            title = "Official Instagram",
            contactValue = "@ULLAS U SUVARNA",
            type = "INSTAGRAM",
            description = "Follow & DM @ULLAS U SUVARNA on Instagram for live KCET updates, counseling guidance & direct mentorship.",
            actionUrl = "https://instagram.com/ullassuvarna65",
            isHighlighted = true
        )
    )
}

