package ru.ndevelop.educlient.ui.diary

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.tuyenmonkey.mkloader.MKLoader
import ru.ndevelop.educlient.R
import ru.ndevelop.educlient.models.Day
import ru.ndevelop.educlient.models.MessageTypes
import ru.ndevelop.educlient.repositories.PreferencesRepository
import ru.ndevelop.educlient.utils.*
import java.util.*


class DiaryFragment : IDiaryFragment(), SharedPreferences.OnSharedPreferenceChangeListener,
    View.OnClickListener {

    private lateinit var diaryViewModel: DiaryViewModel
    private lateinit var vp: ViewPager2
    private lateinit var prefs: SharedPreferences
    private lateinit var llDiary: ConstraintLayout
    private lateinit var tabLayout: TabLayout
    private lateinit var fab: FloatingActionButton
    private var firstDay: Int = 0
    private var firstMonth: Int = 0
    private var currentYear: Int = 0
    private var lastDay: Int = 0
    private var lastMonth: Int = 0
    private lateinit var llEmptyData: CardView
    private lateinit var tvNoData: TextView
    private lateinit var mkLoadingProgress: MKLoader
    private lateinit var appBar: AppBarLayout
    private lateinit var ivError: ImageView
    private var daysBetween = 0
    private var isNoDataMessageAlreadyClicked = false
    private var currentDate: Calendar = Calendar.getInstance()
    private lateinit var pagerAdapter: ScreenSlidePagerAdapter
    var isDataEmpty = false
    private var isFirstLaunch = true
    private var d =
        OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            currentDate.set(Calendar.YEAR, year)
            currentDate.set(Calendar.MONTH, monthOfYear)
            currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            vp.setCurrentItem(
                Utils.getDaysBetween(
                    Utils.getCalendar(
                        firstDay, firstMonth - 1, currentDate.get(
                            Calendar.YEAR
                        )
                    ), currentDate
                ) / 3, false
            )
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<IDiaryFragment>.onCreate(savedInstanceState)
        prefs =
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        prefs.registerOnSharedPreferenceChangeListener(this)
        pagerAdapter = ScreenSlidePagerAdapter(this)
        isDataEmpty = PreferencesRepository.getDiary() == ""
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_diary, container, false)

        initViews(root)
        if (isDataEmpty) {
            showLoadingStateMessage("Загрузка данных...", LoadingStates.LOADING)
            appBar.visibility = View.GONE
            fab.hide()
            fab.isClickable = false
        }
        vp.offscreenPageLimit = 1
        vp.adapter = pagerAdapter
        initViewModel()
        return root
    }


    private fun initViewModel() {

        diaryViewModel = ViewModelProvider(this).get(DiaryViewModel::class.java)
        diaryViewModel.getDays()
            .observe(
                viewLifecycleOwner,
            ) { payload ->
                if (payload.first.rawArray.size > 0) {
                    hideLoadingStateMessage()
                    if (isDataEmpty) {
                        fab.show()
                        fab.isClickable = true
                    }
                    isDataEmpty = false
                    currentDate = Calendar.getInstance()
                    CurrentDiaryState.additionalSize =
                        payload.first.rawArray.size - payload.first.rawArray.size / 3 * 3
                    pagerAdapter.setItems(payload.first.sublistedArray)
                    appBar.visibility = View.VISIBLE
                    pagerAdapter.disableLoading()
                    if (isFirstLaunch || CurrentDiaryState.isTabLayoutNeedsToBeUpdated) {
                        firstDay = payload.first.rawArray.first().date.toInt()
                        firstMonth = Utils.getMonthNumber(payload.first.rawArray.first().month)
                        lastDay = payload.first.rawArray.last().date.toInt()
                        lastMonth = Utils.getMonthNumber(payload.first.rawArray.last().month)
                        currentYear = currentDate.get(Calendar.YEAR)
                        if (currentDate.get(Calendar.MONTH) + 1 < firstMonth) {
                            currentYear--
                        }
                        daysBetween = Utils.getDaysBetween(
                            Utils.getCalendar(
                                firstDay, firstMonth - 1, currentYear,
                            ),
                            currentDate,
                        )

                        vp.setCurrentItem(daysBetween / 3, false)
                        isFirstLaunch = false
                        attachMediator(payload.first.rawArray)
                        CurrentDiaryState.isTabLayoutNeedsToBeUpdated = false
                    }


                } else {
                    isDataEmpty = true
                    isNoDataMessageAlreadyClicked = false
                    if (!Utils.isNetworkAvailable(requireContext())) {
                        showLoadingStateMessage(
                            "Нет доступа в интернет",
                            LoadingStates.ERROR
                        )
                        Snackbar.make(
                            vp,
                            "Ошибка сети. Проверьте интернет-соединение",
                            Snackbar.LENGTH_LONG,
                        ).show()

                    } else {
                        diaryViewModel.loadDiary()
                    }
                }
            }
        diaryViewModel.error.observe(viewLifecycleOwner) {
            showLoadingStateMessage(
                "Ошибка загрузки данных\nНажмите, чтобы повторить",
                LoadingStates.ERROR
            )
            isNoDataMessageAlreadyClicked = false
            Snackbar.make(
                vp,
                it,
                Snackbar.LENGTH_LONG
            ).show()
            pagerAdapter.disableLoading()
        }
        diaryViewModel.message.observe(viewLifecycleOwner) {
            when (it.type) {
                MessageTypes.FIRST_LAUNCH_SIGNAL -> {
                    Snackbar.make(
                        vp,
                        "Подождите, производится первичная загрузка...",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                MessageTypes.TEXT_MESSAGE -> {

                }
            }
        }
    }


    fun attachMediator(payloadData: ArrayList<Day>) {
        TabLayoutMediator(tabLayout, vp, false) { tab, position ->

            if (position < payloadData.size / 3)
                tab.text =
                    "${payloadData[position * 3].date} ${
                        Utils.getCorrectMonthTitle(
                            payloadData[position * 3].month
                        )
                    } - ${payloadData[position * 3 + 2].date} ${
                        Utils.getCorrectMonthTitle(
                            payloadData[position * 3 + 2].month
                        )
                    }"
            else {
                when (CurrentDiaryState.additionalSize) {
                    1 -> tab.text =
                        "${payloadData[payloadData.size - 1].date} ${
                            Utils.getCorrectMonthTitle(
                                payloadData[payloadData.size - 1].month
                            )
                        } "
                    2 -> tab.text =
                        "${payloadData[payloadData.size - 2].date} ${
                            Utils.getCorrectMonthTitle(
                                payloadData[payloadData.size - 2].month
                            )
                        } - ${payloadData[payloadData.size - 1].date} ${
                            Utils.getCorrectMonthTitle(
                                payloadData[payloadData.size - 1].month
                            )
                        }"
                }
            }

        }.attach()
    }

    private fun initViews(root: View) {
        llEmptyData = root.findViewById(R.id.cv_loading_progress)
        appBar = root.findViewById(R.id.appBarLayout)
        with(llEmptyData) {
            setOnClickListener {
                if (!isNoDataMessageAlreadyClicked) {
                    showLoadingStateMessage("Загрузка данных...", LoadingStates.LOADING)
                    pagerAdapter.enableLoading()
                    diaryViewModel.loadDiary()
                    Snackbar.make(
                        vp,
                        "Подождите, производится первичная загрузка...",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                isNoDataMessageAlreadyClicked = true
            }
        }
        mkLoadingProgress = root.findViewById(R.id.mk_loading_progress)
        ivError = root.findViewById(R.id.iv_error)
        tvNoData = root.findViewById(R.id.tv_loading_progress)
        fab = root.findViewById(R.id.diary_fab)
        fab.setOnClickListener(this)
        vp = root.findViewById(R.id.viewPager2)
        vp.reduceDragSensitivity()
        llDiary = root.findViewById(R.id.diary)
        tabLayout = root.findViewById(R.id.tabs)


    }

   override fun updateDiary() {
        if (!Utils.isNetworkAvailable(requireContext())) {
            showLoadingStateMessage("Нет доступа в интернет", LoadingStates.ERROR)
            Snackbar.make(
                vp,
                "Ошибка сети. Проверьте интернет-соединение",
                Snackbar.LENGTH_LONG
            ).show()
            pagerAdapter.disableLoading()
        } else {
            diaryViewModel.loadDiary()
        }

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "LOGGED_USER_LOGIN" -> {
                pagerAdapter.enableLoading()
                diaryViewModel.loadDiary()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            fab -> {
                val dialog =
                    DatePickerDialog(
                        requireContext(), d,
                        currentDate.get(Calendar.YEAR),
                        currentDate.get(Calendar.MONTH),
                        currentDate.get(Calendar.DAY_OF_MONTH)
                    )
                dialog.datePicker.calendarViewShown = false

                val yearDiff = if (firstMonth > (currentDate.get(Calendar.MONTH)) + 1) 1 else 0
                dialog.datePicker.minDate = Utils.getCalendar(
                    firstDay, firstMonth - 1, currentYear - yearDiff
                ).timeInMillis
                dialog.datePicker.maxDate = Utils.getCalendar(
                    lastDay, lastMonth - 1, currentYear - yearDiff
                ).timeInMillis
                dialog.show()


            }
        }
    }

    override fun setToToday() {
        vp.setCurrentItem(daysBetween / 3, true)
    }

    private inner class ScreenSlidePagerAdapter(f: Fragment) : FragmentStateAdapter(f) {
        private var subListItems: Array<List<Day>> = arrayOf()
        private var isRefreshing: Boolean = true
        private var currentPos = 0
        val fragmentMap: MutableMap<Int, DiaryPageFragment> = mutableMapOf()
        var isFirstLaunch = true
        override fun getItemCount(): Int = subListItems.size
        override fun createFragment(position: Int): Fragment {
            val fragment = DiaryPageFragment.newInstance()
            fragment.setItem(subListItems[position])
            if (!isRefreshing) fragment.disableRefreshing()
            fragmentMap[position] = fragment
            return fragment
        }

        override fun onBindViewHolder(
            holder: FragmentViewHolder,
            position: Int,
            payloads: MutableList<Any>
        ) {
            super.onBindViewHolder(holder, position)
            currentPos = holder.adapterPosition
            if (fragmentMap[holder.adapterPosition] != null) {
                fragmentMap[holder.adapterPosition]!!.setItem(subListItems[holder.adapterPosition])
            }

        }

        fun setItems(newItems: Array<List<Day>>) {
            subListItems = newItems
            notifyDataSetChanged()
            isFirstLaunch = false

        }

        fun disableLoading() {
            fragmentMap.forEach { it.value.disableRefreshing() }
        }

        fun enableLoading() {
            fragmentMap.forEach { it.value.enableRefreshing() }
        }
    }

    override fun showFAB() {
        fab.show()
        fab.isClickable = true
    }

    override fun hideFAB() {
        fab.hide()
        fab.isClickable = false

    }

    private fun showLoadingStateMessage(text: String, states: LoadingStates) {
        if (isDataEmpty)
            when (states) {
                LoadingStates.LOADING -> {
                    tvNoData.text = text
                    mkLoadingProgress.isGone = false
                    ivError.isGone = true
                    llEmptyData.show()
                }
                LoadingStates.ERROR -> {
                    tvNoData.text = text
                    ivError.isGone = false
                    mkLoadingProgress.isGone = true
                    llEmptyData.show()
                }
            }

    }

    private fun hideLoadingStateMessage() {
        llEmptyData.hide()
    }

}
