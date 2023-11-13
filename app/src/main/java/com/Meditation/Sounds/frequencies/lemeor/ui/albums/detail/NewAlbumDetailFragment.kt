package com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.Meditation.Sounds.frequencies.lemeor.data.model.Rife
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.DownloaderActivity
import com.Meditation.Sounds.frequencies.lemeor.tools.player.MusicRepository
import com.Meditation.Sounds.frequencies.lemeor.tools.player.PlayerSelected
import com.Meditation.Sounds.frequencies.lemeor.tools.player.PlayerService
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs.TiersPagerFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.main.NavigationActivity
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.NewProgramFragment
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.Utils
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_new_album_detail.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.io.File


class NewAlbumDetailFragment : Fragment() {

    private val albumId: Int by lazy {
        arguments?.getInt(ARG_ALBUM_ID)
            ?: throw IllegalArgumentException("Must call through newInstance()")
    }

    private val categoryId: Int by lazy {
        arguments?.getInt(ARG_CATEGORY_ID)
            ?: throw IllegalArgumentException("Must call through newInstance()")
    }

    private val type: String by lazy {
        arguments?.getString(ARG_TYPE)
            ?: throw IllegalArgumentException("Must call through newInstance()")
    }

    var mRife: Rife? = null

    private lateinit var mViewModel: NewAlbumDetailViewModel
    private var mDescriptionAdapter: DescriptionAdapter? = null
    private var mTrackAdapter: AlbumTrackAdapter? = null

    private var mRifeAdapter: RifeAdapter? = null

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private var trackDao: TrackDao? = null
    private var albumDao: AlbumDao? = null
    private var isFirst = true

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_album_detail, container, false)
    }

    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        firebaseAnalytics = Firebase.analytics
        initUI()
        if (type == Constants.TYPE_ALBUM) {
            mViewModel.album(albumId, categoryId)?.observe(viewLifecycleOwner) {
                if (it != null) {
                    album = it
                    setUI(it)
                }
            }
        } else if (type == Constants.TYPE_RIFE) {
            if (mRife != null) {
                setUI(mRife!!)
            }
        }


        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener { _, keyCode, event ->
            if (event != null && event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                onBackPressed()
                true
            } else false
        }
    }

    private fun onBackPressed() {
        tierPositionSelected = tierPosition
        var fragment = selectedNaviFragment
        if (fragment == null) {
            fragment = TiersPagerFragment()
        }

        parentFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.trans_left_to_right_in,
            R.anim.trans_left_to_right_out,
            R.anim.trans_right_to_left_in,
            R.anim.trans_right_to_left_out
        ).replace(R.id.nav_host_fragment, fragment, fragment.javaClass.simpleName).commit()
    }

    private fun initUI() {
        mViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        ).get(NewAlbumDetailViewModel::class.java)
        if (type == Constants.TYPE_ALBUM) {
            trackDao = DataBase.getInstance(requireContext()).trackDao()
            albumDao = DataBase.getInstance(requireContext()).albumDao()
        }
    }

    private fun setUI(album: Album) {
        currentTrackIndex.observe(viewLifecycleOwner) {
            album.tracks.forEachIndexed { index, _ ->
                if (index == it && playAlbumId == albumId) {
                    mTrackAdapter?.setSelected(index)
                }
            }
        }

        album_back.setOnClickListener { onBackPressed() }

        album_image.radius = resources.getDimensionPixelOffset(R.dimen.corner_radius_album)

        loadImage(requireContext(), album_image, album)

        mDescriptionAdapter = album.descriptions?.let { DescriptionAdapter(requireContext(), it) }
        album_description_recycler.adapter = mDescriptionAdapter

        album_play.setOnClickListener {
            if (album.tracks.isNotEmpty()) {
                playAndDownload(album)
            }
        }

        mTrackAdapter = AlbumTrackAdapter(requireContext(), album.tracks, album)

        mTrackAdapter?.setOnClickListener(object : AlbumTrackAdapter.Listener {
            override fun onTrackClick(track: Track, i: Int, isDownloaded: Boolean) {
                isMultiPlay = false
                mTrackAdapter?.setSelected(i)
                play(album)
                Handler(Looper.getMainLooper()).postDelayed({
                    EventBus.getDefault().post(PlayerSelected(i))
                }, 200)
            }

            override fun onTrackOptions(track: Track, i: Int) {
                startActivityForResult(
                    TrackOptionsPopUpActivity.newIntent(
                        requireContext(), track.id.toDouble()
                    ), 1001
                )
            }
        })
        album_tracks_recycler.adapter = mTrackAdapter

        if (currentTrack.value != null) {
            val track = currentTrack.value
            if (track is MusicRepository.Track) {
                val indexSelected = album.tracks.indexOfFirst { it.id == track.trackId }
                if (indexSelected >= 0) {
                    mTrackAdapter?.setSelected(indexSelected)
                }
            }
        }

    }

    private fun setUI(rife: Rife) {

        currentTrackIndex.observe(viewLifecycleOwner) {
            rife.getFrequency().forEachIndexed { index, _ ->
                if (index == it && playAlbumId == rife.id) {
                    mRifeAdapter?.setSelected(index)
                }
            }
        }

        album_back.setOnClickListener { onBackPressed() }

        album_image.radius = resources.getDimensionPixelOffset(R.dimen.corner_radius_album)

//        loadImage(requireContext(), album_image, )

        mDescriptionAdapter =
            DescriptionAdapter(requireContext(), arrayListOf(rife.title))
        album_description_recycler.adapter = mDescriptionAdapter

        album_play.setOnClickListener {
            if (rife.getFrequency().isNotEmpty()) {
                play(rife)
            }
        }
        val local = rife.getFrequency().mapIndexed { index, s ->
            MusicRepository.Frequency(
                index,
                rife.title,
                s.toFloat(),
                rife.id,
                index,
                false,
                0,
                0,
            )
        }
        mRifeAdapter = RifeAdapter(requireContext(), local, listener = { frequency, index, option ->
            if (option == 0) {
                isMultiPlay = false
                mRifeAdapter?.setSelected(index)
                play(rife)
                Handler(Looper.getMainLooper()).postDelayed({
                    EventBus.getDefault().post(PlayerSelected(index))
                }, 200)
            } else if (option == 1) {
                startActivityForResult(
                    TrackOptionsPopUpActivity.newIntent(
                        requireContext(), -frequency.frequency.toDouble(),
                        rife = mRife
                    ), 1001
                )
            }
        })
        album_tracks_recycler.adapter = mRifeAdapter

        if (currentTrack.value != null) {
            val item = currentTrack.value
            if (item is MusicRepository.Frequency) {
                val indexSelected = item.index
                if (indexSelected >= 0 && playAlbumId == rife.id) {
                    mRifeAdapter?.setSelected(indexSelected)
                }
            }
        }

    }

    private fun playAndDownload(album: Album) {
        firebaseAnalytics.logEvent("Downloads") {
            param("Album Id", album.id.toString())
            param("Album Name", album.name)
            // param(FirebaseAnalytics.Param.CONTENT_TYPE, "image")
        }
        if (Utils.isConnectedToNetwork(requireContext())) {

            val tracks = ArrayList<Track>()
            val trackDao = DataBase.getInstance(requireContext()).trackDao()

            CoroutineScope(Dispatchers.IO).launch {
                album.tracks.forEach { t ->
                    val file = File(getSaveDir(requireContext(), t.filename, album.audio_folder))
                    val preloaded =
                        File(getPreloadedSaveDir(requireContext(), t.filename, album.audio_folder))

                    if (!file.exists() && !preloaded.exists()) {
                        trackDao.isTrackDownloaded(true, t.id)
                        t.isDownloaded = false
                        tracks.add(t)
                    }
                    t.album = Album(
                        album.index,
                        album.id, album.category_id,
                        album.tier_id,
                        album.name,
                        album.image,
                        album.audio_folder,
                        album.is_free,
                        album.order,
                        album.order_by,
                        album.updated_at, null, listOf(), null,
                        isDownloaded = false, isUnlocked = false,
                    )

                }

                CoroutineScope(Dispatchers.Main).launch {
                    activity?.let {
                        DownloaderActivity.startDownload(it, tracks)
                    }
                }

            }
        }
        play(album)
        EventBus.getDefault().post(PlayerSelected(0))
    }

    fun play(album: Album) {
        val activity = activity as NavigationActivity

        if (isPlayProgram || playAlbumId != album.id) {
            activity.hidePlayerUI()
        }

        isPlayAlbum = true
        playAlbumId = album.id
        isPlayProgram = false
        playProgramId = -1

        val data: ArrayList<MusicRepository.Track> = ArrayList()
        val local = album.tracks
        val db = DataBase.getInstance(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            local.forEach {
                try {
                    val track = db.trackDao().getTrackById(it.id)
                    if (track != null) {
                        data.add(
                            MusicRepository.Track(
                                it.id,
                                it.name,
                                album.name,
                                album.id,
                                album,
                                R.drawable.launcher,
                                track.duration,
                                0,
                                it.filename
                            )
                        )
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
            if (isFirst) {
                val mIntent = Intent(requireContext(), PlayerService::class.java).apply {
                    putParcelableArrayListExtra("playlist", data)
                }
                requireActivity().startService(mIntent)
                isFirst = false
            }
            CoroutineScope(Dispatchers.Main).launch { activity.showPlayerUI() }
        }
    }

    fun play(rife: Rife) {
        val activity = activity as NavigationActivity

        if (isPlayProgram || playAlbumId != rife.id) {
            activity.hidePlayerUI()
        }

        isPlayAlbum = true
        playAlbumId = rife.id
        isPlayProgram = false
        playProgramId = -1

        val data: ArrayList<MusicRepository.Frequency> = ArrayList()
        val local = rife.getFrequency()

        CoroutineScope(Dispatchers.IO).launch {
            local.forEachIndexed { index, s ->
                try {
                    data.add(
                        MusicRepository.Frequency(
                            index,
                            rife.title,
                            s.toFloat(),
                            rife.id,
                            index,
                            false,
                            0,
                            0,
                        )
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
            if (isFirst) {
                val mIntent = Intent(requireContext(), PlayerService::class.java).apply {
                    putParcelableArrayListExtra("playlist", data)
                }
                requireActivity().startService(mIntent)
                isFirst = false
            }

            CoroutineScope(Dispatchers.Main).launch { activity.showPlayerUI() }
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            typeBack= type
            rifeBackProgram = mRife
            albumIdBackProgram = albumId
            categoryIdBackProgram = categoryId
            isTrackAdd = true

            parentFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.trans_right_to_left_in,
                R.anim.trans_right_to_left_out,
                R.anim.trans_left_to_right_in,
                R.anim.trans_left_to_right_out
            ).replace(
                R.id.nav_host_fragment,
                NewProgramFragment(),
                NewProgramFragment().javaClass.simpleName
            ).commit()
        }
    }

    companion object {
        const val ARG_ALBUM_ID = "arg_album"
        const val ARG_CATEGORY_ID = "arg_category"
        const val ARG_TYPE = "arg_type"
        var album: Album? = null

        @JvmStatic
        fun newInstance(
            id: Int,
            category_id: Int,
            type: String = Constants.TYPE_ALBUM,
            item: Rife? = null
        ) = NewAlbumDetailFragment().apply {
            mRife = item
            arguments = Bundle().apply {
                putInt(ARG_ALBUM_ID, id)
                putInt(ARG_CATEGORY_ID, category_id)
                putString(ARG_TYPE, type)
            }
        }
    }
}