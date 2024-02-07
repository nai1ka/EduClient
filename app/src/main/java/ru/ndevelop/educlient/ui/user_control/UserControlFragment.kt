package ru.ndevelop.educlient.ui.user_control


import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import ru.ndevelop.educlient.R
import ru.ndevelop.educlient.ui.LoginActivity
import ru.ndevelop.educlient.ui.adapters.UserControlAdapter
import ru.ndevelop.educlient.utils.DataBaseHandler
import ru.ndevelop.educlient.utils.Utils


class UserControlFragment : DialogFragment(), View.OnClickListener {


    private lateinit var userControlAdapter: UserControlAdapter
    private var db = DataBaseHandler
    private lateinit var rvUsers: RecyclerView
    private lateinit var btnAddUser: CardView
    private lateinit var btnOkUser: CardView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_user_control, container, false)
        initViews(root)
        userControlAdapter.items = db.getUserList()
        return root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog

    }

    fun refresh() {
        userControlAdapter.items = db.getUserList()
        userControlAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        val windowParams = Utils.getScreenSize(requireContext())
        val width = windowParams.first-100
        val height = windowParams.second-400
        dialog?.window?.setLayout(width, height)
        dialog?.window?.setBackgroundDrawable(null)
    }

    private fun initViews(root: View) {

        btnAddUser = root.findViewById(R.id.btn_add_user)
        btnOkUser = root.findViewById(R.id.btn_ok_user)
        btnAddUser.setOnClickListener(this)
        btnOkUser.setOnClickListener(this)
        userControlAdapter = UserControlAdapter(requireContext())
        rvUsers = root.findViewById(
            R.id.rv_user_control
        )
        with(rvUsers) {
            adapter = userControlAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            btnAddUser -> {
                val i = Intent(requireContext(), LoginActivity::class.java)
                startActivity(i)
            }
            btnOkUser -> {
               dialog?.dismiss()

            }
        }
    }


}

