package ru.ndevelop.educlient.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.ndevelop.educlient.databinding.ItemHomeworkBinding
import ru.ndevelop.educlient.models.HomeworkItem
import ru.ndevelop.educlient.utils.DataBaseHandler


class SingleHomeworkAdapter(
    private val items: ArrayList<HomeworkItem>,
    val dayId: String,
    val index: Int,
    val parentContext: HomeworkAdapter
) :
    RecyclerView.Adapter<SingleHomeworkAdapter.SingleViewHolder>() {


    inner class SingleViewHolder(private val binding : ItemHomeworkBinding) : RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        private val tvLesson = binding.tvHomeworkLesson
        private val tvTask = binding.tvHomeworkTask
        private val checkBox = binding.cbHomework
        private val llMain = binding.llHomework


        fun bind(item: HomeworkItem, position: Int) {
            if (item.lesson != "") {
                tvLesson.text = item.lesson
                tvTask.text = item.text
                checkBox.setOnClickListener(this)
                checkBox.isChecked = item.isDone
                llMain.setOnClickListener(this)
                checkBox.id =
                    (dayId + position).toInt()
            } else {
                llMain.visibility = View.GONE
            }


        }

        override fun onClick(v: View?) {
            when (v) {
                checkBox -> {
                    DataBaseHandler.setHomeworkState(checkBox.id, checkBox.isChecked)
                    parentContext.setHomeworkState(index, adapterPosition, checkBox.isChecked)
                }

                llMain -> {
                    checkBox.isChecked = !checkBox.isChecked
                    DataBaseHandler.setHomeworkState(checkBox.id, checkBox.isChecked)
                    parentContext.setHomeworkState(index, adapterPosition, checkBox.isChecked)
                }

            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleViewHolder {
        val binding = ItemHomeworkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
        holder.bind(items[position], position)
    }


}