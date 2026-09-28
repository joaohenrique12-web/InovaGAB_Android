package br.com.aguiabranca.inovagab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.aguiabranca.inovagab.data.model.DashboardExecutivo
import br.com.aguiabranca.inovagab.data.remote.BackendRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiretoriaUiState(
    val carregando: Boolean = false,
    val dashboard: DashboardExecutivo? = null,
    val erro: String? = null
)

class DiretoriaViewModel : ViewModel() {

    private val repository = BackendRepository()

    private val _uiState = MutableStateFlow(DiretoriaUiState())
    val uiState: StateFlow<DiretoriaUiState> = _uiState.asStateFlow()

    fun carregarDashboard() {
        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    carregando = true,
                    erro = null
                )
            }

            repository.buscarDadosDashboard()
                .onSuccess { dashboard ->
                    _uiState.update {
                        it.copy(
                            carregando = false,
                            dashboard = dashboard
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            carregando = false,
                            erro = "Erro ao carregar dashboard: ${e.message}"
                        )
                    }
                }
        }
    }
}