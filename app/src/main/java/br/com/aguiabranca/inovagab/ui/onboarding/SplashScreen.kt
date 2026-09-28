package br.com.aguiabranca.inovagab.ui.onboarding

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.com.aguiabranca.inovagab.data.remote.UserSession
import br.com.aguiabranca.inovagab.navigation.Screen
import br.com.aguiabranca.inovagab.ui.theme.InovaColors
import kotlinx.coroutines.delay

private const val PREFS_NAME = "inovagab_preferences"
private const val KEY_SPLASH_EXIBIDA = "splash_exibida"

@Composable
fun SplashScreen(navController: NavController) {

    val context = LocalContext.current

    var visible by remember {
        mutableStateOf(false)
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "splash_alpha"
    )

    LaunchedEffect(Unit) {

        val preferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val splashJaExibida = preferences.getBoolean(
            KEY_SPLASH_EXIBIDA,
            false
        )

        val usuarioLogado = !UserSession.token.isNullOrBlank()

        // Se o usuário já estiver autenticado,
        // não precisa mostrar Splash nem Login.
        if (usuarioLogado) {

            navController.navigate(Screen.HomeRedirect.route) {
                popUpTo(Screen.Splash.route) {
                    inclusive = true
                }
            }

            return@LaunchedEffect
        }

        // Se a Splash já apareceu alguma vez,
        // vai diretamente para o Login.
        if (splashJaExibida) {

            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) {
                    inclusive = true
                }
            }

            return@LaunchedEffect
        }

        // PRIMEIRO ACESSO
        // Marca que a Splash já foi exibida.
        preferences
            .edit()
            .putBoolean(KEY_SPLASH_EXIBIDA, true)
            .apply()

        visible = true

        delay(2000)

        // Depois da primeira Splash,
        // mostra o Onboarding.
        navController.navigate(Screen.Onboarding.route) {
            popUpTo(Screen.Splash.route) {
                inclusive = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(InovaColors.NavyPrimary),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(alpha)
        ) {

            // Logo
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(
                        color = InovaColors.BlueAccent,
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "IG",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "ÁGUIA INOVA",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Plataforma de Inovação",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(40.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                listOf(
                    "🚌",
                    "🚛",
                    "🏢"
                ).forEach { icon ->

                    Text(
                        text = icon,
                        fontSize = 24.sp
                    )
                }
            }
        }

        // Rodapé
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(alpha)
        ) {

            Text(
                text = "Grupo Águia Branca · 80 anos",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp
            )
        }
    }
}