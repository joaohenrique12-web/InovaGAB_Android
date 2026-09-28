package br.com.aguiabranca.inovagab.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.aguiabranca.inovagab.data.model.CategoriaIdeia
import br.com.aguiabranca.inovagab.data.model.Complexidade
import br.com.aguiabranca.inovagab.data.model.Ideia
import br.com.aguiabranca.inovagab.data.model.StatusIdeia
import br.com.aguiabranca.inovagab.data.remote.BackendRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException
import kotlin.collections.plus

data class IdeiaUiState(
    val carregando: Boolean = false,
    val carregandoDetalhe: Boolean = false,
    val enviada: Boolean = false,
    val ideiaDetalhe: Ideia? = null,
    val erro: String? = null,
    val excluida: Boolean = false,
    val atualizada: Boolean = false,
    val sugestaoPreviaIA: String = "",
    val carregandoIA: Boolean = false
)

class IdeiaViewModel : ViewModel() {

    private val repository = BackendRepository()

    private val _uiState = MutableStateFlow(IdeiaUiState())
    val uiState: StateFlow<IdeiaUiState> = _uiState.asStateFlow()
    private var ultimoTituloAnalisado = ""
    private var ultimaDescricaoAnalisada = ""

    fun gerarSugestaoPrevia(titulo: String, descricao: String) {
        val tituloLimpo = titulo.trim()
        val descricaoLimpa = descricao.trim()

        if (tituloLimpo.isBlank() || descricaoLimpa.length < 30) {
            _uiState.update {
                it.copy(sugestaoPreviaIA = "Escreva um título e uma descrição com pelo menos 30 caracteres para usar a IA.")
            }
            return
        }

        if (_uiState.value.carregandoIA) return

        if (tituloLimpo == ultimoTituloAnalisado && descricaoLimpa == ultimaDescricaoAnalisada) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    carregandoIA = true,
                    sugestaoPreviaIA = "Analisando sua ideia..."
                )
            }

            repository.gerarSugestaoIA(tituloLimpo, descricaoLimpa)
                .onSuccess { sugestao ->
                    ultimoTituloAnalisado = tituloLimpo
                    ultimaDescricaoAnalisada = descricaoLimpa

                    _uiState.update {
                        it.copy(
                            carregandoIA = false,
                            sugestaoPreviaIA = sugestao
                        )
                    }
                }
                .onFailure { e ->
                    Log.e("IA_BACKEND", "Erro ao gerar sugestão IA", e)

                    val mensagem = when (e) {
                        is SocketTimeoutException -> "A IA demorou para responder. Tente novamente em alguns segundos."
                        is HttpException -> "Erro da IA no backend: HTTP ${e.code()}"
                        else -> "Erro ao chamar IA: ${e.message}"
                    }

                    _uiState.update {
                        it.copy(
                            carregandoIA = false,
                            sugestaoPreviaIA = mensagem
                        )
                    }
                }
        }
    }

    fun criarIdeia(
        titulo: String,
        descricao: String,
        categoria: CategoriaIdeia?,
        localidade: String,
        impactoEstimado: Double = 0.0
    ) {
        if (titulo.isBlank() || descricao.isBlank() || categoria == null) {
            _uiState.update {
                it.copy(erro = "Preencha todos os campos obrigatórios.")
            }
            return
        }

        _uiState.update {
            it.copy(carregando = true, erro = null)
        }

        viewModelScope.launch {
            val uid: String = repository.uidAtual() ?: run {
                _uiState.update {
                    it.copy(
                        carregando = false,
                        erro = "Usuário não autenticado."
                    )
                }
                return@launch
            }

            try {
                val nomeAutor: String = repository.buscarUsuario(uid)
                    .getOrNull()
                    ?.nome
                    ?.takeIf { it.isNotBlank() }
                    ?: "Usuário"

                val novaIdeia = Ideia(
                    id = "",
                    titulo = titulo,
                    descricao = descricao,
                    autorId = uid,
                    autorNome = nomeAutor,
                    status = StatusIdeia.EM_ANALISE.name,
                    categoria = categoria.name,
                    complexidade = Complexidade.MEDIA.name,
                    localidade = localidade,
                    impactoEstimado = impactoEstimado,
                    votos = 0,
                    percentualAprovacao = 0,
                    sugestaoIA = _uiState.value.sugestaoPreviaIA
                )

                repository.criarIdeia(novaIdeia)
                    .onSuccess {
                        _uiState.update {
                            it.copy(carregando = false, enviada = true)
                        }
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                carregando = false,
                                erro = "Erro ao enviar ideia: ${e.message}"
                            )
                        }
                    }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        carregando = false,
                        erro = "Erro ao enviar ideia: ${e.message}"
                    )
                }
            }
        }
    }

    fun carregarDetalhe(ideiaId: String) {
        // Observa a ideia em tempo real
        viewModelScope.launch {
            _uiState.update { it.copy(carregandoDetalhe = true) }
            repository.observarIdeia(ideiaId).collect { ideia ->
                _uiState.update { it.copy(ideiaDetalhe = ideia, carregandoDetalhe = false) }
            }
        }
    }

    fun deletarIdeia(ideiaId: String) {
        _uiState.update { it.copy(carregando = true) }
        viewModelScope.launch {
            repository.deletarIdeia(ideiaId)
                .onSuccess {
                    _uiState.update { it.copy(carregando = false, excluida = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(carregando = false, erro = e.message) }
                }
        }
    }

    fun editarIdeia(ideiaId: String, titulo: String, descricao: String, categoria: String, localidade: String, impactoEstimado: Double = 0.0) {
        _uiState.update { it.copy(carregando = true) }
        viewModelScope.launch {
            repository.editarIdeiaComImpacto(ideiaId, titulo, descricao, categoria, localidade, impactoEstimado)
                .onSuccess {
                    _uiState.update { it.copy(carregando = false, atualizada = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(carregando = false, erro = e.message) }
                }
        }
    }

    fun resetarEstado() {
        _uiState.update { it.copy(enviada = false, erro = null, excluida = false, atualizada = false, sugestaoPreviaIA = "", carregandoIA = false) }
    }
}
