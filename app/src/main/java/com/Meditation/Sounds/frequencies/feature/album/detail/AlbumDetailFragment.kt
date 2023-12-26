package com.Meditation.Sounds.frequencies.feature.album.detail

import android.content.Intent
import android.text.TextUtils
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.FileEncyptUtil
import com.Meditation.Sounds.frequencies.MusicService
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.api.ApiListener
import com.Meditation.Sounds.frequencies.db.QFDatabase
import com.Meditation.Sounds.frequencies.feature.base.BaseActivity
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.feature.main.MainActivity
import com.Meditation.Sounds.frequencies.feature.playlist.PlaylistFragment
import com.Meditation.Sounds.frequencies.feature.playlist.detail.PlaylistDetailViewModel
import com.Meditation.Sounds.frequencies.models.*
import com.Meditation.Sounds.frequencies.tasks.BaseTask
import com.Meditation.Sounds.frequencies.tasks.GetAllAlbumTask
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import com.Meditation.Sounds.frequencies.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_album_detail.*
import kotlin.math.max

class AlbumDetailFragment : BaseFragment(), MusicService.IGetSongPlaying {

    private lateinit var mAlbum: Album
    private lateinit var mAdapter: AlbumDetailAdapter
    private lateinit var mViewModel: AlbumsDetailViewModel
    private lateinit var mViewModelPD: PlaylistDetailViewModel
    private var mSongs = ArrayList<Song>()
    private var playlistItems = ArrayList<PlaylistItem>()
    private var songAndSongs = ArrayList<PlaylistItemSongAndSong>()
    private var mDescriptionAdapter: DescriptionAdapter? = null
    private var mTypeAlbum = 0

    override fun initLayout(): Int {
        return R.layout.fragment_album_detail
    }

    override fun initComponents() {
        tvAlbumName.text = mAlbum.name
        tvAlbumName.isSelected = true
        tvDescription.text = mAlbum.artist
        if (!TextUtils.isEmpty(mAlbum.albumArt)) {
            if (mAlbum.albumArt!!.startsWith("http")) {
                Glide.with(requireActivity())
                        .applyDefaultRequestOptions(RequestOptions().placeholder(R.drawable.ic_album_placeholder))
                        .load(mAlbum.albumArt)
                        .into(imvAlbumArt2!!)
            } else {
                Glide.with(requireActivity())
                        .applyDefaultRequestOptions(RequestOptions().placeholder(R.drawable.ic_album_placeholder))
                        .load(mAlbum.albumArt)
                        .into(imvAlbumArt2!!)
            }
        } else {
            imvAlbumArt2?.setBackgroundResource(R.drawable.ic_album_default_small)
        }

        mAdapter = AlbumDetailAdapter(mContext!!, ArrayList(), object : AlbumDetailAdapter.IOnItemClickListener {
            override fun onPlayItemSong(song: Song) {
                val musicService = (activity as MainActivity).musicService
                musicService?.playItemSongAlbum(song)
                (activity as MainActivity).showPlayerController(true)
            }

            override fun onAddToFavorite(song: Song, isAdd: Int) {
                mViewModel.updateFavoriteSong(song,isAdd)
                val intent = Intent("BROADCAST_RELOAD_POPUP_ALBUM")
                mContext?.sendBroadcast(intent)
                mAdapter.notifyDataSetChanged()
            }

            override fun onPlusDuration(song: Song, isPlus: Boolean) {
                if (isPlus) {
                    if (song.duration < 86100000) {
                        song.duration += 300000
                    } else if (song.duration >= 86100000) {
                        song.duration = 86400000
                    }
                } else {
                    if (song.duration > 600000) {
                        song.duration -= 300000
                    } else {
                        song.duration = 300000
                    }
                }

                if (activity is MainActivity) {
                    val musicService = (activity as MainActivity).musicService
                    val itemSongAndSong = PlaylistItemSongAndSong()
                    val playlistItemSong = PlaylistItemSong()
                    playlistItemSong.id = -1
                    playlistItemSong.songId = song.id
                    itemSongAndSong.item = playlistItemSong
                    itemSongAndSong.song = song
                    itemSongAndSong.item.startOffset = 0
                    itemSongAndSong.item.endOffset = song.duration

                    var playlistItem = PlaylistItem()
                    for (item in playlistItems) {
                        for (temp in item.songs) {
                            if (temp.song == song) {
                                playlistItem = item
                            }
                        }
                    }
                    musicService!!.changeDuration(playlistItem, itemSongAndSong)
                }

                SharedPreferenceHelper.getInstance().setBool("CHANGE_DURATION", true)
                mViewModel.updateDurationSong(song, song.duration)
                updateSongToPlaylist(song)
                val intent = Intent("BROADCAST_RELOAD_POPUP_ALBUM")
                mContext?.sendBroadcast(intent)
                mAdapter.notifyDataSetChanged()
            }

            override fun onPlaySong(song: Song) {
                val musicService = (activity as MainActivity).musicService
                musicService?.playItemSongAlbum(song)
                (activity as MainActivity).showPlayerController(true)
            }

            override fun onAddSongToPlaylist(song: Song) {
                val parent = parentFragment as BaseFragment
                parent.addToStackFragment(PlaylistFragment.newInstance(true, song), R.id.frame2, this@AlbumDetailFragment)
            }
        })

        rcAlbum.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rcAlbum.adapter = mAdapter

        mDescriptionAdapter = DescriptionAdapter(mContext!!, ArrayList())
        rcDescription.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rcDescription.adapter = mDescriptionAdapter

        mViewModel = ViewModelProviders.of(this).get(AlbumsDetailViewModel::class.java)
        mViewModelPD = ViewModelProviders.of(this).get(PlaylistDetailViewModel::class.java)
        mViewModel.getSongs(mAlbum.id).observe(this, Observer {
            if (it != null) {
                mAdapter.data = it
                mSongs = it as ArrayList<Song>
                getSongPlaylistItem()
                if (mAlbum.benefits.isNotEmpty()) {
                    mDescriptionAdapter?.data = FileEncyptUtil.getDescription(mAlbum)
                    mDescriptionAdapter?.notifyDataSetChanged()
                } else {
                    if (Utils.isConnectedToNetwork(mContext!!)) {
                        GetAllAlbumTask(mContext!!, object : ApiListener<Any> {
                            override fun onConnectionOpen(task: BaseTask<*>?) {
                            }

                            override fun onConnectionSuccess(task: BaseTask<*>?, data: Any?) {
                                initComponents()
                            }

                            override fun onConnectionError(task: BaseTask<*>?, exception: Exception?) {
                            }
                        }).execute()
                    } else {
                        (activity as BaseActivity).showAlert(getString(R.string.err_network_available))
                    }
                }
                mAdapter.notifyDataSetChanged()
            }
        })
        btnBack.setOnClickListener {
            if (parentFragment!!::class != AlbumDetailGroupFragment::class) {
                if (requireParentFragment().childFragmentManager.backStackEntryCount > 0) {
                    for (i in requireParentFragment().childFragmentManager.backStackEntryCount - 1 downTo 0 step 1) {
                        requireParentFragment().childFragmentManager.popBackStackImmediate()
                    }
                }
            } else {
                if (requireParentFragment().requireFragmentManager().backStackEntryCount > 0) {
                    for (i in requireParentFragment().requireFragmentManager().backStackEntryCount - 1 downTo 0 step 1) {
                        requireParentFragment().requireFragmentManager().popBackStackImmediate()
                    }
                }
            }
            if (activity is MainActivity) {
                val musicService = (activity as MainActivity).musicService
                musicService?.stopMusicService()
            }
        }

        if (activity is MainActivity) {
            val musicService = (activity as MainActivity).musicService
            musicService?.addCallbackSongPlaying(this)
        }
    }

    private fun getSongPlaylistItem() {
        playlistItems = ArrayList()
        songAndSongs = ArrayList()

        for (song in mSongs) {
            val itemSongAndSong = PlaylistItemSongAndSong()
            val playlistItemSong = PlaylistItemSong()
            playlistItemSong.id = -1
            playlistItemSong.songId = song.id
            itemSongAndSong.item = playlistItemSong
            itemSongAndSong.song = song
            itemSongAndSong.item.startOffset = 0
            itemSongAndSong.item.endOffset = song.duration
            songAndSongs.add(itemSongAndSong)
        }

        for (i in 0 until songAndSongs.size) {
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
            playlistItems.add(playlistItem)
        }

    }

    override fun addListener() {
        btnPlay?.setOnClickListener {
            val musicService = (activity as MainActivity).musicService
            if (mSongs.size > 0) {
                musicService?.playSong(mSongs)
                (activity as MainActivity).showPlayerController(true)
            }
        }
    }

    override fun songPlaying(songId: Long) {
        mAdapter.setSongPlaying(songId)
    }

    fun updateSongToPlaylist(song: Song) {
        val database = QFDatabase.getDatabase(mContext!!.applicationContext)
        val listPlaylist = database.playlistDAO().getAll()
        for (playlist in listPlaylist) {
            val listPlaylistItem = mViewModelPD.getPlaylistItems(playlist.id).blockingGet()
            for (playlistItem in listPlaylistItem) {
                val songs = playlistItem.songs
                for (itemSong in songs) {
                    if (itemSong.song.title == song.title) {

                        var mediaType = Constants.MEDIA_TYPE_BASIC_FREE
                        var totalTime = 0L
                        var max = 0L
                        max = max(max, itemSong.item.endOffset - itemSong.item.startOffset)
                        totalTime += max
                        if (mediaType < itemSong.song.mediaType!!) {
                            mediaType = itemSong.song.mediaType!!
                        }
                        database.playlistItemSongDAO().updateEndOffset(itemSong.item.id, song.duration)
                        mViewModel.updateDurationSong(song, song.duration)
                        database.playlistDAO().updateTotalDurationPlaylist(playlist.id, totalTime, mediaType, playlist.fromUsers)
                    }
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(album: Album, type: Int): AlbumDetailFragment {
            val fragment = AlbumDetailFragment()
            fragment.mAlbum = album
            fragment.mTypeAlbum = type
            return fragment
        }
    }
}
