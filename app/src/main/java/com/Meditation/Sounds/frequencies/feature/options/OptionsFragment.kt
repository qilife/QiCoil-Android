package com.Meditation.Sounds.frequencies.feature.options

import android.content.Intent
import androidx.appcompat.app.AlertDialog
import com.Meditation.Sounds.frequencies.BuildConfig
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.base.BaseActivity
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.lemeor.InstructionsActivity
import com.Meditation.Sounds.frequencies.views.DisclaimerDialog
import kotlinx.android.synthetic.main.fragment_options.*

class OptionsFragment : BaseFragment() {

    var baseActivity: BaseActivity? = null

    override fun initLayout(): Int {
        return R.layout.fragment_options
    }

    override fun initComponents() {

        baseActivity = mContext as BaseActivity

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

    override fun addListener() {
    }
}
