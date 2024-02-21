package com.Meditation.Sounds.frequencies.lemeor.ui.auth

import android.app.Activity.RESULT_CANCELED
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.api.exception.ApiException
import com.Meditation.Sounds.frequencies.lemeor.showAlert
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.Utils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_login.*


class LoginFragment : Fragment() {

    interface OnLoginListener {
        fun onLoginInteraction(email: String, password: String)
        fun onOpenRegistration()
        fun onOpenForgotPassword()
        fun onGoogleLogin(email: String, name: String, google_id: String)
        fun onFbLogin(email: String, name: String, fb_id: String)
    }

    private var mListener: OnLoginListener? = null
    var RC_SIGN_IN = 100
    var RC_FB_SIGN_IN = 200
    var id = ""
    var firstName = ""
    var middleName = ""
    var lastName = ""
    var name = ""
    var picture = ""
    var email = ""
    var accessToken = ""
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnLoginListener) {
            mListener = context
        } else {
            throw RuntimeException(
                context.toString()
                        + " must implement OnLoginFragmentListener"
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAnalytics = Firebase.analytics
        mTvSignUp.text = Html.fromHtml(getString(R.string.tv_link_sign_up))
        mTvForgotPassword.text = Html.fromHtml(getString(R.string.tv_forgotten_password))
        if (com.Meditation.Sounds.frequencies.BuildConfig.DEBUG) {
//            mEdEmailSignIn.setText("tester03@yopmail.com")
//            mEdPasswordSignIn.setText("123123")
//        mEdEmailSignIn.setText("manufacturing@qilifestore.com")
//        mEdPasswordSignIn.setText("12345678")
//        mEdPasswordSignIn.setText("1234test")
//        mEdEmailSignIn.setText("pongpopong@gmail.com")
//        mEdPasswordSignIn.setText("goldfish")
//            mEdEmailSignIn.setText("janetshelton1913@gmail.com")
//            mEdPasswordSignIn.setText("12345678")
//        mEdEmailSignIn.setText("lailani.raphaell@gmail.com")
//        mEdPasswordSignIn.setText("lailani1234")
//            mEdEmailSignIn.setText("tester02@yopmail.com")
//             mEdPasswordSignIn.setText("12345678")
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        val mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);

        val account = GoogleSignIn.getLastSignedInAccount(activity)

        mBtnSignIn.setOnClickListener {
            if (Utils.isConnectedToNetwork(requireContext())) {
                if (isValidLogin()) {
                    mListener?.onLoginInteraction(
                        mEdEmailSignIn.text.toString(),
                        mEdPasswordSignIn.text.toString()
                    )
                }
            } else {
                showAlert(requireContext(), getString(R.string.err_network_available))
            }
        }

        mTvSignUp.setOnClickListener { mListener?.onOpenRegistration() }

        mTvForgotPassword.setOnClickListener { mListener?.onOpenForgotPassword() }

        btn_guest.setOnClickListener {
            mListener?.onLoginInteraction("guest", "")
            Constants.isGuestLogin = true
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
        if (!isValidEmail(mEdEmailSignIn.text.toString())) {
            mEdEmailSignIn.error = "Invalid email!"
            return false
        }
        if (mEdPasswordSignIn.text.toString().trim().isEmpty()) {
            mEdPasswordSignIn.error = "Please enter Password!"
            return false
        }
        return true
    }

    private fun isValidEmail(email: String): Boolean {
        val pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_CANCELED)
            if (requestCode === RC_SIGN_IN) {
                // The Task returned from this call is always completed, no need to attach
                // a listener.
                if (data != null) {
                    val task: Task<GoogleSignInAccount> =
                        GoogleSignIn.getSignedInAccountFromIntent(data)
                    handleSignInResult(task)
                }
            }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {

            val account = completedTask.result
            if (account != null) {
                val personName = account.displayName
                val personGivenName = account.givenName
                val personFamilyName = account.familyName
                val personEmail = account.email
                val personId = account.id
            }
            mListener?.onGoogleLogin(account.email, account.displayName, account.id)
            firebaseAnalytics.logEvent("Sign_Up") {
                param("Name", account.displayName)
                param("Email", account.email)
                // param(FirebaseAnalytics.Param.CONTENT_TYPE, "image")
            }
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            //Log.e("TAG", "signInResult:failed code=" + e.getMessage(activity))
            //updateUI(null)
        }
    }

}
