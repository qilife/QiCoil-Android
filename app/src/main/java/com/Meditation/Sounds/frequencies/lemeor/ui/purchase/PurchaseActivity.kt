package com.Meditation.Sounds.frequencies.lemeor.ui.purchase

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.Constants.Companion.SKU_RIFE_ADVANCED_MONTHLY
import com.Meditation.Sounds.frequencies.utils.Constants.Companion.SKU_RIFE_HIGHER_MONTHLY
import com.Meditation.Sounds.frequencies.utils.Constants.Companion.SKU_RIFE_MONTHLY
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_purchase.subs_abundanceI
import kotlinx.android.synthetic.main.activity_purchase.subs_abundanceII
import kotlinx.android.synthetic.main.activity_purchase.subs_abundanceII_buy_now
import kotlinx.android.synthetic.main.activity_purchase.subs_abundanceII_price
import kotlinx.android.synthetic.main.activity_purchase.subs_abundanceI_buy_now
import kotlinx.android.synthetic.main.activity_purchase.subs_abundanceI_price
import kotlinx.android.synthetic.main.activity_purchase.subs_back
import kotlinx.android.synthetic.main.activity_purchase.subs_bottom_layout
import kotlinx.android.synthetic.main.activity_purchase.subs_continue
import kotlinx.android.synthetic.main.activity_purchase.subs_free
import kotlinx.android.synthetic.main.activity_purchase.subs_info
import kotlinx.android.synthetic.main.activity_purchase.subs_master
import kotlinx.android.synthetic.main.activity_purchase.subs_master_buy_now
import kotlinx.android.synthetic.main.activity_purchase.subs_master_price
import kotlinx.android.synthetic.main.activity_purchase.subs_starter_buy_now
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Random

class PurchaseActivity : AppCompatActivity() {

    private var billingClient: BillingClient? = null
    private var mSkuDetails: SkuDetails? = null
    private var mSkuDetailsList: List<SkuDetails>? = null

    private var selectedList = ArrayList<TextView>()
    private var pricesList = ArrayList<TextView>()
    private var fragmentList =
        arrayListOf(StarterFragment(), MasterFragment(), Abundance1Fragment(), Abundance2Fragment())
    private var index = Random().nextInt(fragmentList.size - 1) + 1
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase)
        firebaseAnalytics = Firebase.analytics
        initUI()

        setUpBillingClient()

        setFragment(fragmentList[index], index)
    }

    private fun initUI() {
        subs_back.setOnClickListener { onBackPressed() }

        initSelectedList()

        initPricesList()

        setTermsSpan()

        subs_continue.setOnClickListener { pay() }

        //a little bit of a hardcode - todo fix it
        subs_free.setOnClickListener { setFragment(StarterFragment(), 0) }

        subs_master.setOnClickListener { setFragment(MasterFragment(), 1) }

        subs_abundanceI.setOnClickListener { setFragment(Abundance1Fragment(), 2) }

        subs_abundanceII.setOnClickListener { setFragment(Abundance2Fragment(), 3) }
    }

    private fun initPricesList() {
        pricesList.add(subs_master_price)
        pricesList.add(subs_abundanceI_price)
        pricesList.add(subs_abundanceII_price)
    }

    private fun initSelectedList() {
        selectedList.add(subs_starter_buy_now)
        selectedList.add(subs_master_buy_now)
        selectedList.add(subs_abundanceI_buy_now)
        selectedList.add(subs_abundanceII_buy_now)
    }

    private fun setTermsSpan() {
        try {
            val text = getString(R.string.tv_term_and_privacy)
            val list = text.split("|")
            if (list.size == 3) {
                val listLength = list.map { it.length }
                val mSpannableText = SpannableString(text.replace("|", ""))
                val clickPrivacy = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        val url = "http://www.tattoobookapp.com/quantumwavebiotechnology/privacy"
                        val i = Intent(Intent.ACTION_VIEW)
                        i.data = Uri.parse(url)
                        startActivity(i)
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.color = Color.WHITE
                        ds.typeface = Typeface.DEFAULT_BOLD
                    }
                }
                val clickTerms = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        val url = "http://www.tattoobookapp.com/quantumwavebiotechnology/terms"
                        val i = Intent(Intent.ACTION_VIEW)
                        i.data = Uri.parse(url)
                        startActivity(i)
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.color = Color.WHITE
                        ds.typeface = Typeface.DEFAULT_BOLD
                    }
                }
                mSpannableText.setSpan(
                    clickTerms,
                    0,
                    listLength[0],
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                mSpannableText.setSpan(
                    clickPrivacy,
                    listLength[0] + listLength[1],
                    mSpannableText.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                subs_info.text =
                    TextUtils.concat(
                        getString(R.string.tv_title_subscription_new),
                        " ",
                        mSpannableText
                    )
                subs_info.movementMethod = LinkMovementMethod.getInstance()
            }
        } catch (_: Exception) {
        }
    }

    private fun setFragment(fragment: Fragment, id: Int) {
        index = id

        setSelected(id)

        supportFragmentManager.beginTransaction().replace(
            R.id.subs_container,
            fragment,
            fragment.javaClass.simpleName
        )
            .commit()

        if (id == 0) {
            subs_bottom_layout.visibility = View.GONE
        } else {
            subs_bottom_layout.visibility = View.VISIBLE
            mSkuDetails = mSkuDetailsList?.get(id - 1)
        }
    }

    private fun setSelected(id: Int) {
        selectedList.forEachIndexed { index, view ->
            if (view.text != "ACTIVATED") {
                if (index == id) {
                    view.visibility = View.VISIBLE
                } else {
                    view.visibility = View.INVISIBLE
                }
            }
        }
    }

    //region Billing System
    private fun setUpBillingClient() {
        billingClient = BillingClient.newBuilder(applicationContext)
            .setListener(purchaseUpdateListener)
            .enablePendingPurchases()
            .build()
        startConnection()
    }

    private fun startConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    Log.d("TAG_INAPP", "Setup Billing Done")
                    // The BillingClient is ready. You can query purchases here.
                    queryAvailableProducts()

                    //get purchases
                    val list: List<Purchase> = getPurchases()
                    if (list.isEmpty()) {
                        Log.d("LOG", "purchases is empty")
                    } else {
                        list.forEach {
                            var categoryId: Int? = null

                            when (it.skus.get(0)) {
                                SKU_RIFE_MONTHLY,
                                Constants.SKU_RIFE_YEARLY_FLASHSALE -> {
                                    categoryId = 1
                                }

                                SKU_RIFE_ADVANCED_MONTHLY,
                                Constants.SKU_RIFE_ADVANCED_YEAR_FLASHSALE -> {
                                    categoryId = 2
                                }

                                SKU_RIFE_HIGHER_MONTHLY,
                                Constants.SKU_RIFE_HIGHER_ANNUAL_FLASH_SALE -> {
                                    categoryId = 3
                                }
                            }

                            if (categoryId == null) {
                                Toast.makeText(
                                    applicationContext,
                                    "Critical error!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@forEach
                            }

                            selectedList[categoryId].text = "ACTIVATED"
                            selectedList[categoryId].visibility = View.VISIBLE
                        }
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.e("TAG_INAPP", "Billing client Disconnected")
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    private fun getPurchases(): List<Purchase> {
        //return billingClient?.queryPurchases(BillingClient.SkuType.SUBS)?.purchasesList!!
        val subsList: List<Purchase> = ArrayList()
        billingClient?.queryPurchasesAsync(
            BillingClient.SkuType.SUBS,
            PurchasesResponseListener { billingResult, mutableList -> subsList })

        return subsList
    }

    private fun queryAvailableProducts() {
        val skuList = ArrayList<String>()
        skuList.add(SKU_RIFE_MONTHLY)
        skuList.add(SKU_RIFE_ADVANCED_MONTHLY)
        skuList.add(SKU_RIFE_HIGHER_MONTHLY)

        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)

        billingClient?.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
            // Process the result.
            if (billingResult.responseCode == BillingResponseCode.OK && !skuDetailsList.isNullOrEmpty()) {
                mSkuDetailsList = skuDetailsList
                mSkuDetails = skuDetailsList[index - 1]

                skuDetailsList.forEachIndexed { i, skuDetails ->
                    updatePrice(i, skuDetails)
                }
            }
        }
    }

    private fun updatePrice(index: Int, skuDetails: SkuDetails?) {
        pricesList.forEachIndexed { i, textView ->
            if (index == i) {
                textView.text = skuDetails?.price
            }
        }
    }

    private val purchaseUpdateListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            Log.e("TAG_INAPP", "billingResult responseCode : ${billingResult.responseCode}")

            if (billingResult.responseCode == BillingResponseCode.OK && purchases != null) {
                firebaseAnalytics.logEvent("In_App_Purchase") {
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "Purchase Complete")
                }
                for (purchase in purchases) {
                    handleConsumedPurchases(purchase)
                }
            } else if (billingResult.responseCode == BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
                firebaseAnalytics.logEvent("In_App_Purchase") {
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "Purchase CANCELED")
                }
            } else {
                // Handle any other error codes.
            }
        }

    private fun handleConsumedPurchases(purchase: Purchase) {
        Log.e("TAG_INAPP", "handleConsumablePurchasesAsync foreach it is $purchase")
        val consumeParams =
            ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        billingClient?.consumeAsync(consumeParams) { billingResult, purchaseToken ->
            when (billingResult.responseCode) {
                BillingResponseCode.OK -> {
                    // Handle the success of the consume operation.
                    Log.e(
                        "TAG_INAPP",
                        "Update the appropriate tables/databases to grant user the items"
                    )
                    val db = DataBase.getInstance(applicationContext)
                    val categoryDao = db.categoryDao()
                    val albumDao = db.albumDao()
                    val trackDao = db.trackDao()

                    var categoryId: Int? = null

                    when (mSkuDetails?.sku) {
                        SKU_RIFE_MONTHLY -> {
                            categoryId = 1
                        }

                        SKU_RIFE_ADVANCED_MONTHLY -> {
                            categoryId = 2
                        }

                        SKU_RIFE_HIGHER_MONTHLY -> {
                            categoryId = 3
                        }
                    }

                    if (categoryId == null) {
                        Toast.makeText(applicationContext, "Critical error!", Toast.LENGTH_SHORT)
                            .show()
                        return@consumeAsync
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        categoryDao.updatePurchaseStatus(true, categoryId)
                        albumDao.setUnlockedStatusByCategoryId(true, categoryId, false)

                        val albums = albumDao.getUnlockedAlbums(true)
                        albums.forEach { a ->
                            a.tracks.forEach { t ->
                                trackDao.setTrackUnlocked(true, t.id)
                            }
                        }
                    }
                }

                else -> {
                    Log.e("TAG_INAPP", billingResult.debugMessage)
                }
            }
        }
    }

    private fun handleNonConsumablePurchase(purchase: Purchase) {
        Log.v("TAG_INAPP", "handlePurchase : $purchase")
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken).build()
                billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    val billingResponseCode = billingResult.responseCode
                    val billingDebugMessage = billingResult.debugMessage

                    Log.v("TAG_INAPP", "response code: $billingResponseCode")
                    Log.v("TAG_INAPP", "debugMessage : $billingDebugMessage")

                }
            }
        }
    }

    private fun pay() {
        mSkuDetails?.let {
            val flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(it)
                .build()
            billingClient?.launchBillingFlow(this, flowParams)?.responseCode
        } ?: noSKUMessage()
    }

    private fun noSKUMessage() {} //???
    //endregion
}