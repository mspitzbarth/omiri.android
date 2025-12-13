package com.example.omiri.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Centralized color system for Omiri dummy starter.
 * Keep these tokens reusable across screens & components.
 */
object AppColors {

    // Brand
    val BrandOrange = Color(0xFFFE8357)
    val BrandOrangeSoft = Color(0xFFFFE5DB)
    val BrandInk = Color(0xFF0F172A)

    // Neutrals
    val Bg = Color(0xFFF9FAFB)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceAlt = Color(0xFFF3F5FA)
    val Border = Color(0xFFE6E8F0)
    val MutedText = Color(0xFF6B7280)
    val SubtleText = Color(0xFF94A3B8)

    var SubTextGrey = Color(0xFF6C7280)

    // Status / tags
    val Success = Color(0xFF1E9E5A)
    val SuccessSoft = Color(0xFFE8F7EF)
    val Danger = Color(0xFFE24A3B)
    val DangerSoft = Color(0xFFFFE7E4)
    val Info = Color(0xFF2563EB)
    val InfoSoft = Color(0xFFEAF1FF)

    // Placeholder "image" gradients / blocks
    val HeroBlue = Color(0xFF0EA5E9)
    val HeroBlueSoft = Color(0xFFBFE8FF)
    val HeroSand = Color(0xFFF4B07D)
    val HeroSlate = Color(0xFF64748B)
    val HeroViolet = Color(0xFF8B5CF6)
    val HeroForest = Color(0xFF16A34A)
    val HeroAqua = Color(0xFF06B6D4)
    val HeroCream = Color(0xFFF6D6B8)
    val HeroMint = Color(0xFFBCEAD5)
    val HeroIce = Color(0xFFCFE9FF)
    val HeroOrange = Color(0xFFFFA24D)
    val HeroLemon = Color(0xFFFDE68A)

    // Pastel Palette (Soft, using BrandOrangeSoft as reference)
    // Reference Orange: 0xFFFE8357 -> Soft: 0xFFFFE5DB (BrandOrangeSoft)
    // We will define specific "Pastel" tokens that are slightly more saturated than "Soft" but less than "Brand".
    // Or just use the requested "pastel" look which often implies "Soft" colors used as backgrounds.
    
    val PastelOrange = Color(0xFFFFDBC9) // Slightly stronger than BrandOrangeSoft
    val PastelBlue = Color(0xFFDBEAFE)   
    val PastelGreen = Color(0xFFDCFCE7)
    val PastelPurple = Color(0xFFF3E8FF)
    val PastelYellow = Color(0xFFFEF3C7) // Amber-ish pastel
    val PastelPink = Color(0xFFFFE4E6)
    val PastelGrey = Color(0xFFF3F4F6)
    
    val PastelOrangeText = Color(0xFFC2410C) // Darker for text on pastel orange
    val PastelBlueText = Color(0xFF1E40AF)
    val PastelGreenText = Color(0xFF166534)
    val PastelPurpleText = Color(0xFF6B21A8)
    val PastelYellowText = Color(0xFF92400E)
}
