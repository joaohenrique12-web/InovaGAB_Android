package br.com.aguiabranca.inovagab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.aguiabranca.inovagab.data.model.OrientacaoEstrategica
import br.com.aguiabranca.inovagab.data.remote.BackendRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstrategiaGestorUiState(
    val carregando: Boolean = true,
    val orientacoes: List<OrientacaoEstrategica> = emptyList(),
    val erro: String? = null
)

class EstrategiaGestorViewModel(
    private val repository: BackendRepository = BackendRepository()
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(EstrategiaGestorUiState())

    val uiState: StateFlow<EstrategiaGestorUiState> =
        _uiState.asStateFlow()

    fun carregarOrientacoes() {
        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    carregando = true,
                    erro = null
                )
            }

            repository.buscarOrientacoes()
                .onSuccess { lista ->
                    _uiState.update {
                        it.copy(
                            orientacoes = lista,
                            carregando = false,
                            erro = null
                        )
                    }
                }
                .onFailure { ex ->
                    _uiState.update {
                        it.copy(
                            carregando = false,
                            erro = "Erro ao carregar orientações: ${ex.message}"
                        )
                    }
                }
        }
    }

    init {
        carregarOrientacoes()
    }
}