package br.com.aguiabranca.inovagab.ui.operacional

import HomeViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.aguiabranca.inovagab.data.model.Ideia
import br.com.aguiabranca.inovagab.data.model.OrientacaoEstrategica
import br.com.aguiabranca.inovagab.data.model.StatusIdeia
import br.com.aguiabranca.inovagab.ui.theme.InovaColors

@Composable
fun HomeOperadorScreen(
    onNovaIdeia: () -> Unit,
    onVerIdeia: (String) -> Unit,
    onNavigate: (String) -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val uiState by homeViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        homeViewModel.carregarDados()
    }

    Scaffold(
        containerColor = InovaColors.NavyPrimary,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNovaIdeia,
                containerColor = InovaColors.BlueAccent,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nova ideia", tint = Color.White)
            }
        },
        bottomBar = {
            BottomNavOperador(
                rotaAtual = "home_motorista",
                onNavigate = onNavigate
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // ── Header ──────────────────────────────────────────────────────
            item {
                HeaderOperador(
                    nomeUsuario = uiState.nomeUsuario,
                    divisao = uiState.divisao,
                    pontos = uiState.pontos,
                    posicao = uiState.posicao,
                    onNotificacoes = { onNavigate("notificacoes") }
                )
            }

            // ── Card de ação principal ───────────────────────────────────────
            item {
                CardRegistrarIdeia(
                    onClick = onNovaIdeia,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // ── Foco estratégico ─────────────────────────────────────────────
            if (uiState.orientacoes.isNotEmpty()) {
                item {
                    SectionLabel(
                        texto = "Foco estratégico",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                item {
                    uiState.orientacoes.firstOrNull()?.let { orientacao ->
                        CardFocoEstrategico(
                            orientacao = orientacao,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // ── Minhas ideias ────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel(texto = "Minhas ideias")
                    TextButton(onClick = { onNavigate("minhas_ideias") }) {
                        Text("Ver todas", color = InovaColors.BlueAccent, fontSize = 13.sp)
                    }
                }
            }

            if (uiState.carregando) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = InovaColors.BlueAccent)
                    }
                }
            } else if (uiState.ideias.isEmpty()) {
                item {
                    Text(
                        text = "Nenhuma ideia enviada ainda.\nToque em + para começar!",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            } else {
                items(uiState.ideias.take(3)) { ideia ->
                    CardIdeia(
                        ideia = ideia,
                        onClick = { onVerIdeia(ideia.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // ── Minha jornada ────────────────────────────────────────────────
            item {
                CardJornada(
                    totalIdeias = uiState.totalIdeias,
                    aprovadas = uiState.aprovadas,
                    projetos = uiState.projetos,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Componentes internos
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeaderOperador(
    nomeUsuario: String,
    divisao: String,
    pontos: Int,
    posicao: Int,
    onNotificacoes: () -> Unit
) {

    val primeiroNome = nomeUsuario
        .trim()
        .split(" ")
        .firstOrNull { it.isNotBlank() }
        ?: "usuário"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(InovaColors.NavyPrimary)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = divisao.ifBlank { "Grupo Águia Branca" },
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
                IconButton(onClick = onNotificacoes) {
                    Icon(
                        Icons.Filled.Notifications,
                        contentDescription = "Notificações",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Text(
                text = "Olá, $primeiroNome!",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Badge de ranking
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Outlined.Star,
                    contentDescription = null,
                    tint = InovaColors.AmberWarning,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "TOP 10 inovadores · $pontos pts · posição #$posicao",
                    color = InovaColors.AmberWarning,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun CardRegistrarIdeia(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = InovaColors.BlueAccent),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "+ Registrar ideia ou problema",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Em 30 segundos, sem parar",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }
            Icon(
                Icons.Outlined.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun CardFocoEstrategico(
    orientacao: OrientacaoEstrategica,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.07f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = orientacao.titulo,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = orientacao.descricao,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${orientacao.autorNome} · até ${orientacao.prazo}",
                color = InovaColors.BlueAccent,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun CardIdeia(
    ideia: Ideia,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.07f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ideia.titulo,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(6.dp))
                StatusBadge(status = ideia.status)
            }
            if (ideia.impactoEstimado > 0) {
                Text(
                    text = "+R\$${formatarMoeda(ideia.impactoEstimado)}/ano",
                    color = InovaColors.GreenSuccess,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (label, color) = when (status) {
        StatusIdeia.ENVIADA.name     -> "Enviada"     to InovaColors.BlueAccent
        StatusIdeia.EM_ANALISE.name  -> "Em análise"  to InovaColors.AmberWarning
        StatusIdeia.APROVADA.name    -> "Aprovada"    to InovaColors.GreenSuccess
        StatusIdeia.REPROVADA.name   -> "Reprovada"   to InovaColors.RedDanger
        else                         -> status        to Color.White.copy(alpha = 0.5f)
    }
    Box(
        modifier = Modifier
            .background(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CardJornada(
    totalIdeias: Int,
    aprovadas: Int,
    projetos: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Minha jornada",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricaJornada(valor = totalIdeias.toString(), label = "Ideias")
                VerticalDividerJornada()
                MetricaJornada(valor = aprovadas.toString(), label = "Aprovada")
                VerticalDividerJornada()
                MetricaJornada(valor = projetos.toString(), label = "Projeto")
            }
        }
    }
}

@Composable
private fun MetricaJornada(valor: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = valor, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
    }
}

@Composable
private fun VerticalDividerJornada() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(Color.White.copy(alpha = 0.15f))
    )
}

@Composable
private fun SectionLabel(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        color = Color.White.copy(alpha = 0.6f),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}

@Composable
private fun BottomNavOperador(rotaAtual: String, onNavigate: (String) -> Unit) {
    val itens = listOf(
        Triple("home_motorista", Icons.Outlined.Home, "Início"),
        Triple("minhas_ideias", Icons.Outlined.Lightbulb, "Ideias"),
        Triple("estrategia", Icons.Outlined.Flag, "Estratégia"),
        Triple("notificacoes", Icons.Outlined.Notifications, "Notif."),
        Triple("perfil", Icons.Outlined.Person, "Perfil")
    )
    NavigationBar(containerColor = InovaColors.NavyPrimary) {
        itens.forEach { (rota, icone, label) ->
            NavigationBarItem(
                selected = rotaAtual == rota,
                onClick = { if (rotaAtual != rota) onNavigate(rota) },
                icon = { Icon(icone, contentDescription = label) },
                label = { Text(label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = InovaColors.BlueAccent,
                    selectedTextColor = InovaColors.BlueAccent,
                    unselectedIconColor = Color.White.copy(alpha = 0.4f),
                    unselectedTextColor = Color.White.copy(alpha = 0.4f),
                    indicatorColor = InovaColors.BlueAccent.copy(alpha = 0.15f)
                )
            )
        }
    }
}

private fun formatarMoeda(valor: Double): String {
    return when {
        valor >= 1_000_000 -> "${(valor / 1_000_000).toInt()}M"
        valor >= 1_000     -> "${(valor / 1_000).toInt()}K"
        else               -> valor.toInt().toString()
    }
}