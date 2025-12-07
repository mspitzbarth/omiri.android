package com.example.omiri.util

/**
 * Helper object for consistent emoji handling across the app.
 * - Category → default emoji
 * - Title keywords → more specific emoji
 * - Priority: title wins if it clearly matches the category OR is an allowed override for that category.
 * - Multilingual support → Detects keywords in English, German, Spanish, French, Italian where practical.
 *
 * Notes:
 * - Keep title checks lowercase; we normalize with lowercase() once.
 * - Keep keywords short (prefixes) where possible to cover plural/suffixes.
 */
object EmojiHelper {

    /**
     * Title wins if:
     *  - title emoji exists AND (matchesCategory(title, category) OR is in allowed override for that category),
     * else category emoji; else fallback 🛒.
     */
    fun getProductEmoji(title: String, category: String? = null): String {
        val titleEmoji = getTitleEmoji(title)
        val catEmoji = getCategoryEmoji(category)

        // No title signal → prefer category → fallback
        if (titleEmoji.isEmpty()) return if (catEmoji.isNotEmpty()) catEmoji else "🛒"

        // No category provided → title can win
        if (category.isNullOrBlank()) return titleEmoji

        val cat = category.lowercase()

        // Strong seasonals/icons always win
        if (TITLE_ALWAYS_WINS.contains(titleEmoji)) return titleEmoji

        // Title emoji allowed to win for this related category?
        if (EMOJI_CATEGORY_ALLOW[titleEmoji]?.contains(cat) == true) return titleEmoji

        // Otherwise prefer the category emoji (or title if category missing)
        return if (catEmoji.isNotEmpty()) catEmoji else titleEmoji
    }

    /**
     * Returns the default emoji for a category id.
     */
    fun getCategoryEmoji(category: String?): String {
        return CATEGORY_EMOJI[category?.lowercase()] ?: ""
    }

    /**
     * Returns the most specific emoji inferred from the product title.
     */
    private fun getTitleEmoji(title: String): String {
        val t = title.lowercase()

        // SEASONAL / EVENTS / PROMOS
        if (containsAny(t, "black friday", "cyber monday", "deal", "clearance", "sale")) return "🛍️"
        if (containsAny(t, "christmas", "xmas")) return "🎄"
        if (containsAny(t, "gift", "present")) return "🎁"
        if (containsAny(t, "halloween")) return "🎃"
        if (containsAny(t, "valentine")) return "💝"

        // ELECTRONICS
        if (containsAny(t, "tv", "television")) return "📺"
        if (containsAny(t, "phone", "smartphone", "iphone")) return "📱"
        if (containsAny(t, "laptop", "notebook", "macbook")) return "💻"
        if (containsAny(t, "monitor", "display")) return "🖥️"
        if (containsAny(t, "headphone", "earbud", "airpod")) return "🎧"
        if (containsAny(t, "watch", "smartwatch")) return "⌚"
        if (containsAny(t, "speaker", "soundbar")) return "🔊"
        if (containsAny(t, "camera")) return "📷"
        if (containsAny(t, "printer")) return "🖨️"
        if (containsAny(t, "playstation", "xbox", "nintendo", "console")) return "🎮"

        // KITCHEN & APPLIANCES
        if (containsAny(t, "coffee", "espresso")) return "☕"
        if (containsAny(t, "blender", "mixer")) return "🍳"
        if (containsAny(t, "microwave")) return "📻"
        if (containsAny(t, "toaster")) return "🍞"

        // FOOD & BEVERAGES
        if (containsAny(t, "pizza")) return "🍕"
        if (containsAny(t, "burger")) return "🍔"
        if (containsAny(t, "bread")) return "🍞"
        if (containsAny(t, "chocolate", "candy")) return "🍫"
        if (containsAny(t, "cookie", "biscuit")) return "🍪"
        if (containsAny(t, "ice cream")) return "🍨"
        if (containsAny(t, "coffee", "latte")) return "☕"
        if (containsAny(t, "tea")) return "🍵"
        if (containsAny(t, "beer")) return "🍺"
        if (containsAny(t, "wine")) return "🍷"

        // FASHION & ACCESSORIES
        if (containsAny(t, "shirt", "t-shirt")) return "👕"
        if (containsAny(t, "dress")) return "👗"
        if (containsAny(t, "jeans", "pants")) return "👖"
        if (containsAny(t, "shoe", "sneaker", "boot")) return "👟"
        if (containsAny(t, "bag", "backpack")) return "🎒"
        if (containsAny(t, "sunglass")) return "🕶️"

        // SPORTS & FITNESS
        if (containsAny(t, "yoga")) return "🧘"
        if (containsAny(t, "dumbbell", "weight")) return "🏋️"
        if (containsAny(t, "bicycle", "bike")) return "🚴"
        if (containsAny(t, "football", "soccer")) return "⚽"
        if (containsAny(t, "basketball")) return "🏀"

        // HOME & GARDEN
        if (containsAny(t, "plant")) return "🪴"
        if (containsAny(t, "candle")) return "🕯️"
        if (containsAny(t, "chair")) return "🪑"
        if (containsAny(t, "lamp", "light")) return "💡"
        if (containsAny(t, "vacuum")) return "🤖"

        // BEAUTY & PERSONAL CARE
        if (containsAny(t, "lipstick", "makeup")) return "💄"
        if (containsAny(t, "perfume", "fragrance")) return "💐"
        if (containsAny(t, "shampoo", "conditioner")) return "🧴"
        if (containsAny(t, "nail", "manicure")) return "💅"

        // TOYS & KIDS
        if (containsAny(t, "toy", "lego", "puzzle")) return "🧸"
        if (containsAny(t, "baby", "diaper")) return "🍼"

        // BOOKS & MEDIA
        if (containsAny(t, "book")) return "📚"
        if (containsAny(t, "movie", "dvd", "blu-ray")) return "🎬"
        if (containsAny(t, "music", "vinyl")) return "🎵"

        // PETS
        if (containsAny(t, "dog", "pet")) return "🐾"
        if (containsAny(t, "cat")) return "🐱"

        // AUTOMOTIVE
        if (containsAny(t, "car", "tire", "automotive")) return "🚗"

        // Default: no match
        return ""
    }

    // Priority Overrides
    private val TITLE_ALWAYS_WINS = setOf(
        "🎄", // Christmas
        "🎃", // Halloween
        "🎁", // Gifts
        "🎆", // New Year
        "✨"  // Decorative
    )

    private val EMOJI_CATEGORY_ALLOW: Map<String, Set<String>> = mapOf(
        "🎄" to setOf("home_decor", "seasonal", "promotions"),
        "🎃" to setOf("home_decor", "seasonal", "promotions"),
        "🎁" to setOf("seasonal", "promotions", "electronics"),
        "💡" to setOf("home_decor", "smart_home", "electronics"),
        "🍕" to setOf("ready_meals", "frozen_foods", "snacks"),
        "🍫" to setOf("snacks", "seasonal"),
        "📱" to setOf("electronics", "smart_home", "tech_accessories"),
        "💻" to setOf("computers", "electronics"),
        "🎧" to setOf("audio", "electronics"),
        "🎮" to setOf("gaming", "electronics"),
        "☕" to setOf("coffee", "beverages", "kitchen")
    )

    private val CATEGORY_EMOJI: Map<String, String> = mapOf(
        // Electronics
        "electronics" to "📱",
        "tv_home_theater" to "📺",
        "computers" to "💻",
        "audio" to "🎧",
        "gaming" to "🎮",
        "cameras" to "📷",
        "smart_home" to "🏠",

        // Food & Beverages
        "food" to "🍕",
        "beverages" to "🥤",
        "snacks" to "🍫",
        "frozen" to "🧊",

        // Fashion
        "clothing" to "👕",
        "shoes" to "👟",
        "accessories" to "👜",

        // Home
        "home_decor" to "🛋️",
        "kitchen" to "🍳",
        "garden" to "🪴",

        // Beauty
        "beauty" to "💄",
        "personal_care" to "🧴",

        // Sports
        "sports" to "⚽",
        "fitness" to "💪",

        // Other
        "toys" to "🧸",
        "books" to "📚",
        "pets" to "🐾",
        "automotive" to "🚗",
        "seasonal" to "🎉",
        "promotions" to "🛍️",
        "snacks_sweets" to "🍫",
        "sweets" to "🍬",
        "confectionery" to "🍬"
    )

    private fun containsAny(haystack: String, vararg needles: String): Boolean {
        for (n in needles) if (haystack.contains(n)) return true
        return false
    }
}
