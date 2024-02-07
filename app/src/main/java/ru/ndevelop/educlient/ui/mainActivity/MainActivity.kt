package ru.ndevelop.educlient.ui.mainActivity


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.preference.PreferenceManager
import com.android.billingclient.api.*
import com.android.billingclient.api.QueryProductDetailsParams.Product
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.yandex.mobile.ads.banner.AdSize
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.ndevelop.educlient.R
import ru.ndevelop.educlient.databinding.ActivityMainBinding
import ru.ndevelop.educlient.repositories.PreferencesRepository
import ru.ndevelop.educlient.ui.LoginActivity
import ru.ndevelop.educlient.ui.dialogs.PermissionDialog
import ru.ndevelop.educlient.ui.diary.DiaryFragment
import ru.ndevelop.educlient.ui.diary.DiaryFragmentOneDayPerPage
import ru.ndevelop.educlient.ui.diary.IDiaryFragment
import ru.ndevelop.educlient.ui.homework.HomeworkFragment
import ru.ndevelop.educlient.ui.profile.ProfileFragment
import ru.ndevelop.educlient.ui.table.TableFragment
import ru.ndevelop.educlient.utils.Utils
import ru.ndevelop.educlient.utils.Utils.getScreenWidth
import ru.ndevelop.educlient.utils.onExitAnswer
import kotlin.system.exitProcess

const val galleryRequestCode = 228

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener,
    PermissionDialogListener, PurchasesUpdatedListener, DialogPageFragmentListener,
    BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var manager: FragmentManager
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var profileFragment = ProfileFragment()
    private lateinit var diaryFragment: IDiaryFragment
    private lateinit var navView: BottomNavigationView
    private lateinit var prefs: SharedPreferences
    private lateinit var billingClient: BillingClient
    private var homeworkFragment = HomeworkFragment()
    private val tableFragment = TableFragment()
    private var isDiaryAdded = false
    var isTableAdded = false
    var isHomeworkAdded = false
    private val bannerAdEventListener = BannerMobileMediationAdEventListener()
    private lateinit var bannerAdView: BannerAdView
    private val productsNames = listOf("disable_banner")
    private var listOfAvailableProducts: List<ProductDetails> = listOf()

    private lateinit var binding: ActivityMainBinding

    init {
        diaryFragment = if (PreferencesRepository.getIsSingleDayMode())
            DiaryFragmentOneDayPerPage()
        else DiaryFragment()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        val theme = PreferencesRepository.getTheme()
        val themesArray = Utils.getThemesArray()
        if (theme in themesArray) setTheme(PreferencesRepository.getTheme())
        else setTheme(R.style.KiwiTheme)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        initBanner()
        initBilling()
        prefs.registerOnSharedPreferenceChangeListener(this)
        navView = findViewById(R.id.nav_view)
        manager = supportFragmentManager
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                openGallery()
            } else {
                val fm: FragmentManager = supportFragmentManager
                val permissionDiaryFragment = PermissionDialog(
                    "Не удалось открыть галерею",
                    "Для этого необходимо разрешение на чтение файлов",
                    R.drawable.ic_error,
                    PermissionDialogTypes.GALLERY,
                    this
                )
                permissionDiaryFragment.show(fm, "permissionDialog")
            }
        }

        val tempSelectedFragment =
            savedInstanceState?.getInt("opened_fragment", R.id.navigation_diary)
                ?: when (prefs.getString("START", "Diary")) {
                    "Profile" -> R.id.navigation_profile
                    "Diary" -> R.id.navigation_diary
                    "Homework" -> R.id.navigation_homework
                    "Table" -> R.id.navigation_table
                    else -> R.id.navigation_diary
                }
        when (tempSelectedFragment) {
            R.id.navigation_profile -> {
                manager.beginTransaction().replace(R.id.main_layout, profileFragment).commit()
                navView.selectedItemId = R.id.navigation_profile
            }

            R.id.navigation_diary -> {
                manager.beginTransaction().replace(R.id.main_layout, diaryFragment).commit()
                navView.selectedItemId = R.id.navigation_diary
                isDiaryAdded = true

            }

            R.id.navigation_homework -> {
                manager.beginTransaction().replace(R.id.main_layout, homeworkFragment).commit()
                navView.selectedItemId = R.id.navigation_homework
                isHomeworkAdded = true

            }

            R.id.navigation_table -> {
                manager.beginTransaction().replace(R.id.main_layout, tableFragment).commit()
                navView.selectedItemId = R.id.navigation_table
                isTableAdded = true

            }
        }

        when (PreferencesRepository.getBackgroundType()) {
            Background.STANDART -> {
                binding.ivBackground.setImageResource(R.drawable.bg_default)
            }

            Background.WHITE -> binding.ivBackground.setImageResource(R.drawable.bg_white)
            Background.CUSTOM -> {

                val tempBackgroundString = PreferencesRepository.getBackgroundRaw()
                if (tempBackgroundString != "") {

                    val byteArray: ByteArray = Base64.decode(tempBackgroundString, Base64.DEFAULT)

                    val resultBitmap = BitmapFactory.decodeByteArray(
                        byteArray, 0, byteArray.size
                    )
                    setBackground(
                        resultBitmap
                    )
                }
            }
        }

        navView.setOnNavigationItemSelectedListener(this)
        navView.setOnNavigationItemReselectedListener {
            if (homeworkFragment.isAdded) homeworkFragment.setToToday()
            if (diaryFragment.isVisible) diaryFragment.setToToday()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("opened_fragment", navView.selectedItemId)
        super.onSaveInstanceState(outState)
    }

    private fun initBilling() {
        billingClient =
            BillingClient.newBuilder(this).setListener(this).enablePendingPurchases().build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    //getProductAvailableForBuying()
                    getAvailableProducts()
                    // This is used to fetch purchased items from Google Play Store
                    getPurchasedProducts()
                } else {
                    loadBannerAd()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    private fun getPurchasedProducts() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { _, purchaseList ->
            var areAdsDisabled = true
            if (purchaseList.isEmpty()) {
                areAdsDisabled = false
            } else {
                var isPurchaseFound = false
                purchaseList.forEach {
                    when (it.products[0]) {
                        productsNames[0] -> {
                            isPurchaseFound = true
                            areAdsDisabled = true
                        }

                    }
                }
                if (!isPurchaseFound) areAdsDisabled = false
            }

            if (areAdsDisabled) makeBannerGone()
            else {
                runOnUiThread {
                    loadBannerAd()
                }
            }
        }
    }

    fun getAvailableProducts() {
        val productList: ArrayList<Product> = arrayListOf()
        productsNames.forEach {
            productList.add(
                Product.newBuilder().setProductId("disable_banner")
                    .setProductType(BillingClient.ProductType.INAPP).build()
            )
        }
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList)
        billingClient.queryProductDetailsAsync(params.build()) { _, productDetailsList ->
            listOfAvailableProducts = productDetailsList

        }

    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult, purchaseList: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                for (purchase in purchaseList!!) {
                    Toast.makeText(
                        this, "Спасибо за покупку! Баннер отключён навсегда :)", Toast.LENGTH_SHORT
                    ).show()
                    makeBannerGone()
                    acknowledgePurchase(purchase.purchaseToken)
                }
            }
        }
    }


    private fun acknowledgePurchase(purchaseToken: String) {
        val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchaseToken).build()
        billingClient.acknowledgePurchase(params) {}
    }


    fun launchBillingFlow(productIndex: Int) {
        if (listOfAvailableProducts.isEmpty()) {
            Toast.makeText(
                this,
                "Ошибка покупки. Проверьте, что Ваша версия Google Play актуальна",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(listOfAvailableProducts[productIndex])

                .build()
        )

        val billingFlowParams =
            BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList)
                .build()

        billingClient.launchBillingFlow(this, billingFlowParams)

    }

    private fun makeBannerGone() {
        binding.adViewLayout.visibility = View.GONE
    }

    fun onExitClicked(view: View) {
        notifyItemClicked(true)
        CoroutineScope(Dispatchers.Default).launch {
            val exitAnswer = Utils.onUserExit(PreferencesRepository.getCurrentGlobalUser().login)
            withContext(Dispatchers.Main) {
                when (exitAnswer) {
                    onExitAnswer.EMPTY_USER_LIST -> {

                        val i = Intent(this@MainActivity, LoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        this@MainActivity.finish()
                        startActivity(i)
                    }

                    onExitAnswer.USER_UPDATED_SUCCESFULLY -> {
                        profileFragment.refreshUserControlLayout()
                    }

                    onExitAnswer.ERROR -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Не удалось выйти из аккаунта. Проверьте интернет-соединение",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }
                notifyItemClicked(false)
            }

        }
    }

    private val IMAGE_PICK_CODE = 1000
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "THEME" -> {
                when (sharedPreferences?.getString("THEME", "Kiwi")) {
                    "Avocado" -> {
                        PreferencesRepository.setTheme(R.style.AvocadoTheme)
                        profileFragment.hideSettings()
                        recreate()
                    }

                    "Purple" -> {
                        PreferencesRepository.setTheme(R.style.PurpleTheme)
                        profileFragment.hideSettings()
                        recreate()
                    }

                    "Orange" -> {
                        PreferencesRepository.setTheme(R.style.OrangeTheme)
                        profileFragment.hideSettings()
                        recreate()
                    }

                    "Blue" -> {
                        PreferencesRepository.setTheme(R.style.BlueTheme)
                        profileFragment.hideSettings()
                        recreate()
                    }

                    "Kiwi" -> {
                        PreferencesRepository.setTheme(R.style.KiwiTheme)
                        if (!PreferencesRepository.isFirstLaunch()) {
                            profileFragment.hideSettings()
                            recreate()
                        }
                    }

                    "Strawberry" -> {
                        PreferencesRepository.setTheme(R.style.StrawberryTheme)
                        profileFragment.hideSettings()
                        recreate()
                    }

                    "Watermelon" -> {
                        PreferencesRepository.setTheme(R.style.WatermelonTheme)
                        profileFragment.hideSettings()
                        recreate()
                    }

                    else -> {
                        PreferencesRepository.setTheme(R.style.KiwiTheme)
                        profileFragment.hideSettings()
                        recreate()
                    }

                }
                PreferencesRepository.setIsNotFirstLaunch()
            }

            "ONLY_ONE_DAY_DIARY" -> {
                diaryFragment = if (PreferencesRepository.getIsSingleDayMode())
                    DiaryFragmentOneDayPerPage()
                else DiaryFragment()
                val transaction = manager.beginTransaction()
               transaction.remove(diaryFragment)
                transaction.add(R.id.main_layout,diaryFragment).hide(diaryFragment)
                transaction.commit()




            }


        }
    }

    private fun requestGalleryPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                openGallery()
            }

            else -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                } else {
                    requestPermissionLauncher.launch(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                }

            }
        }

    }

    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_PICK_CODE)
    }


    private fun exitApp() {
        finishAffinity()
        exitProcess(1)
    }

    private var doubleBackToExitPressedOnce = false

    override fun onBackPressed() {
        when (tableFragment.isVisible) {
            true -> {
                if (!tableFragment.isFABOpen) handleAppExit()
                else tableFragment.closeFABMenu()
            }

            false -> handleAppExit()
        }
    }

    private fun handleAppExit() {
        if (doubleBackToExitPressedOnce) {
            exitApp()
        }
        this.doubleBackToExitPressedOnce = true

        Toast.makeText(this, "Нажмите ещё раз для выхода", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            val uri = data?.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                setBackground(bitmap)
                PreferencesRepository.saveBackground(bitmap)
                PreferencesRepository.setBackgroundType(Background.CUSTOM)

            } catch (e: Exception) {
                Toast.makeText(this, "Ошибки загрузки. Попробуйте ещё раз", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun setBackground(bitmap: Bitmap) {
        try {
            binding.ivBackground.setImageBitmap(bitmap)
        } catch (e: RuntimeException) {
            Toast.makeText(this, "Неподходящая картинка. Выберите другую", Toast.LENGTH_SHORT)
                .show()
        }
    }


    fun openLoginActivity() {
        val i = Intent(this, LoginActivity::class.java)
        startActivity(i)
    }


    fun updateDiary() {
        diaryFragment.updateDiary()
    }

    fun changeBg(bg: Background) {
        when (bg) {
            Background.STANDART -> {
                binding.ivBackground.setImageResource(R.drawable.bg_default)
                PreferencesRepository.setBackgroundType(Background.STANDART)
            }

            Background.WHITE -> {
                binding.ivBackground.setImageResource(R.drawable.bg_white)
                PreferencesRepository.setBackgroundType(Background.WHITE)
            }

            Background.CUSTOM -> requestGalleryPermission()
        }

    }


    fun notifyItemClicked(isNewUser: Boolean) {
        profileFragment.notifyItemClicked(isNewUser)
    }


    override fun onPermissionOkButtonClicked(mode: PermissionDialogTypes) {
        when (mode) {
            PermissionDialogTypes.GALLERY -> {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), galleryRequestCode
                )
            }
        }
    }

    override fun onFABHide() {
        diaryFragment.hideFAB()
    }

    override fun onFABShow() {
        diaryFragment.showFAB()
    }

    fun showSnackBar(text: String) {
        val snackbar = Snackbar.make(findViewById(R.id.main_layout), text, Snackbar.LENGTH_SHORT)
        snackbar.anchorView = navView
        snackbar.show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_profile -> {

                val transaction = manager.beginTransaction()
                transaction.hide(diaryFragment).hide(tableFragment).hide(homeworkFragment)
                    .add(R.id.main_layout, profileFragment).commit()
                return true
            }

            R.id.navigation_diary -> {

                val transaction = manager.beginTransaction()
                transaction.remove(profileFragment).hide(tableFragment).hide(homeworkFragment)
                if (!isDiaryAdded) {
                    transaction.add(R.id.main_layout, diaryFragment)
                    isDiaryAdded = true
                }
                transaction.show(diaryFragment)
                transaction.commit()
                return true
            }

            R.id.navigation_homework -> {

                val transaction = manager.beginTransaction()
                transaction.remove(profileFragment).hide(diaryFragment).hide(tableFragment)
                if (!isHomeworkAdded) {
                    transaction.add(R.id.main_layout, homeworkFragment)
                    isHomeworkAdded = true

                }
                transaction.show(homeworkFragment)
                transaction.commit()
                return true
            }

            R.id.navigation_table -> {

                val transaction = manager.beginTransaction()
                transaction.remove(profileFragment).hide(diaryFragment).hide(homeworkFragment)
                if (!isTableAdded) {
                    transaction.add(R.id.main_layout, tableFragment)
                    isTableAdded = true

                }
                transaction.show(tableFragment)
                transaction.commit()
                return true
            }

        }
        return false
    }

    private inner class BannerMobileMediationAdEventListener : BannerAdEventListener {

        override fun onAdLoaded() {
            binding.adViewLayout.visibility = View.VISIBLE
        }

        override fun onAdFailedToLoad(error: AdRequestError) {
            Log.e("ERROR", error.description)
        }

        override fun onAdClicked() {

        }

        override fun onImpression(impressionData: ImpressionData?) {

        }

        override fun onLeftApplication() {

        }

        override fun onReturnedToApplication() {

        }
    }

    private fun initBanner() {
       // binding.adView.setAdUnitId("R-M-1995623-1")
       // binding.adView.setAdSize(AdSize.stickySize(this, getScreenWidth(this)))
       // binding.adView.setBannerAdEventListener(bannerAdEventListener)
    }

    private fun loadBannerAd() {
      //  binding.adView.loadAd(com.yandex.mobile.ads.common.AdRequest.Builder().build())
    }


}


enum class Background {
    STANDART, WHITE, CUSTOM
}

enum class PermissionDialogTypes {
    GALLERY
}

interface PermissionDialogListener {
    fun onPermissionOkButtonClicked(mode: PermissionDialogTypes)
}

interface DialogPageFragmentListener {
    fun onFABShow()
    fun onFABHide()
}

