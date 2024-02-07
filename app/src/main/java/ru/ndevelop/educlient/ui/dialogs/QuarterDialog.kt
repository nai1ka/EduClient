package ru.ndevelop.educlient.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.ndevelop.educlient.R
import ru.ndevelop.educlient.models.QuarterItem
import ru.ndevelop.educlient.models.QuarterTable
import ru.ndevelop.educlient.ui.adapters.QuarterAdapter
import ru.ndevelop.educlient.utils.NetworkHelper
import ru.ndevelop.educlient.utils.ParserHelper
import ru.ndevelop.educlient.utils.Utils
import java.lang.Exception


class QuarterDialog : DialogFragment() {

    private var fullTableItemsArrayList: ArrayList<QuarterItem> = arrayListOf()
    private lateinit var loadingLinearLayout: LinearLayout
    private lateinit var llQuarter: LinearLayout
    private lateinit var llNoData: LinearLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var quarterPagerAdapter: QuarterPagerAdapter
    private var quarterTable: QuarterTable? = null
    private lateinit var quarterTabs: TabLayout

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        quarterPagerAdapter = QuarterPagerAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_quarter, container, false)

        initViews(root)
        CoroutineScope(Dispatchers.Default).launch {
            var isError = false
            try {
                val requestResult = NetworkHelper.getQuarterMarks()
                if (requestResult.second == null) {
                    quarterTable = requestResult.first
                    if (quarterTable != null)
                        fullTableItemsArrayList = ParserHelper.parseQuarter(
                            quarterTable!!.table!!
                        )
                }
            } catch (e: Exception) {
                NetworkHelper.clearCookies()
                isError = true
            }

            withContext(Dispatchers.Main) {
                if (fullTableItemsArrayList.isNotEmpty() && quarterTable != null) {
                    quarterPagerAdapter.notifyDataSetChanged()
                    loadingLinearLayout.visibility = View.GONE
                    llQuarter.visibility = View.VISIBLE
                    TabLayoutMediator(quarterTabs, viewPager, true) { tab, position ->
                        val tabTitles = quarterTable!!.tableTypeItems
                        tab.text = getTabTitleText(tabTitles[position])
                        viewPager.setCurrentItem(0, true)
                    }.attach()
                } else if (isError || fullTableItemsArrayList.isEmpty()) {
                    loadingLinearLayout.visibility = View.GONE
                    llNoData.visibility = View.VISIBLE

                }
            }
        }
        return root

    }

    fun getTabTitleText(rawText: String): String {
        return if (arrayOf('1', '2', '3', '4').contains(rawText[0])) {
            Utils.getNumberOfQuarter(rawText[0])
        } else rawText
    }

    override fun onResume() {
        super.onResume()
        val params: ViewGroup.LayoutParams = dialog!!.window!!.attributes
        val windowParams = Utils.getScreenSize(requireContext())
        params.width = windowParams.first - 140
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog?.window?.attributes = params as WindowManager.LayoutParams
        dialog?.window?.setBackgroundDrawable(null)
    }

    private fun initViews(r: View) {
        quarterTabs = r.findViewById(R.id.quarter_tabs)
        loadingLinearLayout = r.findViewById(R.id.ll_loading_quarter)
        viewPager = r.findViewById(R.id.vp_quarter)
        viewPager.adapter = quarterPagerAdapter
        llQuarter = r.findViewById(R.id.ll_quarter)
        llNoData = r.findViewById(R.id.ll_nodata_quarter)
    }


    inner class QuarterPagerAdapter : RecyclerView.Adapter<PagerVH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerVH =
            PagerVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_quarter,
                    parent,
                    false
                )
            )

        override fun getItemCount(): Int =
            if (quarterTable != null) quarterTable!!.tableTypeItems.size
            else 0

        override fun onBindViewHolder(holder: PagerVH, position: Int) = holder.itemView.run {
            holder.bind(fullTableItemsArrayList, position)
        }
    }

    inner class PagerVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rvQuarter: RecyclerView = itemView.findViewById(R.id.rv_quarter)
        private val quarterAdapter = QuarterAdapter()
        fun bind(payload: ArrayList<QuarterItem>, position: Int) {
            quarterAdapter.loadItems(payload, position)
            with(rvQuarter) {
                adapter = quarterAdapter
                layoutManager = LinearLayoutManager(context)
            }
        }
    }


}