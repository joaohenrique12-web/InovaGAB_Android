// app/src/main/java/br/com/aguiabranca/inovagab/ui/diretoria/HomeDiretoriaScreen.kt
package br.com.aguiabranca.inovagab.ui.diretoria

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.aguiabranca.inovagab.data.model.*
import br.com.aguiabranca.inovagab.ui.components.*
import br.com.aguiabranca.inovagab.ui.theme.InovaColors
import br.com.aguiabranca.inovagab.ui.theme.InovaGABTheme
import br.com.aguiabranca.inovagab.viewmodel.DiretoriaUiState
import br.com.aguiabranca.inovagab.viewmodel.DiretoriaViewModel
import br.com.aguiabranca.inovagab.data.remote.UserSession

@Composable
fun HomeDiretoriaScreen(
    onNavigate: (String) -> Unit,
    nomeUsuario: String = UserSession.nome ?: "Lider",
    diretoriaViewModel: DiretoriaViewModel = viewModel()
) {
    val uiState by diretoriaViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { diretoriaViewModel.carregarDashboard() }
    HomeDiretoriaContent(
        uiState     = uiState,
        onNavigate  = onNavigate,
        nomeUsuario = nomeUsuario
    )
}

@Composable
fun HomeDiretoriaContent(
    uiState: DiretoriaUiState,
    onNavigate: (String) -> Unit,
    nomeUsuario: String = UserSession.nome ?: "Lider"
) {
    var rotaAtual by remember { mutableStateOf("home_diretoria") }
    val dashboard  = uiState.dashboard
    val iniciais   = nomeUsuario.split(" ").take(2).joinToString("") { it.first().toString() }
    val maiorImpacto = dashboard?.impactoPorDivisao?.maxByOrNull { it.economia }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            InovaBottomNav(
                items        = liderNavItems,
                currentRoute = rotaAtual,
                onItemClick  = { rota ->
                    rotaAtual = rota
                    if (rota != "home_diretoria") onNavigate(rota)
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Header ────────────────────────────────────────────────────────
            InovaHeader(
                subtitulo = "Diretoria Executiva - Grupo Águia Branca",
                nome = nomeUsuario, // Nome simplificado como na imagem
                iniciais = iniciais,
                onAvatarClick = { onNavigate("perfil") }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ── KPIs ─────────────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {

                // Linha 1
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashboardKpiCard(
                        modifier   = Modifier.weight(1f),
                        valor      = dashboard?.let { "R$${formatarMilhares(it.economiaTotal)}" } ?: "—",
                        label      = "Economia\ntotal",
                        corValor   = InovaColors.GreenSuccess, // Verde como na imagem
                        bgCard     = MaterialTheme.colorScheme.surface,
                        corLabel   = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    DashboardKpiCard(
                        modifier   = Modifier.weight(1f),
                        valor      = dashboard?.let { "${it.roiMedio}%" } ?: "—",
                        label      = "ROI\nmédio",
                        corValor   = Color.White,
                        bgCard     = MaterialTheme.colorScheme.surface,
                        corLabel   = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    DashboardKpiCard(
                        modifier   = Modifier.weight(1f),
                        valor      = dashboard?.totalProjetos?.toString() ?: "—",
                        label      = "Total\nprojetos",
                        corValor   = Color.White,
                        bgCard     = MaterialTheme.colorScheme.surface,
                        corLabel   = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Linha 2
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashboardKpiCard(
                        modifier   = Modifier.weight(1f),
                        valor      = dashboard?.ideiasNoFunil?.enviadas?.toString() ?: "—",
                        label      = "Ideias",
                        corValor   = Color.White,
                        bgCard     = MaterialTheme.colorScheme.surface,
                        corLabel   = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    DashboardKpiCard(
                        modifier   = Modifier.weight(1f),
                        valor      = dashboard?.projetosAtivos?.toString() ?: "—",
                        label      = "Em\nandamento",
                        corValor   = Color.White,
                        bgCard     = MaterialTheme.colorScheme.surface,
                        corLabel   = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    DashboardKpiCard(
                        modifier   = Modifier.weight(1f),
                        valor      = dashboard?.projetosConcluidos?.toString() ?: "—",
                        label      = "Concluídos",
                        corValor   = Color.White,
                        bgCard     = MaterialTheme.colorScheme.surface,
                        corLabel   = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Atalho para Ranking ──────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clickable { onNavigate("ranking") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = InovaColors.BlueAccent.copy(alpha = 0.15f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(InovaColors.BlueAccent))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.EmojiEvents,
                        contentDescription = null,
                        tint = InovaColors.BlueAccent,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Ver Ranking de Inovadores",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Confira quem são os colaboradores de destaque",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Card divisões ─────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = SolidColor(MaterialTheme.colorScheme.outlineVariant)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "IMPACTO POR DIVISÃO",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    if (dashboard?.impactoPorDivisao.isNullOrEmpty()) {
                        Text(
                            text = "Sem dados por divisão no momento.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    } else {
                        dashboard?.impactoPorDivisao?.forEach { item ->
                            DivisaoDashboardItem(
                                item       = item,
                                corTexto   = Color.White,
                                corMuted   = MaterialTheme.colorScheme.onSurfaceVariant,
                                bgTrack    = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Destaque ──────────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F1FB)) // Azul claro conforme imagem
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val corTextoDestaque = InovaColors.BlueAccent
                    Text(
                        text       = "Destaque — maior impacto",
                        color      = corTextoDestaque,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text     = maiorImpacto?.let {
                            "${it.divisao} · R$${formatarMilhares(it.economia)} gerados"
                        } ?: "Sem destaque calculado pelo backend.",
                        color    = corTextoDestaque,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (maiorImpacto != null) {
                        Text(
                            text       = maiorImpacto.divisao,
                            color      = corTextoDestaque,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun DashboardKpiCard(
    modifier: Modifier = Modifier,
    valor: String,
    label: String,
    corValor: Color,
    bgCard: Color,
    corLabel: Color
) {
    Card(
        modifier = modifier.height(92.dp),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = bgCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(MaterialTheme.colorScheme.outlineVariant)
        )
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text       = valor,
                color      = corValor,
                fontWeight = FontWeight.Bold,
                fontSize   = 26.sp,
                textAlign  = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text       = label,
                color      = corLabel,
                fontSize   = 11.sp,
                lineHeight = 13.sp,
                textAlign  = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DivisaoDashboardItem(
    item: ImpactoDivisao,
    corTexto: Color,
    corMuted: Color,
    bgTrack: Color
) {
    val corBarra = when (item.divisao.lowercase()) {
        "passageiros"            -> InovaColors.GreenSuccess
        "logística", "logistica" -> InovaColors.BlueAccent
        "comércio",  "comercio"  -> InovaColors.AmberWarning
        "terminal"               -> InovaColors.PurpleAI
        else                     -> InovaColors.GrayMuted
    }

    Column {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text = item.divisao,
                fontSize = 13.sp,
                color = corTexto,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "R$${formatarMilhares(item.economia)} · ${item.percentual}%",
                fontSize = 12.sp,
                color = corTexto,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress   = { item.percentual / 100f },
            modifier   = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color      = corBarra,
            trackColor = bgTrack,
            strokeCap  = StrokeCap.Round
        )
    }
}

private fun formatarMilhares(valor: Double): String {
    val fmt = when {
        valor >= 1_000_000 -> "%.1fM".format(valor / 1_000_000)
        valor >= 1_000     -> "%.0fk".format(valor / 1_000)
        else               -> "%.0f".format(valor)
    }
    return fmt.replace(".", ",")
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewHomeDiretoriaEscuro() {
    InovaGABTheme(darkTheme = true) {
        HomeDiretoriaContent(
            onNavigate  = {},
            nomeUsuario = "Lider Demo",
            uiState     = fakeUiState()
        )
    }
}

private fun fakeUiState() = DiretoriaUiState(
    carregando = false,
    dashboard  = DashboardExecutivo(
        economiaTotal  = 3200000.0,
        roiMedio       = 35,
        projetosAtivos = 24,
        ideiasNoFunil  = FunilInovacao(
            enviadas  = 120,
            triagem   = 48,
            aprovadas = 24,
            projetos  = 24
        ),
        engajamentoPorPerfil = emptyList(),
        impactoPorDivisao = emptyList()
    )
)
