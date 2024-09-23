package com.aiko.data.remote

import com.aiko.domain.model.Linha
import com.aiko.domain.model.Parada
import com.aiko.domain.model.PosicaoVeiculo
import com.aiko.domain.model.Previsao
import com.aiko.domain.network.NetworkResult
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

const val token_key = "8df2ebbc7fed5723047bf5505576bc6f3986f9f6e43e1efc0b40086fc4e438c1"

interface VisionService {

    @POST("Login/Autenticar")
    suspend fun postAuth(@Query("token") token: String = token_key): NetworkResult<Boolean>

    @GET("Parada/Buscar")
    suspend fun getStopsBySearchTerm(@Query("termosBusca") searchTerm: String): NetworkResult<List<Parada>>



    @GET("vision-api/lines")
    suspend fun getLines(): NetworkResult<List<Linha>>

    @GET("vision-api/vehicles")
    suspend fun getVehiclePosition(@Query("lineCode") lineCode: Int): NetworkResult<PosicaoVeiculo>

    @GET("vision-api/forecast")
    suspend fun getForecast(
        @Query("stopCode") stopCode: Int,
        @Query("lineCode") lineCode: Int
    ): NetworkResult<Previsao>

    @GET("vision-api/search")
    suspend fun searchLines(@Query("searchTerms") searchTerms: String): NetworkResult<List<Linha>>
}