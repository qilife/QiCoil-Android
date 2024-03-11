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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.albumIdBackProgram
import com.Meditation.Sounds.frequencies.lemeor.categoryIdBackProgram
import com.Meditation.Sounds.frequencies.lemeor.convertSecondsToTime
import com.Meditation.Sounds.frequencies.lemeor.currentTrack
import com.Meditation.Sounds.frequencies.lemeor.currentTrackIndex
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Rife
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.getPreloadedSaveDir
import com.Meditation.Sounds.frequencies.lemeor.getSaveDir
import com.Meditation.Sounds.frequencies.lemeor.isMultiPlay
import com.Meditation.Sounds.frequencies.lemeor.isPlayAlbum
import com.Meditation.Sounds.frequencies.lemeor.isPlayProgram
import com.Meditation.Sounds.frequencies.lemeor.isTrackAdd
import com.Meditation.Sounds.frequencies.lemeor.loadImage
import com.Meditation.Sounds.frequencies.lemeor.playAlbumId
import com.Meditation.Sounds.frequencies.lemeor.playProgramId
import com.Meditation.Sounds.frequencies.lemeor.playRife
import com.Meditation.Sounds.frequencies.lemeor.playtimeRife
import com.Meditation.Sounds.frequencies.lemeor.rifeBackProgram
import com.Meditation.Sounds.frequencies.lemeor.selectedNaviFragment
import com.Meditation.Sounds.frequencies.lemeor.tierPosition
import com.Meditation.Sounds.frequencies.lemeor.tierPositionSelected
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.DownloaderActivity
import com.Meditation.Sounds.frequencies.lemeor.tools.player.MusicRepository
import com.Meditation.Sounds.frequencies.lemeor.tools.player.PlayerSelected
import com.Meditation.Sounds.frequencies.lemeor.tools.player.PlayerService
import com.Meditation.Sounds.frequencies.lemeor.trackList
import com.Meditation.Sounds.frequencies.lemeor.typeBack
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs.TiersPagerFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.main.NavigationActivity
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.NewProgramFragment
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.utils.firstIndexOrNull
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_new_album_detail.album_back
import kotlinx.android.synthetic.main.fragment_new_album_detail.album_image
import kotlinx.android.synthetic.main.fragment_new_album_detail.album_play
import kotlinx.android.synthetic.main.fragment_new_album_detail.album_tracks_recycler
import kotlinx.android.synthetic.main.fragment_new_album_detail.program_time
import kotlinx.android.synthetic.main.fragment_new_album_detail.tvDescription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import kotlin.math.abs


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

    private lateinit var mViewModel: NewAlbumDetailViewModel
    private var mAlbum: Album? = null
    private var mRife: Rife? = null

    private val trackAdapter by lazy {
        AlbumTrackAdapter(onClickItem = { _, pos, _ ->
            isMultiPlay = false
            mAlbum?.play()
            Handler(Looper.getMainLooper()).postDelayed({
                EventBus.getDefault().post(PlayerSelected(pos))
                timeDelay = 200L
            }, timeDelay)
        }, onClickOptions = { t, _ ->
            startActivityForResult(
                TrackOptionsPopUpActivity.newIntent(
                    requireContext(), t.id.toDouble()
                ), 1001
            )
        })
    }

    private val rifeAdapter by lazy {
        RifeAdapter(onClickItem = { f, pos ->
            if (-f.frequency.toDouble() >= Constants.defaultHz) {
                isMultiPlay = false
                mRife?.play()
                Handler(Looper.getMainLooper()).postDelayed({
                    EventBus.getDefault().post(PlayerSelected(pos))
                    timeDelay = 200L
                }, timeDelay)
            }
        }, onClickOptions = { f, _ ->
            if (-f.frequency.toDouble() >= Constants.defaultHz) {
                startActivityForResult(
                    TrackOptionsPopUpActivity.newIntent(
                        requireContext(), -f.frequency.toDouble(), rife = mRife
                    ), 1001
                )
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_hz_exceeded, abs(Constants.defaultHz).toString()),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private var isFirst = true
    private var timeDelay = 500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_album_detail, container, false)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: Any?) {
        event?.let { ev ->
            if (ev is Rife) {
                mRife?.let { m ->
                    if (ev.id == m.id) {
                        program_time.text =
                            getString(R.string.total_time, convertSecondsToTime(ev.playtime))
                    }
                }
            }
        }
    }

    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        firebaseAnalytics = Firebase.analytics
        initUI()
        program_time.visibility = View.GONE
        if (type == Constants.TYPE_ALBUM) {
            album_tracks_recycler.adapter = trackAdapter
            mViewModel.album(albumId, categoryId)?.observe(viewLifecycleOwner) { a ->
                if (a != null) {
                    mAlbum = a
                    a.initView()
                }
            }
        } else if (type == Constants.TYPE_RIFE) {
            album_tracks_recycler.adapter = rifeAdapter
            mRife?.let { r ->
                program_time.visibility = View.VISIBLE
                if (playRife != null) {
                    if (playRife!!.id == r.id && playtimeRife > 0L) {
                        program_time.text = getString(
                            R.string.total_time, convertSecondsToTime(playtimeRife)
                        )
                    } else {
                        program_time.text = getString(
                            R.string.total_time,
                            convertSecondsToTime((mRife!!.getFrequency().size * 3 * 60).toLong())
                        )
                    }
                } else {
                    program_time.text = getString(
                        R.string.total_time,
                        convertSecondsToTime((mRife!!.getFrequency().size * 3 * 60).toLong())
                    )
                }
                r.initView()
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
        )[NewAlbumDetailViewModel::class.java]
    }

    private fun Album.initView() {
        currentTrackIndex.observe(viewLifecycleOwner) {
            val track =
                tracks.firstIndexOrNull { index, _ -> index == it && playAlbumId == albumId }
            track?.let { item ->
                trackAdapter.setSelectedItem(item)
            }
        }
        trackAdapter.submitList(tracks)
        tvDescription.text = benefits_text
        album_back.setOnClickListener { onBackPressed() }

        album_image.radius = resources.getDimensionPixelOffset(R.dimen.corner_radius_album)

        loadImage(requireContext(), album_image, this)

        album_play.setOnClickListener {
            if (tracks.isNotEmpty()) {
                playAndDownload(this)
            }
        }

        if (currentTrack.value != null) {
            val track = currentTrack.value
            if (track is MusicRepository.Track) {
                tracks.firstOrNull { it.id == track.trackId }?.let {
                    trackAdapter.setSelectedItem(it)
                }
            }
        }
    }

    private fun Rife.initView() {
        val local = getFrequency().mapIndexed { index, s ->
            MusicRepository.Frequency(
                index,
                title,
                s,
                id,
                index,
                false,
                0,
                0,
            )
        }
        currentTrackIndex.observe(viewLifecycleOwner) {
            val frequency = local.firstIndexOrNull { index, _ -> index == it && playAlbumId == id }
            frequency?.let { item ->
                rifeAdapter.setSelectedItem(item)
            }
        }
        rifeAdapter.submitList(local)
        album_back.setOnClickListener { onBackPressed() }
        album_image.radius = resources.getDimensionPixelOffset(R.dimen.corner_radius_album)
        album_image.setImageResource(R.drawable.frequency)
        tvDescription.text = description
        album_play.setOnClickListener {
            if (getFrequency().isNotEmpty()) {
                play()
            }
        }
        if (currentTrack.value != null) {
            val item = currentTrack.value
            if (item is MusicRepository.Frequency) {
                val indexSelected = item.index
                if (indexSelected >= 0 && playAlbumId == id) {
                    rifeAdapter.setSelectedItem(item)
                }
            }
        }

    }

    private fun playAndDownload(album: Album) {
        playRife = null
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
                        album.id,
                        album.category_id,
                        album.tier_id,
                        album.name,
                        album.image,
                        album.audio_folder,
                        album.is_free,
                        album.order,
                        album.order_by,
                        album.updated_at,
                        null,
                        listOf(),
                        null,
                        isDownloaded = false,
                        isUnlocked = false,
                        album.unlock_url,
                        album.benefits_text
                    )

                }

                CoroutineScope(Dispatchers.Main).launch {
                    activity?.let {
                        DownloaderActivity.startDownload(it, tracks)
                    }
                }

            }
        } else {
            Toast.makeText(
                requireContext(), getString(R.string.err_network_available), Toast.LENGTH_SHORT
            ).show()
        }
        album.play()
        EventBus.getDefault().post(PlayerSelected(0))
    }

    @Suppress("UNCHECKED_CAST")
    fun Album.play() {
        playRife = null
        val activity = activity as NavigationActivity

        if (isPlayProgram || playAlbumId != id) {
            activity.hidePlayerUI()
        }

        isPlayAlbum = true
        playAlbumId = id
        isPlayProgram = false
        playProgramId = -1

        CoroutineScope(Dispatchers.IO).launch {
            val data = tracks.mapIndexedNotNull { _, t ->
                try {
                    MusicRepository.Track(
                        t.id,
                        t.name,
                        name,
                        id,
                        this@play,
                        R.drawable.launcher,
                        t.duration,
                        0,
                        t.filename
                    )
                } catch (ex: Exception) {
                    null
                }
            } as ArrayList<MusicRepository.Music>
            if (isFirst) {
                trackList?.clear()
                trackList = data
                val mIntent = Intent(requireContext(), PlayerService::class.java).apply {
                    putParcelableArrayListExtra("playlist", arrayListOf<MusicRepository.Music>())
                }
                requireActivity().stopService(mIntent)
                requireActivity().startService(mIntent)
                isFirst = false
            }
            CoroutineScope(Dispatchers.Main).launch { activity.showPlayerUI() }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun Rife.play() {
        if (playRife != null) {
            if (playRife!!.id != id) {
                playRife = this
            }
        } else {
            playRife = this
        }
        val activity = activity as NavigationActivity

        if (isPlayProgram || playAlbumId != id) {
            activity.hidePlayerUI()
        }

        isPlayAlbum = true
        playAlbumId = id
        isPlayProgram = false
        playProgramId = -1

        CoroutineScope(Dispatchers.IO).launch {
            val data = getFrequency().mapIndexedNotNull { index, s ->
                try {
                    MusicRepository.Frequency(
                        index,
                        title,
                        s,
                        id,
                        index,
                        false,
                        0,
                        0,
                    )
                } catch (ex: Exception) {
                    null
                }
            } as ArrayList<MusicRepository.Music>
            if (isFirst) {
                trackList?.clear()
                trackList = data
                val mIntent = Intent(requireContext(), PlayerService::class.java).apply {
                    putParcelableArrayListExtra("playlist", arrayListOf<MusicRepository.Music>())
                }
                requireActivity().stopService(mIntent)
                requireActivity().startService(mIntent)
                isFirst = false
            }
            withContext(Dispatchers.Main) {
                activity.showPlayerUI()
            }
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            typeBack = type
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

        @JvmStatic
        fun newInstance(
            id: Int, categoryId: Int, type: String = Constants.TYPE_ALBUM, item: Rife? = null
        ) = NewAlbumDetailFragment().apply {
            mRife = item
            arguments = Bundle().apply {
                putInt(ARG_ALBUM_ID, id)
                putInt(ARG_CATEGORY_ID, categoryId)
                putString(ARG_TYPE, type)
            }
        }
    }
}
