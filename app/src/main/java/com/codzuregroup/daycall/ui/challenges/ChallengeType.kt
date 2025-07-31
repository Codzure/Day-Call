package com.codzuregroup.daycall.ui.challenges

import kotlin.random.Random

enum class ChallengeType(val displayName: String, val description: String) {
    MATH("Math", "Solve math problems to wake up your brain"),
    QR_SCAN("QR Scan", "Scan a QR code to get out of bed"),
    MEMORY_MATCH("Memory Match", "Match pairs to focus your mind"),
    SHAKE("Shake", "Shake your device"),
    MEMORY("Memory", "Remember the sequence"),
    PATTERN("Pattern", "Complete the pattern"),
    WORD("Word", "Unscramble the word"),
    LOGIC("Logic", "Solve the logic puzzle")
}

data class Challenge(
    val id: String,
    val type: ChallengeType,
    val question: String,
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val difficulty: ChallengeDifficulty = ChallengeDifficulty.EASY,
    val timeLimit: Int = 30 // seconds
)

enum class ChallengeDifficulty {
    EASY, MEDIUM, HARD
}

object ChallengeGenerator {
    fun getRandomChallenge(): Challenge {
        val challenges = listOf(
            generateMathChallenge(),
            generateQRScanChallenge(),
            generateMemoryMatchChallenge(),
            generateShakeChallenge(),
            generateMemoryChallenge(),
            generatePatternChallenge(),
            generateWordChallenge(),
            generateLogicChallenge()
        )
        return challenges.random()
    }

    private fun generateMathChallenge(): Challenge {
        // Progressive difficulty: 2-3 problems
        val problemCount = Random.nextInt(2, 4)
        val problems = mutableListOf<MathProblem>()
        
        repeat(problemCount) { index ->
            val difficulty = when (index) {
                0 -> MathDifficulty.EASY
                1 -> MathDifficulty.MEDIUM
                else -> MathDifficulty.HARD
            }
            problems.add(generateMathProblem(difficulty))
        }

        val question = problems.joinToString("\n") { it.question }
        val correctAnswer = problems.joinToString(",") { it.answer.toString() }
        
        // Generate wrong answers for multiple choice
        val wrongAnswers = generateWrongMathAnswers(problems.map { it.answer })
        val allOptions = (wrongAnswers + correctAnswer).shuffled()

        return Challenge(
            id = "math_${System.currentTimeMillis()}",
            type = ChallengeType.MATH,
            question = question,
            options = allOptions,
            correctAnswer = correctAnswer,
            difficulty = ChallengeDifficulty.MEDIUM,
            timeLimit = 45 // More time for multiple problems
        )
    }

    private fun generateMathProblem(difficulty: MathDifficulty): MathProblem {
        return when (difficulty) {
            MathDifficulty.EASY -> generateEasyMathProblem()
            MathDifficulty.MEDIUM -> generateMediumMathProblem()
            MathDifficulty.HARD -> generateHardMathProblem()
        }
    }

    private fun generateEasyMathProblem(): MathProblem {
        val operations = listOf("+", "-")
        val operation = operations.random()
        
        val (num1, num2, result) = when (operation) {
            "+" -> {
                val a = Random.nextInt(5, 25)
                val b = Random.nextInt(5, 25)
                Triple(a, b, a + b)
            }
            "-" -> {
                val a = Random.nextInt(20, 50)
                val b = Random.nextInt(5, a)
                Triple(a, b, a - b)
            }
            else -> Triple(10, 5, 15)
        }

        return MathProblem(
            question = "$num1 $operation $num2 = ?",
            answer = result
        )
    }

    private fun generateMediumMathProblem(): MathProblem {
        val operations = listOf("×", "÷")
        val operation = operations.random()
        
        val (num1, num2, result) = when (operation) {
            "×" -> {
                val a = Random.nextInt(2, 12)
                val b = Random.nextInt(2, 12)
                Triple(a, b, a * b)
            }
            "÷" -> {
                val b = Random.nextInt(2, 12)
                val result = Random.nextInt(2, 12)
                val a = b * result
                Triple(a, b, result)
            }
            else -> Triple(6, 3, 2)
        }

        return MathProblem(
            question = "$num1 $operation $num2 = ?",
            answer = result
        )
    }

    private fun generateHardMathProblem(): MathProblem {
        // Multi-step problems
        val problemType = Random.nextInt(3)
        
        return when (problemType) {
            0 -> {
                val a = Random.nextInt(10, 30)
                val b = Random.nextInt(5, 15)
                val c = Random.nextInt(2, 8)
                val result = a + b * c
                MathProblem(
                    question = "$a + $b × $c = ?",
                    answer = result
                )
            }
            1 -> {
                val a = Random.nextInt(20, 50)
                val b = Random.nextInt(5, 15)
                val result = a - b
                MathProblem(
                    question = "$a - $b = ?",
                    answer = result
                )
            }
            else -> {
                val a = Random.nextInt(2, 12)
                val b = Random.nextInt(2, 12)
                val c = Random.nextInt(2, 6)
                val result = a * b + c
                MathProblem(
                    question = "$a × $b + $c = ?",
                    answer = result
                )
            }
        }
    }

    private fun generateQRScanChallenge(): Challenge {
        val qrCodes = listOf(
            "DAYCALL_WAKE_UP_001",
            "DAYCALL_WAKE_UP_002", 
            "DAYCALL_WAKE_UP_003",
            "DAYCALL_WAKE_UP_004",
            "DAYCALL_WAKE_UP_005"
        )
        
        val selectedCode = qrCodes.random()
        
        return Challenge(
            id = "qr_scan_${System.currentTimeMillis()}",
            type = ChallengeType.QR_SCAN,
            question = "Scan the QR code to dismiss the alarm",
            correctAnswer = selectedCode,
            difficulty = ChallengeDifficulty.MEDIUM,
            timeLimit = 60 // More time for movement-based challenge
        )
    }

    private fun generateMemoryMatchChallenge(): Challenge {
        val symbols = listOf("🌟", "🎈", "🎨", "🎭", "🎪", "🎯", "🎲", "🎮")
        val selectedSymbols = symbols.shuffled().take(4)
        val pairs = selectedSymbols + selectedSymbols
        val shuffledPairs = pairs.shuffled()
        
        return Challenge(
            id = "memory_match_${System.currentTimeMillis()}",
            type = ChallengeType.MEMORY_MATCH,
            question = "Match the pairs: ${shuffledPairs.joinToString(" ")}",
            correctAnswer = selectedSymbols.joinToString(","),
            difficulty = ChallengeDifficulty.MEDIUM,
            timeLimit = 45
        )
    }

    private fun generateShakeChallenge(): Challenge {
        return Challenge(
            id = "shake_${System.currentTimeMillis()}",
            type = ChallengeType.SHAKE,
            question = "Shake your device to dismiss the alarm",
            correctAnswer = "Shake",
            difficulty = ChallengeDifficulty.EASY,
            timeLimit = 30
        )
    }

    private fun generateWrongMathAnswers(correctAnswers: List<Int>): List<String> {
        val wrongAnswers = mutableListOf<String>()
        
        correctAnswers.forEach { correct ->
            repeat(2) {
                val wrong = when (Random.nextInt(3)) {
                    0 -> correct + Random.nextInt(-5, 6).coerceAtLeast(1)
                    1 -> correct + Random.nextInt(-10, 11).coerceAtLeast(1)
                    else -> correct * Random.nextInt(2, 4)
                }
                wrongAnswers.add(wrong.toString())
            }
        }
        
        return wrongAnswers.distinct().take(6)
    }

    private fun generateMemoryChallenge(): Challenge {
        val sequence = (1..4).map { Random.nextInt(1, 10) }
        val sequenceStr = sequence.joinToString(" - ")
        
        return Challenge(
            id = "memory_${System.currentTimeMillis()}",
            type = ChallengeType.MEMORY,
            question = "Remember this sequence: $sequenceStr",
            correctAnswer = sequence.joinToString("")
        )
    }

    private fun generatePatternChallenge(): Challenge {
        val patterns = listOf(
            Triple("2, 4, 6, 8, ?", "10", "Even numbers"),
            Triple("1, 3, 6, 10, ?", "15", "Triangular numbers"),
            Triple("1, 2, 4, 8, ?", "16", "Powers of 2"),
            Triple("1, 1, 2, 3, 5, ?", "8", "Fibonacci sequence")
        )
        
        val (pattern, answer, _) = patterns.random()
        
        return Challenge(
            id = "pattern_${System.currentTimeMillis()}",
            type = ChallengeType.PATTERN,
            question = "Complete the pattern: $pattern",
            correctAnswer = answer
        )
    }

    private fun generateWordChallenge(): Challenge {
        val words = listOf(
            "WAKE" to "AWEK",
            "ALARM" to "LARMA",
            "MORNING" to "NINMORG",
            "SUNRISE" to "SERISUN",
            "ENERGY" to "YRGENE"
        )
        
        val (correct, scrambled) = words.random()
        
        return Challenge(
            id = "word_${System.currentTimeMillis()}",
            type = ChallengeType.WORD,
            question = "Unscramble: $scrambled",
            correctAnswer = correct
        )
    }

    private fun generateLogicChallenge(): Challenge {
        val challenges = listOf(
            Triple("If all roses are flowers, and some flowers fade quickly, then:", "Some roses may fade quickly", "Logic reasoning"),
            Triple("Which number comes next: 2, 6, 12, 20, ?", "30", "Square numbers + n"),
            Triple("If A=1, B=2, C=3, what is ABC?", "123", "Letter to number conversion")
        )
        
        val (question, answer, _) = challenges.random()
        
        return Challenge(
            id = "logic_${System.currentTimeMillis()}",
            type = ChallengeType.LOGIC,
            question = question,
            correctAnswer = answer
        )
    }

    private fun generateWrongAnswers(correct: Int, count: Int): List<String> {
        val wrongAnswers = mutableSetOf<String>()
        while (wrongAnswers.size < count) {
            val wrong = correct + Random.nextInt(-5, 6)
            if (wrong != correct && wrong > 0) {
                wrongAnswers.add(wrong.toString())
            }
        }
        return wrongAnswers.toList()
    }
}

data class MathProblem(
    val question: String,
    val answer: Int
)

enum class MathDifficulty {
    EASY, MEDIUM, HARD
} 