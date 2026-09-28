package br.com.aguiabranca.inovagab.data.remote

import br.com.aguiabranca.inovagab.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BackendRepository(
    private val api: ApiService = ApiClient.apiService
) {
    fun uidAtual(): String? = UserSession.usuarioId

    fun logout() {
        UserSession.clear()
    }

    suspend fun buscarUsuario(uid: String = UserSession.usuarioId.orEmpty()): Result<Usuario> = runCatching {
        api.usuarioAtual().toAppUsuario()
    }

    suspend fun buscarDadosDashboard(): Result<DashboardExecutivo> = runCatching {
        api.dashboardResumo().toAppDashboard()
    }

    suspend fun buscarOrientacoes(): Result<List<OrientacaoEstrategica>> = runCatching {
        api.listarOrientacoes(ativa = true)
            .filter { it.ativa == true }
            .map { it.toAppOrientacao() }
    }

    fun observarOrientacoes(): Flow<List<OrientacaoEstrategica>> = flow {
        emit(buscarOrientacoes().getOrThrow())
    }

    suspend fun criarOrientacao(orientacao: OrientacaoEstrategica): Result<String> = runCatching {
        api.criarOrientacao(orientacao.toBackendRequest()).id.orEmpty()
    }

    suspend fun atualizarOrientacao(orientacao: OrientacaoEstrategica): Result<Unit> = runCatching {
        api.atualizarOrientacao(orientacao.id, orientacao.toBackendRequest())
        Unit
    }

    suspend fun deletarOrientacao(id: String): Result<Unit> = runCatching {
        api.deletarOrientacao(id)
        Unit
    }

    fun observarIdeiasParaAvaliar(): Flow<List<Ideia>> = flow {
        val submetidas = api.listarIdeias(status = "SUBMETIDA").map { it.toAppIdeia() }
        val emAnalise = api.listarIdeias(status = "EM_ANALISE").map { it.toAppIdeia() }
        emit((submetidas + emAnalise).distinctBy { it.id })
    }

    fun observarHistoricoGestor(): Flow<List<Ideia>> = flow {
        val aprovadas = api.listarIdeias(status = "APROVADA").map { it.toAppIdeia() }
        val rejeitadas = api.listarIdeias(status = "REJEITADA").map { it.toAppIdeia() }
        emit((aprovadas + rejeitadas).distinctBy { it.id })
    }

    suspend fun buscarTodasIdeias(): Result<List<Ideia>> = runCatching {
        api.listarIdeias().map { it.toAppIdeia() }
    }

    fun observarIdeia(ideiaId: String): Flow<Ideia?> = flow {
        emit(api.buscarIdeia(ideiaId).toAppIdeia())
    }

    fun observarIdeiasDoUsuario(uid: String): Flow<List<Ideia>> = flow {
        emit(api.minhasIdeias().map { it.toAppIdeia() })
    }

    suspend fun criarIdeia(ideia: Ideia): Result<String> = runCatching {
        val orientacaoId = api.listarOrientacoes(ativa = true)
            .firstOrNull()
            ?.id
            ?: error("Crie uma orientação ativa antes de enviar ideias.")

        api.criarIdeia(
            IdeiaBackendRequest(
                titulo = ideia.titulo,
                descricao = ideia.descricao,
                categoria = ideia.categoria,
                orientacaoId = orientacaoId
            )
        ).id.orEmpty()
    }

    suspend fun atualizarStatusIdeia(
        ideiaId: String,
        status: StatusIdeia,
        descricao: String = "",
        urgente: Boolean? = null
    ): Result<Unit> = runCatching {
        api.atualizarStatusIdeia(
            id = ideiaId,
            request = AtualizarStatusIdeiaBackendRequest(status.toBackendStatus())
        )
        Unit
    }

    suspend fun gerarSugestaoIA(titulo: String, descricao: String): Result<String> = runCatching {
        api.sugerirIdeia(
            SugestaoIaRequest(
                titulo = titulo,
                descricao = descricao
            )
        ).sugestao
    }

    suspend fun deletarIdeia(ideiaId: String): Result<Unit> = runCatching {
        api.deletarIdeia(ideiaId)
        Unit
    }

    suspend fun editarIdeiaComImpacto(
        ideiaId: String,
        titulo: String,
        descricao: String,
        categoria: String,
        localidade: String,
        impactoEstimado: Double
    ): Result<Unit> = runCatching {
        val ideiaAtual = api.buscarIdeia(ideiaId)
        val orientacaoId = ideiaAtual.orientacaoId
            ?.takeIf { it.isNotBlank() }
            ?: api.listarOrientacoes(ativa = true).firstOrNull()?.id
            ?: error("Crie uma orientação ativa antes de editar ideias.")

        api.atualizarIdeia(
            id = ideiaId,
            request = IdeiaBackendRequest(
                titulo = titulo,
                descricao = descricao,
                categoria = categoria.ifBlank { ideiaAtual.categoria.orEmpty().ifBlank { "GERAL" } },
                orientacaoId = orientacaoId
            )
        )

        Unit
    }

    fun observarProjetos(): Flow<List<Projeto>> = flow {
        val responsaveisPorId = api.rankingUsuarios()
            .mapNotNull { usuario ->
                val id = usuario.usuarioId.orEmpty()
                val nome = usuario.nome.orEmpty()
                if (id.isNotBlank() && nome.isNotBlank()) id to nome else null
            }
            .toMap()

        emit(api.listarProjetos().map { it.toAppProjeto(responsaveisPorId) })
    }

    fun observarProjeto(projetoId: String): Flow<Projeto?> = flow {
        val responsaveisPorId = api.rankingUsuarios()
            .mapNotNull { usuario ->
                val id = usuario.usuarioId.orEmpty()
                val nome = usuario.nome.orEmpty()
                if (id.isNotBlank() && nome.isNotBlank()) id to nome else null
            }
            .toMap()

        emit(api.listarProjetos().firstOrNull { it.id == projetoId }?.toAppProjeto(responsaveisPorId))
    }

    suspend fun criarProjeto(projeto: Projeto): Result<String> = runCatching {
        api.criarProjeto(
            ProjetoBackendRequest(
                nome = projeto.titulo,
                descricao = "Projeto criado a partir da ideia ${projeto.ideiaOrigemId}",
                ideiaId = projeto.ideiaOrigemId,
                responsavelId = UserSession.usuarioId,
                orcamento = projeto.economiaGerada.coerceAtLeast(0.0),
                dataFim = projeto.prazo.toBackendDateOrNull()
            )
        ).id.orEmpty()
    }

    suspend fun atualizarProjeto(
        projetoId: String,
        progresso: Int,
        economiaGerada: Double,
        status: StatusProjeto
    ): Result<Unit> = runCatching {
        if (status == StatusProjeto.CONCLUIDO) {
            api.registrarResultadoProjeto(
                id = projetoId,
                request = ResultadoBackendRequest(
                    investimento = 0.0,
                    retorno = economiaGerada.coerceAtLeast(0.0),
                    economia = economiaGerada.coerceAtLeast(0.0),
                    descricao = "Resultado registrado pelo aplicativo."
                )
            )
        } else {
            api.atualizarStatusProjeto(
                id = projetoId,
                request = ProjetoStatusBackendRequest(
                    status = when (status) {
                        StatusProjeto.PLANEJADO -> "PLANEJADO"
                        StatusProjeto.EM_DESENVOLVIMENTO -> "EM_EXECUCAO"
                        StatusProjeto.CONCLUIDO -> "CONCLUIDO"
                    }
                )
            )
        }

        Unit
    }

    suspend fun buscarRanking(): Result<List<RankingItem>> = runCatching {
        api.rankingUsuarios().mapIndexed { index, usuario ->
            val item = usuario.toAppRankingItem()
            if (item.posicao > 0) item else item.copy(posicao = index + 1)
        }
    }

    suspend fun buscarNotificacoes(uid: String): Result<List<Notificacao>> = Result.success(emptyList())

    suspend fun marcarNotificacaoLida(notificacaoId: String): Result<Unit> = Result.success(Unit)

    suspend fun analisarIdeiaComIa(ideiaId: String): Result<AnaliseIaResponse> = runCatching {
        api.analisarIdeiaComIa(
            AnaliseIaRequest(ideiaId = ideiaId)
        )
    }

    private fun OrientacaoEstrategica.toBackendRequest() = OrientacaoBackendRequest(
        titulo = titulo,
        descricao = descricao,
        categoria = divisao.ifBlank { "GERAL" },
        prazo = prazo.toBackendDateOrNull(),
        ativa = true
    )

    private fun String.toBackendDateOrNull(): String? {
        val valor = trim()
        if (valor.isBlank()) return null

        val partes = valor.split("/")
        if (partes.size != 3) return null

        val dia = partes[0].padStart(2, '0')
        val mes = partes[1].padStart(2, '0')
        val ano = partes[2]

        if (dia.length != 2 || mes.length != 2 || ano.length != 4) return null

        return "$ano-$mes-$dia"
    }
}
