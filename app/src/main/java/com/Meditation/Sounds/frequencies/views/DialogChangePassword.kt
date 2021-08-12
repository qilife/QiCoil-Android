package com.Meditation.Sounds.frequencies.views

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.api.ApiListener
import com.Meditation.Sounds.frequencies.api.models.BaseOutput
import com.Meditation.Sounds.frequencies.api.models.LoginOutput
import com.Meditation.Sounds.frequencies.api.objects.ChangePasswordInput
import com.Meditation.Sounds.frequencies.api.objects.LoginInput
import com.Meditation.Sounds.frequencies.feature.base.BaseActivity
import com.Meditation.Sounds.frequencies.feature.main.MainActivity
import com.Meditation.Sounds.frequencies.tasks.BaseTask
import com.Meditation.Sounds.frequencies.tasks.ChangePasswordTask
import com.Meditation.Sounds.frequencies.tasks.LoginTask
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import com.Meditation.Sounds.frequencies.utils.Utils
import com.google.gson.Gson
import kotlinx.android.synthetic.main.dialog_change_pasword.*

class DialogChangePassword(private val mContext: Context) : Dialog(mContext, android.R.style.Theme_Black_NoTitleBar_Fullscreen), ApiListener<Any> {
    var baseActivity: BaseActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_change_pasword)
        baseActivity = mContext as BaseActivity
        val window = this.window
        val wlp = window?.attributes
        wlp?.let {
            it.gravity = Gravity.CENTER
            it.height = WindowManager.LayoutParams.MATCH_PARENT
            it.width = WindowManager.LayoutParams.MATCH_PARENT
            it.flags = it.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN
            it.windowAnimations = R.style.DialogAnimation
        }

        window?.attributes = wlp
        getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.window?.attributes = wlp
        setCancelable(false)
        initComponents()
        addListener()
    }

    fun initComponents() {
        mTvTitleTop.text = mContext.getString(R.string.tv_change_password)
    }

    fun addListener() {
        mImvDismiss.setOnClickListener {
            dismiss()
        }

        mViewBtnChangePassword.setOnClickListener {
            if (Utils.isConnectedToNetwork(mContext)) {
                if (isValidChangePassword()) {
                    baseActivity?.showLoading(true)
                    ChangePasswordTask(mContext,
                            ChangePasswordInput(SharedPreferenceHelper.getInstance().get(Constants.PREF_PASSWORD),
                                    mEdNewPassword.text.toString(),
                                    mEdConfirmNewPassword.text.toString()), this)
                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                }
            } else {
                baseActivity?.showAlert(mContext.getString(R.string.err_network_available))
            }
        }
    }

    override fun onConnectionOpen(task: BaseTask<*>?) {
    }

    override fun onConnectionSuccess(task: BaseTask<*>?, data: Any?) {
        if (task is ChangePasswordTask) {
            baseActivity?.showLoading(false)
            val output = data as BaseOutput
            if (output.success) {
                baseActivity?.hideKeyBoard()
                Toast.makeText(mContext, mContext.getString(R.string.tv_forgot_password_success), Toast.LENGTH_SHORT).show()
                LoginTask(mContext, LoginInput(SharedPreferenceHelper.getInstance().get(Constants.PREF_EMAIL), mEdNewPassword.text.toString()), this).execute()
            } else {
                if (output.errorCode != null) {
//                    SharedPreferenceHelper.getInstance()[(Constants.PREF_PROFILE)] = null
//                    SharedPreferenceHelper.getInstance()[(Constants.PREF_SESSION_ID)] = null
//                    SharedPreferenceHelper.getInstance().setBool(Constants.IS_PREMIUM, false)
                    baseActivity?.showAlert(output.error)
                }
            }
        } else if (task is LoginTask) {
            val output = data as LoginOutput
            if (output.success && output.data != null) {
                SharedPreferenceHelper.getInstance().set(Constants.PREF_SESSION_ID, "Bearer " + output.data.token)
                SharedPreferenceHelper.getInstance().set(Constants.PREF_PROFILE, Gson().toJson(output.data.profile))
                val profile = output.data.profile

                if (profile.isMaster == 1) {
                    SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED, true)
                } else {
                    SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED, false)
                }
                if (profile.isPremium == 1) {
                    SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_ADVANCED, true)
                } else {
                    SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_ADVANCED, false)
                }
                if (profile.isHighAbundance == 1) {
                    SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_HIGH_ABUNDANCE, true)
                } else {
                    SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_HIGH_ABUNDANCE, false)
                }
                if (profile.isHighQuantum == 1) {
                    SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_HIGH_QUANTUM, true)
                } else {
                    SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_HIGH_QUANTUM, false)
                }

                SharedPreferenceHelper.getInstance().set(Constants.PREF_PASSWORD, mEdNewPassword.text.toString())
                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
                mContext.sendBroadcast(intent)
                //stop music
                if (baseActivity is MainActivity) {
                    val musicService = (baseActivity as MainActivity).musicService
                    musicService?.stopMusicService()
                }
                val intentReload = Intent("RELOAD_VIEW")
                mContext.sendBroadcast(intentReload)
                dismiss()
            }
        }
    }

    override fun onConnectionError(task: BaseTask<*>?, exception: Exception) {
        baseActivity?.showLoading(false)
        baseActivity?.showAlert(exception)
//        if (task is ChangePasswordTask) {
//            SharedPreferenceHelper.getInstance()[(Constants.PREF_PROFILE)] = null
//            SharedPreferenceHelper.getInstance()[(Constants.PREF_SESSION_ID)] = null
//            SharedPreferenceHelper.getInstance().setBool(Constants.IS_PREMIUM, false)
//            SharedPreferenceHelper.getInstance().setBool(Constants.IS_MASTER, false)
//            SharedPreferenceHelper.getInstance().setBool(Constants.IS_UNLOCK_ALL, false)
//            SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED, false)
//            SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_ADVANCED, false)
//            SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_HIGH_QUANTUM, false)
//            SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_HIGH_ABUNDANCE, false)
//        }
    }

    private fun isValidChangePassword(): Boolean {
        if (mEdOldPassword.text.toString().isEmpty()) {
            mEdOldPassword.error = "Please enter Old Password!"
            return false
        }
        if (mEdOldPassword.text.toString().length < 6) {
            mEdOldPassword.error = "Old Password cannot be less than 6 characters!"
            return false
        }
        if (mEdNewPassword.text.toString().isEmpty()) {
            mEdNewPassword.error = "Please enter New password!"
            return false
        }
        if (mEdNewPassword.text.toString().length < 6) {
            mEdNewPassword.error = "New password cannot be less than 6 characters!"
            return false
        }
        if (mEdConfirmNewPassword.text.toString().isEmpty()) {
            mEdConfirmNewPassword.error = "Please enter Confirm new password!"
            return false
        }
        if (mEdConfirmNewPassword.text.toString().length < 6) {
            mEdConfirmNewPassword.error = "Confirm new password cannot be less than 6 characters!"
            return false
        }
        if (mEdConfirmNewPassword.text.toString() != mEdNewPassword.text.toString()) {
            mEdConfirmNewPassword.error = "Please check Confirm new password!"
            return false
        }
        val oldPassword = SharedPreferenceHelper.getInstance().get(Constants.PREF_PASSWORD)
        if (mEdOldPassword.text.toString() != oldPassword) {
            mEdOldPassword.error = "Old password is incorrect!"
            return false
        }
        return true
    }

    override fun dismiss() {
        super.dismiss()
    }
}
