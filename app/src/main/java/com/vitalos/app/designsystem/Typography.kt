package com.vitalos.app.designsystem

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.vitalos.app.R

val SpaceGroteskFamily = FontFamily(
    Font(R.font.space_grotesk_medium, FontWeight.Medium),
    Font(R.font.space_grotesk_semibold, FontWeight.SemiBold),
    Font(R.font.space_grotesk_bold, FontWeight.Bold),
)

val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
)

val JetBrainsMonoFamily = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
)

data class VitalOsTypography(
    val displayHero: TextStyle = TextStyle(
        fontFamily = SpaceGroteskFamily, fontSize = 56.sp, fontWeight = FontWeight.Bold,
        lineHeight = 60.sp, letterSpacing = (-0.04).em(56),
    ),
    val displayHeroMobile: TextStyle = TextStyle(
        fontFamily = SpaceGroteskFamily, fontSize = 40.sp, fontWeight = FontWeight.Bold,
        lineHeight = 44.sp, letterSpacing = (-0.03).em(40),
    ),
    val headlineLg: TextStyle = TextStyle(
        fontFamily = SpaceGroteskFamily, fontSize = 32.sp, fontWeight = FontWeight.SemiBold,
        lineHeight = 38.sp, letterSpacing = (-0.02).em(32),
    ),
    val headlineLgMobile: TextStyle = TextStyle(
        fontFamily = SpaceGroteskFamily, fontSize = 26.sp, fontWeight = FontWeight.SemiBold,
        lineHeight = 32.sp, letterSpacing = (-0.02).em(26),
    ),
    val headlineMd: TextStyle = TextStyle(
        fontFamily = SpaceGroteskFamily, fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
        lineHeight = 28.sp, letterSpacing = (-0.01).em(22),
    ),
    val headlineSm: TextStyle = TextStyle(
        fontFamily = InterFamily, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp,
    ),
    val bodyLg: TextStyle = TextStyle(
        fontFamily = InterFamily, fontSize = 16.sp, fontWeight = FontWeight.Normal,
        lineHeight = 24.sp, letterSpacing = (-0.01).em(16),
    ),
    val bodyMd: TextStyle = TextStyle(
        fontFamily = InterFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp,
    ),
    val bodySm: TextStyle = TextStyle(
        fontFamily = InterFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal,
        lineHeight = 16.sp, letterSpacing = 0.01.em(12),
    ),
    val labelCaps: TextStyle = TextStyle(
        fontFamily = InterFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
        lineHeight = 14.sp, letterSpacing = 0.08.em(11),
    ),
    val dataTabularLg: TextStyle = TextStyle(
        fontFamily = JetBrainsMonoFamily, fontSize = 28.sp, fontWeight = FontWeight.Medium,
        lineHeight = 32.sp, letterSpacing = (-0.02).em(28),
    ),
    val dataTabularMd: TextStyle = TextStyle(
        fontFamily = JetBrainsMonoFamily, fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp,
    ),
    val dataTabularSm: TextStyle = TextStyle(
        fontFamily = JetBrainsMonoFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal,
        lineHeight = 16.sp, letterSpacing = 0.02.em(12),
    ),
)

/** Converts an em multiplier (relative to [sizeSp]) into an absolute sp letter-spacing value. */
private fun Double.em(sizeSp: Int) = (this * sizeSp).sp
