package com.myjar.jarassignment.ui.vm

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myjar.jarassignment.createRetrofit
import com.myjar.jarassignment.data.model.ComputerItem
import com.myjar.jarassignment.data.repository.JarRepository
import com.myjar.jarassignment.data.repository.JarRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JarViewModel : ViewModel() {

    private val _listStringData = MutableStateFlow<List<ComputerItem>>(emptyList())
    val listStringData: StateFlow<List<ComputerItem>>
        get() = _listStringData

    private val repository: JarRepository = JarRepositoryImpl(createRetrofit())

    private val _query = MutableStateFlow("")
    val query : StateFlow<String> = _query.asStateFlow()
//    val query: State<String> = _query

    val _filteredList = MutableStateFlow(listStringData.value)
    val filteredList: StateFlow<List<ComputerItem>> = _filteredList

    init {
        fetchData()
        viewModelScope.launch {
            _query.collect {
                Log.i("fetch", "query-$it")
                _filteredList.value = if (it.isBlank()) {
                    Log.i("fetch", "query empty")
                    listStringData.value
                }
                else
                     listStringData.value.filter { item ->
                        val data = item.toString()
                        data.contains(it, ignoreCase = true )
                    }
            }
        }
    }

    fun updateQuery(text: String) {
        _query.value = text
    }

    private fun fetchData() {
        viewModelScope.launch {
            repository.fetchResults().collect { list ->
                _listStringData.value = list
                Log.i("fetch", "fetchData: ${_listStringData.value}")
            }
        }
    }
}