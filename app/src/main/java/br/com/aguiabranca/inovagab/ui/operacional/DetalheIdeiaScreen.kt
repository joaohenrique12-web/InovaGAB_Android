package br.com.aguiabranca.inovagab.ui.operacional

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.aguiabranca.inovagab.data.model.StatusIdeia
import br.com.aguiabranca.inovagab.ui.theme.InovaColors
import br.com.aguiabranca.inovagab.viewmodel.IdeiaViewModel
import br.com.aguiabranca.inovagab.ui.components.StatusBadge
import br.com.aguiabranca.inovagab.ui.components.CardSugestaoIA
import br.com.aguiabranca.inovagab.data.remote.UserSession
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalheIdeiaScreen(
    ideiaId: String,
    onVoltar: () -> Unit,
    ideiaViewModel: IdeiaViewModel = viewModel()
) {
    val uiState by ideiaViewModel.uiState.collectAsState()
    val uidAtual = UserSession.usuarioId

    var mostrarConfirmacaoExclusao by remember { mutableStateOf(false) }
    var mostrarConfirmacaoEdicao by remember { mutableStateOf(false) }
    var modoEdicao by remember { mutableStateOf(false) }

    var tituloEdit by remember { mutableStateOf("") }
    var descricaoEdit by remember { mutableStateOf("") }
    var impactoEdit by remember { mutableStateOf("") }

    LaunchedEffect(ideiaId) {
        ideiaViewModel.carregarDetalhe(ideiaId)
    }

    LaunchedEffect(uiState.ideiaDetalhe) {
        uiState.ideiaDetalhe?.let {
            tituloEdit = it.titulo
            descricaoEdit = it.descricao
            impactoEdit = it.impactoEstimado.toString()
        }
    }

    LaunchedEffect(uiState.excluida) {
        if (uiState.excluida) {
            onVoltar()
            ideiaViewModel.resetarEstado()
        }
    }

    Scaffold(
        containerColor = InovaColors.NavyPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Text("Detalhe da ideia", color = Color.White, fontWeight = FontWeight.SemiBold)
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
                actions = {
                    val ideia = uiState.ideiaDetalhe
                    if (ideia != null && ideia.autorId == uidAtual && (ideia.status == StatusIdeia.EM_ANALISE.name || ideia.status == StatusIdeia.ENVIADA.name)) {
                        if (!modoEdicao) {
                            IconButton(onClick = { modoEdicao = true }) {
                                Icon(Icons.Outlined.Edit, contentDescription = "Editar", tint = Color.White)
                            }
                            IconButton(onClick = { mostrarConfirmacaoExclusao = true }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Excluir", tint = InovaColors.RedDanger)
                            }
                        } else {
                            IconButton(onClick = { mostrarConfirmacaoEdicao = true }) {
                                Icon(Icons.Outlined.Check, contentDescription = "Salvar", tint = InovaColors.GreenSuccess)
                            }
                            IconButton(onClick = { modoEdicao = false }) {
                                Icon(Icons.Outlined.Close, contentDescription = "Cancelar", tint = Color.White)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = InovaColors.NavyPrimary
                )
            )
        }
    ) { padding ->
        if (uiState.carregandoDetalhe) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = InovaColors.BlueAccent)
            }
            return@Scaffold
        }

        val ideia = uiState.ideiaDetalhe
        if (ideia == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Ideia não encontrada.", color = Color.White.copy(alpha = 0.5f))
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(status = ideia.status)
                    if (!modoEdicao && ideia.impactoEstimado > 0) {
                        Text(
                            text = "+R$ ${formatarMoeda(ideia.impactoEstimado)}/ano",
                            color = InovaColors.GreenSuccess,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (modoEdicao) {
                        OutlinedTextField(
                            value = tituloEdit,
                            onValueChange = { tituloEdit = it },
                            label = { Text("Título da ideia") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = campoCores()
                        )
                        OutlinedTextField(
                            value = impactoEdit,
                            onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) impactoEdit = it },
                            label = { Text("Impacto financeiro estimado (R$)") },
                            modifier = Modifier.fillMaxWidth(),
                            prefix = { Text("R$ ", color = InovaColors.GreenSuccess) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = campoCores()
                        )
                        OutlinedTextField(
                            value = descricaoEdit,
                            onValueChange = { descricaoEdit = it },
                            label = { Text("Descrição detalhada") },
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            colors = campoCores()
                        )
                    } else {
                        Text(text = ideia.titulo, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Por ${ideia.autorNome}", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        Text(text = ideia.descricao, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, lineHeight = 20.sp)
                    }
                }
            }

            if (!modoEdicao && ideia.sugestaoIA.isNotBlank()) {
                item {
                    CardSugestaoIA(sugestao = ideia.sugestaoIA)
                }
            }

            if (!modoEdicao) {
                item {
                    Text(text = "TIMELINE", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                val statusOrdem = listOf(StatusIdeia.ENVIADA,
                    StatusIdeia.EM_ANALISE,
                    StatusIdeia.APROVADA,
                    )
                items(statusOrdem) { etapa ->
                    val ativo = statusOrdemIndex(ideia.status) >= statusOrdemIndex(etapa.name)
                    TimelineItem(etapa = etapa, ativo = ativo, atual = ideia.status == etapa.name)
                }
            }
        }
    }

    if (mostrarConfirmacaoExclusao) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacaoExclusao = false },
            title = { Text("Excluir ideia?") },
            text = { Text("Tem certeza que deseja excluir esta ideia permanentemente?") },
            confirmButton = {
                Button(
                    onClick = { 
                        ideiaViewModel.deletarIdeia(ideiaId)
                        mostrarConfirmacaoExclusao = false 
                    }, 
                    colors = ButtonDefaults.buttonColors(containerColor = InovaColors.RedDanger)
                ) {
                    Text("Excluir", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacaoExclusao = false }) { 
                    Text("Cancelar", color = Color.White) 
                }
            },
            containerColor = InovaColors.NavyPrimary,
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.8f)
        )
    }

    if (mostrarConfirmacaoEdicao) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacaoEdicao = false },
            title = { Text("Salvar alterações?") },
            text = { Text("Tem certeza de fazer alterações? Deseja atualizar as informações da sua ideia?") },
            confirmButton = {
                Button(
                    onClick = { 
                        ideiaViewModel.editarIdeia(
                            ideiaId = ideiaId, 
                            titulo = tituloEdit, 
                            descricao = descricaoEdit, 
                            categoria = uiState.ideiaDetalhe?.categoria ?: "", 
                            localidade = uiState.ideiaDetalhe?.localidade ?: "",
                            impactoEstimado = impactoEdit.toDoubleOrNull() ?: 0.0
                        )
                        modoEdicao = false
                        mostrarConfirmacaoEdicao = false 
                    }, 
                    colors = ButtonDefaults.buttonColors(containerColor = InovaColors.BlueAccent)
                ) {
                    Text("Confirmar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacaoEdicao = false }) { 
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
private fun TimelineItem(etapa: StatusIdeia, ativo: Boolean, atual: Boolean) {
    val corPonto = when { atual -> InovaColors.BlueAccent; ativo -> InovaColors.GreenSuccess; else -> Color.White.copy(alpha = 0.2f) }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(20.dp)) {
            Box(modifier = Modifier.size(12.dp).background(corPonto, CircleShape))
            Box(modifier = Modifier.width(2.dp).height(40.dp).background(if (ativo) corPonto.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f)))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = etapa.name.replace("_", " "), color = if (ativo) Color.White else Color.White.copy(alpha = 0.4f), fontSize = 14.sp, fontWeight = if (atual) FontWeight.Bold else FontWeight.Normal)
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

private fun statusOrdemIndex(status: String) = when(status) { "ENVIADA" -> 0; "EM_ANALISE" -> 1; "APROVADA" -> 2; "EM_EXECUCAO" -> 3; "CONCLUIDA" -> 4; else -> -1 }

@Composable
private fun campoCores() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = InovaColors.BlueAccent,
    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = InovaColors.BlueAccent,
    unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
)
