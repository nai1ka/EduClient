package ru.ndevelop.educlient.ui.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.ndevelop.educlient.databinding.SingleHomeworkBinding
import ru.ndevelop.educlient.models.HomeworkDay
import ru.ndevelop.educlient.ui.custom_ui.CustomLinearLayoutManager
import ru.ndevelop.educlient.utils.Utils
import java.util.*


class HomeworkAdapter(val mContext: Context) :
    RecyclerView.Adapter<HomeworkAdapter.SingleViewHolder>() {
    private var items: MutableList<HomeworkDay> = mutableListOf()


    inner class SingleViewHolder(private val binding: SingleHomeworkBinding) : RecyclerView.ViewHolder(binding.root) {
        private val tvDate = binding.tvHomeworkDate
        private val tvWeekday = binding.tvHomeworkWeekday
        private val recyclerView = binding.rvHomeworkSingle


        fun bind(item: HomeworkDay) {
            val singleAdapter = SingleHomeworkAdapter(
                item.tasks,
                item.date.toString() + Utils.getMonthNumber(item.month),
                adapterPosition,
                this@HomeworkAdapter
            )
            tvDate.text = item.date.toString() + " " + Utils.getCorrectMonthTitle(item.month)

            tvWeekday.text = Utils.getWeekDay(Utils.getCalendar(item.date,Utils.getMonthNumber(item.month)-1, Calendar.getInstance().get(Calendar.YEAR)))
            with(recyclerView) {
                adapter = singleAdapter
                layoutManager = object : CustomLinearLayoutManager(mContext) {
                    override fun canScrollVertically(): Boolean {
                        return false
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleViewHolder {
        val binding = SingleHomeworkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SingleViewHolder(binding)
    }


    override fun getItemCount(): Int =
        items.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: SingleViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun loadHomework(payload: MutableList<HomeworkDay>) {

        items = payload
        notifyDataSetChanged()
    }

    fun setHomeworkState(index: Int, lessonNumber: Int, status: Boolean) {
        items[index].tasks[lessonNumber].isDone = status
    }

    fun clearHomeworks() {
        items = arrayListOf()
        notifyDataSetChanged()
    }


}