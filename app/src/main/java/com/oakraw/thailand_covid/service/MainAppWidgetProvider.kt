package com.oakraw.thailand_covid.service

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.oakraw.thailand_covid.R
import com.oakraw.thailand_covid.model.CaseInfo
import com.oakraw.thailand_covid.network.Resource
import com.oakraw.thailand_covid.repository.ApiRepository
import com.oakraw.thailand_covid.ui.main.MainActivity
import com.oakraw.thailand_covid.utils.display
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*


class MainAppWidgetProvider : AppWidgetProvider(), KoinComponent {
    private var shouldShowCovid: Boolean = false
    private var remoteConfig: FirebaseRemoteConfig? = null
    private val apiRepository: ApiRepository by inject()
    private val REFRESH_TAG = "REFRESH_TAG"

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == REFRESH_TAG) {
            Toast.makeText(context, "Updating...", Toast.LENGTH_SHORT).show();
//            val views = RemoteViews(
//                context.packageName,
//                R.layout.view_widget_template1
//            )
//            val componentName = ComponentName(context, MainAppWidgetProvider::class.java)
//            val appWidgetManager = AppWidgetManager.getInstance(context)
//            val ids = appWidgetManager.getAppWidgetIds(componentName)
//            for (id in ids) {
//                appWidgetManager.updateAppWidget(id, views)
//            }
            val updateIntent = Intent(context, MainAppWidgetProvider::class.java)
            updateIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

            val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(
                    ComponentName(
                        context,
                        MainAppWidgetProvider::class.java
                    )
                )

            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(updateIntent)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d("MainAppWidgetProvider", "update")
        fetchRemoteConfig()
        GlobalScope.launch(Dispatchers.IO) {
            apiRepository.getTodayCaseInfo().collectLatest {
                when(it) {
                    is Resource.Error -> {
                        Log.i("TAG", "Error: ")
                    }
                    Resource.Loading -> {
                        Log.i("TAG", "Loading: ")
                    }
                    is Resource.Success -> {
                        Log.i("TAG", "Success: '")
                        appWidgetIds.forEach { appWidgetId ->
                            renderWidget(
                                context,
                                appWidgetManager, appWidgetId, it.data
                            )
                        }
                    }
                }
            }
        }
    }

    private fun fetchRemoteConfig() {
        remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig?.setConfigSettingsAsync(configSettings)
        shouldShowCovid = remoteConfig?.getBoolean("show_covid") ?: false
    }

    private fun renderWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        caseInfo: CaseInfo
    ) {
        val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
            .let { intent ->
                PendingIntent.getActivity(context, 0, intent, 0)
            }

        val views: RemoteViews = RemoteViews(
            context.packageName,
            R.layout.view_widget_template1
        ).apply {
            setOnClickPendingIntent(R.id.root, pendingIntent)
            setTextViewText(R.id.textNewCase, caseInfo.newConfirmedDisplay)
            setTextViewText(R.id.textDate, caseInfo.updatedDisplay)
            setTextViewText(R.id.textDeath, caseInfo.newDeathsDisplay)
            setTextViewText(R.id.textHeal, caseInfo.hospitalizedDisplay)
            setTextViewText(R.id.textUpdateTimestamp, Date().display("dd/MM/YY hh:mm") ?: "")
            setOnClickPendingIntent(R.id.buttonRefresh, getPendingSelfIntent(context, REFRESH_TAG));


            setViewVisibility(
                R.id.imageNewCase,
                if (shouldShowCovid) View.VISIBLE else View.INVISIBLE
            )
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    protected fun getPendingSelfIntent(context: Context?, action: String?): PendingIntent? {
        val intent = Intent(context, javaClass)
        intent.action = action
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }
}