package br.com.aguiabranca.inovagab.ui.gestor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.com.aguiabranca.inovagab.ui.components.CardIdeia
import br.com.aguiabranca.inovagab.ui.components.InovaBottomNav
import br.com.aguiabranca.inovagab.ui.components.gestorNavItems
import br.com.aguiabranca.inovagab.ui.theme.InovaColors
import br.com.aguiabranca.inovagab.viewmodel.GestorViewModel
import kotlinx.coroutines.flow.catch
import br.com.aguiabranca.inovagab.data.remote.BackendRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeiasGestorScreen(
    navController: NavController,
    viewModel: GestorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var tabSelecionada by remember { mutableStateOf(0) }
    val repository = remember { BackendRepository() }
    
    // Lista de histórico local para esta tela
    var listaHistorico by remember { mutableStateOf<List<br.com.aguiabranca.inovagab.data.model.Ideia>>(emptyList()) }
    var carregandoHistorico by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.carregarDados()
    }

    // Carrega histórico quando mudar de aba
    LaunchedEffect(tabSelecionada) {
        if (tabSelecionada == 1) {
            carregandoHistorico = true
            repository.observarHistoricoGestor()
                .catch { carregandoHistorico = false }
                .collect { 
                    listaHistorico = it
                    carregandoHistorico = false
                }
        }
    }

    Scaffold(
        containerColor = InovaColors.NavyPrimary,
        topBar = {
            TopAppBar(
                title = { Text("Gerenciar Ideias", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = InovaColors.NavyPrimary)
            )
        },
        bottomBar = {
            InovaBottomNav(items = gestorNavItems, currentRoute = "ideias_gestor", onItemClick = { navController.navigate(it) })
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            TabRow(
                selectedTabIndex = tabSelecionada,
                containerColor = InovaColors.NavyPrimary,
                contentColor = InovaColors.BlueAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(Modifier.tabIndicatorOffset(tabPositions[tabSelecionada]), color = InovaColors.BlueAccent)
                },
                divider = { Divider(color = Color.White.copy(alpha = 0.1f)) }
            ) {
                Tab(
                    selected = tabSelecionada == 0,
                    onClick = { tabSelecionada = 0 },
                    text = { Text("Pendentes (${uiState.ideiasParaAvaliar.size})", fontSize = 13.sp) }
                )
                Tab(
                    selected = tabSelecionada == 1,
                    onClick = { tabSelecionada = 1 },
                    text = { Text("Avaliadas", fontSize = 13.sp) }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                val listaExibida = if (tabSelecionada == 0) uiState.ideiasParaAvaliar else listaHistorico
                val estaCarregando = if (tabSelecionada == 0) uiState.carregando else carregandoHistorico

                if (estaCarregando && listaExibida.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = InovaColors.BlueAccent)
                } else if (listaExibida.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (tabSelecionada == 0) Icons.Outlined.HourglassEmpty else Icons.Outlined.History,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = if (tabSelecionada == 0) "Nenhuma ideia para avaliar" else "Histórico vazio",
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(listaExibida) { ideia ->
                            CardIdeia(
                                ideia = ideia,
                                onClick = { 
                                    if (tabSelecionada == 0) {
                                        navController.navigate("avaliar_ideia/${ideia.id}")
                                    } else {
                                        navController.navigate("detalhe_ideia/${ideia.id}")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
