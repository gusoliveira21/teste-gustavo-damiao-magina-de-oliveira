package com.aiko.domain.usecase

import com.aiko.domain.model.Linha
import com.aiko.domain.network.NetworkResult
import com.aiko.domain.repository.SPVisionRepository
import com.aiko.domain.usecase.base.BaseUseCase
import com.aiko.domain.usecase.base.DataResult

class SearchLinesUseCase(
    private val spVisionRepository: SPVisionRepository
) : BaseUseCase<String, List<Linha>>() {

    override suspend fun doWork(searchTerms: String): DataResult<List<Linha>> {
        return try {
            val result = spVisionRepository.searchLines(searchTerms)
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
