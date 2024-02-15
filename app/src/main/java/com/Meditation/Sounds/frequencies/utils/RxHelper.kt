package com.Meditation.Sounds.frequencies.utils

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CombinedLiveData<T, K, S>(
    source1: LiveData<T>,
    source2: LiveData<K>,
    private val combine: (data1: T?, data2: K?) -> S
) : MediatorLiveData<S>() {

    private var data1: T? = null
    private var data2: K? = null

    init {
        super.addSource(source1) {
            data1 = it
            value = combine(data1, data2)
        }
        super.addSource(source2) {
            data2 = it
            value = combine(data1, data2)
        }
    }

    override fun <S : Any?> addSource(source: LiveData<S>, onChanged: Observer<in S>) {
        throw UnsupportedOperationException()
    }

    override fun <T : Any?> removeSource(toRemove: LiveData<T>) {
        throw UnsupportedOperationException()
    }
}


class Combined3LiveData<T, K, M, S>(
    source1: LiveData<T>,
    source2: LiveData<K>,
    source3: LiveData<M>,
    private val combine: (data1: T?, data2: K?, data3: M?) -> S
) : MediatorLiveData<S>() {

    private var data1: T? = null
    private var data2: K? = null
    private var data3: M? = null

    init {
        super.addSource(source1) {
            data1 = it
            value = combine(data1, data2, data3)
        }
        super.addSource(source2) {
            data2 = it
            value = combine(data1, data2, data3)
        }
        super.addSource(source3) {
            data3 = it
            value = combine(data1, data2, data3)
        }
    }

    override fun <S : Any?> addSource(source: LiveData<S>, onChanged: Observer<in S>) {
        throw UnsupportedOperationException()
    }

    override fun <T : Any?> removeSource(toRemove: LiveData<T>) {
        throw UnsupportedOperationException()
    }
}

class Combined4LiveData<T, K, M, N, S>(
    source1: LiveData<T>,
    source2: LiveData<K>,
    source3: LiveData<M>,
    source4: LiveData<N>,
    private val combine: (data1: T?, data2: K?, data3: M?, data4: N?) -> S
) : MediatorLiveData<S>() {

    private var data1: T? = null
    private var data2: K? = null
    private var data3: M? = null
    private var data4: N? = null

    init {
        super.addSource(source1) {
            data1 = it
            value = combine(data1, data2, data3, data4)
        }
        super.addSource(source2) {
            data2 = it
            value = combine(data1, data2, data3, data4)
        }
        super.addSource(source3) {
            data3 = it
            value = combine(data1, data2, data3, data4)
        }
        super.addSource(source4) {
            data4 = it
            value = combine(data1, data2, data3, data4)
        }
    }

    override fun <S : Any?> addSource(source: LiveData<S>, onChanged: Observer<in S>) {
        throw UnsupportedOperationException()
    }

    override fun <T : Any?> removeSource(toRemove: LiveData<T>) {
        throw UnsupportedOperationException()
    }
}


class FlowSearch {
    companion object {
        @SuppressLint("RestrictedApi")
        fun fromSearchView(editText: EditText): StateFlow<String> {
            val searchQuery = MutableStateFlow("")
            editText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(char: CharSequence, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(char: CharSequence, p1: Int, p2: Int, p3: Int) {
                    searchQuery.value = char.toString()
                }
            })
            return searchQuery
        }
    }
}

