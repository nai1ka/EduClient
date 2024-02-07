package ru.ndevelop.educlient.ui.profile

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import ru.ndevelop.educlient.R
import ru.ndevelop.educlient.models.User
import ru.ndevelop.educlient.repositories.PreferencesRepository
import ru.ndevelop.educlient.ui.dialogs.BackgroundChangeDialog
import ru.ndevelop.educlient.ui.dialogs.SettingsDialogFragment
import ru.ndevelop.educlient.ui.mainActivity.MainActivity
import ru.ndevelop.educlient.ui.user_control.UserControlFragment
import ru.ndevelop.educlient.utils.DataBaseHandler



class ProfileFragment : Fragment(), View.OnClickListener,
    SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var profileMainLayout: FrameLayout
    private lateinit var changeBtn: ImageView
    private val bgChangeFragment = BackgroundChangeDialog()
    private lateinit var manager: FragmentManager
    private lateinit var tvName: TextView
    private lateinit var tvSchool: TextView
    private lateinit var pbProfile: RelativeLayout
    private lateinit var tvDate: TextView
    private lateinit var prefs: SharedPreferences
    private lateinit var tvSex: TextView

    private lateinit var settingsBtn: ImageView

    private val userControlFragment =
        UserControlFragment()
    private lateinit var settingsDialogFragment: SettingsDialogFragment


    private lateinit var ivProfile: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsDialogFragment = SettingsDialogFragment()

        profileViewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)
        prefs =
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        prefs.registerOnSharedPreferenceChangeListener(this)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        initViews(root)

        return root

    }

    override fun onResume() {
        super.onResume()
       // setTestScreen()
        manager = childFragmentManager
        pbProfile.visibility - View.GONE
        if (DataBaseHandler.getUserList().size > 1) {
            changeBtn.setImageResource(R.drawable.ic_baseline_swap_horizontal_circle_24)
        } else {
            changeBtn.setImageResource(R.drawable.ic_baseline_add_circle_24)
        }
    }


    private fun initViews(root: View) {
        val user = PreferencesRepository.getUser()

        profileMainLayout = root.findViewById(R.id.profile_main_layout)
        tvName = root.findViewById(R.id.tv_name)
        tvSchool = root.findViewById(R.id.tv_school)
        tvDate = root.findViewById(R.id.tv_date)
        tvSex = root.findViewById(R.id.tv_sex)
        ivProfile = root.findViewById(R.id.iv_profile)
        pbProfile = root.findViewById(R.id.loadingPanel)
        setUserData(user)
        settingsBtn = root.findViewById(R.id.iv_settings)
        settingsBtn.setOnClickListener(this)
        changeBtn = root.findViewById(R.id.iv_change_profile)
        changeBtn.setOnClickListener(this)
        profileMainLayout.setOnClickListener(this)
    }

    private fun setUserData(user: User) {
        tvName.text = user.fullName
        tvSchool.text = user.school
        tvDate.text = user.date
        tvSex.text = user.sex
        if (user.avatar != null)
            ivProfile.setImageBitmap(
                Bitmap.createBitmap(
                    user.avatar!!,
                    0,
                    25,
                    user.avatar!!.width,
                    user.avatar!!.height - 50
                )
            )
        else ivProfile.setImageResource(R.drawable.ic_avatar)
        //setTestScreen()
    }

    fun setTestScreen() {

        tvName.text = "Миннемуллин Наиль"
        tvSchool.text =
            "\tМуниципальное бюджетное общеобразовательное учреждение \"Лицей № 177\" Ново-Савиновского района г. Казани. Казан шәһәре Яңа Савин районының \"177 нче лицей\" гомуми белем бюджет муниципаль учреждениесе"
        tvDate.text = "01.01.2004"
        tvSex.text = "мужской"
        ivProfile.setImageResource(R.drawable.ic_avatar)
    }

    fun refreshUserControlLayout() {
        if (userControlFragment.isAdded) userControlFragment.refresh()

    }

    override fun onClick(v: View?) {
        when (v) {
            changeBtn -> {
                if(!userControlFragment.isVisible) userControlFragment.show(manager, "userControl")
            }
            settingsBtn -> {
                if(!settingsDialogFragment.isVisible)  settingsDialogFragment.show(manager, "settingsFragment")
            }
        }
    }


    fun hideSettings() {
        if (::settingsDialogFragment.isInitialized && settingsDialogFragment.isAdded) settingsDialogFragment.dismiss()
    }

    fun notifyItemClicked(isNewUser: Boolean) {
        if (userControlFragment.isAdded) userControlFragment.dismiss()
        if (isNewUser) {
            pbProfile.visibility = View.VISIBLE
            changeBtn.visibility = View.GONE
            (requireActivity() as MainActivity).showSnackBar("Синхронизация данных. Это может занять некоторое время...")
        } else {
            pbProfile.visibility = View.GONE
            changeBtn.visibility = View.VISIBLE
        }
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {

        when (key) {
            "NAME" -> {
                pbProfile.visibility - View.GONE
                changeBtn.visibility = View.VISIBLE
                setUserData(PreferencesRepository.getUser())
            }
        }
    }


    fun onBackgroundChangedClicked() {
        if(!bgChangeFragment.isVisible) bgChangeFragment.show(manager, "backgroundChangeFragment")

    }

    fun onAdsClicked() {
        (requireActivity() as MainActivity).launchBillingFlow(0)

    }

    fun onCreditsClicked() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://nai1ka.github.io"))
        startActivity(browserIntent)
    }



}
