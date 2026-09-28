package br.com.aguiabranca.inovagab.ui.operacional

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.aguiabranca.inovagab.ui.components.BottomNavOperador
import br.com.aguiabranca.inovagab.ui.theme.InovaColors
import br.com.aguiabranca.inovagab.viewmodel.PerfilViewModel
import br.com.aguiabranca.inovagab.data.remote.UserSession
import br.com.aguiabranca.inovagab.ui.components.InovaBottomNav
import br.com.aguiabranca.inovagab.ui.components.gestorNavItems
import br.com.aguiabranca.inovagab.ui.components.liderNavItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    onVoltar: () -> Unit,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit,
    vm: PerfilViewModel = viewModel()
) {
    val uiState by vm.uiState.collectAsState()
    val perfilAtual = uiState.perfil
        .ifBlank { UserSession.perfil.orEmpty() }
        .trim()
        .uppercase()

    LaunchedEffect(Unit) { vm.carregar() }

    LaunchedEffect(uiState.logoutFeito) {
        if (uiState.logoutFeito) onLogout()
    }

    Scaffold(
        containerColor = InovaColors.NavyPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Text("Meu Perfil", color = Color.White, fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = InovaColors.NavyPrimary
                )
            )
        },
        bottomBar = {
            when (perfilAtual) {
                "GESTOR" -> InovaBottomNav(
                    items = gestorNavItems,
                    currentRoute = "perfil",
                    onItemClick = onNavigate
                )

                "LIDER", "LIDERANCA" -> InovaBottomNav(
                    items = liderNavItems,
                    currentRoute = "perfil",
                    onItemClick = onNavigate
                )

                else -> BottomNavOperador(
                    rotaAtual = "perfil",
                    onNavigate = onNavigate
                )
            }
        }
    ) { padding ->

        if (uiState.carregando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = InovaColors.BlueAccent)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(InovaColors.BlueAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val iniciais = uiState.nome
                            .split(" ")
                            .filter { it.isNotBlank() }
                            .take(2)
                            .joinToString("") { it.first().uppercase() }

                        Text(
                            text = iniciais.ifBlank { "??" },
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = uiState.nome,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.email,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Badge de perfil (Somente leitura)
                    Surface(
                        color = InovaColors.BlueAccent.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, InovaColors.BlueAccent.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = perfilLabel(uiState.perfil),
                            color = InovaColors.BlueAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Métrica de Pontuação e Ranking
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricaPerfil(valor = uiState.pontos.toString(), label = "Pontos", cor = InovaColors.AmberWarning)
                    Box(Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.1f)))
                    MetricaPerfil(
                        valor = "#${uiState.posicao}",
                        label = "Ranking",
                        cor = InovaColors.GreenSuccess,
                        modifier = Modifier.clickable { onNavigate("ranking") }
                    )
                    Box(Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.1f)))
                    MetricaPerfil(valor = uiState.totalIdeias.toString(), label = "Ideias", cor = InovaColors.BlueAccent)
                }
            }

            // Seção de ações
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("CONTA", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = { vm.logout() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = InovaColors.RedDanger),
                        border = androidx.compose.foundation.BorderStroke(1.dp, InovaColors.RedDanger.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sair da conta", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricaPerfil(
    valor: String,
    label: String,
    cor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = valor, color = cor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
    }
}

private fun perfilLabel(perfil: String): String = when (perfil) {
    "OPERACIONAL" -> "Operador"
    "TERMINAL"    -> "Terminal"
    "LOGISTICA"   -> "Logística"
    "GESTOR"      -> "Gestor"
    "LIDER"       -> "Líder"
    else          -> perfil
}
