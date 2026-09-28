package br.com.aguiabranca.inovagab.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.aguiabranca.inovagab.data.model.PerfilUsuario
import br.com.aguiabranca.inovagab.data.remote.ApiClient
import br.com.aguiabranca.inovagab.data.remote.LoginRequest
import br.com.aguiabranca.inovagab.data.remote.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val carregando: Boolean = false,
    val erro: String? = null,
    val rotaDestino: String? = null
)

class LoginViewModel : ViewModel() {
    //private val repository = BackendRepository()
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, senha: String, perfilSelecionado: PerfilUsuario?) {
        if (email.isBlank() || senha.isBlank() || perfilSelecionado == null) {
            _uiState.value = _uiState.value.copy(erro = "Preencha todos os campos e selecione seu perfil.")
            return
        }

        _uiState.value = LoginUiState(carregando = true)

        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.login(
                    LoginRequest(
                        email = email,
                        senha = senha)
                )
                val perfilUsuario = when (response.usuario.role?.trim()?.uppercase()){
                    "OPERADOR",
                    "OPERACIONAL" -> PerfilUsuario.OPERACIONAL

                    "TERMINAL" -> PerfilUsuario.TERMINAL
                    "GESTOR" -> PerfilUsuario.GESTOR
                    "LOGISTICA" -> PerfilUsuario.LOGISTICA

                    "LIDERANCA",
                    "LIDER" -> PerfilUsuario.LIDER
                    else -> null
                }

                if (perfilUsuario == null) {
                    _uiState.value = LoginUiState(erro = "Perfil de usuário inválido no sistema.")
                    return@launch
                }


                if (perfilUsuario != perfilSelecionado) {
                    _uiState.value = LoginUiState(erro = "Perfil incorreto para este usuário.")
                    return@launch
                }

                val rota = when (perfilUsuario) {
                    PerfilUsuario.OPERACIONAL,
                    PerfilUsuario.TERMINAL,
                    PerfilUsuario.LOGISTICA -> "home_motorista"

                    PerfilUsuario.GESTOR    -> "home_gestor"
                    PerfilUsuario.LIDER     -> "home_diretoria"
                }
                UserSession.save(response)
                _uiState.value = LoginUiState(rotaDestino = rota)

            } catch (e: retrofit2.HttpException) {
                Log.e(
                    "LOGIN_BACKEND",
                    "Erro HTTP: ${e.code()} - ${e.response()?.errorBody()?.string()}",
                    e
                )

                _uiState.value = LoginUiState(
                    erro = when (e.code()) {
                        401 -> "E-mail ou senha inválidos."
                        404 -> "Usuário não encontrado."
                        403 -> "Acesso negado."
                        else -> "Erro do servidor: ${e.code()}"
                    }
                )

            } catch (e: java.io.IOException) {
                Log.e("LOGIN_BACKEND",
                    "Erro de conexão com backend", e)

                _uiState.value = LoginUiState(
                    erro = "Não foi possível conectar ao backend."
                )

            } catch (e: Exception) {
                Log.e("LOGIN_BACKEND", "Erro inesperado no login", e)

                _uiState.value = LoginUiState(
                    erro = "Erro inesperado: ${e.message}"
                )            }
        }
    }

    fun rotaConsumida() {
        _uiState.value = _uiState.value.copy(rotaDestino = null)
    }
}