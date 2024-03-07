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
import android.os.CountDownTimer
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.adapters.Abundance2Adapter
import com.Meditation.Sounds.frequencies.adapters.TitleAdapter
import com.Meditation.Sounds.frequencies.feature.album.AlbumsViewModel
import com.Meditation.Sounds.frequencies.feature.base.BaseActivity
import com.Meditation.Sounds.frequencies.models.Album
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import com.Meditation.Sounds.frequencies.utils.Utils
import kotlinx.android.synthetic.main.dialog_subscription_flash_sale.mImvDismiss
import kotlinx.android.synthetic.main.dialog_subscription_flash_sale.mTvPriceFS
import kotlinx.android.synthetic.main.dialog_subscription_flash_sale.mTvPriceFrom
import kotlinx.android.synthetic.main.dialog_subscription_flash_sale.mTvUnlockAll
import kotlinx.android.synthetic.main.dialog_subscription_flash_sale.mViewTitleUnlockAdvanced
import kotlinx.android.synthetic.main.dialog_subscription_flash_sale.mViewTitleUnlockBasic
import kotlinx.android.synthetic.main.dialog_subscription_flash_sale.rcImageHigher
import kotlinx.android.synthetic.main.dialog_subscription_flash_sale.starter_recycler_view
import kotlinx.android.synthetic.main.dialog_subscription_flash_sale.subs_continue
import kotlinx.android.synthetic.main.dialog_subscription_flash_sale.subs_info
import kotlinx.android.synthetic.main.dialog_subscription_flash_sale.subs_price
import kotlinx.android.synthetic.main.dialog_subscription_flash_sale.tvHours
import kotlinx.android.synthetic.main.dialog_subscription_flash_sale.tvMinutes
import kotlinx.android.synthetic.main.dialog_subscription_flash_sale.tvSeconds
import kotlinx.android.synthetic.main.dialog_subscription_flash_sale.viewGroupImageAdvanced
import kotlinx.android.synthetic.main.dialog_subscription_flash_sale.viewGroupImageBasic
import kotlinx.android.synthetic.main.dialog_subscription_flash_sale.viewGroupImageHigher
import java.util.Random

class SubscriptionDialogFlashSale(private val mContext: Context?) :
    Dialog(mContext!!, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {

    var baseActivity: BaseActivity? = null
    private lateinit var mViewModel: AlbumsViewModel
    private var mSpannableText: SpannableString? = null
    private var mTitleHigherAdapter: TitleAdapter? = null
    private var mAlbumHigherAdapter: Abundance2Adapter? = null
    private var mAlbumHighers = ArrayList<Album>()
    private var mTitleHighers = ArrayList<String>()
    private var mTypeAlbum: Int = 0

    private val broadcastReceiverSubscriptionController = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            if (SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_ADVANCED)
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
        this.mTypeAlbum = type
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_subscription_flash_sale)
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

    override fun dismiss() {
        try {
            mContext!!.unregisterReceiver(broadcastReceiverSubscriptionController)
        } catch (e: IllegalArgumentException) {

        }
        if (mCountDownTimer != null) {
            mCountDownTimer!!.cancel()
        }
        super.dismiss()
    }

    private fun addListTitleHigher() {
        mTitleHighers.clear()
        mTitleHighers.add("Unlock 43 Essential Frequencies")
        mTitleHighers.add("Unlock 189 Abundance Frequencies")
        mTitleHighers.add("Unlock 261 Higher Abundance Frequencies")
        mTitleHighers.add("Increase Luck and Coincidences")
        mTitleHighers.add("Amplify Your Manifestation Power")
        mTitleHighers.add("Design Your Reality")
        mTitleHighers.add("Enhance Your Intuition")
        mTitleHighers.add("Awaken Your Third Eye")
        mTitleHighers.add("Attract Success and Abundance")
        mTitleHighers.add("Profound Conscious & Subconscious Stimulation")
        mTitleHighers.add("Stimulate Healing & Promote Vitality")
        mTitleHighers.add("Balance Your Chakras")
        mTitleHighers.add("EMF Protection")
    }

    fun initComponents() {
        mViewModel =
            ViewModelProviders.of(mContext as BaseActivity).get(AlbumsViewModel::class.java)
        mViewModel.getAlbumsHigherAbundance().observe(mContext, androidx.lifecycle.Observer {
            if (it != null) {
                mAlbumHighers = it as ArrayList<Album>
//                mAlbumHigherAdapter?.data = it
                mAlbumHigherAdapter?.notifyDataSetChanged()
            }
        })
        addListTitleHigher()
        setTermsSpan()

        mTitleHigherAdapter = TitleAdapter(mTitleHighers)
        starter_recycler_view.layoutManager = LinearLayoutManager(mContext)
        starter_recycler_view.adapter = mTitleHigherAdapter

//        mAlbumHigherAdapter = Abundance2Adapter(mContext, mAlbumHighers)
        rcImageHigher.layoutManager = GridLayoutManager(mContext, 3)
        rcImageHigher.adapter = mAlbumHigherAdapter

        val flashSaleRemainTimeGloble = Utils.getFlashSaleRemainTime()
        if (flashSaleRemainTimeGloble > 0) {
            when (mTypeAlbum) {
                0 -> {//Album basic
                    if (random() == 1) {
                        //Flash Sale Basic Yearly
                        mTvUnlockAll.text =
                            TextUtils.concat("Essential Frequencies Annual Subscription")
                        mTvPriceFS.text = SharedPreferenceHelper.getInstance()
                            .getPriceByCurrency(Constants.PRICE_1_YEAR_FLASH_SALE)
                        mTvPriceFrom.text = SharedPreferenceHelper.getInstance()
                            .getPriceByCurrency(Constants.PRICE_1_YEAR)
                        viewGroupImageAdvanced.visibility = View.GONE
                        viewGroupImageBasic.visibility = View.VISIBLE
                        subs_price.text =
                            context.getString(R.string.tv_yearly_payment_of) + " " + SharedPreferenceHelper.getInstance()
                                .getPriceByCurrency(Constants.PRICE_1_YEAR_FLASH_SALE)
                        subs_info.text = TextUtils.concat(
                            mContext.getString(R.string.tv_title_subscription_new),
                            " ",
                            mSpannableText
                        )
                        subs_info.movementMethod = LinkMovementMethod.getInstance()
                        subs_continue.text = SharedPreferenceHelper.getInstance()
                            .getPriceByCurrency(Constants.PRICE_1_YEAR_FLASH_SALE) + " / Year"
                        subs_continue.setOnClickListener {
                            baseActivity!!.onPurchaseProduct(
                                Constants.SKU_RIFE_YEARLY_FLASHSALE,
                                false
                            )
                        }
                    } else {
                        //Flash Sale Basic Lifetime
                        mTvUnlockAll.text =
                            TextUtils.concat("Essential Frequencies Lifetime Subscription")
                        mTvPriceFS.text = SharedPreferenceHelper.getInstance()
                            .getPriceByCurrency(Constants.PRICE_LIFETIME_FLASH_SALE)
                        mTvPriceFrom.text = SharedPreferenceHelper.getInstance()
                            .getPriceByCurrency(Constants.PRICE_LIFETIME)
                        viewGroupImageAdvanced.visibility = View.GONE
                        viewGroupImageBasic.visibility = View.VISIBLE
                        subs_price.text =
                            context.getString(R.string.tv_one_time_payment) + " of " + SharedPreferenceHelper.getInstance()
                                .getPriceByCurrency(Constants.PRICE_LIFETIME_FLASH_SALE)
                        subs_info.text = TextUtils.concat(
                            mContext.getString(R.string.tv_title_subscription_new),
                            " ",
                            mSpannableText
                        )
                        subs_info.movementMethod = LinkMovementMethod.getInstance()
                        subs_continue.text = SharedPreferenceHelper.getInstance()
                            .getPriceByCurrency(Constants.PRICE_LIFETIME_FLASH_SALE)
                        subs_continue.setOnClickListener {
                            baseActivity!!.onPurchaseProduct(
                                Constants.SKU_RIFE_LIFETIME_FLASHSALE,
                                true
                            )
                        }
                    }
                    mViewTitleUnlockBasic.visibility = View.VISIBLE
                    mViewTitleUnlockAdvanced.visibility = View.GONE
                    viewGroupImageHigher.visibility = View.GONE
                    starter_recycler_view.visibility = View.GONE
                }

                1 -> {//Album advanced
                    if (random() == 1) {
                        //Flash Sale Advanced Lifetime
                        mTvUnlockAll.text =
                            TextUtils.concat("Abundance Frequencies Lifetime Subscription")
                        mTvPriceFS.text = SharedPreferenceHelper.getInstance()
                            .getPriceByCurrency(Constants.PRICE_ADVANCED_LIFETIME_FLASH_SALE)
                        mTvPriceFrom.text = SharedPreferenceHelper.getInstance()
                            .getPriceByCurrency(Constants.PRICE_ADVANCED_LIFETIME)
                        viewGroupImageAdvanced.visibility = View.VISIBLE
                        viewGroupImageBasic.visibility = View.GONE
                        subs_price.text =
                            context.getString(R.string.tv_one_time_payment) + " of " + SharedPreferenceHelper.getInstance()
                                .getPriceByCurrency(Constants.PRICE_ADVANCED_LIFETIME_FLASH_SALE)
                        subs_info.text = TextUtils.concat(
                            mContext.getString(R.string.tv_title_subscription_new),
                            " ",
                            mSpannableText
                        )
                        subs_info.movementMethod = LinkMovementMethod.getInstance()
                        subs_continue.text = SharedPreferenceHelper.getInstance()
                            .getPriceByCurrency(Constants.PRICE_ADVANCED_LIFETIME_FLASH_SALE)
                        subs_continue.setOnClickListener {
                            baseActivity!!.onPurchaseProduct(
                                Constants.SKU_RIFE_ADVANCED_LIFETIME_FLASHSALE,
                                true
                            )
                        }
                    } else {
                        //Flash Sale Advanced Yearly
                        mTvUnlockAll.text =
                            TextUtils.concat("Abundance Frequencies Annual Subscription")
                        mTvPriceFS.text = SharedPreferenceHelper.getInstance()
                            .getPriceByCurrency(Constants.PRICE_ADVANCED_YEAR_FLASH_SALE)
                        mTvPriceFrom.text = SharedPreferenceHelper.getInstance()
                            .getPriceByCurrency(Constants.PRICE_ADVANCED_YEAR)
                        viewGroupImageAdvanced.visibility = View.VISIBLE
                        viewGroupImageBasic.visibility = View.GONE
                        subs_price.text =
                            context.getString(R.string.tv_yearly_payment_of) + " " + SharedPreferenceHelper.getInstance()
                                .getPriceByCurrency(Constants.PRICE_ADVANCED_YEAR_FLASH_SALE)
                        subs_info.text = TextUtils.concat(
                            mContext.getString(R.string.tv_title_subscription_new),
                            " ",
                            mSpannableText
                        )
                        subs_info.movementMethod = LinkMovementMethod.getInstance()
                        subs_continue.text = SharedPreferenceHelper.getInstance()
                            .getPriceByCurrency(Constants.PRICE_ADVANCED_YEAR_FLASH_SALE) + " / Year"
                        subs_continue.setOnClickListener {
                            baseActivity!!.onPurchaseProduct(
                                Constants.SKU_RIFE_ADVANCED_YEAR_FLASHSALE,
                                false
                            )
                        }
                    }

                    mViewTitleUnlockBasic.visibility = View.GONE
                    mViewTitleUnlockAdvanced.visibility = View.VISIBLE
                    viewGroupImageHigher.visibility = View.GONE
                    starter_recycler_view.visibility = View.GONE
                }

                2 -> {//Album Higher
                    mViewTitleUnlockBasic.visibility = View.GONE
                    mViewTitleUnlockAdvanced.visibility = View.GONE
                    viewGroupImageAdvanced.visibility = View.GONE
                    viewGroupImageBasic.visibility = View.GONE
                    viewGroupImageHigher.visibility = View.VISIBLE
                    starter_recycler_view.visibility = View.VISIBLE
                    subs_continue.text = mContext.getString(R.string.tv_continue)
                    if (random() == 1) {
                        //Flash Sale  Lifetime
                        mTvPriceFrom.text = SharedPreferenceHelper.getInstance()
                            .getPriceByCurrency(Constants.PRICE_HIGHER_LIFETIME)
                        mTvPriceFS.text = SharedPreferenceHelper.getInstance()
                            .getPriceByCurrency(Constants.PRICE_HIGHER_LIFETIME_FLASH_SALE)
                        mTvUnlockAll.text =
                            TextUtils.concat("Higher Abundance Frequencies Lifetime Subscription")
                        subs_continue.setOnClickListener {
                            baseActivity!!.onPurchaseProduct(
                                Constants.SKU_RIFE_HIGHER_LIFETIME_FLASH_SALE,
                                true
                            )
                        }
                        subs_price.text =
                            context.getString(R.string.tv_one_time_payment) + " " + SharedPreferenceHelper.getInstance()
                                .getPriceByCurrency(Constants.PRICE_HIGHER_LIFETIME_FLASH_SALE)
                    } else {
                        //Flash Sale  Yearly
                        mTvPriceFrom.text = SharedPreferenceHelper.getInstance()
                            .getPriceByCurrency(Constants.PRICE_HIGHER_ANNUAL)
                        mTvPriceFS.text = SharedPreferenceHelper.getInstance()
                            .getPriceByCurrency(Constants.PRICE_HIGHER_ANNUAL_FLASH_SALE)
                        mTvUnlockAll.text =
                            TextUtils.concat("Higher Abundance Frequencies Annual Subscription")
                        subs_continue.setOnClickListener {
                            baseActivity!!.onPurchaseProduct(
                                Constants.SKU_RIFE_HIGHER_ANNUAL_FLASH_SALE,
                                true
                            )
                        }
                        subs_price.text =
                            context.getString(R.string.tv_yearly_payment_of) + " " + SharedPreferenceHelper.getInstance()
                                .getPriceByCurrency(Constants.PRICE_HIGHER_ANNUAL_FLASH_SALE)
                    }
                    subs_info.text = TextUtils.concat(
                        mContext.getString(R.string.tv_title_subscription_new),
                        " ",
                        mSpannableText
                    )
                    subs_info.movementMethod = LinkMovementMethod.getInstance()
                }
            }
        } else {
            dismiss()
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

    private var mCountDownTimer: CountDownTimer? = null

    private fun setCountdownTimer(totalTime: Long) {
        mCountDownTimer = object : CountDownTimer(totalTime, 1000) {
            override fun onTick(l: Long) {
                val totalSeconds = (l / 1000).toInt()
                val days = totalSeconds / (24 * 3600)
                var remainder = totalSeconds - (days * 24 * 3600)
                val hours = remainder / 3600
                remainder -= (hours * 3600)
                val mins = remainder / 60
                remainder -= mins * 60
                val secs = remainder

                tvHours.text = if (hours > 9) "" + hours else "0$hours"
                tvMinutes.text = if (mins > 9) "" + mins else "0$mins"
                tvSeconds.text = if (secs > 9) "" + secs else "0$secs"
            }

            override fun onFinish() {
                initComponents()
            }
        }
        mCountDownTimer!!.start()
    }

    private fun random(): Int {
        val mRandom = Random()
        return mRandom.nextInt(3) + 1
    }

    fun addListener() {
        val flashSaleRemainTimeGloble = Utils.getFlashSaleRemainTime()
        if (flashSaleRemainTimeGloble > 0) {
            setCountdownTimer(flashSaleRemainTimeGloble)
        }

        mImvDismiss.setOnClickListener {
            dismiss()
        }
    }
}
