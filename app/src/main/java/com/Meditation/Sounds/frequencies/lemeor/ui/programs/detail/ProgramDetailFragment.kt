package com.Meditation.Sounds.frequencies.lemeor.ui.programs.detail

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
import com.Meditation.Sounds.frequencies.lemeor.*
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.database.dao.ProgramDao
import com.Meditation.Sounds.frequencies.lemeor.data.model.Program
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.DownloadService
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.DownloaderActivity
import com.Meditation.Sounds.frequencies.lemeor.tools.player.MusicRepository
import com.Meditation.Sounds.frequencies.lemeor.tools.player.PlayerSelected
import com.Meditation.Sounds.frequencies.lemeor.tools.player.PlayerService
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail.NewAlbumDetailFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.main.NavigationActivity
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.NewProgramFragment
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.Utils
import kotlinx.android.synthetic.main.fragment_program_detail.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.*


class ProgramDetailFragment : Fragment() {

    private val programId: Int by lazy {
        arguments?.getInt(ARG_PROGRAM_ID)
            ?: throw IllegalArgumentException("Must call through newInstance()")
    }

    private var mTracks: ArrayList<Any>? = null
    private var program: Program? = null
    private lateinit var mViewModel: ProgramDetailViewModel
    private var mTrackAdapter: ProgramTrackAdapter? = null
    private var isFirst = true
    private var timeDelay = 500L

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: Any?) {
        if (event == DownloadService.DOWNLOAD_FINISH) {
            val tracks: ArrayList<Any> = ArrayList()

            CoroutineScope(Dispatchers.IO).launch {
                program?.records?.forEachIndexed { index, it ->
                    if (it >= 0) {
                        mViewModel.getTrackById(it.toInt())?.let { track ->
                            tracks.add(track)
                        }
                    } else {
                        tracks.add(
                            MusicRepository.Frequency(
                                index,
                                "",
                                (it * -1).toFloat(),
                                -index,
                                index,
                                false,
                                0,
                                0,
                            )
                        )
                    }
                }

                CoroutineScope(Dispatchers.Main).launch {
                    mTracks = tracks
                    program_play.text = getString(R.string.btn_play)
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
        isTrackAdd = false
        albumIdBackProgram = -1
        categoryIdBackProgram = -1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_program_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initUI()

        mViewModel.program(programId)?.observe(viewLifecycleOwner) {
            program = it
            setUI(it)
        }

        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                onBackPressed()
                true
            } else false
        }
    }

    private fun initUI() {
        mViewModel = ViewModelProvider(
            this,
            ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        ).get(ProgramDetailViewModel::class.java)
    }

    private fun onBackPressed() {
        var fragment: Fragment?

        if (isTrackAdd) {
            if (typeBack == Constants.TYPE_ALBUM) {
                fragment = albumIdBackProgram?.let {
                    NewAlbumDetailFragment.newInstance(
                        it,
                        categoryIdBackProgram!!
                    )
                }
            } else {
                fragment = rifeBackProgram?.let {
                    NewAlbumDetailFragment.newInstance(
                        0,
                        categoryIdBackProgram!!,
                        type = typeBack,
                        item = it
                    )
                }
            }
        } else {
            fragment = selectedNaviFragment
            if (fragment == null) {
                fragment = NewProgramFragment()
            }
        }

        parentFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.trans_left_to_right_in, R.anim.trans_left_to_right_out)
            .replace(R.id.nav_host_fragment, fragment!!, fragment.javaClass.simpleName)
            .commit()
    }

    private fun setUI(program: Program) {

        val tracks: ArrayList<Any> = ArrayList()

        program_back.setOnClickListener { onBackPressed() }

        program_name.text = program.name

        program_play.setOnClickListener {
            if (tracks.size == 0) {
                Toast.makeText(requireContext(), "Empty List", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val list = tracks.filterIsInstance<Track>() as ArrayList<Track>
            if (list.isNotEmpty()) {
                playOrDownload(list)
            }
            play(tracks)
            EventBus.getDefault().post(PlayerSelected(0))
        }

        mTrackAdapter = ProgramTrackAdapter(requireContext(), tracks, program.isMy)
        mTrackAdapter?.setOnClickListener(object : ProgramTrackAdapter.Listener {
            override fun onTrackClick(track: Any, i: Int) {
                isMultiPlay = false
                mTrackAdapter?.setSelected(i)
                play(tracks)
                Handler(Looper.getMainLooper()).postDelayed({
                    EventBus.getDefault().post(PlayerSelected(i))
                    timeDelay = 200L
                }, timeDelay)
            }

            override fun onTrackOptions(track: Any, i: Int) {
                positionFor = i
                if (track is Track) {
                    startActivityForResult(PopActivity.newIntent(requireContext(), track.id.toDouble()), 1002)
                }else if(track is MusicRepository.Frequency) {
                    startActivityForResult(PopActivity.newIntent(requireContext(), track.frequency.toDouble()), 1002)
                }
            }
        })
        program_tracks_recycler.adapter = mTrackAdapter

        val dao = DataBase.getInstance(requireContext()).albumDao()
        CoroutineScope(Dispatchers.IO).launch {
            program.records.forEachIndexed { index, it ->
                if (it >= 0) {
                    mViewModel.getTrackById(it.toInt())?.let { track ->
                        tracks.add(track)
                    }
                } else {
                    tracks.add(
                        MusicRepository.Frequency(
                            index,
                            "",
                            (it * -1).toFloat(),
                            -index,
                            index,
                            false,
                            0,
                            0,
                        )
                    )
                }

            }
            tracks.filterIsInstance<Track>().forEach {
                val album = dao.getAlbumById(it.albumId, it.category_id)
                it.album = album
            }


            CoroutineScope(Dispatchers.Main).launch {
                program_time.text =
                    getString(
                        R.string.total_time,
                        getConvertedTime((tracks.size * 300000).toLong())
                    )
                mTracks = tracks
                mTrackAdapter?.setData(tracks)

                if (currentTrack.value != null) {
                    val track = currentTrack.value
                    if (track is MusicRepository.Track) {
                        val indexSelected = tracks.indexOfFirst {
                            (it is Track) && it.id == track.trackId
                        }
                        if (indexSelected >= 0) {
                            mTrackAdapter?.setSelected(indexSelected)
                        }
                    }
                }

                program_play.text = getString(R.string.btn_play)
            }
        }

        currentTrackIndex.observe(viewLifecycleOwner) {
            tracks.forEachIndexed { index, _ ->
                if (index == it && playProgramId == programId) {
                    mTrackAdapter?.setSelected(index)
                }
            }
        }
    }

    private fun playOrDownload(tracks: ArrayList<Track>) {
        if (Utils.isConnectedToNetwork(requireContext())) {

            val list = ArrayList<Track>()

            val dao = DataBase.getInstance(requireContext()).albumDao()
            CoroutineScope(Dispatchers.IO).launch {
                tracks.forEach { t ->
                    val album = dao.getAlbumById(t.albumId, t.category_id)
                    t.album = album

                    val file =
                        File(getSaveDir(requireContext(), t.filename, album?.audio_folder ?: ""))
                    val preloaded = File(
                        getPreloadedSaveDir(
                            requireContext(),
                            t.filename,
                            album?.audio_folder ?: ""
                        )
                    )

                    if (!file.exists() && !preloaded.exists()) {
                        var isExist = false
                        list.forEach { l ->
                            if (l.id == t.id) {
                                isExist = true
                            }
                        }
                        if (!isExist) {
                            list.add(t)
                        }
                    }
                }

                CoroutineScope(Dispatchers.Main).launch {
                    activity?.let {
                        DownloaderActivity.startDownload(it, list)
                    }
                }
            }
        }
    }

    fun play(tracks: ArrayList<Any>) {
        val activity = activity as NavigationActivity

        if (isPlayAlbum || playProgramId != programId) {
            activity.hidePlayerUI()
        }

        isPlayAlbum = false
        playAlbumId = -1
        isPlayProgram = true
        playProgramId = programId

        val data: ArrayList<MusicRepository.Music> = ArrayList()
        val db = DataBase.getInstance(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            tracks.forEach { t ->
//                val file =
//                    File(getSaveDir(requireContext(), t.filename, t.album?.audio_folder ?: ""))
//                val track = db.trackDao().getTrackById(t.id)
//                if (track?.duration == 0.toLong()) {
//                    track.duration = getDuration(file)
//                }
                if (t is Track) {
                    data.add(
                        MusicRepository.Track(
                            t.id,
                            t.name,
                            t.album?.name!!,
                            t.albumId,
                            t.album!!,
                            R.drawable.launcher,
                            t.duration,
                            0,
                            t.filename
                        )
                    )
                } else if (t is MusicRepository.Frequency) {
                    data.add(t)
                }

            }
            if (isFirst) {
                val mIntent = Intent(requireContext(), PlayerService::class.java).apply {
                    putParcelableArrayListExtra("playlist", data)
                }
                requireActivity().stopService(mIntent)
                requireActivity().startService(mIntent)
                isFirst = false
            }
            trackList = data
            rifeList = null
            CoroutineScope(Dispatchers.Main).launch {
                activity.showPlayerUI()
            }
        }
    }

//    private fun getDuration(file: File): Long {
//        val mediaMetadataRetriever = MediaMetadataRetriever()
//        mediaMetadataRetriever.setDataSource(file.absolutePath)
//        val durationStr =
//            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
//        return durationStr?.toLong() ?: 0L
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1002 && resultCode == RESULT_OK && data != null) {
            isFirst = true
            val action = data.getStringExtra(PopActivity.EXTRA_ACTION)

            val db = DataBase.getInstance(requireContext())
            val programDao = db.programDao()
            val trackDao = db.trackDao()

            val activity = activity as NavigationActivity
            isPlayAlbum = false
            isPlayProgram = false
            activity.hidePlayerUI()

            CoroutineScope(Dispatchers.IO).launch {
                val list = program?.records as MutableList<Double>

                when {
                    action.equals("track_move_up") -> {
                        if (positionFor == 0) {
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(
                                    requireContext(),
                                    "Track in first position",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            return@launch
                        }
                        moveTrack(list, true, programDao)
                    }

                    action.equals("track_move_down") -> {
                        if (positionFor == list.size - 1) {
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(
                                    requireContext(),
                                    "Track in last position",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            return@launch
                        }
                        moveTrack(list, false, programDao)
                    }

                    action.equals("track_remove") -> {
                        positionFor?.let {
                            mTracks?.get(it)?.let { item ->
                                if (item is Track) {
                                    item.id.let { it1 ->
                                        trackDao.isTrackFavorite(
                                            false,
                                            it1
                                        )
                                    }
                                } else if (item is MusicRepository.Frequency) {

                                }
                            }
                        }

                        list.removeAt(positionFor!!)
                        program?.records = list as ArrayList<Double>
                        program?.let { programDao.updateProgram(it) }
                    }
                }
            }
        }
    }

    private suspend fun moveTrack(
        list: MutableList<Double>,
        isMoveUp: Boolean,
        programDao: ProgramDao
    ) {
        val positionFrom = positionFor!!
        val positionTo = if (isMoveUp) {
            positionFor!! - 1
        } else {
            positionFor!! + 1
        }
        Collections.swap(list, positionFrom, positionTo)
        program?.records = list as ArrayList<Double>
        program?.let { programDao.updateProgram(it) }
    }

    companion object {
        const val ARG_PROGRAM_ID = "arg_program"

        @JvmStatic
        fun newInstance(id: Int) = ProgramDetailFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_PROGRAM_ID, id)
            }
        }
    }
}