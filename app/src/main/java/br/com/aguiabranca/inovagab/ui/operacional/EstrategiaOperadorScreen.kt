package br.com.aguiabranca.inovagab.ui.operacional

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
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
import br.com.aguiabranca.inovagab.data.model.OrientacaoEstrategica
import br.com.aguiabranca.inovagab.ui.theme.InovaColors
import br.com.aguiabranca.inovagab.ui.components.BottomNavOperador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import br.com.aguiabranca.inovagab.data.remote.BackendRepository

// ── ViewModel local (somente leitura, simples o suficiente para ficar aqui) ──

data class EstrategiaUiState(
    val carregando: Boolean = false,
    val orientacoes: List<OrientacaoEstrategica> = emptyList(),
    val erro: String? = null
)

class EstrategiaViewModel : ViewModel() {
    private val repository = BackendRepository()
    private val _uiState = MutableStateFlow(EstrategiaUiState())
    val uiState: StateFlow<EstrategiaUiState> = _uiState.asStateFlow()

    fun carregar() {
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erro = null) }
            repository.buscarOrientacoes()
                .onSuccess { lista ->
                    _uiState.update { it.copy(carregando = false, orientacoes = lista) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(carregando = false, erro = "Erro ao carregar: ${e.message}")
                    }
                }
        }
    }
}

// ── Tela ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstrategiaOperadorScreen(
    onVoltar: () -> Unit,
    onNavigate: (String) -> Unit,
    vm: EstrategiaViewModel = viewModel()
) {
    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(Unit) { vm.carregar() }

    Scaffold(
        containerColor = InovaColors.NavyPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Orientações estratégicas",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
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
            BottomNavOperador(
                rotaAtual = "estrategia",
                onNavigate = onNavigate
            )
        }
    ) { padding ->

        when {
            uiState.carregando -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = InovaColors.BlueAccent)
                }
            }

            uiState.erro != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.erro!!,
                        color = InovaColors.RedDanger,
                        fontSize = 14.sp
                    )
                }
            }

            uiState.orientacoes.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma orientação disponível.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Diretrizes da liderança para guiar suas ideias.",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    items(uiState.orientacoes) { orientacao ->
                        CardOrientacao(orientacao = orientacao)
                    }
                }
            }
        }
    }
}

@Composable
private fun CardOrientacao(orientacao: OrientacaoEstrategica) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.07f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Barra colorida de destaque
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(3.dp)
                    .background(
                        color = InovaColors.BlueAccent,
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = orientacao.titulo,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = orientacao.descricao,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = orientacao.autorNome,
                    color = InovaColors.BlueAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                if (orientacao.prazo.isNotBlank()) {
                    Text(
                        text = "até ${orientacao.prazo}",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp
                    )
                }
            }

            if (orientacao.divisao.isNotBlank() &&
                orientacao.divisao.lowercase() != "todas as divisões"
            ) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .background(
                            color = InovaColors.AmberWarning.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = orientacao.divisao,
                        color = InovaColors.AmberWarning,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}