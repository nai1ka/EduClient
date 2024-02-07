package ru.ndevelop.educlient.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import ru.ndevelop.educlient.R
import ru.ndevelop.educlient.ui.SettingsFragment
import ru.ndevelop.educlient.ui.profile.ProfileFragment


class SettingsDialogFragment : DialogFragment() {

    private val settingsFragment = SettingsFragment()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        val manager = this.childFragmentManager
        manager.beginTransaction().add(
            R.id.cv_settings,
            settingsFragment
        ).addToBackStack("settings").commit()
        return dialog
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }

    fun onSettingsButtonClicked(buttonType:SettingButtons){
        when(buttonType){
            SettingButtons.BACKGROUND -> (parentFragment as ProfileFragment).onBackgroundChangedClicked()
            SettingButtons.ADS -> (parentFragment as ProfileFragment).onAdsClicked()
            SettingButtons.CREDITS -> (parentFragment as ProfileFragment).onCreditsClicked()
        }

    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setBackgroundDrawable(null)
    }


}

enum class SettingButtons{
    BACKGROUND, ADS, CREDITS

}