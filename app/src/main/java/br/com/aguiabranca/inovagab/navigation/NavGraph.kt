// navigation/NavGraph.kt
package br.com.aguiabranca.inovagab.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.aguiabranca.inovagab.data.model.PerfilUsuario
import br.com.aguiabranca.inovagab.ui.DetalheProjetoScreen
import br.com.aguiabranca.inovagab.ui.NotificacoesScreen
import br.com.aguiabranca.inovagab.ui.diretoria.*
import br.com.aguiabranca.inovagab.ui.gestor.*
import br.com.aguiabranca.inovagab.ui.onboarding.LoginScreen
import br.com.aguiabranca.inovagab.ui.onboarding.OnboardingScreen
import br.com.aguiabranca.inovagab.ui.onboarding.SplashScreen
import br.com.aguiabranca.inovagab.ui.operacional.DetalheIdeiaScreen
import br.com.aguiabranca.inovagab.ui.operacional.EstrategiaOperadorScreen
import br.com.aguiabranca.inovagab.ui.operacional.HomeOperadorScreen
import br.com.aguiabranca.inovagab.ui.operacional.IdeiasOperadorScreen
import br.com.aguiabranca.inovagab.ui.operacional.NovaIdeiaScreen
import br.com.aguiabranca.inovagab.ui.operacional.PerfilScreen
import br.com.aguiabranca.inovagab.data.remote.UserSession

sealed class Screen(val route: String) {
    object Splash       : Screen("splash")
    object Onboarding   : Screen("onboarding")
    object Login        : Screen("login")
    object HomeRedirect : Screen("home_redirect")

    object HomeMotorista : Screen("home_motorista")
    object NovaIdeia     : Screen("nova_ideia")
    object DetalheIdeia  : Screen("detalhe_ideia/{ideiaId}") {
        fun createRoute(id: String) = "detalhe_ideia/$id"
    }

    object MinhasIdeias: Screen("minhas_ideias")
    object HomeTerminal  : Screen("home_terminal")
    object HomeLogistica : Screen("home_logistica")
    object RegistrarRota : Screen("registrar_rota")
    object Estrategia    : Screen("estrategia")

    object HomeGestor    : Screen("home_gestor")
    object IdeiasGestor  : Screen("ideias_gestor")
    object AvaliarIdeia  : Screen("avaliar_ideia/{ideiaId}") {
        fun createRoute(id: String) = "avaliar_ideia/$id"
    }
    object Projetos         : Screen("projetos")
    object EstrategiaGestor : Screen("estrategia_gestor")

    object HomeDiretoria    : Screen("home_diretoria")
    object Ranking          : Screen("ranking")
    object ProjetosLider    : Screen("projetos_lider")
    object EstrategiaLider  : Screen("estrategia_lider")
    
    object SelecionarIdeiaProjeto : Screen("selecionar_ideia_projeto")
    object CriarProjeto : Screen("criar_projeto/{ideiaId}") {
        fun createRoute(id: String) = "criar_projeto/$id"
    }

    object DetalheProjeto : Screen("detalhe_projeto/{projetoId}") {
        fun createRoute(id: String) = "detalhe_projeto/$id"
    }

    object Notificacoes : Screen("notificacoes")
    object Perfil       : Screen("perfil")
}

@Composable
fun InovaGABNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController    = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) { SplashScreen(navController = navController) }
        composable(Screen.Onboarding.route) { OnboardingScreen(navController = navController) }
        composable(Screen.Login.route) { LoginScreen(navController = navController) }

        composable(Screen.HomeRedirect.route) {
            LaunchedEffect(Unit) {
                val perfilSessao = UserSession.perfil

                if (UserSession.token.isNullOrBlank() || perfilSessao.isNullOrBlank()) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.HomeRedirect.route) { inclusive = true }
                    }
                    return@LaunchedEffect
                }
                val perfil = when (perfilSessao.trim().uppercase()) {
                    "OPERADOR", "OPERACIONAL" -> PerfilUsuario.OPERACIONAL
                    "TERMINAL" -> PerfilUsuario.TERMINAL
                    "LOGISTICA" -> PerfilUsuario.LOGISTICA
                    "GESTOR" -> PerfilUsuario.GESTOR
                    "LIDERANCA", "LIDER" -> PerfilUsuario.LIDER
                    else -> PerfilUsuario.OPERACIONAL
                }

                val destino = when (perfil) {
                    PerfilUsuario.OPERACIONAL,
                    PerfilUsuario.TERMINAL,
                    PerfilUsuario.LOGISTICA -> Screen.HomeMotorista.route

                    PerfilUsuario.GESTOR -> Screen.HomeGestor.route
                    PerfilUsuario.LIDER -> Screen.HomeDiretoria.route
                }

                navController.navigate(destino) {
                    popUpTo(Screen.HomeRedirect.route) { inclusive = true }
                }
            }
            Box(Modifier
                .fillMaxSize()
                .background(Color(0xFF0A1628)),
                contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF185FA5))
            }
        }

        // ── Diretoria ─────────────────────────────────────────────────────────
        composable(Screen.HomeDiretoria.route) {
            HomeDiretoriaScreen(onNavigate = { navController.navigate(it) })        }
        composable(Screen.Ranking.route) {
            RankingLiderScreen(onVoltar = { navController.popBackStack() }, onNavigate = { navController.navigate(it) })
        }
        composable(Screen.ProjetosLider.route) {
             ProjetosLiderScreen(
                 onVoltar = { navController.popBackStack() },
                 onCriarProjeto = { navController.navigate(Screen.SelecionarIdeiaProjeto.route) },
                 onProjetoSelecionado = { id -> navController.navigate(Screen.DetalheProjeto.createRoute(id)) },
                 onNavigate = { navController.navigate(it) }
             )
        }
        composable(Screen.SelecionarIdeiaProjeto.route) {
            SelecaoIdeiaProjetoScreen(
                onVoltar = { navController.popBackStack() },
                onIdeiaSelecionada = { ideiaId -> 
                    navController.navigate(Screen.CriarProjeto.createRoute(ideiaId))
                }
            )
        }
        composable(Screen.CriarProjeto.route) { backStackEntry ->
            val ideiaId = backStackEntry.arguments?.getString("ideiaId") ?: ""
            CriarProjetoScreen(
                ideiaId = ideiaId,
                onSucesso = {
                    navController.navigate(Screen.ProjetosLider.route) {
                        popUpTo(Screen.ProjetosLider.route) { inclusive = true }
                    }
                },
                onVoltar = { navController.popBackStack() }
            )
        }
        composable(Screen.EstrategiaLider.route) {
            EstrategiaLiderScreen(onVoltar = { navController.popBackStack() }, onNavigate = { navController.navigate(it) })
        }

        // ── Gestor ────────────────────────────────────────────────────────────
        composable(Screen.HomeGestor.route) {
            HomeGestorScreen(navController = navController)
        }
        composable(Screen.IdeiasGestor.route) {
            IdeiasGestorScreen(navController = navController)
        }
        composable(Screen.AvaliarIdeia.route) { backStackEntry ->
            val ideiaId = backStackEntry.arguments?.getString("ideiaId") ?: ""
            AvaliarIdeiaScreen(navController = navController, ideiaId = ideiaId)
        }
        composable(Screen.EstrategiaGestor.route) {
            EstrategiaGestorScreen(navController = navController)
        }
        composable(Screen.Projetos.route) {
            ProjetosGestorScreen(
                navController = navController,
                onVerProjeto = { id -> navController.navigate(Screen.DetalheProjeto.createRoute(id)) }
            )
        }


//--Operacional-------
        composable(Screen.HomeMotorista.route) {
            HomeOperadorScreen(
                onNovaIdeia = { navController.navigate(Screen.NovaIdeia.route) },
                onVerIdeia  = { id -> navController.navigate(Screen.DetalheIdeia.createRoute(id)) },
                onNavigate  = { navController.navigate(it) }
            )
        }

        composable(Screen.NovaIdeia.route){
            NovaIdeiaScreen(
                onVoltar = {navController.popBackStack()},
                onIdeiaEnviada = {navController.popBackStack()}
            )
        }

        composable(Screen.DetalheIdeia.route){ backStackEntry ->
            val ideiaId = backStackEntry.arguments?.getString("ideiaId") ?: ""
            DetalheIdeiaScreen(
                ideiaId = ideiaId,
                onVoltar = {navController.popBackStack()},
            )
        }

        composable(Screen.Estrategia.route){
            EstrategiaOperadorScreen(
                onVoltar = {navController.popBackStack()},
                onNavigate = { navController.navigate(it) }
            )
        }
        composable(Screen.MinhasIdeias.route) {
            IdeiasOperadorScreen(
                onVoltar    = { navController.popBackStack() },
                onVerIdeia  = { id -> navController.navigate(Screen.DetalheIdeia.createRoute(id)) },
                onNovaIdeia = { navController.navigate(Screen.NovaIdeia.route) },
                onNavigate  = { navController.navigate(it) }
            )
        }

        composable(Screen.DetalheProjeto.route) { backStackEntry ->
            val projetoId = backStackEntry.arguments?.getString("projetoId") ?: ""
            DetalheProjetoScreen(
                projetoId = projetoId,
                onVoltar = { navController.popBackStack() }
            )
        }

        //-------Perfil
        composable(Screen.Perfil.route) {
            PerfilScreen(
                onVoltar = { navController.popBackStack() },
                onNavigate = {navController.navigate(it)},
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Notificacoes.route) {
            NotificacoesScreen(
                onVoltar = { navController.popBackStack() },
                onNavigate = { navController.navigate(it) }
            )
        }
    }
}
