package br.com.aguiabranca.inovagab.ui.diretoria

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.aguiabranca.inovagab.data.remote.BackendRepository
import kotlinx.coroutines.flow.catch
import br.com.aguiabranca.inovagab.data.model.Ideia
import br.com.aguiabranca.inovagab.data.model.StatusIdeia
import br.com.aguiabranca.inovagab.ui.components.CardIdeia
import br.com.aguiabranca.inovagab.ui.components.InovaTopBar
import br.com.aguiabranca.inovagab.ui.theme.InovaColors

@Composable
fun SelecaoIdeiaProjetoScreen(
    onVoltar: () -> Unit,
    onIdeiaSelecionada: (String) -> Unit
) {
    val repository = remember { BackendRepository() }
    var ideias by remember { mutableStateOf<List<Ideia>>(emptyList()) }
    var carregando by remember { mutableStateOf(true) }
    var erro by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        repository.buscarTodasIdeias()
            .onSuccess { todas ->
                ideias = todas.filter { it.status == StatusIdeia.APROVADA.name }
                erro = null
            }
            .onFailure { ex ->
                erro = "Erro ao carregar ideias: ${ex.message}"
            }
        carregando = false
    }

    Scaffold(
        topBar = { InovaTopBar(title = "Escolher Ideia para Projeto", onBack = onVoltar) },
        containerColor = InovaColors.NavyPrimary
    ) { padding ->
        if (carregando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = InovaColors.BlueAccent)
            }
        } else if (ideias.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("Nenhuma ideia aprovada aguardando projeto.", color = Color.White.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ideias) { ideia ->
                    CardIdeia(ideia = ideia, onClick = { onIdeiaSelecionada(ideia.id) })
                }
            }
        }
    }
}
