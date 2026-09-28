// ui/theme/Theme.kt
package br.com.aguiabranca.inovagab.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── Paleta de cores ───────────────────────────────────────

object InovaColors {
    val NavyPrimary    = Color(0xFF0A1628)
    val BlueAccent     = Color(0xFF185FA5)
    val BlueLight      = Color(0xFF0281CC)
    val GreenSuccess   = Color(0xFF1D9E75)
    val GreenLight     = Color(0xFFEAF3DE)
    val AmberWarning   = Color(0xFFEF9F27)
    val AmberLight     = Color(0xFFFAEEDA)
    val RedDanger      = Color(0xFFE24B4A)
    val RedLight       = Color(0xFFFCEBEB)
    val PurpleAI       = Color(0xFF534AB7)
    val PurpleLight    = Color(0xFFEEEDFE)
    val GrayMuted      = Color(0xFF888780)
    val GrayLight      = Color(0xFFF1EFE8)
    val White          = Color(0xFFFFFFFF)
    val TextOnDark     = Color(0xFFFFFFFF)
    val TextOnDarkMuted = Color(0xB3FFFFFF)
}

// ─── ColorScheme claro ────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    primary          = InovaColors.NavyPrimary,
    onPrimary        = InovaColors.White,
    primaryContainer = InovaColors.BlueLight,
    onPrimaryContainer = InovaColors.BlueAccent,

    secondary        = InovaColors.BlueAccent,
    onSecondary      = InovaColors.White,
    secondaryContainer = InovaColors.BlueLight,
    onSecondaryContainer = Color(0xFF0C447C),

    tertiary         = InovaColors.GreenSuccess,
    onTertiary       = InovaColors.White,
    tertiaryContainer = InovaColors.GreenLight,
    onTertiaryContainer = Color(0xFF27500A),

    error            = InovaColors.RedDanger,
    onError          = InovaColors.White,
    errorContainer   = InovaColors.RedLight,
    onErrorContainer = Color(0xFF791F1F),

    background       = Color(0xFFFAF9F7),
    onBackground     = Color(0xFF1A1A18),
    surface          = InovaColors.White,
    onSurface        = Color(0xFF1A1A18),
    surfaceVariant   = InovaColors.GrayLight,
    onSurfaceVariant = Color(0xFF5F5E5A),
    outline          = Color(0xFFD3D1C7),
    outlineVariant   = Color(0xFFECEAE3),
)

// ─── ColorScheme escuro ───────────────────────────────────

private val DarkColorScheme = darkColorScheme(
    primary          = Color(0xFF85B7EB),
    onPrimary        = Color(0xFF042C53),
    primaryContainer = Color(0xFF0C447C),
    onPrimaryContainer = Color(0xFFB5D4F4),

    secondary        = Color(0xFF85B7EB),
    onSecondary      = Color(0xFF042C53),
    secondaryContainer = Color(0xFF1A61A8),
    onSecondaryContainer = Color(0xFFE6F1FB),

    tertiary         = Color(0xFF5DCAA5),
    onTertiary       = Color(0xFF04342C),
    tertiaryContainer = Color(0xFF0F6E56),
    onTertiaryContainer = Color(0xFF9FE1CB),

    error            = Color(0xFFF09595),
    onError          = Color(0xFF501313),
    errorContainer   = Color(0xFFA32D2D),
    onErrorContainer = Color(0xFFF7C1C1),

    background       = Color(0xFF141412),
    onBackground     = Color(0xFFECEAE3),
    surface          = Color(0xFF1E1D1B),
    onSurface        = Color(0xFFECEAE3),
    surfaceVariant   = Color(0xFF2C2C2A),
    onSurfaceVariant = Color(0xFFB4B2A9),
    outline          = Color(0xFF444441),
    outlineVariant   = Color(0xFF2C2C2A),
)

// ─── Tipografia ───────────────────────────────────────────

val InovaTypography = Typography(
    displayLarge  = Typography().displayLarge.copy(fontFamily  = null),
    headlineLarge = Typography().headlineLarge.copy(fontFamily = null),
    headlineMedium = Typography().headlineMedium.copy(fontFamily = null),
    headlineSmall = Typography().headlineSmall.copy(fontFamily = null),
    titleLarge    = Typography().titleLarge.copy(fontFamily    = null),
    titleMedium   = Typography().titleMedium.copy(fontFamily   = null),
    titleSmall    = Typography().titleSmall.copy(fontFamily    = null),
    bodyLarge     = Typography().bodyLarge.copy(fontFamily     = null),
    bodyMedium    = Typography().bodyMedium.copy(fontFamily    = null),
    bodySmall     = Typography().bodySmall.copy(fontFamily     = null),
    labelLarge    = Typography().labelLarge.copy(fontFamily    = null),
    labelMedium   = Typography().labelMedium.copy(fontFamily   = null),
    labelSmall    = Typography().labelSmall.copy(fontFamily    = null),
)

// ─── Tema principal ───────────────────────────────────────

@Composable
fun InovaGABTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = InovaTypography,
        content     = content
    )
}