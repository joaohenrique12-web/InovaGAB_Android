// app/src/main/java/br/com/aguiabranca/inovagab/ui/diretoria/ProjetosLiderScreen.kt
@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.aguiabranca.inovagab.ui.diretoria

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.aguiabranca.inovagab.data.model.Projeto
import br.com.aguiabranca.inovagab.data.model.StatusProjeto
import br.com.aguiabranca.inovagab.ui.components.InovaBottomNav
import br.com.aguiabranca.inovagab.ui.components.InovaTopBar
import br.com.aguiabranca.inovagab.ui.components.StatusProjetoBadge
import br.com.aguiabranca.inovagab.ui.components.liderNavItems
import br.com.aguiabranca.inovagab.ui.theme.InovaColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import br.com.aguiabranca.inovagab.data.remote.BackendRepository
import kotlinx.coroutines.flow.catch

data class ProjetosLiderUiState(
    val carregando: Boolean = true,
    val projetos: List<Projeto> = emptyList(),
    val erro: String? = null
)

class ProjetosLiderViewModel(
    private val repository: BackendRepository = BackendRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjetosLiderUiState())
    val uiState: StateFlow<ProjetosLiderUiState> = _uiState.asStateFlow()

    private fun observarProjetos() {
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erro = null) }
            repository.observarProjetos()
                .catch { ex ->
                    _uiState.update {
                        it.copy(carregando = false, erro = "Erro ao carregar projetos: ${ex.message}")
                    }
                }
                .collect { lista ->
                    _uiState.update { it.copy(projetos = lista, carregando = false) }
                }
        }
    }

    init { observarProjetos() }
}

@Composable
fun ProjetosLiderScreen(
    onVoltar: () -> Unit,
    onCriarProjeto: () -> Unit,
    onProjetoSelecionado: (String) -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: ProjetosLiderViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    ProjetosLiderScreenStateless(
        uiState = uiState,
        onVoltar = onVoltar,
        onCriarProjeto = onCriarProjeto,
        onProjetoSelecionado = onProjetoSelecionado,
        onNavigate = onNavigate
    )
}

@Composable
fun ProjetosLiderScreenStateless(
    uiState: ProjetosLiderUiState,
    onVoltar: () -> Unit,
    onCriarProjeto: () -> Unit,
    onProjetoSelecionado: (String) -> Unit,
    onNavigate: (String) -> Unit
) {
    var abaAtiva by remember { mutableIntStateOf(0) }

    val projetosFiltrados = remember(uiState.projetos, abaAtiva) {
        uiState.projetos.filter {
            when(abaAtiva) {
                0 -> it.status == StatusProjeto.EM_DESENVOLVIMENTO.name
                1 -> it.status == StatusProjeto.PLANEJADO.name
                else -> it.status == StatusProjeto.CONCLUIDO.name
            }
        }
    }

    Scaffold(
        topBar = {
            InovaTopBar(
                title = "Projetos — visão executiva",
                onBack = onVoltar,
                action = {
                    IconButton(onClick = onCriarProjeto) {
                        Icon(Icons.Default.Add, "Novo projeto", tint = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            InovaBottomNav(
                items = liderNavItems,
                currentRoute = "projetos_lider",
                onItemClick = onNavigate
            )
        },
        containerColor = InovaColors.NavyPrimary
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(InovaColors.NavyPrimary)
        ) {
            TabRow(
                selectedTabIndex = abaAtiva,
                containerColor = InovaColors.NavyPrimary,
                contentColor = InovaColors.BlueAccent,
                indicator = { positions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(positions[abaAtiva]),
                        color = InovaColors.BlueAccent
                    )
                },
                divider = { HorizontalDivider(color = Color.White.copy(alpha = 0.1f)) }
            ) {
                listOf("Em andamento", "Planejados", "Concluídos").forEachIndexed { index, title ->
                    Tab(
                        selected = abaAtiva == index,
                        onClick = { abaAtiva = index },
                        text = { 
                            Text(
                                title, 
                                fontSize = 13.sp,
                                fontWeight = if (abaAtiva == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (abaAtiva == index) Color.White else Color.White.copy(alpha = 0.5f)
                            ) 
                        }
                    )
                }
            }

            if (uiState.carregando && uiState.projetos.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = InovaColors.BlueAccent)
                }
            } else if (projetosFiltrados.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum projeto encontrado nesta etapa.", color = Color.White.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
                ) {
                    items(projetosFiltrados) { projeto ->
                        ProjetoCardExecutivo(
                            projeto = projeto,
                            onClick = { onProjetoSelecionado(projeto.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProjetoCardExecutivo(
    projeto: Projeto,
    onClick: () -> Unit
) {
    val investimento = projeto.economiaGerada * 0.15
    val roi = if (investimento > 0) ((projeto.economiaGerada - investimento) / investimento * 100).toInt() else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.07f)),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = projeto.titulo,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                StatusProjetoBadge(projeto.statusEnum())
            }
            
            Text(
                text = "Resp.: ${projeto.responsavel} - Prazo: ${projeto.prazo}",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { projeto.progresso / 100f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(99.dp)),
                    color = InovaColors.GreenSuccess,
                    trackColor = Color.Transparent
                )
                Text(text = "${projeto.progresso}%", color = InovaColors.GreenSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow(label = "Investimento", value = "R$ ${String.format("%,.0f", investimento)}")
                InfoRow(label = "Retorno estimado", value = "R$ ${String.format("%,.0f", projeto.economiaGerada)}", valueColor = InovaColors.GreenSuccess)
                InfoRow(label = "ROI", value = "+$roi%", valueColor = InovaColors.GreenSuccess)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        Text(value, color = if (valueColor == Color.Unspecified) Color.White else valueColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
