package br.com.aguiabranca.inovagab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.aguiabranca.inovagab.data.model.Ideia
import br.com.aguiabranca.inovagab.data.model.StatusIdeia
import br.com.aguiabranca.inovagab.data.remote.BackendRepository
import br.com.aguiabranca.inovagab.data.remote.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.firstOrNull
import kotlin.collections.orEmpty

data class PerfilUiState(
    val carregando: Boolean = false,
    val nome: String = "",
    val email: String = "",
    val perfil: String = "",
    val divisao: String = "",
    val pontos: Int = 0,
    val posicao: Int = 0,
    val totalIdeias: Int = 0,
    val aprovadas: Int = 0,
    val ideias: List<Ideia> = emptyList(),
    val fotoUrl: String = "",
    val logoutFeito: Boolean = false,
    val erro: String? = null
)

class PerfilViewModel : ViewModel() {

    private val repository = BackendRepository()

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    fun carregar() {
        val uid = repository.uidAtual() ?: run {
            _uiState.update { it.copy(carregando = false, erro = "Usuário não autenticado.") }
            return
        }

        if (uid == null) {
            _uiState.update {
                it.copy(
                    carregando = false,
                    nome = UserSession.nome.orEmpty(),
                    email = UserSession.email.orEmpty(),
                    perfil = UserSession.perfil.orEmpty(),
                    erro = null
                )
            }
            return
        }

        // Observa ideias em tempo real
        viewModelScope.launch {
            repository.observarIdeiasDoUsuario(uid).collect { ideias ->
                _uiState.update {
                    it.copy(
                        ideias      = ideias,
                        totalIdeias = ideias.size,
                        aprovadas   = ideias.count { i ->
                            i.status == StatusIdeia.APROVADA.name
                        }
                    )
                }
            }
        }

        viewModelScope.launch {
            val ranking = repository.buscarRanking().getOrNull().orEmpty()

            repository.buscarUsuario(uid)
                .onSuccess { usuario ->
                    val itemRanking = ranking.firstOrNull { item ->
                        item.usuarioId == uid ||
                                item.usuarioNome.equals(usuario.nome, ignoreCase = true)
                    }

                    _uiState.update {
                        it.copy(
                            nome = usuario.nome,
                            email = usuario.email,
                            perfil = usuario.perfil,
                            divisao = usuario.divisao.ifBlank { "Grupo Águia Branca" },
                            pontos = itemRanking?.pontos ?: usuario.pontos,
                            posicao = itemRanking?.posicao ?: usuario.posicaoRanking,
                            fotoUrl = usuario.fotoUrl
                        )
                    }
                }
        }
    }

    fun resetarEstado() {
        _uiState.update { it.copy(erro = null) }
    }

    fun logout() {
        repository.logout()
        UserSession.clear()
        _uiState.update { it.copy(logoutFeito = true) }
    }
}
