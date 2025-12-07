package com.example.omiri.data.dummy

import com.example.omiri.data.models.Deal
import com.example.omiri.ui.theme.AppColors

object DummyDeals {

    val featured = listOf(
        Deal(
            id = "1",
            title = "Premium Wireless Headphones",
            store = "TechMart",
            price = "$79",
            originalPrice = "$159",
            discountLabel = "50% OFF",
            timeLeftLabel = "2 days left",
            category = "Electronics",
            heroColor = AppColors.HeroBlue
        ),
        Deal(
            id = "2",
            title = "Designer Sunglasses",
            store = "SunStyle",
            price = "$45",
            originalPrice = "$89",
            discountLabel = "49% OFF",
            timeLeftLabel = "Ends today",
            category = "Accessories",
            heroColor = AppColors.HeroSand
        ),
        Deal(
            id = "3",
            title = "Smart Fitness Watch Pro",
            store = "FitGear",
            price = "$129",
            originalPrice = "$249",
            discountLabel = "48% OFF",
            timeLeftLabel = "12 hours left",
            category = "Electronics",
            heroColor = AppColors.HeroSlate
        ),
        Deal(
            id = "4",
            title = "Portable Bluetooth Speaker",
            store = "AudioHub",
            price = "$39",
            originalPrice = "$79",
            discountLabel = "51% OFF",
            timeLeftLabel = "3 days left",
            category = "Electronics",
            heroColor = AppColors.HeroViolet
        ),
        Deal(
            id = "5",
            title = "Adventure Backpack 30L",
            store = "TravelGo",
            price = "$59",
            originalPrice = "$119",
            discountLabel = "50% OFF",
            timeLeftLabel = "5 days left",
            category = "Outdoors",
            heroColor = AppColors.HeroForest
        ),
        Deal(
            id = "6",
            title = "Waterproof Phone Case",
            store = "PhoneShield",
            price = "$19",
            originalPrice = "$39",
            discountLabel = "51% OFF",
            timeLeftLabel = "Ends today",
            category = "Accessories",
            heroColor = AppColors.HeroAqua
        )
    )

    val cleaningSupplies = listOf(
        Deal(
            id = "c1",
            title = "Tide Laundry Detergent 64 oz",
            store = "ALDI USA",
            price = "$2.99",
            originalPrice = "$4.99",
            discountLabel = "40% OFF",
            timeLeftLabel = "5 days left",
            category = "Cleaning",
            heroColor = AppColors.HeroCream
        ),
        Deal(
            id = "c2",
            title = "Lysol Disinfectant Spray 19 oz",
            store = "Walmart",
            price = "$3.49",
            originalPrice = "$5.99",
            discountLabel = "42% OFF",
            timeLeftLabel = "Ends today",
            category = "Cleaning",
            heroColor = AppColors.HeroMint
        ),
        Deal(
            id = "c3",
            title = "Clorox Bleach Regular 121 oz",
            store = "Target",
            price = "$4.29",
            originalPrice = "$6.49",
            discountLabel = "34% OFF",
            timeLeftLabel = "3 days left",
            category = "Cleaning",
            heroColor = AppColors.HeroIce
        ),
        Deal(
            id = "c4",
            title = "Dawn Ultra Dish Soap 19.4 oz",
            store = "ALDI USA",
            price = "$1.99",
            originalPrice = "$3.49",
            discountLabel = "43% OFF",
            timeLeftLabel = "7 days left",
            category = "Cleaning",
            heroColor = AppColors.HeroOrange
        ),
        Deal(
            id = "c5",
            title = "Swiffer WetJet Starter Kit",
            store = "Kroger",
            price = "$19.99",
            originalPrice = "$29.99",
            discountLabel = "33% OFF",
            timeLeftLabel = "12 hours left",
            category = "Cleaning",
            heroColor = AppColors.HeroBlueSoft
        ),
        Deal(
            id = "c6",
            title = "Mr. Clean Magic Eraser 4 Pack",
            store = "ALDI USA",
            price = "$3.99",
            originalPrice = "$5.49",
            discountLabel = "27% OFF",
            timeLeftLabel = "4 days left",
            category = "Cleaning",
            heroColor = AppColors.HeroLemon
        )
    )

    /**
     * Deals that are about to expire.
     * Use this for your "Leaving soon" section.
     */
    val leavingSoon = listOf(
        Deal(
            id = "ls1",
            title = "Smart Fitness Watch Pro",
            store = "FitGear",
            price = "$129",
            originalPrice = "$249",
            discountLabel = "48% OFF",
            timeLeftLabel = "12 hours left",
            category = "Electronics",
            heroColor = AppColors.HeroSlate
        ),
        Deal(
            id = "ls2",
            title = "Designer Sunglasses",
            store = "SunStyle",
            price = "$45",
            originalPrice = "$89",
            discountLabel = "49% OFF",
            timeLeftLabel = "Ends today",
            category = "Accessories",
            heroColor = AppColors.HeroSand
        ),
        Deal(
            id = "ls3",
            title = "Lysol Disinfectant Spray 19 oz",
            store = "Walmart",
            price = "$3.49",
            originalPrice = "$5.99",
            discountLabel = "42% OFF",
            timeLeftLabel = "Ends today",
            category = "Cleaning",
            heroColor = AppColors.HeroMint
        ),
        Deal(
            id = "ls4",
            title = "Swiffer WetJet Starter Kit",
            store = "Kroger",
            price = "$19.99",
            originalPrice = "$29.99",
            discountLabel = "33% OFF",
            timeLeftLabel = "12 hours left",
            category = "Cleaning",
            heroColor = AppColors.HeroBlueSoft
        )
    )

    /**
     * Sample deals you might have added to a shopping list.
     * This keeps it compatible with your existing DealsCarousel (List<Deal>).
     */
    val shoppingList = listOf(
        Deal(
            id = "sl1",
            title = "Tide Laundry Detergent 64 oz",
            store = "ALDI USA",
            price = "$2.99",
            originalPrice = "$4.99",
            discountLabel = "List Item",
            timeLeftLabel = "5 days left",
            category = "Cleaning",
            heroColor = AppColors.HeroCream
        ),
        Deal(
            id = "sl2",
            title = "Dawn Ultra Dish Soap 19.4 oz",
            store = "ALDI USA",
            price = "$1.99",
            originalPrice = "$3.49",
            discountLabel = "List Item",
            timeLeftLabel = "7 days left",
            category = "Cleaning",
            heroColor = AppColors.HeroOrange
        ),
        Deal(
            id = "sl3",
            title = "Waterproof Phone Case",
            store = "PhoneShield",
            price = "$19",
            originalPrice = "$39",
            discountLabel = "List Item",
            timeLeftLabel = "Ends today",
            category = "Accessories",
            heroColor = AppColors.HeroAqua
        ),
        Deal(
            id = "sl4",
            title = "Portable Bluetooth Speaker",
            store = "AudioHub",
            price = "$39",
            originalPrice = "$79",
            discountLabel = "List Item",
            timeLeftLabel = "3 days left",
            category = "Electronics",
            heroColor = AppColors.HeroViolet
        )
    )
}
