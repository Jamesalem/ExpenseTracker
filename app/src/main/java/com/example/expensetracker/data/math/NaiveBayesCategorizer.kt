package com.example.expensetracker.data.math

import com.example.expensetracker.data.model.Expense
import java.util.Locale
import kotlin.math.ln

/**
 * On-Device Multinomial Naive Bayes Text Classifier.
 * Automatically predicts transaction categories from transaction title or note keywords
 * completely offline with sub-millisecond inference time.
 */
class NaiveBayesCategorizer {

    private val categoryDocCounts = mutableMapOf<String, Int>()
    private val categoryWordCounts = mutableMapOf<String, MutableMap<String, Int>>()
    private val categoryTotalWords = mutableMapOf<String, Int>()
    private val vocabulary = mutableSetOf<String>()
    private var totalDocuments = 0

    /**
     * Trains or updates the model with historical expense data.
     */
    fun train(expenses: List<Expense>) {
        clear()
        for (expense in expenses) {
            val text = "${expense.title} ${expense.note.orEmpty()}".trim()
            if (text.isNotBlank() && expense.category.isNotBlank()) {
                addDocument(text, expense.category)
            }
        }
    }

    /**
     * Incrementally trains on a single newly added transaction.
     */
    fun addDocument(text: String, category: String) {
        val tokens = tokenize(text)
        if (tokens.isEmpty()) return

        totalDocuments++
        categoryDocCounts[category] = (categoryDocCounts[category] ?: 0) + 1

        val wordMap = categoryWordCounts.getOrPut(category) { mutableMapOf() }
        for (token in tokens) {
            vocabulary.add(token)
            wordMap[token] = (wordMap[token] ?: 0) + 1
            categoryTotalWords[category] = (categoryTotalWords[category] ?: 0) + 1
        }
    }

    /**
     * Predicts the most likely category for a given title/note text.
     * Returns a ranked list of (Category, Confidence Score 0..1).
     */
    fun predict(text: String): List<Pair<String, Double>> {
        val tokens = tokenize(text)
        if (tokens.isEmpty() || totalDocuments == 0 || categoryDocCounts.isEmpty()) {
            return emptyList()
        }

        val vocabSize = vocabulary.size.coerceAtLeast(1)
        val logScores = mutableMapOf<String, Double>()

        for ((category, docCount) in categoryDocCounts) {
            // Prior probability: ln(P(Category))
            var logProb = ln(docCount.toDouble() / totalDocuments.toDouble())

            val wordMap = categoryWordCounts[category] ?: emptyMap()
            val totalWords = categoryTotalWords[category] ?: 0

            // Likelihood: ln(P(w | Category)) with Laplace smoothing (+1)
            for (token in tokens) {
                val count = wordMap[token] ?: 0
                val wordProb = (count + 1.0) / (totalWords + vocabSize)
                logProb += ln(wordProb)
            }

            logScores[category] = logProb
        }

        if (logScores.isEmpty()) return emptyList()

        // Softmax normalization to convert log probabilities to 0..1 confidence values
        val maxLog = logScores.values.maxOrNull() ?: 0.0
        val expScores = logScores.mapValues { kotlin.math.exp(it.value - maxLog) }
        val sumExp = expScores.values.sum().coerceAtLeast(1e-9)

        return expScores.map { (cat, expVal) ->
            cat to (expVal / sumExp)
        }.sortedByDescending { it.second }
    }

    fun clear() {
        categoryDocCounts.clear()
        categoryWordCounts.clear()
        categoryTotalWords.clear()
        vocabulary.clear()
        totalDocuments = 0
    }

    private fun tokenize(text: String): List<String> {
        return text.lowercase(Locale.getDefault())
            .replace(Regex("[^a-zA-Z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .map { it.trim() }
            .filter { it.length >= 2 && !STOP_WORDS.contains(it) }
    }

    companion object {
        private val STOP_WORDS = setOf(
            "the", "a", "an", "and", "or", "to", "for", "in", "on", "at", "by", "of",
            "from", "with", "is", "was", "it", "my", "payment", "paid", "transaction", "cost"
        )
    }
}
