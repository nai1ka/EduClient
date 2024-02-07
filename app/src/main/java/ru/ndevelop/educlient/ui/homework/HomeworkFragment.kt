package ru.ndevelop.educlient.ui.homework

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.tuyenmonkey.mkloader.MKLoader
import ru.ndevelop.educlient.R
import ru.ndevelop.educlient.models.MessageTypes
import ru.ndevelop.educlient.ui.adapters.HomeworkAdapter
import ru.ndevelop.educlient.utils.*
import java.util.*

class HomeworkFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var homeworkViewModel: HomeworkViewModel
    private lateinit var homeworkAdapter: HomeworkAdapter
    private lateinit var rvTasks: RecyclerView
    private lateinit var prefs: SharedPreferences
    var isDataEmpty = false
    private lateinit var llEmptyData: CardView
    private lateinit var tvNoData: TextView
    private lateinit var mkLoadingProgress: MKLoader
    private lateinit var ivError:ImageView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var currentDate: Calendar = Calendar.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs =
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        prefs.registerOnSharedPreferenceChangeListener(this)
        isDataEmpty = DataBaseHandler.isHomeworkTableEmpty()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_homework, container, false)
        initViews(root)
        if (isDataEmpty) llEmptyData.show()
        initViewModel()
        return root
    }

    override fun onPause() {
        super.onPause()
        swipeRefreshLayout.isRefreshing = false
    }

    private fun initViewModel() {
        var errorOccurTimes = 0
        homeworkViewModel = ViewModelProvider(this).get(HomeworkViewModel::class.java)
        homeworkViewModel.homeworks.observe(viewLifecycleOwner) { payload ->

            if (payload.first.isEmpty()) {
                isDataEmpty = true
                homeworkAdapter.clearHomeworks()
                showLoadingStateMessage("Нет данных", LoadingStates.ERROR)

            } else {
                isDataEmpty = false
                hideLoadingStateMessage()
                homeworkAdapter.loadHomework(payload.first)
                if (payload.second) {
                    if (currentDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || (currentDate.get(
                            Calendar.DAY_OF_MONTH
                        ) < payload.first[0].date && currentDate.get(Calendar.MONTH) + 1 == Utils.getMonthNumber(
                            payload.first[0].month
                        )) || (currentDate.get(Calendar.MONTH) + 1 != Utils.getMonthNumber(
                            payload.first[0].month
                        ))
                    ) rvTasks.scrollToPosition(
                        0
                    )
                    else rvTasks.scrollToPosition(1)

                }
            }
            swipeRefreshLayout.isRefreshing = false

        }
        homeworkViewModel.error.observe(viewLifecycleOwner) {
            errorOccurTimes += 1
            if (it != "") {
                showLoadingStateMessage(
                    "Ошибка загрузки данных",
                    LoadingStates.ERROR
                )
                Snackbar.make(
                    rvTasks,
                    it,
                    Snackbar.LENGTH_LONG
                ).show()


            }
            swipeRefreshLayout.isRefreshing = false
        }
        homeworkViewModel.message.observe(viewLifecycleOwner) {
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

    private fun initViews(root: View) {
        homeworkAdapter = HomeworkAdapter(requireContext())
        llEmptyData = root.findViewById(R.id.cv_loading_progress)
        tvNoData = root.findViewById(R.id.tv_loading_progress)
        mkLoadingProgress = root.findViewById(R.id.mk_loading_progress)
        ivError = root.findViewById(R.id.iv_error)
        swipeRefreshLayout = root.findViewById(R.id.homework_swipeRefreshLayout)
        swipeRefreshLayout.isRefreshing = true
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
                homeworkViewModel.loadHomeworks()
            }
        }
        rvTasks = root.findViewById(
            R.id.rv_homework
        )
        with(rvTasks) {
            adapter = homeworkAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        }
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "LOGGED_USER_LOGIN" -> {
                DataBaseHandler.clearHomeworks()
                swipeRefreshLayout.isRefreshing = true
                showLoadingStateMessage(
                    "Загрузка данных\nПожалуйста, подождите...",
                   LoadingStates.LOADING
                )
                homeworkViewModel.loadHomeworks()

            }
        }
    }

    fun setToToday() {
        rvTasks.smoothScrollToPosition(0)
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
