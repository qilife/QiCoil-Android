package com.Meditation.Sounds.frequencies.lemeor.ui.programs.detail

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.database.dao.TrackDao
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.getConvertedTime
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail.TrackOptionsPopUpActivity
import com.Meditation.Sounds.frequencies.utils.Constants
import kotlinx.android.synthetic.main.activity_pop.pop_btn_minus
import kotlinx.android.synthetic.main.activity_pop.pop_btn_plus
import kotlinx.android.synthetic.main.activity_pop.pop_tv_duration
import kotlinx.android.synthetic.main.activity_pop.track_move_down
import kotlinx.android.synthetic.main.activity_pop.track_move_up
import kotlinx.android.synthetic.main.activity_pop.track_remove
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.abs

class PopActivity : AppCompatActivity() {

    private var track: Track? = null
    private var duration: Long = 0
    private var db: DataBase? = null
    private var trackDao: TrackDao? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pop)

        db = DataBase.getInstance(applicationContext)
        trackDao = db?.trackDao()

        initUI()

        setUI()
    }

    private fun initUI() {
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)

        val width = dm.widthPixels
        val height = dm.heightPixels

        if (resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            window.setLayout((width * .25).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)
        } else if (resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            window.setLayout((width * .55).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        val params = window.attributes
        params.gravity = Gravity.CENTER
        params.x = 0
        params.y = -20

        window.attributes = params

        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun setUI() {
        val trackId =
            intent.getDoubleExtra(TrackOptionsPopUpActivity.EXTRA_TRACK_ID, Constants.defaultHz - 1)

        if (trackId <= Constants.defaultHz - 1) {
            Toast.makeText(
                applicationContext,
                getString(R.string.error_hz_exceeded, abs(Constants.defaultHz).toString()),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        GlobalScope.launch {
            if (trackId >= 0) {
                track = trackDao?.getTrackById(trackId.toInt())

                val dur = track?.duration ?: 0
                duration = if (dur > 0) {
                    dur
                } else {
                    300000
                }
                pop_tv_duration.text = getConvertedTime(duration)
            }
        }

        track_move_up.setOnClickListener {
            sendResult("track_move_up", trackId)
        }

        track_move_down.setOnClickListener {
            sendResult("track_move_down", trackId)
        }

        track_remove.setOnClickListener {
            sendResult("track_remove", trackId)
        }

        pop_btn_minus.setOnClickListener {
            var d = duration

            if (d > 600000) {
                d -= 300000
            } else {
                d = 300000
            }

            pop_tv_duration.text = getConvertedTime(d)

            duration = d
        }

        pop_btn_plus.setOnClickListener {
            var d = duration

            if (d < 86400000) {
                d += 300000
            } else if (d >= 86400000) {
                d = 86400000
            }

            pop_tv_duration.text = getConvertedTime(d)

            duration = d
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            if (track != null) {
                GlobalScope.launch { trackDao?.setDuration(duration, track?.id!!) }
            }
        } catch (_: Exception) {
        }
    }

    private fun sendResult(action: String, trackId: Double) {
        val intent = Intent()
        intent.putExtra(EXTRA_ACTION, action)
        intent.putExtra(EXTRA_TRACK_ID, trackId)
        setResult(RESULT_OK, intent)
        finish()
    }

    companion object {
        const val EXTRA_TRACK_ID = "extra_track_id"
        const val EXTRA_ACTION = "extra_action"

        fun newIntent(context: Context?, id: Double): Intent {
            val intent = Intent(context, PopActivity::class.java)
            intent.putExtra(EXTRA_TRACK_ID, id)
            return intent
        }
    }
}