package com.aiko.domain.usecase

import com.aiko.domain.model.Parada
import com.aiko.domain.network.NetworkResult
import com.aiko.domain.repository.SPVisionRepository
import com.aiko.domain.usecase.base.BaseUseCase
import com.aiko.domain.usecase.base.DataResult

class GetStopsByLineUseCase(
    private val spVisionRepository: SPVisionRepository
) : BaseUseCase<Int, List<Parada>>() {
    override suspend fun doWork(lineCode: Int): DataResult<List<Parada>> {
        return try {
            val result = spVisionRepository.getStopsByLine(lineCode)
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
