package com.Meditation.Sounds.frequencies.views

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.adapters.TitleAdapter
import com.Meditation.Sounds.frequencies.feature.album.AlbumsViewModel
import com.Meditation.Sounds.frequencies.feature.base.BaseActivity
import com.Meditation.Sounds.frequencies.models.Album
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import kotlinx.android.synthetic.main.dialog_subscription_no_flash_sale.mImvDismiss
import kotlinx.android.synthetic.main.dialog_subscription_no_flash_sale.mPriceAdvanced
import kotlinx.android.synthetic.main.dialog_subscription_no_flash_sale.mPriceBasic
import kotlinx.android.synthetic.main.dialog_subscription_no_flash_sale.mPriceHigher
import kotlinx.android.synthetic.main.dialog_subscription_no_flash_sale.mViewImageAdvanced
import kotlinx.android.synthetic.main.dialog_subscription_no_flash_sale.mViewImageBasic
import kotlinx.android.synthetic.main.dialog_subscription_no_flash_sale.mViewImageFree
import kotlinx.android.synthetic.main.dialog_subscription_no_flash_sale.mViewSubscriptionAdvanced
import kotlinx.android.synthetic.main.dialog_subscription_no_flash_sale.mViewSubscriptionBasic
import kotlinx.android.synthetic.main.dialog_subscription_no_flash_sale.mViewSubscriptionFree
import kotlinx.android.synthetic.main.dialog_subscription_no_flash_sale.mViewSubscriptionHigher
import kotlinx.android.synthetic.main.dialog_subscription_no_flash_sale.mViewTitleAdvanced
import kotlinx.android.synthetic.main.dialog_subscription_no_flash_sale.mViewTitleBasic
import kotlinx.android.synthetic.main.dialog_subscription_no_flash_sale.mViewTitleFree
import kotlinx.android.synthetic.main.dialog_subscription_no_flash_sale.rcImageHigher
import kotlinx.android.synthetic.main.dialog_subscription_no_flash_sale.starter_recycler_view
import kotlinx.android.synthetic.main.dialog_subscription_no_flash_sale.subs_continue
import kotlinx.android.synthetic.main.dialog_subscription_no_flash_sale.subs_info
import kotlinx.android.synthetic.main.dialog_subscription_no_flash_sale.subs_price
import kotlinx.android.synthetic.main.dialog_subscription_no_flash_sale.viewBuyNowAdvanced
import kotlinx.android.synthetic.main.dialog_subscription_no_flash_sale.viewBuyNowBasic
import kotlinx.android.synthetic.main.dialog_subscription_no_flash_sale.viewBuyNowHigher
import kotlinx.android.synthetic.main.dialog_subscription_no_flash_sale.viewPriceAbundance
import kotlinx.android.synthetic.main.dialog_subscription_no_flash_sale.viewPriceAdvanced
import kotlinx.android.synthetic.main.dialog_subscription_no_flash_sale.viewPriceMaster
import java.util.Random

class SubscriptionDialogNormal(private val mContext: Context?) :
    Dialog(mContext!!, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {

    var baseActivity: BaseActivity? = null
    private lateinit var mViewModel: AlbumsViewModel
    private var mSpannableText: SpannableString? = null
    private var mTypeAlbum = -1
    private var mTitleHigherAdapter: TitleAdapter? = null

    //    private var mAlbumHigherAdapter: Abundance2Adapter? = null
    private var mAlbumHighers = ArrayList<Album>()
    private var mTitleHighers = ArrayList<String>()

    private val broadcastReceiverSubscriptionController = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            if (SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED)
                && SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_ADVANCED)
                && SharedPreferenceHelper.getInstance()
                    .getBool(Constants.KEY_PURCHASED_HIGH_QUANTUM)
                && SharedPreferenceHelper.getInstance()
                    .getBool(Constants.KEY_PURCHASED_HIGH_ABUNDANCE)
            ) {
                dismiss()
            }
        }
    }

    fun setTypeAlbum(type: Int) {
        mTypeAlbum = type
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_subscription_no_flash_sale)
        baseActivity = mContext as BaseActivity
        val window = this.window
        val wlp = window?.attributes
        wlp?.let {
            it.gravity = Gravity.CENTER
            it.height = WindowManager.LayoutParams.MATCH_PARENT
            it.width = WindowManager.LayoutParams.MATCH_PARENT
            it.flags = it.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN
            it.windowAnimations = R.style.DialogAnimation
        }

        window?.attributes = wlp
        getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.window?.attributes = wlp
        setCancelable(false)
        val isMaster = SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED)
        val isAdvanced =
            SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_ADVANCED)
        var isHigher = false
        if (SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_HIGH_ABUNDANCE)
            || SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_HIGH_QUANTUM)
        ) {
            isHigher = true
        }
        if (isMaster) {
            if (random() == 1) {
                setTypeAlbum(1)
            } else {
                setTypeAlbum(2)
            }
        }

        if (isAdvanced) {
            if (random() == 1) {
                setTypeAlbum(0)
            } else {
                setTypeAlbum(2)
            }
        }

        if (isHigher) {
            if (random() == 1) {
                setTypeAlbum(1)
            } else {
                setTypeAlbum(0)
            }
        }
        addListTitleHigher()
        initComponents()
        addListener()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mContext.registerReceiver(
                broadcastReceiverSubscriptionController,
                IntentFilter(Constants.BROADCAST_ACTION_PURCHASED),
                Context.RECEIVER_EXPORTED
            )
        } else {
            mContext.registerReceiver(
                broadcastReceiverSubscriptionController,
                IntentFilter(Constants.BROADCAST_ACTION_PURCHASED)
            )
        }
    }

    private fun addListTitleHigher() {
        mTitleHighers.clear()

    }

    override fun dismiss() {
        try {
            mContext!!.unregisterReceiver(broadcastReceiverSubscriptionController)
        } catch (_: IllegalArgumentException) {
        }
        super.dismiss()
    }

    fun initComponents() {
        mViewModel =
            ViewModelProviders.of(mContext as BaseActivity).get(AlbumsViewModel::class.java)
        mViewModel.getAlbumsHigherAbundance().observe(mContext) {
            if (it != null) {
                mAlbumHighers = it as ArrayList<Album>
//                mAlbumHigherAdapter?.data = it
//                mAlbumHigherAdapter?.notifyDataSetChanged()
            }
        }

        setTermsSpan()

        mTitleHigherAdapter = TitleAdapter(mTitleHighers)
        starter_recycler_view.layoutManager = LinearLayoutManager(mContext)
        starter_recycler_view.adapter = mTitleHigherAdapter

//        mAlbumHigherAdapter = Abundance2Adapter(mContext, mAlbumHighers)
//        rcImageHigher.layoutManager = GridLayoutManager(mContext, 3)
//        rcImageHigher.adapter = mAlbumHigherAdapter

        mPriceBasic.text =
            SharedPreferenceHelper.getInstance().getPriceByCurrency(Constants.PRICE_1_MONTH)
        mPriceAdvanced.text = SharedPreferenceHelper.getInstance()
            .getPriceByCurrency(Constants.PRICE_ADVANCED_MONTHLY)
        mPriceHigher.text =
            SharedPreferenceHelper.getInstance().getPriceByCurrency(Constants.PRICE_HIGHER_MONTHLY)

        when (mTypeAlbum) {
            0 -> {
                mViewImageBasic.visibility = View.VISIBLE
                mViewTitleBasic.visibility = View.VISIBLE
                mViewImageAdvanced.visibility = View.GONE
                mViewImageFree.visibility = View.GONE
                mViewTitleAdvanced.visibility = View.GONE
                mViewTitleFree.visibility = View.GONE
                rcImageHigher.visibility = View.GONE
                starter_recycler_view.visibility = View.GONE

                subs_price.text = "Monthly Payment of " + SharedPreferenceHelper.getInstance()
                    .getPriceByCurrency(Constants.PRICE_1_MONTH)
                subs_info.text = TextUtils.concat(
                    mContext.getString(R.string.tv_title_subscription_new),
                    " ",
                    mSpannableText
                )
                subs_info.movementMethod = LinkMovementMethod.getInstance()

                if (SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED)) {
                    viewBuyNowBasic.visibility = View.VISIBLE
                    viewBuyNowBasic.text = mContext.getString(R.string.tv_current_plan)
                    viewPriceMaster.visibility = View.INVISIBLE
                    subs_continue.setBackgroundResource(R.drawable.bg_button_current_plan)
                    subs_continue.text = mContext.getString(R.string.tv_current_plan)
                    subs_continue.visibility = View.VISIBLE
                    subs_price.visibility = View.GONE
                    subs_info.visibility = View.GONE
                } else {
                    viewBuyNowBasic.visibility = View.VISIBLE
                    viewBuyNowBasic.text = mContext.getString(R.string.tv_subscription_now)
                    viewPriceMaster.visibility = View.VISIBLE
                    subs_price.visibility = View.VISIBLE
                    subs_info.visibility = View.VISIBLE
                    subs_continue.setBackgroundResource(R.drawable.bg_continue_subscription)
                    subs_continue.text = mContext.getString(R.string.tv_continue)
                    subs_continue.visibility = View.VISIBLE
                    subs_continue.setOnClickListener {
                        baseActivity!!.onPurchaseProduct(Constants.SKU_RIFE_MONTHLY, false)
                    }
                }

                if (SharedPreferenceHelper.getInstance()
                        .getBool(Constants.KEY_PURCHASED_ADVANCED)
                ) {
                    viewBuyNowAdvanced.visibility = View.VISIBLE
                    viewBuyNowAdvanced.text = mContext.getString(R.string.tv_current_plan)
                    viewPriceAdvanced.visibility = View.INVISIBLE
                } else {
                    viewBuyNowAdvanced.visibility = View.INVISIBLE
                    viewPriceAdvanced.visibility = View.VISIBLE
                }

                if (SharedPreferenceHelper.getInstance()
                        .getBool(Constants.KEY_PURCHASED_HIGH_ABUNDANCE)
                ) {
                    viewBuyNowHigher.visibility = View.VISIBLE
                    viewBuyNowHigher.text = mContext.getString(R.string.tv_current_plan)
                    viewPriceAbundance.visibility = View.INVISIBLE
                } else {
                    viewBuyNowHigher.visibility = View.INVISIBLE
                    viewPriceAbundance.visibility = View.VISIBLE
                }
            }

            1 -> {
                mViewImageBasic.visibility = View.GONE
                mViewImageFree.visibility = View.GONE
                mViewImageAdvanced.visibility = View.VISIBLE
                mViewTitleAdvanced.visibility = View.VISIBLE
                mViewTitleBasic.visibility = View.GONE
                mViewTitleFree.visibility = View.GONE
                rcImageHigher.visibility = View.GONE
                starter_recycler_view.visibility = View.GONE

                subs_price.text = "Monthly Payment of " + SharedPreferenceHelper.getInstance()
                    .getPriceByCurrency(Constants.PRICE_ADVANCED_MONTHLY)
                subs_info.text = TextUtils.concat(
                    mContext.getString(R.string.tv_title_subscription_new),
                    " ",
                    mSpannableText
                )
                subs_info.movementMethod = LinkMovementMethod.getInstance()

                if (SharedPreferenceHelper.getInstance()
                        .getBool(Constants.KEY_PURCHASED_ADVANCED)
                ) {
                    viewBuyNowAdvanced.visibility = View.VISIBLE
                    viewBuyNowAdvanced.text = mContext.getString(R.string.tv_current_plan)
                    viewPriceAdvanced.visibility = View.INVISIBLE
                    subs_price.visibility = View.GONE
                    subs_info.visibility = View.GONE
                    subs_continue.visibility = View.VISIBLE
                    subs_continue.setBackgroundResource(R.drawable.bg_button_current_plan)
                    subs_continue.text = mContext.getString(R.string.tv_current_plan)
                } else {
                    subs_price.visibility = View.VISIBLE
                    subs_info.visibility = View.VISIBLE
                    viewBuyNowAdvanced.visibility = View.VISIBLE
                    viewBuyNowAdvanced.text = mContext.getString(R.string.tv_subscription_now)
                    viewPriceAdvanced.visibility = View.VISIBLE
                    subs_continue.setBackgroundResource(R.drawable.bg_continue_subscription)
                    subs_continue.text = mContext.getString(R.string.tv_continue)
                    subs_continue.visibility = View.VISIBLE
                    subs_continue.setOnClickListener {
                        baseActivity!!.onPurchaseProduct(Constants.SKU_RIFE_ADVANCED_MONTHLY, false)
                    }
                }

                if (SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED)) {
                    viewBuyNowBasic.visibility = View.VISIBLE
                    viewBuyNowBasic.text = mContext.getString(R.string.tv_current_plan)
                    viewPriceMaster.visibility = View.INVISIBLE
                } else {
                    viewBuyNowBasic.visibility = View.INVISIBLE
                    viewPriceMaster.visibility = View.VISIBLE
                }

                if (SharedPreferenceHelper.getInstance()
                        .getBool(Constants.KEY_PURCHASED_HIGH_ABUNDANCE)
                ) {
                    viewBuyNowHigher.visibility = View.VISIBLE
                    viewBuyNowHigher.text = mContext.getString(R.string.tv_current_plan)
                    viewPriceAbundance.visibility = View.INVISIBLE
                } else {
                    viewBuyNowHigher.visibility = View.INVISIBLE
                    viewPriceAbundance.visibility = View.VISIBLE
                }
            }

            2 -> {
                mViewImageBasic.visibility = View.GONE
                mViewImageFree.visibility = View.GONE
                mViewImageAdvanced.visibility = View.GONE
                mViewTitleAdvanced.visibility = View.GONE
                mViewTitleBasic.visibility = View.GONE
                mViewTitleFree.visibility = View.GONE
                rcImageHigher.visibility = View.VISIBLE
                starter_recycler_view.visibility = View.VISIBLE

                subs_price.text = "Monthly Payment of " + SharedPreferenceHelper.getInstance()
                    .getPriceByCurrency(Constants.PRICE_HIGHER_MONTHLY)
                subs_info.text = TextUtils.concat(
                    mContext.getString(R.string.tv_title_subscription_new),
                    " ",
                    mSpannableText
                )
                subs_info.movementMethod = LinkMovementMethod.getInstance()

                if (SharedPreferenceHelper.getInstance()
                        .getBool(Constants.KEY_PURCHASED_HIGH_ABUNDANCE)
                ) {
                    viewBuyNowHigher.visibility = View.VISIBLE
                    viewBuyNowHigher.text = mContext.getString(R.string.tv_current_plan)
                    viewPriceAbundance.visibility = View.INVISIBLE
                    subs_price.visibility = View.GONE
                    subs_info.visibility = View.GONE
                    subs_continue.visibility = View.VISIBLE
                    subs_continue.setBackgroundResource(R.drawable.bg_button_current_plan)
                    subs_continue.text = mContext.getString(R.string.tv_current_plan)
                } else {
                    subs_price.visibility = View.VISIBLE
                    subs_info.visibility = View.VISIBLE
                    viewBuyNowHigher.visibility = View.VISIBLE
                    viewBuyNowHigher.text = mContext.getString(R.string.tv_subscription_now)
                    viewPriceAbundance.visibility = View.VISIBLE
                    subs_continue.setBackgroundResource(R.drawable.bg_continue_subscription)
                    subs_continue.text = mContext.getString(R.string.tv_continue)
                    subs_continue.visibility = View.VISIBLE
                    subs_continue.setOnClickListener {
//                        baseActivity?.showAlert("Higher Month")
                        baseActivity!!.onPurchaseProduct(Constants.SKU_RIFE_HIGHER_MONTHLY, false)
                    }
                }

                if (SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED)) {
                    viewBuyNowBasic.visibility = View.VISIBLE
                    viewBuyNowBasic.text = mContext.getString(R.string.tv_current_plan)
                    viewPriceMaster.visibility = View.INVISIBLE
                } else {
                    viewBuyNowBasic.visibility = View.INVISIBLE
                    viewPriceMaster.visibility = View.VISIBLE
                }
                if (SharedPreferenceHelper.getInstance()
                        .getBool(Constants.KEY_PURCHASED_ADVANCED)
                ) {
                    viewBuyNowAdvanced.visibility = View.VISIBLE
                    viewBuyNowAdvanced.text = mContext.getString(R.string.tv_current_plan)
                    viewPriceAdvanced.visibility = View.INVISIBLE
                } else {
                    viewBuyNowAdvanced.visibility = View.INVISIBLE
                    viewPriceAdvanced.visibility = View.VISIBLE
                }
            }
        }

    }
    private fun setTermsSpan() {
        try {
            val text = context.getString(R.string.tv_term_and_privacy)
            val list = text.split("|")
            if (list.size == 3) {
                val listLength = list.map { it.length }
                mSpannableText = SpannableString(text.replace("|", ""))
                val clickPrivacy = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        val url = "http://www.tattoobookapp.com/quantumwavebiotechnology/privacy"
                        val i = Intent(Intent.ACTION_VIEW)
                        i.data = Uri.parse(url)
                        baseActivity!!.startActivity(i)
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
                        baseActivity!!.startActivity(i)
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.color = Color.WHITE
                        ds.typeface = Typeface.DEFAULT_BOLD
                    }
                }
                mSpannableText?.setSpan(
                    clickTerms,
                    0,
                    listLength[0],
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                mSpannableText?.setSpan(
                    clickPrivacy,
                    listLength[0] + listLength[1],
                    mSpannableText!!.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        } catch (_: Exception) {
        }
    }

    private fun random(): Int {
        val mRandom = Random()
        return mRandom.nextInt(3) + 1
    }

    fun addListener() {

        mImvDismiss.setOnClickListener {
            dismiss()
        }

        mViewSubscriptionFree.setOnClickListener {
            mViewImageFree.visibility = View.VISIBLE
            mViewTitleFree.visibility = View.VISIBLE
            mViewImageAdvanced.visibility = View.GONE
            mViewImageBasic.visibility = View.GONE
            mViewTitleAdvanced.visibility = View.GONE
            mViewTitleBasic.visibility = View.GONE
            rcImageHigher.visibility = View.GONE
            starter_recycler_view.visibility = View.GONE
            subs_price.visibility = View.INVISIBLE
            subs_info.visibility = View.INVISIBLE
            subs_continue.visibility = View.INVISIBLE
            if (mContext != null) {
                if (SharedPreferenceHelper.getInstance()
                        .getBool(Constants.KEY_PURCHASED_ADVANCED)
                ) {
                    viewBuyNowAdvanced.visibility = View.VISIBLE
                    viewBuyNowAdvanced.text = mContext.getString(R.string.tv_current_plan)
                    viewPriceAdvanced.visibility = View.INVISIBLE
                } else {
                    viewBuyNowAdvanced.visibility = View.INVISIBLE
                    viewPriceAdvanced.visibility = View.VISIBLE
                }

                if (SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED)) {
                    viewBuyNowBasic.visibility = View.VISIBLE
                    viewBuyNowBasic.text = mContext.getString(R.string.tv_current_plan)
                    viewPriceMaster.visibility = View.INVISIBLE
                } else {
                    viewBuyNowBasic.visibility = View.INVISIBLE
                    viewPriceMaster.visibility = View.VISIBLE
                }

                if (SharedPreferenceHelper.getInstance()
                        .getBool(Constants.KEY_PURCHASED_HIGH_ABUNDANCE)
                ) {
                    viewBuyNowHigher.visibility = View.VISIBLE
                    viewBuyNowHigher.text = mContext.getString(R.string.tv_current_plan)
                    viewPriceAbundance.visibility = View.INVISIBLE
                } else {
                    viewBuyNowHigher.visibility = View.INVISIBLE
                    viewPriceAbundance.visibility = View.VISIBLE
                }
            }
        }
        mViewSubscriptionBasic.setOnClickListener {
            setTypeAlbum(0)
            initComponents()
        }
        mViewSubscriptionAdvanced.setOnClickListener {
            setTypeAlbum(1)
            initComponents()
        }
        mViewSubscriptionHigher.setOnClickListener {
            setTypeAlbum(2)
            initComponents()
        }
    }
}
