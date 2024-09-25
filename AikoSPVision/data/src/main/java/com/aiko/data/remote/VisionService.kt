package com.aiko.data.remote

import com.aiko.domain.model.Linha
import com.aiko.domain.model.Parada
import com.aiko.domain.model.Previsao
import com.aiko.domain.network.NetworkResult
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


/**
 * Interface que define os serviços de rede para o VisionService.
 */
interface VisionService {

    /**
     * Realiza a autenticação através de um token.
     *
     * @param token Token de autenticação utilizado na requisição. Um valor padrão é fornecido.
     * @return [NetworkResult] com o resultado da autenticação.
     */
    @POST("Login/Autenticar")
    suspend fun postAuth(@Query("token") token: String = token_key): NetworkResult<Boolean>

    /**
     * Busca paradas de ônibus com base em um termo de pesquisa.
     *
     * @param searchTerm Termo de busca utilizado para encontrar as paradas.
     * @return [NetworkResult] contendo a lista de paradas encontradas.
     */
    @GET("Parada/Buscar")
    suspend fun getStopsBySearchTerm(@Query("termosBusca") searchTerm: String): NetworkResult<List<Parada>>

    /**
     * Obtém previsões de chegada de ônibus para uma parada específica.
     *
     * @param stopCode Código identificador da parada.
     * @return [NetworkResult] contendo as previsões para a parada.
     */
    @GET("Previsao/Parada")
    suspend fun getForecast(@Query("codigoParada") stopCode: Long): NetworkResult<Previsao>

    /**
     * Busca linhas de ônibus com base em um código ou número identificador.
     *
     * @param lineCode Código identificador da linha.
     * @return [NetworkResult] contendo a lista de linhas encontradas.
     */
    @GET("Linha/Buscar")
    suspend fun getLinesBySearchTerm(@Query("termosBusca") lineCode: Long): NetworkResult<List<Linha>>
}