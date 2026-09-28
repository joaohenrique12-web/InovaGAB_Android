package br.com.aguiabranca.inovagab.data.remote

object UserSession {
    var token: String? = null
        private set

    var usuarioId: String? = null
        private set

    var perfil: String? = null
        private set

    var nome: String? = null
        private set

    var email: String? = null
        private set

    fun save(response: LoginResponse) {
        token = response.token
        usuarioId = response.usuario.id
        perfil = response.usuario.role?.trim()?.uppercase()
        nome = response.usuario.nome
        email = response.usuario.email
    }

    fun clear() {
        token = null
        usuarioId = null
        perfil = null
        nome = null
        email = null
    }
}