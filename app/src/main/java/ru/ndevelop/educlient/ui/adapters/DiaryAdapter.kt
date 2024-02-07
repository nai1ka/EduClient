package ru.ndevelop.educlient.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import ru.ndevelop.educlient.R
import ru.ndevelop.educlient.databinding.SingleDiaryBinding
import ru.ndevelop.educlient.models.Day
import ru.ndevelop.educlient.ui.custom_ui.CustomLinearLayoutManager
import ru.ndevelop.educlient.utils.Utils
import java.util.*


class DiaryAdapter(val mContext: Context) : RecyclerView.Adapter<DiaryAdapter.SingleViewHolder>() {
    private var items: List<Day> = listOf()
    inner class SingleViewHolder(private val binding: SingleDiaryBinding) : RecyclerView.ViewHolder(binding.root) {
        private val tvWeekDay = binding.tvDayWeekday
        private val recyclerView:RecyclerView = binding.rvSingleDay
        private val tvError:LinearLayout = binding.llError



        fun bind(item: Day) {
            val singleAdapter = SingleDayAdapter(item)
            if(item.date!=""){
                binding.tvDaySingle.text = "${item.date}, ${item.month}"
                tvWeekDay.text = Utils.getShortWeekDay(Utils.getCalendar(item.date.toInt(),Utils.getMonthNumber(item.month)-1, Calendar.getInstance().get(Calendar.YEAR)))
            }

            if (item.isEmpty) emptyDay()
            else {
                fullDay()
            }
            with(recyclerView) {
                adapter = singleAdapter
                layoutManager = object :CustomLinearLayoutManager(mContext){ override fun canScrollVertically(): Boolean { return false } }
            }
        }

        private fun fullDay() {
            recyclerView.visibility = View.VISIBLE
            tvError.visibility = View.GONE
        }

        private fun emptyDay() {
            tvError.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleViewHolder {
        val binding = SingleDiaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SingleViewHolder(binding)

    }

    override fun getItemCount(): Int = items.size


    fun setData(data: List<Day>) {
        items = data
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: SingleViewHolder, position: Int) {
        holder.bind(items[position])

    }

}
