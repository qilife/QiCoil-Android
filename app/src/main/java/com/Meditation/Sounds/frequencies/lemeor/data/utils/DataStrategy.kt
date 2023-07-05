package com.Meditation.Sounds.frequencies.lemeor.data.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.Meditation.Sounds.frequencies.lemeor.data.utils.Resource.Status.*
import kotlinx.coroutines.Dispatchers

fun <T, A> performGetOperation(databaseQuery: () -> LiveData<T>,
                               networkCall: suspend () -> Resource<A>,
                               saveCallResult: suspend (A) -> Unit): LiveData<Resource<T>> =
        liveData(Dispatchers.IO) {
            emit(Resource.loading())

            val source = databaseQuery.invoke().map { Resource.success(it) }

            emitSource(source)

            val responseStatus = networkCall.invoke()

            if (responseStatus.status == SUCCESS) {
                if (responseStatus.data != null) {
                    saveCallResult(responseStatus.data)
                } else {
                    emit(Resource.error(""))
                    emitSource(source)
                }
            } else if (responseStatus.status == ERROR) {
                emit(Resource.error(responseStatus.message!!))
                emitSource(source)
            }
        }