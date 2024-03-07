package com.Meditation.Sounds.frequencies.lemeor.ui.purchase.new_flow

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.Meditation.Sounds.frequencies.BuildConfig
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.InappPurchase
import com.Meditation.Sounds.frequencies.lemeor.InappPurchase.HIGHER_QUANTUM_TIER_INAPP_ABUNDANCE_HAPPINESS
import com.Meditation.Sounds.frequencies.lemeor.InappPurchase.HIGHER_QUANTUM_TIER_INAPP_ABUNDANCE_LOVE
import com.Meditation.Sounds.frequencies.lemeor.InappPurchase.HIGHER_QUANTUM_TIER_INAPP_ABUNDANCE_LUCK
import com.Meditation.Sounds.frequencies.lemeor.InappPurchase.HIGHER_QUANTUM_TIER_INAPP_ABUNDANCE_SUCCESS
import com.Meditation.Sounds.frequencies.lemeor.InappPurchase.HIGHER_QUANTUM_TIER_INAPP_AYAHUASCA
import com.Meditation.Sounds.frequencies.lemeor.InappPurchase.HIGHER_QUANTUM_TIER_INAPP_BEAUTY_I
import com.Meditation.Sounds.frequencies.lemeor.InappPurchase.HIGHER_QUANTUM_TIER_INAPP_BEAUTY_II
import com.Meditation.Sounds.frequencies.lemeor.InappPurchase.HIGHER_QUANTUM_TIER_INAPP_BRAIN
import com.Meditation.Sounds.frequencies.lemeor.InappPurchase.HIGHER_QUANTUM_TIER_INAPP_DIGITAL_IVM
import com.Meditation.Sounds.frequencies.lemeor.InappPurchase.HIGHER_QUANTUM_TIER_INAPP_DMT
import com.Meditation.Sounds.frequencies.lemeor.InappPurchase.HIGHER_QUANTUM_TIER_INAPP_FITNESS
import com.Meditation.Sounds.frequencies.lemeor.InappPurchase.HIGHER_QUANTUM_TIER_INAPP_LIFE_FORCE
import com.Meditation.Sounds.frequencies.lemeor.InappPurchase.HIGHER_QUANTUM_TIER_INAPP_MANIFESTING
import com.Meditation.Sounds.frequencies.lemeor.InappPurchase.HIGHER_QUANTUM_TIER_INAPP_NAD
import com.Meditation.Sounds.frequencies.lemeor.InappPurchase.HIGHER_QUANTUM_TIER_INAPP_NMN
import com.Meditation.Sounds.frequencies.lemeor.InappPurchase.HIGHER_QUANTUM_TIER_INAPP_PROTECTION
import com.Meditation.Sounds.frequencies.lemeor.InappPurchase.HIGHER_QUANTUM_TIER_INAPP_SKIN_CARE
import com.Meditation.Sounds.frequencies.lemeor.InappPurchase.HIGHER_QUANTUM_TIER_INAPP_TRANSFORMATION_MEDITATION
import com.Meditation.Sounds.frequencies.lemeor.InappPurchase.HIGHER_QUANTUM_TIER_INAPP_WELLNESS_I
import com.Meditation.Sounds.frequencies.lemeor.InappPurchase.HIGHER_QUANTUM_TIER_INAPP_WELLNESS_II
import com.Meditation.Sounds.frequencies.lemeor.InappPurchase.HIGHER_QUANTUM_TIER_INAPP_WELLNESS_III
import com.Meditation.Sounds.frequencies.lemeor.InappPurchase.HIGHER_QUANTUM_TIER_INAPP_WISDOM
import com.Meditation.Sounds.frequencies.lemeor.QUANTUM_TIER_SUBS_ANNUAL
import com.Meditation.Sounds.frequencies.lemeor.QUANTUM_TIER_SUBS_ANNUAL_7_DAY_TRIAL
import com.Meditation.Sounds.frequencies.lemeor.QUANTUM_TIER_SUBS_MONTH
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.views.BottomSheetWebView
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AppsFlyerLib
import kotlinx.android.synthetic.main.activity_new_purchase.purchase_back
import kotlinx.android.synthetic.main.activity_new_purchase.purchase_container
import kotlinx.android.synthetic.main.activity_new_purchase.purchase_continue
import kotlinx.android.synthetic.main.activity_new_purchase.purchase_info
import kotlinx.android.synthetic.main.activity_new_purchase.purchase_price
import kotlinx.android.synthetic.main.activity_new_purchase.purchase_screen_name
import kotlinx.android.synthetic.main.activity_new_purchase.purchase_terms
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


class NewPurchaseActivity : AppCompatActivity() {

    private var categoryId = EXTRA_DEFAULT_INT
    private var tierId = QUANTUM_TIER_ID
    private var Id = QUANTUM_TIER_ID

    private val mSubsList = ArrayList<SkuDetails>()
    private var billingClient: BillingClient? = null
    private var mInapp: SkuDetails? = null
    private var mSubsAnnual: SkuDetails? = null
    private var mSubsMonth: SkuDetails? = null
    private var mSkuDetails: SkuDetails? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_purchase)

        initUI()

        setTermsSpan()

        setUpBillingClient()
    }

    private fun initUI() {
        if (intent != null) {
            categoryId = intent.getIntExtra(EXTRA_CATEGORY_ID, EXTRA_DEFAULT_INT)
            tierId = intent.getIntExtra(EXTRA_TIER_ID, EXTRA_DEFAULT_INT)
            Id = intent.getIntExtra(EXTRA_ALBUM_ID, EXTRA_DEFAULT_INT)
        }

        purchase_back.setOnClickListener { onBackPressed() }

        val albumDao = DataBase.getInstance(applicationContext).albumDao()
        val tierDao = DataBase.getInstance(applicationContext).tierDao()
        val categoryDao = DataBase.getInstance(applicationContext).categoryDao()

        CoroutineScope(Dispatchers.IO).launch {

            var screenName = ""
            val albumList = ArrayList<Album>()
            // 1 quantum, 2 rife, higher 3, inner 4
            if (BuildConfig.IS_FREE) {
                when (tierId) {
                    4 -> {
                        purchase_continue.text = getText(R.string.tv_apply_now)
                    }

                    else -> {
                        purchase_continue.text = getText(R.string.tv_unlock_now)
                    }
                }
                albumDao.getAlbumsByTierId(tierId).let {
                    if (tierId == 8) {
                        albumList.addAll(it.filter { it.category_id == categoryId })
                    } else {
                        albumList.addAll(it)
                    }

                }
                tierDao.getTierNameById(tierId)?.let { screenName = it }
            } else {
                when (tierId) {
                    QUANTUM_TIER_ID -> {
                        albumDao.getAlbumsByTierId(tierId).let { albumList.addAll(it) }
                        tierDao.getTierNameById(tierId)?.let { screenName = it }
                    }

                    INNER_CIRCLE_TIER_ID -> {
                        purchase_continue.text = "APPLY NOW"
                        albumDao.getAlbumsByTierId(tierId).let { albumList.addAll(it) }
                        tierDao.getTierNameById(tierId)?.let { screenName = it }
                    }

                    else -> {
                        albumDao.getAlbumsByCategory(categoryId).let { albumList.addAll(it) }
                        /* if (Id == 219 || Id == 220) {
                                     albumDao.getAlbumById(Id)?.let { albumList.add(it) }
                                 } else {
                                     albumDao.getAlbumsByCategory(categoryId)?.let { albumList.addAll(it) }
                                 }*/

                        categoryDao.getCategoryNameById(categoryId)?.let { screenName = it }
                    }
                }
            }

            CoroutineScope(Dispatchers.Main).launch {
                purchase_screen_name.text = screenName

                val albumsPagerAdapter = AlbumsPagerAdapter(supportFragmentManager, albumList)
                purchase_container.adapter = albumsPagerAdapter
                val index = albumList.indexOfFirst {
                    it.id == Id
                }
                purchase_container.currentItem = index


            }
        }
        purchase_container.clipToPadding = false

        orientationChangesUI(resources.configuration.orientation)

        purchase_continue.setOnClickListener {
            if (BuildConfig.IS_FREE) {
                val bottomSheetWebView = BottomSheetWebView(this@NewPurchaseActivity)
                // 1 quantum, 2 rife, higher 3, inner 4, special 8
                when (tierId) {
                    4 -> {
                        bottomSheetWebView.showWithUrl("https://qilifestore.com/collections/inner-circle-members-area")
                    }

                    1 -> {
                        bottomSheetWebView.showWithUrl("https://qilifestore.com/products/ultimate-quantum-frequency-bundle")
                    }

                    3 -> {
                        bottomSheetWebView.showWithUrl("https://qilifestore.com/products/ultimate-higher-quantum-frequencies-collection")
                    }

                    8 -> {
                        when (categoryId) {
                            //Genesis
                            48 -> {
                                bottomSheetWebView.showWithUrl("https://qilifestore.com/products/genesis-frequency-pack")
                            }
                            //Parasite
                            53 -> {
                                bottomSheetWebView.showWithUrl("https://qilifestore.com/products/parasite-detox-frequency-pack")
                            }
                            //Brain Biohacking
                            54 -> {
                                bottomSheetWebView.showWithUrl("https://qilifestore.com/collections/braintap-brain-training-for-sleep-focus-peak-performance/products/braintap-frequency-collection")
                            }
                            //Lyme Remission
                            56 -> {
                                bottomSheetWebView.showWithUrl("https://qilifestore.com/collections/lyme-remission-with-qi-coil-rife-machine/products/lyme-remission-frequency-pack")
                            }
                        }
                    }

                    else -> {
                        bottomSheetWebView.showWithUrl("https://qilifestore.com/products/professional-rife-frequency-collection-mp3-466-audio-files")
                    }
                }
            } else {
                pay()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        orientationChangesUI(newConfig.orientation)
    }

    private fun orientationChangesUI(orientation: Int) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            purchase_container.setPadding(275, 0, 275, 0)
            purchase_container.pageMargin = 50
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val width = displayMetrics.widthPixels
            val result = (width / 4.0).roundToInt()
            purchase_container.setPadding(result, 0, result, 0)
            purchase_container.pageMargin = 16
        }
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

                if (tierId == QUANTUM_TIER_ID || tierId == HIGHER_QUANTUM_TIER_ID) {
                    purchase_info.text = getString(R.string.tv_title_subscription_new)
                } else {
                    purchase_info.visibility = View.GONE
                }
                purchase_terms.text = mSpannableText
                purchase_terms.movementMethod = LinkMovementMethod.getInstance()
            }

        } catch (_: Exception) {
        }
    }

    private fun setUpBillingClient() {
        billingClient = BillingClient.newBuilder(applicationContext)
            .setListener(purchaseUpdateListener)
            .enablePendingPurchases()
            .build()
        startConnection()
    }

    private val purchaseUpdateListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            Log.d("TAG_INAPP", "billingResult responseCode : ${billingResult.responseCode}")

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {

                val eventValues = HashMap<String, Any>()
                eventValues.put(AFInAppEventParameterName.REVENUE, 0)
                AppsFlyerLib.getInstance().logEvent(
                    applicationContext,
                    "purchase",
                    eventValues
                )

                for (purchase in purchases) {
                    handleConsumedPurchases(purchase)
                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                val eventValues = HashMap<String, Any>()
                eventValues.put(AFInAppEventParameterName.REVENUE, 0)
                AppsFlyerLib.getInstance().logEvent(
                    applicationContext,
                    "cancel_purchase",
                    eventValues
                )
                // Handle an error caused by a user cancelling the purchase flow.
            }
        }

    private fun startConnection() {
        try {
            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d("TAG_INAPP", "Setup Billing Done")
                        // The BillingClient is ready. You can query purchases here.
                        queryAvailableProducts()
                    }
                }

                override fun onBillingServiceDisconnected() {
                    Log.e("TAG_INAPP", "Billing client Disconnected")
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                }
            })
        } catch (_: Exception) {
        }
    }

    private fun queryAvailableProducts() {
        val subsList = ArrayList<String>()
        subsList.add(QUANTUM_TIER_SUBS_MONTH)
        subsList.add(QUANTUM_TIER_SUBS_ANNUAL)
        subsList.add(QUANTUM_TIER_SUBS_ANNUAL_7_DAY_TRIAL)

        val subsParams = SkuDetailsParams.newBuilder()
        subsParams.setSkusList(subsList).setType(BillingClient.SkuType.SUBS)

        billingClient?.querySkuDetailsAsync(subsParams.build()) { billingResult, skuDetailsList ->
            // Process the result.
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !skuDetailsList.isNullOrEmpty()) {
                mSubsList.addAll(skuDetailsList)
                mSubsList.forEach {
                    if (it.subscriptionPeriod == QUANTUM_SUBS_MONTH) {
                        mSubsMonth = it
                    } else if (it.subscriptionPeriod == QUANTUM_SUBS_YEAR) {
                        mSubsAnnual = it
                    }
                }

                if (tierId == QUANTUM_TIER_ID) {
                    purchase_price.text = getString(
                        R.string.subs_purchase_info,
                        mSubsMonth?.price,
                        mSubsAnnual?.price
                    )
                }
            }
        }

        val inappList = ArrayList<String>()
        inappList.add(HIGHER_QUANTUM_TIER_INAPP_WELLNESS_I.sku)
        inappList.add(HIGHER_QUANTUM_TIER_INAPP_WELLNESS_II.sku)
        inappList.add(HIGHER_QUANTUM_TIER_INAPP_WELLNESS_III.sku)
        inappList.add(HIGHER_QUANTUM_TIER_INAPP_LIFE_FORCE.sku)
        inappList.add(HIGHER_QUANTUM_TIER_INAPP_ABUNDANCE_LUCK.sku)
        inappList.add(HIGHER_QUANTUM_TIER_INAPP_ABUNDANCE_SUCCESS.sku)
        inappList.add(HIGHER_QUANTUM_TIER_INAPP_ABUNDANCE_HAPPINESS.sku)
        inappList.add(HIGHER_QUANTUM_TIER_INAPP_ABUNDANCE_LOVE.sku)
        inappList.add(HIGHER_QUANTUM_TIER_INAPP_BRAIN.sku)
        inappList.add(HIGHER_QUANTUM_TIER_INAPP_WISDOM.sku)
        inappList.add(HIGHER_QUANTUM_TIER_INAPP_MANIFESTING.sku)
        inappList.add(HIGHER_QUANTUM_TIER_INAPP_TRANSFORMATION_MEDITATION.sku)
        inappList.add(HIGHER_QUANTUM_TIER_INAPP_PROTECTION.sku)
        inappList.add(HIGHER_QUANTUM_TIER_INAPP_BEAUTY_I.sku)
        inappList.add(HIGHER_QUANTUM_TIER_INAPP_BEAUTY_II.sku)
        inappList.add(HIGHER_QUANTUM_TIER_INAPP_SKIN_CARE.sku)
        inappList.add(HIGHER_QUANTUM_TIER_INAPP_FITNESS.sku)
        inappList.add(HIGHER_QUANTUM_TIER_INAPP_DMT.sku)
        inappList.add(HIGHER_QUANTUM_TIER_INAPP_AYAHUASCA.sku)
        inappList.add(HIGHER_QUANTUM_TIER_INAPP_NAD.sku)
        inappList.add(HIGHER_QUANTUM_TIER_INAPP_NMN.sku)
        inappList.add(HIGHER_QUANTUM_TIER_INAPP_DIGITAL_IVM.sku)

        val inappParams = SkuDetailsParams.newBuilder()
        inappParams.setSkusList(inappList).setType(BillingClient.SkuType.INAPP)

        billingClient?.querySkuDetailsAsync(inappParams.build()) { billingResult, skuDetailsList ->
            // Process the result.
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !skuDetailsList.isNullOrEmpty()) {
                val enum = InappPurchase.getCategoryId(categoryId)
                skuDetailsList.forEach {
                    if (it.sku == enum.sku) {
                        mInapp = it
                    }
                }

                if (tierId != QUANTUM_TIER_ID && tierId != INNER_CIRCLE_TIER_ID) {
                    purchase_price.text = getString(R.string.inapp_purchase_info, mInapp?.price)
                } else
                    purchase_price.text = ""
            }
        }
    }

    private fun handleConsumedPurchases(purchase: Purchase) {
        Log.d("TAG_INAPP", "handleConsumablePurchasesAsync foreach it is $purchase")
        val consumeParams =
            ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        billingClient?.consumeAsync(consumeParams) { billingResult, _ ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    // Handle the success of the consume operation.
                    Log.d(
                        "TAG_INAPP",
                        "Update the appropriate tables/databases to grant user the items"
                    )

                    val albumDao = DataBase.getInstance(applicationContext).albumDao()


                    CoroutineScope(Dispatchers.IO).launch {
                        when (mSkuDetails?.sku) {

                            QUANTUM_TIER_SUBS_MONTH,
                            QUANTUM_TIER_SUBS_ANNUAL -> {
                                albumDao.setNewUnlockedByTierId(true, QUANTUM_TIER_ID)
                            }

                            HIGHER_QUANTUM_TIER_INAPP_WELLNESS_I.sku -> {
                                albumDao.setNewUnlockedByCategoryId(
                                    true,
                                    HIGHER_QUANTUM_TIER_INAPP_WELLNESS_I.categoryId
                                )
                            }

                            HIGHER_QUANTUM_TIER_INAPP_WELLNESS_II.sku -> {
                                albumDao.setNewUnlockedByCategoryId(
                                    true,
                                    HIGHER_QUANTUM_TIER_INAPP_WELLNESS_II.categoryId
                                )
                            }

                            HIGHER_QUANTUM_TIER_INAPP_WELLNESS_III.sku -> {
                                albumDao.setNewUnlockedByCategoryId(
                                    true,
                                    HIGHER_QUANTUM_TIER_INAPP_WELLNESS_III.categoryId
                                )
                            }

                            HIGHER_QUANTUM_TIER_INAPP_LIFE_FORCE.sku -> {
                                albumDao.setNewUnlockedByCategoryId(
                                    true,
                                    HIGHER_QUANTUM_TIER_INAPP_LIFE_FORCE.categoryId
                                )
                            }

                            HIGHER_QUANTUM_TIER_INAPP_ABUNDANCE_LUCK.sku -> {
                                albumDao.setNewUnlockedByCategoryId(
                                    true,
                                    HIGHER_QUANTUM_TIER_INAPP_ABUNDANCE_LUCK.categoryId
                                )
                            }

                            HIGHER_QUANTUM_TIER_INAPP_ABUNDANCE_SUCCESS.sku -> {
                                albumDao.setNewUnlockedByCategoryId(
                                    true,
                                    HIGHER_QUANTUM_TIER_INAPP_ABUNDANCE_SUCCESS.categoryId
                                )
                            }

                            HIGHER_QUANTUM_TIER_INAPP_ABUNDANCE_HAPPINESS.sku -> {
                                albumDao.setNewUnlockedByCategoryId(
                                    true,
                                    HIGHER_QUANTUM_TIER_INAPP_ABUNDANCE_HAPPINESS.categoryId
                                )
                            }

                            HIGHER_QUANTUM_TIER_INAPP_ABUNDANCE_LOVE.sku -> {
                                albumDao.setNewUnlockedByCategoryId(
                                    true,
                                    HIGHER_QUANTUM_TIER_INAPP_ABUNDANCE_LOVE.categoryId
                                )
                            }

                            HIGHER_QUANTUM_TIER_INAPP_BRAIN.sku -> {
                                albumDao.setNewUnlockedByCategoryId(
                                    true,
                                    HIGHER_QUANTUM_TIER_INAPP_BRAIN.categoryId
                                )
                            }

                            HIGHER_QUANTUM_TIER_INAPP_WISDOM.sku -> {
                                albumDao.setNewUnlockedByCategoryId(
                                    true,
                                    HIGHER_QUANTUM_TIER_INAPP_WISDOM.categoryId
                                )
                            }

                            HIGHER_QUANTUM_TIER_INAPP_MANIFESTING.sku -> {
                                albumDao.setNewUnlockedByCategoryId(
                                    true,
                                    HIGHER_QUANTUM_TIER_INAPP_MANIFESTING.categoryId
                                )
                            }

                            HIGHER_QUANTUM_TIER_INAPP_TRANSFORMATION_MEDITATION.sku -> {
                                albumDao.setNewUnlockedByCategoryId(
                                    true,
                                    HIGHER_QUANTUM_TIER_INAPP_TRANSFORMATION_MEDITATION.categoryId
                                )
                            }

                            HIGHER_QUANTUM_TIER_INAPP_PROTECTION.sku -> {
                                albumDao.setNewUnlockedByCategoryId(
                                    true,
                                    HIGHER_QUANTUM_TIER_INAPP_PROTECTION.categoryId
                                )
                            }

                            HIGHER_QUANTUM_TIER_INAPP_BEAUTY_I.sku -> {
                                albumDao.setNewUnlockedByCategoryId(
                                    true,
                                    HIGHER_QUANTUM_TIER_INAPP_BEAUTY_I.categoryId
                                )
                            }

                            HIGHER_QUANTUM_TIER_INAPP_BEAUTY_II.sku -> {
                                albumDao.setNewUnlockedByCategoryId(
                                    true,
                                    HIGHER_QUANTUM_TIER_INAPP_BEAUTY_II.categoryId
                                )
                            }

                            HIGHER_QUANTUM_TIER_INAPP_SKIN_CARE.sku -> {
                                albumDao.setNewUnlockedByCategoryId(
                                    true,
                                    HIGHER_QUANTUM_TIER_INAPP_SKIN_CARE.categoryId
                                )
                            }

                            HIGHER_QUANTUM_TIER_INAPP_FITNESS.sku -> {
                                albumDao.setNewUnlockedByCategoryId(
                                    true,
                                    HIGHER_QUANTUM_TIER_INAPP_FITNESS.categoryId
                                )
                            }

                            HIGHER_QUANTUM_TIER_INAPP_DMT.sku -> {
                                //albumDao.setNewUnlockedById(true, Id)
                                albumDao.setNewUnlockedByCategoryId(
                                    true,
                                    HIGHER_QUANTUM_TIER_INAPP_DMT.categoryId
                                )
                            }

                            HIGHER_QUANTUM_TIER_INAPP_AYAHUASCA.sku -> {
                                albumDao.setNewUnlockedByCategoryId(
                                    true,
                                    HIGHER_QUANTUM_TIER_INAPP_AYAHUASCA.categoryId
                                )
                            }

                            HIGHER_QUANTUM_TIER_INAPP_NAD.sku -> {
                                albumDao.setNewUnlockedByCategoryId(
                                    true,
                                    HIGHER_QUANTUM_TIER_INAPP_NAD.categoryId
                                )
                            }

                            HIGHER_QUANTUM_TIER_INAPP_NMN.sku -> {
                                albumDao.setNewUnlockedByCategoryId(
                                    true,
                                    HIGHER_QUANTUM_TIER_INAPP_NMN.categoryId
                                )
                            }

                            HIGHER_QUANTUM_TIER_INAPP_DIGITAL_IVM.sku -> {
                                albumDao.setNewUnlockedByCategoryId(
                                    true,
                                    HIGHER_QUANTUM_TIER_INAPP_DIGITAL_IVM.categoryId
                                )
                            }
                        }
                    }

                    finish()
                }

                else -> {
                    Log.e("TAG_INAPP", billingResult.debugMessage)
                }
            }
        }
    }

    private fun pay() {
        if (tierId == QUANTUM_TIER_ID) {
            AlertDialog.Builder(this).setTitle("Subscribe for")
                .setCancelable(true)
                .setNegativeButton(getString(R.string.purchase_dialog_btn_month)) { dialog, _ ->
                    mSubsMonth?.let { openPurchaseDialog(it) }
                    dialog.dismiss()
                }
                .setPositiveButton(getString(R.string.purchase_dialog_btn_annual)) { dialog, _ ->
                    mSubsAnnual?.let { openPurchaseDialog(it) }
                    dialog.dismiss()
                }.show()
        } else {
            mInapp?.let { openPurchaseDialog(it) }
        }
    }

    private fun openPurchaseDialog(skuDetails: SkuDetails) {
        mSkuDetails = skuDetails

        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()
        billingClient?.launchBillingFlow(this, flowParams)?.responseCode
    }

    companion object {
        private const val QUANTUM_SUBS_MONTH = "P1M"
        private const val QUANTUM_SUBS_YEAR = "P1Y"
        const val QUANTUM_TIER_ID = 1
        const val HIGHER_QUANTUM_TIER_ID = 2
        const val INNER_CIRCLE_TIER_ID = 3
        private const val EXTRA_DEFAULT_INT = -1
        private const val EXTRA_TIER_ID = "tier_id"
        private const val EXTRA_CATEGORY_ID = "category_id"
        private const val EXTRA_ALBUM_ID = "album_id"

        fun newIntent(context: Context, categoryId: Int, tierId: Int, Id: Int): Intent {
            val intent = Intent(context, NewPurchaseActivity::class.java)
            intent.putExtra(EXTRA_CATEGORY_ID, categoryId)
            intent.putExtra(EXTRA_TIER_ID, tierId)
            intent.putExtra(EXTRA_ALBUM_ID, Id)
            return intent
        }
    }
}