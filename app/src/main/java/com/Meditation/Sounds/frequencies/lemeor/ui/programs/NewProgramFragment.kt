package com.Meditation.Sounds.frequencies.lemeor.ui.programs

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.lemeor.FAVORITES
import com.Meditation.Sounds.frequencies.lemeor.albumIdBackProgram
import com.Meditation.Sounds.frequencies.lemeor.categoryIdBackProgram
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Program
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.isTrackAdd
import com.Meditation.Sounds.frequencies.lemeor.rifeBackProgram
import com.Meditation.Sounds.frequencies.lemeor.trackIdForProgram
import com.Meditation.Sounds.frequencies.lemeor.typeBack
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail.NewAlbumDetailFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.main.UpdateTrack
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.detail.ProgramDetailFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.purchase.new_flow.NewPurchaseActivity
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.isNotString
import com.Meditation.Sounds.frequencies.utils.loadImageWithGif
import com.Meditation.Sounds.frequencies.views.AlertMessageDialog
import kotlinx.android.synthetic.main.fragment_new_program.program_back
import kotlinx.android.synthetic.main.fragment_new_program.program_create_new
import kotlinx.android.synthetic.main.fragment_new_program.program_title
import kotlinx.android.synthetic.main.fragment_new_program.programs_recycler_view
import kotlinx.android.synthetic.main.fragment_program_page.ivImage
import kotlinx.android.synthetic.main.fragment_program_page.loadingFrame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class NewProgramFragment : BaseFragment() {

    private lateinit var mViewModel: NewProgramViewModel
    private var mProgramAdapter: ProgramAdapter = ProgramAdapter()
    private var mListProgram = ArrayList<Program>()
    override fun initLayout() = R.layout.fragment_new_program

    override fun initComponents() {
        init()
    }

    override fun addListener() {
        mViewModel.getPrograms(viewLifecycleOwner) {
            mProgramAdapter.setData(it)
            loadingFrame.visibility = View.GONE
        }

        program_back.setOnClickListener { onBackPressed() }

        program_create_new.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(requireContext()).create()
            val inflater = this.layoutInflater
            val dialogView: View = inflater.inflate(R.layout.dialog_add_edit_playlist, null)
            dialogBuilder.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val programName = dialogView.findViewById<View>(R.id.edtPlayListName) as EditText
            val btnAdd: Button = dialogView.findViewById<View>(R.id.btnSubmit) as Button

            btnAdd.setOnClickListener {
                if (programName.text.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        //call api createProgram
                        try {
                            val result = withContext(Dispatchers.Default) {
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
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.tv_error_playlist_name),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            dialogBuilder.setView(dialogView)
            dialogBuilder.show()
        }

        mProgramAdapter.setOnClickListener(object : ProgramAdapter.Listener {
            override fun onClickItem(program: Program, i: Int) {
                if (program.isUnlocked) {
                    if (isTrackAdd && trackIdForProgram != (Constants.defaultHz - 1).toString()) {
                        val db = DataBase.getInstance(requireContext())
                        val programDao = db.programDao()
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val programRoom = programDao.getProgramById(program.id)
                                programRoom?.let { p ->
                                    p.records.add(trackIdForProgram)
                                    programDao.updateProgram(p)
                                    if (p.user_id.isNotEmpty()) {
                                        try {
                                            mViewModel.updateTrackToProgram(
                                                UpdateTrack(
                                                    track_id = listOf(trackIdForProgram),
                                                    id = p.id,
                                                    track_type = if (trackIdForProgram.isNotString()) "mp3" else "rife",
                                                    request_type = "add",
                                                    is_favorite = (p.name.uppercase() == FAVORITES.uppercase() && p.favorited)
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

                    parentFragmentManager.beginTransaction().setCustomAnimations(
                        R.anim.trans_right_to_left_in,
                        R.anim.trans_right_to_left_out,
                        R.anim.trans_left_to_right_in,
                        R.anim.trans_left_to_right_out
                    ).replace(
                        R.id.nav_host_fragment,
                        ProgramDetailFragment.newInstance(program.id),
                        ProgramDetailFragment().javaClass.simpleName
                    ).commit()
                } else {
                    var album: Album? = null
                    val tracks: ArrayList<Track> = ArrayList()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            program.records.forEach { r ->
                                if (r.isNotString()) {
                                    mViewModel.getTrackById(r.toDouble().toInt())
                                        ?.let { track -> tracks.add(track) }
                                }
                            }
                            tracks.forEach { t ->
                                val temp_album = mViewModel.getAlbumById(t.albumId, t.category_id)
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
    }

    private fun onBackPressed() {
        parentFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.trans_left_to_right_in,
            R.anim.trans_left_to_right_out,
            R.anim.trans_right_to_left_in,
            R.anim.trans_right_to_left_out
        ).replace(
            R.id.nav_host_fragment, NewAlbumDetailFragment.newInstance(
                albumIdBackProgram!!, categoryIdBackProgram!!, typeBack, rifeBackProgram
            ), NewAlbumDetailFragment().javaClass.simpleName
        ).commit()
    }

    fun init() {
        loadingFrame.visibility = View.VISIBLE
        loadImageWithGif(ivImage, R.raw.loading_grey)
        if (isTrackAdd) {
            program_title.text = getString(R.string.txt_my_playlists)
            program_back.visibility = View.VISIBLE
        } else {
            program_title.text = getString(R.string.navigation_lbl_programs)
            program_back.visibility = View.INVISIBLE
        }

        mViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        )[NewProgramViewModel::class.java]

        programs_recycler_view.adapter = mProgramAdapter
    }
}