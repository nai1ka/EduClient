package ru.ndevelop.educlient.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import ru.ndevelop.educlient.R
import ru.ndevelop.educlient.ui.mainActivity.Background
import ru.ndevelop.educlient.ui.mainActivity.MainActivity


class BackgroundChangeDialog : DialogFragment(), View.OnClickListener {

    private lateinit var llStandart: LinearLayout
    private lateinit var llWhite: LinearLayout
    private lateinit var llCustom: LinearLayout


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_background_change, container, false)
        initViews(root)
        return root
    }


    private fun initViews(root: View) {
        llStandart = root.findViewById(R.id.ll_bg_standart)
        llWhite = root.findViewById(R.id.ll_bg_white)
        llCustom = root.findViewById(R.id.ll_bg_custom)
        llStandart.setOnClickListener(this)
        llWhite.setOnClickListener(this)
        llCustom.setOnClickListener(this)
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
    override fun onClick(v: View?) {
        when (v) {

            llStandart -> {
                (requireActivity() as MainActivity).changeBg(Background.STANDART)
            }
            llWhite -> {
                (requireActivity() as MainActivity).changeBg(Background.WHITE)
            }
            llCustom -> {
                (requireActivity() as MainActivity).changeBg(Background.CUSTOM)
            }
        }
        dialog?.dismiss()

    }


}
