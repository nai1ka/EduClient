package ru.ndevelop.educlient.ui.calculators

import android.app.Dialog
import android.content.Context
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.DialogFragment
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayout
import ru.ndevelop.educlient.R
import ru.ndevelop.educlient.models.TableItem
import ru.ndevelop.educlient.repositories.PreferencesRepository
import ru.ndevelop.educlient.utils.DataBaseHandler

class VirtualCalculatorFragment : DialogFragment(), View.OnClickListener {
    private lateinit var tvResult: TextView
    private lateinit var spinner: Spinner
    var adapter: ArrayAdapter<String>? = null
    private var markButtons = mutableListOf<TextView>()
    lateinit var flexboxLayout: FlexboxLayout
    private var tempMarkArray = mutableListOf<String>()
    private var lessonsNames: MutableList<String> = mutableListOf()
    private var flexTextsArray = arrayListOf<TextView>()
    private lateinit var tvMarkNumber: TextView
    private lateinit var ibClear: ImageButton
    private lateinit var svMarks: ScrollView
    var currentPos = 0
    private var fullTableItemsArrayList: ArrayList<TableItem> = arrayListOf()
    private var tableItemsArrayListWithoutTotalMark: MutableList<TableItem> = mutableListOf()
    private var marksCount = arrayOf(0, 0, 0, 0, 0)
    override fun onAttach(context: Context) {
        super.onAttach(context)
        fullTableItemsArrayList =
            DataBaseHandler.getTable(PreferencesRepository.getCurrentGlobalUser().login).tableArray
        if (fullTableItemsArrayList.size > 0)
            tableItemsArrayListWithoutTotalMark = fullTableItemsArrayList.subList(
                0,
                fullTableItemsArrayList.size - 1
            )
        else dismiss()

    }

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_virtual_calculator, container, false)
        initViews(root)
        initSpinner()
        return root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog

    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setBackgroundDrawable(null)


    }

    private fun initViews(r: View) {

        markButtons.clear()
        markButtons.apply {
            add(r.findViewById(R.id.btn_add_1))
            add(r.findViewById(R.id.btn_add_2))
            add(r.findViewById(R.id.btn_add_3))
            add(r.findViewById(R.id.btn_add_4))
            add(r.findViewById(R.id.btn_add_5))

        }
        ibClear = r.findViewById(R.id.ib_clear)
        ibClear.setOnClickListener(this)
        tvMarkNumber = r.findViewById(R.id.tv_mark_number)
        svMarks = r.findViewById(R.id.sv_calc)
        tvResult = r.findViewById(R.id.tv_virtual_calc_result)
        spinner = r.findViewById(R.id.spinner_virtual_calc)
        markButtons.forEach { it.setOnClickListener(this) }
        flexboxLayout = r.findViewById(R.id.flexbox_calculator) as FlexboxLayout

        flexboxLayout.flexDirection = FlexDirection.ROW
    }

    private fun initSpinner() {
        tableItemsArrayListWithoutTotalMark.forEach { lessonsNames.add(it.lesson) }
        val adapter: ArrayAdapter<String> =
            ArrayAdapter(requireContext(), R.layout.item_lesson, lessonsNames)
        adapter.setDropDownViewResource(R.layout.item_lesson)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                if (position >= 0 && parentView != null) {
                    currentPos = position
                    clear(currentPos)


                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // your code here
            }
        }
    }

    fun clear(pos: Int) {
        flexTextsArray.clear()
        marksCount = arrayOf(0, 0, 0, 0, 0)
        tvMarkNumber.text = getMarksCountString(marksCount)
        flexboxLayout.removeAllViews()
        tempMarkArray.clear()
        val tempSize = tableItemsArrayListWithoutTotalMark[pos].marks.size
        if (tempSize > 0)
            tempMarkArray = (tableItemsArrayListWithoutTotalMark[pos].marks.toMutableList().subList(
                0,
                tempSize - 1
            ))
        tempMarkArray.forEach { addToFlexLayout(it) }
        tvResult.text = getAverageMark(tempMarkArray)


    }

    override fun onClick(v: View?) {
        when (v) {
            markButtons[0] -> {
                addToFlexLayout("1")
                tempMarkArray.add("1")
                marksCount[0]++

            }
            markButtons[1] -> {
                addToFlexLayout("2")
                tempMarkArray.add("2")
                marksCount[1]++

            }
            markButtons[2] -> {
                addToFlexLayout("3")
                tempMarkArray.add("3")
                marksCount[2]++

            }
            markButtons[3] -> {
                addToFlexLayout("4")
                tempMarkArray.add("4")
                marksCount[3]++

            }
            markButtons[4] -> {
                addToFlexLayout("5")
                tempMarkArray.add("5")
                marksCount[4]++

            }
            ibClear -> {
                clear(currentPos)

            }
            else -> {
                if (v is TextView)
                    flexTextsArray.clear()
                tempMarkArray.removeAt(v?.tag as Int)
                val indexOfMark = Integer.parseInt((v as TextView).text.toString()) - 1
                if (marksCount[indexOfMark] > 0) marksCount[indexOfMark]--

                flexboxLayout.removeAllViews()
                tempMarkArray.forEach { addToFlexLayout(it) }


            }
        }
        tvMarkNumber.text = getMarksCountString(marksCount)
        tvResult.text = getAverageMark(tempMarkArray)
        svMarks.fullScroll(View.FOCUS_DOWN)
    }

    private fun addToFlexLayout(mark: String) {
        val trHead =
            layoutInflater.inflate(R.layout.flexbox_layout, flexboxLayout, false) as RelativeLayout
        val labelDate = trHead.findViewById<View>(R.id.tv_flexbox) as TextView
        when (mark) {
            "1" -> {
                labelDate.background = resources.getDrawable(R.drawable.mark1_background)

            }
            "2" -> {
                labelDate.background = resources.getDrawable(R.drawable.mark2_background)
            }
            "3" -> {
                labelDate.background = resources.getDrawable(R.drawable.mark3_background)

            }
            "4" -> {
                labelDate.background = resources.getDrawable(R.drawable.mark4_background)
            }
            "5" -> {
                labelDate.background = resources.getDrawable(R.drawable.mark5_background)
            }
        }
        labelDate.text = mark
        // if(flexTextsArray.size<=tempMarkArray.size)
        labelDate.tag = flexTextsArray.size
        //else label_date.tag = tempMarkArray.size-1

        flexTextsArray.add(labelDate)
        labelDate.setOnClickListener(this)
        flexboxLayout.addView(trHead)
    }

    fun getAverageMark(payload: MutableList<String>): String {
        var sum = 0.0
        payload.forEach {
            sum += it.toInt()
        }
        return if (payload.size > 0)
            String.format("%.2f", (sum / payload.size))
        else "Нет оценок"

    }

    fun getMarksCountString(array: Array<Int>): String {
        var resultString = "Добавлено оценок:\n"
        for (i in array.indices.reversed()) {
            if (i > 0)
                resultString += "${i + 1}: ${array[i]} шт.\n"

        }
        resultString += "${1}: ${array[0]} шт."
        return resultString
    }
}