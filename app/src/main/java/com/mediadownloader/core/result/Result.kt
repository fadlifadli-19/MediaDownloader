package com.mediadownloader.core.result

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

typealias AppResult<T> = kotlin.Result<T>

fun <T> Flow<T>.asResource(): Flow<Resource<T>> =
    this
        .map<T, Resource<T>> { Resource.Success(it) }
        .onStart { emit(Resource.Loading) }
        .catch { emit(Resource.Error(it.message ?: "Unknown error", it)) }

suspend fun <T> safeCall(block: suspend () -> T): Resource<T> =
    runCatching { block() }
        .fold(
            onSuccess = { Resource.Success(it) },
            onFailure = { Resource.Error(it.message ?: "Unknown error", it) }
        )
