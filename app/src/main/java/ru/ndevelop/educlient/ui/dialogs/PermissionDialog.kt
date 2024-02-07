package ru.ndevelop.educlient.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import ru.ndevelop.educlient.R
import ru.ndevelop.educlient.ui.mainActivity.PermissionDialogListener
import ru.ndevelop.educlient.ui.mainActivity.PermissionDialogTypes
import ru.ndevelop.educlient.utils.Utils


class PermissionDialog(
    private val title: String,
    private val description: String,
    private val image: Int,
    private val mode: PermissionDialogTypes,
    private val dialogListener: PermissionDialogListener
) : DialogFragment(), View.OnClickListener {


    private lateinit var okButton: CardView
    private lateinit var tvTitle: TextView
    private lateinit var tvDescriptor: TextView
    private lateinit var mainImage: ImageView


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.dialog_permissions, container, false)
        initViews(root)
        return root

    }

    override fun onResume() {
        super.onResume()
        val params: ViewGroup.LayoutParams = dialog!!.window!!.attributes
        val windowParams = Utils.getScreenSize(requireContext())
        params.width = windowParams.first - 140
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog?.window?.attributes = params as WindowManager.LayoutParams

    }

    private fun initViews(r: View) {
        tvTitle = r.findViewById(R.id.tv_permission_title)
        tvTitle.text = title
        tvDescriptor = r.findViewById(R.id.tv_permission_description)
        tvDescriptor.text = description
        mainImage = r.findViewById(R.id.iv_permissions)
        mainImage.setImageResource(image)
        okButton = r.findViewById(R.id.btn_permission_ok)
        okButton.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v) {
            okButton -> {
                dialogListener.onPermissionOkButtonClicked(mode)
                dialog?.cancel()
            }
        }
    }


}
