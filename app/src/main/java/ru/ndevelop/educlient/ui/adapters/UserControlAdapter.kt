package ru.ndevelop.educlient.ui.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.ndevelop.educlient.R
import ru.ndevelop.educlient.databinding.SingleUserControlBinding
import ru.ndevelop.educlient.models.AuthData
import ru.ndevelop.educlient.repositories.PreferencesRepository
import ru.ndevelop.educlient.ui.mainActivity.MainActivity
import ru.ndevelop.educlient.utils.*


class UserControlAdapter(val context: Context) :
    RecyclerView.Adapter<UserControlAdapter.SingleViewHolder>() {
    var items: ArrayList<Pair<String, AuthData>> = ArrayList()
    private var deleteBtnClickCount: Int = 0

    inner class SingleViewHolder(private val binding: SingleUserControlBinding) : RecyclerView.ViewHolder(binding.root),
        View.OnClickListener, View.OnLongClickListener {
        val llUserControl: ConstraintLayout = binding.llUserControl
        private val tvName: TextView = binding.tvUserControlName
        private val btnDelete: ImageView = binding.ivDeleteUser
        private val mainActivity = context as MainActivity
        fun bind(item: Pair<String, AuthData>) {

            llUserControl.setOnClickListener(this)
            btnDelete.setOnLongClickListener(this)
            btnDelete.setOnClickListener(this)
            tvName.text = item.first

        }

        override fun onClick(v: View?) {
            val tempString = llUserControl.tag.toString().split(';')
            when (v) {
                llUserControl -> {
                    notifyDataSetChanged()
                    val authData = AuthData(tempString[0], tempString[1])
                    CoroutineScope(Dispatchers.Default).launch {
                        if (PreferencesRepository.getCurrentGlobalUser().login != authData.login) {
                            withContext(Dispatchers.Main) {
                                mainActivity.notifyItemClicked(true)
                            }
                            val authStatus =
                                NetworkHelper.checkAuth(authData)
                            if (authStatus.first == Answers.SUCCESSFUL && authStatus.second != null) {


                                NetworkHelper.clearCookies()
                                NetworkHelper.saveUser(authStatus.second!!)
                                PreferencesRepository.setCurrentGlobalUser(authData)
                            } else {
                                withContext(Dispatchers.Main) {
                                    NetworkHelper.clearCookies()
                                    Toast.makeText(
                                        context,
                                        "Ошибка сети. Проверьте интернет-соединение",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    mainActivity.notifyItemClicked(false)
                                }


                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                mainActivity.notifyItemClicked(false)

                            }

                        }
                    }
                }
                btnDelete -> {
                    deleteBtnClickCount++
                    if (deleteBtnClickCount > 1) {
                        deleteBtnClickCount = 0
                        Toast.makeText(
                            context,
                            "Удерживайте кнопку для удаления",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        override fun onLongClick(v: View?): Boolean {
            val tempString = llUserControl.tag.toString().split(';')
            when (v) {
                btnDelete -> {

                    mainActivity.notifyItemClicked(true)
                    CoroutineScope(Dispatchers.Default).launch {
                        val exitAnswer = Utils.onUserExit(tempString.first())
                        withContext(Dispatchers.Main) {
                            when (exitAnswer) {
                                onExitAnswer.EMPTY_USER_LIST -> {
                                    mainActivity.openLoginActivity()
                                }
                                onExitAnswer.USER_UPDATED_SUCCESFULLY -> {
                                    notifyItemRemoved(adapterPosition)
                                }
                                onExitAnswer.ERROR -> {
                                    Toast.makeText(
                                        context,
                                        "Не удалось выйти из аккаунта. Проверьте интернет-соединение",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            mainActivity.notifyItemClicked(false)
                        }

                    }
                }
            }
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleViewHolder {
        val binding = SingleUserControlBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SingleViewHolder(binding)
    }


    override fun getItemCount(): Int = items.size


    override fun onBindViewHolder(holder: SingleViewHolder, position: Int) {
        holder.bind(items[position])
        holder.llUserControl.tag =
            "${items[position].second.login};${items[position].second.password}"


    }


}

