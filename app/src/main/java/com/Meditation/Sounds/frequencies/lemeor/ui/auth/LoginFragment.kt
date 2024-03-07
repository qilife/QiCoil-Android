package com.Meditation.Sounds.frequencies.lemeor.ui.auth

import android.app.Activity.RESULT_CANCELED
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.BuildConfig
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.api.exception.ApiException
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.Resource
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.hideKeyboard
import com.Meditation.Sounds.frequencies.lemeor.showAlert
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.codeLanguage
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.isFirstSync
import com.Meditation.Sounds.frequencies.lemeor.ui.main.HomeViewModel
import com.Meditation.Sounds.frequencies.models.Language
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.LanguageUtils
import com.Meditation.Sounds.frequencies.utils.Utils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.tonyodev.fetch2core.isNetworkAvailable
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class LoginFragment : Fragment() {

    interface OnLoginListener {
        fun onLoginInteraction(email: String, password: String)
        fun onOpenRegistration()
        fun onOpenForgotPassword()
        fun onGoogleLogin(email: String, name: String, google_id: String)
        fun onFbLogin(email: String, name: String, fb_id: String)
    }

    private var mListener: OnLoginListener? = null
    private var RC_SIGN_IN = 100
    private var id = ""
    private var name = ""
    private var email = ""
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var mViewModel: HomeViewModel

    private val languages by lazy {
        LanguageUtils.getLanguages(requireContext()).toMutableList().sortedBy {
            if (it.code == PreferenceHelper.preference(requireContext()).codeLanguage) 0 else 1
        }
    }

    private val languageAdapter by lazy {
        CustomSpinnerAdapter(
            requireActivity(),
            languages
        )
    }


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
        mViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        )[HomeViewModel::class.java]
        firebaseAnalytics = Firebase.analytics
        mTvSignUp.text = Html.fromHtml(getString(R.string.tv_link_sign_up))
        mTvForgotPassword.text = Html.fromHtml(getString(R.string.tv_forgotten_password))
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

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
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }


        spLanguage.adapter = languageAdapter
        spLanguage.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?, position: Int, id: Long
            ) {
                parent ?: return
                view ?: return

                val lang: Language = languages[position]
                if (lang.code != PreferenceHelper.preference(requireContext()).codeLanguage) {
                    LanguageUtils.changeLanguage(requireContext(), lang.code)
                    clearData()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }

    private fun clearData() {
        val database = DataBase.getInstance(requireContext())
        CoroutineScope(Dispatchers.IO).launch {
            database.homeDao().clear()
            database.tierDao().clear()
            database.categoryDao().clear()
            database.tagDao().clear()
            database.albumDao().clear()
            database.trackDao().clear()
            database.programDao().clear()
            database.playlistDao().clear()
        }
    }

    private fun syncData() {
        if (requireActivity().isNetworkAvailable()) {
            try {
                mViewModel.getHome("").observe(this) {
                    when (it.status) {
                        Resource.Status.SUCCESS -> {
                            if (PreferenceHelper.preference(requireContext()).isFirstSync) {
                                PreferenceHelper.preference(requireContext()).isFirstSync = true
                                if (it.data == null && !BuildConfig.IS_FREE) {
                                    try {
                                        mViewModel.loadFromCache(requireContext())
                                    } catch (_: Exception) {
                                    }
                                }
                            }
                        }

                        Resource.Status.ERROR -> {
                        }

                        Resource.Status.LOADING -> {
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun isValidLogin(): Boolean {
        if (mEdEmailSignIn.text.toString().trim().isEmpty()) {
            mEdEmailSignIn.error = getString(R.string.tv_please_enter_email)
            return false
        }
        if (!isValidEmail(mEdEmailSignIn.text.toString())) {
            mEdEmailSignIn.error = getString(R.string.tv_invalid_email)
            return false
        }
        if (mEdPasswordSignIn.text.toString().trim().isEmpty()) {
            mEdPasswordSignIn.error = getString(R.string.tv_please_enter_pass)
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

class CustomSpinnerAdapter(context: Context, list: List<Language>) :
    ArrayAdapter<Language>(context, 0, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    private fun initView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertViewNew = convertView
        if (convertViewNew == null) {
            convertViewNew = LayoutInflater.from(context)
                .inflate(R.layout.item_language_spinner, parent, false)
        }
        val textViewName = convertViewNew!!.findViewById<TextView>(R.id.tvCountries)
        val imageView: AppCompatImageView = convertViewNew.findViewById(R.id.imgFlag)
        val currentItem = getItem(position)


        textViewName.text = currentItem?.name ?: "English"
        imageView.setImageResource(currentItem?.image ?: R.drawable.ic_england_flag)
        return convertViewNew
    }
}