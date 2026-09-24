package com.example.recommendation

import android.util.Log
import com.example.data.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tracks user interaction event logs in real time, manages batches,
 * triggers the recommendation analysis engine, and scores feed content.
 */
object UserInterestTracker {

    private const val TAG = "UserInterestTracker"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

    private val _recentEvents = MutableStateFlow<List<UserInteractionEvent>>(
        UserEventBatch.SampleBenchmarkBatch.recentEvents
    )
    val recentEvents: StateFlow<List<UserInteractionEvent>> = _recentEvents.asStateFlow()

    private val _activeProfile = MutableStateFlow<UserInterestAnalysisResult?>(null)
    val activeProfile: StateFlow<UserInterestAnalysisResult?> = _activeProfile.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val _personalizationEnabled = MutableStateFlow(true)
    val personalizationEnabled: StateFlow<Boolean> = _personalizationEnabled.asStateFlow()

    init {
        // Run initial local analysis with the preloaded events so user immediately has an active profile
        scope.launch {
            try {
                val batch = UserEventBatch(
                    userId = "usr_7821",
                    recentEvents = _recentEvents.value
                )
                val initialResult = UserInterestEngine.processLocally(batch)
                _activeProfile.value = initialResult
            } catch (e: Exception) {
                Log.e(TAG, "Failed initial analysis: ${e.message}")
            }
        }
    }

    /**
     * Records a user interaction event into the current batch.
     */
    fun recordEvent(
        itemId: String,
        tags: List<String>,
        watchPercentage: Double,
        dwellTimeSec: Int,
        action: String,
        userId: String = "usr_current"
    ) {
        val event = UserInteractionEvent(
            itemId = itemId,
            tags = tags.ifEmpty { listOf("general_feed") },
            watchPercentage = watchPercentage,
            dwellTimeSec = dwellTimeSec,
            action = action,
            timestamp = dateFormat.format(Date())
        )

        // Keep most recent 50 events in memory
        val updated = (listOf(event) + _recentEvents.value).take(50)
        _recentEvents.value = updated
        Log.d(TAG, "Recorded event on $itemId: action=$action, dwell=${dwellTimeSec}s, tags=$tags")
    }

    /**
     * Executes the interest analysis engine on the current batch of events.
     */
    fun runAnalysis(
        userId: String = "usr_7821",
        preferLocal: Boolean = false,
        onComplete: ((UserInterestAnalysisResult) -> Unit)? = null
    ) {
        scope.launch {
            _isAnalyzing.value = true
            _lastError.value = null
            try {
                val batch = UserEventBatch(
                    userId = userId,
                    recentEvents = _recentEvents.value.ifEmpty {
                        UserEventBatch.SampleBenchmarkBatch.recentEvents
                    }
                )
                val result = UserInterestEngine.analyze(batch, preferLocal = preferLocal)
                _activeProfile.value = result
                onComplete?.invoke(result)
            } catch (e: Exception) {
                Log.e(TAG, "Analysis error: ${e.message}", e)
                _lastError.value = e.message
                // Fallback to local
                val fallback = UserInterestEngine.processLocally(
                    UserEventBatch(userId, _recentEvents.value)
                )
                _activeProfile.value = fallback
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    /**
     * Loads the benchmark reference sample dataset from the prompt instructions.
     */
    fun loadBenchmarkSample() {
        _recentEvents.value = UserEventBatch.SampleBenchmarkBatch.recentEvents
        runAnalysis(userId = UserEventBatch.SampleBenchmarkBatch.userId, preferLocal = true)
    }

    fun clearEvents() {
        _recentEvents.value = emptyList()
    }

    fun togglePersonalization(enabled: Boolean) {
        _personalizationEnabled.value = enabled
    }

    /**
     * Computes an algorithmic interest relevance modifier for feed ranking.
     * Uses primary interest affinity scores, recommended tags, and penalizes disliked/skipped topics.
     */
    fun computePostRelevanceWeight(post: Post): Double {
        if (!_personalizationEnabled.value) return 1.0

        val profile = _activeProfile.value ?: return 1.0
        val postTags = post.effectiveTags().map { it.lowercase() }

        var boostScore = 0.0
        var penaltyScore = 0.0

        // Check primary interests match
        profile.interestProfile.primaryInterests.forEach { interest ->
            val tag = interest.tag.lowercase()
            if (postTags.any { it.contains(tag) || tag.contains(it) }) {
                // Boost proportional to affinity score (0.0 .. 10.0)
                val multiplier = when (interest.confidence) {
                    "HIGH" -> 1.5
                    "MEDIUM" -> 1.2
                    else -> 1.0
                }
                boostScore += (interest.affinityScore * multiplier * 2.0)
            }
        }

        // Check recommended content tags
        profile.feedGenerationStrategy.recommendedContentTags.forEach { recTag ->
            val tag = recTag.lowercase()
            if (postTags.any { it.contains(tag) || tag.contains(it) }) {
                boostScore += 10.0
            }
        }

        // Check exploration tags (slight discovery bump)
        profile.feedGenerationStrategy.explorationTags.forEach { expTag ->
            val tag = expTag.lowercase()
            if (postTags.any { it.contains(tag) || tag.contains(it) }) {
                boostScore += 4.0
            }
        }

        // Check disliked / fatigued topics penalty
        profile.interestProfile.dislikedOrFatiguedTopics.forEach { disliked ->
            val tag = disliked.tag.lowercase()
            if (postTags.any { it.contains(tag) || tag.contains(it) }) {
                // penaltyScore is negative, e.g. -3.0 .. -10.0
                penaltyScore += disliked.penaltyScore * 5.0
            }
        }

        val netMultiplier = 1.0 + (boostScore + penaltyScore) / 20.0
        return netMultiplier.coerceIn(0.1, 15.0)
    }

    fun setProfileForTesting(result: UserInterestAnalysisResult?) {
        _activeProfile.value = result
    }
}
