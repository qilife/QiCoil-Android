package com.Meditation.Sounds.frequencies.lemeor.ui.programs.detail

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.lemeor.FAVORITES
import com.Meditation.Sounds.frequencies.lemeor.albumIdBackProgram
import com.Meditation.Sounds.frequencies.lemeor.categoryIdBackProgram
import com.Meditation.Sounds.frequencies.lemeor.currentTrack
import com.Meditation.Sounds.frequencies.lemeor.currentTrackIndex
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.database.dao.ProgramDao
import com.Meditation.Sounds.frequencies.lemeor.data.model.Program
import com.Meditation.Sounds.frequencies.lemeor.data.model.Search
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.getConvertedTime
import com.Meditation.Sounds.frequencies.lemeor.getPreloadedSaveDir
import com.Meditation.Sounds.frequencies.lemeor.getSaveDir
import com.Meditation.Sounds.frequencies.lemeor.isMultiPlay
import com.Meditation.Sounds.frequencies.lemeor.isPlayAlbum
import com.Meditation.Sounds.frequencies.lemeor.isPlayProgram
import com.Meditation.Sounds.frequencies.lemeor.isTrackAdd
import com.Meditation.Sounds.frequencies.lemeor.playAlbumId
import com.Meditation.Sounds.frequencies.lemeor.playProgramId
import com.Meditation.Sounds.frequencies.lemeor.playRife
import com.Meditation.Sounds.frequencies.lemeor.positionFor
import com.Meditation.Sounds.frequencies.lemeor.rifeBackProgram
import com.Meditation.Sounds.frequencies.lemeor.selectedNaviFragment
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.DownloadService
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.DownloaderActivity
import com.Meditation.Sounds.frequencies.lemeor.tools.player.MusicRepository
import com.Meditation.Sounds.frequencies.lemeor.tools.player.PlayerSelected
import com.Meditation.Sounds.frequencies.lemeor.tools.player.PlayerService
import com.Meditation.Sounds.frequencies.lemeor.trackList
import com.Meditation.Sounds.frequencies.lemeor.typeBack
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail.NewAlbumDetailFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.main.NavigationActivity
import com.Meditation.Sounds.frequencies.lemeor.ui.main.UpdateTrack
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.NewProgramFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.NewProgramViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.dialog.FrequenciesDialogFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.search.AddProgramsFragment
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.utils.isNotString
import com.Meditation.Sounds.frequencies.views.ItemLastOffsetBottomDecoration
import kotlinx.android.synthetic.main.fragment_program_detail.action_frequencies
import kotlinx.android.synthetic.main.fragment_program_detail.action_quantum
import kotlinx.android.synthetic.main.fragment_program_detail.action_rife
import kotlinx.android.synthetic.main.fragment_program_detail.fabOption
import kotlinx.android.synthetic.main.fragment_program_detail.program_back
import kotlinx.android.synthetic.main.fragment_program_detail.program_name
import kotlinx.android.synthetic.main.fragment_program_detail.program_play
import kotlinx.android.synthetic.main.fragment_program_detail.program_time
import kotlinx.android.synthetic.main.fragment_program_detail.program_tracks_recycler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.Collections


class ProgramDetailFragment : BaseFragment() {

    private val programId: Int by lazy {
        arguments?.getInt(ARG_PROGRAM_ID)
            ?: throw IllegalArgumentException("Must call through newInstance()")
    }

    private lateinit var mViewModel: ProgramDetailViewModel
    private lateinit var mNewProgramViewModel: NewProgramViewModel
    private var mTracks: ArrayList<Any>? = null
    private var program: Program? = null
    private var isFirst = true
    private var timeDelay = 500L
    private val tracks: ArrayList<Search> = ArrayList()

    private val programTrackAdapter by lazy {
        ProgramTrackAdapter(
            onClickItem = { item ->
                isMultiPlay = false
                play(tracks.map { it.obj } as ArrayList<Any>)
                Handler(Looper.getMainLooper()).postDelayed({
                    EventBus.getDefault().post(PlayerSelected(item.id))
                    timeDelay = 200L
                }, timeDelay)
            },
            onCLickOptions = { item ->
                positionFor = item.id
                if (item.obj is Track) {
                    val t = item.obj as Track
                    startActivityForResult(
                        PopActivity.newIntent(
                            requireContext(), t.id.toDouble()
                        ), 1002
                    )
                } else if (item.obj is MusicRepository.Frequency) {
                    val f = item.obj as MusicRepository.Frequency
                    startActivityForResult(
                        PopActivity.newIntent(
                            requireContext(), f.frequency.toDouble()
                        ), 1002
                    )
                }

            },
        )
    }
    private val itemDecoration by lazy {
        ItemLastOffsetBottomDecoration(resources.getDimensionPixelOffset(R.dimen.dp_70))
    }

    override fun initLayout() = R.layout.fragment_program_detail

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: Any?) {
        if (event == DownloadService.DOWNLOAD_FINISH) {
            program?.let { p ->
                try {
                    mViewModel.convertData(p) { list ->
                        mTracks?.clear()
                        mTracks?.addAll(list.map { it.obj })
                        program_play.text = getString(R.string.btn_play)
                    }
                } catch (_: Exception) {
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun initComponents() {
        mViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        )[ProgramDetailViewModel::class.java]

        mNewProgramViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        )[NewProgramViewModel::class.java]

        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                onBackPressed()
                true
            } else false
        }
    }

    override fun addListener() {
        program_back.setOnClickListener { onBackPressed() }

        program_play.setOnClickListener {
            if (tracks.size == 0) {
                Toast.makeText(
                    requireContext(), getString(R.string.tv_empty_list), Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            val list = tracks.filterIsInstance<Track>() as ArrayList<Track>
            if (list.isNotEmpty()) {
                playOrDownload(list)
            }
            play(tracks.map { it.obj } as ArrayList<Any>)
            programTrackAdapter.setSelectedItem(tracks.first())
            EventBus.getDefault().post(PlayerSelected(0))
        }

        action_rife.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.trans_right_to_left_in, R.anim.trans_right_to_left_out)
                .replace(
                    R.id.nav_host_fragment,
                    AddProgramsFragment.newInstance(programId, 1),
                    AddProgramsFragment.newInstance(programId).javaClass.simpleName
                ).commit()
        }

        action_quantum.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.trans_right_to_left_in, R.anim.trans_right_to_left_out)
                .replace(
                    R.id.nav_host_fragment,
                    AddProgramsFragment.newInstance(programId),
                    AddProgramsFragment.newInstance(programId).javaClass.simpleName
                ).commit()
        }

        action_frequencies.setOnClickListener {
            fabOption.collapse()
            FrequenciesDialogFragment.newInstance(listener = { f, m ->
                mNewProgramViewModel.addFrequencyToProgram(programId, f)
                m.dismiss()
            }).showAllowingStateLoss(childFragmentManager)
        }

        mViewModel.program(programId).observe(viewLifecycleOwner) {
            if (it != null && it.id != 0) {
                programTrackAdapter.isMy = it.isMy
                program = it
                program_tracks_recycler.removeItemDecoration(itemDecoration)
                setUI(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
        isTrackAdd = false
        albumIdBackProgram = -1
        categoryIdBackProgram = -1
    }

    private fun onBackPressed() {
        var fragment: Fragment?

        if (isTrackAdd) {
            fragment = if (typeBack == Constants.TYPE_ALBUM) {
                albumIdBackProgram?.let {
                    NewAlbumDetailFragment.newInstance(
                        it, categoryIdBackProgram!!
                    )
                }
            } else {
                rifeBackProgram?.let {
                    NewAlbumDetailFragment.newInstance(
                        0, categoryIdBackProgram!!, type = typeBack, item = it
                    )
                }
            }
        } else {
            fragment = selectedNaviFragment
            if (fragment == null) {
                fragment = NewProgramFragment()
            }
        }

        parentFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.trans_left_to_right_in, R.anim.trans_left_to_right_out)
            .replace(R.id.nav_host_fragment, fragment!!, fragment.javaClass.simpleName).commit()
    }

    private fun setUI(program: Program) {
        program_name.text = program.name

        program_tracks_recycler.apply {
            adapter = programTrackAdapter
            addItemDecoration(itemDecoration)
        }
        mViewModel.convertData(program) { list ->
            tracks.clear()
            tracks.addAll(list)

            program_time.text = getString(
                R.string.total_time, getConvertedTime(tracks.sumOf {
                    if (it.obj is Track) 300000L
                    else 180000L
                })
            )
            mTracks?.clear()
            mTracks?.addAll(tracks.map { it.obj })
            programTrackAdapter.submitList(tracks)

            if (currentTrack.value != null) {
                val track = currentTrack.value
                if (track is MusicRepository.Track) {
                    tracks.firstOrNull {
                        (it.obj is Track) && it.id == track.trackId
                    }?.let {
                        programTrackAdapter.setSelectedItem(it)
                    }
                }
            }

            program_play.text = getString(R.string.btn_play)

            currentTrackIndex.observe(viewLifecycleOwner) {
                val t = tracks.firstOrNull { item -> item.id == it }
                t?.let { item ->
                    if (playProgramId == programId) {
                        programTrackAdapter.setSelectedItem(item)
                    }
                }
            }
        }
    }

    private fun playOrDownload(tracks: ArrayList<Track>) {
        if (Utils.isConnectedToNetwork(requireContext())) {
            val list = ArrayList<Track>()
            CoroutineScope(Dispatchers.IO).launch {
                tracks.forEach { t ->
                    val file =
                        File(getSaveDir(requireContext(), t.filename, t.album?.audio_folder ?: ""))
                    val preloaded = File(
                        getPreloadedSaveDir(
                            requireContext(), t.filename, t.album?.audio_folder ?: ""
                        )
                    )

                    if (!file.exists() && !preloaded.exists()) {
                        var isExist = false
                        for (item in list) {
                            if (item.id == t.id) {
                                isExist = true
                                break
                            }
                        }
                        if (!isExist) {
                            list.add(t)
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    activity?.let {
                        DownloaderActivity.startDownload(it, list)
                    }
                }
            }
        }
    }

    fun play(tracks: ArrayList<Any>) {
        playRife = null
        val activity = activity as NavigationActivity

        if (isPlayAlbum || playProgramId != programId) {
            activity.hidePlayerUI()
        }

        isPlayAlbum = false
        playAlbumId = -1
        isPlayProgram = true
        playProgramId = programId
        CoroutineScope(Dispatchers.IO).launch {
            val data = tracks.mapNotNull { t ->
                when (t) {
                    is Track -> {
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
                    }

                    is MusicRepository.Frequency -> {
                        t
                    }

                    else -> null
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
                val list = program?.records ?: arrayListOf()

                when {
                    action.equals("track_move_up") -> {
                        if (positionFor == 0) {
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.tv_track_first_pos),
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
                                    getString(R.string.tv_track_last_pos),
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
                                            false, it1
                                        )
                                    }
                                }
                            }
                        }
                        val trackId = list[positionFor!!]
                        list.removeAt(positionFor!!)
                        program?.records = list
                        program?.let {
                            programDao.updateProgram(it)
                            if (it.user_id.isNotEmpty()) {
                                try {
                                    mNewProgramViewModel.updateTrackToProgram(
                                        UpdateTrack(
                                            track_id = listOf(trackId),
                                            id = it.id,
                                            track_type = if (trackId.isNotString()) "mp3" else "rife",
                                            request_type = "remove",
                                            is_favorite = (it.name.uppercase() == FAVORITES.uppercase() && it.favorited)
                                        )
                                    )
                                } catch (_: Exception) {
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun moveTrack(
        list: MutableList<String>, isMoveUp: Boolean, programDao: ProgramDao
    ) {
        val positionFrom = positionFor!!
        val positionTo = if (isMoveUp) {
            positionFor!! - 1
        } else {
            positionFor!! + 1
        }
        Collections.swap(list, positionFrom, positionTo)
        program?.let {
            it.records = list as java.util.ArrayList<String>
            programDao.updateProgram(it)
        }
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