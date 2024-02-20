package com.Meditation.Sounds.frequencies.feature.playlist.detail

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.MusicService
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.db.QFDatabase
import com.Meditation.Sounds.frequencies.feature.album.AlbumsViewModel
import com.Meditation.Sounds.frequencies.feature.album.detail.AlbumDetailGroupFragment
import com.Meditation.Sounds.frequencies.feature.album.detail.AlbumsDetailViewModel
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.feature.main.MainActivity
import com.Meditation.Sounds.frequencies.feature.playlist.PlaylistAlbumFragment
import com.Meditation.Sounds.frequencies.feature.playlist.PlaylistGroupFragment
import com.Meditation.Sounds.frequencies.models.*
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.StringsUtils
import com.Meditation.Sounds.frequencies.utils.SwipeHelpers.SimpleItemTouchHelperCallback
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.views.AddEditPlaylistDialog
import kotlinx.android.synthetic.main.fragment_playlist_detail.*
import java.io.File

class PlaylistDetailFragment : BaseFragment(), Observer<Playlist?> , MusicService.IGetSongPlaying{//, OnStartDragListener

    private lateinit var linearLayoutManager: LinearLayoutManager
    private var mAdapter: PlaylistDetailAdapter? = null
    private var mPlaylist: LiveData<Playlist?>? = null
    private var mPlaylistObject: Playlist? = null
    private lateinit var mViewModel: PlaylistDetailViewModel
    private lateinit var mViewModelAD: AlbumsDetailViewModel
    private lateinit var mViewModelAlbum: AlbumsViewModel
    private var isEdited = false
    private var mItemTouchHelper: ItemTouchHelper? = null
    private var mSettingsContentObserver = SettingsContentObserver(Handler())
    lateinit var mAudioManager: AudioManager
    var playlistItem: PlaylistItem? = null
    var isAddFromAlbum = false
    var listAlbums = ArrayList<Album>()
    var isSongFavorite = false
    var listSongFavorite = ArrayList<Song>()

    inner class SettingsContentObserver(handler: Handler) : ContentObserver(handler) {

        override fun deliverSelfNotifications(): Boolean {
            return super.deliverSelfNotifications()
        }

        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            if (mAudioManager != null) {
                var max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                var current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                if (mAdapter!!.data != null) {
                    var position: Int = 0
                    for (item in mAdapter!!.data) {
                        if (item.isPlaying) {
                            for (it in item.songs) {
                                it.item.volumeLevel = (current / (max * 1.0)).toFloat()
                                QFDatabase.getDatabase(mContext!!).playlistItemSongDAO().updateVolumeLevel(it.item.id, it.item.volumeLevel)
                            }
                            mAdapter!!.notifyItemChanged(position)
                            break
                        }
                        position++
                    }

                }
            }
        }
    }

    private val broadCastReceiverPlaylist = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            if (intent != null && intent.hasExtra(Constants.EXTRAX_PLAYLIST_ITEM_ID)) {
                val playlistItemId = intent.getLongExtra(Constants.EXTRAX_PLAYLIST_ITEM_ID, -1)
                val isPlaying = intent.getBooleanExtra(Constants.EXTRAX_PLAYLIST_IS_PLAYING, false)
                updatePlaylistPlaying(playlistItemId, isPlaying)
            }
        }
    }

    private val broadCastBackAlbumFromPhone = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            tvBack.performClick()
        }
    }

    private val broadCastReceiverAddSong = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            when (intent?.action) {
                Constants.BROADCAST_ADD_SONG -> {
                    if (mPlaylist != null && mPlaylist!!.value != null) {
                        val song = intent.getSerializableExtra(Constants.EXTRAX_SONG) as Song
                        val playlistItemSong = PlaylistItemSong()
                        playlistItemSong.song = song
                        playlistItemSong.songId = song.id
                        playlistItemSong.volumeLevel = 1f
                        playlistItemSong.startOffset = 0
                        playlistItemSong.endOffset = song.duration

                        val playlistItem = PlaylistItem()
                        playlistItem.playlistId = mPlaylistObject!!.id
                        playlistItem.songs.add(PlaylistItemSongAndSong(playlistItemSong, song))
                        //update database
                        mViewModel.addSongToPlaylist(mPlaylistObject!!.id, playlistItem)

                        //update playlist
                        mAdapter?.data?.add(playlistItem)

                        mPlaylist!!.value!!.totalTime = 0
                        var mediaType = Constants.MEDIA_TYPE_BASIC_FREE
                        for (item in mAdapter!!.data) {
                            var max = 0L
                            for (s in item.songs) {
                                max = Math.max(max, s.item.endOffset - s.item.startOffset)
                            }
                            mPlaylist!!.value!!.totalTime += max

                            for (it in item.songs) {
                                if (mediaType < it.song.mediaType!!) {
                                    mediaType = it.song.mediaType!!
                                }
                            }
                        }
                        mPlaylist!!.value!!.mediaType = mediaType
                        mViewModel.updatePlaylist(mPlaylist!!.value!!)

                        //Update playlist in controller player
                        if (activity is MainActivity) {
                            val musicService = (activity as MainActivity).musicService
                            if (musicService != null) {
                                musicService?.addSongToPlaylist(PlaylistItem.clone(playlistItem))
                            }
                        }

                        tvNoData.visibility = View.GONE
                        if (mAdapter != null && mAdapter!!.data.size > 0) {
                            btnPlayPlaylist.visibility = View.VISIBLE
                        } else {
                            btnPlayPlaylist.visibility = View.GONE
                        }

                        mAdapter!!.notifyItemInserted(mAdapter!!.itemCount - 1)
                    }
                }
            }
        }
    }

    private val broadCastReceiverPlaySongFromMain = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            if (mAdapter != null) {
                var playlistItems: ArrayList<PlaylistItem> = ArrayList()
                if (mAdapter!!.data != null) {
                    for (item in mAdapter!!.data) {
                        playlistItems.add(item)
                    }
                }
                val musicService = (activity as MainActivity).musicService
                musicService?.play(playlistItems)
            }
        }
    }

    override fun initLayout(): Int {
        return R.layout.fragment_playlist_detail
    }

    override fun initComponents() {
        linearLayoutManager = LinearLayoutManager(context)
        val service = (activity as MainActivity).musicService
        service?.addCallbackSongPlaying(this)

        listSongFavorite.clear()
        val db = QFDatabase.getDatabase(mContext!!.applicationContext)
        listSongFavorite = db.songDAO().getSongFavorite() as ArrayList<Song>

        Log.e("LOG", "initComponents")

        listSongFavorite.forEach {
            Log.e("LOG", "title " + it.title)
            Log.e("LOG", "duration " + it.duration)
            Log.e("LOG", "path " + it.path)
            Log.e("LOG", "albumId " + it.albumId)
            Log.e("LOG", "albumName " + it.albumName)
            Log.e("LOG", "fileName " + it.fileName)
            Log.e("LOG", "favorite " + it.favorite)

            val file = File(it.path)
            val result = file.name.replace(".shapee", "")
            Log.e("LOG", "RESULT => $result")
        }

        mAdapter = PlaylistDetailAdapter(requireContext(), ArrayList(), mPlaylistObject?.fromUsers == 1, object : PlaylistDetailAdapter.IOnItemClickListener {
            override fun onMoveFavorite(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong) {
            }

            override fun onRemoveFavorite(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong) {
                val database = QFDatabase.getDatabase(context!!.applicationContext)
                database.songDAO().removeFavorite(song.song.id, song.song.albumId)
                val intent = Intent("BROADCAST_RELOAD_POPUP_ALBUM")
                mContext?.sendBroadcast(intent)
                mAdapter?.updateDuration(mContext!!, song)

                //Update controller player
                if (activity is MainActivity) {
                    val musicService = (activity as MainActivity).musicService
                    if (musicService != null) {
                        musicService.deleteSong(playlistItem, song)
                        musicService.changeDuration(playlistItem, song)
                    }
                }

                getAndShowSongFavorite()
            }

            override fun onDeletedItem(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong) {
                mViewModel.deleteSongFromPlaylistItem(mPlaylistObject!!.id, playlistItem, song)

                //Update controller player
                if (activity is MainActivity) {
                    val musicService = (activity as MainActivity).musicService
                    if (musicService != null) {
                        musicService.deleteSong(playlistItem, song)
                        musicService.changeDuration(playlistItem, song)
                    }
                }

                //update playlist
                mPlaylist!!.value!!.totalTime = 0
                var mediaType = Constants.MEDIA_TYPE_BASIC_FREE
                for (item in mAdapter!!.data) {
                    var max = 0L
                    for (s in item.songs) {
                        max = Math.max(max, s.item.endOffset - s.item.startOffset)
                    }
                    mPlaylist!!.value!!.totalTime += max

                    for (it in item.songs) {
                        if (mediaType < it.song.mediaType!!) {
                            mediaType = it.song.mediaType!!
                        }
                    }
                }
                mPlaylist!!.value!!.mediaType = mediaType
                mViewModel.updatePlaylist(mPlaylist!!.value!!)

                if (mAdapter != null && mAdapter!!.data.size > 0) {
                    btnPlayPlaylist.visibility = View.VISIBLE
                    tvNoData.visibility = View.GONE
                } else {
                    tvNoData.visibility = View.VISIBLE
                    btnPlayPlaylist.visibility = View.GONE
                }
                mAdapter?.updateDuration(mContext!!, song)
                updateDurationOfPlaylist()
            }

            override fun onSelectedItemChanged(selectedItems: ArrayList<PlaylistItem>, playlistItem: PlaylistItem) {
//                if (selectedItems.size > 1 || isEdited) {
//                    btnSave.text = getString(R.string.txt_save)
//                } else {
//                    btnSave.text = getString(R.string.txt_add)
//                }
            }

            override fun onItemChanged(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong, isVolumeChange: Boolean) {
//                isEdited = true
//                btnSave.text = getString(R.string.txt_save)
//                if (mAdapter != null && mAdapter!!.data.size > 0) {
//                    btnPlayPlaylist.visibility = View.VISIBLE
//                } else {
//                    btnPlayPlaylist.visibility = View.GONE
//                }
                if (isVolumeChange) {
                    //Change Volumn
                    if (activity is MainActivity) {
                        val musicService = (activity as MainActivity).musicService
                        if (musicService != null) {
                            musicService.changeVolume(playlistItem, song)
                        }
                    }
                } else {
                    //Change Duration
                    if (activity is MainActivity) {
                        val musicService = (activity as MainActivity).musicService
                        if (musicService != null) {
                            musicService.changeDuration(playlistItem, song)
                        }
                    }
                    updateDurationOfPlaylist()
                }
//                if (mItemTouchHelper != null) {
//                    mItemTouchHelper!!.attachToRecyclerView(null)
//                }
//                mSimpleItemTouchHelperCallback = SimpleItemTouchHelperCallback(mAdapter)
//                mItemTouchHelper = ItemTouchHelper(mSimpleItemTouchHelperCallback!!)
//                mItemTouchHelper!!.attachToRecyclerView(rcTrack)
            }

            override fun onAutoSavePlaylist() {
                savePlaylist(true)
            }

            override fun onChangeDuration(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong, isPlus: Boolean) {
                if (activity is MainActivity) {
                    val musicService = (activity as MainActivity).musicService
                    if (isPlus) {
                        if (song.item.endOffset < 86100000) {
                            song.item.endOffset += 300000
                        } else if (song.item.endOffset >= 86100000) {
                            song.item.endOffset = 86400000
                        }
                    } else {
                        if (song.item.endOffset > 600000) {//song.song.duration
                            song.item.endOffset -= 300000
                        } else {
                            song.item.endOffset = 300000
                        }
                    }

                    val database = QFDatabase.getDatabase(mContext!!.applicationContext)
                    val listPlaylist = database.playlistDAO().getAll()
                    for (playlist in listPlaylist) {
                        val listPlaylistItem = mAdapter!!.data
                        var isUpdated = false
                        for (item in listPlaylistItem) {
                            val songs = item.songs
                            for (itemSong in songs) {
                                if (itemSong.song.title == song.song.title) {
                                    database.playlistItemSongDAO().updateEndOffset(itemSong.item.id, song.item.endOffset)
                                    mViewModelAD.updateDurationSong(song.song, song.item.endOffset)
                                    musicService!!.changeDuration(playlistItem, song)
                                    mAdapter?.updateDuration(mContext!!, song)
                                    isUpdated = true
                                    break
                                }
                            }
                            if (isUpdated) {
                                break
                            }
                        }
                        if (isUpdated) {
                            break
                        }
                    }
                    updateDurationOfPlaylist()
//                    if (mItemTouchHelper != null) {
//                        mItemTouchHelper!!.attachToRecyclerView(null)
//                    }
//                    mSimpleItemTouchHelperCallback = SimpleItemTouchHelperCallback(mAdapter)
//                    mItemTouchHelper = ItemTouchHelper(mSimpleItemTouchHelperCallback!!)
//                    mItemTouchHelper!!.attachToRecyclerView(rcTrack)
                }
            }

            override fun onPlayItemSong(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong) {
//                if (song.song.favorite == 1) {
//                    musicService?.playItemSongAlbum(song.song)
//                } else {
//                    musicService?.playItemSongPlaylist(playlistItem)
//                }
                val musicService = (activity as MainActivity).musicService
                musicService?.playItemSongPlaylist(playlistItem)
                (activity as MainActivity).showPlayerController(true)
            }
        })
        rcTrack.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rcTrack.adapter = mAdapter
        rcTrack.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        mSimpleItemTouchHelperCallback = SimpleItemTouchHelperCallback(mAdapter)
        mItemTouchHelper = ItemTouchHelper(mSimpleItemTouchHelperCallback!!)
        mItemTouchHelper!!.attachToRecyclerView(rcTrack)

        mViewModelAD = ViewModelProviders.of(this).get(AlbumsDetailViewModel::class.java)
        mViewModel = ViewModelProviders.of(this).get(PlaylistDetailViewModel::class.java)
        mViewModelAlbum = ViewModelProviders.of(this).get(AlbumsViewModel::class.java)

        getAndShowPlayList()

        if (isAddFromAlbum) {
            updatePlaylist()
        }
        if (isSongFavorite) {
            tvPlayListName.text = getString(R.string.tv_favorites)
        } else {
            tvPlayListName.text = mPlaylistObject?.title
        }

        btnSave.text = getString(R.string.txt_add)

        Log.d("MEN", "mediaType" + mPlaylistObject!!.mediaType)

    }

    private fun updatePlaylist() {
        val listPlaylist = mViewModel.getPlaylistItems(mPlaylistObject!!.id).blockingGet()

        val albumDao = QFDatabase.getDatabase(requireContext()).albumDAO()
        val albums = albumDao.getAll() as ArrayList<Album>
        listAlbums.clear()
//        var playlist = Playlist()
        mViewModel.getPlaylist(mPlaylistObject!!.id).observe(this, Observer {
            if (it != null) {
//                playlist = it/
                if (listPlaylist != null) {
                    mAdapter?.updateData(listPlaylist)
                    it.totalTime = 0
                    var mediaType = Constants.MEDIA_TYPE_BASIC_FREE
                    for (item in listPlaylist) {
                        var max = 0L
                        for (s in item.songs) {
                            max = Math.max(max, s.item.endOffset - s.item.startOffset)

                            for (album in albums) {
                                if (album.id == s.song.albumId && album.name == s.song.albumName) {
                                    listAlbums.add(album)
                                }
                            }
                        }
                        it.totalTime += max

                        for (song in item.songs) {
                            if (mediaType < song.song.mediaType!!) {
                                mediaType = song.song.mediaType!!
                            }
                        }


                    }
                    it.mediaType = mediaType
                    mPlaylistObject = it
                    tvTotalTime.text = StringsUtils.toString(it.totalTime)
                    mAdapter?.setDataAlbum(listAlbums)
                    mAdapter?.notifyDataSetChanged()

                    if (mContext is MainActivity) {
                        val mainActivity = mContext as MainActivity
                        val playlistItemPlaying = mainActivity.getCurrentPlaylistItem()
                        if (playlistItemPlaying != null) {
                            updatePlaylistPlaying(playlistItemPlaying.id, mainActivity.getPlayingAction())
                        }
                    }
                }
            }
        })
    }

    fun updateDurationOfPlaylist() {
        if (mPlaylist != null && mAdapter!!.data!! != null) {
            var mediaType = Constants.MEDIA_TYPE_BASIC_FREE
            var totalTime = 0L
            for (item in mAdapter!!.data) {
                var max = 0L
                for (s in item.songs) {
                    max = Math.max(max, s.item.endOffset - s.item.startOffset)
                }
                totalTime += max

                for (it in item.songs) {
                    if (mediaType < it.song.mediaType!!) {
                        mediaType = it.song.mediaType!!
                    }
                }
            }
            mViewModel.updateDurationOfAllPlaylist(mPlaylist!!.value!!, totalTime, mediaType, mPlaylist!!.value!!.fromUsers)
            tvTotalTime.text = StringsUtils.toString(totalTime)
            val intent = Intent("BROADCAST_CHANGE_DURATION_PROGRAM")
            mContext?.sendBroadcast(intent)
        }
    }

    var mSimpleItemTouchHelperCallback: SimpleItemTouchHelperCallback? = null
//    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
//        viewHolder?.let {
//            mItemTouchHelper?.startDrag(it)
//        }
//    }

    fun updatePlaylistPlaying(playlistItemId: Long, isPlaying: Boolean) {
        if (mAdapter!!.data != null) {
            var position: Int = 0
            for (item in mAdapter!!.data) {
                if (item.id == playlistItemId) {
                    item.isPlaying = true
                    item.isPlayingAction = isPlaying
                    mAdapter!!.notifyItemChanged(position)
                } else {
                    if (item.isPlaying) {
                        item.isPlaying = false
                        mAdapter!!.notifyItemChanged(position)
                    }
                }
                position++
            }

        }
    }

    private fun getAndShowPlayList() {
        val albumDao = QFDatabase.getDatabase(requireContext()).albumDAO()
        val albums = albumDao.getAll() as ArrayList<Album>
        if (isSongFavorite) {
            getAndShowSongFavorite()
        } else {
            if (mPlaylist != null) {
                mPlaylist!!.removeObserver(this)
            }
            mPlaylist = mViewModel.getPlaylist(mPlaylistObject!!.id)
            mPlaylist!!.observe(this, this)

            mAdapter!!.data = mViewModel.getPlaylistItems(mPlaylistObject!!.id).blockingGet()
            val listItemPlaylist = mViewModel.getPlaylistItems(mPlaylistObject!!.id).blockingGet()
            if (mAdapter!!.data.size > 0) {
                tvNoData.visibility = View.GONE
                btnPlayPlaylist.visibility = View.VISIBLE
            } else {
                tvNoData.visibility = View.VISIBLE
                btnPlayPlaylist.visibility = View.GONE
            }
            listAlbums.clear()
            listItemPlaylist?.let {
                for (item in it) {
                    for (song in item.songs) {
                        for (album in albums) {
                            if (album.id == song.song.albumId && album.name == song.song.albumName) {
                                listAlbums.add(album)
                            }
                        }
                    }
                }
            }
            mAdapter?.setDataAlbum(listAlbums)
            mAdapter?.setFromFavorite(false)
        }
        if (mContext is MainActivity) {
            val mainActivity = mContext as MainActivity
            val playlistItemPlaying = mainActivity.getCurrentPlaylistItem()
            if (playlistItemPlaying != null) {
                updatePlaylistPlaying(playlistItemPlaying.id, mainActivity.getPlayingAction())
            }
        }
    }

    fun getAndShowSongFavorite(){
        val albumDao = QFDatabase.getDatabase(requireContext()).albumDAO()
        val albums = albumDao.getAll() as ArrayList<Album>
        var totalTime = 0L

        val playlist = Playlist()
        playlist.id = -1
        playlist.fromUsers = 1
        playlist.title = getString(R.string.tv_favorites)
        listAlbums.clear()
        val listItem = ArrayList<PlaylistItem>()
        val songAndSongs = ArrayList<PlaylistItemSongAndSong>()
        for (song in listSongFavorite) {
            val itemSongAndSong = PlaylistItemSongAndSong()
            val playlistItemSong = PlaylistItemSong()
            playlistItemSong.id = -1
            playlistItemSong.songId = song.id
            itemSongAndSong.item = playlistItemSong
            itemSongAndSong.song = song
            itemSongAndSong.item.startOffset = 0
            itemSongAndSong.item.endOffset = song.duration
            songAndSongs.add(itemSongAndSong)

            for (album in albums) {
                if (album.id == song.albumId && album.name == song.albumName) {
                    listAlbums.add(album)
                }
            }
            var max = 0L
            max = Math.max(max, song.duration)
            totalTime += max
        }



        for (i in 0..songAndSongs.size - 1) {
            val itemSong = PlaylistItemSongAndSong()
            val playlistItem = PlaylistItem()
            itemSong.song = songAndSongs[i].song
            itemSong.item.startOffset = 0
            itemSong.item.endOffset = songAndSongs[i].item.endOffset
            itemSong.item.volumeLevel = songAndSongs[i].item.volumeLevel
            itemSong.item.id = songAndSongs[i].item.id
            playlistItem.playlistId = -i.toLong()
            playlistItem.id = -i.toLong()
            if (playlistItem.songs.size == 0) {
                playlistItem.songs.add(itemSong)
            }
            listItem.add(playlistItem)
        }
        tvTotalTime.text = StringsUtils.toString(totalTime)
        mAdapter?.updateData(listItem)
        mAdapter!!.data = listItem
        mAdapter?.setDataAlbum(listAlbums)
        mAdapter?.setFromFavorite(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mAudioManager = mContext!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mContext?.registerReceiver(
                broadCastReceiverAddSong,
                IntentFilter(Constants.BROADCAST_ADD_SONG),
                Context.RECEIVER_EXPORTED
            )
            mContext!!.registerReceiver(
                broadCastReceiverPlaylist,
                IntentFilter(Constants.BROADCAST_PLAY_PLAYLIST),
                Context.RECEIVER_EXPORTED
            )
        } else {
            mContext?.registerReceiver(
                broadCastReceiverAddSong,
                IntentFilter(Constants.BROADCAST_ADD_SONG)
            )
            mContext!!.registerReceiver(
                broadCastReceiverPlaylist,
                IntentFilter(Constants.BROADCAST_PLAY_PLAYLIST)
            )
        }
        mContext!!.contentResolver.registerContentObserver(
            android.provider.Settings.System.CONTENT_URI,
            true,
            mSettingsContentObserver
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mContext!!.registerReceiver(
                broadCastBackAlbumFromPhone,
                IntentFilter(Constants.BROADCAST_BACK_ALBUM_FROM_PHONE),
                Context.RECEIVER_EXPORTED
            )
        } else {
            mContext!!.registerReceiver(
                broadCastBackAlbumFromPhone,
                IntentFilter(Constants.BROADCAST_BACK_ALBUM_FROM_PHONE)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mContext!!.unregisterReceiver(broadCastReceiverAddSong)
        mContext!!.unregisterReceiver(broadCastReceiverPlaylist)
        mContext!!.contentResolver.unregisterContentObserver(mSettingsContentObserver)
        mContext!!.unregisterReceiver(broadCastBackAlbumFromPhone)
    }

    override fun addListener() {
        tvPlayListName.setOnClickListener {
            if (mPlaylistObject!!.fromUsers == 1) {
                val dialog = AddEditPlaylistDialog(mContext, mPlaylist!!.value, object : AddEditPlaylistDialog.IOnSubmitListener {
                    override fun submit(name: String) {
                        if (mPlaylist!!.value == null) {
                            var playlist = Playlist(name)
                            playlist = mViewModel.insertPlaylist(playlist)

                            mPlaylistObject!!.id = playlist.id
                            getAndShowPlayList()
                        } else {
                            mPlaylist!!.value!!.title = name
                            mViewModel.updatePlaylist(mPlaylist!!.value!!)
                            Toast.makeText(mContext, mContext!!.getString(R.string.txt_edit_playlist_name_success), Toast.LENGTH_SHORT).show()
                        }

                    }
                })
                dialog.show()
            }
        }
        btnSave.setOnClickListener {
            if (btnSave.text.toString() == getString(R.string.txt_add)) {
                for (i in requireParentFragment().childFragmentManager.backStackEntryCount - 1 downTo 0 step 1) {
                    if (requireParentFragment().childFragmentManager.getBackStackEntryAt(i).name == PlaylistDetailFragment::class.java.name) {
                        break
                    } else {
                        requireParentFragment().childFragmentManager.popBackStackImmediate()
                    }
                }
                val parent = parentFragment as BaseFragment
                parent.addFragment(PlaylistAlbumFragment(), R.id.frame2)
                parent.addFragment(PlaylistAlbumFragment(), R.id.frame2)

                if (!Utils.isTablet(mContext)) {
                    if (parent is PlaylistGroupFragment) {
                        parent.showAlbumFragment()
                    }
                }

                if (!Utils.isTablet(mContext)) {
                    tvBack.visibility = View.GONE
                }

            } else {
                savePlaylist(false)
            }
        }
        btnClear.setOnClickListener {
            if (mAdapter!!.data.size > 0) {
                dialogConfirmClearPlaylist()
            }
        }
        btnPlayPlaylist.setOnClickListener {
            val musicService = (activity as MainActivity).musicService
            if (isSongFavorite) {
                if (listSongFavorite.size > 0) {
                    musicService?.playSong(listSongFavorite)
                }else{
                    listSongFavorite.clear()
                    val db = QFDatabase.getDatabase(mContext!!.applicationContext)
                    listSongFavorite = db.songDAO().getSongFavorite() as ArrayList<Song>
                    musicService?.playSong(listSongFavorite)
                }
            } else {
                val playlistItems: ArrayList<PlaylistItem> = ArrayList()
                if (mAdapter!!.data != null) {
                    for (item in mAdapter!!.data) {
                        playlistItems.add(item)
                    }
                }
                musicService?.play(playlistItems)
            }
            (activity as MainActivity).showPlayerController(true)
        }

        tvBack.setOnClickListener {
            mContext?.sendBroadcast(Intent("BROADCAST_DISMISS_POPUP_PROGRAM"))
            fragmentManager?.popBackStack()
        }
    }

    fun savePlaylist(autoSave: Boolean) {
        if (mAdapter!!.seletectedItems.size > 1) {
            val rootItem = mAdapter!!.seletectedItems[0]
            for (i in 1 until mAdapter!!.seletectedItems.size) {
                val item = mAdapter!!.seletectedItems[i]
                for (song in item.songs) {
                    song.item.playlistItemId = rootItem.id
                    rootItem.songs.add(song)
                }
                mAdapter!!.data.remove(item)
            }
        }

        mViewModel.savePlaylistTask(mPlaylistObject!!.id, mAdapter!!.data).blockingGet()

        if (activity is MainActivity) {
            val musicService = (activity as MainActivity).musicService
            if (musicService != null) {
                musicService!!.clearPlaylist(mPlaylistObject!!.id)
            }
        }

        for (item in mAdapter!!.data) {
            item.isPlaying = false
        }

        mAdapter!!.seletectedItems.clear()
        if (!autoSave) {
            mAdapter!!.notifyDataSetChanged()
        }

        mPlaylist!!.value!!.totalTime = 0
        for (playlistItem in mAdapter!!.data) {
            var max = 0L
            for (song in playlistItem.songs) {
                max = Math.max(max, song.item.endOffset - song.item.startOffset)
            }
            mPlaylist!!.value!!.totalTime += max
        }
        mViewModel.updatePlaylist(mPlaylist!!.value!!)
        tvTotalTime.text = StringsUtils.toString(mPlaylist!!.value!!.totalTime)

        btnSave.text = getString(R.string.txt_add)
        if (!autoSave) {
            Toast.makeText(mContext, "Saved!", Toast.LENGTH_SHORT).show()
            if (requireParentFragment()::class != AlbumDetailGroupFragment::class) {
                if (requireParentFragment().childFragmentManager.backStackEntryCount > 0) {
                    for (i in requireParentFragment().childFragmentManager.backStackEntryCount - 1 downTo 0 step 1) {
                        if (requireParentFragment().childFragmentManager.getBackStackEntryAt(i).name == PlaylistDetailFragment::class.java.name) {
                            break
                        } else {
                            requireParentFragment().childFragmentManager.popBackStackImmediate()
                        }
                    }
                }
            }
        }
        isEdited = false
    }

    private fun clearPlaylist() {
        //Clear data
        mAdapter!!.data.clear()
        mAdapter!!.seletectedItems.clear()
        mAdapter!!.notifyDataSetChanged()

        savePlaylist(false)
    }

    private fun dialogConfirmClearPlaylist() {
        AlertDialog.Builder(mContext)
                .setTitle(R.string.app_name)
                .setMessage(R.string.txt_warning_clear_playlist)
                .setPositiveButton(R.string.txt_agree) { _, _ ->
                    clearPlaylist()
                }.setNegativeButton(R.string.txt_disagree) { _, _ ->

                }.show()
    }

    override fun onChanged(playlist: Playlist?) {
        if (playlist != null) {
            tvPlayListName.text = playlist.title

            layoutTotalTime.visibility = View.VISIBLE
        } else {
            tvPlayListName.text = getString(R.string.txt_create_playlist)
            layoutTotalTime.visibility = View.GONE
        }
    }

    override fun songPlaying(songId: Long) {
        mAdapter?.setSongPlaying(songId)
    }

    companion object {
        @JvmStatic
        fun newInstance(playlist: Playlist, playlistItem: PlaylistItem, isAdd: Boolean, isFavorite: Boolean): PlaylistDetailFragment {
            val fragment = PlaylistDetailFragment()
            fragment.mPlaylistObject = playlist
            fragment.playlistItem = playlistItem
            fragment.isAddFromAlbum = isAdd
            fragment.isSongFavorite = isFavorite
            return fragment
        }
    }
}
