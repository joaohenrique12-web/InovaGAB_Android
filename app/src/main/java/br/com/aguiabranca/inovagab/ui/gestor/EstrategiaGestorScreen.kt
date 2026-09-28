package br.com.aguiabranca.inovagab.ui.gestor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.com.aguiabranca.inovagab.data.model.OrientacaoEstrategica
import br.com.aguiabranca.inovagab.ui.components.InovaBottomNav
import br.com.aguiabranca.inovagab.ui.components.gestorNavItems
import br.com.aguiabranca.inovagab.viewmodel.EstrategiaLiderViewModel

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
fun EstrategiaGestorScreen(
    navController: NavController,
    viewModel: EstrategiaLiderViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.carregarOrientacoes()
    }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Orientações estratégicas",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
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
                currentRoute = "estrategia_gestor",
                onItemClick = { route -> navController.navigate(route) }
            )
        }
    ) { padding ->
        if (uiState.carregando && uiState.orientacoes.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BlueAccent)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgDark)
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Subtítulo
                item {
                    Text(
                        text = "Consulte as diretrizes definidas pela liderança.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (uiState.orientacoes.isEmpty() && !uiState.carregando) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Nenhuma orientação estratégica encontrada.", color = TextSecondary)
                        }
                    }
                } else {
                    // Cards de orientações
                    items(uiState.orientacoes) { orientacao ->
                        OrientacaoCard(orientacao)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrientacaoCard(orientacao: OrientacaoEstrategica) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Título + badge prazo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = orientacao.titulo,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                if (orientacao.prazo.isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AmberWarning.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = orientacao.prazo,
                            color = AmberWarning,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Descrição
            Text(
                text = orientacao.descricao,
                color = TextPrimary.copy(alpha = 0.85f),
                fontSize = 13.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Autor e Divisão
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${orientacao.divisao} · ${orientacao.autorNome}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chip somente leitura
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = GreenSuccess.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, GreenSuccess.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = GreenSuccess,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Somente leitura",
                        color = GreenSuccess,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
