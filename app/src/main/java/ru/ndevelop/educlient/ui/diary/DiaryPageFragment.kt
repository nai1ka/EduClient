package ru.ndevelop.educlient.ui.diary


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ru.ndevelop.educlient.App
import ru.ndevelop.educlient.R
import ru.ndevelop.educlient.models.Day
import ru.ndevelop.educlient.ui.adapters.DiaryAdapter
import ru.ndevelop.educlient.ui.mainActivity.DialogPageFragmentListener
import ru.ndevelop.educlient.ui.mainActivity.MainActivity
import ru.ndevelop.educlient.utils.CurrentDiaryState
import ru.ndevelop.educlient.utils.Utils


class DiaryPageFragment : Fragment() {
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var recyclerView: RecyclerView
    private val diaryAdapter: DiaryAdapter = DiaryAdapter(App.applicationContext())
    private lateinit var dialogPageFragmentListener: DialogPageFragmentListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.single_page, container, false)
        initViews(root)
        with(recyclerView) {
            adapter = diaryAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        }
        return root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dialogPageFragmentListener = context as DialogPageFragmentListener
    }

    fun initViews(root: View) {
        recyclerView = root.findViewById(R.id.rv_diary_list_page)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    dialogPageFragmentListener.onFABHide()

                } else if (dy < 0) {
                    dialogPageFragmentListener.onFABShow()
                }
            }
        })
        swipeRefreshLayout = root.findViewById(R.id.swipeRefresh)
        if (CurrentDiaryState.isFirstLaunch) {
            swipeRefreshLayout.isRefreshing = true
            CurrentDiaryState.isFirstLaunch = false
        }
        if (!Utils.isNetworkAvailable(App.applicationContext())) swipeRefreshLayout.isRefreshing =
            false
        swipeRefreshLayout.setOnRefreshListener {
            (requireActivity() as MainActivity).updateDiary()
        }
    }

    fun setItem(payload: List<Day>) {
        disableRefreshing()
        diaryAdapter.setData(payload)
    }

    fun setItem(payload: Day) {
        disableRefreshing()
        diaryAdapter.setData(listOf(payload))
    }

    override fun onPause() {
        super.onPause()
        swipeRefreshLayout.isRefreshing = false
    }


    companion object {

        @JvmStatic
        fun newInstance() =
            DiaryPageFragment().apply {
                arguments = Bundle().apply {
                    // putInt(ARG_PARAM1, position)
                }
            }
    }

    fun disableRefreshing() {
        if (::swipeRefreshLayout.isInitialized) swipeRefreshLayout.isRefreshing = false
    }

    fun enableRefreshing() {
        if (::swipeRefreshLayout.isInitialized) swipeRefreshLayout.isRefreshing = true
    }
}
