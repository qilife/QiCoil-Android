package com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.album.detail.DescriptionAdapter
import com.Meditation.Sounds.frequencies.lemeor.*
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.database.dao.AlbumDao
import com.Meditation.Sounds.frequencies.lemeor.data.database.dao.TrackDao
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.DownloadService
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.DownloaderActivity
import com.Meditation.Sounds.frequencies.lemeor.tools.player.MusicRepository
import com.Meditation.Sounds.frequencies.lemeor.tools.player.PlayerSelected
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs.TiersPagerFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.main.NavigationActivity
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.NewProgramFragment
import com.Meditation.Sounds.frequencies.utils.Utils
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.analytics.FirebaseAnalytics.Param
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.album_item.view.*
import kotlinx.android.synthetic.main.fragment_new_album_detail.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

class NewAlbumDetailFragment : Fragment() {

    private val albumId: Int by lazy {
        arguments?.getInt(ARG_ALBUM_ID) ?: throw IllegalArgumentException("Must call through newInstance()")
    }

    private lateinit var mViewModel: NewAlbumDetailViewModel
    private var mDescriptionAdapter: DescriptionAdapter? = null
    private var mTrackAdapter: AlbumTrackAdapter? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private var trackDao: TrackDao? = null
    private var albumDao: AlbumDao? = null
    private var isDownloaded: Boolean = true

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: Any?) {
        if (event == DownloadService.DOWNLOAD_FINISH) {
            downloadedTracks = null

            GlobalScope.launch {
                var isDownloaded = true

                album?.tracks?.forEach {
                    Log.e("DIRRRRRRRR", getSaveDir(requireContext(), it, album!!))
                    val file = File(getSaveDir(requireContext(), it, album!!))
                    val preloaded = File(getPreloadedSaveDir(requireContext(), it, album!!))

                    if (!file.exists() && !preloaded.exists()) {
                        isDownloaded = false
                    }
                }

                this@NewAlbumDetailFragment.isDownloaded = isDownloaded

                CoroutineScope(Dispatchers.Main).launch {
                    if (!isDownloaded) {
                        album_play.text = getString(R.string.btn_download)
                    } else {
                        album_play.text = getString(R.string.btn_play)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_new_album_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        firebaseAnalytics = Firebase.analytics
        initUI()

        mViewModel.album(albumId)?.observe(viewLifecycleOwner, {
            album = it
            setUI(it)
        })

        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                onBackPressed()
                true
            } else false
        }
    }

    private fun onBackPressed() {
        var fragment = selectedNaviFragment
        if (fragment == null) { fragment = TiersPagerFragment() }

        parentFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.trans_left_to_right_in, R.anim.trans_left_to_right_out, R.anim.trans_right_to_left_in, R.anim.trans_right_to_left_out)
                .replace(R.id.nav_host_fragment, fragment, fragment.javaClass.simpleName)
                .commit()
    }

    private fun initUI() {
        mViewModel = ViewModelProvider(this,
                ViewModelFactory(
                        ApiHelper(RetrofitBuilder(requireContext()).apiService),
                        DataBase.getInstance(requireContext()))
        ).get(NewAlbumDetailViewModel::class.java)

        trackDao = DataBase.getInstance(requireContext()).trackDao()
        albumDao = DataBase.getInstance(requireContext()).albumDao()
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun setUI(album: Album) {
        currentTrackIndex.observe(viewLifecycleOwner, {
            album.tracks.forEachIndexed { index, _ ->
                if (index == it) {
                    mTrackAdapter?.setSelected(index)
                }
            }
        })

        album_back.setOnClickListener { onBackPressed() }

        album_image.radius = resources.getDimensionPixelOffset(R.dimen.corner_radius_album)

        loadImage(requireContext(), album_image, album)

        mDescriptionAdapter = album.descriptions?.let { DescriptionAdapter(requireContext(), it) }
        album_description_recycler.adapter = mDescriptionAdapter

        album_play.setOnClickListener { playOrDownload(album) }

        mTrackAdapter = AlbumTrackAdapter(requireContext(), album.tracks, album)

        mTrackAdapter!!.setOnClickListener(object : AlbumTrackAdapter.Listener {
            override fun onTrackClick(track: Track, i: Int, isDownloaded: Boolean) {
                if (this@NewAlbumDetailFragment.isDownloaded) {
                    isMultiPlay = false
                    mTrackAdapter?.setSelected(i)
                    play(album)
                    Handler(Looper.getMainLooper()).postDelayed({ EventBus.getDefault().post(PlayerSelected(i)) }, 200)
                }
            }
            override fun onTrackOptions(track: Track, i: Int) {
                startActivityForResult(TrackOptionsPopUpActivity.newIntent(requireContext(), track.id), 1001)
            }
        })
        album_tracks_recycler.adapter = mTrackAdapter

        GlobalScope.launch {
            var isDownloaded = true

            album.tracks.forEach {
                //for preloaded tracks
                val file = File(getSaveDir(requireContext(), it, album))
                val preloaded = File(getPreloadedSaveDir(requireContext(), it, album))

                if (!file.exists() && !preloaded.exists()) {
                    isDownloaded = false
                }
            }

            this@NewAlbumDetailFragment.isDownloaded = isDownloaded

            CoroutineScope(Dispatchers.Main).launch {
                if (!isDownloaded) {
                    album_play.text = getString(R.string.btn_download)
                } else {
                    album_play.text = getString(R.string.btn_play)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun playOrDownload(album: Album) {
        if (!isDownloaded) {
            firebaseAnalytics.logEvent("Downloads") {
                param("Album Id", album.id.toString())
                param("Album Name", album.name)
               // param(FirebaseAnalytics.Param.CONTENT_TYPE, "image")
            }
            if (!Utils.isConnectedToNetwork(requireContext())) {
                Toast.makeText(requireContext(), getString(R.string.err_network_available), Toast.LENGTH_SHORT).show()
                return
            }

            val tracks = ArrayList<Track>()
            val trackDao = DataBase.getInstance(requireContext()).trackDao()

            GlobalScope.launch {
                album.tracks.forEach { t->
                    val track = trackDao.getTrackById(t.id)
                    // /data/user/0/com.Meditation.Sounds.frequencies/files/.tracks/06. Spiritual Awakening Bundle/Remove Negative Energy.mp3
                    //for preloaded tracks
                    Log.e("DIRRRRRRRR", getSaveDir(requireContext(), t, Companion.album!!))
                    val file = File(getSaveDir(requireContext(), t, album))
                    val preloaded = File(getPreloadedSaveDir(requireContext(), t, album))

                    if (!file.exists() && !preloaded.exists()) {
                        track?.let { tracks.add(it) }
                    }

                }

                downloadedTracks = tracks

                CoroutineScope(Dispatchers.Main).launch {
                    startActivity(DownloaderActivity.newIntent(requireContext(), tracks))
                }
            }
        } else {
            play(album)
            EventBus.getDefault().post(PlayerSelected(0))
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun play(album: Album) {
        val activity = activity as NavigationActivity

        if (isPlayProgram || playAlbumId != album.id) { activity.hidePlayerUI() }

        isPlayAlbum = true
        playAlbumId = album.id
        isPlayProgram = false
        playProgramId = -1

        val data: ArrayList<MusicRepository.Track> = ArrayList()
        val local = album.tracks
        val db = DataBase.getInstance(requireContext())

        GlobalScope.launch {
            local.forEach {
                 
                val file = File(getSaveDir(requireContext(), it, album))
                val preloaded = File(getPreloadedSaveDir(requireContext(), it, album))

                var uri: Uri? = null

                if (file.exists()) { uri = Uri.fromFile(file) }

                if (preloaded.exists()) { uri = Uri.fromFile(preloaded) }

              //  val track = db.trackDao().getTrackById(it.id)
              //  if (track?.duration == 0.toLong()) { track.duration = getDuration(file) }
              //  val multiplay = track?.duration!! / 300000

                val track = db.trackDao().getTrackById(it.id)
               // if (track?.duration == 0.toLong()) { track.duration = 300000 }
               // if (track?.duration == 0.toLong()) { track.duration = getDuration(file) }
                Log.e("DURATION",getDuration(file).toString())
                val multiplay = track?.duration!! / 300000
                data.add(MusicRepository.Track(it.name, album.name, album, R.drawable.launcher, uri!!, getDuration(file), 0, multiplay.toInt()))

            }

            trackList = data

            CoroutineScope(Dispatchers.Main).launch { activity.showPlayerUI() }
        }
    }

    private fun getDuration(file: File): Long {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(file.absolutePath)
        val durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return durationStr!!.toLong()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            albumIdBackProgram = albumId
            isTrackAdd = true

            parentFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.trans_right_to_left_in, R.anim.trans_right_to_left_out, R.anim.trans_left_to_right_in, R.anim.trans_left_to_right_out)
                    .replace(R.id.nav_host_fragment, NewProgramFragment(), NewProgramFragment().javaClass.simpleName)
                    .commit()
        }
    }

    companion object {
        const val ARG_ALBUM_ID = "arg_album"
        var album: Album? = null
        @JvmStatic
        fun newInstance(id: Int) = NewAlbumDetailFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_ALBUM_ID, id)
            }
        }
    }
}