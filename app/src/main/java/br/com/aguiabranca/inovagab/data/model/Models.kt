// data/model/Models.kt
package br.com.aguiabranca.inovagab.data.model

// ─── Enums ───────────────────────────────────────────────

enum class PerfilUsuario {
    OPERACIONAL,
    TERMINAL,
    LOGISTICA,
    GESTOR,
    LIDER
}

enum class StatusIdeia {
    ENVIADA,
    EM_ANALISE,
    APROVADA,
    REPROVADA
}

enum class CategoriaIdeia {
    EMBARQUE,
    INCIDENTE,
    BOA_PRATICA,
    INFRAESTRUTURA,
    COMBUSTIVEL,
    ROTA,
    MECANICA,
    CARGA
}

enum class Complexidade {
    BAIXA,
    MEDIA,
    ALTA
}

enum class StatusProjeto {
    PLANEJADO,
    EM_DESENVOLVIMENTO,
    CONCLUIDO
}

enum class TipoAnexo {
    FOTO,
    AUDIO
}

enum class TipoOcorrencia {
    CONSUMO_EXCESSIVO,
    PNEU,
    MECANICA,
    SEGURANCA
}

enum class PrioridadeOcorrencia {
    URGENTE,
    ATENCAO,
    NORMAL
}

// ─── Data Classes ─────────────────────────────────────────

data class Usuario(
    val id: String = "",
    val nome: String = "",
    val email: String = "",
    val perfil: String = "",
    val divisao: String = "",
    val linha: String = "",
    val placa: String = "",
    val rota: String = "",
    val pontos: Int = 0,
    val posicaoRanking: Int = 0,
    val fotoUrl: String = ""
)

data class Ideia(
    val id: String = "",
    val titulo: String = "",
    val descricao: String = "",
    val autorId: String = "",
    val autorNome: String = "",
    val status: String = StatusIdeia.ENVIADA.name,
    val categoria: String = CategoriaIdeia.EMBARQUE.name,
    val complexidade: String = Complexidade.MEDIA.name,
    val localidade: String = "",
    val impactoEstimado: Double = 0.0,
    val votos: Int = 0,
    val percentualAprovacao: Int = 0,
    val sugestaoIA: String = "",
    val dataCriacao: String = "",
    val dataAtualizacao: String = "",
    val urgente: Boolean = false,
    val anexos: List<Anexo> = emptyList()
) {
    // Helpers para converter String em Enum sem quebrar a tela se o backend enviar valor inesperado.
    fun statusEnum(): StatusIdeia =
        runCatching { StatusIdeia.valueOf(status) }.getOrDefault(StatusIdeia.ENVIADA)

    fun complexidadeEnum(): Complexidade =
        runCatching { Complexidade.valueOf(complexidade) }.getOrDefault(Complexidade.MEDIA)
}

data class TimelineEvento(
    val status: String = "",
    val data: String = "",
    val descricao: String = ""
)

data class Anexo(
    val tipo: String = "",
    val url: String = ""
)

data class Projeto(
    val id: String = "",
    val titulo: String = "",
    val ideiaOrigemId: String = "",
    val responsavel: String = "",
    val status: String = StatusProjeto.PLANEJADO.name,
    val progresso: Int = 0,
    val investimento: Double = 0.0,
    val retornoEstimado: Double = 0.0,
    val economiaGerada: Double = 0.0,
    val roi: Double = 0.0,
    val prazo: String = "",
    val divisao: String = "",
    val dataCriacao: String = ""
) {
    fun statusEnum(): StatusProjeto =
        runCatching { StatusProjeto.valueOf(status) }.getOrDefault(StatusProjeto.PLANEJADO)
}

data class OrientacaoEstrategica(
    val id: String = "",
    val titulo: String = "",
    val descricao: String = "",
    val divisao: String = "",
    val autorNome: String = "",
    val prazo: String = "",
    val dataCriacao: String = ""
)

data class Ocorrencia(
    val id: String = "",
    val tipo: String = "",
    val descricao: String = "",
    val veiculoPlaca: String = "",
    val motoristaNome: String = "",
    val rota: String = "",
    val prioridade: String = PrioridadeOcorrencia.NORMAL.name,
    val data: String = ""
) {
    fun prioridadeEnum(): PrioridadeOcorrencia =
        runCatching { PrioridadeOcorrencia.valueOf(prioridade) }.getOrDefault(PrioridadeOcorrencia.NORMAL)
}

data class Notificacao(
    val id: String = "",
    val usuarioId: String = "",
    val mensagem: String = "",
    val lida: Boolean = false,
    val criadaEm: String = ""
)

data class RankingItem(
    val posicao: Int = 0,
    val usuarioId: String = "",
    val usuarioNome: String = "",
    val divisao: String = "",
    val pontos: Int = 0,
    val ideiasEnviadas: Int = 0,
    val ideiasAprovadas: Int = 0,
    val impactoTotal: Double = 0.0
)

data class DashboardExecutivo(
    val economiaTotal: Double = 0.0,
    val roiMedio: Int = 0,
    val totalProjetos: Int = 0,
    val projetosAtivos: Int = 0,
    val projetosConcluidos: Int = 0,
    val produtividade: Int = 0,
    val ideiasNoFunil: FunilInovacao = FunilInovacao(),
    val engajamentoPorPerfil: List<EngajamentoPerfil> = emptyList(),
    val impactoPorDivisao: List<ImpactoDivisao> = emptyList()
)

data class FunilInovacao(
    val enviadas: Int = 0,
    val triagem: Int = 0,
    val aprovadas: Int = 0,
    val projetos: Int = 0,
    val urgentes: Int = 0
)

data class EngajamentoPerfil(
    val perfil: String = "",
    val percentual: Int = 0
)

data class ImpactoDivisao(
    val divisao: String = "",
    val economia: Double = 0.0,
    val percentual: Int = 0
)
