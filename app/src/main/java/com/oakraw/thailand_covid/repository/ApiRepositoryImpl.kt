package com.oakraw.thailand_covid.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.oakraw.thailand_covid.model.CaseInfo
import com.oakraw.thailand_covid.network.ApiService
import com.oakraw.thailand_covid.network.Resource
import com.oakraw.thailand_covid.network.networkBoundResource
import kotlinx.coroutines.flow.Flow

class ApiRepositoryImpl(
    private val service: ApiService,
    private val pref: SharedPreferences
) : ApiRepository {
    override fun getTodayCaseInfo(): Flow<Resource<CaseInfo>> = networkBoundResource(
        loadFromDb = {
            val raw = pref.getString(CACHE_KEY, null)
            if (raw == null) null
            else Gson().fromJson(raw, CaseInfo::class.java)
        },
        createCall = { service.getTodayCaseInfo() },
        saveCallResult = { pref.edit { putString(CACHE_KEY, Gson().toJson(it)) } },
        shouldFetch = { true }
    )

    companion object {
        const val CACHE_KEY = "cache_key"
    }
}