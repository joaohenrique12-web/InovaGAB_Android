package br.com.aguiabranca.inovagab.ui.gestor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.com.aguiabranca.inovagab.data.model.Ideia
import br.com.aguiabranca.inovagab.viewmodel.GestorViewModel
import br.com.aguiabranca.inovagab.ui.components.InovaBottomNav
import br.com.aguiabranca.inovagab.ui.components.gestorNavItems
import androidx.compose.foundation.lazy.items
import br.com.aguiabranca.inovagab.data.model.StatusIdeia
import br.com.aguiabranca.inovagab.data.model.StatusProjeto

// Paleta dark mode
private val BgDark        = Color(0xFF0A1628)   // fundo geral
private val CardDark      = Color(0xFF142235)   // card padrão
private val CardBorder    = Color(0xFF1E2F47)   // borda sutil dos cards
private val BlueAccent    = Color(0xFF4A90E2)
private val GreenSuccess  = Color(0xFF1D9E75)
private val AmberWarning  = Color(0xFFEF9F27)
private val RedDanger     = Color(0xFFE24B4A)
private val TextPrimary   = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9AA5B5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeGestorScreen(
    navController: NavController,
    gestorViewModel: GestorViewModel = viewModel()
) {
    val uiState by gestorViewModel.uiState.collectAsState()
    val ideiasPendentes = uiState.ideiasParaAvaliar.filter { ideia ->
        ideia.status == StatusIdeia.ENVIADA.name || ideia.status == StatusIdeia.EM_ANALISE.name
    }

    val ideiasEmAnalise = ideiasPendentes.count { ideia ->
        ideia.status == StatusIdeia.EM_ANALISE.name
    }

    val projetosAtivos = uiState.projetos.count { projeto ->
        projeto.status == StatusProjeto.PLANEJADO.name ||
                projeto.status == StatusProjeto.EM_DESENVOLVIMENTO.name
    }

    LaunchedEffect(Unit) {
        gestorViewModel.carregarDados()
    }

    Scaffold(
        containerColor = BgDark,
        bottomBar = { 
            InovaBottomNav(
                items = gestorNavItems,
                currentRoute = "home_gestor",
                onItemClick = { route -> navController.navigate(route) }
            )
        }
    ) { padding ->
        if (uiState.carregando && uiState.dashboard == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BlueAccent)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgDark)
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    HeaderGestor(
                        nome = uiState.nomeGestor,
                        iniciais = uiState.iniciais,
                        onClickAvatar = { navController.navigate("perfil") }
                    ) 
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiCardTopo(
                            valor = uiState.ideiasParaAvaliar.size.toString(),
                            label = "Para\navaliar",
                            valorColor = TextPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (uiState.ideiasParaAvaliar.isNotEmpty()) {
                                    navController.navigate("avaliar_ideia/${uiState.ideiasParaAvaliar.first().id}")
                                }
                            }
                        )
                        KpiCardTopo(
                            valor = ideiasEmAnalise.toString(),
                            label = "Em\nanalise",
                            valorColor = BlueAccent,
                            modifier = Modifier.weight(1f)
                        )
                        KpiCardTopo(
                            valor = projetosAtivos.toString(),
                            label = "Proj.\nativos",
                            valorColor = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Funil de Inovação: Dados reais
                item {
                    FunilInovacao(
                        enviados = uiState.dashboard?.ideiasNoFunil?.enviadas ?: 0,
                        triagem = uiState.dashboard?.ideiasNoFunil?.triagem ?: 0,
                        aprovadas = uiState.dashboard?.ideiasNoFunil?.aprovadas ?: 0,
                        projetos = uiState.dashboard?.ideiasNoFunil?.projetos ?: 0
                    )
                }

                // Ideia em destaque (Pendente)
                if (ideiasPendentes.isNotEmpty()) {
                    items(
                        ideiasPendentes.sortedWith(
                            compareByDescending<Ideia> { it.urgente }
                                .thenBy { it.status != StatusIdeia.EM_ANALISE.name }
                        ).take(3)
                    ) { ideia ->
                        IdeiaDestaqueCard(ideia, onClick = {
                            navController.navigate("avaliar_ideia/${ideia.id}")
                        })
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Nenhuma ideia pendente para avaliação.",
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderGestor(nome: String, iniciais: String, onClickAvatar: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Gestor · Grupo Águia Branca",
                color = TextSecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Olá, ${nome.trim().split(" ").firstOrNull { it.isNotBlank() } ?: "Gestor"}!",                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(BlueAccent.copy(alpha = 0.25f))
                .border(1.dp, BlueAccent, CircleShape)
                .clickable { onClickAvatar() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = iniciais.ifBlank { "G" }, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun KpiCardTopo(
    valor: String,
    label: String,
    valorColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = valor, color = valorColor, fontWeight = FontWeight.Bold, fontSize = 26.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
private fun FunilInovacao(enviados: Int, triagem: Int, aprovadas: Int, projetos: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "FUNIL DE INOVAÇÃO",
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FunilItem(enviados.toString(), "Enviados", Modifier.weight(1f))
                FunilItem(triagem.toString(), "Triagem",  Modifier.weight(1f))
                FunilItem(aprovadas.toString(), "Aprov.",   Modifier.weight(1f))
                FunilItem(projetos.toString(),  "Proj.",    Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FunilItem(valor: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = valor, color = BgDark, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, color = BgDark.copy(alpha = 0.7f), fontSize = 10.sp)
    }
}

@Composable
private fun IdeiaDestaqueCard(ideia: Ideia, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "IDEIA PENDENTE",
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = ideia.titulo,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${ideia.autorNome} · meta: ${ideia.categoria}",
                color = TextSecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = BgDark,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BlueAccent),
                modifier = Modifier.clickable { onClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Avaliar", color = BlueAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = BlueAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeGestorScreenPreview() {
    HomeGestorScreen(navController = androidx.navigation.compose.rememberNavController())
}
