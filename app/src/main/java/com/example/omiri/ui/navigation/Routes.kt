package com.example.omiri.ui.navigation

object Routes {
    const val Home = "home"
    const val AllDealsBase = "all_deals"
    const val AllDealsArgQuery = "query"
    const val AllDealsArgFilter = "filter"
    const val AllDeals = "all_deals?$AllDealsArgQuery={$AllDealsArgQuery}&$AllDealsArgFilter={$AllDealsArgFilter}"

    fun allDeals(query: String? = null, filter: String? = null): String {
        val q = query?.let { java.net.URLEncoder.encode(it, "UTF-8") }
        val f = filter?.let { java.net.URLEncoder.encode(it, "UTF-8") }
        
        return buildString {
            append("all_deals")
            if (q != null || f != null) {
                append("?")
                if (q != null) append("$AllDealsArgQuery=$q")
                if (f != null) {
                    if (q != null) append("&")
                    append("$AllDealsArgFilter=$f")
                }
            }
        }
    }
    const val ShoppingList = "shopping_list"
    const val Settings = "settings"
    const val MyStores = "my_stores"
    const val ProductDetailsArg = "dealId"
    const val ProductDetails = "product_details/{$ProductDetailsArg}"
    
    fun productDetails(dealId: String): String {
        return "product_details/$dealId"
    }
    const val Notifications = "notifications"
    const val AiChat = "ai_chat"
    const val ShoppingListMatches = "shopping_list_matches"
    const val MembershipCards = "membership_cards"
    const val Recipes = "recipes"
    const val Interests = "interests"

    const val WebViewArgUrl = "url"
    const val WebViewArgTitle = "title"
    const val WebView = "webview?url={$WebViewArgUrl}&title={$WebViewArgTitle}"
    
    fun webView(url: String, title: String = "Web View"): String {
        val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
        val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
        return "webview?url=$encodedUrl&title=$encodedTitle"
    }

    const val FlyerArgUrl = "url"
    const val FlyerArgStore = "store"
    const val FlyerArgPage = "page"
    const val FlyerViewer = "flyer_viewer?url={$FlyerArgUrl}&store={$FlyerArgStore}&page={$FlyerArgPage}"

    fun flyerViewer(url: String, store: String, page: Int? = null): String {
        val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
        val encodedStore = java.net.URLEncoder.encode(store, "UTF-8")
        val base = "flyer_viewer?url=$encodedUrl&store=$encodedStore"
        return if (page != null) "$base&page=$page" else base
    }

    // Legacy route alias for backward compatibility
    @Deprecated("Use Settings instead", ReplaceWith("Settings"))
    const val Profile = "settings"
}
