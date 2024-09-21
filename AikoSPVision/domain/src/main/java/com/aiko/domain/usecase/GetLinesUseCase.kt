package com.aiko.domain.usecase

import com.aiko.domain.model.Linha
import com.aiko.domain.network.NetworkResult
import com.aiko.domain.repository.SPVisionRepository
import com.aiko.domain.usecase.base.BaseUseCase
import com.aiko.domain.usecase.base.DataResult

class GetLinesUseCase(
    private val spVisionRepository: SPVisionRepository
) : BaseUseCase<Unit, List<Linha>>() {
    override suspend fun doWork(value: Unit): DataResult<List<Linha>> {
        return try {
            val result = spVisionRepository.getLines()
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
