package com.example.omiri.ui.navigation

object Routes {
    const val Home = "home"
    const val AllDeals = "all_deals"
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

    const val WebViewArgUrl = "url"
    const val WebViewArgTitle = "title"
    const val WebView = "webview?url={$WebViewArgUrl}&title={$WebViewArgTitle}"
    
    fun webView(url: String, title: String = "Web View"): String {
        val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
        val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
        return "webview?url=$encodedUrl&title=$encodedTitle"
    }

    // Legacy route alias for backward compatibility
    @Deprecated("Use Settings instead", ReplaceWith("Settings"))
    const val Profile = "settings"
}
