package ru.ndevelop.educlient.ui.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.ndevelop.educlient.R
import ru.ndevelop.educlient.databinding.SingleQuarterBinding
import ru.ndevelop.educlient.models.QuarterItem


class QuarterAdapter: RecyclerView.Adapter<QuarterAdapter.SingleViewHolder>() {
    private var items: ArrayList<QuarterItem> = ArrayList()
    private var quarterIndex = 0

    inner class SingleViewHolder(private val binding: SingleQuarterBinding) : RecyclerView.ViewHolder(binding.root) {
        private val tvLesson: TextView = binding.tvQuarterName
        private val tvMark: TextView = binding.tvQuarterMark
        private val divider: View = binding.divQuarter

        fun bind(item: QuarterItem, isLastIndex: Boolean) {
                tvLesson.text = item.lessonName
                tvMark.text = item.marks[quarterIndex].text()
            if (isLastIndex) divider.visibility = View.GONE
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleViewHolder {
        val binding = SingleQuarterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SingleViewHolder(binding)
    }


    override fun getItemCount(): Int = items.size


    override fun onBindViewHolder(holder: SingleViewHolder, position: Int) {
        holder.bind(items[position], position == items.size - 1)
    }

    fun loadItems(payload: ArrayList<QuarterItem>,quarterIndex:Int) {
        items = payload
        this.quarterIndex = quarterIndex
       notifyDataSetChanged()
    }





}
