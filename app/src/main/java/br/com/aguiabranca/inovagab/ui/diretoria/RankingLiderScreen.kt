// app/src/main/java/br/com/aguiabranca/inovagab/ui/diretoria/RankingLiderScreen.kt
@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.aguiabranca.inovagab.ui.diretoria

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.aguiabranca.inovagab.data.model.RankingItem
import br.com.aguiabranca.inovagab.data.remote.BackendRepository
import br.com.aguiabranca.inovagab.ui.components.InovaBottomNav
import br.com.aguiabranca.inovagab.ui.components.InovaTopBar
import br.com.aguiabranca.inovagab.ui.components.liderNavItems
import br.com.aguiabranca.inovagab.ui.theme.InovaColors
import br.com.aguiabranca.inovagab.ui.theme.InovaGABTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import br.com.aguiabranca.inovagab.data.remote.UserSession
import br.com.aguiabranca.inovagab.ui.components.BottomNavOperador
import br.com.aguiabranca.inovagab.ui.components.gestorNavItems

// ─── ViewModel ─────────────────────────────────────────────────────────────

data class RankingLiderUiState(
    val carregando: Boolean = true,
    val ranking: List<RankingItem> = emptyList(),
    val erro: String? = null
)

class RankingLiderViewModel(
    private val repository: BackendRepository = BackendRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RankingLiderUiState())
    val uiState: StateFlow<RankingLiderUiState> = _uiState.asStateFlow()

    fun carregarRanking() {
        _uiState.value = _uiState.value.copy(carregando = true, erro = null)

        viewModelScope.launch {
            try {
                val result = repository.buscarRanking()
                result.onSuccess { ranking ->
                    _uiState.value = _uiState.value.copy(
                        ranking = ranking,
                        carregando = false
                    )
                }
                result.onFailure { ex ->
                    _uiState.value = _uiState.value.copy(
                        carregando = false,
                        erro = ex.message ?: "Erro ao carregar ranking"
                    )
                }
            } catch (ex: Exception) {
                _uiState.value = _uiState.value.copy(
                    carregando = false,
                    erro = "Erro inesperado"
                )
            }
        }
    }

    init {
        carregarRanking()
    }
}

// ─── Screen Stateful ─────────────────────────────────────────────────────────

@Composable
fun RankingLiderScreen(
    onVoltar: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: RankingLiderViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val perfil = UserSession.perfil.orEmpty().uppercase()

    val tipoRanking = when (perfil) {
        "OPERADOR", "OPERACIONAL", "TERMINAL", "LOGISTICA" -> "operador"
        "GESTOR" -> "gestor"
        "LIDER", "LIDERANCA" -> "lider"
        else -> "operador"
    }

    RankingLiderScreenStateless(
        uiState = uiState,
        onVoltar = onVoltar,
        onNavigate = onNavigate,
        tipoRanking = tipoRanking,
        onRecarregar = { viewModel.carregarRanking() }
    )
}

// ─── Screen Stateless ─────────────────────────────────────────────────────────

@Composable
fun RankingLiderScreenStateless(
    uiState: RankingLiderUiState,
    onVoltar: () -> Unit,
    onNavigate: (String) -> Unit,
    tipoRanking: String = "lider",
    onRecarregar: () -> Unit
) {
    var abaAtiva by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            InovaTopBar(
                title = "Ranking de inovadores",
                onBack = onVoltar
            )
        },
        bottomBar = {
            when (tipoRanking) {
                "operador" -> BottomNavOperador(
                    rotaAtual = "ranking",
                    onNavigate = onNavigate
                )

                "gestor" -> InovaBottomNav(
                    items = gestorNavItems,
                    currentRoute = "ranking",
                    onItemClick = onNavigate
                )

                else -> InovaBottomNav(
                    items = liderNavItems,
                    currentRoute = "ranking",
                    onItemClick = onNavigate
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Abas
            TabRow(
                selectedTabIndex = abaAtiva,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { positions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(positions[abaAtiva]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }
            ) {
                Tab(
                    selected = abaAtiva == 0,
                    onClick = { abaAtiva = 0 },
                    text = { 
                        Text(
                            "Geral", 
                            color = if (abaAtiva == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant 
                        ) 
                    }
                )
                Tab(
                    selected = abaAtiva == 1,
                    onClick = { abaAtiva = 1 },
                    text = { 
                        Text(
                            "Time", 
                            color = if (abaAtiva == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant 
                        ) 
                    }
                )
            }

            // Conteúdo
            if (uiState.carregando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.erro != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Erro ao carregar", color = MaterialTheme.colorScheme.onBackground)
                        Button(onClick = onRecarregar, modifier = Modifier.padding(top = 16.dp)) {
                            Text("Recarregar")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
                ) {
                    // Pódio (top 3)
                    item {
                        PodioCardLider(top3 = uiState.ranking.take(3))
                    }

                    // Lista (4º em diante)
                    items(uiState.ranking.drop(3)) { item ->
                        RankingItemCardLider(item)
                    }
                }
            }
        }
    }
}

// ─── Pódio (Top 3) ────────────────────────────────────────────────────────────

@Composable
private fun PodioCardLider(
    top3: List<RankingItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth().height(180.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(MaterialTheme.colorScheme.outlineVariant)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 2º lugar
            if (top3.size > 1) PodioItemLider(top3[1], 2, Modifier.weight(1f))
            else Spacer(Modifier.weight(1f))

            // 1º lugar
            if (top3.isNotEmpty()) PodioItemLider(top3[0], 1, Modifier.weight(1f), destaque = true)
            else Spacer(Modifier.weight(1f))

            // 3º lugar
            if (top3.size > 2) PodioItemLider(top3[2], 3, Modifier.weight(1f))
            else Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun PodioItemLider(
    ranking: RankingItem,
    posicao: Int,
    modifier: Modifier = Modifier,
    destaque: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            color = if (destaque) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = CircleShape,
            modifier = Modifier.size(if (destaque) 60.dp else 50.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    ranking.usuarioNome.take(2).uppercase(),
                    color = if (destaque) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(ranking.usuarioNome, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("${ranking.pontos} pts", color = InovaColors.GreenSuccess, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

// ─── Item do Ranking (4º em diante) ────────────────────────────────────────

@Composable
private fun RankingItemCardLider(
    item: RankingItem
) {
    Card(
        modifier = Modifier.fillMaxWidth().height(80.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(MaterialTheme.colorScheme.outlineVariant)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("${item.posicao}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    }
                }
                Column {
                    Text(item.usuarioNome, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(item.divisao, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
            }
            Text("${item.pontos} pts", color = InovaColors.GreenSuccess, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview
@Composable
fun RankingLiderScreenPreview() {
    val rankingDemo = listOf(
        RankingItem(1, "1", "Carlos Mendes", "Logística - ES/SP", 960, 8, 3, 45000.0),
        RankingItem(2, "2", "Ana Silva", "Operação", 850, 6, 2, 35000.0),
        RankingItem(3, "3", "Mariana Costa", "Passageiros", 720, 5, 1, 20000.0),
        RankingItem(4, "4", "João Pedro", "Logística", 650, 4, 1, 15000.0)
    )
    InovaGABTheme(darkTheme = true) {
        RankingLiderScreenStateless(
            uiState = RankingLiderUiState(carregando = false, ranking = rankingDemo),
            onVoltar = {}, onNavigate = {}, tipoRanking = "lider", onRecarregar = {}
        )
    }
}
