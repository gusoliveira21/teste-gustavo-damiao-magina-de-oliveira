package com.aiko.data.remote

import com.aiko.domain.model.Linha
import com.aiko.domain.model.Parada
import com.aiko.domain.model.PosicaoVeiculo
import com.aiko.domain.model.Previsao
import com.aiko.domain.network.NetworkResult
import retrofit2.http.GET
import retrofit2.http.Query

    interface VisionService {
        @GET("vision-api/lines")
        suspend fun getLines(): NetworkResult<List<Linha>>

        @GET("vision-api/vehicles")
        suspend fun getVehiclePosition(@Query("lineCode") lineCode: Int): NetworkResult<PosicaoVeiculo>

        @GET("vision-api/stops")
        suspend fun getStopsByLine( @Query("lineCode") lineCode: Int): NetworkResult<List<Parada>>

        @GET("vision-api/forecast")
        suspend fun getForecast(
            @Query("stopCode") stopCode: Int,
            @Query("lineCode") lineCode: Int
        ): NetworkResult<Previsao>

        @GET("vision-api/search")
        suspend fun searchLines(@Query("searchTerms") searchTerms: String): NetworkResult<List<Linha>>
    }