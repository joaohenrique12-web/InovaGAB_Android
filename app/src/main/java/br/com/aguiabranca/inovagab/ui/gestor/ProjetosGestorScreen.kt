package br.com.aguiabranca.inovagab.ui.gestor

import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.navigation.NavController
import br.com.aguiabranca.inovagab.data.model.Projeto
import br.com.aguiabranca.inovagab.data.model.StatusProjeto
import br.com.aguiabranca.inovagab.ui.components.InovaBottomNav
import br.com.aguiabranca.inovagab.ui.components.gestorNavItems
import br.com.aguiabranca.inovagab.viewmodel.GestorViewModel

// Paleta dark
private val BgDark        = Color(0xFF0A1628)
private val CardDark      = Color(0xFF142235)
private val CardBorder    = Color(0xFF1E2F47)
private val BlueAccent    = Color(0xFF4A90E2)
private val GreenSuccess  = Color(0xFF1D9E75)
private val AmberWarning  = Color(0xFFEF9F27)
private val TextPrimary   = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9AA5B5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjetosGestorScreen(
    navController: NavController,
    onVerProjeto: (String) -> Unit,
    viewModel: GestorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var tabSelecionada by remember { mutableStateOf(0) }
    val tabs = listOf("Em andamento", "Planejados", "Concluídos")

    LaunchedEffect(Unit) {
        viewModel.carregarDados()
    }

    val projetosFiltrados = remember(uiState.projetos, tabSelecionada) {
        uiState.projetos.filter { projeto ->
            when (tabSelecionada) {
                0 -> projeto.status == StatusProjeto.EM_DESENVOLVIMENTO.name
                1 -> projeto.status == StatusProjeto.PLANEJADO.name
                2 -> projeto.status == StatusProjeto.CONCLUIDO.name
                else -> false
            }
        }
    }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Projetos",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Voltar",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        bottomBar = {
            InovaBottomNav(
                items = gestorNavItems,
                currentRoute = "projetos",
                onItemClick = { route -> navController.navigate(route) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDark)
                .padding(padding)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = tabSelecionada,
                containerColor = BgDark,
                contentColor = BlueAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[tabSelecionada]),
                        color = BlueAccent
                    )
                }
            ) {
                tabs.forEachIndexed { index, titulo ->
                    Tab(
                        selected = tabSelecionada == index,
                        onClick = { tabSelecionada = index },
                        text = {
                            Text(
                                text = titulo,
                                fontSize = 12.sp,
                                color = if (tabSelecionada == index) BlueAccent else TextSecondary
                            )
                        }
                    )
                }
            }

            if (uiState.carregando && uiState.projetos.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BlueAccent)
                }
            } else {
                // Conteúdo
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (projetosFiltrados.isNotEmpty()) {
                        items(projetosFiltrados) { projeto ->
                            ProjetoCard(
                                projeto = projeto,
                                onClick = { onVerProjeto(projeto.id) }
                            )
                        }
                        
                        if (tabSelecionada == 0) {
                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                                RodapeTotais(
                                    economia = uiState.dashboard?.economiaTotal ?: 0.0,
                                    produtividade = uiState.dashboard?.produtividade ?: 0
                                )
                            }
                        }
                    } else {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (tabSelecionada) {
                                        0 -> "Nenhum projeto em andamento"
                                        1 -> "Nenhum projeto planejado"
                                        else -> "Nenhum projeto concluído"
                                    },
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjetoCard(projeto: Projeto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = projeto.titulo,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = BlueAccent.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, BlueAccent.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = when(projeto.status) {
                            StatusProjeto.EM_DESENVOLVIMENTO.name -> "Em dev."
                            StatusProjeto.PLANEJADO.name -> "Plan."
                            StatusProjeto.CONCLUIDO.name -> "Concl."
                            else -> projeto.status
                        },
                        color = BlueAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Resp.: ${projeto.responsavel.ifBlank { "Não definido" }} · Prazo: ${projeto.prazo.ifBlank { "Sem prazo" }}",                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { projeto.progresso / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = GreenSuccess,
                trackColor = CardBorder
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${projeto.progresso}%",
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (projeto.economiaGerada > 0) {
                    BadgeInfo(label = "Impacto: R$${formatarMoeda(projeto.economiaGerada)}", color = GreenSuccess)
                }
                BadgeInfo(label = projeto.divisao, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun BadgeInfo(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun RodapeTotais(economia: Double, produtividade: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Economia total", color = TextSecondary, fontSize = 11.sp)
                Text(text = "R$${formatarMoeda(economia)}", color = GreenSuccess, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Produtividade", color = TextSecondary, fontSize = 11.sp)
                Text(text = "+$produtividade%", color =  GreenSuccess, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
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
