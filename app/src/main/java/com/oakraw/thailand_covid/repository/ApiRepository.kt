package com.oakraw.thailand_covid.repository

import com.oakraw.thailand_covid.model.CaseInfo
import com.oakraw.thailand_covid.network.Resource
import kotlinx.coroutines.flow.Flow

interface ApiRepository {
    fun getTodayCaseInfo(): Flow<Resource<CaseInfo>>
}