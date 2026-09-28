package br.com.aguiabranca.inovagab.data.remote

data class LoginResponse(
    val token: String,
    val usuario: UsuarioResponse
)

data class UsuarioResponse(
    val id: String?,
    val nome: String?,
    val email: String?,
    val role: String?,
    val ativo: Boolean? = null,
    val pontos: Int? = null
)
