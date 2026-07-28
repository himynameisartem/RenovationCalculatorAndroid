package com.skfamily.renovationcalculatir.ui.onboarding

import android.content.Context
import androidx.core.content.edit

class OnboardingPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)

    fun shouldShow(key: String): Boolean = prefs.getBoolean(key, true)

    fun markShown(key: String) {
        prefs.edit {
            putBoolean(key, false)
        }
    }
}

object OnboardingKeys {
    const val HOME = "home_onboarding"
    const val CALCULATOR = "calculator_onboarding"
    const val ESTIMATES = "estimates_onboarding"
}
