package ru.ndevelop.educlient.ui.table

import android.animation.Animator
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.tuyenmonkey.mkloader.MKLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.ndevelop.educlient.R
import ru.ndevelop.educlient.models.MessageTypes
import ru.ndevelop.educlient.models.TableItem
import ru.ndevelop.educlient.repositories.PreferencesRepository
import ru.ndevelop.educlient.ui.adapters.TableAdapter
import ru.ndevelop.educlient.ui.calculators.CalculateFragment
import ru.ndevelop.educlient.ui.calculators.VirtualCalculatorFragment
import ru.ndevelop.educlient.ui.dialogs.QuarterDialog
import ru.ndevelop.educlient.utils.*


class TableFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener,
    View.OnClickListener {
    private lateinit var manager: FragmentManager
    private lateinit var tableViewModel: TableViewModel
    private lateinit var tableAdapter: TableAdapter
    private lateinit var bottomSheet: View
    private lateinit var rvTable: RecyclerView
    private lateinit var prefs: SharedPreferences
    private lateinit var fragmentLayout: CardView
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var frTable: View
    private lateinit var tvNewMarksOwnerName:TextView
    private lateinit var tvNewMarks: TextView
    var isFABOpen = false
    private lateinit var llEmptyData: CardView
    private lateinit var tvNoData: TextView
    private lateinit var mkLoadingProgress: MKLoader
    private lateinit var ivError:ImageView
    private var userLogin = ""
    private lateinit var fabLayout1: LinearLayout
    private lateinit var fabLayout2: LinearLayout
    private lateinit var fabLayout3: LinearLayout
    private lateinit var fab1: FloatingActionButton
    private lateinit var fab2: FloatingActionButton
    private lateinit var fab3: FloatingActionButton
    private var isDataEmpty = false

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs =
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        prefs.registerOnSharedPreferenceChangeListener(this)
        manager = childFragmentManager
        userLogin = PreferencesRepository.getCurrentGlobalUser().login
        isDataEmpty = DataBaseHandler.isTableEmpty(userLogin)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_table, container, false)

        initViews(root)
        if (isDataEmpty) {
            floatingActionButton.visibility =
                View.GONE
            llEmptyData.show()
        }
        swipeRefreshLayout.isRefreshing = true
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        floatingActionButton.isClickable = true
                        floatingActionButton.visibility = View.VISIBLE
                        frTable.visibility = View.GONE
                        if (isFABOpen) closeFABMenu()
                        floatingActionButton.animate().scaleX(1F).scaleY(1F).setDuration(300)
                            .start()
                    }
                    else -> {
                        frTable.visibility = View.VISIBLE
                        floatingActionButton.visibility = View.GONE
                        floatingActionButton.isClickable = false
                        floatingActionButton.animate().scaleX(0F).scaleY(0F).setDuration(0).start()
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })


        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
       // testBottomSheet()
        initViewModel()
        return root
    }


    fun testBottomSheet(
    ) {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        tvNewMarks.text = "Биология: 5 \nФизическая культура: 5, 4"
        floatingActionButton.visibility = View.GONE
        floatingActionButton.isClickable = false
        frTable.visibility = View.VISIBLE
        floatingActionButton.animate().scaleX(0F).scaleY(0F).setDuration(0).start()
    }

    private fun initViewModel() {
        tableViewModel = ViewModelProvider(this).get(TableViewModel::class.java)
        tableViewModel.table.observe(viewLifecycleOwner) {
            hideLoadingStateMessage()
            isDataEmpty = false
            val oldTable = DataBaseHandler.getTable(userLogin)
            tableAdapter.loadItems(it.first.tableArray)
            if (!it.second) swipeRefreshLayout.isRefreshing = false  //If not first launch
            if (floatingActionButton.visibility == View.GONE) floatingActionButton.visibility =
                View.VISIBLE
            if (oldTable.userLogin == it.first.userLogin) findNewMarks(
                it.first.tableArray,
                oldTable.tableArray
            )

        }
        tableViewModel.error.observe(viewLifecycleOwner) {
            showLoadingStateMessage("Ошибка загрузки данных", LoadingStates.ERROR)
            Snackbar.make(
                swipeRefreshLayout,
                it,
                Snackbar.LENGTH_SHORT
            ).show()
            NetworkHelper.clearCookies()
            swipeRefreshLayout.isRefreshing = false


        }
        tableViewModel.message.observe(viewLifecycleOwner) {
            when (it.type) {
                MessageTypes.FIRST_LAUNCH_SIGNAL -> {

                    Snackbar.make(
                        swipeRefreshLayout,
                        "Подождите, производится первичная загрузка...",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                MessageTypes.TEXT_MESSAGE -> {
                }
            }


        }
    }

    override fun onStop() {
        super.onStop()
        closeFABMenu()
    }


    private fun initViews(root: View) {
        tableAdapter = TableAdapter(requireContext())
        llEmptyData = root.findViewById(R.id.cv_loading_progress)
        tvNoData = root.findViewById(R.id.tv_loading_progress)
        mkLoadingProgress = root.findViewById(R.id.mk_loading_progress)
        ivError = root.findViewById(R.id.iv_error)
        this.swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout)
        bottomSheet = root.findViewById(R.id.bottom_sheet)
        tvNewMarksOwnerName = root.findViewById(R.id.tv_new_marks_name)
        frTable = root.findViewById(R.id.fr_table)
        frTable.setOnClickListener(this)
        floatingActionButton = root.findViewById(R.id.floatingActionButton)
        fabLayout1 = root.findViewById(R.id.fabLayout1)
        fabLayout2 = root.findViewById(R.id.fabLayout2)
        fabLayout3 = root.findViewById(R.id.fabLayout3)
        fab1 = root.findViewById(R.id.fab1)
        fab2 = root.findViewById(R.id.fab2)
        fab3 = root.findViewById(R.id.fab3)
        fab1.setOnClickListener(this)
        fab2.setOnClickListener(this)
        fab3.setOnClickListener(this)
        tvNewMarks = root.findViewById(R.id.tv_newMarks)
        fragmentLayout = root.findViewById(R.id.cv_table)
        floatingActionButton.setOnClickListener(this)
        swipeRefreshLayout.setOnRefreshListener {
            if (!Utils.isNetworkAvailable(requireContext())) {
                showLoadingStateMessage("Нет доступа в интернет", LoadingStates.ERROR)
                Snackbar.make(
                    swipeRefreshLayout,
                    "Ошибка сети. Проверьте интернет-соединение",
                    Snackbar.LENGTH_LONG
                ).show()
                swipeRefreshLayout.isRefreshing = false
            } else {
                showLoadingStateMessage("Загрузка данных...", LoadingStates.LOADING)
                tableViewModel.loadTable()
            }

        }
        rvTable = root.findViewById(
            R.id.rv_table
        )
        with(rvTable) {
            adapter = tableAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        }
        rvTable.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && floatingActionButton.visibility == View.VISIBLE) {
                    floatingActionButton.hide()
                    floatingActionButton.isClickable = false
                } else if (dy < 0 && floatingActionButton.visibility != View.VISIBLE) {
                    floatingActionButton.show()
                    floatingActionButton.isClickable = true

                }
            }
        })
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "LOGGED_USER_LOGIN" -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                userLogin = PreferencesRepository.getCurrentGlobalUser().login
                swipeRefreshLayout.isRefreshing = true
                showLoadingStateMessage("Загрузка данных...", LoadingStates.LOADING)
                if (!DataBaseHandler.isTableEmpty(userLogin)) {
                    tableAdapter.loadItems(
                        DataBaseHandler.getTable(
                            userLogin
                        ).tableArray
                    )
                }
                tableViewModel.loadTable()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            frTable -> {
                if (isFABOpen) closeFABMenu()

            }
            floatingActionButton -> {
                if (!isFABOpen) {
                    showFABMenu()
                } else {
                    closeFABMenu()
                }
            }
            fab1 -> {
                val fm: FragmentManager = childFragmentManager
                val virtualCalculatorFragment =
                    VirtualCalculatorFragment()
                virtualCalculatorFragment.show(fm, "virtualCalculateFragment")
            }
            fab2 -> {
                val fm: FragmentManager = childFragmentManager
                val calculateFragment =
                    CalculateFragment()
                calculateFragment.show(fm, "calculateFragment")
            }

            fab3 -> {
                val fm: FragmentManager = childFragmentManager
                val quarterFragment =
                    QuarterDialog()
                quarterFragment.show(fm, "quarterFragment")
            }
            rvTable -> {
                fragmentLayout.visibility = View.GONE
            }
        }
    }


    private fun findNewMarks(newDiary: ArrayList<TableItem>, oldDiary: ArrayList<TableItem>) {
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        CoroutineScope(Dispatchers.Default).launch {
            try {
                var resultString = ""
                val resultArrayList = Utils.getDifferenceBetweenTwoTables(oldDiary, newDiary)
                 //val resultArrayList = Utils.testDIff()
                resultArrayList.forEach { resultString += "${it.lessonName}: ${it.resultMarks.joinToString()} \n" }
                if (resultArrayList.size > 0) {
                    withContext(Dispatchers.Main) {

                        if (DataBaseHandler.getUserList().size>1){
                            val fullName = PreferencesRepository.getUser().fullName.split(" ")
                            tvNewMarksOwnerName.visibility = View.VISIBLE
                            tvNewMarksOwnerName.text = "Пользователь: ${fullName[0]} ${fullName[1]}"
                        }
                        else{
                            tvNewMarksOwnerName.visibility = View.GONE
                        }
                        floatingActionButton.visibility = View.GONE
                        floatingActionButton.isClickable = false
                        if (isFABOpen) closeFABMenu()
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        tvNewMarks.text = resultString

                    }
                }
            } catch (e: Exception) {
                Log.e("ERROR", e.toString())
            }
        }


    }

    private fun showFABMenu() {
        isFABOpen = true
        fabLayout1.visibility = View.VISIBLE
        fabLayout2.visibility = View.VISIBLE
        fabLayout3.visibility = View.VISIBLE
        frTable.visibility = View.VISIBLE
        floatingActionButton.animate().rotationBy(180f)
        fabLayout1.animate().translationY(-resources.getDimension(R.dimen.standard_55))
        fabLayout2.animate().translationY(-resources.getDimension(R.dimen.standard_120))
        fabLayout3.animate().translationY(-resources.getDimension(R.dimen.standard_185))

    }

    fun closeFABMenu() {
        isFABOpen = false
        fabLayout1.visibility = View.GONE
        fabLayout2.visibility = View.GONE
        fabLayout3.visibility = View.GONE
        frTable.visibility = View.GONE
        floatingActionButton.animate().rotation(0f)
        fabLayout1.animate().translationY(0f)
        fabLayout2.animate().translationY(0f)
        fabLayout3.animate().translationY(0f).setListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}
            override fun onAnimationEnd(animator: Animator) {
                if (!isFABOpen) {
                    fabLayout1.visibility = View.GONE
                    fabLayout2.visibility = View.GONE
                    fabLayout3.visibility = View.GONE
                }
            }

            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
    }

    private fun showLoadingStateMessage(text: String, states: LoadingStates) {
        if(isDataEmpty)
            when(states){
                LoadingStates.LOADING->{
                    tvNoData.text = text
                    mkLoadingProgress.isGone = false
                    ivError.isGone = true
                    llEmptyData.show()
                }
                LoadingStates.ERROR->{
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
