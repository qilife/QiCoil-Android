package com.Meditation.Sounds.frequencies.feature.options

import android.content.*
import android.os.Build
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.base.BaseActivity
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.feature.main.MainActivity
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.ui.main.HomeViewModel
import com.Meditation.Sounds.frequencies.models.Profile
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.FilesUtils
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.views.CustomFontTextView
import com.google.gson.Gson

class OptionFragment : BaseFragment() {

    private var mBaseActivity: BaseActivity? = null
    private var mUser: Profile? = null
    private lateinit var mViewModel: HomeViewModel
    private var broadcastReceiverReload = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            initComponents()
        }
    }

    override fun initLayout(): Int {
        return R.layout.fragment_option_new
    }

    override fun initComponents() {
        val userJson = SharedPreferenceHelper.getInstance().get(Constants.PREF_PROFILE)
        mUser = Gson().fromJson(userJson, Profile::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mContext?.registerReceiver(
                broadcastReceiverReload,
                IntentFilter("RELOAD_VIEW"),
                Context.RECEIVER_EXPORTED
            )
        } else {
            mContext?.registerReceiver(broadcastReceiverReload, IntentFilter("RELOAD_VIEW"))
        }

        mBaseActivity = mContext as BaseActivity

        mViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        )[HomeViewModel::class.java]

        val tvSignIn = view?.findViewById<CustomFontTextView>(R.id.tvSignIn)
        val mBtnLogout = view?.findViewById<CustomFontTextView>(R.id.mBtnLogout)
        val tvChangePassword = view?.findViewById<CustomFontTextView>(R.id.tvChangePassword)
        val tvFavorite = view?.findViewById<CustomFontTextView>(R.id.tvFavorite)

        if (userJson != null) {
            tvSignIn?.visibility = View.GONE
            mBtnLogout?.visibility = View.VISIBLE
            tvChangePassword?.visibility = View.VISIBLE
        } else {
            tvSignIn?.visibility = View.VISIBLE
            mBtnLogout?.visibility = View.GONE
            tvChangePassword?.visibility = View.GONE
        }


        tvFavorite?.setOnClickListener {
            FilesUtils.showComingSoon(mContext!!)
        }

        tvChangePassword?.setOnClickListener {
//            val dialog = DialogChangePassword(context!!)
//            dialog.show()
        }

        tvSignIn?.setOnClickListener {
//            val dialog = activity?.let { it1 -> DialogSignIn(requireContext(), it1) }
//            dialog?.show()
        }
        mBtnLogout?.setOnClickListener {
            if (Utils.isConnectedToNetwork(mContext)) {
                mBaseActivity?.showAlertWithAction(getString(R.string.txt_msg_logout), R.string.txt_ok, R.string.txt_no, DialogInterface.OnClickListener { _, _ ->

                    mViewModel.syncProgramsToServer()
                    SharedPreferenceHelper.getInstance()[(Constants.PREF_PROFILE)] = null
                    SharedPreferenceHelper.getInstance()[(Constants.PREF_SESSION_ID)] = null
                    SharedPreferenceHelper.getInstance().setBool(Constants.IS_PREMIUM, false)
                    SharedPreferenceHelper.getInstance().setBool(Constants.IS_MASTER, false)
                    SharedPreferenceHelper.getInstance().setBool(Constants.IS_UNLOCK_ALL, false)
                    SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED, false)
                    SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_ADVANCED, false)
                    SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_HIGH_QUANTUM, false)
                    SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_HIGH_ABUNDANCE, false)
                    initComponents()

                    //stop music
                    if (activity is MainActivity) {
                        val musicService = (activity as MainActivity).musicService
                        musicService?.stopMusicService()
                    }
                })
            } else {
                mBaseActivity?.showAlert(getString(R.string.err_network_available))
            }
        }
    }

    override fun addListener() {

    }

    companion object {
        fun newInstance(): OptionFragment {
            return OptionFragment()
        }
    }
}
