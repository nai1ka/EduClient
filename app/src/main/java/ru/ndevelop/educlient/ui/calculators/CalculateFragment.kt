package ru.ndevelop.educlient.ui.calculators

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import ru.ndevelop.educlient.R
import ru.ndevelop.educlient.models.TableItem
import ru.ndevelop.educlient.repositories.PreferencesRepository
import ru.ndevelop.educlient.utils.DataBaseHandler
import kotlin.math.roundToInt


class CalculateFragment : DialogFragment(), View.OnClickListener {
    private lateinit var spinner: Spinner
    private lateinit var tvResult: TextView
    private lateinit var etWanted: EditText
    private lateinit var btnResult: CardView
    private var lessonsNames: MutableList<String> = mutableListOf()
    private var fullTableItemsArrayList: ArrayList<TableItem> = arrayListOf()
    private var tableItemsArrayListWithoutTotalMark: MutableList<TableItem> = mutableListOf()
    private lateinit var tvCurrentMark: TextView
    private var wantedMark: Double = 5.0
    private var currentNumberOfMarks = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_calculate, container, false)
        fullTableItemsArrayList =
            DataBaseHandler.getTable(PreferencesRepository.getCurrentGlobalUser().login).tableArray
        if(fullTableItemsArrayList.size >0){
            tableItemsArrayListWithoutTotalMark =
                fullTableItemsArrayList.subList(0, fullTableItemsArrayList.size - 1)
            initViews(root)
            initSpinner()
        }
        else{
            dismiss()
        }

        return root
    }

    private fun initViews(r: View) {
        spinner = r.findViewById(R.id.spinner_table)
        tvResult = r.findViewById(R.id.tv_table_result)
        etWanted = r.findViewById(R.id.et_wanted_mark)
        btnResult = r.findViewById(R.id.btn_result_table)
        tvCurrentMark = r.findViewById(R.id.tv_current_mark)
        btnResult.setOnClickListener(this)
    }

    private fun initSpinner() {

        tableItemsArrayListWithoutTotalMark.forEach { lessonsNames.add(it.lesson) }
        val adapter: ArrayAdapter<String> =
            ArrayAdapter(requireContext(), R.layout.item_lesson, lessonsNames)
        adapter.setDropDownViewResource(R.layout.item_lesson)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                itemSelected: View, selectedItemPosition: Int, selectedId: Long
            ) {
                if (!tableItemsArrayListWithoutTotalMark[selectedItemPosition].marks.isNullOrEmpty()) {
                    tvResult.text = "Результат:"
                    tvCurrentMark.text =
                        tableItemsArrayListWithoutTotalMark[selectedItemPosition].marks.last()
                } else tvCurrentMark.text = "Нет оценок"

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setBackgroundDrawable(null)
    }

    override fun onClick(v: View?) {
        when (v) {
            btnResult -> {
                if (tableItemsArrayListWithoutTotalMark[spinner.selectedItemPosition].marks.isNotEmpty())
                    try {
                        wantedMark = etWanted.text.toString().toDouble()
                        if (wantedMark < 5.0) {

                            currentNumberOfMarks =
                                tableItemsArrayListWithoutTotalMark[spinner.selectedItemPosition].marks.size - 1
                            var sumOfCurrentMarks =
                                tableItemsArrayListWithoutTotalMark[spinner.selectedItemPosition].marks.sumOf { if (it.length == 1) it.toDouble() else 0.0 }
                            val approximateResult =
                                (wantedMark * currentNumberOfMarks - sumOfCurrentMarks) / (5 - wantedMark)
                            var res = ">100"
                            var tempRes = 0
                            if (approximateResult <= 100) {
                                while ((sumOfCurrentMarks / currentNumberOfMarks * 100).roundToInt() / 100.0 < wantedMark && tempRes <= 100) {
                                    sumOfCurrentMarks += 5
                                    currentNumberOfMarks++
                                    tempRes++
                                }
                                res = "$tempRes"
                            }

                            if ((wantedMark == tableItemsArrayListWithoutTotalMark[spinner.selectedItemPosition].marks.last()
                                    .toDouble())
                            ) res = "0"
                            tvResult.text = "Результат: $res"
                        } else if (wantedMark == 5.0 && tableItemsArrayListWithoutTotalMark[spinner.selectedItemPosition].marks.last() == "5.00") tvResult.text =
                            "Результат: 0"
                        else tvResult.text = "Результат: ∞"

                    } catch (numberException: NumberFormatException) {
                        tvResult.text = "Неправильный формат ввода"
                    } catch (e: Exception) {
                        tvResult.text = "0"
                    }
                else tvResult.text = "Здесь ещё нет оценок"

            }
        }
    }

}