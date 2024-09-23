package com.aiko.domain.usecase

import com.aiko.domain.model.Previsao
import com.aiko.domain.network.NetworkResult
import com.aiko.domain.repository.SPVisionRepository
import com.aiko.domain.usecase.base.BaseUseCase
import com.aiko.domain.usecase.base.DataResult

class GetForecastUseCase(
    private val spVisionRepository: SPVisionRepository
) : BaseUseCase<Long, Previsao>() {

    override suspend fun doWork(stopCode: Long): DataResult<Previsao> {
        return try {
            val result = spVisionRepository.getForecast(stopCode)
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
