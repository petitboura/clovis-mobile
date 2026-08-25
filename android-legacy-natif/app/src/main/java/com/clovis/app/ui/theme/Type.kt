// Lot 2A, 24/08/2026. Space Grotesk pour les titres/UI, Work Sans pour le
// texte courant -- meme paire que clovis-frontend (app/layout.tsx,
// next/font/google).
//
// Pattern verifie par recherche en ligne (developer.android.com/develop/ui/
// compose/text/fonts) : Font(resId, variationSettings = FontVariation.
// Settings(...)) directement sur le .ttf variable brut place dans res/font/
// -- PAS de XML font-family intermediaire (approche View-system plus
// ancienne, jamais confirmee pour Compose). API encore @ExperimentalTextApi
// a ce jour, opt-in necessaire. Polices variables supportees a partir
// d'Android O (API 26) uniquement -- exactement notre minSdk, donc aucun
// appareil pouvant installer cette app ne peut se trouver sous ce seuil,
// pas de repli necessaire.
package com.clovis.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.clovis.app.R

@OptIn(ExperimentalTextApi::class)
private fun spaceGrotesk(poids: Int) = Font(
    R.font.space_grotesk,
    variationSettings = FontVariation.Settings(FontVariation.weight(poids)),
)

@OptIn(ExperimentalTextApi::class)
private fun workSans(poids: Int) = Font(
    R.font.work_sans,
    variationSettings = FontVariation.Settings(FontVariation.weight(poids)),
)

private val SpaceGroteskSemiBold = FontFamily(spaceGrotesk(600))
private val SpaceGroteskMedium = FontFamily(spaceGrotesk(500))
private val WorkSansRegular = FontFamily(workSans(400))
private val WorkSansMedium = FontFamily(workSans(500))

val ClovisTypography = Typography(
    displayMedium = TextStyle(fontFamily = SpaceGroteskSemiBold, fontWeight = FontWeight.SemiBold, fontSize = 36.sp, lineHeight = 42.sp),
    headlineMedium = TextStyle(fontFamily = SpaceGroteskSemiBold, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontFamily = SpaceGroteskSemiBold, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = SpaceGroteskMedium, fontWeight = FontWeight.Medium, fontSize = 17.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = SpaceGroteskMedium, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = WorkSansRegular, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = WorkSansRegular, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = WorkSansRegular, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = WorkSansMedium, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = WorkSansMedium, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = WorkSansMedium, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp),
)
