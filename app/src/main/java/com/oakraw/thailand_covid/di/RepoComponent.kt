package com.oakraw.thailand_covid.di

import com.oakraw.thailand_covid.repository.ApiRepository
import com.oakraw.thailand_covid.repository.ApiRepositoryImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

val RepoDependency = module {
    single<ApiRepository> { ApiRepositoryImpl(get(), get(named("AppPref"))) }
}
