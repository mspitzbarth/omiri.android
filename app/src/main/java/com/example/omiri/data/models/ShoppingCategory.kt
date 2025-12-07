package com.example.omiri.data.models

import androidx.compose.ui.graphics.Color

data class ShoppingCategory(
    val id: String,
    val nameEn: String,
    val nameDe: String,
    val nameFr: String,
    val nameEs: String,
    val nameIt: String,
    val nameNl: String,
    val color: Color,
    val parent: String,
    val icon: String = ""
) {
    fun getName(language: String = "en"): String {
        return when (language) {
            "de" -> nameDe
            "fr" -> nameFr
            "es" -> nameEs
            "it" -> nameIt
            "nl" -> nameNl
            else -> nameEn
        }
    }
}

object PredefinedCategories {
    // Food & Beverages
    val FRUITS_VEGETABLES = ShoppingCategory(
        id = "fruits_vegetables",
        nameEn = "Fruits & Vegetables",
        nameDe = "Obst & Gemüse",
        nameFr = "Fruits & Légumes",
        nameEs = "Frutas & Verduras",
        nameIt = "Frutta & Verdura",
        nameNl = "Fruit & Groenten",
        color = Color(0xFF10B981),
        parent = "food_beverages"
    )

    val MEAT_POULTRY = ShoppingCategory(
        id = "meat_poultry",
        nameEn = "Meat & Poultry",
        nameDe = "Fleisch & Geflügel",
        nameFr = "Viande & Volaille",
        nameEs = "Carne & Aves",
        nameIt = "Carne & Pollame",
        nameNl = "Vlees & Gevogelte",
        color = Color(0xFFEF4444),
        parent = "food_beverages"
    )

    val FISH_SEAFOOD = ShoppingCategory(
        id = "fish_seafood",
        nameEn = "Fish & Seafood",
        nameDe = "Fisch & Meeresfrüchte",
        nameFr = "Poisson & Fruits de mer",
        nameEs = "Pescado & Mariscos",
        nameIt = "Pesce & Frutti di mare",
        nameNl = "Vis & Zeevruchten",
        color = Color(0xFF06B6D4),
        parent = "food_beverages"
    )

    val DAIRY_EGGS = ShoppingCategory(
        id = "dairy_eggs",
        nameEn = "Dairy & Eggs",
        nameDe = "Milchprodukte & Eier",
        nameFr = "Produits laitiers & Œufs",
        nameEs = "Lácteos & Huevos",
        nameIt = "Latticini & Uova",
        nameNl = "Zuivel & Eieren",
        color = Color(0xFF3B82F6),
        parent = "food_beverages"
    )

    val BREAD_BAKERY = ShoppingCategory(
        id = "bread_bakery",
        nameEn = "Bread & Bakery",
        nameDe = "Brot & Backwaren",
        nameFr = "Pain & Boulangerie",
        nameEs = "Pan & Panadería",
        nameIt = "Pane & Pasticceria",
        nameNl = "Brood & Bakkerij",
        color = Color(0xFFF59E0B),
        parent = "food_beverages"
    )

    val FROZEN_FOODS = ShoppingCategory(
        id = "frozen_foods",
        nameEn = "Frozen Foods",
        nameDe = "Tiefkühlkost",
        nameFr = "Produits surgelés",
        nameEs = "Congelados",
        nameIt = "Surgelati",
        nameNl = "Diepvriesproducten",
        color = Color(0xFF60A5FA),
        parent = "food_beverages"
    )

    val SNACKS_SWEETS = ShoppingCategory(
        id = "snacks_sweets",
        nameEn = "Snacks & Sweets",
        nameDe = "Snacks & Süßigkeiten",
        nameFr = "Collations & Bonbons",
        nameEs = "Aperitivos & Dulces",
        nameIt = "Snack & Dolci",
        nameNl = "Snacks & Snoep",
        color = Color(0xFFEC4899),
        parent = "food_beverages"
    )

    val BEVERAGES = ShoppingCategory(
        id = "beverages",
        nameEn = "Beverages",
        nameDe = "Getränke",
        nameFr = "Boissons",
        nameEs = "Bebidas",
        nameIt = "Bevande",
        nameNl = "Dranken",
        color = Color(0xFF8B5CF6),
        parent = "food_beverages"
    )

    val PANTRY_STAPLES = ShoppingCategory(
        id = "pantry_staples",
        nameEn = "Pantry Staples",
        nameDe = "Vorratskammer",
        nameFr = "Produits de base",
        nameEs = "Alimentos básicos",
        nameIt = "Dispensa",
        nameNl = "Basisproducten",
        color = Color(0xFF6B7280),
        parent = "food_beverages"
    )

    val ORGANIC_HEALTH = ShoppingCategory(
        id = "organic_health",
        nameEn = "Organic & Health",
        nameDe = "Bio & Gesundheit",
        nameFr = "Bio & Santé",
        nameEs = "Orgánico & Salud",
        nameIt = "Biologico & Salute",
        nameNl = "Biologisch & Gezondheid",
        color = Color(0xFF059669),
        parent = "food_beverages"
    )

    // Household & Cleaning
    val CLEANING_SUPPLIES = ShoppingCategory(
        id = "cleaning_supplies",
        nameEn = "Cleaning Supplies",
        nameDe = "Reinigungsmittel",
        nameFr = "Produits de nettoyage",
        nameEs = "Productos de limpieza",
        nameIt = "Prodotti per la pulizia",
        nameNl = "Schoonmaakmiddelen",
        color = Color(0xFF14B8A6),
        parent = "household_cleaning"
    )

    val LAUNDRY_CARE = ShoppingCategory(
        id = "laundry_care",
        nameEn = "Laundry Care",
        nameDe = "Wäschepflege",
        nameFr = "Lessive",
        nameEs = "Cuidado de ropa",
        nameIt = "Cura del bucato",
        nameNl = "Wasverzorging",
        color = Color(0xFF0D9488),
        parent = "household_cleaning"
    )

    val PAPER_PRODUCTS = ShoppingCategory(
        id = "paper_products",
        nameEn = "Paper Products",
        nameDe = "Papierprodukte",
        nameFr = "Produits en papier",
        nameEs = "Productos de papel",
        nameIt = "Prodotti di carta",
        nameNl = "Papierproducten",
        color = Color(0xFF78716C),
        parent = "household_cleaning"
    )

    // Personal Care
    val BEAUTY_COSMETICS = ShoppingCategory(
        id = "beauty_cosmetics",
        nameEn = "Beauty & Cosmetics",
        nameDe = "Schönheit & Kosmetik",
        nameFr = "Beauté & Cosmétiques",
        nameEs = "Belleza & Cosméticos",
        nameIt = "Bellezza & Cosmetici",
        nameNl = "Schoonheid & Cosmetica",
        color = Color(0xFFF472B6),
        parent = "personal_care"
    )

    val HAIR_CARE = ShoppingCategory(
        id = "hair_care",
        nameEn = "Hair Care",
        nameDe = "Haarpflege",
        nameFr = "Soins capillaires",
        nameEs = "Cuidado del cabello",
        nameIt = "Cura dei capelli",
        nameNl = "Haarverzorging",
        color = Color(0xFFE879F9),
        parent = "personal_care"
    )

    val BODY_CARE = ShoppingCategory(
        id = "body_care",
        nameEn = "Body Care",
        nameDe = "Körperpflege",
        nameFr = "Soins du corps",
        nameEs = "Cuidado corporal",
        nameIt = "Cura del corpo",
        nameNl = "Lichaamsverzorging",
        color = Color(0xFFC084FC),
        parent = "personal_care"
    )

    val ORAL_CARE = ShoppingCategory(
        id = "oral_care",
        nameEn = "Oral Care",
        nameDe = "Zahnpflege",
        nameFr = "Soins bucco-dentaires",
        nameEs = "Cuidado oral",
        nameIt = "Igiene orale",
        nameNl = "Mondverzorging",
        color = Color(0xFFA855F7),
        parent = "personal_care"
    )

    val HYGIENE_PRODUCTS = ShoppingCategory(
        id = "hygiene_products",
        nameEn = "Hygiene Products",
        nameDe = "Hygieneprodukte",
        nameFr = "Produits d'hygiène",
        nameEs = "Productos de higiene",
        nameIt = "Prodotti per l'igiene",
        nameNl = "Hygiëneproducten",
        color = Color(0xFF9333EA),
        parent = "personal_care"
    )

    // Baby & Kids
    val BABY_CARE = ShoppingCategory(
        id = "baby_care",
        nameEn = "Baby Care",
        nameDe = "Babypflege",
        nameFr = "Soins bébé",
        nameEs = "Cuidado del bebé",
        nameIt = "Cura del bambino",
        nameNl = "Babyverzorging",
        color = Color(0xFFFBBF24),
        parent = "baby_kids"
    )

    val BABY_FOOD = ShoppingCategory(
        id = "baby_food",
        nameEn = "Baby Food",
        nameDe = "Babynahrung",
        nameFr = "Alimentation bébé",
        nameEs = "Comida para bebés",
        nameIt = "Alimenti per bambini",
        nameNl = "Babyvoeding",
        color = Color(0xFFFCD34D),
        parent = "baby_kids"
    )

    val DIAPERS_WIPES = ShoppingCategory(
        id = "diapers_wipes",
        nameEn = "Diapers & Wipes",
        nameDe = "Windeln & Feuchttücher",
        nameFr = "Couches & Lingettes",
        nameEs = "Pañales & Toallitas",
        nameIt = "Pannolini & Salviette",
        nameNl = "Luiers & Doekjes",
        color = Color(0xFFFDE68A),
        parent = "baby_kids"
    )

    val TOYS_GAMES = ShoppingCategory(
        id = "toys_games",
        nameEn = "Toys & Games",
        nameDe = "Spielzeug & Spiele",
        nameFr = "Jouets & Jeux",
        nameEs = "Juguetes & Juegos",
        nameIt = "Giocattoli & Giochi",
        nameNl = "Speelgoed & Spellen",
        color = Color(0xFFFCA5A5),
        parent = "baby_kids"
    )

    // Home & Garden
    val HOME_DECOR = ShoppingCategory(
        id = "home_decor",
        nameEn = "Home Decor",
        nameDe = "Heimdekoration",
        nameFr = "Décoration d'intérieur",
        nameEs = "Decoración del hogar",
        nameIt = "Arredamento casa",
        nameNl = "Woondecoratie",
        color = Color(0xFF7C3AED),
        parent = "home_garden"
    )

    val KITCHEN_DINING = ShoppingCategory(
        id = "kitchen_dining",
        nameEn = "Kitchen & Dining",
        nameDe = "Küche & Esszimmer",
        nameFr = "Cuisine & Salle à manger",
        nameEs = "Cocina & Comedor",
        nameIt = "Cucina & Sala da pranzo",
        nameNl = "Keuken & Eetkamer",
        color = Color(0xFFEF4444),
        parent = "home_garden"
    )

    val BEDDING_BATH = ShoppingCategory(
        id = "bedding_bath",
        nameEn = "Bedding & Bath",
        nameDe = "Bettwäsche & Bad",
        nameFr = "Literie & Salle de bain",
        nameEs = "Ropa de cama & Baño",
        nameIt = "Biancheria da letto & Bagno",
        nameNl = "Beddengoed & Bad",
        color = Color(0xFF8B5CF6),
        parent = "home_garden"
    )

    val GARDEN_OUTDOOR = ShoppingCategory(
        id = "garden_outdoor",
        nameEn = "Garden & Outdoor",
        nameDe = "Garten & Outdoor",
        nameFr = "Jardin & Extérieur",
        nameEs = "Jardín & Exterior",
        nameIt = "Giardino & Esterno",
        nameNl = "Tuin & Buitenleven",
        color = Color(0xFF22C55E),
        parent = "home_garden"
    )

    val TOOLS_HARDWARE = ShoppingCategory(
        id = "tools_hardware",
        nameEn = "Tools & Hardware",
        nameDe = "Werkzeuge & Eisenwaren",
        nameFr = "Outils & Quincaillerie",
        nameEs = "Herramientas & Ferretería",
        nameIt = "Utensili & Ferramenta",
        nameNl = "Gereedschap & IJzerwaren",
        color = Color(0xFF64748B),
        parent = "home_garden"
    )

    // Electronics & Media
    val ELECTRONICS = ShoppingCategory(
        id = "electronics",
        nameEn = "Electronics",
        nameDe = "Elektronik",
        nameFr = "Électronique",
        nameEs = "Electrónica",
        nameIt = "Elettronica",
        nameNl = "Elektronica",
        color = Color(0xFF3B82F6),
        parent = "electronics_media"
    )

    val APPLIANCES = ShoppingCategory(
        id = "appliances",
        nameEn = "Appliances",
        nameDe = "Haushaltsgeräte",
        nameFr = "Électroménager",
        nameEs = "Electrodomésticos",
        nameIt = "Elettrodomestici",
        nameNl = "Huishoudelijke apparaten",
        color = Color(0xFF2563EB),
        parent = "electronics_media"
    )

    val COMPUTERS_ACCESSORIES = ShoppingCategory(
        id = "computers_accessories",
        nameEn = "Computers & Accessories",
        nameDe = "Computer & Zubehör",
        nameFr = "Ordinateurs & Accessoires",
        nameEs = "Computadoras & Accesorios",
        nameIt = "Computer & Accessori",
        nameNl = "Computers & Accessoires",
        color = Color(0xFF1D4ED8),
        parent = "electronics_media"
    )

    val MEDIA_ENTERTAINMENT = ShoppingCategory(
        id = "media_entertainment",
        nameEn = "Media & Entertainment",
        nameDe = "Medien & Unterhaltung",
        nameFr = "Médias & Divertissement",
        nameEs = "Medios & Entretenimiento",
        nameIt = "Media & Intrattenimento",
        nameNl = "Media & Entertainment",
        color = Color(0xFF6366F1),
        parent = "electronics_media"
    )

    // Clothing & Accessories
    val CLOTHING_MENS = ShoppingCategory(
        id = "clothing_mens",
        nameEn = "Clothing - Men's",
        nameDe = "Kleidung - Herren",
        nameFr = "Vêtements - Hommes",
        nameEs = "Ropa - Hombres",
        nameIt = "Abbigliamento - Uomo",
        nameNl = "Kleding - Heren",
        color = Color(0xFF0EA5E9),
        parent = "clothing_accessories"
    )

    val CLOTHING_WOMENS = ShoppingCategory(
        id = "clothing_womens",
        nameEn = "Clothing - Women's",
        nameDe = "Kleidung - Damen",
        nameFr = "Vêtements - Femmes",
        nameEs = "Ropa - Mujeres",
        nameIt = "Abbigliamento - Donna",
        nameNl = "Kleding - Dames",
        color = Color(0xFFDB2777),
        parent = "clothing_accessories"
    )

    val CLOTHING_KIDS = ShoppingCategory(
        id = "clothing_kids",
        nameEn = "Clothing - Kids",
        nameDe = "Kleidung - Kinder",
        nameFr = "Vêtements - Enfants",
        nameEs = "Ropa - Niños",
        nameIt = "Abbigliamento - Bambini",
        nameNl = "Kleding - Kinderen",
        color = Color(0xFFF59E0B),
        parent = "clothing_accessories"
    )

    val SHOES_FOOTWEAR = ShoppingCategory(
        id = "shoes_footwear",
        nameEn = "Shoes & Footwear",
        nameDe = "Schuhe",
        nameFr = "Chaussures",
        nameEs = "Calzado",
        nameIt = "Scarpe",
        nameNl = "Schoenen",
        color = Color(0xFF84CC16),
        parent = "clothing_accessories"
    )

    val ACCESSORIES = ShoppingCategory(
        id = "accessories",
        nameEn = "Accessories",
        nameDe = "Accessoires",
        nameFr = "Accessoires",
        nameEs = "Accesorios",
        nameIt = "Accessori",
        nameNl = "Accessoires",
        color = Color(0xFF06B6D4),
        parent = "clothing_accessories"
    )

    // Sports & Leisure
    val SPORTS_FITNESS = ShoppingCategory(
        id = "sports_fitness",
        nameEn = "Sports & Fitness",
        nameDe = "Sport & Fitness",
        nameFr = "Sport & Fitness",
        nameEs = "Deportes & Fitness",
        nameIt = "Sport & Fitness",
        nameNl = "Sport & Fitness",
        color = Color(0xFFEF4444),
        parent = "sports_leisure"
    )

    val OUTDOOR_CAMPING = ShoppingCategory(
        id = "outdoor_camping",
        nameEn = "Outdoor & Camping",
        nameDe = "Outdoor & Camping",
        nameFr = "Plein air & Camping",
        nameEs = "Aire libre & Camping",
        nameIt = "Outdoor & Camping",
        nameNl = "Outdoor & Camping",
        color = Color(0xFF16A34A),
        parent = "sports_leisure"
    )

    val HOBBY_CRAFT = ShoppingCategory(
        id = "hobby_craft",
        nameEn = "Hobby & Craft",
        nameDe = "Hobby & Basteln",
        nameFr = "Loisirs & Artisanat",
        nameEs = "Hobby & Manualidades",
        nameIt = "Hobby & Artigianato",
        nameNl = "Hobby & Knutselen",
        color = Color(0xFFD946EF),
        parent = "sports_leisure"
    )

    // Pets
    val PET_FOOD = ShoppingCategory(
        id = "pet_food",
        nameEn = "Pet Food",
        nameDe = "Tiernahrung",
        nameFr = "Nourriture pour animaux",
        nameEs = "Comida para mascotas",
        nameIt = "Cibo per animali",
        nameNl = "Dierenvoeding",
        color = Color(0xFF92400E),
        parent = "pets"
    )

    val PET_SUPPLIES = ShoppingCategory(
        id = "pet_supplies",
        nameEn = "Pet Supplies",
        nameDe = "Tierbedarf",
        nameFr = "Fournitures pour animaux",
        nameEs = "Suministros para mascotas",
        nameIt = "Forniture per animali",
        nameNl = "Dierenbenodigdheden",
        color = Color(0xFFA16207),
        parent = "pets"
    )

    // Seasonal & Special
    val SEASONAL = ShoppingCategory(
        id = "seasonal",
        nameEn = "Seasonal",
        nameDe = "Saisonal",
        nameFr = "Saisonnier",
        nameEs = "Estacional",
        nameIt = "Stagionale",
        nameNl = "Seizoensgebonden",
        color = Color(0xFFEAB308),
        parent = "seasonal_special"
    )

    val HOLIDAY_ITEMS = ShoppingCategory(
        id = "holiday_items",
        nameEn = "Holiday Items",
        nameDe = "Feiertagsartikel",
        nameFr = "Articles de vacances",
        nameEs = "Artículos de vacaciones",
        nameIt = "Articoli per le vacanze",
        nameNl = "Vakantieartikelen",
        color = Color(0xFFF59E0B),
        parent = "seasonal_special"
    )

    // Office & School
    val OFFICE_SUPPLIES = ShoppingCategory(
        id = "office_supplies",
        nameEn = "Office Supplies",
        nameDe = "Bürobedarf",
        nameFr = "Fournitures de bureau",
        nameEs = "Suministros de oficina",
        nameIt = "Forniture per ufficio",
        nameNl = "Kantoorbenodigdheden",
        color = Color(0xFF475569),
        parent = "office_school"
    )

    val SCHOOL_SUPPLIES = ShoppingCategory(
        id = "school_supplies",
        nameEn = "School Supplies",
        nameDe = "Schulbedarf",
        nameFr = "Fournitures scolaires",
        nameEs = "Útiles escolares",
        nameIt = "Forniture scolastiche",
        nameNl = "Schoolbenodigdheden",
        color = Color(0xFF334155),
        parent = "office_school"
    )

    // Automotive
    val AUTOMOTIVE = ShoppingCategory(
        id = "automotive",
        nameEn = "Automotive",
        nameDe = "Automobil",
        nameFr = "Automobile",
        nameEs = "Automotriz",
        nameIt = "Automotive",
        nameNl = "Auto",
        color = Color(0xFF1E293B),
        parent = "automotive"
    )

    val CAR_CARE = ShoppingCategory(
        id = "car_care",
        nameEn = "Car Care",
        nameDe = "Autopflege",
        nameFr = "Entretien automobile",
        nameEs = "Cuidado del automóvil",
        nameIt = "Cura dell'auto",
        nameNl = "Autoonderhoud",
        color = Color(0xFF0F172A),
        parent = "automotive"
    )

    // Other
    val OTHER = ShoppingCategory(
        id = "other",
        nameEn = "Other",
        nameDe = "Sonstiges",
        nameFr = "Autre",
        nameEs = "Otro",
        nameIt = "Altro",
        nameNl = "Anders",
        color = Color(0xFF64748B),
        parent = "other"
    )

    val ALL_CATEGORIES = listOf(
        FRUITS_VEGETABLES, MEAT_POULTRY, FISH_SEAFOOD, DAIRY_EGGS, BREAD_BAKERY,
        FROZEN_FOODS, SNACKS_SWEETS, BEVERAGES, PANTRY_STAPLES, ORGANIC_HEALTH,
        CLEANING_SUPPLIES, LAUNDRY_CARE, PAPER_PRODUCTS,
        BEAUTY_COSMETICS, HAIR_CARE, BODY_CARE, ORAL_CARE, HYGIENE_PRODUCTS,
        BABY_CARE, BABY_FOOD, DIAPERS_WIPES, TOYS_GAMES,
        HOME_DECOR, KITCHEN_DINING, BEDDING_BATH, GARDEN_OUTDOOR, TOOLS_HARDWARE,
        ELECTRONICS, APPLIANCES, COMPUTERS_ACCESSORIES, MEDIA_ENTERTAINMENT,
        CLOTHING_MENS, CLOTHING_WOMENS, CLOTHING_KIDS, SHOES_FOOTWEAR, ACCESSORIES,
        SPORTS_FITNESS, OUTDOOR_CAMPING, HOBBY_CRAFT,
        PET_FOOD, PET_SUPPLIES,
        SEASONAL, HOLIDAY_ITEMS,
        OFFICE_SUPPLIES, SCHOOL_SUPPLIES,
        AUTOMOTIVE, CAR_CARE,
        OTHER
    )

    fun getCategoryById(id: String): ShoppingCategory {
        return ALL_CATEGORIES.find { it.id == id } ?: OTHER
    }

    fun getCategoriesByParent(parent: String, language: String = "en"): List<ShoppingCategory> {
        return ALL_CATEGORIES.filter { it.parent == parent }
    }

    fun getAllParents(): List<String> {
        return ALL_CATEGORIES.map { it.parent }.distinct().sorted()
    }
}
