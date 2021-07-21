package com.oakraw.thailand_covid.network

import android.util.Log
import com.haroldadmin.cnradapter.NetworkResponse
import com.oakraw.thailand_covid.model.ErrorResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

inline fun <ResultType : Any, RequestType : Any> networkBoundResource(
    crossinline loadFromDb: suspend () -> ResultType?,
    crossinline createCall: suspend () -> NetworkResponse<RequestType, ErrorResponse>,
    crossinline saveCallResult: suspend (RequestType) -> Unit,
//    crossinline onFetchFailed: (Throwable) -> Unit = { },
    crossinline shouldFetch: (ResultType?) -> Boolean = { true }
): Flow<Resource<ResultType>> = flow {
    val dbValue = loadFromDb()
    if (dbValue != null) emit(Resource.Success(dbValue))
    when {
        shouldFetch(dbValue) -> {
            emit(Resource.Loading)
            createCall().also {
                Log.i("TAG", "networkBoundResource: $it")
                when(it) {
                    is NetworkResponse.Success -> {
                        saveCallResult(it.body)
                        emit(Resource.Success(loadFromDb()!!))
                    }
                    is NetworkResponse.ServerError -> {
                        emit(Resource.Error("Server Error"))
//                        onFetchFailed(it.error)
                    }
                    is NetworkResponse.NetworkError -> {
                        emit(Resource.Error("Network Error"))
//                        onFetchFailed(it.error)
                    }
                    is NetworkResponse.UnknownError -> {
                        emit(Resource.Error("Unknown Error"))
//                        onFetchFailed(it.error)
                    }
                }
            }
        }
    }
}
