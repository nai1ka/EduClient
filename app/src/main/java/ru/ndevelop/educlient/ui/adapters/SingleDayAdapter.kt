package ru.ndevelop.educlient.ui.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import ru.ndevelop.educlient.App
import ru.ndevelop.educlient.R
import ru.ndevelop.educlient.databinding.SingleSingledayBinding
import ru.ndevelop.educlient.models.Day


class SingleDayAdapter(val item:Day) : RecyclerView.Adapter<SingleDayAdapter.SingleViewHolder>() {

    inner class SingleViewHolder(binding:SingleSingledayBinding) : RecyclerView.ViewHolder(binding.root),
        View.OnLongClickListener {
        private val tvLesson = binding.tvLesson
        private val rvDiv = binding.divSingle
        private val tvHomework = binding.tvHomework
        private val rvContainer = binding.rvContainer
        private val tvMark = binding.tvMark

        fun bind(item: Day, position: Int) {

            rvContainer.tag = position
            rvContainer.setOnLongClickListener(this)
            tvLesson.text = item.lessons[position]

            if (item.lessons.size-item.emptyLessonsInBottom<position) {
                rvContainer.visibility = View.GONE
                rvDiv.visibility = View.GONE
            }

            tvHomework.text = item.tasks[position]
            tvMark.visibility = item.visibility[position]
            tvMark.text = item.marks[position]
        }


        override fun onLongClick(v: View?): Boolean {
            var homeworkText = ""
            when (v) {
                rvContainer -> {
                    if (v != null) homeworkText = item.tasks[v.tag.toString().toInt()]
                }
            }
            if (homeworkText != "") {

                val clipboard: ClipboardManager? =
                    App.applicationContext()
                        .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                val clip = ClipData.newPlainText("Homework", homeworkText)
                Toast.makeText(App.applicationContext(), "Текст скопирован", Toast.LENGTH_SHORT)
                    .show()
                clipboard?.setPrimaryClip(clip)
            }
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleViewHolder {
        val binding = SingleSingledayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SingleViewHolder(binding)
    }

    override fun getItemCount(): Int =item.lessons.size - item.emptyLessonsInBottom



    override fun onBindViewHolder(holder: SingleViewHolder, position: Int) {
        holder.bind(item, position)
    }

}
