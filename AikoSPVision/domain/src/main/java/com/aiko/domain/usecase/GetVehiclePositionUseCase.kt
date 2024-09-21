package com.aiko.domain.usecase

import com.aiko.domain.model.PosicaoVeiculo
import com.aiko.domain.network.NetworkResult
import com.aiko.domain.repository.SPVisionRepository
import com.aiko.domain.usecase.base.BaseUseCase
import com.aiko.domain.usecase.base.DataResult

class GetVehiclePositionUseCase(
    private val spVisionRepository: SPVisionRepository
) : BaseUseCase<Int, PosicaoVeiculo>() {
    override suspend fun doWork(lineCode: Int): DataResult<PosicaoVeiculo> {
        return try {
            val result = spVisionRepository.getVehiclePosition(lineCode)
            when (result) {
                is NetworkResult.Success -> DataResult.Success(result.data)
                is NetworkResult.Error -> DataResult.Failure(Throwable(result.body?.error))
                is NetworkResult.Exception -> DataResult.Failure(result.e)
            }
        } catch (e: Exception) {
            DataResult.Failure(e)
        }
    }
}
