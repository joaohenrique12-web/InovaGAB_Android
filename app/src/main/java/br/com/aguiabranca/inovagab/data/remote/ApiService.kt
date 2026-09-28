package br.com.aguiabranca.inovagab.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("auth/health")
    suspend fun health(): String

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    @GET("auth/me")
    suspend fun usuarioAtual(): UsuarioResponse

    @GET("dashboard/resumo")
    suspend fun dashboardResumo(): DashboardBackendResponse

    @GET("ideias")
    suspend fun listarIdeias(
        @Query("status") status: String? = null,
        @Query("categoria") categoria: String? = null
    ): List<IdeiaBackendResponse>

    @GET("ideias/minhas")
    suspend fun minhasIdeias(): List<IdeiaBackendResponse>

    @GET("ideias/{id}")
    suspend fun buscarIdeia(
        @Path("id") id: String
    ): IdeiaBackendResponse

    @POST("ideias")
    suspend fun criarIdeia(
        @Body request: IdeiaBackendRequest
    ): IdeiaBackendResponse

    @PUT("ideias/{id}")
    suspend fun atualizarIdeia(
        @Path("id") id: String,
        @Body request: IdeiaBackendRequest
    ): IdeiaBackendResponse

    @DELETE("ideias/{id}")
    suspend fun deletarIdeia(
        @Path("id") id: String
    )

    @PUT("ideias/{id}/status")
    suspend fun atualizarStatusIdeia(
        @Path("id") id: String,
        @Body request: AtualizarStatusIdeiaBackendRequest
    ): IdeiaBackendResponse

    @GET("orientacoes")
    suspend fun listarOrientacoes(
        @Query("ativa") ativa: Boolean? = true
    ): List<OrientacaoBackendResponse>

    @POST("orientacoes")
    suspend fun criarOrientacao(
        @Body request: OrientacaoBackendRequest
    ): OrientacaoBackendResponse

    @PUT("orientacoes/{id}")
    suspend fun atualizarOrientacao(
        @Path("id") id: String,
        @Body request: OrientacaoBackendRequest
    ): OrientacaoBackendResponse

    @DELETE("orientacoes/{id}")
    suspend fun deletarOrientacao(
        @Path("id") id: String
    )

    @GET("projetos")
    suspend fun listarProjetos(
        @Query("status") status: String? = null
    ): List<ProjetoBackendResponse>

    @POST("projetos")
    suspend fun criarProjeto(
        @Body request: ProjetoBackendRequest
    ): ProjetoBackendResponse

    @PUT("projetos/{id}")
    suspend fun atualizarProjeto(
        @Path("id") id: String,
        @Body request: ProjetoBackendRequest
    ): ProjetoBackendResponse

    @DELETE("projetos/{id}")
    suspend fun deletarProjeto(
        @Path("id") id: String
    )

    @PUT("projetos/{id}/resultados")
    suspend fun registrarResultadoProjeto(
        @Path("id") id: String,
        @Body request: ResultadoBackendRequest
    ): ProjetoBackendResponse

    @PUT("projetos/{id}/status")
    suspend fun atualizarStatusProjeto(
        @Path("id") id: String,
        @Body request: ProjetoStatusBackendRequest
    ): ProjetoBackendResponse

    @GET("usuarios/ranking")
    suspend fun rankingUsuarios(): List<RankingUsuarioBackendResponse>

    @POST("ia/analisar-ideia")
    suspend fun analisarIdeiaComIa(
        @Body request: AnaliseIaRequest
    ): AnaliseIaResponse

    @POST("ia/sugerir-ideia")
    suspend fun sugerirIdeia(
        @Body request: SugestaoIaRequest
    ): SugestaoIaResponse
}