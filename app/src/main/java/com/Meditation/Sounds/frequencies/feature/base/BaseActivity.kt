package com.Meditation.Sounds.frequencies.feature.base

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.*
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.Meditation.Sounds.frequencies.QApplication
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.api.exception.ApiException
import com.Meditation.Sounds.frequencies.api.models.GetFlashSaleOutput
import com.Meditation.Sounds.frequencies.feature.main.MainActivity
import com.Meditation.Sounds.frequencies.utilbilling.IabBroadcastReceiver
import com.Meditation.Sounds.frequencies.utilbilling.Purchase
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.views.SubscriptionDialogFlashSale
import com.Meditation.Sounds.frequencies.views.SubscriptionDialogNormal
import com.google.gson.Gson
import java.io.IOException

/**
 * Created by Admin on 3/22/2017.
 */

abstract class BaseActivity : AppCompatActivity(), IabBroadcastReceiver.IabBroadcastListener {

    private var mProgressDialog: ProgressDialog? = null
    //in-app
    // Will the subscription auto-renew?
    var mAutoRenewEnabled = false

    // (arbitrary) request code for the purchase flow
    val RC_REQUEST = 10001
    // The helper object
//    var mHelper: IabHelper? = null
    // Provides purchase notification while this app is running
    var mBroadcastReceiver: IabBroadcastReceiver? = null

    protected var mFragment: Fragment? = null

    protected abstract fun initLayout(): Int

    protected abstract fun initComponents()

    protected abstract fun addListener()

//    // Listener that's called when we finish querying the items and subscriptions we own
//    internal var mGotInventoryListener: IabHelper.QueryInventoryFinishedListener = object : IabHelper.QueryInventoryFinishedListener {
//        override fun onQueryInventoryFinished(result: IabResult, inventory: Inventory) {
//
//            // Have we been disposed of in the meantime? If so, quit.
//            if (mHelper == null) return
//
//            // Is it a failure?
//            if (result.isFailure()) {
//                complain("Failed to query inventory: $result")
//                return
//            }
//            //            Log.d(TAG, "Query inventory was successful.");
//
//            val detail7Day = inventory.getSkuDetails(Constants.SKU_RIFE_FREE)
//            val detailMonth = inventory.getSkuDetails(Constants.SKU_RIFE_MONTHLY)
//            val detailYear = inventory.getSkuDetails(Constants.SKU_RIFE_YEARLY)
//            val detail7DayFlashsale = inventory.getSkuDetails(Constants.SKU_RIFE_FREE_FLASHSALE)
//            val detailMonthFlashsale = inventory.getSkuDetails(Constants.SKU_RIFE_MONTHLY_FLASHSALE)
//            val detailYearFlashsale = inventory.getSkuDetails(Constants.SKU_RIFE_YEARLY_FLASHSALE)
//            val detailLifeTime = inventory.getSkuDetails(Constants.SKU_RIFE_LIFETIME)
//            val detailLifeTimeFlashsale = inventory.getSkuDetails(Constants.SKU_RIFE_LIFETIME_FLASHSALE)
//
//            if (detail7Day != null) {
//                SharedPreferenceHelper.getInstance().setPriceByCurrency(Constants.PRICE_7_DAY_TRIAL, detail7Day.price)
//            }
//            if (detailMonth != null) {
//                SharedPreferenceHelper.getInstance().setPriceByCurrency(Constants.PRICE_1_MONTH, detailMonth.price)
//            }
//            if (detailYear != null) {
//                SharedPreferenceHelper.getInstance().setPriceByCurrency(Constants.PRICE_1_YEAR, detailYear.price)
//            }
//            if (detail7DayFlashsale != null) {
//                SharedPreferenceHelper.getInstance().setPriceByCurrency(Constants.PRICE_7_DAY_TRIAL_FLASH_SALE, detail7DayFlashsale.price)
//            }
//            if (detailMonthFlashsale != null) {
//                SharedPreferenceHelper.getInstance().setPriceByCurrency(Constants.PRICE_1_MONTH_FLASH_SALE, detailMonthFlashsale.price)
//            }
//            if (detailYearFlashsale != null) {
//                SharedPreferenceHelper.getInstance().setPriceByCurrency(Constants.PRICE_1_YEAR_FLASH_SALE, detailYearFlashsale.price)
//            }
//            if (detailLifeTime != null) {
//                SharedPreferenceHelper.getInstance().setPriceByCurrency(Constants.PRICE_LIFETIME, detailLifeTime.price)
//            }
//            if (detailLifeTimeFlashsale != null) {
//                SharedPreferenceHelper.getInstance().setPriceByCurrency(Constants.PRICE_LIFETIME_FLASH_SALE, detailLifeTimeFlashsale.price)
//            }
//
//            /*advanced*/
//            val detailAdvancedFreeTrial = inventory.getSkuDetails(Constants.SKU_RIFE_ADVANCED_FREE_TRIAL)
//            val detailAdvancedMonthly = inventory.getSkuDetails(Constants.SKU_RIFE_ADVANCED_MONTHLY)
//            val detailAdvancedAnnual = inventory.getSkuDetails(Constants.SKU_RIFE_ADVANCED_YEAR)
//            val detailAdvancedLifeTime = inventory.getSkuDetails(Constants.SKU_RIFE_ADVANCED_LIFETIME)
//            val detailAdvancedFreeTrialFlashsale = inventory.getSkuDetails(Constants.SKU_RIFE_ADVANCED_FREE_TRIAL_FLASHSALE)
//            val detailAdvancedMonthlyFlashsale = inventory.getSkuDetails(Constants.SKU_RIFE_ADVANCED_MONTHLY_FLASHSALE)
//            val detailAdvancedAnnualFlashsale = inventory.getSkuDetails(Constants.SKU_RIFE_ADVANCED_YEAR_FLASHSALE)
//            val detailAdvancedLifeTimeFlashsale = inventory.getSkuDetails(Constants.SKU_RIFE_ADVANCED_LIFETIME_FLASHSALE)
//            //advanced
//            if (detailAdvancedFreeTrial != null) {
//                SharedPreferenceHelper.getInstance().setPriceByCurrency(Constants.PRICE_ADVANCED_FREE_TRIAL, detailAdvancedFreeTrial.price)
//            }
//            if (detailAdvancedMonthly != null) {
//                SharedPreferenceHelper.getInstance().setPriceByCurrency(Constants.PRICE_ADVANCED_MONTHLY, detailAdvancedMonthly.price)
//            }
//            if (detailAdvancedAnnual != null) {
//                SharedPreferenceHelper.getInstance().setPriceByCurrency(Constants.PRICE_ADVANCED_YEAR, detailAdvancedAnnual.price)
//            }
//            if (detailAdvancedLifeTime != null) {
//                SharedPreferenceHelper.getInstance().setPriceByCurrency(Constants.PRICE_ADVANCED_LIFETIME, detailAdvancedLifeTime.price)
//            }
//            if (detailAdvancedFreeTrialFlashsale != null) {
//                SharedPreferenceHelper.getInstance().setPriceByCurrency(Constants.PRICE_ADVANCED_FREE_TRIAL_FLASH_SALE, detailAdvancedFreeTrialFlashsale.price)
//            }
//            if (detailAdvancedMonthlyFlashsale != null) {
//                SharedPreferenceHelper.getInstance().setPriceByCurrency(Constants.PRICE_ADVANCED_MONTHLY_FLASH_SALE, detailAdvancedMonthlyFlashsale.price)
//            }
//            if (detailAdvancedAnnualFlashsale != null) {
//                SharedPreferenceHelper.getInstance().setPriceByCurrency(Constants.PRICE_ADVANCED_YEAR_FLASH_SALE, detailAdvancedAnnualFlashsale.price)
//            }
//            if (detailAdvancedLifeTimeFlashsale != null) {
//                SharedPreferenceHelper.getInstance().setPriceByCurrency(Constants.PRICE_ADVANCED_LIFETIME_FLASH_SALE, detailAdvancedLifeTimeFlashsale.price)
//            }
//            //Higher
//            val detailHigherMonthly = inventory.getSkuDetails(Constants.SKU_RIFE_HIGHER_MONTHLY)
//            val detailHigherAnnual = inventory.getSkuDetails(Constants.SKU_RIFE_HIGHER_ANNUAL)
//            val detailHigherLifeTime = inventory.getSkuDetails(Constants.SKU_RIFE_HIGHER_LIFETIME)
//            val detailHigherAnnualFlashsale = inventory.getSkuDetails(Constants.SKU_RIFE_HIGHER_ANNUAL_FLASH_SALE)
//            val detailHigherLifeTimeFlashsale = inventory.getSkuDetails(Constants.SKU_RIFE_HIGHER_LIFETIME_FLASH_SALE)
//
//            if (detailHigherMonthly != null) {
//                SharedPreferenceHelper.getInstance().setPriceByCurrency(Constants.PRICE_HIGHER_MONTHLY, detailHigherMonthly.price)
//            }
//            if (detailHigherAnnual != null) {
//                SharedPreferenceHelper.getInstance().setPriceByCurrency(Constants.PRICE_HIGHER_ANNUAL, detailHigherAnnual.price)
//            }
//            if (detailHigherLifeTime != null) {
//                SharedPreferenceHelper.getInstance().setPriceByCurrency(Constants.PRICE_HIGHER_LIFETIME, detailHigherLifeTime.price)
//            }
//            if (detailHigherAnnualFlashsale != null) {
//                SharedPreferenceHelper.getInstance().setPriceByCurrency(Constants.PRICE_HIGHER_ANNUAL_FLASH_SALE, detailHigherAnnualFlashsale.price)
//            }
//            if (detailHigherLifeTimeFlashsale != null) {
//                SharedPreferenceHelper.getInstance().setPriceByCurrency(Constants.PRICE_HIGHER_LIFETIME_FLASH_SALE, detailHigherLifeTimeFlashsale.price)
//            }
//
//            // First find out which subscription is auto renewing
//            val rifeSkuFree = inventory.getPurchase(Constants.SKU_RIFE_FREE)
//            val rifeSkuMonth = inventory.getPurchase(Constants.SKU_RIFE_MONTHLY)
//            val rifeSkuYear = inventory.getPurchase(Constants.SKU_RIFE_YEARLY)
//            val rifeSkuFreeFlasesale = inventory.getPurchase(Constants.SKU_RIFE_FREE_FLASHSALE)
//            val rifeSkuMonthFlasesale = inventory.getPurchase(Constants.SKU_RIFE_MONTHLY_FLASHSALE)
//            val rifeSkuYearFlasesale = inventory.getPurchase(Constants.SKU_RIFE_YEARLY_FLASHSALE)
//
//            val rifeSkuLifeTime = inventory.getPurchase(Constants.SKU_RIFE_LIFETIME)
//            val rifeSkuLifeTimeFlasesale = inventory.getPurchase(Constants.SKU_RIFE_LIFETIME_FLASHSALE)
//
//            val rifeSkuAdvancedFreeTrial = inventory.getPurchase(Constants.SKU_RIFE_ADVANCED_FREE_TRIAL)
//            val rifeSkuAdvancedMonthly = inventory.getPurchase(Constants.SKU_RIFE_ADVANCED_MONTHLY)
//            val rifeSkuAdvancedAnnual = inventory.getPurchase(Constants.SKU_RIFE_ADVANCED_YEAR)
//            val rifeSkuAdvancedLifeTime = inventory.getPurchase(Constants.SKU_RIFE_ADVANCED_LIFETIME)
//            val rifeSkuAdvancedFreeTrialFlashsale = inventory.getPurchase(Constants.SKU_RIFE_ADVANCED_FREE_TRIAL_FLASHSALE)
//            val rifeSkuAdvancedMonthlyFlashsale = inventory.getPurchase(Constants.SKU_RIFE_ADVANCED_MONTHLY_FLASHSALE)
//            val rifeSkuAdvancedAnnualFlashsale = inventory.getPurchase(Constants.SKU_RIFE_ADVANCED_YEAR_FLASHSALE)
//            val rifeSkuAdvancedLifeTimeFlashsale = inventory.getPurchase(Constants.SKU_RIFE_ADVANCED_LIFETIME_FLASHSALE)
//
//            if ((rifeSkuFree != null && rifeSkuFree.isAutoRenewing()) || (rifeSkuFreeFlasesale != null && rifeSkuFreeFlasesale.isAutoRenewing())) {
//                mAutoRenewEnabled = true
//                SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_DEVICE, true)
//                SharedPreferenceHelper.getInstance().setInt(Constants.INApp_PACKED, Constants.INApp_FREE)
//                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
//                sendBroadcast(intent)
//
//                QcAlarmManager.clearAlarms(this@BaseActivity)
//            } else if ((rifeSkuMonth != null && rifeSkuMonth.isAutoRenewing()) || (rifeSkuMonthFlasesale != null && rifeSkuMonthFlasesale.isAutoRenewing())) {
//                mAutoRenewEnabled = true
//                SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_DEVICE, true)
//                SharedPreferenceHelper.getInstance().setInt(Constants.INApp_PACKED, Constants.INApp_1_MONTH_25)
//                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
//                sendBroadcast(intent)
//
//                QcAlarmManager.clearAlarms(this@BaseActivity)
//            } else if ((rifeSkuYear != null && rifeSkuYear.isAutoRenewing()) || (rifeSkuYearFlasesale != null && rifeSkuYearFlasesale.isAutoRenewing())) {
//                mAutoRenewEnabled = true
//                SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_DEVICE, true)
//                SharedPreferenceHelper.getInstance().setInt(Constants.INApp_PACKED, Constants.INApp_1_MONTH_99)
//                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
//                sendBroadcast(intent)
//
//                QcAlarmManager.clearAlarms(this@BaseActivity)
//            } else if ((rifeSkuLifeTime != null && rifeSkuLifeTime.token != null)
//                    || (rifeSkuLifeTimeFlasesale != null && rifeSkuLifeTimeFlasesale.token != null)) {
//                mAutoRenewEnabled = true
//                SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_DEVICE, true)
//                SharedPreferenceHelper.getInstance().setInt(Constants.INApp_PACKED, Constants.INApp_LIFETIME)
//                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
//                sendBroadcast(intent)
//                QcAlarmManager.clearAlarms(this@BaseActivity)
//            } else {
//                mAutoRenewEnabled = false
//                SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_DEVICE, false)
//                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
//                sendBroadcast(intent)
//            }
//
//            /**/
//            if ((rifeSkuAdvancedFreeTrial != null && rifeSkuAdvancedFreeTrial.isAutoRenewing)
//                    || (rifeSkuAdvancedFreeTrialFlashsale != null && rifeSkuAdvancedFreeTrialFlashsale.isAutoRenewing)) {
//                mAutoRenewEnabled = true
//                SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_ADVANCED_DEVICE, true)
//                SharedPreferenceHelper.getInstance().setInt(Constants.INApp_ADVANCED_PACKED, Constants.INApp_ADVANCED_FREE_TRIAL)
//                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
//                sendBroadcast(intent)
//
//                QcAlarmManager.clearAlarms(this@BaseActivity)
//            } else if ((rifeSkuAdvancedMonthly != null && rifeSkuAdvancedMonthly.isAutoRenewing)
//                    || (rifeSkuAdvancedMonthlyFlashsale != null && rifeSkuAdvancedMonthlyFlashsale.isAutoRenewing)) {
//                mAutoRenewEnabled = true
//                SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_ADVANCED_DEVICE, true)
//                SharedPreferenceHelper.getInstance().setInt(Constants.INApp_ADVANCED_PACKED, Constants.INApp_ADVANCED_MONTHLY)
//                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
//                sendBroadcast(intent)
//                QcAlarmManager.clearAlarms(this@BaseActivity)
//            } else if ((rifeSkuAdvancedAnnual != null && rifeSkuAdvancedAnnual.isAutoRenewing)
//                    || (rifeSkuAdvancedAnnualFlashsale != null && rifeSkuAdvancedAnnualFlashsale.isAutoRenewing)) {
//                mAutoRenewEnabled = true
//                SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_ADVANCED_DEVICE, true)
//                SharedPreferenceHelper.getInstance().setInt(Constants.INApp_ADVANCED_PACKED, Constants.INApp_ADVANCED_YEAR)
//                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
//                sendBroadcast(intent)
//                QcAlarmManager.clearAlarms(this@BaseActivity)
//            } else if ((rifeSkuAdvancedLifeTime != null && rifeSkuAdvancedLifeTime.token != null)
//                    || (rifeSkuAdvancedLifeTimeFlashsale != null && rifeSkuAdvancedLifeTimeFlashsale.token != null)) {
//                mAutoRenewEnabled = true
//                SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_ADVANCED_DEVICE, true)
//                SharedPreferenceHelper.getInstance().setInt(Constants.INApp_ADVANCED_PACKED, Constants.INApp_ADVANCED_LIFETIME)
//                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
//                sendBroadcast(intent)
//                QcAlarmManager.clearAlarms(this@BaseActivity)
//            } else {
//                mAutoRenewEnabled = false
//                SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_ADVANCED_DEVICE, false)
//                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
//                sendBroadcast(intent)
//            }
//
//            val rifeSkuHigherMonthly = inventory.getPurchase(Constants.SKU_RIFE_HIGHER_MONTHLY)
//            val rifeSkuHigherAnnual = inventory.getPurchase(Constants.SKU_RIFE_HIGHER_ANNUAL)
//            val rifeSkuHigherLifeTime = inventory.getPurchase(Constants.SKU_RIFE_HIGHER_LIFETIME)
//            val rifeSkuHigherAnnualFlashsale = inventory.getPurchase(Constants.SKU_RIFE_HIGHER_ANNUAL_FLASH_SALE)
//            val rifeSkuHigherLifeTimeFlashsale = inventory.getPurchase(Constants.SKU_RIFE_HIGHER_LIFETIME_FLASH_SALE)
//            if ((rifeSkuHigherMonthly != null && rifeSkuHigherMonthly.isAutoRenewing)) {
//                mAutoRenewEnabled = true
//                SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_HIGHER_DEVICE, true)
//                SharedPreferenceHelper.getInstance().setInt(Constants.INApp_HIGHER_PACKED, Constants.INApp_HIGHER_MONTHLY)
//                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
//                sendBroadcast(intent)
//                QcAlarmManager.clearAlarms(this@BaseActivity)
//            } else if ((rifeSkuHigherAnnual != null && rifeSkuHigherAnnual.isAutoRenewing)
//                    || (rifeSkuHigherAnnualFlashsale != null && rifeSkuHigherAnnualFlashsale.isAutoRenewing)) {
//                mAutoRenewEnabled = true
//                SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_HIGHER_DEVICE, true)
//                SharedPreferenceHelper.getInstance().setInt(Constants.INApp_HIGHER_PACKED, Constants.INApp_HIGHER_YEAR)
//                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
//                sendBroadcast(intent)
//                QcAlarmManager.clearAlarms(this@BaseActivity)
//            } else if ((rifeSkuHigherLifeTime != null && rifeSkuHigherLifeTime.token != null)
//                    || (rifeSkuHigherLifeTimeFlashsale != null && rifeSkuHigherLifeTimeFlashsale.token != null)) {
//                mAutoRenewEnabled = true
//                SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_HIGHER_DEVICE, true)
//                SharedPreferenceHelper.getInstance().setInt(Constants.INApp_HIGHER_PACKED, Constants.INApp_HIGHER_LIFETIME)
//                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
//                sendBroadcast(intent)
//                QcAlarmManager.clearAlarms(this@BaseActivity)
//            } else {
//                mAutoRenewEnabled = false
//                SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_HIGHER_DEVICE, false)
//                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
//                sendBroadcast(intent)
//            }
//            UpdateDurationOfAllPlaylistTask(this@BaseActivity.application, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
//        }
//    }
//
//    // Callback for when a purchase is finished
//    internal var mPurchaseFinishedListener: IabHelper.OnIabPurchaseFinishedListener = object : IabHelper.OnIabPurchaseFinishedListener {
//        override fun onIabPurchaseFinished(result: IabResult, purchase: Purchase?) {
//            //            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
//
//            // if we were disposed of in the meantime, quit.
//            if (mHelper == null) return
//
//            if (result.isFailure) {
//                complain("Error purchasing: $result")
//                return
//            }
//            if (!verifyDeveloperPayload(purchase!!)) {
//                complain("Error purchasing. Authenticity verification failed.")
//                return
//            }
//            if (purchase.getSku().equals(Constants.SKU_RIFE_FREE) || purchase.getSku().equals(Constants.SKU_RIFE_FREE_FLASHSALE)) {
//                // bought the rasbita subscription
//                mAutoRenewEnabled = purchase.isAutoRenewing()
//                SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_DEVICE, true)
//                SharedPreferenceHelper.getInstance().setInt(Constants.INApp_PACKED, Constants.INApp_FREE)
//                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
//                sendBroadcast(intent)
//
//                UpdateDurationOfAllPlaylistTask(this@BaseActivity.application, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
//                QcAlarmManager.clearAlarms(this@BaseActivity)
//            }
//            if (purchase.getSku().equals(Constants.SKU_RIFE_MONTHLY) || purchase.getSku().equals(Constants.SKU_RIFE_MONTHLY_FLASHSALE)) {
//                // bought the rasbita subscription
//                mAutoRenewEnabled = purchase.isAutoRenewing()
//                SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_DEVICE, true)
//                SharedPreferenceHelper.getInstance().setInt(Constants.INApp_PACKED, Constants.INApp_1_MONTH_25)
//                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
//                sendBroadcast(intent)
//
//                UpdateDurationOfAllPlaylistTask(this@BaseActivity.application, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
//                QcAlarmManager.clearAlarms(this@BaseActivity)
//            }
//            if (purchase.getSku().equals(Constants.SKU_RIFE_YEARLY) || purchase.getSku().equals(Constants.SKU_RIFE_YEARLY_FLASHSALE)) {
//                // bought the rasbita subscription
//                mAutoRenewEnabled = purchase.isAutoRenewing
//                SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_DEVICE, true)
//                SharedPreferenceHelper.getInstance().setInt(Constants.INApp_PACKED, Constants.INApp_1_MONTH_99)
//                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
//                sendBroadcast(intent)
//
//                UpdateDurationOfAllPlaylistTask(this@BaseActivity.application, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
//                QcAlarmManager.clearAlarms(this@BaseActivity)
//            }
//            if (purchase.getSku().equals(Constants.SKU_RIFE_LIFETIME) || purchase.getSku().equals(Constants.SKU_RIFE_LIFETIME_FLASHSALE)) {
//                // bought the rasbita subscription
//                mAutoRenewEnabled = purchase.isAutoRenewing
//                SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_DEVICE, true)
//                SharedPreferenceHelper.getInstance().setInt(Constants.INApp_PACKED, Constants.INApp_LIFETIME)
//                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
//                sendBroadcast(intent)
//
//                UpdateDurationOfAllPlaylistTask(this@BaseActivity.application, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
//                QcAlarmManager.clearAlarms(this@BaseActivity)
//            }
//
//            if (purchase.getSku().equals(Constants.SKU_RIFE_ADVANCED_FREE_TRIAL) || purchase.getSku().equals(Constants.SKU_RIFE_ADVANCED_FREE_TRIAL_FLASHSALE)) {
//                // bought the rasbita subscription
//                mAutoRenewEnabled = purchase.isAutoRenewing()
//                SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_ADVANCED_DEVICE, true)
//                SharedPreferenceHelper.getInstance().setInt(Constants.INApp_ADVANCED_PACKED, Constants.INApp_ADVANCED_FREE_TRIAL)
//                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
//                sendBroadcast(intent)
//
//                UpdateDurationOfAllPlaylistTask(this@BaseActivity.application, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
//                QcAlarmManager.clearAlarms(this@BaseActivity)
//            }
//            if (purchase.getSku().equals(Constants.SKU_RIFE_ADVANCED_MONTHLY) || purchase.getSku().equals(Constants.SKU_RIFE_ADVANCED_MONTHLY_FLASHSALE)) {
//                // bought the rasbita subscription
//                mAutoRenewEnabled = purchase.isAutoRenewing()
//                SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_ADVANCED_DEVICE, true)
//                SharedPreferenceHelper.getInstance().setInt(Constants.INApp_ADVANCED_PACKED, Constants.INApp_ADVANCED_MONTHLY)
//                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
//                sendBroadcast(intent)
//
//                UpdateDurationOfAllPlaylistTask(this@BaseActivity.application, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
//                QcAlarmManager.clearAlarms(this@BaseActivity)
//            }
//            if (purchase.getSku().equals(Constants.SKU_RIFE_ADVANCED_YEAR) || purchase.getSku().equals(Constants.SKU_RIFE_ADVANCED_YEAR_FLASHSALE)) {
//                // bought the rasbita subscription
//                mAutoRenewEnabled = purchase.isAutoRenewing()
//                SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_ADVANCED_DEVICE, true)
//                SharedPreferenceHelper.getInstance().setInt(Constants.INApp_ADVANCED_PACKED, Constants.INApp_ADVANCED_YEAR)
//                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
//                sendBroadcast(intent)
//
//                UpdateDurationOfAllPlaylistTask(this@BaseActivity.application, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
//                QcAlarmManager.clearAlarms(this@BaseActivity)
//            }
//            if (purchase.getSku().equals(Constants.SKU_RIFE_ADVANCED_LIFETIME) || purchase.getSku().equals(Constants.SKU_RIFE_ADVANCED_LIFETIME_FLASHSALE)) {
//                // bought the rasbita subscription
//                mAutoRenewEnabled = purchase.isAutoRenewing()
//                SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_ADVANCED_DEVICE, true)
//                SharedPreferenceHelper.getInstance().setInt(Constants.INApp_ADVANCED_PACKED, Constants.INApp_ADVANCED_LIFETIME)
//                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
//                sendBroadcast(intent)
//
//                UpdateDurationOfAllPlaylistTask(this@BaseActivity.application, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
//                QcAlarmManager.clearAlarms(this@BaseActivity)
//            }
//
//            //Higher
//            if (purchase.getSku().equals(Constants.SKU_RIFE_HIGHER_MONTHLY)) {
//                // bought the rasbita subscription
//                mAutoRenewEnabled = purchase.isAutoRenewing()
//                SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_HIGHER_DEVICE, true)
//                SharedPreferenceHelper.getInstance().setInt(Constants.INApp_HIGHER_PACKED, Constants.INApp_HIGHER_MONTHLY)
//                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
//                sendBroadcast(intent)
//
//                UpdateDurationOfAllPlaylistTask(this@BaseActivity.application, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
//                QcAlarmManager.clearAlarms(this@BaseActivity)
//            }
//            if (purchase.getSku().equals(Constants.SKU_RIFE_HIGHER_ANNUAL) || purchase.getSku().equals(Constants.SKU_RIFE_HIGHER_ANNUAL_FLASH_SALE)) {
//                // bought the rasbita subscription
//                mAutoRenewEnabled = purchase.isAutoRenewing()
//                SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_HIGHER_DEVICE, true)
//                SharedPreferenceHelper.getInstance().setInt(Constants.INApp_HIGHER_PACKED, Constants.INApp_HIGHER_YEAR)
//                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
//                sendBroadcast(intent)
//
//                UpdateDurationOfAllPlaylistTask(this@BaseActivity.application, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
//                QcAlarmManager.clearAlarms(this@BaseActivity)
//            }
//            if (purchase.getSku().equals(Constants.SKU_RIFE_HIGHER_LIFETIME) || purchase.getSku().equals(Constants.SKU_RIFE_HIGHER_LIFETIME_FLASH_SALE)) {
//                // bought the rasbita subscription
//                mAutoRenewEnabled = purchase.isAutoRenewing()
//                SharedPreferenceHelper.getInstance().setBool(Constants.KEY_PURCHASED_HIGHER_DEVICE, true)
//                SharedPreferenceHelper.getInstance().setInt(Constants.INApp_HIGHER_PACKED, Constants.INApp_HIGHER_LIFETIME)
//                val intent = Intent(Constants.BROADCAST_ACTION_PURCHASED)
//                sendBroadcast(intent)
//
//                UpdateDurationOfAllPlaylistTask(this@BaseActivity.application, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
//                QcAlarmManager.clearAlarms(this@BaseActivity)
//            }
//        }
//    }

    var isResumedActivity = false

    private val broadcastReceiverFlashSaleNotification = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            if (intent!!.hasExtra(Constants.ETRAX_FLASH_SALE_TYPE)) {
                val type = intent.getIntExtra(Constants.ETRAX_FLASH_SALE_TYPE, 0)
                val topActivity = (application as QApplication).topActivity
                if (!SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED)
                        || !SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_ADVANCED)
                        || !SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_HIGH_ABUNDANCE)
                        || !SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_HIGH_QUANTUM)) {
                    if (type > 0 && type != Constants.ETRAX_REMINDER_NOTIFICATION) {
                        if (topActivity != null) {
                            if (topActivity::class.java != this@BaseActivity::class.java) {
                                dismissSubscription()
                            } else {
                                if (this@BaseActivity is MainActivity) {
                                    (this@BaseActivity as MainActivity).loadCountdownTime()
                                }
                                if (isResumedActivity) {
                                    dismissSubscription()
//                                    showSubscriptionDialog()
//                                    showDialogSubscriptionFS(false)
                                } else {
                                    val intentMain = Intent(this@BaseActivity, MainActivity::class.java)
                                    intentMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                            Intent.FLAG_ACTIVITY_NEW_TASK)
                                    intentMain.putExtra(Constants.ETRAX_FLASH_SALE_TYPE, type)
                                    startActivity(intentMain)
                                }
                            }
                        } else {
                            dismissSubscription()
                        }
                    }
                }
                if (type == Constants.ETRAX_REMINDER_NOTIFICATION) {
                    if (topActivity::class.java == this@BaseActivity::class.java && !isResumedActivity) {
                        val intentMain = Intent(this@BaseActivity, MainActivity::class.java)
                        intentMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                Intent.FLAG_ACTIVITY_NEW_TASK)
                        intentMain.putExtra(Constants.ETRAX_FLASH_SALE_TYPE, type)
                        startActivity(intentMain)
                    }
                }
            }
        }
    }

    private val broadcastReceiverPurchase2 = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            if (SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED)) {

            }
            loadDs()
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setCancelable(false)
        mProgressDialog!!.setMessage(getString(R.string.txt_waiting))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                broadcastReceiverFlashSaleNotification,
                IntentFilter(Constants.ACTION_RECEIVE_FLASHSALE_NOTIFICATION),
                RECEIVER_EXPORTED
            )
            registerReceiver(
                broadcastReceiverPurchase2,
                IntentFilter(Constants.BROADCAST_ACTION_PURCHASED),
                RECEIVER_EXPORTED
            )
        } else {
            registerReceiver(
                broadcastReceiverFlashSaleNotification,
                IntentFilter(Constants.ACTION_RECEIVE_FLASHSALE_NOTIFICATION)
            )
            registerReceiver(
                broadcastReceiverPurchase2,
                IntentFilter(Constants.BROADCAST_ACTION_PURCHASED)
            )
        }

        requestedOrientation = if (Utils.isTablet(this)) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        val layoutId = initLayout()
        if (layoutId != 0) {
            setContentView(layoutId)
        }
        initNavigation()
        initComponents()
        addListener()
//        initHelper()

        (application as QApplication).addActivityToStack(this)
    }

    fun loadDs() {
        val jsonOrgrialString = SharedPreferenceHelper.getInstance().get(Constants.PREF_FLASH_SALE)
        if (jsonOrgrialString != null && jsonOrgrialString.length > 0) {
            val flashSaleOutput = Gson().fromJson<GetFlashSaleOutput>(jsonOrgrialString, GetFlashSaleOutput::class.java!!)

            if (flashSaleOutput.advertisements != null && flashSaleOutput.advertisements.enableBanner!! && (this is MainActivity)) {

            }
        }
    }

    fun setMessageLoadingDialog(title: String) {
        if (mProgressDialog != null) {
            mProgressDialog!!.setMessage(title)
        }
    }

    fun showLoading(isShow: Boolean) {
        try {
            if (isShow) {
                mProgressDialog!!.show()
            } else {
                if (mProgressDialog!!.isShowing()) {
                    mProgressDialog!!.dismiss()
                }
            }
        } catch (ex: IllegalArgumentException) {
        }

    }

    override fun onResume() {
        super.onResume()
        loadDs()
        isResumedActivity = true
    }

    override fun onPause() {
        super.onPause()
        isResumedActivity = false
    }

    override fun onStop() {
        super.onStop()
        isResumedActivity = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isResumedActivity = false
        unregisterReceiver(broadcastReceiverFlashSaleNotification)
        unregisterReceiver(broadcastReceiverPurchase2)
        try {
            unregisterReceiver(mBroadcastReceiver)
        } catch (e: IllegalArgumentException) {

        }
        (application as QApplication).removeActivityToStack(this)
    }

    private fun initNavigation() {
    }

    protected var isShowSubscription = false

    fun showDialogSubscriptionFS(type: Int) {
        val mSubscription = SubscriptionDialogFlashSale(this)
        mSubscription.setTypeAlbum(type)
        mSubscription.show()
    }

    fun showDialogSubscriptionNormal(type: Int) {
        val mSubscription = SubscriptionDialogNormal(this)
        mSubscription.setTypeAlbum(type)
        mSubscription.show()
    }

    fun dismissSubscription() {
        val flashSale = Utils.getFlashSaleRemainTime()
        if (flashSale > 0) {
            val mSubscription = SubscriptionDialogFlashSale(this)
            if (mSubscription.isShowing) {
                mSubscription.dismiss()
            }
        } else {
            val mSubscription = SubscriptionDialogNormal(this)
            if (mSubscription.isShowing) {
                mSubscription.dismiss()
            }
        }
        isShowSubscription = true
    }

//    private fun initHelper() {
//        // Create the helper, passing it our context and the public key to verify signatures with
//        mHelper = IabHelper(this, Constants.base64EncodedPublicKey)
//
//        // enable debug logging (for a production application, you should set this to false).
//        mHelper!!.enableDebugLogging(true)
//
//        // Start setup. This is asynchronous and the specified listener
//        // will be called once setup completes.
//        mHelper!!.startSetup(object : IabHelper.OnIabSetupFinishedListener {
//            override fun onIabSetupFinished(result: IabResult) {
//                if (!result.isSuccess) {
//                    // Oh noes, there was a problem.
//                    //                    complain("Problem setting up in-app billing: " + result);
//                    complain("In App Set UP error:: Please check gmail account settings/ Credit Card Info etc")
//                    return
//                }
//                // Have we been disposed of in the meantime? If so, quit.
//                if (mHelper == null) return
//                mBroadcastReceiver = IabBroadcastReceiver(this@BaseActivity)
//                val broadcastFilter = IntentFilter(IabBroadcastReceiver.ACTION)
//                registerReceiver(mBroadcastReceiver, broadcastFilter)
//
//                // IAB is fully set up. Now, let's get an inventory of stuff we own.
//                //                Log.d(TAG, "Setup successful. Querying inventory.");
//                try {
//                    val skuList = ArrayList<String>()
//                    skuList.add(Constants.SKU_RIFE_FREE)
//                    skuList.add(Constants.SKU_RIFE_FREE_FLASHSALE)
//
//                    skuList.add(Constants.SKU_RIFE_MONTHLY)
//                    skuList.add(Constants.SKU_RIFE_MONTHLY_FLASHSALE)
//                    skuList.add(Constants.SKU_RIFE_YEARLY)
//                    skuList.add(Constants.SKU_RIFE_YEARLY_FLASHSALE)
//                    skuList.add(Constants.SKU_RIFE_LIFETIME)
//                    skuList.add(Constants.SKU_RIFE_LIFETIME_FLASHSALE)
//
//                    skuList.add(Constants.SKU_RIFE_ADVANCED_FREE_TRIAL)
//                    skuList.add(Constants.SKU_RIFE_ADVANCED_FREE_TRIAL_FLASHSALE)
//                    skuList.add(Constants.SKU_RIFE_ADVANCED_MONTHLY)
//                    skuList.add(Constants.SKU_RIFE_ADVANCED_MONTHLY_FLASHSALE)
//                    skuList.add(Constants.SKU_RIFE_ADVANCED_YEAR)
//                    skuList.add(Constants.SKU_RIFE_ADVANCED_YEAR_FLASHSALE)
//                    skuList.add(Constants.SKU_RIFE_ADVANCED_LIFETIME)
//                    skuList.add(Constants.SKU_RIFE_ADVANCED_LIFETIME_FLASHSALE)
//
//                    skuList.add(Constants.SKU_RIFE_HIGHER_MONTHLY)
//                    skuList.add(Constants.SKU_RIFE_HIGHER_ANNUAL)
//                    skuList.add(Constants.SKU_RIFE_HIGHER_ANNUAL_FLASH_SALE)
//                    skuList.add(Constants.SKU_RIFE_HIGHER_LIFETIME)
//                    skuList.add(Constants.SKU_RIFE_HIGHER_LIFETIME_FLASH_SALE)
//
//                    mHelper!!.queryInventoryAsync(true, null, skuList, mGotInventoryListener)
//                } catch (e: IabHelper.IabAsyncInProgressException) {
//                    complain("Error querying inventory. Another async operation in progress.")
//                }
//
//            }
//        })
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (mHelper == null) return
//        // Pass on the activity result to the helper for handling
//        if (!mHelper!!.handleActivityResult(requestCode, resultCode, data)) {
//            // not handled, so handle it ourselves (here's where you'd
//            // perform any handling of activity results not related to in-app
//            // billing...
//            super.onActivityResult(requestCode, resultCode, data)
//        } else {
//            //            Log.d(TAG, "onActivityResult handled by IABUtil.");
//        }
//    }

    fun showNavigation(imageView: ImageView?, resId: Int, listener: View.OnClickListener?) {
        if (imageView != null) {
            imageView.setImageResource(resId)
            if (listener != null) {
                imageView.setOnClickListener(listener)
            }
        }
    }

    fun setNewPage(fragment: Fragment) {
        hideKeyBoard()
        try {
            if (supportFragmentManager.backStackEntryCount > 0) {
                for (i in 0 until supportFragmentManager.backStackEntryCount) {
                    supportFragmentManager.popBackStackImmediate()
                }
            }
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_main, fragment, "currentFragment")
            transaction.commitAllowingStateLoss()
            mFragment?.let{
                transaction.remove(it)
            }
            mFragment = fragment

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addFragment(fragment: Fragment) {
        hideKeyBoard()
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.trans_right_to_left_in, R.anim.trans_right_to_left_out,
                        R.anim.trans_left_to_right_in, R.anim.trans_left_to_right_out)
                .replace(R.id.frame_main, fragment)
                .addToBackStack(null)
                .commit()
    }

    fun addToStackFragment(fragment: Fragment, prevFragment: Fragment) {
        hideKeyBoard()
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.trans_right_to_left_in, R.anim.trans_right_to_left_out,
                        R.anim.trans_left_to_right_in, R.anim.trans_left_to_right_out)
                .hide(prevFragment)
                .add(R.id.frame_main, fragment)
                .addToBackStack(null)
                .commit()
    }

    fun replaceFragment(fragment: Fragment) {
        hideKeyBoard()
        supportFragmentManager.popBackStackImmediate(R.id.frame_main, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
                .replace(R.id.frame_main, fragment)
                .commit()
    }

    override fun finish() {
        hideKeyBoard()
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun startActivity(intent: Intent) {
        // TODO Auto-generated method stub
        super.startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        super.startActivityForResult(intent, requestCode)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    public fun hideKeyBoard() {
        try {
            runOnUiThread {
                try {
                    val inputManager = this@BaseActivity
                            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.hideSoftInputFromWindow(
                            this@BaseActivity.currentFocus!!.applicationWindowToken,
                            InputMethodManager.HIDE_NOT_ALWAYS)
                } catch (e: IllegalStateException) {
                } catch (e: Exception) {
                }
            }

        } catch (e: IllegalStateException) {
            // TODO: handle exception
        } catch (e: Exception) {
        }

    }

    fun showToast(title: String) {
        Toast.makeText(this, title, Toast.LENGTH_SHORT).show()
    }

    fun showToast(title: Int) {
        Toast.makeText(this, getString(title), Toast.LENGTH_SHORT).show()
    }

    fun showAlert(e: Exception) {
        if (e is ApiException)
            showAlert(R.string.err_unexpected_exception_api)
        else if (e is IOException)
            showAlert(R.string.err_network_available)
        else
            showAlert(R.string.err_unexpected_exception)
    }

    private fun showAlert(messageId: Int) {
        showAlert(getString(messageId))
    }

    fun showAlert(content: String) {
        AlertDialog.Builder(this)
                .setMessage(content)
                .setPositiveButton(R.string.txt_ok, null)
                .show()
    }

    private var mAlertDialog: AlertDialog? = null
    fun showAlertWithAction(message: String, titleButtonPo: Int, titleButtonNegative: Int, listener: DialogInterface.OnClickListener) {
        if (mAlertDialog?.isShowing == true) {
            mAlertDialog?.dismiss()
        }

        mAlertDialog = AlertDialog.Builder(this@BaseActivity).apply {
            setMessage(message)
            setCancelable(false)
            setPositiveButton(titleButtonPo, listener)
            setNegativeButton(titleButtonNegative, null)
        }.create()

        mAlertDialog?.show()
    }

    override fun receivedBroadcast() {
//        try {
//            val skuList = ArrayList<String>()
//            skuList.add(Constants.SKU_RIFE_FREE)
//            skuList.add(Constants.SKU_RIFE_FREE_FLASHSALE)
//
//            skuList.add(Constants.SKU_RIFE_MONTHLY)
//            skuList.add(Constants.SKU_RIFE_MONTHLY_FLASHSALE)
//            skuList.add(Constants.SKU_RIFE_YEARLY)
//            skuList.add(Constants.SKU_RIFE_YEARLY_FLASHSALE)
//            skuList.add(Constants.SKU_RIFE_LIFETIME)
//            skuList.add(Constants.SKU_RIFE_LIFETIME_FLASHSALE)
//
//            skuList.add(Constants.SKU_RIFE_ADVANCED_FREE_TRIAL)
//            skuList.add(Constants.SKU_RIFE_ADVANCED_FREE_TRIAL_FLASHSALE)
//            skuList.add(Constants.SKU_RIFE_ADVANCED_MONTHLY)
//            skuList.add(Constants.SKU_RIFE_ADVANCED_MONTHLY_FLASHSALE)
//            skuList.add(Constants.SKU_RIFE_ADVANCED_YEAR)
//            skuList.add(Constants.SKU_RIFE_ADVANCED_YEAR_FLASHSALE)
//            skuList.add(Constants.SKU_RIFE_ADVANCED_LIFETIME)
//            skuList.add(Constants.SKU_RIFE_ADVANCED_LIFETIME_FLASHSALE)
//
//            skuList.add(Constants.SKU_RIFE_HIGHER_MONTHLY)
//            skuList.add(Constants.SKU_RIFE_HIGHER_ANNUAL)
//            skuList.add(Constants.SKU_RIFE_HIGHER_ANNUAL_FLASH_SALE)
//            skuList.add(Constants.SKU_RIFE_HIGHER_LIFETIME)
//            skuList.add(Constants.SKU_RIFE_HIGHER_LIFETIME_FLASH_SALE)
////            mHelper!!.queryInventoryAsync(true, null, skuList, mGotInventoryListener)
//        } catch (e: IabHelper.IabAsyncInProgressException) {
//            complain("Error querying inventory. Another async operation in progress.")
//        }
    }

    fun onPurchaseProduct(skuRife: String, isLifeTime: Boolean) {
//        try {
//            if (!isLifeTime) {
//                mHelper?.launchSubscriptionPurchaseFlow(this, skuRife, RC_REQUEST, mPurchaseFinishedListener, "")
//            } else {
//                mHelper?.launchPurchaseFlow(this, skuRife, RC_REQUEST, mPurchaseFinishedListener, "")
//            }
//        } catch (e: IabHelper.IabAsyncInProgressException) {
//            complain("Error launching purchase flow. Another async operation in progress.")
//        }


//        FilesUtils.showComingSoon(this)

        // Reset the dialog options
    }

    internal fun complain(message: String) {
        //alert("Error: " + message);
    }

    /**
     * Verifies the developer payload of a purchase.
     */
    internal fun verifyDeveloperPayload(p: Purchase): Boolean {
        val payload = p.developerPayload
        return true
    }
}
