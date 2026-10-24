package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldDeep

/**
 * 5 Major Interactive Social Entities:
 * 1. URL -> Opens browser
 * 2. Email -> mailto:
 * 3. Phone -> tel: dialer
 * 4. Hashtag -> Feed filter
 * 5. Mention -> Profile or group navigation
 */
enum class EntityType {
    URL,
    EMAIL,
    PHONE,
    HASHTAG,
    MENTION
}

data class ParsedEntity(
    val type: EntityType,
    val rawText: String,
    val value: String,
    val startIndex: Int,
    val endIndex: Int
)

object InteractiveTextParser {

    // Regex definitions
    // Matches http://, https://, www., and common domain suffixes (e.g. google.com, meskot.et, github.com)
    private val URL_REGEX = Regex(
        "(?:https?://|www\\.|(?:[a-zA-Z0-9-]+\\.)+(?:com|org|net|et|edu|gov|io|me|app|ai|co|info|biz|tv|ly|online|xyz|store|dev|tech|page))" +
        "[-a-zA-Z0-9@:%._+~#=]{0,256}(?:/[a-zA-Z0-9()@:%_+.~#?&/=]*)?",
        RegexOption.IGNORE_CASE
    )

    private val EMAIL_REGEX = Regex(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",
        RegexOption.IGNORE_CASE
    )

    // Supports domestic (e.g. 0911234567, 0712345678, 0911-23-45-67) and international (+251..., +1-800-555-0199) formats
    private val PHONE_REGEX = Regex(
        "(?:\\+?\\d{1,4}[-.\\s]?)?(?:\\(\\d{2,4}\\)[-.\\s]?|\\d{2,4}[-.\\s]?)\\d{2,4}[-.\\s]?\\d{2,9}"
    )

    private val HASHTAG_REGEX = Regex(
        "#[\\w\\u1200-\\u137F]+" // Supports alphanumeric and Ethiopic Ge'ez script
    )

    private val MENTION_REGEX = Regex(
        "@[a-zA-Z0-9_\\u1200-\\u137F.]+"
    )

    // Trailing punctuation characters to strip from matches if attached to the end
    private val TRAILING_PUNCTUATION = setOf('.', ',', '!', '?', ';', ':', ')', ']', '}', '\'', '"', '>', '-')

    /**
     * Parses the given text and returns a sorted list of non-overlapping entities.
     */
    fun parse(text: String): List<ParsedEntity> {
        if (text.isBlank()) return emptyList()

        val rawMatches = mutableListOf<ParsedEntity>()

        // 1. URLs
        for (match in URL_REGEX.findAll(text)) {
            cleanAndAdd(match.value, match.range.first, EntityType.URL, rawMatches)
        }

        // 2. Emails
        for (match in EMAIL_REGEX.findAll(text)) {
            cleanAndAdd(match.value, match.range.first, EntityType.EMAIL, rawMatches)
        }

        // 3. Mentions
        for (match in MENTION_REGEX.findAll(text)) {
            cleanAndAdd(match.value, match.range.first, EntityType.MENTION, rawMatches)
        }

        // 4. Hashtags
        for (match in HASHTAG_REGEX.findAll(text)) {
            cleanAndAdd(match.value, match.range.first, EntityType.HASHTAG, rawMatches)
        }

        // 5. Phone numbers
        for (match in PHONE_REGEX.findAll(text)) {
            val trimmed = match.value.trim()
            val digitCount = trimmed.count { it.isDigit() }
            if (digitCount in 7..15) {
                // Ignore date formats such as 2026-10-20 or 20/10/2026
                val isDate = trimmed.matches(Regex("\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}|\\d{1,2}[-/]\\d{1,2}[-/]\\d{4}"))
                if (!isDate) {
                    cleanAndAdd(trimmed, match.range.first, EntityType.PHONE, rawMatches)
                }
            }
        }

        // Sort by start position and resolve overlaps (earlier or longer matches take precedence)
        rawMatches.sortBy { it.startIndex }
        val nonOverlapping = mutableListOf<ParsedEntity>()
        var lastEnd = 0

        for (item in rawMatches) {
            if (item.startIndex >= lastEnd) {
                nonOverlapping.add(item)
                lastEnd = item.endIndex
            }
        }

        return nonOverlapping
    }

    private fun cleanAndAdd(
        rawMatch: String,
        startIndex: Int,
        type: EntityType,
        outputList: MutableList<ParsedEntity>
    ) {
        var cleanValue = rawMatch
        var cutFromEnd = 0

        // Strip trailing punctuation cleanly
        while (cleanValue.isNotEmpty() && cleanValue.last() in TRAILING_PUNCTUATION) {
            cleanValue = cleanValue.dropLast(1)
            cutFromEnd++
        }

        if (cleanValue.isNotBlank()) {
            val adjustedEnd = startIndex + rawMatch.length - cutFromEnd
            outputList.add(
                ParsedEntity(
                    type = type,
                    rawText = cleanValue,
                    value = cleanValue,
                    startIndex = startIndex,
                    endIndex = adjustedEnd
                )
            )
        }
    }

    /**
     * Builds an AnnotatedString with clickable entity annotations and distinct styling.
     */
    fun buildAnnotatedStringWithEntities(
        text: String,
        entities: List<ParsedEntity>,
        linkColor: Color = GoldDeep,
        mentionColor: Color = Color(0xFF1877F2),
        hashtagColor: Color = GoldDeep
    ): AnnotatedString {
        return buildAnnotatedString {
            var currentPos = 0

            for (entity in entities) {
                if (entity.startIndex > currentPos) {
                    append(text.substring(currentPos, entity.startIndex))
                }

                pushStringAnnotation(tag = entity.type.name, annotation = entity.value)

                val (styleColor, weight, decoration) = when (entity.type) {
                    EntityType.URL -> Triple(linkColor, FontWeight.Medium, TextDecoration.Underline)
                    EntityType.EMAIL -> Triple(linkColor, FontWeight.Medium, TextDecoration.Underline)
                    EntityType.PHONE -> Triple(linkColor, FontWeight.SemiBold, TextDecoration.Underline)
                    EntityType.HASHTAG -> Triple(hashtagColor, FontWeight.Bold, TextDecoration.None)
                    EntityType.MENTION -> Triple(mentionColor, FontWeight.Bold, TextDecoration.None)
                }

                withStyle(
                    SpanStyle(
                        color = styleColor,
                        fontWeight = weight,
                        textDecoration = decoration
                    )
                ) {
                    append(entity.value)
                }

                pop()
                currentPos = entity.endIndex
            }

            if (currentPos < text.length) {
                append(text.substring(currentPos))
            }
        }
    }

    /**
     * Default intent handler for standard Android actions (browser, dialer, mail client).
     */
    fun handleDefaultEntityClick(
        context: Context,
        entityType: EntityType,
        value: String,
        onHashtagClick: ((String) -> Unit)? = null,
        onMentionClick: ((String) -> Unit)? = null
    ) {
        try {
            when (entityType) {
                EntityType.URL -> {
                    val fullUrl = if (!value.startsWith("http://", ignoreCase = true) && !value.startsWith("https://", ignoreCase = true)) {
                        "https://$value"
                    } else {
                        value
                    }
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Opening link: $fullUrl", Toast.LENGTH_SHORT).show()
                    }
                }
                EntityType.EMAIL -> {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$value")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Email: $value", Toast.LENGTH_SHORT).show()
                    }
                }
                EntityType.PHONE -> {
                    val cleanNumber = value.filter { it.isDigit() || it == '+' }
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Dialing: $cleanNumber", Toast.LENGTH_SHORT).show()
                    }
                }
                EntityType.HASHTAG -> {
                    val tag = value.removePrefix("#")
                    if (onHashtagClick != null) {
                        onHashtagClick(tag)
                    } else {
                        Toast.makeText(context, "Exploring #$tag on Meskot", Toast.LENGTH_SHORT).show()
                    }
                }
                EntityType.MENTION -> {
                    val username = value.removePrefix("@")
                    if (onMentionClick != null) {
                        onMentionClick(username)
                    } else {
                        Toast.makeText(context, "Viewing @$username", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Action: ${e.localizedMessage ?: "App not found"}", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Reusable Jetpack Compose Interactive Text Component
 * Backed by ClickableText with exact character range matching for reliable tap detection.
 */
@Composable
fun InteractiveSocialText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = 15.sp,
    lineHeight: TextUnit = 22.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    fontFamily: FontFamily? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    linkColor: Color = GoldDeep,
    mentionColor: Color = Color(0xFF1877F2),
    hashtagColor: Color = GoldDeep,
    onHashtagClick: ((String) -> Unit)? = null,
    onMentionClick: ((String) -> Unit)? = null,
    onUrlClick: ((String) -> Unit)? = null,
    onEmailClick: ((String) -> Unit)? = null,
    onPhoneClick: ((String) -> Unit)? = null,
    onNonEntityClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val entities = remember(text) { InteractiveTextParser.parse(text) }
    val annotatedString = remember(text, entities, linkColor, mentionColor, hashtagColor) {
        InteractiveTextParser.buildAnnotatedStringWithEntities(
            text = text,
            entities = entities,
            linkColor = linkColor,
            mentionColor = mentionColor,
            hashtagColor = hashtagColor
        )
    }

    ClickableText(
        text = annotatedString,
        modifier = modifier,
        style = TextStyle(
            color = color,
            fontSize = fontSize,
            lineHeight = lineHeight,
            fontWeight = fontWeight,
            fontFamily = fontFamily
        ),
        maxLines = maxLines,
        overflow = overflow,
        onClick = { characterPosition ->
            var clickedEntity = false

            val allAnnotations = annotatedString.getStringAnnotations(
                start = 0,
                end = annotatedString.length
            )
            val matched = allAnnotations.firstOrNull { characterPosition in it.start until it.end }

            if (matched != null) {
                clickedEntity = true
                val type = runCatching { EntityType.valueOf(matched.tag) }.getOrNull()
                if (type != null) {
                    when (type) {
                        EntityType.URL -> {
                            if (onUrlClick != null) onUrlClick(matched.item)
                            else InteractiveTextParser.handleDefaultEntityClick(context, type, matched.item)
                        }
                        EntityType.EMAIL -> {
                            if (onEmailClick != null) onEmailClick(matched.item)
                            else InteractiveTextParser.handleDefaultEntityClick(context, type, matched.item)
                        }
                        EntityType.PHONE -> {
                            if (onPhoneClick != null) onPhoneClick(matched.item)
                            else InteractiveTextParser.handleDefaultEntityClick(context, type, matched.item)
                        }
                        EntityType.HASHTAG -> {
                            if (onHashtagClick != null) onHashtagClick(matched.item)
                            else InteractiveTextParser.handleDefaultEntityClick(
                                context = context,
                                entityType = type,
                                value = matched.item
                            )
                        }
                        EntityType.MENTION -> {
                            if (onMentionClick != null) onMentionClick(matched.item)
                            else InteractiveTextParser.handleDefaultEntityClick(
                                context = context,
                                entityType = type,
                                value = matched.item
                            )
                        }
                    }
                }
            }

            if (!clickedEntity && onNonEntityClick != null) {
                onNonEntityClick()
            }
        }
    )
}
