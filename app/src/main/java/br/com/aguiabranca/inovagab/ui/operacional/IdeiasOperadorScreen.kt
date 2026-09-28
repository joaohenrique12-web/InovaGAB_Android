package br.com.aguiabranca.inovagab.ui.operacional

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
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
import br.com.aguiabranca.inovagab.data.model.Ideia
import br.com.aguiabranca.inovagab.data.model.StatusIdeia
import br.com.aguiabranca.inovagab.ui.theme.InovaColors
import br.com.aguiabranca.inovagab.ui.components.BottomNavOperador
import br.com.aguiabranca.inovagab.ui.components.CardIdeia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import br.com.aguiabranca.inovagab.data.remote.BackendRepository
import kotlinx.coroutines.flow.catch
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

// ── ViewModel ─────────────────────────────────────────────────────────────────

data class MinhasIdeiasUiState(
    val carregando: Boolean = true,
    val ideias: List<Ideia> = emptyList(),
    val erro: String? = null
)

class MinhasIdeiasViewModel(
    private val repository: BackendRepository = BackendRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MinhasIdeiasUiState())
    val uiState: StateFlow<MinhasIdeiasUiState> = _uiState.asStateFlow()

    private fun observarIdeias() {
        val uid = repository.uidAtual()

        if (uid == null) {
            _uiState.update {
                it.copy(
                    carregando = false,
                    erro = "Usuário não autenticado. Faça login novamente."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erro = null) }
            repository.observarIdeiasDoUsuario(uid)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            carregando = false,
                            erro = "Erro ao carregar ideias: ${e.message}"
                        )
                    }
                }
                .collect { lista ->
                    _uiState.update {
                        it.copy(
                            carregando = false,
                            ideias = lista.sortedByDescending { ideia -> ideia.dataCriacao }
                        )
                    }
                }
        }
    }

    init { observarIdeias() }

    // Mantido para compatibilidade ou retry se necessário, embora o observer deva se recuperar
    fun recarregar() { observarIdeias() }
}

// ── Tela ──────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeiasOperadorScreen(
    onVoltar: () -> Unit,
    onVerIdeia: (String) -> Unit,
    onNovaIdeia: () -> Unit,
    onNavigate: (String) -> Unit,
    vm: MinhasIdeiasViewModel = viewModel()
) {
    val uiState by vm.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.recarregar()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Contadores por status
    val enviadas  = uiState.ideias.count { it.status == StatusIdeia.ENVIADA.name }
    val analise   = uiState.ideias.count { it.status == StatusIdeia.EM_ANALISE.name }
    val aprovadas = uiState.ideias.count { it.status == StatusIdeia.APROVADA.name }

    Scaffold(
        containerColor = InovaColors.NavyPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Text("Minhas ideias", color = Color.White, fontWeight = FontWeight.SemiBold)
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
                rotaAtual = "minhas_ideias",
                onNavigate = onNavigate
            )
        }
    ) { padding ->

        when {
            uiState.carregando && uiState.ideias.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = InovaColors.BlueAccent)
                }
            }

            uiState.erro != null -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.erro!!, color = InovaColors.RedDanger, fontSize = 14.sp)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { vm.recarregar() },
                            colors = ButtonDefaults.buttonColors(containerColor = InovaColors.BlueAccent)
                        ) { Text("Tentar novamente") }
                    }
                }
            }

            uiState.ideias.isEmpty() -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "Nenhuma ideia enviada ainda.",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                        Text(
                            "Toque em + para começar!",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 13.sp
                        )
                    }
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
                    // ── Resumo ─────────────────────────────────────────────
                    item {
                        CardResumoIdeias(
                            total     = uiState.ideias.size,
                            aprovadas = aprovadas,
                            projetos  = aprovadas
                        )
                    }

                    // ── Chips de status ────────────────────────────────────
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ChipContador("Enviadas", enviadas, InovaColors.BlueAccent, Modifier.weight(1f))
                            ChipContador("Análise", analise, InovaColors.AmberWarning, Modifier.weight(1f))
                            ChipContador("Aprovadas", aprovadas, InovaColors.GreenSuccess, Modifier.weight(1f))
                        }
                    }

                    // ── Label ──────────────────────────────────────────────
                    item {
                        Text(
                            text = "Todas as ideias",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // ── Lista ──────────────────────────────────────────────
                    items(uiState.ideias) { ideia ->
                        CardIdeia(
                            ideia   = ideia,
                            onClick = { onVerIdeia(ideia.id) }
                        )
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

// ── Componentes ───────────────────────────────────────────────────────────────

@Composable
private fun CardResumoIdeias(total: Int, aprovadas: Int, projetos: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = InovaColors.BlueAccent.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricaResumo(valor = total.toString(),     label = "Total",     cor = Color.White)
            DivisorVertical()
            MetricaResumo(valor = aprovadas.toString(), label = "Aprovadas", cor = InovaColors.GreenSuccess)
            DivisorVertical()
            MetricaResumo(valor = projetos.toString(),  label = "Projetos",  cor = InovaColors.AmberWarning)
        }
    }
}

@Composable
private fun MetricaResumo(valor: String, label: String, cor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, color = cor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
    }
}

@Composable
private fun DivisorVertical() {
    Box(
        Modifier
            .width(1.dp)
            .height(40.dp)
            .background(Color.White.copy(alpha = 0.1f))
    )
}

@Composable
private fun ChipContador(label: String, count: Int, cor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(cor.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(count.toString(), color = cor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(label, color = cor.copy(alpha = 0.8f), fontSize = 11.sp)
        }
    }
}
