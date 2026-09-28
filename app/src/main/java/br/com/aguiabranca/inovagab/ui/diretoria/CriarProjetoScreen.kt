package br.com.aguiabranca.inovagab.ui.diretoria

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.aguiabranca.inovagab.data.model.Projeto
import br.com.aguiabranca.inovagab.data.model.StatusProjeto
import br.com.aguiabranca.inovagab.ui.components.InovaTopBar
import br.com.aguiabranca.inovagab.ui.theme.InovaColors
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import br.com.aguiabranca.inovagab.data.remote.BackendRepository

@Composable
fun CriarProjetoScreen(
    ideiaId: String,
    onSucesso: () -> Unit,
    onVoltar: () -> Unit
) {
    val repository = remember { BackendRepository() }
    val scope = rememberCoroutineScope()
    
    var titulo by remember { mutableStateOf("") }
    var responsavel by remember { mutableStateOf("") }
    var prazo by remember { mutableStateOf("") }
    var divisao by remember { mutableStateOf("Operacional") }
    var impacto by remember { mutableStateOf(0.0) }
    var carregando by remember { mutableStateOf(true) }

    LaunchedEffect(ideiaId) {
        repository.observarIdeia(ideiaId).firstOrNull()?.let { ideia ->
            titulo = ideia.titulo
            impacto = ideia.impactoEstimado
        }
        carregando = false
    }

    Scaffold(
        topBar = { InovaTopBar(title = "Formalizar Projeto", onBack = onVoltar) },
        containerColor = InovaColors.NavyPrimary
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Transformar ideia em projeto executivo", color = Color.White, style = MaterialTheme.typography.titleMedium)
            
            OutlinedTextField(
                value = titulo, onValueChange = { titulo = it },
                label = { Text("Título do Projeto") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedTextColor = Color.White, 
                    focusedTextColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    focusedLabelColor = InovaColors.BlueAccent
                )
            )
            
            OutlinedTextField(
                value = responsavel, onValueChange = { responsavel = it },
                label = { Text("Responsável (Gestor)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ex: Carlos Mendes") },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedTextColor = Color.White, 
                    focusedTextColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                )
            )
            
            OutlinedTextField(
                value = prazo, onValueChange = { prazo = it },
                label = { Text("Prazo Estimado") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ex: 31/12/2025") },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedTextColor = Color.White, 
                    focusedTextColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                )
            )

            Spacer(Modifier.weight(1f))
            
            Button(
                onClick = {
                    scope.launch {
                        val novoProjeto = Projeto(
                            titulo = titulo,
                            ideiaOrigemId = ideiaId,
                            responsavel = responsavel,
                            status = StatusProjeto.PLANEJADO.name,
                            prazo = prazo,
                            divisao = divisao,
                            economiaGerada = impacto,
                            progresso = 0
                        )
                        repository.criarProjeto(novoProjeto)
                        onSucesso()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = InovaColors.BlueAccent),
                enabled = responsavel.isNotBlank() && prazo.isNotBlank()
            ) {
                Text("Iniciar Execução do Projeto")
            }
        }
    }
}
