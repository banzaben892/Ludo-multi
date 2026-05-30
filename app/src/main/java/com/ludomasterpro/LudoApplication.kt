package com.ludomasterpro

import android.app.Application

/**
 * Application class — point d'initialisation global.
 * Peut servir à initialiser des SDKs tiers ici si besoin.
 */
class LudoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialisation future (analytics, crash reporting…)
    }
}
