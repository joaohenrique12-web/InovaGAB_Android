

package br.com.aguiabranca.inovagab.ui.diretoria

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.aguiabranca.inovagab.data.remote.BackendRepository
import br.com.aguiabranca.inovagab.data.model.OrientacaoEstrategica
import br.com.aguiabranca.inovagab.ui.components.InovaBottomNav
import br.com.aguiabranca.inovagab.ui.components.InovaTopBar
import br.com.aguiabranca.inovagab.ui.components.liderNavItems
import br.com.aguiabranca.inovagab.ui.theme.InovaColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─── ViewModel ─────────────────────────────────────────────────────────────

data class EstrategiaLiderUiState(
    val carregando: Boolean = true,
    val orientacoes: List<OrientacaoEstrategica> = emptyList(),
    val erro: String? = null,
    val mensagemSucesso: String? = null
)

class EstrategiaLiderViewModel(
    private val repository: BackendRepository = BackendRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EstrategiaLiderUiState())
    val uiState: StateFlow<EstrategiaLiderUiState> = _uiState.asStateFlow()

    private fun carregarOrientacoes() {
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erro = null) }

            repository.buscarOrientacoes()
                .onSuccess { lista ->
                    _uiState.update {
                        it.copy(
                            orientacoes = lista,
                            carregando = false,
                            erro = null
                        )
                    }
                }
                .onFailure { ex ->
                    _uiState.update {
                        it.copy(
                            carregando = false,
                            erro = "Erro ao carregar orientações: ${ex.message}"
                        )
                    }
                }
        }
    }

    fun criarOrientacao(orientacao: OrientacaoEstrategica) {
        viewModelScope.launch {
            repository.criarOrientacao(orientacao)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            mensagemSucesso = "Orientação criada com sucesso.",
                            erro = null
                        )
                    }
                    carregarOrientacoes()
                }
                .onFailure { ex ->
                    _uiState.update { it.copy(erro = "Erro ao criar: ${ex.message}") }
                }
        }
    }

    fun atualizarOrientacao(orientacao: OrientacaoEstrategica) {
        viewModelScope.launch {
            repository.atualizarOrientacao(orientacao)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            mensagemSucesso = "Orientação atualizada com sucesso.",
                            erro = null
                        )
                    }
                    carregarOrientacoes()
                }
                .onFailure { ex ->
                    _uiState.update { it.copy(erro = "Erro ao atualizar: ${ex.message}") }
                }
        }
    }

    fun deletarOrientacao(id: String) {
        viewModelScope.launch {
            repository.deletarOrientacao(id)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            orientacoes = state.orientacoes.filterNot { it.id == id },
                            mensagemSucesso = "Orientação desativada com sucesso.",
                            erro = null
                        )
                    }
                }
                .onFailure { ex ->
                    _uiState.update { it.copy(erro = "Erro ao desativar orientação: ${ex.message}") }
                }
        }
    }

    init {
        carregarOrientacoes()
    }

    fun limparMensagemSucesso() {
        _uiState.update { it.copy(mensagemSucesso = null) }
    }
}

// ─── Screen Stateful ─────────────────────────────────────────────────────────

@Composable
fun EstrategiaLiderScreen(
    onVoltar: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: EstrategiaLiderViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    EstrategiaLiderScreenStateless(
        uiState = uiState,
        onVoltar = onVoltar,
        onNavigate = onNavigate,
        onCriar = { viewModel.criarOrientacao(it) },
        onAtualizar = { viewModel.atualizarOrientacao(it) },
        onDeletar = { viewModel.deletarOrientacao(it) },
        onLimparMensagem = { viewModel.limparMensagemSucesso() }
    )
}

// ─── Screen Stateless ─────────────────────────────────────────────────────────

@Composable
fun EstrategiaLiderScreenStateless(
    uiState: EstrategiaLiderUiState,
    onVoltar: () -> Unit,
    onNavigate: (String) -> Unit,
    onCriar: (OrientacaoEstrategica) -> Unit,
    onAtualizar: (OrientacaoEstrategica) -> Unit,
    onDeletar: (String) -> Unit,
    onLimparMensagem: () -> Unit
) {
    var mostrarDialogCriar by remember { mutableStateOf(false) }
    var orientacaoEditando by remember { mutableStateOf<OrientacaoEstrategica?>(null) }

    var orientacaoParaDeletar by remember { mutableStateOf<OrientacaoEstrategica?>(null) }
    var orientacaoParaConfirmarSalvar by remember { mutableStateOf<OrientacaoEstrategica?>(null) }

    LaunchedEffect(uiState.mensagemSucesso) {
        if (uiState.mensagemSucesso != null) {
            delay(3000)
            onLimparMensagem()
        }
    }

    Scaffold(
        topBar = {
            InovaTopBar(
                title = "Orientações estratégicas",
                onBack = onVoltar,
                action = {
                    IconButton(onClick = { mostrarDialogCriar = true }) {
                        Icon(Icons.Default.Add, "Nova orientação")
                    }
                }
            )
        },
        bottomBar = {
            InovaBottomNav(
                items = liderNavItems,
                currentRoute = "estrategia_lider",
                onItemClick = onNavigate
            )
        },
        containerColor = InovaColors.NavyPrimary
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(InovaColors.NavyPrimary)
        ) {
            if (uiState.carregando && uiState.orientacoes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = InovaColors.BlueAccent)
                }
            } else if (uiState.orientacoes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhuma orientação cadastrada.", color = Color.White.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 150.dp)
                ) {
                    items(
                        items = uiState.orientacoes,
                        key = { orientacao -> orientacao.id }
                    ) { orientacao ->
                        OrientacaoCard(
                            orientacao = orientacao,
                            onEditar = { orientacaoEditando = orientacao },
                            onDeletar = { orientacaoParaDeletar = orientacao }
                        )
                    }
                }
            }

            if (uiState.orientacoes.isNotEmpty() && !uiState.carregando) {
                Button(
                    onClick = { mostrarDialogCriar = true },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = InovaColors.BlueAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.padding(end = 8.dp))
                    Text("Nova orientação estratégica", fontWeight = FontWeight.SemiBold)
                }
            }

            uiState.mensagemSucesso?.let { mensagem ->
                val bottomPadding = if (uiState.orientacoes.isNotEmpty() && !uiState.carregando) {
                    84.dp
                } else {
                    16.dp
                }

                MensagemTemporariaSucesso(
                    mensagem = mensagem,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 16.dp, end = 16.dp, bottom = bottomPadding)
                )
            }
        }
    }

    // Dialog Criar/Editar
    if (mostrarDialogCriar || orientacaoEditando != null) {
        OrientacaoDialog(
            orientacao = orientacaoEditando,
            onSalvar = {
                if (orientacaoEditando != null) {
                    orientacaoParaConfirmarSalvar = it
                } else {
                    onCriar(it)
                    mostrarDialogCriar = false
                }
            },
            onFechar = {
                mostrarDialogCriar = false
                orientacaoEditando = null
            }
        )
    }

    // Confirmação de Exclusão
    if (orientacaoParaDeletar != null) {
        AlertDialog(
            onDismissRequest = { orientacaoParaDeletar = null },
            title = { Text("Desativar orientação?") },
            text = { Text("Tem certeza que deseja desativar esta orientação? Ela deixará de aparecer entre as orientações ativas, mas continuará armazenada no histórico.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletar(orientacaoParaDeletar!!.id)
                        orientacaoParaDeletar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = InovaColors.RedDanger)
                ) { Text("Desativar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { orientacaoParaDeletar = null }) {
                    Text("Cancelar", color = Color.White)
                }
            },
            containerColor = InovaColors.NavyPrimary,
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.8f)
        )
    }

    // Confirmação de Atualização
    if (orientacaoParaConfirmarSalvar != null) {
        AlertDialog(
            onDismissRequest = { orientacaoParaConfirmarSalvar = null },
            title = { Text("Salvar alterações?") },
            text = { Text("Deseja atualizar as informações desta orientação estratégica?") },
            confirmButton = {
                Button(
                    onClick = {
                        onAtualizar(orientacaoParaConfirmarSalvar!!)
                        orientacaoParaConfirmarSalvar = null
                        orientacaoEditando = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = InovaColors.BlueAccent)
                ) { Text("Confirmar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { orientacaoParaConfirmarSalvar = null }) {
                    Text("Cancelar", color = Color.White)
                }
            },
            containerColor = InovaColors.NavyPrimary,
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun MensagemTemporariaSucesso(
    mensagem: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF082D2A)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = InovaColors.GreenSuccess,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = mensagem,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun OrientacaoCard(
    orientacao: OrientacaoEstrategica,
    onEditar: () -> Unit,
    onDeletar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.07f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(orientacao.titulo, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(orientacao.divisao, color = InovaColors.BlueAccent, fontSize = 12.sp)
                }
                Row {
                    IconButton(onClick = onEditar) {
                        Icon(Icons.Default.Edit, "Editar", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDeletar) {
                        Icon(Icons.Default.Delete, "Deletar", tint = InovaColors.RedDanger.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                orientacao.descricao,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (orientacao.prazo.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.DateRange, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Prazo: ${orientacao.prazo}", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun OrientacaoDialog(
    orientacao: OrientacaoEstrategica?,
    onSalvar: (OrientacaoEstrategica) -> Unit,
    onFechar: () -> Unit
) {
    var titulo by remember { mutableStateOf(orientacao?.titulo ?: "") }
    var descricao by remember { mutableStateOf(orientacao?.descricao ?: "") }
    var divisao by remember { mutableStateOf(orientacao?.divisao ?: "") }
    var prazo by remember { mutableStateOf(orientacao?.prazo ?: "") }
    var prazoErro by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text(if (orientacao == null) "Nova orientação" else "Editar orientação", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = campoCores()
                )
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = campoCores()
                )
                OutlinedTextField(
                    value = divisao,
                    onValueChange = { divisao = it },
                    label = { Text("Divisão") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = campoCores()
                )
                OutlinedTextField(
                    value = prazo,
                    onValueChange = {
                        prazo = it
                        prazoErro = null
                    },
                    label = { Text("Prazo") },
                    placeholder = { Text("dd/MM/aaaa") },
                    isError = prazoErro != null,
                    supportingText = {
                        prazoErro?.let { Text(it) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = campoCores()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val prazoValidado = prazo.trim()
                    if (!prazoValidoOuVazio(prazoValidado)) {
                        prazoErro = "Use uma data válida no formato dd/MM/aaaa."
                        return@Button
                    }

                    if (titulo.isNotBlank() && descricao.isNotBlank()) {
                        onSalvar(OrientacaoEstrategica(
                            id = orientacao?.id ?: "",
                            titulo = titulo,
                            descricao = descricao,
                            divisao = divisao,
                            autorNome = orientacao?.autorNome ?: "Líder",
                            prazo = prazoValidado
                        ))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = InovaColors.BlueAccent)
            ) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onFechar) { Text("Cancelar", color = Color.White) }
        },
        containerColor = InovaColors.NavyPrimary
    )
}

@Composable
private fun campoCores() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = InovaColors.BlueAccent,
    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = InovaColors.BlueAccent,
    unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
)

private fun prazoValidoOuVazio(valor: String): Boolean {
    if (valor.isBlank()) return true

    val partes = valor.split("/")
    if (partes.size != 3) return false
    if (partes[0].length != 2 || partes[1].length != 2 || partes[2].length != 4) return false

    val dia = partes[0].toIntOrNull() ?: return false
    val mes = partes[1].toIntOrNull() ?: return false
    val ano = partes[2].toIntOrNull() ?: return false

    if (ano < 2000 || ano > 2100 || mes !in 1..12) return false

    val maxDia = when (mes) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (anoBissexto(ano)) 29 else 28
        else -> return false
    }

    return dia in 1..maxDia
}

private fun anoBissexto(ano: Int): Boolean {
    return (ano % 4 == 0 && ano % 100 != 0) || ano % 400 == 0
}

