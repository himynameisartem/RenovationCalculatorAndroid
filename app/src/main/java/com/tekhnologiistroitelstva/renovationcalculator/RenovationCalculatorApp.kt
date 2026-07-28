package com.tekhnologiistroitelstva.renovationcalculator

import android.app.Application
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig

class RenovationCalculatorApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val apiKey = getString(R.string.appmetrica_api_key)
        if (apiKey.isBlank()) {
            return
        }

        val config = AppMetricaConfig.newConfigBuilder(apiKey).build()
        AppMetrica.activate(this, config)
    }
}
