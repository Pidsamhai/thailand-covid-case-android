package com.oakraw.thailand_covid.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oakraw.thailand_covid.model.CaseInfo
import com.oakraw.thailand_covid.network.Resource
import com.oakraw.thailand_covid.repository.ApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class MainViewModel(private val apiRepository: ApiRepository) : ViewModel() {
    private val fetchKey = MutableStateFlow(System.currentTimeMillis())
    val resource: Flow<Resource<CaseInfo>> = fetchKey.flatMapLatest {
        apiRepository.getTodayCaseInfo()
    }

    fun refresh() = viewModelScope.launch(Dispatchers.IO) {
        fetchKey.emit(System.currentTimeMillis())
    }
}