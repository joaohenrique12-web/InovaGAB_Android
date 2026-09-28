package br.com.aguiabranca.inovagab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
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
import br.com.aguiabranca.inovagab.data.model.Notificacao
import br.com.aguiabranca.inovagab.ui.theme.InovaColors
import br.com.aguiabranca.inovagab.ui.components.BottomNavOperador
import br.com.aguiabranca.inovagab.ui.components.InovaBottomNav
import br.com.aguiabranca.inovagab.ui.components.liderNavItems
import br.com.aguiabranca.inovagab.ui.components.gestorNavItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import br.com.aguiabranca.inovagab.data.remote.UserSession
import br.com.aguiabranca.inovagab.data.remote.BackendRepository


data class NotificacoesUiState(
    val carregando: Boolean = true,
    val notificacoes: List<Notificacao> = emptyList(),
    val erro: String? = null,
    val perfil: String = "OPERACIONAL"
)

class NotificacoesViewModel(
    private val repository: BackendRepository = BackendRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificacoesUiState())
    val uiState: StateFlow<NotificacoesUiState> = _uiState.asStateFlow()

    fun carregar() {
        val perfilSessao: String = UserSession.perfil
            ?.trim()
            ?.uppercase()
            ?: "OPERACIONAL"

        val uid = repository.uidAtual()

        if (uid == null) {
            _uiState.value = _uiState.value.copy(
                carregando = false,
                notificacoes = emptyList(),
                perfil = perfilSessao
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(carregando = true)
            
            // Busca o perfil do usuário para definir a navegação correta
            val usuarioResult = repository.buscarUsuario(uid)
            val perfil: String = usuarioResult.getOrNull()
                ?.perfil
                ?.toString()
                ?.trim()
                ?.uppercase()
                ?: perfilSessao


            repository.buscarNotificacoes(uid)
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(
                        carregando = false, 
                        notificacoes = list,
                        perfil = perfil
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        carregando = false, 
                        erro = e.message,
                        perfil = perfil
                    )
                }
        }
    }

    fun marcarComoLida(id: String) {
        viewModelScope.launch {
            repository.marcarNotificacaoLida(id)
            _uiState.value = _uiState.value.copy(
                notificacoes = _uiState.value.notificacoes.map {
                    if (it.id == id) it.copy(lida = true) else it
                }
            )
        }
    }

    init { carregar() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacoesScreen(
    onVoltar: () -> Unit,
    onNavigate: (String) -> Unit,
    vm: NotificacoesViewModel = viewModel()
) {
    val uiState by vm.uiState.collectAsState()

    Scaffold(
        containerColor = InovaColors.NavyPrimary,
        topBar = {
            TopAppBar(
                title = { Text("Notificações", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = InovaColors.NavyPrimary)
            )
        },
        bottomBar = {
            // Lógica dinâmica para mostrar o menu correto baseado no perfil (ignora case e espaços)
            val perfilUpper = uiState.perfil.trim().uppercase()
            
            when (perfilUpper) {
                "LIDER", "LIDERANCA" -> {
                    InovaBottomNav(
                        items = liderNavItems,
                        currentRoute = "notificacoes",
                        onItemClick = onNavigate
                    )
                }
                "GESTOR" -> {
                    InovaBottomNav(
                        items = gestorNavItems,
                        currentRoute = "notificacoes",
                        onItemClick = onNavigate
                    )
                }
                else -> {
                    BottomNavOperador(
                        rotaAtual = "notificacoes",
                        onNavigate = onNavigate
                    )
                }
            }
        }
    ) { padding ->
        if (uiState.carregando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = InovaColors.BlueAccent)
            }
        } else if (uiState.notificacoes.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.Notifications,
                        null,
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Nenhuma notificação por aqui", color = Color.White.copy(alpha = 0.5f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.notificacoes) { notif ->
                    CardNotificacao(notif) { vm.marcarComoLida(notif.id) }
                }
            }
        }
    }
}

@Composable
fun CardNotificacao(notif: Notificacao, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (notif.lida) Color.White.copy(alpha = 0.03f) else Color.White.copy(alpha = 0.07f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (notif.lida) Color.Transparent else InovaColors.BlueAccent, CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(notif.mensagem, color = Color.White, fontSize = 14.sp)
                if (notif.criadaEm.isNotEmpty()) {
                    Text(
                        notif.criadaEm,
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
