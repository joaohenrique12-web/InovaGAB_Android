package br.com.aguiabranca.inovagab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.aguiabranca.inovagab.data.model.Ideia
import br.com.aguiabranca.inovagab.data.model.Projeto
import br.com.aguiabranca.inovagab.data.model.StatusProjeto
import br.com.aguiabranca.inovagab.data.remote.BackendRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProjetoUiState(
    val carregando: Boolean = false,
    val projeto: Projeto? = null,
    val ideiasRelacionada: Ideia? = null,
    val erro: String? = null,
    val atualizado: Boolean = false
)

class ProjetoViewModel(
    private val repository: BackendRepository = BackendRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjetoUiState())
    val uiState: StateFlow<ProjetoUiState> = _uiState.asStateFlow()

    fun carregarProjeto(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erro = null) }
            repository.observarProjeto(id)
                .catch { e -> _uiState.update { it.copy(erro = e.message, carregando = false) } }
                .collect { proj ->
                    _uiState.update { it.copy(projeto = proj, carregando = false) }

                    // Se o projeto tem uma ideia de origem, vamos buscá-la
                    if (proj != null && proj.ideiaOrigemId.isNotEmpty()) {
                        repository.observarIdeia(proj.ideiaOrigemId).collect { ideia ->
                            _uiState.update { it.copy(ideiasRelacionada = ideia) }
                        }
                    }
                }
        }
    }

    fun atualizarProgresso(projetoId: String, progresso: Int, economia: Double, status: StatusProjeto) {
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true) }
            repository.atualizarProjeto(projetoId, progresso, economia, status)
                .onSuccess { _uiState.update { it.copy(carregando = false, atualizado = true) } }
                .onFailure { e -> _uiState.update { it.copy(carregando = false, erro = e.message) } }
        }
    }

    fun resetarEstado() {
        _uiState.update { it.copy(atualizado = false, erro = null) }
    }
}