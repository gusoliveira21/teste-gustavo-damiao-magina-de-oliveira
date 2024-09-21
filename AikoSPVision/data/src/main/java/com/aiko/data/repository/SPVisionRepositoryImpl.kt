package com.aiko.data.repository

import com.aiko.data.remote.VisionService
import com.aiko.domain.model.Linha
import com.aiko.domain.model.Parada
import com.aiko.domain.model.PosicaoVeiculo
import com.aiko.domain.model.Previsao
import com.aiko.domain.network.ErrorBody
import com.aiko.domain.network.NetworkResult
import com.aiko.domain.repository.SPVisionRepository

class SPVisionRepositoryImpl(
    private val visionService: VisionService
) : SPVisionRepository {
    override suspend fun getLines(): NetworkResult<List<Linha>> {
        return when (val result = visionService.getLines()) {
            is NetworkResult.Success -> NetworkResult.Success(result.data)
            is NetworkResult.Exception -> NetworkResult.Exception(Error("${result.e}"))
            is NetworkResult.Error -> NetworkResult.Error( code = 0, ErrorBody("UNKNOWN_MESSAGE_ERROR") )
        }
    }

    override suspend fun getVehiclePosition(
        lineCode: Int
    ): NetworkResult<PosicaoVeiculo> {
        return when (val result = visionService.getVehiclePosition(lineCode)) {
            is NetworkResult.Success -> NetworkResult.Success(result.data)
            is NetworkResult.Exception -> NetworkResult.Exception(Error("${result.e}"))
            is NetworkResult.Error -> NetworkResult.Error( code = 0, ErrorBody("UNKNOWN_MESSAGE_ERROR") )
        }
    }

    override suspend fun getStopsByLine(lineCode: Int): NetworkResult<List<Parada>> {
        return when (val result = visionService.getStopsByLine(lineCode)) {
            is NetworkResult.Success -> NetworkResult.Success(result.data)
            is NetworkResult.Exception -> NetworkResult.Exception(Error("${result.e}"))
            is NetworkResult.Error -> NetworkResult.Error( code = 0, ErrorBody("UNKNOWN_MESSAGE_ERROR") )
        }
    }

    override suspend fun getForecast(
        stopCode: Int,
        lineCode: Int
    ): NetworkResult<Previsao> {
        return when (val result = visionService.getForecast(stopCode = stopCode, lineCode = lineCode)) {
            is NetworkResult.Success -> NetworkResult.Success(result.data)
            is NetworkResult.Exception -> NetworkResult.Exception(Error("${result.e}"))
            is NetworkResult.Error -> NetworkResult.Error( code = 0, ErrorBody("UNKNOWN_MESSAGE_ERROR") )
        }
    }

    override suspend fun searchLines(
        searchTerms: String
    ): NetworkResult<List<Linha>> {
        return when (val result = visionService.searchLines(searchTerms = searchTerms)) {
            is NetworkResult.Success -> NetworkResult.Success(result.data)
            is NetworkResult.Exception -> NetworkResult.Exception(Error("${result.e}"))
            is NetworkResult.Error -> NetworkResult.Error( code = 0, ErrorBody("UNKNOWN_MESSAGE_ERROR") )
        }
    }

}