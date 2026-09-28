package br.com.aguiabranca.inovagab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.aguiabranca.inovagab.data.model.*
import br.com.aguiabranca.inovagab.data.remote.AnaliseIaResponse
import br.com.aguiabranca.inovagab.data.remote.BackendRepository
import br.com.aguiabranca.inovagab.data.remote.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GestorUiState(
    val carregando: Boolean = false,
    val nomeGestor: String = "",
    val iniciais: String = "",
    val dashboard: DashboardExecutivo? = null,
    val ideiasParaAvaliar: List<Ideia> = emptyList(),
    val projetos: List<Projeto> = emptyList(),
    val ideiaSelecionada: Ideia? = null,
    val analisandoIa: Boolean = false,
    val analiseIa: AnaliseIaResponse? = null,
    val acaoConcluida: Boolean = false,
    val erro: String? = null
)

class GestorViewModel(
    private val repository: BackendRepository =
        BackendRepository()
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(GestorUiState())

    val uiState: StateFlow<GestorUiState> =
        _uiState.asStateFlow()

    fun carregarDados() {

        val uid = repository.uidAtual()

        if (uid == null) {

            _uiState.update {
                it.copy(
                    carregando = false,
                    nomeGestor =
                        UserSession.nome.orEmpty(),
                    iniciais =
                        UserSession.nome
                            ?.split(" ")
                            ?.filter { parte ->
                                parte.isNotBlank()
                            }
                            ?.take(2)
                            ?.joinToString("") { parte ->
                                parte.take(1)
                            }
                            ?.uppercase()
                            .orEmpty(),
                    erro = null
                )
            }

            return
        }

        _uiState.update {
            it.copy(
                carregando = true,
                erro = null,
                nomeGestor =
                    UserSession.nome
                        ?: it.nomeGestor,
                iniciais =
                    UserSession.nome
                        ?.split(" ")
                        ?.filter { parte ->
                            parte.isNotBlank()
                        }
                        ?.take(2)
                        ?.joinToString("") { parte ->
                            parte.take(1)
                        }
                        ?.uppercase()
                        ?: it.iniciais
            )
        }

        // Ideias aguardando avaliação
        viewModelScope.launch {

            repository.observarIdeiasParaAvaliar()
                .catch { e ->

                    _uiState.update {
                        it.copy(
                            erro =
                                "Erro ao observar ideias: ${e.message}",
                            carregando = false
                        )
                    }
                }
                .collect { lista ->

                    _uiState.update {
                        it.copy(
                            ideiasParaAvaliar = lista,
                            carregando = false
                        )
                    }
                }
        }

        // Projetos
        viewModelScope.launch {

            repository.observarProjetos()
                .catch { e ->

                    _uiState.update {
                        it.copy(
                            erro =
                                "Erro ao observar projetos: ${e.message}"
                        )
                    }
                }
                .collect { lista ->

                    _uiState.update {
                        it.copy(
                            projetos = lista
                        )
                    }
                }
        }

        viewModelScope.launch {

            repository.buscarUsuario(uid)
                .onSuccess { user ->

                    val nomeFinal =
                        user.nome.ifBlank {
                            UserSession.nome
                                ?: UserSession.email
                                    ?.substringBefore("@")
                                ?: "Gestor"
                        }

                    _uiState.update {
                        it.copy(
                            nomeGestor = nomeFinal,
                            iniciais =
                                nomeFinal
                                    .split(" ")
                                    .filter { parte ->
                                        parte.isNotBlank()
                                    }
                                    .take(2)
                                    .joinToString("") { parte ->
                                        parte.take(1)
                                    }
                                    .uppercase()
                        )
                    }
                }

            repository.buscarDadosDashboard()
                .onSuccess { dashboard ->

                    _uiState.update {
                        it.copy(
                            dashboard = dashboard
                        )
                    }
                }
                .onFailure { e ->

                    _uiState.update {
                        it.copy(
                            erro =
                                "Erro ao carregar dashboard: ${e.message}"
                        )
                    }
                }
        }
    }

    fun carregarIdeia(id: String) {

        viewModelScope.launch {

            repository.observarIdeia(id)
                .catch { e ->

                    _uiState.update {
                        it.copy(
                            erro = e.message
                        )
                    }
                }
                .collect { ideia ->

                    _uiState.update {
                        it.copy(
                            ideiaSelecionada = ideia
                        )
                    }
                }
        }
    }

    fun analisarIdeiaComIa(
        ideiaId: String
    ) {
        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    analisandoIa = true,
                    erro = null
                )
            }

            repository
                .analisarIdeiaComIa(ideiaId)
                .onSuccess { analise ->

                    val textoAnalise =
                        montarTextoAnaliseIa(
                            analise
                        )

                    _uiState.update { estadoAtual ->

                        estadoAtual.copy(
                            analisandoIa = false,
                            analiseIa = analise,
                            ideiaSelecionada =
                                estadoAtual
                                    .ideiaSelecionada
                                    ?.copy(
                                        sugestaoIA =
                                            textoAnalise,
                                        impactoEstimado =
                                            analise.score
                                                .toDouble()
                                    )
                        )
                    }
                }
                .onFailure { e ->

                    _uiState.update {
                        it.copy(
                            analisandoIa = false,
                            erro =
                                "Erro ao analisar ideia com IA: ${e.message}"
                        )
                    }
                }
        }
    }

    private fun montarTextoAnaliseIa(
        analise: AnaliseIaResponse
    ): String {

        val riscos =
            analise.riscos
                .takeIf {
                    it.isNotEmpty()
                }
                ?.joinToString(", ")
                ?: "Nenhum risco informado"

        return """
            Score geral: ${analise.score}/100

            Inovação: ${analise.inovacao}/100
            Viabilidade: ${analise.viabilidade}/100
            Impacto: ${analise.impacto}/100
            Alinhamento estratégico: ${analise.alinhamentoEstrategico}/100

            Justificativa:
            ${analise.justificativa}

            Riscos:
            $riscos
        """.trimIndent()
    }

    fun avaliarIdeia(
        ideiaId: String,
        status: StatusIdeia,
        observacao: String = "",
        urgente: Boolean? = null
    ) {

        _uiState.update {
            it.copy(
                carregando = true
            )
        }

        viewModelScope.launch {

            repository.atualizarStatusIdeia(
                ideiaId,
                status,
                observacao,
                urgente
            )
                .onSuccess {

                    _uiState.update {
                        it.copy(
                            carregando = false,
                            acaoConcluida = true
                        )
                    }
                }
                .onFailure { e ->

                    _uiState.update {
                        it.copy(
                            carregando = false,
                            erro = e.message
                        )
                    }
                }
        }
    }

    fun resetarEstado() {
        _uiState.update {
            it.copy(
                acaoConcluida = false,
                erro = null
            )
        }
    }
}