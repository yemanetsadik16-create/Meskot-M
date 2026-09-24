package com.example

import com.example.data.Post
import com.example.recommendation.UserEventBatch
import com.example.recommendation.UserInteractionEvent
import com.example.recommendation.UserInterestEngine
import com.example.recommendation.UserInterestTracker
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class UserInterestEngineTest {

    @Test
    fun testBenchmarkBatchProcessesAccurately() {
        val batch = UserEventBatch.SampleBenchmarkBatch
        val result = UserInterestEngine.processLocally(batch)

        // 1. Verify user id
        assertEquals("usr_7821", result.userId)

        // 2. Verify primary interests have valid scores and tags
        assertFalse("Primary interests should not be empty", result.interestProfile.primaryInterests.isEmpty())
        result.interestProfile.primaryInterests.forEach { interest ->
            assertTrue("Affinity score should be between 0.0 and 10.0", interest.affinityScore in 0.0..10.0)
            assertTrue("Confidence must be HIGH, MEDIUM, or LOW", interest.confidence in listOf("HIGH", "MEDIUM", "LOW"))
            assertTrue("Primary trigger must be non-empty", interest.primaryTrigger.isNotBlank())
        }

        // 3. Verify disliked / fatigued topics have negative penalties
        assertFalse("Disliked topics should catch fast skips", result.interestProfile.dislikedOrFatiguedTopics.isEmpty())
        result.interestProfile.dislikedOrFatiguedTopics.forEach { disliked ->
            assertTrue("Penalty score must be <= 0.0 and >= -10.0", disliked.penaltyScore in -10.0..0.0)
            assertTrue("Reason must be valid", disliked.reason in listOf("FAST_SKIP", "EXPLICIT_DISLIKE"))
        }

        // 4. Verify feed generation strategy
        assertFalse("Recommended content tags should be populated", result.feedGenerationStrategy.recommendedContentTags.isEmpty())
        assertTrue("Diversity ratio should match requirement", result.feedGenerationStrategy.diversityRatio.contains("%"))

        // 5. Verify JSON output compliance
        val jsonString = result.toFormattedJson()
        val json = JSONObject(jsonString)
        assertEquals("usr_7821", json.getString("user_id"))
        assertTrue(json.has("interest_profile"))
        assertTrue(json.has("feed_generation_strategy"))

        val profile = json.getJSONObject("interest_profile")
        assertTrue(profile.has("primary_interests"))
        assertTrue(profile.has("disliked_or_fatigued_topics"))
    }

    @Test
    fun testFastSkipPenaltyRule() {
        val singleSkipBatch = UserEventBatch(
            userId = "usr_test_skip",
            recentEvents = listOf(
                UserInteractionEvent(
                    itemId = "vid_bad_crypto",
                    tags = listOf("crypto_scam"),
                    watchPercentage = 0.05,
                    dwellTimeSec = 1,
                    action = "FAST_SKIP",
                    timestamp = "2026-09-23T12:00:00Z"
                )
            )
        )

        val result = UserInterestEngine.processLocally(singleSkipBatch)
        val penalized = result.interestProfile.dislikedOrFatiguedTopics.find { it.tag == "crypto_scam" }
        assertNotNull("crypto_scam should be recorded in disliked topics", penalized)
        assertTrue("Penalty should be negative", (penalized?.penaltyScore ?: 0.0) < 0.0)
        assertEquals("FAST_SKIP", penalized?.reason)
    }

    @Test
    fun testHighEngagementConfidenceBoost() {
        val engagementBatch = UserEventBatch(
            userId = "usr_test_eng",
            recentEvents = listOf(
                UserInteractionEvent(
                    itemId = "vid_dev_1",
                    tags = listOf("android_development"),
                    watchPercentage = 0.95,
                    dwellTimeSec = 20,
                    action = "FULL_WATCH",
                    timestamp = "2026-09-23T10:00:00Z"
                ),
                UserInteractionEvent(
                    itemId = "vid_dev_2",
                    tags = listOf("android_development"),
                    watchPercentage = 0.90,
                    dwellTimeSec = 35,
                    action = "EXPLICIT_LIKE",
                    timestamp = "2026-09-23T10:05:00Z"
                ),
                UserInteractionEvent(
                    itemId = "vid_dev_3",
                    tags = listOf("android_development"),
                    watchPercentage = 1.0,
                    dwellTimeSec = 45,
                    action = "SHARE",
                    timestamp = "2026-09-23T10:10:00Z"
                )
            )
        )

        val result = UserInterestEngine.processLocally(engagementBatch)
        val topInterest = result.interestProfile.primaryInterests.find { it.tag == "android_development" }
        assertNotNull(topInterest)
        assertEquals("Repeated high engagement should boost confidence to HIGH", "HIGH", topInterest?.confidence)
        assertTrue("Score should reflect full watch + like + share boosts", (topInterest?.affinityScore ?: 0.0) >= 7.0)
    }

    @Test
    fun testRelevanceWeightingOnFeedPost() {
        val result = UserInterestEngine.processLocally(UserEventBatch.SampleBenchmarkBatch)
        UserInterestTracker.setProfileForTesting(result)

        val likedPost = Post(
            id = "p_android_kotlin",
            uid = "creator_1",
            authorName = "Android Dev",
            text = "Building modern Jetpack Compose apps with Kotlin #android #kotlin"
        )

        val weight = UserInterestTracker.computePostRelevanceWeight(likedPost)
        assertTrue("Relevance weight for high-affinity post ($weight) should be > 1.0", weight > 1.0)

        val dislikedPost = Post(
            id = "p_crypto_scam",
            uid = "creator_2",
            authorName = "Crypto Guy",
            text = "100x crypto trading strategy #crypto_trading #finance"
        )
        val dislikedWeight = UserInterestTracker.computePostRelevanceWeight(dislikedPost)
        assertTrue("Relevance weight for penalized post ($dislikedWeight) should be < 1.0", dislikedWeight < 1.0)
    }
}
