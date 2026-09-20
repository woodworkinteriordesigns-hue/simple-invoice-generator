package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.BusinessProfile

class BusinessProfileManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("business_profile_prefs", Context.MODE_PRIVATE)

    fun getProfile(): BusinessProfile {
        return BusinessProfile(
            name = prefs.getString(KEY_NAME, "") ?: "",
            address = prefs.getString(KEY_ADDRESS, "") ?: "",
            phone = prefs.getString(KEY_PHONE, "") ?: "",
            email = prefs.getString(KEY_EMAIL, "") ?: "",
            logoPath = prefs.getString(KEY_LOGO_PATH, null),
            currency = prefs.getString(KEY_CURRENCY, "Rs") ?: "Rs",
            defaultNotes = prefs.getString(KEY_DEFAULT_NOTES, "") ?: ""
        )
    }

    fun saveProfile(profile: BusinessProfile) {
        prefs.edit()
            .putString(KEY_NAME, profile.name)
            .putString(KEY_ADDRESS, profile.address)
            .putString(KEY_PHONE, profile.phone)
            .putString(KEY_EMAIL, profile.email)
            .putString(KEY_LOGO_PATH, profile.logoPath)
            .putString(KEY_CURRENCY, profile.currency)
            .putString(KEY_DEFAULT_NOTES, profile.defaultNotes)
            .apply()
    }

    companion object {
        private const val KEY_NAME = "business_name"
        private const val KEY_ADDRESS = "business_address"
        private const val KEY_PHONE = "business_phone"
        private const val KEY_EMAIL = "business_email"
        private const val KEY_LOGO_PATH = "business_logo_path"
        private const val KEY_CURRENCY = "business_currency"
        private const val KEY_DEFAULT_NOTES = "business_default_notes"
    }
}
