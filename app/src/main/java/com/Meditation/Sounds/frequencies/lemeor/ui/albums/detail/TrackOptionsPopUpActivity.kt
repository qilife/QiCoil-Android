package com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.FAVORITES
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.database.dao.TrackDao
import com.Meditation.Sounds.frequencies.lemeor.data.model.Rife
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.getConvertedTime
import com.Meditation.Sounds.frequencies.lemeor.getPreloadedSaveDir
import com.Meditation.Sounds.frequencies.lemeor.getSaveDir
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.DownloadService
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.DownloaderActivity
import com.Meditation.Sounds.frequencies.lemeor.trackIdForProgram
import com.Meditation.Sounds.frequencies.lemeor.ui.main.UpdateTrack
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.NewProgramViewModel
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.utils.getRifeFormat
import com.Meditation.Sounds.frequencies.utils.parcelable
import kotlinx.android.synthetic.main.activity_pop_up_track_options.btn_minus
import kotlinx.android.synthetic.main.activity_pop_up_track_options.btn_plus
import kotlinx.android.synthetic.main.activity_pop_up_track_options.track_add_favorites
import kotlinx.android.synthetic.main.activity_pop_up_track_options.track_add_program
import kotlinx.android.synthetic.main.activity_pop_up_track_options.track_redownload
import kotlinx.android.synthetic.main.activity_pop_up_track_options.tv_duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import kotlin.math.abs

class TrackOptionsPopUpActivity : AppCompatActivity() {

    private var track: Track? = null
    private var duration: Long = 0
    private var db: DataBase? = null
    private var trackDao: TrackDao? = null
    private lateinit var mViewModel: NewProgramViewModel

    private val rife: Rife? by lazy {
        intent.parcelable<Rife>(EXTRA_RIFE)
    }
    private val trackId: Double by lazy {
        intent.getDoubleExtra(EXTRA_TRACK_ID, Constants.defaultHz - 1)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: Any?) {
        if (event == DownloadService.DOWNLOAD_FINISH) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pop_up_track_options)

        db = DataBase.getInstance(applicationContext)
        trackDao = db?.trackDao()
        mViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(this).apiService), DataBase.getInstance(this)
            )
        )[NewProgramViewModel::class.java]
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
        //  program add two obj(rife and track) "rife limit -22000->0" and "track id integer"
        if (trackId <= Constants.defaultHz - 1) {
            Toast.makeText(
                applicationContext,
                getString(R.string.error_hz_exceeded, abs(Constants.defaultHz).toString()),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (trackId >= 0.0) {
            CoroutineScope(Dispatchers.IO).launch {
                track = trackDao?.getTrackById(trackId.toInt())
                if (track is Track) {
                    CoroutineScope(Dispatchers.Main).launch {
                        if (track == null) {
                            finish()
                        }

                        if (track != null && (track as Track).isFavorite) {
                            track_add_favorites.text = getString(R.string.tv_remove_from_favorite)
                        } else {
                            track_add_favorites.text = getString(R.string.tv_add_to_favorites)
                        }


                        val dur = (track as Track).duration
                        duration = if (dur > 0) {
                            dur
                        } else {
                            300000
                        }
                        tv_duration.text = getConvertedTime(duration)
                    }
                }
            }
        } else {
            val programDao = db?.programDao()
            CoroutineScope(Dispatchers.IO).launch {
                val program = programDao?.getProgramByName(FAVORITES)
                program?.let { p ->
                    val formatRife = rife?.getRifeFormat(trackId)
                    val frequency = p.records.firstOrNull {
                        it == (formatRife ?: trackId.toString())
                    }
                    if (frequency != null) {
                        track_add_favorites.text = getString(R.string.tv_remove_from_favorite)
                    } else {
                        track_add_favorites.text = getString(R.string.tv_add_to_favorites)
                    }
                }
            }
            track_add_favorites.visibility = View.VISIBLE
            track_redownload.visibility = View.GONE
        }

        track_add_program.setOnClickListener {
            trackIdForProgram = if (trackId < 0.0 && trackId >= Constants.defaultHz) {
                rife?.getRifeFormat(trackId) ?: (Constants.defaultHz - 1).toString()
            } else {
                track?.id.toString()
            }

            val intent = Intent()
            setResult(RESULT_OK, intent)
            finish()
        }

        track_add_favorites.setOnClickListener {
            val programDao = db?.programDao()
            if (rife != null) {
                if (trackId < 0.0 && trackId >= Constants.defaultHz) {
                    val formatRife = rife!!.getRifeFormat(trackId)
                    CoroutineScope(Dispatchers.IO).launch {
                        val program = programDao?.getProgramByName(FAVORITES)
                        program?.let { p ->
                            val frequency = p.records.firstOrNull {
                                it == formatRife
                            }
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    applicationContext,
                                    if (frequency != null) getString(R.string.tv_removed_favorite) else getString(
                                        R.string.tv_add_favorite
                                    ),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            if (frequency != null) p.records.remove(frequency)
                            else p.records.add(formatRife)
                            programDao.updateProgram(p)
                            if (p.user_id.isNotEmpty()) {
                                try {
                                    mViewModel.updateTrackToProgram(
                                        UpdateTrack(
                                            track_id = listOf(formatRife),
                                            id = p.id,
                                            track_type = "rife",
                                            request_type = if (frequency != null) "remove" else "add",
                                            is_favorite = (p.name.uppercase() == FAVORITES.uppercase() && p.favorited)
                                        )
                                    )
                                } catch (_: Exception) {
                                }
                            }
                        }
                    }
                }
            } else {
                track?.let { t ->
                    Toast.makeText(
                        applicationContext,
                        if (t.isFavorite) getString(R.string.tv_removed_favorite) else getString(R.string.tv_add_favorite),
                        Toast.LENGTH_SHORT
                    ).show()

                    CoroutineScope(Dispatchers.IO).launch {
                        val program = programDao?.getProgramByName(FAVORITES)
                        program?.let { p ->
                            if (t.isFavorite) {
                                p.records.remove(t.id.toString())
                            } else {
                                p.records.add(t.id.toString())
                            }
                            programDao.updateProgram(p)
                            if (p.user_id.isNotEmpty()) {
                                try {
                                    mViewModel.updateTrackToProgram(
                                        UpdateTrack(
                                            track_id = listOf(t.id.toString()),
                                            id = p.id,
                                            track_type = "mp3",
                                            request_type = if (t.isFavorite) "remove" else "add",
                                            is_favorite = (p.name.uppercase() == FAVORITES.uppercase() && p.favorited)
                                        )
                                    )
                                } catch (_: Exception) {
                                }
                            }
                        }

                        trackDao?.isTrackFavorite(!t.isFavorite, t.id)
                    }
                }
            }

            finish()
        }

        track_redownload.setOnClickListener {
            if (!Utils.isConnectedToNetwork(applicationContext)) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.err_network_available),
                    Toast.LENGTH_SHORT
                ).show()
            }
            finish()
            val tracks = ArrayList<Track>()

            val dao = DataBase.getInstance(applicationContext).albumDao()

            CoroutineScope(Dispatchers.IO).launch {

                // val track = trackDao.getTrackById(track.id)

                //for preloaded tracks
                try {
                    track?.let {
                        val album = it.album ?: dao.getAlbumById(it.albumId, it.category_id)
                        it.album = album
                        val file = File(
                            getSaveDir(
                                applicationContext, it.filename, album?.audio_folder ?: ""
                            )
                        )
                        val preloaded = File(
                            getPreloadedSaveDir(
                                applicationContext, it.filename, album?.audio_folder ?: ""
                            )
                        )
                        if (!file.exists() && !preloaded.exists()) {
                            tracks.add(it)
                        }
                        CoroutineScope(Dispatchers.Main).launch {
                            DownloaderActivity.startDownload(this@TrackOptionsPopUpActivity, tracks)
                            startActivity(DownloaderActivity.newIntent(applicationContext))
                        }
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }

        btn_minus.setOnClickListener {
            var d = duration

            if (d > 600000) {
                d -= 300000
            } else {
                d = 300000
            }

            tv_duration.text = getConvertedTime(d)

            duration = d
        }

        btn_plus.setOnClickListener {
            var d = duration

            if (d < 86400000) {
                d += 300000
            } else {
                d = 86400000
            }

            tv_duration.text = getConvertedTime(d)

            duration = d
        }
    }

    override fun onStop() {
        super.onStop()
        val trackId = intent.getDoubleExtra(EXTRA_TRACK_ID, Constants.defaultHz - 1)
        if (trackId >= 0.0) {
            CoroutineScope(Dispatchers.IO).launch { trackDao?.setDuration(duration, track?.id!!) }
        }
    }

    companion object {
        const val EXTRA_TRACK_ID = "extra_track_id"
        private const val EXTRA_RIFE = "extra_rife"

        fun newIntent(context: Context?, id: Double, rife: Rife? = null): Intent {
            val intent = Intent(context, TrackOptionsPopUpActivity::class.java)
            intent.putExtra(EXTRA_TRACK_ID, id)
            intent.putExtra(EXTRA_RIFE, rife)

            return intent
        }
    }
}