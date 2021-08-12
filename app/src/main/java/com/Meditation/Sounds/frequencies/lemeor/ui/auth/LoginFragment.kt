package com.Meditation.Sounds.frequencies.lemeor.ui.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.api.exception.ApiException
import com.Meditation.Sounds.frequencies.lemeor.showAlert
import com.Meditation.Sounds.frequencies.utils.Utils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.fragment_login.*


class LoginFragment : Fragment() {

    interface OnLoginListener {
        fun onLoginInteraction(email: String, password: String)
        fun onOpenRegistration()
        fun onOpenForgotPassword()
    }

    private var mListener: OnLoginListener? = null
    var RC_SIGN_IN = 100

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnLoginListener) {
            mListener = context
        } else {
            throw RuntimeException(context.toString()
                    + " must implement OnLoginFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mTvSignUp.text = Html.fromHtml(getString(R.string.tv_link_sign_up))
        mTvForgotPassword.text = Html.fromHtml(getString(R.string.tv_forgotten_password))

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

        // Build a GoogleSignInClient with the options specified by gso.
        val mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);

        val account = GoogleSignIn.getLastSignedInAccount(activity)
        //updateUI(account)


        mBtnSignIn.setOnClickListener {
            if (Utils.isConnectedToNetwork(requireContext())) {
                if (isValidLogin()) {
                    mListener?.onLoginInteraction(mEdEmailSignIn.text.toString(), mEdPasswordSignIn.text.toString())
                }
            } else {
                showAlert(requireContext(), getString(R.string.err_network_available))
            }
        }

        mTvSignUp.setOnClickListener { mListener?.onOpenRegistration() }

        mTvForgotPassword.setOnClickListener { mListener?.onOpenForgotPassword() }

        btn_guest.setOnClickListener {
            mListener?.onLoginInteraction("guest", "")
        }

        rlgoogle_signin.setOnClickListener {
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    private fun isValidLogin(): Boolean {
        if (mEdEmailSignIn.text.toString().trim().isEmpty()) {
            mEdEmailSignIn.error = "Please enter Email!"
            return false
        }
        if (!mEdEmailSignIn.text.toString().matches("[a-zA-Z0-9._-]+@[a-z]+.[a-z]+".toRegex())) {
            mEdEmailSignIn.error = "Invalid email!"
            return false
        }
        if (mEdPasswordSignIn.text.toString().trim().isEmpty()) {
            mEdPasswordSignIn.error = "Please enter Password!"
            return false
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

          //  val acct = GoogleSignIn.getLastSignedInAccount(activity)
            Toast.makeText(activity,"Login successful",Toast.LENGTH_SHORT).show()
            if (account != null) {
                val personName = account.displayName
                val personGivenName = account.givenName
                val personFamilyName = account.familyName
                val personEmail = account.email
                val personId = account.id
            }


        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            //Log.w(TAG, "signInResult:failed code=" + e.getStatusCode())
            //updateUI(null)
        }
    }
}