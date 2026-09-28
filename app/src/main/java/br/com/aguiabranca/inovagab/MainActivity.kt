package br.com.aguiabranca.inovagab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import br.com.aguiabranca.inovagab.data.remote.UserSession
import br.com.aguiabranca.inovagab.navigation.InovaGABNavGraph
import br.com.aguiabranca.inovagab.navigation.Screen
import br.com.aguiabranca.inovagab.ui.theme.InovaGABTheme

class MainActivity : ComponentActivity() {

    companion object {
        private const val PREFS_NAME = "inovagab_preferences"
        private const val KEY_SPLASH_EXIBIDA = "splash_exibida"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val preferences = getSharedPreferences(
            PREFS_NAME,
            MODE_PRIVATE
        )

        val usuarioLogado =
            !UserSession.token.isNullOrBlank()

        val splashJaExibida =
            preferences.getBoolean(
                KEY_SPLASH_EXIBIDA,
                false
            )

        val startDestination = when {

            // Usuário já autenticado
            usuarioLogado -> {
                Screen.HomeRedirect.route
            }

            // Splash já foi exibida anteriormente
            splashJaExibida -> {
                Screen.Login.route
            }

            // Primeiro acesso ao aplicativo
            else -> {
                Screen.Splash.route
            }
        }

        setContent {
            InovaGABTheme {

                val navController = rememberNavController()

                InovaGABNavGraph(
                    navController = navController,
                    startDestination = startDestination
                )
            }
        }
    }
}