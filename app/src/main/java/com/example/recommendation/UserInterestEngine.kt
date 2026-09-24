package com.example.recommendation

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Advanced Recommendation & User Interest Analysis Engine for Meskot Global.
 * Implements the system prompt, signal weighting rules, recency decay, confidence rating,
 * and Gemini 3.5 Flash REST API + On-device deterministic execution.
 */
object UserInterestEngine {

    private const val TAG = "UserInterestEngine"
    private const val GEMINI_MODEL = "gemini-3.5-flash"
    private const val GEMINI_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/$GEMINI_MODEL:generateContent"

    val SYSTEM_INSTRUCTION = """
You are an Advanced Recommendation & User Interest Analysis Engine for a content platform. Your task is to process a batch of raw user interaction event logs, deduce the user's short-term and long-term interest profile, and generate ranked content targets for their feed.

### Core Processing Rules:
1. Signal Weighting Strategy:
   - FAST_SKIP (Watched < 15% or < 2s): -3.0 penalty score to topic tags.
   - DWELL/READ (Stayed 5s+ without skip): +1.0 boost score to topic tags.
   - FULL_WATCH (Watched > 90% or rewatched): +2.5 boost score to topic tags.
   - EXPLICIT_LIKE / FAVORITE: +3.5 boost score to topic tags.
   - SHARE / SAVE: +5.0 boost score to topic tags.

2. Decay & Recency:
   - Give higher weight to events with the most recent timestamps.
   - Repeated engagement with the same tag boosts the "affinity_confidence" rating.

3. Output Requirements:
   - Output ONLY valid JSON matching the specified schema.
   - Do NOT include conversational filler, markdown commentary, or extra text outside the JSON block.

### Output JSON Schema:
{
  "user_id": "string",
  "interest_profile": {
    "primary_interests": [
      {
        "tag": "string",
        "affinity_score": 0.0 to 10.0,
        "confidence": "HIGH | MEDIUM | LOW",
        "primary_trigger": "string"
      }
    ],
    "disliked_or_fatigued_topics": [
      {
        "tag": "string",
        "penalty_score": -10.0 to 0.0,
        "reason": "FAST_SKIP | EXPLICIT_DISLIKE"
      }
    ]
  },
  "feed_generation_strategy": {
    "recommended_content_tags": ["tag1", "tag2"],
    "exploration_tags": ["tag1"],
    "diversity_ratio": "e.g., 70% primary, 20% secondary, 10% discovery"
  }
}
""".trimIndent()

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Executes the analysis. If Gemini API key is valid, queries Gemini 3.5 Flash;
     * otherwise or on failure, falls back to the local deterministic engine.
     */
    suspend fun analyze(batch: UserEventBatch, preferLocal: Boolean = false): UserInterestAnalysisResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        if (preferLocal) {
            val localResult = processLocally(batch)
            return@withContext localResult.copy(
                latencyMs = System.currentTimeMillis() - startTime,
                sourceEngine = "On-Device Engine"
            )
        }

        val apiKey = getApiKey()
        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d(TAG, "No valid Gemini API key found, executing on-device engine.")
            val localResult = processLocally(batch)
            return@withContext localResult.copy(
                latencyMs = System.currentTimeMillis() - startTime,
                sourceEngine = "On-Device Engine (Deterministic)"
            )
        }

        try {
            val geminiResult = callGeminiApi(batch, apiKey)
            val duration = System.currentTimeMillis() - startTime
            return@withContext geminiResult.copy(
                latencyMs = duration,
                sourceEngine = "Gemini 3.5 Flash"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API call failed, falling back to local engine: ${e.message}", e)
            val localResult = processLocally(batch)
            return@withContext localResult.copy(
                latencyMs = System.currentTimeMillis() - startTime,
                sourceEngine = "On-Device Fallback (${e.message?.take(30)})"
            )
        }
    }

    private fun getApiKey(): String? {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            null
        }
    }

    /**
     * Direct REST call to Gemini 3.5 Flash API with system instructions and JSON response schema.
     */
    private suspend fun callGeminiApi(batch: UserEventBatch, apiKey: String): UserInterestAnalysisResult = withContext(Dispatchers.IO) {
        val url = "$GEMINI_ENDPOINT?key=$apiKey"

        val userPrompt = "Analyze the following user interaction event logs and return the updated interest profile:\n\n${batch.toJsonString(2)}"

        // Build Gemini REST Payload
        val requestJson = JSONObject().apply {
            // System instructions
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", SYSTEM_INSTRUCTION) })
                })
            })

            // Content parts
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", userPrompt) })
                    })
                })
            })

            // Generation config with structured JSON output
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.2)
                put("topP", 0.9)
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: ""
            throw IllegalStateException("Gemini API error code ${response.code}: $errorBody")
        }

        val responseBodyStr = response.body?.string() ?: throw IllegalStateException("Empty response from Gemini")
        val responseObj = JSONObject(responseBodyStr)

        val candidates = responseObj.optJSONArray("candidates")
        val firstCandidate = candidates?.optJSONObject(0)
        val content = firstCandidate?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val text = parts?.optJSONObject(0)?.optString("text")

        if (text.isNullOrBlank()) {
            throw IllegalStateException("No content text received from Gemini candidates")
        }

        return@withContext UserInterestAnalysisResult.fromJsonString(text, sourceEngine = "Gemini 3.5 Flash")
    }

    /**
     * High-precision on-device deterministic implementation of the exact scoring rules:
     * - Signal weighting: FAST_SKIP (-3.0), DWELL (+1.0), FULL_WATCH (+2.5), LIKE (+3.5), SHARE/SAVE (+5.0)
     * - Timestamp decay and recency weighting
     * - Affinity confidence rating based on repeated engagement
     * - Generates schema-compliant JSON output
     */
    fun processLocally(batch: UserEventBatch): UserInterestAnalysisResult {
        val events = batch.recentEvents

        // Parse timestamps or fallback to index-based recency
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val parsedTimes = events.map { event ->
            try {
                if (event.timestamp.isNotBlank()) dateFormat.parse(event.timestamp)?.time ?: 0L else 0L
            } catch (e: Exception) {
                0L
            }
        }

        val maxTime = parsedTimes.maxOrNull() ?: 0L
        val minTime = parsedTimes.minOrNull() ?: 0L
        val timeSpan = max(1L, maxTime - minTime)

        data class TagStats(
            var rawScore: Double = 0.0,
            var engagementCount: Int = 0,
            val triggers: MutableList<String> = mutableListOf(),
            val fastSkipReasons: MutableList<String> = mutableListOf()
        )

        val tagMap = mutableMapOf<String, TagStats>()

        events.forEachIndexed { index, event ->
            // Recency decay factor: newest events get weight 1.0 down to 0.7 for older events
            val recencyFactor = if (maxTime > 0L) {
                val t = parsedTimes[index]
                0.70 + 0.30 * ((t - minTime).toDouble() / timeSpan)
            } else {
                0.70 + 0.30 * ((index + 1).toDouble() / max(1, events.size))
            }

            var eventScore = 0.0
            val eventTriggers = mutableListOf<String>()
            var isSkip = false

            val normAction = event.action.uppercase(Locale.US)

            // Rule 1: FAST_SKIP (Watched < 15% or < 2s) -> -3.0 penalty
            if (normAction == "FAST_SKIP" || event.watchPercentage < 0.15 || event.dwellTimeSec < 2) {
                eventScore += -3.0
                eventTriggers.add("FAST_SKIP")
                isSkip = true
            }

            // Rule 2: DWELL/READ (Stayed 5s+ without skip) -> +1.0 boost
            if (!isSkip && event.dwellTimeSec >= 5) {
                eventScore += 1.0
                eventTriggers.add("DWELL (5s+)")
            }

            // Rule 3: FULL_WATCH (Watched > 90% or rewatched) -> +2.5 boost
            if (event.watchPercentage >= 0.90) {
                eventScore += 2.5
                eventTriggers.add("FULL_WATCH (90%+)")
            }

            // Rule 4: EXPLICIT_LIKE / FAVORITE -> +3.5 boost
            if (normAction.contains("LIKE") || normAction.contains("FAVORITE")) {
                eventScore += 3.5
                eventTriggers.add("EXPLICIT_LIKE")
            }

            // Rule 5: SHARE / SAVE -> +5.0 boost
            if (normAction.contains("SHARE") || normAction.contains("SAVE") || normAction.contains("BOOKMARK")) {
                eventScore += 5.0
                eventTriggers.add("SHARE/SAVE")
            }

            val weightedScore = eventScore * recencyFactor

            event.tags.forEach { rawTag ->
                val cleanTag = rawTag.trim().lowercase(Locale.US)
                if (cleanTag.isNotBlank()) {
                    val stats = tagMap.getOrPut(cleanTag) { TagStats() }
                    stats.rawScore += weightedScore
                    if (eventScore > 0) stats.engagementCount++
                    if (isSkip) {
                        stats.fastSkipReasons.add(normAction.ifBlank { "FAST_SKIP" })
                    }
                    stats.triggers.addAll(eventTriggers)
                }
            }
        }

        // Partition tags into primary interests vs disliked/fatigued topics
        val primaryInterests = mutableListOf<PrimaryInterest>()
        val dislikedTopics = mutableListOf<DislikedTopic>()

        tagMap.forEach { (tag, stats) ->
            if (stats.rawScore > 0.0) {
                // Scale score to 0.0 .. 10.0
                val affinity = min(10.0, max(0.5, stats.rawScore)).let {
                    ((it * 10).roundToInt()) / 10.0
                }

                // Confidence rating: boosted by repeated engagement with the same tag
                val confidence = when {
                    stats.engagementCount >= 2 || affinity >= 7.5 -> "HIGH"
                    stats.engagementCount == 1 || affinity >= 3.5 -> "MEDIUM"
                    else -> "LOW"
                }

                val primaryTrigger = stats.triggers.distinct().take(3).joinToString(" & ").ifBlank { "ENGAGEMENT_DWELL" }

                primaryInterests.add(
                    PrimaryInterest(
                        tag = tag,
                        affinityScore = affinity,
                        confidence = confidence,
                        primaryTrigger = primaryTrigger
                    )
                )
            } else if (stats.rawScore < 0.0) {
                // Penalty score: -10.0 to 0.0
                val penalty = max(-10.0, min(-0.5, stats.rawScore)).let {
                    ((it * 10).roundToInt()) / 10.0
                }

                val reason = if (stats.fastSkipReasons.any { it.contains("DISLIKE") }) {
                    "EXPLICIT_DISLIKE"
                } else {
                    "FAST_SKIP"
                }

                dislikedTopics.add(
                    DislikedTopic(
                        tag = tag,
                        penaltyScore = penalty,
                        reason = reason
                    )
                )
            }
        }

        // Sort primary interests by affinity descending
        primaryInterests.sortByDescending { it.affinityScore }
        // Sort disliked topics by severity (most negative first)
        dislikedTopics.sortBy { it.penaltyScore }

        // Recommended content tags from top primary interests
        val recommendedTags = primaryInterests.take(4).map { it.tag }

        // Exploration tags: tangential or secondary topics
        val explorationTags = if (primaryInterests.size > 4) {
            primaryInterests.drop(4).take(2).map { it.tag }
        } else {
            // Suggest discovery tags
            listOf("jetpack_compose", "habesha_tech", "creators").filterNot { recommendedTags.contains(it) }.take(1)
        }

        val feedStrategy = FeedGenerationStrategy(
            recommendedContentTags = recommendedTags,
            explorationTags = explorationTags,
            diversityRatio = "70% primary, 20% secondary, 10% discovery"
        )

        val interestProfile = InterestProfile(
            primaryInterests = primaryInterests,
            dislikedOrFatiguedTopics = dislikedTopics
        )

        return UserInterestAnalysisResult(
            userId = batch.userId,
            interestProfile = interestProfile,
            feedGenerationStrategy = feedStrategy,
            sourceEngine = "On-Device Engine"
        )
    }
}
