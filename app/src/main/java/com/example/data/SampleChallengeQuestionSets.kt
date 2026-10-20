package com.example.data

object SampleChallengeQuestionSets {

    val set1Questions = listOf(
        // Physics (7)
        Question(
            id = 1001,
            subject = Subject.PHYSICS,
            questionText = "An object is thrown vertically upwards with a velocity of 19.6 m/s. What is the maximum height attained by the object? (g = 9.8 m/s²)",
            optionA = "9.8 m",
            optionB = "19.6 m",
            optionC = "39.2 m",
            optionD = "14.7 m",
            correctOption = Option.B,
            explanation = "Using formula h = u² / (2g): h = (19.6)² / (2 * 9.8) = 384.16 / 19.6 = 19.6 m."
        ),
        Question(
            id = 1002,
            subject = Subject.PHYSICS,
            questionText = "The dimensional formula of Torque is equivalent to that of:",
            optionA = "Momentum",
            optionB = "Power",
            optionC = "Work",
            optionD = "Force",
            correctOption = Option.C,
            explanation = "Torque = Force x Distance [M L² T⁻²]. Work = Force x Displacement [M L² T⁻²]. Hence both have identical dimensions."
        ),
        Question(
            id = 1003,
            subject = Subject.PHYSICS,
            questionText = "Two point charges +2 μC and +6 μC repel each other with a force of 12 N. If a charge of -4 μC is added to each, what is the new force?",
            optionA = "4 N attractive",
            optionB = "4 N repulsive",
            optionC = "8 N attractive",
            optionD = "12 N repulsive",
            correctOption = Option.A,
            explanation = "New q1 = +2 - 4 = -2 μC, New q2 = +6 - 4 = +2 μC. Ratio F'/F = (|q1' q2'|) / (|q1 q2|) = (2 * 2)/(2 * 6) = 4/12 = 1/3. F' = 12 / 3 = 4 N. Since charges are opposite, force is attractive."
        ),
        Question(
            id = 1004,
            subject = Subject.PHYSICS,
            questionText = "A wire of resistance 10 Ω is stretched to double its length. What is its new resistance?",
            optionA = "10 Ω",
            optionB = "20 Ω",
            optionC = "40 Ω",
            optionD = "5 Ω",
            correctOption = Option.C,
            explanation = "When length becomes L'=2L, area A'=A/2 to keep volume constant. R' = ρ L' / A' = ρ (2L) / (A/2) = 4 (ρ L / A) = 4 * 10 = 40 Ω."
        ),
        Question(
            id = 1005,
            subject = Subject.PHYSICS,
            questionText = "The angle of dip at the magnetic equator of the Earth is:",
            optionA = "0°",
            optionB = "45°",
            optionC = "90°",
            optionD = "180°",
            correctOption = Option.A,
            explanation = "At the magnetic equator, the magnetic field lines are parallel to the Earth's surface, so angle of dip = 0°."
        ),
        Question(
            id = 1006,
            subject = Subject.PHYSICS,
            questionText = "The phenomenon of interference of light proves that light has:",
            optionA = "Particle nature",
            optionB = "Wave nature",
            optionC = "Transverse nature only",
            optionD = "Longitudinal nature",
            correctOption = Option.B,
            explanation = "Interference and diffraction phenomenon strictly demonstrate the wave nature of light."
        ),
        Question(
            id = 1007,
            subject = Subject.PHYSICS,
            questionText = "The threshold frequency for a metallic surface corresponds to energy of 3.3 eV. What is the work function of the metal?",
            optionA = "1.6 eV",
            optionB = "3.3 eV",
            optionC = "6.6 eV",
            optionD = "0 eV",
            correctOption = Option.B,
            explanation = "Work function φ = h ν₀. Since threshold frequency corresponds to 3.3 eV photon energy, the work function is 3.3 eV."
        ),

        // Chemistry (7)
        Question(
            id = 1008,
            subject = Subject.CHEMISTRY,
            questionText = "Which among the following noble gases has the highest boiling point?",
            optionA = "Helium (He)",
            optionB = "Neon (Ne)",
            optionC = "Argon (Ar)",
            optionD = "Xenon (Xe)",
            correctOption = Option.D,
            explanation = "Down the noble gas group, atomic size increases, van der Waals forces strengthen, leading to higher boiling points. Xenon has the highest boiling point among options."
        ),
        Question(
            id = 1009,
            subject = Subject.CHEMISTRY,
            questionText = "The oxidation state of Chromium in K₂Cr₂O₇ is:",
            optionA = "+3",
            optionB = "+4",
            optionC = "+6",
            optionD = "+7",
            correctOption = Option.C,
            explanation = "K₂Cr₂O₇: 2(+1) + 2(x) + 7(-2) = 0 => 2 + 2x - 14 = 0 => 2x = 12 => x = +6."
        ),
        Question(
            id = 1010,
            subject = Subject.CHEMISTRY,
            questionText = "Which reagent converts ethyl alcohol to ethyl chloride with high purity?",
            optionA = "PCl₅",
            optionB = "PCl₃",
            optionC = "SOCl₂ in presence of pyridine",
            optionD = "HCl / ZnCl₂ (Lucas reagent)",
            correctOption = Option.C,
            explanation = "Darzen's process using SOCl₂ yields SO₂ and HCl gases which escape, leaving pure alkyl chloride."
        ),
        Question(
            id = 1011,
            subject = Subject.CHEMISTRY,
            questionText = "The unit of rate constant for a second-order reaction is:",
            optionA = "s⁻¹",
            optionB = "mol L⁻¹ s⁻¹",
            optionC = "L mol⁻¹ s⁻¹",
            optionD = "L² mol⁻² s⁻¹",
            correctOption = Option.C,
            explanation = "Unit of k = (mol/L)^(1-n) s⁻¹. For n=2: (mol/L)⁻¹ s⁻¹ = L mol⁻¹ s⁻¹."
        ),
        Question(
            id = 1012,
            subject = Subject.CHEMISTRY,
            questionText = "Which isomerism is exhibited by [Co(NH₃)₅(NO₂)]Cl₂?",
            optionA = "Linkage isomerism",
            optionB = "Hydrate isomerism",
            optionC = "Optical isomerism",
            optionD = "Geometrical isomerism",
            correctOption = Option.A,
            explanation = "NO₂⁻ is an ambidentate ligand that can coordinate through Nitrogen (-NO₂) or Oxygen (-ONO), producing linkage isomerism."
        ),
        Question(
            id = 1013,
            subject = Subject.CHEMISTRY,
            questionText = "Which among the following is an example of a condensation polymer?",
            optionA = "Polythene",
            optionB = "Nylon-6,6",
            optionC = "Teflon",
            optionD = "Buna-S",
            correctOption = Option.B,
            explanation = "Nylon-6,6 is formed by condensation polymerization of hexamethylenediamine and adipic acid with elimination of water molecules."
        ),
        Question(
            id = 1014,
            subject = Subject.CHEMISTRY,
            questionText = "The pH of a 10⁻³ M solution of NaOH is:",
            optionA = "3",
            optionB = "11",
            optionC = "7",
            optionD = "14",
            correctOption = Option.B,
            explanation = "[OH⁻] = 10⁻³ M. pOH = -log(10⁻³) = 3. pH = 14 - pOH = 14 - 3 = 11."
        ),

        // Mathematics (6)
        Question(
            id = 1015,
            subject = Subject.MATHEMATICS,
            questionText = "If A = {1, 2, 3}, then the number of equivalence relations containing (1, 2) is:",
            optionA = "1",
            optionB = "2",
            optionC = "3",
            optionD = "4",
            correctOption = Option.B,
            explanation = "Smallest equivalence relation is {(1,1),(2,2),(3,3),(1,2),(2,1)}. Largest is A x A. Total = 2 relations."
        ),
        Question(
            id = 1016,
            subject = Subject.MATHEMATICS,
            questionText = "If matrix A = [[1, 2], [3, 4]], then det(A⁻¹) is equal to:",
            optionA = "-1/2",
            optionB = "-2",
            optionC = "1/2",
            optionD = "2",
            correctOption = Option.A,
            explanation = "det(A) = (1*4 - 2*3) = 4 - 6 = -2. det(A⁻¹) = 1 / det(A) = -1/2."
        ),
        Question(
            id = 1017,
            subject = Subject.MATHEMATICS,
            questionText = "Evaluate integral ∫ sin³(x) cos(x) dx:",
            optionA = "sin⁴(x) / 4 + C",
            optionB = "cos⁴(x) / 4 + C",
            optionC = "sin²(x) / 2 + C",
            optionD = "-sin⁴(x) / 4 + C",
            correctOption = Option.A,
            explanation = "Let u = sin(x), du = cos(x) dx. ∫ u³ du = u⁴ / 4 + C = sin⁴(x)/4 + C."
        ),
        Question(
            id = 1018,
            subject = Subject.MATHEMATICS,
            questionText = "The angle between vectors a⃗ = î + j⃗ - k̂ and b⃗ = î - j⃗ + k̂ is:",
            optionA = "cos⁻¹(1/3)",
            optionB = "cos⁻¹(-1/3)",
            optionC = "π/2",
            optionD = "π/3",
            correctOption = Option.B,
            explanation = "a⃗ · b⃗ = 1(1) + 1(-1) + (-1)(1) = 1 - 1 - 1 = -1. |a⃗| = √3, |b⃗| = √3. cos θ = -1 / (√3 * √3) = -1/3. θ = cos⁻¹(-1/3)."
        ),
        Question(
            id = 1019,
            subject = Subject.MATHEMATICS,
            questionText = "The distance between parallel planes 2x - y + 2z = 4 and 4x - 2y + 4z = 14 is:",
            optionA = "1 unit",
            optionB = "2 units",
            optionC = "3 units",
            optionD = "1/3 units",
            correctOption = Option.A,
            explanation = "Divide second plane by 2: 2x - y + 2z = 7. Distance = |d₂ - d₁| / √(a² + b² + c²) = |7 - 4| / √(4 + 1 + 4) = 3 / 3 = 1 unit."
        ),
        Question(
            id = 1020,
            subject = Subject.MATHEMATICS,
            questionText = "In a binomial distribution B(n, p), if mean = 4 and variance = 2, then value of n is:",
            optionA = "6",
            optionB = "8",
            optionC = "10",
            optionD = "12",
            correctOption = Option.B,
            explanation = "Mean np = 4, Variance npq = 2. q = (npq)/(np) = 2/4 = 1/2. p = 1 - q = 1/2. np = n(1/2) = 4 => n = 8."
        )
    )

    val set2Questions = set1Questions.mapIndexed { idx, q ->
        q.copy(
            id = 2000 + idx + 1,
            questionText = "Set 02 - " + q.questionText,
            testSetId = "set_02"
        )
    }

    val set3Questions = set1Questions.mapIndexed { idx, q ->
        q.copy(
            id = 3000 + idx + 1,
            questionText = "Set 03 - " + q.questionText,
            testSetId = "set_03"
        )
    }

    val set4Questions = set1Questions.mapIndexed { idx, q ->
        q.copy(
            id = 4000 + idx + 1,
            questionText = "Set 04 - " + q.questionText,
            testSetId = "set_04"
        )
    }

    val set5Questions = set1Questions.mapIndexed { idx, q ->
        q.copy(
            id = 5000 + idx + 1,
            questionText = "Set 05 - " + q.questionText,
            testSetId = "set_05"
        )
    }

    val initialSets = listOf(
        ChallengeQuestionSet(
            setId = "set_01",
            name = "KCET PCM Challenge Set 01",
            description = "Balanced Physics, Chemistry, and Mathematics 20-Question Challenge.",
            questions = set1Questions,
            isActive = true,
            createdAt = System.currentTimeMillis()
        ),
        ChallengeQuestionSet(
            setId = "set_02",
            name = "KCET PCM Challenge Set 02",
            description = "High-Yield PCM KCET 2026 Test Questions.",
            questions = set2Questions,
            isActive = true,
            createdAt = System.currentTimeMillis() - 86400000L
        ),
        ChallengeQuestionSet(
            setId = "set_03",
            name = "KCET PCM Challenge Set 03",
            description = "Speed & Accuracy Booster Pack.",
            questions = set3Questions,
            isActive = true,
            createdAt = System.currentTimeMillis() - 172800000L
        ),
        ChallengeQuestionSet(
            setId = "set_04",
            name = "KCET PCM Challenge Set 04",
            description = "Previous Year Standard KCET PCM Exam Paper.",
            questions = set4Questions,
            isActive = true,
            createdAt = System.currentTimeMillis() - 259200000L
        ),
        ChallengeQuestionSet(
            setId = "set_05",
            name = "KCET PCM Challenge Set 05",
            description = "Grand Mock PCM Challenge Set.",
            questions = set5Questions,
            isActive = true,
            createdAt = System.currentTimeMillis() - 345600000L
        )
    )
}
