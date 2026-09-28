package br.com.aguiabranca.inovagab.ui.operacional

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.aguiabranca.inovagab.data.model.CategoriaIdeia
import br.com.aguiabranca.inovagab.ui.components.CardSugestaoIA
import br.com.aguiabranca.inovagab.ui.theme.InovaColors
import br.com.aguiabranca.inovagab.viewmodel.IdeiaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaIdeiaScreen(
    onVoltar: () -> Unit,
    onIdeiaEnviada: () -> Unit = { onVoltar() },
    ideiaViewModel: IdeiaViewModel = viewModel()
) {
    val uiState by ideiaViewModel.uiState.collectAsState()

    var titulo by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var categoriaSelecionada by remember { mutableStateOf<CategoriaIdeia?>(null) }
    var localidade by remember { mutableStateOf("") }
    var impactoEstimado by remember { mutableStateOf("") }

    val podeAnalisarIA =
        titulo.trim().isNotBlank() &&
                descricao.trim().length >= 30 &&
                !uiState.carregandoIA

    // --- Debounce para chamada da IA ---


    LaunchedEffect(uiState.enviada) {
        if (uiState.enviada) {
            onIdeiaEnviada()
            ideiaViewModel.resetarEstado()
        }
    }

    Scaffold(
        containerColor = InovaColors.NavyPrimary,
        topBar = {
            TopAppBar(
                title = { Text("Nova ideia", color = Color.White, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = InovaColors.NavyPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título da ideia") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = campoCores(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("Descreva sua ideia ou problema…") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5,
                colors = campoCores(),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    ideiaViewModel.gerarSugestaoPrevia(
                        titulo = titulo.trim(),
                        descricao = descricao.trim()
                    )
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = podeAnalisarIA,
                colors = ButtonDefaults.buttonColors(
                    containerColor = InovaColors.PurpleAI,
                    disabledContainerColor = Color.White.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.carregandoIA) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analisando ideia...")
                } else {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analisar ideia com IA")
                }
            }

            AnimatedVisibility(
                visible = uiState.sugestaoPreviaIA.isNotBlank() || uiState.carregandoIA,
                enter = fadeIn() + expandVertically()
            ) {
                CardSugestaoIA(
                    sugestao = uiState.sugestaoPreviaIA.ifBlank {
                        "Preencha título e descrição, depois toque em Analisar ideia com IA."
                    }
                )
            }

            Text(
                text = "Categoria",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            CategoriaGrid(selecionada = categoriaSelecionada, onSelecionar = { categoriaSelecionada = it })

            OutlinedTextField(
                value = localidade,
                onValueChange = { localidade = it },
                label = { Text("Onde acontece?") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.LocationOn, null, tint = Color.White.copy(alpha = 0.5f)) },
                colors = campoCores(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = impactoEstimado,
                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) impactoEstimado = it },
                label = { Text("Estimativa de impacto financeiro (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text("R$ ", color = InovaColors.GreenSuccess) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = campoCores(),
                shape = RoundedCornerShape(12.dp)
            )

            if (uiState.erro != null) {
                Text(text = uiState.erro!!, color = InovaColors.RedDanger, fontSize = 13.sp)
            }

            Button(
                onClick = {
                    ideiaViewModel.criarIdeia(
                        titulo = titulo.trim(),
                        descricao = descricao.trim(),
                        categoria = categoriaSelecionada,
                        localidade = localidade.trim(),
                        impactoEstimado = impactoEstimado.toDoubleOrNull() ?: 0.0
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !uiState.carregando,
                colors = ButtonDefaults.buttonColors(containerColor = InovaColors.BlueAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.carregando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("Enviar ideia · +10 pts", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CategoriaGrid(selecionada: CategoriaIdeia?, onSelecionar: (CategoriaIdeia) -> Unit) {
    val categorias = listOf(
        CategoriaIdeia.EMBARQUE to "🚌 Embarque",
        CategoriaIdeia.INCIDENTE to "⚠️ Incidente",
        CategoriaIdeia.BOA_PRATICA to "✅ Boa prática",
        CategoriaIdeia.INFRAESTRUTURA to "🏗️ Infraestrutura",
        CategoriaIdeia.COMBUSTIVEL to "⛽ Combustível",
        CategoriaIdeia.ROTA to "🗺️ Rota",
        CategoriaIdeia.MECANICA to "🔧 Mecânica",
        CategoriaIdeia.CARGA to "📦 Carga"
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        categorias.chunked(4).forEach { linha ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                linha.forEach { (cat, label) ->
                    val sel = selecionada == cat
                    Box(
                        modifier = Modifier.weight(1f).background(if (sel) InovaColors.BlueAccent.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                            .border(1.dp, if (sel) InovaColors.BlueAccent else Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            .clickable { onSelecionar(cat) }.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = if (sel) Color.White else Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun campoCores() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = InovaColors.BlueAccent,
    unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = InovaColors.BlueAccent,
    unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
)
