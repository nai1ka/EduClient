package ru.ndevelop.educlient.ui

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import ru.ndevelop.educlient.R
import ru.ndevelop.educlient.ui.dialogs.SettingButtons
import ru.ndevelop.educlient.ui.dialogs.SettingsDialogFragment


class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences,rootKey)
        val backgroundButton: Preference? = findPreference(getString(R.string.bg_key))
        val adsButton: Preference? = findPreference(getString(R.string.ads_key))
        val siteButton:Preference? = findPreference("DEVELOPER_SITE")
        backgroundButton?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            (parentFragment as SettingsDialogFragment).onSettingsButtonClicked(SettingButtons.BACKGROUND)
            true
        }
        adsButton?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            (parentFragment as SettingsDialogFragment).onSettingsButtonClicked(SettingButtons.ADS)
            true
        }
        siteButton?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            (parentFragment as SettingsDialogFragment).onSettingsButtonClicked(SettingButtons.CREDITS)
            true
        }
    }
}
