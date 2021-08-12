package com.Meditation.Sounds.frequencies.lemeor.ui.auth

import android.content.Context
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.utils.StringUtils
import kotlinx.android.synthetic.main.dialog_sign_up.*

class RegistrationFragment : Fragment() {

    interface OnRegistrationListener {
        fun onRegistrationInteraction(name: String, email: String, pass: String, confirm: String, uuid: String)
        fun onOpenLogin()
    }

    private var mListener: OnRegistrationListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRegistrationListener) {
            mListener = context
        } else {
            throw RuntimeException(context.toString()
                    + " must implement OnRegistrationListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mTvSignIn.text = Html.fromHtml(getString(R.string.tv_link_sign_in))

        mTvSignIn.setOnClickListener { mListener?.onOpenLogin() }

        mBtnGetStartedRegister.setOnClickListener {
            if (isValidRegister()) {
                mListener?.onRegistrationInteraction(
                        mEdNameRegister.text.toString(),
                        mEdEmailRegister.text.toString(),
                        mEdPasswordRegister.text.toString(),
                        mEdConfirmPasswordRegister.text.toString(),
                        StringUtils.getDeviceId(requireContext())
                )
            }
        }
    }

    private fun isValidRegister(): Boolean {
        if (mEdNameRegister.text.toString().isEmpty()) {
            mEdNameRegister.error = "Please enter Name!"
            return false
        }
        if (mEdEmailRegister.text.toString().isEmpty()) {
            mEdEmailRegister.error = "Please enter Email!"
            return false
        }
        if (mEdPasswordRegister.text.toString().isEmpty()) {
            mEdPasswordRegister.error = "Please enter Password!"
            return false
        }
        if (mEdPasswordRegister.text.toString().length < 6) {
            mEdPasswordRegister.error = "Password cannot be less than 6 characters!"
            return false
        }
        if (mEdConfirmPasswordRegister.text.toString().isEmpty()) {
            mEdConfirmPasswordRegister.error = "Please enter Confirm Password!"
            return false
        }
        if (mEdConfirmPasswordRegister.text.toString() != mEdPasswordRegister.text.toString()) {
            mEdConfirmPasswordRegister.error = "Please check Confirm Password!"
            return false
        }
        if (!mEdEmailRegister.text.toString().matches("[a-zA-Z0-9._-]+@[a-z]+.[a-z]+".toRegex())) {
            mEdEmailRegister.error = "Your email address is invalid!"
            return false
        }
        return true
    }
}