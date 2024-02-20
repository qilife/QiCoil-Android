package com.Meditation.Sounds.frequencies.feature.profile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.Meditation.Sounds.frequencies.BuildConfig
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.api.ApiListener
import com.Meditation.Sounds.frequencies.feature.base.BaseActivity
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.lemeor.InstructionsActivity
import com.Meditation.Sounds.frequencies.models.Profile
import com.Meditation.Sounds.frequencies.tasks.BaseTask
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.views.DisclaimerDialog
import com.Meditation.Sounds.frequencies.views.SubscriptionDialogFlashSale
import com.Meditation.Sounds.frequencies.views.SubscriptionDialogNormal
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_profile.*
import java.util.*

@Suppress("DEPRECATION")
class ProfileFragment : BaseFragment(), ApiListener<Any> {

    private var mBaseActivity: BaseActivity? = null
    private var mUser: Profile? = null

    private val broadcastReceiverSubscriptionController = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
//            if (SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED)
//                    && SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_ADVANCED)) {
//                btnSubscription.visibility = View.GONE
//                mViewTimerFlashSale.visibility = View.GONE
//            } else {
//                btnSubscription.visibility = View.VISIBLE
//                mViewTimerFlashSale.visibility = View.VISIBLE
//            }
        }
    }

    override fun initLayout(): Int {
        return R.layout.fragment_profile
    }

    override fun initComponents() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mContext?.registerReceiver(
                broadcastReceiverSubscriptionController,
                IntentFilter(Constants.BROADCAST_ACTION_PURCHASED),
                Context.RECEIVER_EXPORTED
            )
        } else {
            mContext?.registerReceiver(
                broadcastReceiverSubscriptionController,
                IntentFilter(Constants.BROADCAST_ACTION_PURCHASED)
            )
        }

        val userJson = SharedPreferenceHelper.getInstance().get(Constants.PREF_PROFILE)
        mUser = Gson().fromJson(userJson, Profile::class.java)
        if (userJson != null) {
            mTvUser.visibility = View.VISIBLE
        } else {
            mTvUser.visibility = View.GONE
        }

        mBaseActivity = mContext as BaseActivity
        if (SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED)
                && SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_ADVANCED)
                && SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_HIGH_ABUNDANCE)
                && SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_HIGH_QUANTUM)) {
            btnSubscription.visibility = View.GONE
            mViewTimerFlashSale.visibility = View.GONE
        } else {
            btnSubscription.visibility = View.VISIBLE
            mViewTimerFlashSale.visibility = View.VISIBLE
        }

        val flashSaleRemainTimeGloble = Utils.getFlashSaleRemainTime()
        if (flashSaleRemainTimeGloble > 0) {
            setCountdownTimer(flashSaleRemainTimeGloble)
        } else {
            mViewTimerFlashSale.visibility = View.GONE
        }
    }

    override fun onConnectionOpen(task: BaseTask<*>?) {
    }

    override fun onConnectionSuccess(task: BaseTask<*>?, data: Any?) {
    }

    override fun onConnectionError(task: BaseTask<*>?, exception: Exception) {

    }

    override fun addListener() {
        mViewTimerFlashSale.setOnClickListener {

            val dialog = SubscriptionDialogFlashSale(mContext)
            if (!SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED)
                    && !SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_ADVANCED)
                    && !SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_HIGH_ABUNDANCE)) {
                when {
                    random() == 1 -> dialog.setTypeAlbum(0)
                    random() == 2 -> dialog.setTypeAlbum(1)
                    else -> dialog.setTypeAlbum(2)
                }
            }
            dialog.show()
        }
        btnSubscription.setOnClickListener {
            val dialog = SubscriptionDialogNormal(mContext)
            if (!SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED)
                    && !SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_ADVANCED)
                    && !SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_HIGH_ABUNDANCE)) {
                when {
                    random() == 1 -> dialog.setTypeAlbum(0)
                    random() == 2 -> dialog.setTypeAlbum(1)
                    else -> dialog.setTypeAlbum(2)
                }
            }
            dialog.show()
        }
        btnInstructions!!.setOnClickListener {
            startActivity(Intent(mContext, InstructionsActivity::class.java))
        }
        btnAbout!!.setOnClickListener {
            activity?.let {
                AlertDialog.Builder(it)
                        .setTitle(R.string.txt_about)
                        .setMessage(getString(R.string.app_name) + " v" + BuildConfig.VERSION_NAME)
                        .setPositiveButton(R.string.txt_ok, null).show()
            }
        }
        btnDisclaimerInfor!!.setOnClickListener {
            val dialog = DisclaimerDialog(mContext, false, object : DisclaimerDialog.IOnSubmitListener {
                override fun submit(isCheck: Boolean) {
                }
            })
            dialog.show()
            dialog.setButtonText(getString(R.string.txt_ok))
        }
    }

    private fun random(): Int {
        val random = Random()
        return random.nextInt(2) + 1
    }

    private var mCountDownTimer: CountDownTimer? = null

    private fun setCountdownTimer(totalTime: Long) {
        mCountDownTimer = object : CountDownTimer(totalTime, 1000) {
            override fun onTick(l: Long) {
                val totalSeconds = (l / 1000).toInt()
                val days = totalSeconds / (24 * 3600)
                var remainder = totalSeconds - (days * 24 * 3600)
                val hours = remainder / 3600
                remainder -= (hours * 3600)
                val mins = remainder / 60
                remainder -= mins * 60
                val secs = remainder

                if (tvHoursC != null) {
                    tvHoursC.text = if (hours > 9) "" + hours else "0$hours"
                }
                if (tvSeconds != null) {
                    tvSeconds.text = if (secs > 9) "" + secs else "0$secs"
                }
                if (tvMinutes != null) {
                    tvMinutes.text = if (mins > 9) "" + mins else "0$mins"
                }
            }

            override fun onFinish() {
                initComponents()
            }
        }
        mCountDownTimer!!.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mContext!!.unregisterReceiver(broadcastReceiverSubscriptionController)
        if (mCountDownTimer != null) {
            mCountDownTimer!!.cancel()
        }
    }
}
