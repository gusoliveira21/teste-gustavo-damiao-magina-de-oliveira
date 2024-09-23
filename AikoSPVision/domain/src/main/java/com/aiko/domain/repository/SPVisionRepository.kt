package com.aiko.domain.repository

import com.aiko.domain.model.Linha
import com.aiko.domain.model.Parada
import com.aiko.domain.model.PosicaoVeiculo
import com.aiko.domain.model.Previsao
import com.aiko.domain.network.NetworkResult

interface SPVisionRepository {
    suspend fun postAuth(): NetworkResult<Boolean>

    suspend fun getStopsBySearchTerm(term: String ): NetworkResult<List<Parada>>

    suspend fun getLines(): NetworkResult<List<Linha>>

    suspend fun getVehiclePosition( lineCode: Int ): NetworkResult<PosicaoVeiculo>

    suspend fun getForecast( stopCode: Int, lineCode: Int ): NetworkResult<Previsao>

    suspend fun searchLines( searchTerms: String ): NetworkResult<List<Linha>>
}
