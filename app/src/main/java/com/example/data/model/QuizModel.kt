package com.example.data.model

data class QuizQuestion(
    val id: Int,
    val text: String,
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0,
    val isEssay: Boolean = false,
    val modelAnswer: String = "",
    val explanation: String = ""
)

data class QuizSubmission(
    val answersMcq: Map<Int, Int>, // questionId to selectedIndex
    val answersEssay: Map<Int, String>, // questionId to text
    val essaySelfScores: Map<Int, Boolean> = emptyMap() // questionId to correct
)
