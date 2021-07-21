package com.oakraw.thailand_covid.di

import android.content.Context
import android.content.SharedPreferences
import com.oakraw.thailand_covid.R
import org.koin.core.qualifier.named
import org.koin.dsl.module

val AppDependency = module {
    fun provideSharePreference(context: Context): SharedPreferences {
        return context.getSharedPreferences(context.getString(R.string.app_pref), Context.MODE_PRIVATE)
    }

    single(named("AppPref")) { provideSharePreference(get()) }
}