package com.codzuregroup.daycall.ui.challenges

import kotlin.random.Random

enum class ChallengeType(val displayName: String, val description: String) {
    MATH("Math", "Solve a math problem"),
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
            generateMemoryChallenge(),
            generatePatternChallenge(),
            generateWordChallenge(),
            generateLogicChallenge()
        )
        return challenges.random()
    }

    private fun generateMathChallenge(): Challenge {
        val operations = listOf("+", "-", "×", "÷")
        val operation = operations.random()
        
        val (num1, num2, result) = when (operation) {
            "+" -> {
                val a = Random.nextInt(10, 50)
                val b = Random.nextInt(10, 50)
                Triple(a, b, a + b)
            }
            "-" -> {
                val a = Random.nextInt(20, 100)
                val b = Random.nextInt(10, a)
                Triple(a, b, a - b)
            }
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
            else -> Triple(5, 3, 8)
        }

        val wrongAnswers = generateWrongAnswers(result, 3)
        val allOptions = (wrongAnswers + result.toString()).shuffled()

        return Challenge(
            id = "math_${System.currentTimeMillis()}",
            type = ChallengeType.MATH,
            question = "$num1 $operation $num2 = ?",
            options = allOptions,
            correctAnswer = result.toString()
        )
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