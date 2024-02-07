package ru.ndevelop.educlient.ui.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.ndevelop.educlient.R
import ru.ndevelop.educlient.databinding.SingleTableBinding
import ru.ndevelop.educlient.models.TableItem


class TableAdapter(val context: Context)   : RecyclerView.Adapter<TableAdapter.SingleViewHolder>() {
    private var items:ArrayList<TableItem> = ArrayList()
    inner class SingleViewHolder(private val binding: SingleTableBinding) : RecyclerView.ViewHolder(binding.root){
        private val tvLesson:TextView = binding.tvTableLesson
        private val tvMarks:TextView = binding.tvTableMarks
        private val tvAverage:TextView = binding.tvTableAverage
        private val tvFinal:TextView = binding.tvTableFinal
        fun bind(item: TableItem) {
            tvLesson.text = item.lesson
            if(item.marks.size>0 ) {
                tvMarks.text = item.marks.subList(0, item.marks.size - 1).joinToString()
                tvAverage.text = item.marks.last()

            }
            else{
                tvMarks.text = "Нет оценок"
                tvAverage.text = ""
            }
            if(item.finalMark!="")
                tvFinal.text = "(${item.finalMark})"
            else tvFinal.text = ""

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleViewHolder {
        val binding = SingleTableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SingleViewHolder(binding)
    }


    override fun getItemCount(): Int = items.size


    override fun onBindViewHolder(holder: SingleViewHolder, position: Int) {
        holder.bind(items[position])




    }
    fun loadItems(payload:ArrayList<TableItem>){
        items = payload
        notifyDataSetChanged()
    }





}