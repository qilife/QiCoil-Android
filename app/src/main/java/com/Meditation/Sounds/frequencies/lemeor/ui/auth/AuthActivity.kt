package com.Meditation.Sounds.frequencies.lemeor.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.BuildConfig
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.api.ApiConfig.getPassResetUrl
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.AuthResponse
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.Resource
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.tools.HudHelper
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.isLogged
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.preference
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.saveUser
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.token
import com.Meditation.Sounds.frequencies.lemeor.ui.TrialActivity
import com.Meditation.Sounds.frequencies.lemeor.ui.auth.LoginFragment.OnLoginListener
import com.Meditation.Sounds.frequencies.lemeor.ui.auth.RegistrationFragment.OnRegistrationListener
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AppsFlyerLib

class AuthActivity : AppCompatActivity(), OnLoginListener, OnRegistrationListener {

    private lateinit var mViewModel: AuthViewModel

    override fun onBackPressed() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        mViewModel = ViewModelProvider(
            this,
            ViewModelFactory(
                ApiHelper(RetrofitBuilder(applicationContext).apiService),
                DataBase.getInstance(applicationContext)
            )
        ).get(AuthViewModel::class.java)

        replaceFragment(LoginFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()

        transaction.replace(R.id.auth_container, fragment, fragment.javaClass.simpleName)

        if (!supportFragmentManager.isStateSaved) {
            transaction.commit()
        } else {
            transaction.commitAllowingStateLoss()
        }
    }

    private fun saveAuthData(resource: Resource<AuthResponse>) {
        preference(applicationContext).isLogged = true
        preference(applicationContext).token = resource.data?.token
        saveUser(applicationContext, resource.data?.user)

        resource.data?.user?.let { user -> updateUnlocked(applicationContext, user, true) }
    }

    private fun sendData() {
        val intent = Intent()
        setResult(RESULT_OK, intent)
        if (!BuildConfig.IS_FREE) {
            val intent1 = Intent(this, TrialActivity::class.java)
            startActivity(intent1)
        }
        finish()
    }

    override fun onLoginInteraction(email: String, password: String) {
        if (email.toString().equals("guest")) {
            sendData()
            val eventValues = HashMap<String, Any>()
            eventValues.put(AFInAppEventParameterName.REVENUE, 0)
            AppsFlyerLib.getInstance().logEvent(
                getApplicationContext(),
                "guest_login",
                eventValues
            )
        } else {
            mViewModel.login(email, password).observe(this, {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            HudHelper.hide()

                            saveAuthData(resource)

                            sendData()
                            val eventValues = HashMap<String, Any>()
                            eventValues.put(AFInAppEventParameterName.REVENUE, 0)
                            AppsFlyerLib.getInstance().logEvent(
                                getApplicationContext(),
                                "login",
                                eventValues
                            )
                        }
                        Resource.Status.ERROR -> {
                            HudHelper.hide()
                            Toast.makeText(applicationContext, it.message, Toast.LENGTH_LONG).show()
                        }
                        Resource.Status.LOADING -> {
                            HudHelper.show(this)
                        }
                    }
                }
            })
        }
    }

    override fun onOpenRegistration() {
        replaceFragment(RegistrationFragment())
    }

    override fun onRegistrationInteraction(
        name: String,
        email: String,
        pass: String,
        confirm: String,
        uuid: String
    ) {
        mViewModel.register(email, pass, confirm, name, uuid).observe(this, {
            it?.let { resource ->
                when (resource.status) {
                    Resource.Status.SUCCESS -> {
                        HudHelper.hide()

                        saveAuthData(resource)

                        sendData()
                        val eventValues = HashMap<String, Any>()
                        eventValues.put(AFInAppEventParameterName.REVENUE, 0)
                        AppsFlyerLib.getInstance().logEvent(
                            getApplicationContext(),
                            "register",
                            eventValues
                        )
                    }
                    Resource.Status.ERROR -> {
                        HudHelper.hide()
                        Toast.makeText(applicationContext, it.message, Toast.LENGTH_LONG).show()
                    }
                    Resource.Status.LOADING -> {
                        HudHelper.show(this)
                    }
                }
            }
        })
    }

    override fun onOpenLogin() {
        replaceFragment(LoginFragment())
    }

    override fun onOpenForgotPassword() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getPassResetUrl())))
    }

    override fun onGoogleLogin(email: String, name: String, google_id: String) {
        mViewModel.googleLogin(email, name, google_id).observe(this, {
            it?.let { resource ->
                when (resource.status) {
                    Resource.Status.SUCCESS -> {
                        HudHelper.hide()

                        saveAuthData(resource)

                        sendData()
                    }
                    Resource.Status.ERROR -> {
                        HudHelper.hide()
                        Toast.makeText(applicationContext, it.message, Toast.LENGTH_LONG).show()
                    }
                    Resource.Status.LOADING -> {
                        HudHelper.show(this)
                    }
                }
            }
        })
    }


    override fun onFbLogin(email: String, name: String, fb_id: String) {
        mViewModel.fbLogin(email, name, fb_id).observe(this, {
            it?.let { resource ->
                when (resource.status) {
                    Resource.Status.SUCCESS -> {
                        HudHelper.hide()

                        saveAuthData(resource)

                        sendData()
                    }
                    Resource.Status.ERROR -> {
                        HudHelper.hide()
                        Toast.makeText(applicationContext, it.message, Toast.LENGTH_LONG).show()
                    }
                    Resource.Status.LOADING -> {
                        HudHelper.show(this)
                    }
                }
            }
        })
    }
}