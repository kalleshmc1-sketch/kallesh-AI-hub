package com.example.data.remote

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.net.URI
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GroundingWebSource(
    val title: String,
    val url: String,
    val domain: String,
    val publishedAt: String = "",
    val snippet: String = ""
) {
    fun toCitationString(): String {
        val cleanTitle = title.trim().ifBlank { domain.ifBlank { "Web Source" } }
        return if (publishedAt.isNotBlank()) {
            "$cleanTitle ($publishedAt) — $url"
        } else {
            "$cleanTitle — $url"
        }
    }
}

data class GroundingMetadataResult(
    val sources: List<GroundingWebSource> = emptyList(),
    val searchQueries: List<String> = emptyList(),
    val searchEntryPointHtml: String = ""
)

object GoogleSearchGroundingEngine {

    private val currentEventsKeywords = listOf(
        "today", "latest", "current", "news", "breaking", "live", "now",
        "recent", "update", "updates", "happening", "weather", "forecast",
        "stock", "price", "market", "bitcoin", "crypto", "score", "match",
        "tournament", "election", "president", "prime minister", "minister",
        "ceo", "release", "launched", "2025", "2026", "this week", "this month",
        "this year", "yesterday", "who won", "what happened", "schedule",
        "standings", "trending", "real-time", "realtime", "google search"
    )

    /**
     * Determines whether a prompt asks about current events, live facts, or real-time information.
     */
    fun isRealTimeOrCurrentEventsQuery(prompt: String): Boolean {
        val lower = prompt.trim().lowercase(Locale.ROOT)
        if (lower.isBlank()) return false
        return currentEventsKeywords.any { keyword -> lower.contains(keyword) }
    }

    /**
     * Returns a system instruction suffix that anchors the model to current real-time dates
     * and instructs it to synthesize grounded facts clearly.
     */
    fun buildGroundingSystemInstruction(): String {
        val nowFormatted = SimpleDateFormat("EEEE, MMMM d, yyyy HH:mm z", Locale.US).format(Date())
        return "\n[Google Search Real-Time Grounding Active]: Current date/time is $nowFormatted. " +
            "Use the googleSearch tool to retrieve up-to-date, factual information for current events, " +
            "recent developments, live metrics, and real-time questions. Clearly state dates, facts, " +
            "and context from the grounded search results."
    }

    /**
     * Parses Gemini API `groundingMetadata` from a candidate JSONObject.
     */
    fun parseGroundingMetadata(firstCandidate: JSONObject): GroundingMetadataResult {
        val grounding = firstCandidate.optJSONObject("groundingMetadata")
            ?: return GroundingMetadataResult()

        val queries = mutableListOf<String>()
        val searchQueriesArr = grounding.optJSONArray("webSearchQueries")
        if (searchQueriesArr != null) {
            for (i in 0 until searchQueriesArr.length()) {
                val q = searchQueriesArr.optString(i).trim()
                if (q.isNotBlank() && !queries.contains(q)) {
                    queries.add(q)
                }
            }
        }

        val sources = mutableListOf<GroundingWebSource>()
        val chunks = grounding.optJSONArray("groundingChunks")
        if (chunks != null) {
            for (i in 0 until chunks.length()) {
                val chunk = chunks.optJSONObject(i) ?: continue
                val web = chunk.optJSONObject("web")
                if (web != null) {
                    val uri = web.optString("uri", "").trim()
                    val title = web.optString("title", "Web Source").trim()
                    if (uri.isNotBlank()) {
                        sources.add(
                            GroundingWebSource(
                                title = title,
                                url = uri,
                                domain = extractDomain(uri)
                            )
                        )
                    }
                }
                val maps = chunk.optJSONObject("maps")
                if (maps != null) {
                    val uri = maps.optString("uri", "").trim()
                    val title = maps.optString("title", "Google Maps Place").trim()
                    if (uri.isNotBlank()) {
                        sources.add(
                            GroundingWebSource(
                                title = "📍 $title",
                                url = uri,
                                domain = "maps.google.com"
                            )
                        )
                    }
                }
            }
        }

        val entryPointHtml = grounding
            .optJSONObject("searchEntryPoint")
            ?.optString("renderedContent", "")
            .orEmpty()

        return GroundingMetadataResult(
            sources = sources.distinctBy { it.url }.take(10),
            searchQueries = queries,
            searchEntryPointHtml = entryPointHtml
        )
    }

    /**
     * Parses a stored citation string ("Title — https://...") back into a structured GroundingWebSource
     * so the UI can render rich clickable cards and domain badges.
     */
    fun parseCitationString(rawCitation: String): GroundingWebSource {
        val trimmed = rawCitation.trim()
        val separatorIdx = trimmed.lastIndexOf(" — ")
        return if (separatorIdx > 0) {
            val titlePart = trimmed.substring(0, separatorIdx).trim()
            val urlPart = trimmed.substring(separatorIdx + 3).trim()
            GroundingWebSource(
                title = titlePart.ifBlank { extractDomain(urlPart) },
                url = urlPart,
                domain = extractDomain(urlPart)
            )
        } else if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            GroundingWebSource(
                title = extractDomain(trimmed),
                url = trimmed,
                domain = extractDomain(trimmed)
            )
        } else {
            val encoded = runCatching { URLEncoder.encode(trimmed, "UTF-8") }.getOrDefault("")
            val searchUrl = "https://www.google.com/search?q=$encoded"
            GroundingWebSource(
                title = trimmed,
                url = searchUrl,
                domain = "google.com"
            )
        }
    }

    fun extractDomain(url: String): String {
        return runCatching {
            val host = URI(url).host.orEmpty().removePrefix("www.")
            host.ifBlank { "google.com" }
        }.getOrDefault("google.com")
    }

    /**
     * Fetches real-time web news and encyclopedia context when the local fallback engine is used
     * so current events queries still return real-time headlines and clickable web links.
     */
    fun fetchLiveWebSearchFallback(
        okHttpClient: OkHttpClient,
        rawPrompt: String
    ): Pair<String, GroundingMetadataResult>? {
        return runCatching {
            val cleanedQuery = rawPrompt
                .replace(Regex("(?i)^(what is|what are|tell me about|search for|latest news on|whats happening with)\\s+"), "")
                .trim()
                .take(120)
                .ifBlank { rawPrompt.trim().take(120) }

            val encodedQuery = URLEncoder.encode(cleanedQuery, "UTF-8")
            val rssUrl = "https://news.google.com/rss/search?q=$encodedQuery&hl=en-US&gl=US&ceid=US:en"

            val request = Request.Builder()
                .url(rssUrl)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
                .get()
                .build()

            val headlines = mutableListOf<GroundingWebSource>()
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val xmlBody = response.body?.string().orEmpty()
                    if (xmlBody.isNotBlank()) {
                        headlines.addAll(parseRssItems(xmlBody, maxItems = 6))
                    }
                }
            }

            if (headlines.isEmpty()) return null

            val nowStr = SimpleDateFormat("MMM d, yyyy • HH:mm z", Locale.US).format(Date())
            val markdown = buildString {
                append("### 🌐 Live Google Search Grounded Update\n")
                append("**Search Query:** `$cleanedQuery` • **Retrieved:** $nowStr\n\n")
                append("Here is the latest real-time information and current coverage found across the web:\n\n")
                headlines.forEachIndexed { index, item ->
                    val pubBadge = if (item.publishedAt.isNotBlank()) " _(${item.publishedAt})_" else ""
                    val sourceBadge = if (item.domain.isNotBlank()) "**[${item.domain}]**" else ""
                    append("${index + 1}. $sourceBadge ${item.title}$pubBadge\n")
                    if (item.snippet.isNotBlank()) {
                        append("   - ${item.snippet}\n")
                    }
                }
                append("\n#### Key Takeaways\n")
                append("- **Real-Time Grounding:** The above items reflect live web coverage indexed via Google Search.\n")
                append("- **Verified Citations:** Tap any source link below to open the full article directly in your browser.\n")
            }

            val metadata = GroundingMetadataResult(
                sources = headlines,
                searchQueries = listOf(cleanedQuery)
            )
            Pair(markdown, metadata)
        }.getOrNull()
    }

    private fun parseRssItems(xml: String, maxItems: Int): List<GroundingWebSource> {
        val items = mutableListOf<GroundingWebSource>()
        runCatching {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            var insideItem = false
            var currentTitle = ""
            var currentLink = ""
            var currentPubDate = ""
            var currentSource = ""
            var currentSourceUrl = ""

            while (eventType != XmlPullParser.END_DOCUMENT && items.size < maxItems) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        val tagName = parser.name.lowercase(Locale.ROOT)
                        if (tagName == "item") {
                            insideItem = true
                            currentTitle = ""
                            currentLink = ""
                            currentPubDate = ""
                            currentSource = ""
                            currentSourceUrl = ""
                        } else if (insideItem) {
                            when (tagName) {
                                "title" -> currentTitle = parser.nextText().trim()
                                "link" -> currentLink = parser.nextText().trim()
                                "pubdate" -> currentPubDate = parser.nextText().trim().take(22)
                                "source" -> {
                                    currentSourceUrl = parser.getAttributeValue(null, "url").orEmpty()
                                    currentSource = parser.nextText().trim()
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name.equals("item", ignoreCase = true) && insideItem) {
                            insideItem = false
                            if (currentTitle.isNotBlank() && currentLink.isNotBlank()) {
                                val domain = if (currentSource.isNotBlank()) {
                                    currentSource
                                } else if (currentSourceUrl.isNotBlank()) {
                                    extractDomain(currentSourceUrl)
                                } else {
                                    extractDomain(currentLink)
                                }
                                items.add(
                                    GroundingWebSource(
                                        title = currentTitle,
                                        url = currentLink,
                                        domain = domain,
                                        publishedAt = currentPubDate
                                    )
                                )
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        }
        return items
    }
}
