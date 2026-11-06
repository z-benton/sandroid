package com.quinton.sandroid

import android.content.Context
import android.content.SharedPreferences
import android.os.UserManager

/** Speech must work on the boot lock screen, before credential storage is available. */
internal object TtsStorage {
    fun context(context: Context): Context =
        if (context.isDeviceProtectedStorage) context else context.createDeviceProtectedStorageContext()

    @Synchronized
    fun settings(context: Context): SharedPreferences {
        val storage = context(context)
        // Upgrades keep the existing voice and controls. Never touch credential storage
        // while locked; retry after unlock even if this process started during Direct Boot.
        if (context.getSystemService(UserManager::class.java).isUserUnlocked) {
            // The application uses Android's normal credential storage context; only
            // speech resources explicitly opt into device protected storage.
            val legacy = context.applicationContext
            check(storage.moveSharedPreferencesFrom(legacy, "settings")) {
                "Cannot migrate speech settings to device protected storage"
            }
        }
        return storage.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }
}
