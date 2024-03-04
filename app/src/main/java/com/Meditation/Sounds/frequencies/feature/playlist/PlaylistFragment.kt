package com.Meditation.Sounds.frequencies.feature.playlist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.db.QFDatabase
import com.Meditation.Sounds.frequencies.feature.album.detail.AlbumDetailGroupFragment
import com.Meditation.Sounds.frequencies.feature.base.BaseActivity
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.feature.main.MainActivity
import com.Meditation.Sounds.frequencies.feature.playlist.detail.PlaylistDetailFragment
import com.Meditation.Sounds.frequencies.feature.playlist.detail.PlaylistDetailViewModel
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.models.*
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.StringsUtils
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.views.AddEditPlaylistDialog
import com.Meditation.Sounds.frequencies.views.AddPlaylistDialog
import com.Meditation.Sounds.frequencies.views.AlertMessageDialog
import kotlinx.android.synthetic.main.fragment_playlist.*
import kotlin.math.max

class PlaylistFragment : BaseFragment() {
    private lateinit var mAdapter: PlaylistAdapter
    private lateinit var mAdapterAdd: PlaylistAdapter
    private lateinit var mViewModel: PlaylistViewModel
    private var mDataAdded = ArrayList<Playlist>()
    private var mData = ArrayList<Playlist>()
    private var fromAlbumDetail = false
    private var mSong = Song()
    private var listSongFavorite = ArrayList<Song>()
    private lateinit var mViewModelPD: PlaylistDetailViewModel

    private val broadcastReceiverPurchased = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            loadPlaylist()
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            loadPlaylist()
        }
    }

    override fun initLayout(): Int { return R.layout.fragment_playlist }

    override fun initComponents() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mContext?.registerReceiver(broadcastReceiverPurchased, IntentFilter(Constants.BROADCAST_ACTION_PURCHASED),Context.RECEIVER_EXPORTED)
            mContext?.registerReceiver(broadcastReceiver, IntentFilter(Constants.BROADCAST_ADD_SONG_TO_ALBUM),Context.RECEIVER_EXPORTED)
        }else{
            mContext?.registerReceiver(broadcastReceiverPurchased, IntentFilter(Constants.BROADCAST_ACTION_PURCHASED))
            mContext?.registerReceiver(broadcastReceiver, IntentFilter(Constants.BROADCAST_ADD_SONG_TO_ALBUM))
        }
        mViewModelPD = ViewModelProviders.of(this).get(PlaylistDetailViewModel::class.java)

        if (fromAlbumDetail) {
            tvPlayListTitle.text = mContext?.getString(R.string.txt_my_playlists)
            viewFavorite.visibility = View.GONE
            rcPlaylist.visibility = View.GONE
            tvPrograms.visibility = View.GONE
            tvBack.visibility = View.VISIBLE
        } else {
            tvPlayListTitle.text = mContext?.getString(R.string.tv_programs)
            viewFavorite.visibility = View.VISIBLE
            rcPlaylist.visibility = View.VISIBLE
            tvPrograms.visibility = View.GONE
            tvBack.visibility = View.INVISIBLE
        }

        mAdapter = PlaylistAdapter(ArrayList(), object : PlaylistAdapter.IOnItemClickListener {
            override fun onItemClick(playlist: Playlist, isUnlock: Boolean) {
                if (isUnlock) {
                    val parent = parentFragment as PlaylistGroupFragment
                    parent.addFragment(PlaylistDetailFragment.newInstance(playlist, PlaylistItem(), false, false), R.id.frame1)
                } else {
                    val baseActivity = mContext as BaseActivity
                    baseActivity.showDialogSubscriptionFS(0)
                }
            }

            override fun onDeleteItem(playlist: Playlist) {
                val alertDialog = AlertMessageDialog(mContext, object : AlertMessageDialog.IOnSubmitListener {

                    override fun submit() {
                        mViewModel.delete(playlist.id)
                        Toast.makeText(mContext, mContext!!.getString(R.string.txt_delete_playlist_name_success), Toast.LENGTH_SHORT).show()
                    }

                    override fun cancel() {
                    }
                })
                alertDialog.show()
                alertDialog.setWarningMessage(getString(R.string.txt_warning_delete_playlist))
            }
        })
        rcPlaylist.layoutManager = LinearLayoutManager(context)
        rcPlaylist.adapter = mAdapter
        loadPlaylist()
        mAdapterAdd = PlaylistAdapter(mDataAdded, object : PlaylistAdapter.IOnItemClickListener {
            override fun onItemClick(playlist: Playlist, isUnlock: Boolean) {
                if (isUnlock){
                    if (fromAlbumDetail) {
                        val playlistItemSong = PlaylistItemSong()
                        playlistItemSong.song = mSong
                        playlistItemSong.songId = mSong.id
                        playlistItemSong.volumeLevel = 1f
                        playlistItemSong.startOffset = 0
                        playlistItemSong.endOffset = mSong.duration

                        val playlistItem = PlaylistItem()
                        playlistItem.playlistId = playlist.id

                        val playlistItemSongAndSong = PlaylistItemSongAndSong()
                        playlistItemSongAndSong.item = playlistItemSong
                        playlistItemSongAndSong.song = mSong
                        playlistItem.songs.add(playlistItemSongAndSong)
                        mViewModelPD.addSongToPlaylist(playlist.id, playlistItem)

                        playlist.totalTime = 0
                        var mediaType = Constants.MEDIA_TYPE_BASIC_FREE
                        val listPlaylist = mViewModelPD.getPlaylistItems(playlist.id).blockingGet()
                        for (item in listPlaylist) {
                            var max = 0L
                            for (s in item.songs) {
                                max = Math.max(max, s.item.endOffset - s.item.startOffset)
                            }
                            playlist.totalTime += max

                            for (song in item.songs) {
                                if (mediaType < song.song.mediaType!!) {
                                    mediaType = song.song.mediaType!!
                                }
                            }
                        }
                        playlist.mediaType = mediaType

                        mViewModelPD.updatePlaylist(playlist)

                        QFDatabase.getDatabase(mContext!!).playlistItemSongDAO().updateEndOffset(playlistItemSongAndSong.item.id, playlistItemSongAndSong.item.endOffset)

                        val musicService = (activity as MainActivity).musicService
                        musicService?.changeDuration(playlistItem, playlistItemSongAndSong)
                        musicService?.addSongToPlaylist(PlaylistItem.clone(playlistItem))

                        mAdapterAdd.notifyDataSetChanged()

                        val parent = parentFragment as AlbumDetailGroupFragment
                        parent.addToStackFragment(PlaylistDetailFragment.newInstance(playlist, playlistItem, true, false), R.id.frame2, this@PlaylistFragment)
                    } else {
                        val parent = parentFragment as PlaylistGroupFragment
                        parent.addToStackFragment(PlaylistDetailFragment.newInstance(playlist, PlaylistItem(), true, false), R.id.frame1, this@PlaylistFragment)
                    }
                }else{
                    val flashSaleRemainTimeGloble = Utils.getFlashSaleRemainTime()
                    val baseActivity = mContext as BaseActivity
                    if (flashSaleRemainTimeGloble>0){
                        baseActivity?.showDialogSubscriptionFS(0)
                    }else{
                        baseActivity.showDialogSubscriptionNormal(0)
                    }
                }
            }

            override fun onDeleteItem(playlist: Playlist) {
                val alertDialog = AlertMessageDialog(mContext, object : AlertMessageDialog.IOnSubmitListener {

                    override fun submit() {
                        mViewModel.delete(playlist.id)
                        Toast.makeText(mContext, mContext!!.getString(R.string.txt_delete_playlist_name_success), Toast.LENGTH_SHORT).show()
                    }

                    override fun cancel() {
                    }
                })
                alertDialog.show()
                alertDialog.setWarningMessage(getString(R.string.txt_warning_delete_playlist))
            }
        })
        rcPlaylistAdd?.layoutManager = LinearLayoutManager(context)
        rcPlaylistAdd?.adapter = mAdapterAdd

        listSongFavorite.clear()
        val database = QFDatabase.getDatabase(mContext!!.applicationContext)
        val listSong = database.songDAO().getSongFavorite()
        listSongFavorite.addAll(listSong)
        var totalTime = 0L
        for (song in listSong) {
            var max = 0L
            max = max(max, song.duration)
            totalTime += max
        }
        tvTotalTimeFavorite.text = StringsUtils.toString(totalTime)
    }

    fun loadPlaylist() {
        mViewModel = ViewModelProviders.of(this)[PlaylistViewModel::class.java]
        mViewModel.getAllPlayList().observe(this) {
            if (it != null) {
                mDataAdded.clear()
                mData.clear()
                if (fromAlbumDetail) {
                    for (item in it) {
                        if (item.fromUsers == 0) {
                            mData.add(item)
                        } else {
                            mDataAdded.add(item)
                        }
                    }
                } else {
                    mDataAdded.addAll(it)
                }

                if (mDataAdded.isEmpty()) {
                    line?.visibility = View.INVISIBLE
                } else {
                    line?.visibility = View.VISIBLE
                }
            }
            mAdapter.setListData(mData)
            mAdapterAdd.setListData(mDataAdded)
            mAdapter.initPurchaseRequest()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mContext!!.unregisterReceiver(broadcastReceiverPurchased)
        mContext!!.unregisterReceiver(broadcastReceiver)
    }

    override fun addListener() {
        btnAdd.setOnClickListener {
            val dialog = AddEditPlaylistDialog(mContext, null, object : AddEditPlaylistDialog.IOnSubmitListener {
                override fun submit(name: String) {
                    val dialogAdd = AddPlaylistDialog(mContext!!)
                    dialogAdd.show()
                    val handler = Handler()
                    val run = Runnable {
                        if (dialogAdd.isShowing) {
                            dialogAdd.dismiss()
                        }
                    }
                    dialogAdd.setOnDismissListener {
                        handler.removeCallbacks(run)
                    }
                    handler.postDelayed(run, 2000)
                    val playlist = Playlist(name)
                    playlist.fromUsers = 1
                    mViewModel.insert(playlist)
                    loadPlaylist()
                }
            })
            dialog.show()
        }
        tvBack.setOnClickListener {
            fragmentManager?.popBackStack()
        }

        viewFavorite.setOnClickListener {
            val parent = parentFragment as PlaylistGroupFragment
            parent.addToStackFragment(PlaylistDetailFragment.newInstance(Playlist(), PlaylistItem(), false,true), R.id.frame1, this@PlaylistFragment)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(fromAlbum: Boolean, song: Song): PlaylistFragment {
            val fragment = PlaylistFragment()
            fragment.fromAlbumDetail = fromAlbum
            fragment.mSong = song
            return fragment
        }
    }
}
