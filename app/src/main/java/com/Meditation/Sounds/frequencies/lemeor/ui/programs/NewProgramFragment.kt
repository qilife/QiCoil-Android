package com.Meditation.Sounds.frequencies.lemeor.ui.programs

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.*
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Program
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail.NewAlbumDetailFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.main.UpdateTrack
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.detail.ProgramDetailFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.purchase.new_flow.NewPurchaseActivity
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.views.AlertMessageDialog
import kotlinx.android.synthetic.main.fragment_new_program.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class NewProgramFragment : Fragment() {

    private lateinit var mViewModel: NewProgramViewModel
    private var mProgramAdapter: ProgramAdapter = ProgramAdapter()
    private var mListProgram = ArrayList<Program>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_program, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        init()

        mViewModel.getPrograms(isTrackAdd).observe(viewLifecycleOwner) {
            CoroutineScope(Dispatchers.IO).launch {
                val data = checkUnlocked(it)
                withContext(Dispatchers.Main){
                    mProgramAdapter.setData(data)
                }
            }
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

    private suspend fun checkUnlocked(programList: List<Program>): List<Program> {
        //todo remake this(some freezing?)
        programList.forEach { program ->
            val tracks: ArrayList<Track> = ArrayList()

            program.records.forEach { r ->
                if (r >= 0) {
                    mViewModel.getTrackById(r.toInt())?.let { track -> tracks.add(track) }
                }
            }

            var isUnlocked = true

            tracks.forEach { t ->

                val temp_album = mViewModel.getAlbumById(t.albumId, t.category_id)

                if (temp_album?.isUnlocked == false) {
                    isUnlocked = false
                }
            }
            program.isUnlocked = isUnlocked
        }
        return programList
    }

    private fun onBackPressed() {
        parentFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.trans_left_to_right_in,
                R.anim.trans_left_to_right_out,
                R.anim.trans_right_to_left_in,
                R.anim.trans_right_to_left_out
            )
            .replace(
                R.id.nav_host_fragment,
                NewAlbumDetailFragment.newInstance(
                    albumIdBackProgram!!, categoryIdBackProgram!!,
                    typeBack, rifeBackProgram
                ),
                NewAlbumDetailFragment().javaClass.simpleName
            )
            .commit()
    }

    fun init() {
        if (isTrackAdd) {
            program_title.text = getString(R.string.txt_my_playlists)
            program_back.visibility = View.VISIBLE
        } else {
            program_title.text = getString(R.string.navigation_lbl_programs)
            program_back.visibility = View.INVISIBLE
        }

        program_back.setOnClickListener { onBackPressed() }

        mViewModel = ViewModelProvider(
            this,
            ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        ).get(NewProgramViewModel::class.java)

        program_create_new.setOnClickListener {
            // call api create program
            val dialogBuilder = AlertDialog.Builder(requireContext()).create()
            val inflater = this.layoutInflater
            val dialogView: View = inflater.inflate(R.layout.dialog_add_edit_playlist, null)
            dialogBuilder.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val programName = dialogView.findViewById<View>(R.id.edtPlayListName) as EditText
            val btnAdd: Button = dialogView.findViewById<View>(R.id.btnSubmit) as Button

            btnAdd.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    //call api createProgram
                    try {
                        val result =
                            withContext(Dispatchers.Default) {
                                mViewModel.createProgram(programName.text.toString())
                            }
                        val program = result.data
                        mViewModel.insert(program)
                    } catch (_: Exception) {
                        mViewModel.insert(
                            Program(
                                0,
                                programName.text.toString(),
                                "",
                                0,
                                Date().time,
                                ArrayList(),
                                isMy = true,
                                false,
                                is_dirty = false
                            )
                        )
                    }


                }
                dialogBuilder.dismiss()
            }

            dialogBuilder.setView(dialogView)
            dialogBuilder.show()
        }

        mProgramAdapter.setOnClickListener(object : ProgramAdapter.Listener {
            override fun onClickItem(program: Program, i: Int) {
                if (program.isUnlocked) {
                    if (isTrackAdd && trackIdForProgram != Constants.defaultHz - 1) {
                        val db = DataBase.getInstance(requireContext())
                        val programDao = db.programDao()

                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val p = programDao.getProgramById(program.id)
                                p?.records?.add((trackIdForProgram ?: 0.0).toDouble())
                                p?.let { it1 ->
                                    programDao.updateProgram(it1)
                                    if (it1.user_id.isNotEmpty()) {
                                        try {
                                            mViewModel.updateTrackToProgram(
                                                UpdateTrack(
                                                    track_id = listOf(
                                                        (trackIdForProgram ?: 0.0).toDouble()
                                                    ),
                                                    id = it1.id,
                                                    track_type = if ((trackIdForProgram
                                                            ?: 0.0).toDouble() >= 0
                                                    ) "mp3" else "rife",
                                                    request_type = "add",
                                                    is_favorite = (it1.name.uppercase() == FAVORITES.uppercase() && it1.favorited)
                                                )
                                            )
                                        } catch (_: Exception) {
                                        }
                                    }
                                }
                            } catch (_: Exception) {
                            }
                        }
                    }

                    parentFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(
                            R.anim.trans_right_to_left_in,
                            R.anim.trans_right_to_left_out,
                            R.anim.trans_left_to_right_in,
                            R.anim.trans_left_to_right_out
                        )
                        .replace(
                            R.id.nav_host_fragment,
                            ProgramDetailFragment.newInstance(program.id),
                            ProgramDetailFragment().javaClass.simpleName
                        )
                        .commit()
                } else {
                    var album: Album? = null
                    val tracks: ArrayList<Track> = ArrayList()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            program.records.forEach { r ->
                                if (r >= 0) {
                                    mViewModel.getTrackById(r.toInt())
                                        ?.let { track -> tracks.add(track) }
                                }
                            }
                            tracks.forEach { t ->
                                val temp_album = mViewModel.getAlbumById(t.albumId, t.category_id);
                                if (temp_album?.isUnlocked == false && album == null) {
                                    album = temp_album
                                    CoroutineScope(Dispatchers.Main).launch {
                                        startActivity(
                                            NewPurchaseActivity.newIntent(
                                                requireContext(),
                                                temp_album.category_id,
                                                temp_album.tier_id,
                                                temp_album.id
                                            )
                                        )
                                    }
                                }
                            }
                        } catch (_: Exception) {
                        }
                    }
                }
            }

            override fun onDeleteItem(program: Program, i: Int) {
                val alertDialog = AlertMessageDialog(
                    requireContext(),
                    object : AlertMessageDialog.IOnSubmitListener {
                        override fun submit() {
                            CoroutineScope(Dispatchers.IO).launch {
                                //call api delete program
                                try {
                                    mViewModel.deleteProgram(program.id.toString())
                                    mViewModel.delete(program)
                                } catch (_: Exception) {
                                    mViewModel.udpate(
                                        program.copy(
                                            deleted = true
                                        )
                                    )
                                }
                            }
                            Toast.makeText(
                                requireContext(),
                                requireContext().getString(R.string.txt_delete_playlist_name_success),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun cancel() {}
                    })
                alertDialog.show()
                alertDialog.setWarningMessage(getString(R.string.txt_warning_delete_playlist))
            }
        })
        programs_recycler_view.adapter = mProgramAdapter
    }
}