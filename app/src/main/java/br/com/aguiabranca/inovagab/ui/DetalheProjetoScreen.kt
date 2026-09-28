package br.com.aguiabranca.inovagab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import br.com.aguiabranca.inovagab.data.model.StatusProjeto
import br.com.aguiabranca.inovagab.ui.theme.InovaColors
import br.com.aguiabranca.inovagab.viewmodel.ProjetoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalheProjetoScreen(
    projetoId: String,
    onVoltar: () -> Unit,
    viewModel: ProjetoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(projetoId) {
        viewModel.carregarProjeto(projetoId)
    }

    LaunchedEffect(uiState.atualizado) {
        if (uiState.atualizado) {
            viewModel.resetarEstado()
            viewModel.carregarProjeto(projetoId)
        }
    }

    Scaffold(
        containerColor = InovaColors.NavyPrimary,
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Projeto", color = Color.White, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = InovaColors.NavyPrimary)
            )
        }
    ) { padding ->
        if (uiState.carregando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = InovaColors.BlueAccent)
            }
            return@Scaffold
        }

        val projeto = uiState.projeto
        if (projeto == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Projeto não encontrado", color = Color.White.copy(alpha = 0.5f))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header do Projeto
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = projeto.titulo,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        StatusBadgeProjeto(projeto.statusEnum())
                    }
                    
                    Divider(color = Color.White.copy(alpha = 0.1f))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        InfoItem(label = "Responsável", value = projeto.responsavel)
                        InfoItem(label = "Prazo", value = projeto.prazo)
                    }
                }
            }

            // Progresso
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Progresso da Implantação", color = Color.White, fontWeight = FontWeight.Medium)
                    Text("${projeto.progresso}%", color = InovaColors.GreenSuccess, fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(
                    progress = { projeto.progresso / 100f },
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                    color = InovaColors.GreenSuccess,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }

            // Impacto Financeiro
            // Impacto Financeiro
            Card(
                colors = CardDefaults.cardColors(containerColor = InovaColors.GreenSuccess.copy(alpha = 0.1f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, InovaColors.GreenSuccess.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Retorno Estimado", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        Text(
                            "R$ ${String.format("%,.2f", projeto.retornoEstimado)} /ano",
                            color = InovaColors.GreenSuccess,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "ROI: ${String.format("%.1f", projeto.roi)}%",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                    Icon(Icons.Outlined.TrendingUp, null, tint = InovaColors.GreenSuccess, modifier = Modifier.size(32.dp))
                }
            }
            if (projeto.statusEnum() == StatusProjeto.PLANEJADO) {
                Button(
                    onClick = {
                        viewModel.atualizarProgresso(
                            projetoId = projeto.id,
                            progresso = 50,
                            economia = projeto.economiaGerada,
                            status = StatusProjeto.EM_DESENVOLVIMENTO
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = InovaColors.BlueAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Marcar como em andamento")
                }
            }

            // Ideia de Origem (se houver)
            uiState.ideiasRelacionada?.let { ideia ->
                Text("Ideia que deu origem", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(ideia.titulo, color = Color.White, fontWeight = FontWeight.Medium)
                        Text(ideia.descricao, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, maxLines = 3)
                        Text("Autor: ${ideia.autorNome}", color = InovaColors.BlueAccent, fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatusBadgeProjeto(status: StatusProjeto) {
    val (color, text) = when(status) {
        StatusProjeto.PLANEJADO -> Color.Gray to "Planejado"
        StatusProjeto.EM_DESENVOLVIMENTO -> InovaColors.BlueAccent to "Em Execução"
        StatusProjeto.CONCLUIDO -> InovaColors.GreenSuccess to "Concluído"
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
