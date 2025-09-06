package app.chesspresso.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

private val DarkColorScheme = darkColorScheme(
    primary = CoffeeCreme,
    onPrimary = CoffeeBrownContrast,
    background = CoffeeBrownDark,
    onBackground = CoffeeCremeLight,
    surface = CoffeeBrownContrast,
    onSurface = CoffeeCremeLight,
    secondary = CoffeeOrange,
    tertiary = CoffeeRust
)

private val LightColorScheme = lightColorScheme(
    primary = CoffeeBrownDark,
    onPrimary = CoffeeCremeLight,
    background = CoffeeCremeLight,
    onBackground = CoffeeBrownContrast,
    surface = CoffeeCremeMid, // jetzt mittleres Creme für Cards
    onSurface = CoffeeBrownContrast,
    secondary = CoffeeOrange,
    tertiary = CoffeeRust
)

val LocalAppDarkTheme = staticCompositionLocalOf { false }

@Composable
fun ChessPressoAppTheme(
    darkTheme: Boolean = false, // Default ist jetzt helles Theme
    dynamicColor: Boolean = false, // Dynamic Color deaktiviert
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    androidx.compose.runtime.CompositionLocalProvider(LocalAppDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CoffeeTypography,
            content = content
        )
    }
}