package com.example.recommendation

import org.json.JSONArray
import org.json.JSONObject

/**
 * Raw interaction event emitted during user content consumption (Feed, Reels, Stories).
 */
data class UserInteractionEvent(
    val itemId: String,
    val tags: List<String>,
    val watchPercentage: Double, // 0.0 to 1.0+
    val dwellTimeSec: Int,
    val action: String, // FAST_SKIP, DWELL, FULL_WATCH, LIKE, SHARE, SAVE, FAVORITE
    val timestamp: String // ISO-8601 string, e.g. "2026-09-23T08:00:00Z"
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("item_id", itemId)
            put("tags", JSONArray(tags))
            put("watch_percentage", watchPercentage)
            put("dwell_time_sec", dwellTimeSec)
            put("action", action)
            put("timestamp", timestamp)
        }
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): UserInteractionEvent {
            val tagsArr = obj.optJSONArray("tags")
            val tagsList = mutableListOf<String>()
            if (tagsArr != null) {
                for (i in 0 until tagsArr.length()) {
                    tagsList.add(tagsArr.getString(i))
                }
            }
            return UserInteractionEvent(
                itemId = obj.optString("item_id", ""),
                tags = tagsList,
                watchPercentage = obj.optDouble("watch_percentage", 0.0),
                dwellTimeSec = obj.optInt("dwell_time_sec", 0),
                action = obj.optString("action", "DWELL"),
                timestamp = obj.optString("timestamp", "")
            )
        }
    }
}

data class UserEventBatch(
    val userId: String,
    val recentEvents: List<UserInteractionEvent>
) {
    fun toJsonString(indentSpaces: Int = 2): String {
        val root = JSONObject()
        root.put("user_id", userId)
        val arr = JSONArray()
        recentEvents.forEach { event ->
            arr.put(event.toJsonObject())
        }
        root.put("recent_events", arr)
        return root.toString(indentSpaces)
    }

    companion object {
        fun fromJsonString(jsonStr: String): UserEventBatch {
            val root = JSONObject(jsonStr)
            val userId = root.optString("user_id", "unknown_user")
            val eventsArr = root.optJSONArray("recent_events") ?: JSONArray()
            val list = mutableListOf<UserInteractionEvent>()
            for (i in 0 until eventsArr.length()) {
                list.add(UserInteractionEvent.fromJsonObject(eventsArr.getJSONObject(i)))
            }
            return UserEventBatch(userId, list)
        }

        /**
         * Prompt benchmark reference sample data (from prompt spec).
         */
        val SampleBenchmarkBatch = UserEventBatch(
            userId = "usr_7821",
            recentEvents = listOf(
                UserInteractionEvent(
                    itemId = "vid_101",
                    tags = listOf("mobile_development", "kotlin", "android"),
                    watchPercentage = 1.0,
                    dwellTimeSec = 45,
                    action = "SHARE",
                    timestamp = "2026-09-23T08:00:00Z"
                ),
                UserInteractionEvent(
                    itemId = "vid_102",
                    tags = listOf("celebrity_gossip", "pop_culture"),
                    watchPercentage = 0.05,
                    dwellTimeSec = 1,
                    action = "FAST_SKIP",
                    timestamp = "2026-09-23T08:01:00Z"
                ),
                UserInteractionEvent(
                    itemId = "vid_103",
                    tags = listOf("kotlin", "jetpack_compose", "ui_design"),
                    watchPercentage = 0.95,
                    dwellTimeSec = 60,
                    action = "LIKE",
                    timestamp = "2026-09-23T08:02:00Z"
                ),
                UserInteractionEvent(
                    itemId = "vid_104",
                    tags = listOf("crypto_trading", "finance"),
                    watchPercentage = 0.10,
                    dwellTimeSec = 2,
                    action = "FAST_SKIP",
                    timestamp = "2026-09-23T08:03:00Z"
                )
            )
        )
    }
}

data class PrimaryInterest(
    val tag: String,
    val affinityScore: Double, // 0.0 to 10.0
    val confidence: String, // "HIGH" | "MEDIUM" | "LOW"
    val primaryTrigger: String
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("tag", tag)
        put("affinity_score", ((affinityScore * 10).toInt()) / 10.0)
        put("confidence", confidence)
        put("primary_trigger", primaryTrigger)
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): PrimaryInterest {
            return PrimaryInterest(
                tag = obj.optString("tag", ""),
                affinityScore = obj.optDouble("affinity_score", 0.0),
                confidence = obj.optString("confidence", "MEDIUM"),
                primaryTrigger = obj.optString("primary_trigger", "")
            )
        }
    }
}

data class DislikedTopic(
    val tag: String,
    val penaltyScore: Double, // -10.0 to 0.0
    val reason: String // "FAST_SKIP" | "EXPLICIT_DISLIKE"
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("tag", tag)
        put("penalty_score", ((penaltyScore * 10).toInt()) / 10.0)
        put("reason", reason)
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): DislikedTopic {
            return DislikedTopic(
                tag = obj.optString("tag", ""),
                penaltyScore = obj.optDouble("penalty_score", 0.0),
                reason = obj.optString("reason", "FAST_SKIP")
            )
        }
    }
}

data class InterestProfile(
    val primaryInterests: List<PrimaryInterest> = emptyList(),
    val dislikedOrFatiguedTopics: List<DislikedTopic> = emptyList()
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        val piArr = JSONArray()
        primaryInterests.forEach { piArr.put(it.toJsonObject()) }
        put("primary_interests", piArr)

        val dtArr = JSONArray()
        dislikedOrFatiguedTopics.forEach { dtArr.put(it.toJsonObject()) }
        put("disliked_or_fatigued_topics", dtArr)
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): InterestProfile {
            val piList = mutableListOf<PrimaryInterest>()
            val piArr = obj.optJSONArray("primary_interests")
            if (piArr != null) {
                for (i in 0 until piArr.length()) {
                    piList.add(PrimaryInterest.fromJsonObject(piArr.getJSONObject(i)))
                }
            }

            val dtList = mutableListOf<DislikedTopic>()
            val dtArr = obj.optJSONArray("disliked_or_fatigued_topics")
            if (dtArr != null) {
                for (i in 0 until dtArr.length()) {
                    dtList.add(DislikedTopic.fromJsonObject(dtArr.getJSONObject(i)))
                }
            }

            return InterestProfile(primaryInterests = piList, dislikedOrFatiguedTopics = dtList)
        }
    }
}

data class FeedGenerationStrategy(
    val recommendedContentTags: List<String> = emptyList(),
    val explorationTags: List<String> = emptyList(),
    val diversityRatio: String = "70% primary, 20% secondary, 10% discovery"
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("recommended_content_tags", JSONArray(recommendedContentTags))
        put("exploration_tags", JSONArray(explorationTags))
        put("diversity_ratio", diversityRatio)
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): FeedGenerationStrategy {
            val recList = mutableListOf<String>()
            val recArr = obj.optJSONArray("recommended_content_tags")
            if (recArr != null) {
                for (i in 0 until recArr.length()) {
                    recList.add(recArr.getString(i))
                }
            }

            val expList = mutableListOf<String>()
            val expArr = obj.optJSONArray("exploration_tags")
            if (expArr != null) {
                for (i in 0 until expArr.length()) {
                    expList.add(expArr.getString(i))
                }
            }

            return FeedGenerationStrategy(
                recommendedContentTags = recList,
                explorationTags = expList,
                diversityRatio = obj.optString("diversity_ratio", "70% primary, 20% secondary, 10% discovery")
            )
        }
    }
}

data class UserInterestAnalysisResult(
    val userId: String,
    val interestProfile: InterestProfile,
    val feedGenerationStrategy: FeedGenerationStrategy,
    val rawJson: String = "",
    val sourceEngine: String = "Local Engine",
    val latencyMs: Long = 0L,
    val analyzedAt: Long = System.currentTimeMillis()
) {
    fun toFormattedJson(): String {
        return if (rawJson.isNotBlank()) {
            try {
                JSONObject(rawJson).toString(2)
            } catch (e: Exception) {
                buildRawJson()
            }
        } else {
            buildRawJson()
        }
    }

    private fun buildRawJson(): String {
        val root = JSONObject().apply {
            put("user_id", userId)
            put("interest_profile", interestProfile.toJsonObject())
            put("feed_generation_strategy", feedGenerationStrategy.toJsonObject())
        }
        return root.toString(2)
    }

    companion object {
        fun fromJsonString(jsonStr: String, sourceEngine: String = "Gemini", latencyMs: Long = 0L): UserInterestAnalysisResult {
            // Clean markdown blocks if present (e.g. ```json ... ```)
            val cleanStr = jsonStr.trim().let { raw ->
                var s = raw
                if (s.startsWith("```json")) s = s.removePrefix("```json")
                if (s.startsWith("```")) s = s.removePrefix("```")
                if (s.endsWith("```")) s = s.removeSuffix("```")
                s.trim()
            }

            val root = JSONObject(cleanStr)
            val userId = root.optString("user_id", "unknown_user")
            val ipObj = root.optJSONObject("interest_profile") ?: JSONObject()
            val interestProfile = InterestProfile.fromJsonObject(ipObj)

            val fgsObj = root.optJSONObject("feed_generation_strategy") ?: JSONObject()
            val feedGenerationStrategy = FeedGenerationStrategy.fromJsonObject(fgsObj)

            return UserInterestAnalysisResult(
                userId = userId,
                interestProfile = interestProfile,
                feedGenerationStrategy = feedGenerationStrategy,
                rawJson = cleanStr,
                sourceEngine = sourceEngine,
                latencyMs = latencyMs,
                analyzedAt = System.currentTimeMillis()
            )
        }
    }
}
