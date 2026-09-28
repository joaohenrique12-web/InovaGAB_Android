package br.com.aguiabranca.inovagab.ui.gestor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.com.aguiabranca.inovagab.data.model.StatusIdeia
import br.com.aguiabranca.inovagab.data.model.TipoAnexo
import br.com.aguiabranca.inovagab.ui.components.InovaBottomNav
import br.com.aguiabranca.inovagab.ui.components.gestorNavItems
import br.com.aguiabranca.inovagab.ui.components.CardSugestaoIA
import br.com.aguiabranca.inovagab.ui.theme.InovaColors
import br.com.aguiabranca.inovagab.viewmodel.GestorViewModel
import coil.compose.AsyncImage
import org.json.JSONObject
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvaliarIdeiaScreen(
    navController: NavController,
    ideiaId: String,
    viewModel: GestorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var feedbackGestor by remember { mutableStateOf("") }

    LaunchedEffect(ideiaId) {
        viewModel.carregarIdeia(ideiaId)
    }

    LaunchedEffect(uiState.acaoConcluida) {
        if (uiState.acaoConcluida) {
            viewModel.resetarEstado()
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = InovaColors.NavyPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Avaliar ideia",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = InovaColors.NavyPrimary)
            )
        },
        bottomBar = {
            InovaBottomNav(
                items = gestorNavItems,
                currentRoute = "ideias_gestor",
                onItemClick = { route -> navController.navigate(route) }
            )
        }
    ) { padding ->
        if (uiState.carregando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = InovaColors.BlueAccent)
            }
        } else if (uiState.ideiaSelecionada == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Ideia não encontrada", color = Color.White)
            }
        } else {
            val ideia = uiState.ideiaSelecionada!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(InovaColors.NavyPrimary)
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Cabeçalho autor
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(InovaColors.GreenSuccess.copy(alpha = 0.2f))
                            .border(1.dp, InovaColors.GreenSuccess, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ideia.autorNome.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = ideia.titulo, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = "${ideia.autorNome} · ${ideia.categoria}",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }

                // Descrição
                Text(
                    text = ideia.descricao,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                // --- Seção de Anexos para o Gestor ---
                if (ideia.anexos.isNotEmpty()) {
                    Text(
                        text = "ANEXOS DO COLABORADOR",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(ideia.anexos) { anexo ->
                            if (anexo.tipo == TipoAnexo.FOTO.name) {
                                AsyncImage(
                                    model = anexo.url,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Card(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clickable { /* Play Audio logic */ },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Outlined.PlayCircle, null, tint = Color.White, modifier = Modifier.size(36.dp))
                                        Spacer(Modifier.height(4.dp))
                                        Text("Áudio", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                    Divider(color = Color.White.copy(alpha = 0.05f))
                }

                Button(
                    onClick = { viewModel.analisarIdeiaComIa(ideia.id) },
                    enabled = !uiState.analisandoIa,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = InovaColors.BlueAccent
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.analisandoIa) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Analisando ideia...")
                    } else {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Analisar ideia com IA")
                    }
                }

                if (uiState.analisandoIa || ideia.sugestaoIA.isNotBlank()) {
                    CardSugestaoIA(
                        sugestao = if (uiState.analisandoIa) {
                            "Processando análise da ideia com IA..."
                        } else {
                            formatarAnaliseIa(ideia.sugestaoIA)
                        }
                    )
                }

                // Votos e Métricas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        valor = "${ideia.percentualAprovacao}%",
                        label = "Aprovação",
                        valorColor = InovaColors.GreenSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        valor = if (ideia.impactoEstimado > 0) "R$${formatarMoeda(ideia.impactoEstimado)}" else "Pendente.",
                        label = "Impacto",
                        valorColor = InovaColors.BlueAccent,
                        modifier = Modifier.weight(1f)
                    )
                }

                Divider(color = Color.White.copy(alpha = 0.1f))

                // Campo de Feedback do Gestor
                Text(
                    text = "Feedback da Avaliação",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                OutlinedTextField(
                    value = feedbackGestor,
                    onValueChange = { feedbackGestor = it },
                    placeholder = { Text("Explique sua decisão para o colaborador...", color = Color.White.copy(alpha = 0.3f)) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = InovaColors.BlueAccent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Botões de ação
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ActionButton(
                            icon = Icons.Outlined.CheckCircle,
                            label = "Aprovar",
                            color = InovaColors.GreenSuccess,
                            modifier = Modifier.weight(1f),
                            onClick = { 
                                val msg = feedbackGestor.ifBlank { "Ideia aprovada pelo gestor com base na análise técnica." }
                                viewModel.avaliarIdeia(ideia.id, StatusIdeia.APROVADA, msg) 
                            }
                        )
                        ActionButton(
                            icon = Icons.Outlined.Bolt,
                            label = "Priorizar",
                            color = InovaColors.AmberWarning,
                            modifier = Modifier.weight(1f),
                            onClick = { 
                                val msg = feedbackGestor.ifBlank { "Ideia aprovada como URGENTE para implementação imediata." }
                                viewModel.avaliarIdeia(ideia.id, StatusIdeia.APROVADA, msg, urgente = true) 
                            }
                        )
                    }
                    
                    Button(
                        onClick = { 
                            val msg = feedbackGestor.ifBlank { "A ideia não será aplicada neste momento." }
                            viewModel.avaliarIdeia(ideia.id, StatusIdeia.REPROVADA, msg) 
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = InovaColors.RedDanger.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, InovaColors.RedDanger),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Cancel, null, tint = InovaColors.RedDanger)
                        Spacer(Modifier.width(8.dp))
                        Text("Recusar Ideia", color = InovaColors.RedDanger, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun MetricCard(valor: String, label: String, valorColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = valor, color = valorColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(text = label, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(1.5.dp, color.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = label, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

private fun formatarAnaliseIa(texto: String): String {
    val textoLimpo = texto.trim()

    if (!textoLimpo.startsWith("{")) {
        return texto
    }

    return runCatching {
        val json = JSONObject(textoLimpo)

        val score = if (json.has("score")) json.optInt("score").toString() else "-"
        val inovacao = if (json.has("inovacao")) json.optInt("inovacao").toString() else "-"
        val viabilidade = if (json.has("viabilidade")) json.optInt("viabilidade").toString() else "-"
        val impacto = if (json.has("impacto")) json.optInt("impacto").toString() else "-"
        val alinhamento = if (json.has("alinhamentoEstrategico")) {
            json.optInt("alinhamentoEstrategico").toString()
        } else {
            "-"
        }

        val justificativa = json.optString("justificativa", "")
        val riscosJson = json.optJSONArray("riscos")

        val riscos = if (riscosJson != null && riscosJson.length() > 0) {
            (0 until riscosJson.length()).joinToString("\n") { index ->
                "- ${riscosJson.optString(index)}"
            }
        } else {
            "Nenhum risco informado."
        }

        """
        Score geral: $score / 100

        Inovação: $inovacao / 100
        Viabilidade: $viabilidade / 100
        Impacto: $impacto / 100
        Alinhamento estratégico: $alinhamento / 100

        Justificativa:
        $justificativa

        Riscos:
        $riscos
        """.trimIndent()
    }.getOrElse {
        texto
    }
}

private fun formatarMoeda(valor: Double): String {
    return when {
        valor >= 1_000_000 -> "${(valor / 1_000_000).toInt()}M"
        valor >= 1_000     -> "${(valor / 1_000).toInt()}K"
        else               -> valor.toInt().toString()
    }
}
