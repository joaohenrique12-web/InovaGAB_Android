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

data class EstrategiaLiderUiState(
    val carregando: Boolean = true,
    val orientacoes: List<OrientacaoEstrategica> = emptyList(),
    val erro: String? = null,
    val mensagemSucesso: String? = null
)

class EstrategiaLiderViewModel(
    private val repository: BackendRepository = BackendRepository()
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(EstrategiaLiderUiState())

    val uiState: StateFlow<EstrategiaLiderUiState> =
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

    fun criarOrientacao(
        orientacao: OrientacaoEstrategica
    ) {
        viewModelScope.launch {

            repository.criarOrientacao(orientacao)
                .onSuccess {

                    _uiState.update {
                        it.copy(
                            mensagemSucesso =
                                "Orientação criada com sucesso.",
                            erro = null
                        )
                    }

                    carregarOrientacoes()
                }
                .onFailure { ex ->
                    _uiState.update {
                        it.copy(
                            erro = "Erro ao criar: ${ex.message}"
                        )
                    }
                }
        }
    }

    fun atualizarOrientacao(
        orientacao: OrientacaoEstrategica
    ) {
        viewModelScope.launch {

            repository.atualizarOrientacao(orientacao)
                .onSuccess {

                    _uiState.update {
                        it.copy(
                            mensagemSucesso =
                                "Orientação atualizada com sucesso.",
                            erro = null
                        )
                    }

                    carregarOrientacoes()
                }
                .onFailure { ex ->
                    _uiState.update {
                        it.copy(
                            erro = "Erro ao atualizar: ${ex.message}"
                        )
                    }
                }
        }
    }

    fun deletarOrientacao(id: String) {
        viewModelScope.launch {

            repository.deletarOrientacao(id)
                .onSuccess {

                    _uiState.update { state ->
                        state.copy(
                            orientacoes =
                                state.orientacoes.filterNot {
                                    it.id == id
                                },
                            mensagemSucesso =
                                "Orientação desativada com sucesso.",
                            erro = null
                        )
                    }
                }
                .onFailure { ex ->
                    _uiState.update {
                        it.copy(
                            erro =
                                "Erro ao desativar orientação: ${ex.message}"
                        )
                    }
                }
        }
    }

    fun limparMensagemSucesso() {
        _uiState.update {
            it.copy(
                mensagemSucesso = null
            )
        }
    }

    init {
        carregarOrientacoes()
    }
}