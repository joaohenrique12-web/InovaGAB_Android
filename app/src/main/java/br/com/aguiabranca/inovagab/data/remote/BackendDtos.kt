package br.com.aguiabranca.inovagab.data.remote

import br.com.aguiabranca.inovagab.data.model.DashboardExecutivo
import br.com.aguiabranca.inovagab.data.model.FunilInovacao
import br.com.aguiabranca.inovagab.data.model.Ideia
import br.com.aguiabranca.inovagab.data.model.OrientacaoEstrategica
import br.com.aguiabranca.inovagab.data.model.Projeto
import br.com.aguiabranca.inovagab.data.model.RankingItem
import br.com.aguiabranca.inovagab.data.model.StatusIdeia
import br.com.aguiabranca.inovagab.data.model.StatusProjeto
import br.com.aguiabranca.inovagab.data.model.Usuario

data class DashboardBackendResponse(
    val totalIdeias: Int = 0,
    val ideiasAprovadas: Int = 0,
    val ideiasRejeitadas: Int = 0,
    val ideiasEmAnalise: Int = 0,
    val totalProjetos: Int = 0,
    val projetosAtivos: Int = 0,
    val projetosConcluidos: Int = 0,
    val economiaTotal: Double = 0.0,
    val roiMedio: Double = 0.0
)

data class IdeiaBackendRequest(
    val titulo: String,
    val descricao: String,
    val categoria: String,
    val orientacaoId: String
)

data class IdeiaBackendResponse(
    val id: String? = null,
    val titulo: String? = null,
    val descricao: String? = null,
    val categoria: String? = null,
    val autorId: String? = null,
    val orientacaoId: String? = null,
    val status: String? = null,
    val iaScore: Double? = null,
    val iaAnalise: String? = null,
    val dataCriacao: String? = null,
    val dataAtualizacao: String? = null
)

data class AtualizarStatusIdeiaBackendRequest(val status: String)

data class OrientacaoBackendRequest(
    val titulo: String,
    val descricao: String,
    val categoria: String,
    val prazo: String? = null,
    val ativa: Boolean = true
)

data class OrientacaoBackendResponse(
    val id: String? = null,
    val titulo: String? = null,
    val descricao: String? = null,
    val categoria: String? = null,
    val prazo: String? = null,
    val ativa: Boolean? = true,
    val dataCriacao: String? = null,
    val dataAtualizacao: String? = null
)

data class ProjetoBackendRequest(
    val nome: String,
    val descricao: String,
    val ideiaId: String,
    val responsavelId: String?,
    val orcamento: Double,
    val dataFim: String? = null
)

data class ProjetoStatusBackendRequest(
    val status: String
)


data class ProjetoBackendResponse(
    val id: String? = null,
    val nome: String? = null,
    val descricao: String? = null,
    val ideiaId: String? = null,
    val responsavelId: String? = null,
    val status: String? = null,
    val orcamento: Double = 0.0,
    val dataInicio: String? = null,
    val dataFim: String? = null,
    val resultado: ResultadoBackendResponse? = null
)

data class ResultadoBackendRequest(
    val investimento: Double,
    val retorno: Double,
    val economia: Double,
    val descricao: String
)

data class ResultadoBackendResponse(
    val investimento: Double = 0.0,
    val retorno: Double = 0.0,
    val economia: Double = 0.0,
    val roi: Double? = null,
    val descricao: String? = null
)

data class RankingUsuarioBackendResponse(
    val posicao: Int = 0,
    val usuarioId: String? = null,
    val nome: String? = null,
    val role: String? = null,
    val pontos: Int? = 0
)

data class AnaliseIaRequest(
    val ideiaId: String
)

data class AnaliseIaResponse(
    val ideiaId: String? = null,
    val score: Int = 0,
    val inovacao: Int = 0,
    val viabilidade: Int = 0,
    val impacto: Int = 0,
    val alinhamentoEstrategico: Int = 0,
    val riscos: List<String> = emptyList(),
    val justificativa: String = "",
    val modelo: String? = null,
    val dataAnalise: String? = null
)

data class SugestaoIaRequest(
    val titulo: String,
    val descricao: String
)

data class SugestaoIaResponse(
    val sugestao: String
)

fun UsuarioResponse.toAppUsuario(posicaoRanking: Int = 0) = Usuario(
    id = id.orEmpty(),
    nome = nome.orEmpty(),
    email = email.orEmpty(),
    perfil = role.orEmpty(),
    divisao = "Grupo Águia Branca",
    pontos = pontos ?: 0,
    posicaoRanking = posicaoRanking
)

fun DashboardBackendResponse.toAppDashboard() = DashboardExecutivo(
    economiaTotal = economiaTotal,
    roiMedio = roiMedio.toInt(),
    totalProjetos = totalProjetos,
    projetosAtivos = projetosAtivos,
    projetosConcluidos = projetosConcluidos,
    produtividade = totalIdeias,
    ideiasNoFunil = FunilInovacao(
        enviadas = totalIdeias,
        triagem = ideiasEmAnalise,
        aprovadas = ideiasAprovadas,
        projetos = projetosAtivos,
        urgentes = 0
    )
)

fun IdeiaBackendResponse.toAppIdeia() = Ideia(
    id = id.orEmpty(),
    titulo = titulo.orEmpty(),
    descricao = descricao.orEmpty(),
    autorId = autorId.orEmpty(),
    status = status.toAppStatusIdeia(),
    categoria = categoria.orEmpty(),
    impactoEstimado = iaScore ?: 0.0,
    sugestaoIA = iaAnalise.orEmpty(),
    dataCriacao = dataCriacao.orEmpty(),
    dataAtualizacao = dataAtualizacao.orEmpty()
)

fun OrientacaoBackendResponse.toAppOrientacao() = OrientacaoEstrategica(
    id = id.orEmpty(),
    titulo = titulo.orEmpty(),
    descricao = descricao.orEmpty(),
    divisao = categoria.orEmpty(),
    autorNome = UserSession.nome?.takeIf { it.isNotBlank() } ?: "Grupo Águia Branca",
    prazo = prazo.toBrazilianDateOrEmpty(),
    dataCriacao = dataCriacao.orEmpty()
)

fun ProjetoBackendResponse.toAppProjeto(
    responsaveisPorId: Map<String, String> = emptyMap()
) = Projeto(
    id = id.orEmpty(),
    titulo = nome?.takeIf { it.isNotBlank() && it != "string" } ?: "Projeto sem nome",
    ideiaOrigemId = ideiaId.orEmpty(),
    responsavel = responsaveisPorId[responsavelId.orEmpty()]
        ?: responsavelId?.takeIf { it.isNotBlank() && it != "string" }
        ?: "Não definido",
    status = status.toAppStatusProjeto(),
    progresso = when (status?.uppercase()) {
        "CONCLUIDO" -> 100
        "EM_EXECUCAO" -> 50
        "PLANEJADO" -> 0
        else -> 0
    },
    investimento = resultado?.investimento ?: orcamento,
    retornoEstimado = resultado?.retorno ?: 0.0,
    economiaGerada = resultado?.economia ?: 0.0,
    roi = resultado?.roi ?: 0.0,
    prazo = dataFim?.takeIf { it.isNotBlank() && it != "string" } ?: "Sem prazo",
    dataCriacao = dataInicio.orEmpty()
)

fun RankingUsuarioBackendResponse.toAppRankingItem() = RankingItem(
    posicao = posicao,
    usuarioId = usuarioId.orEmpty(),
    usuarioNome = nome.orEmpty(),
    divisao = role.orEmpty(),
    pontos = pontos ?: 0
)

fun StatusIdeia.toBackendStatus(): String = when (this) {
    StatusIdeia.ENVIADA -> "SUBMETIDA"
    StatusIdeia.REPROVADA -> "REJEITADA"
    StatusIdeia.EM_ANALISE -> "EM_ANALISE"
    StatusIdeia.APROVADA -> "APROVADA"
}

private fun String?.toAppStatusIdeia(): String = when (this?.uppercase()) {
    "SUBMETIDA" -> StatusIdeia.ENVIADA.name
    "REJEITADA" -> StatusIdeia.REPROVADA.name
    "EM_ANALISE" -> StatusIdeia.EM_ANALISE.name
    "APROVADA" -> StatusIdeia.APROVADA.name
    else -> StatusIdeia.ENVIADA.name
}

private fun String?.toAppStatusProjeto(): String = when (this?.uppercase()) {
    "EM_EXECUCAO" -> StatusProjeto.EM_DESENVOLVIMENTO.name
    "CONCLUIDO" -> StatusProjeto.CONCLUIDO.name
    else -> StatusProjeto.PLANEJADO.name
}

private fun String?.toBrazilianDateOrEmpty(): String {
    val valor = this?.trim().orEmpty()
    if (valor.isBlank()) return ""

    val data = valor.take(10)
    val partes = data.split("-")
    if (partes.size != 3) return valor

    val ano = partes[0]
    val mes = partes[1]
    val dia = partes[2]
    if (ano.length != 4 || mes.length != 2 || dia.length != 2) return valor

    return "$dia/$mes/$ano"
}
