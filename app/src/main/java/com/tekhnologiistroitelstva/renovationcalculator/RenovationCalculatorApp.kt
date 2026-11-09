package com.tekhnologiistroitelstva.renovationcalculator

import android.app.Application
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import com.tekhnologiistroitelstva.renovationcalculator.notifications.ReminderScheduler

class RenovationCalculatorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ReminderScheduler.ensureChannel(this)

        val apiKey = getString(R.string.appmetrica_api_key)
        if (apiKey.isBlank()) {
            return
        }

        val config = AppMetricaConfig.newConfigBuilder(apiKey).build()
        AppMetrica.activate(this, config)
    }
}
