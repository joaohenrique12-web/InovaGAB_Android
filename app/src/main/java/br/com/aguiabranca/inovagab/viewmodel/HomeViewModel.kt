import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.aguiabranca.inovagab.data.model.Ideia
import br.com.aguiabranca.inovagab.data.model.OrientacaoEstrategica
import br.com.aguiabranca.inovagab.data.model.StatusIdeia
import br.com.aguiabranca.inovagab.data.remote.BackendRepository
import br.com.aguiabranca.inovagab.data.remote.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.firstOrNull
import kotlin.collections.orEmpty

data class HomeUiState(
    val carregando: Boolean = false,
    val nomeUsuario: String = "",
    val divisao: String = "",
    val pontos: Int = 0,
    val posicao: Int = 0,
    val ideias: List<Ideia> = emptyList(),
    val orientacoes: List<OrientacaoEstrategica> = emptyList(),
    val totalIdeias: Int = 0,
    val aprovadas: Int = 0,
    val projetos: Int = 0,
    val erro: String? = null
)

class HomeViewModel : ViewModel() {

    private val repository = BackendRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun carregarDados() {
        val uid = repository.uidAtual()
            ?: run {
                _uiState.update { it.copy(carregando = false, erro = "Usuário não autenticado.") }
                return
            }



        // Observa ideias em tempo real
        viewModelScope.launch {
            repository.observarIdeiasDoUsuario(uid)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            carregando = false,
                            erro = "Erro ao carregar ideias: ${e.message}"
                        )
                    }
                }.collect { ideias ->
                    _uiState.update {
                        it.copy(
                            ideias      = ideias,
                            totalIdeias = ideias.size,
                            aprovadas   = ideias.count { i -> i.status == StatusIdeia.APROVADA.name },
                            projetos    = ideias.count { i ->
                                i.status == StatusIdeia.APROVADA.name
                            }
                        )
                    }
                }
        }

        viewModelScope.launch {
            repository.buscarOrientacoes()
                .onSuccess { orientacoes ->
                    _uiState.update {
                        it.copy(orientacoes = orientacoes)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(erro = "Erro ao carregar orientações: ${e.message}")
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
                            nomeUsuario = usuario.nome.ifBlank {
                                UserSession.nome
                                    ?: UserSession.email?.substringBefore("@")
                                    ?: "Usuário"
                            },                            divisao = usuario.divisao.ifBlank { "Grupo Águia Branca" },
                            pontos = itemRanking?.pontos ?: usuario.pontos,
                            posicao = itemRanking?.posicao ?: usuario.posicaoRanking
                        )
                    }
                }
        }
    }
}