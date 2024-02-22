package com.Meditation.Sounds.frequencies.feature.playlist

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.adapters.PlaylistAlbumAdapter
import com.Meditation.Sounds.frequencies.db.QFDatabase
import com.Meditation.Sounds.frequencies.feature.album.AlbumsViewModel
import com.Meditation.Sounds.frequencies.feature.album.detail.AlbumDetailAdapter
import com.Meditation.Sounds.frequencies.feature.album.detail.AlbumDetailFragment
import com.Meditation.Sounds.frequencies.feature.base.BaseActivity
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.models.Album
import com.Meditation.Sounds.frequencies.models.Song
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.FilesUtils
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import com.Meditation.Sounds.frequencies.utils.Utils
import kotlinx.android.synthetic.main.fragment_playlist.*

class PlaylistAlbumFragment : BaseFragment() {
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var mAdapterAlbum: PlaylistAlbumAdapter
    private lateinit var mAdapterAlbumAdvanced: PlaylistAlbumAdapter
    private lateinit var mAdapterSong: AlbumDetailAdapter
    private lateinit var mAdapterSongAdvanced: AlbumDetailAdapter
    val mAlbums: ArrayList<Album> = ArrayList()
    private var mAlbumsAdvanced = ArrayList<Album>()
    val mSong: ArrayList<Song> = ArrayList()
    val mSongAdvanced: ArrayList<Song> = ArrayList()
    var mHandler = Handler()

    var baseActivity: BaseActivity? = null
    private val broadcastReceiverPlaylistAlbumController = object : BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(p0: Context?, intent: Intent?) {
            if (SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED)) {
                for (i in 0..mAdapterAlbum.mData.size - 1) {
                    if (i < mAdapterAlbum.mData.size){
                        mAdapterAlbum.mData[i].isPurchase = true
                    }
                }
            }
            mAdapterAlbum.notifyDataSetChanged()

            if (SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_ADVANCED)) {
                for (i in 0 until mAdapterAlbumAdvanced.mData.size) {
                    if (i < mAdapterAlbumAdvanced.mData.size){
                        mAdapterAlbumAdvanced.mData[i].isPurchase = true
                    }
                }
            }
            mAdapterAlbumAdvanced.notifyDataSetChanged()
        }
    }

    var mSearchRunnable = Runnable { searchTracks() }

    private lateinit var mViewModel: AlbumsViewModel
    private var mSongListener = object : AlbumDetailAdapter.IOnItemClickListener {
        override fun onAddToFavorite(song: Song, isAdd: Int) {

        }

        override fun onAddSongToPlaylist(song: Song) {
        }

        override fun onPlusDuration(song: Song, isPlus: Boolean) {

        }

        override fun onPlaySong(song: Song) {
        }

        override fun onPlayItemSong(song: Song) {
        }
    }

    override fun initLayout(): Int {
        return R.layout.fragment_playlist
    }

    override fun initComponents() {
        btnAdd.visibility = View.GONE
        groupSearch.visibility = View.GONE

        linearLayoutManager = LinearLayoutManager(context)
        mAdapterAlbum = PlaylistAlbumAdapter(requireActivity(), mAlbums)
        mAdapterSong = AlbumDetailAdapter(mContext!!, mSong, mSongListener)
        mAdapterSongAdvanced = AlbumDetailAdapter(mContext!!,mSongAdvanced, mSongListener)
        rcPlaylist.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rcPlaylist.isNestedScrollingEnabled = false
        rcPlaylist.adapter = mAdapterAlbum
        rcPlaylist.isNestedScrollingEnabled = false

        mAdapterAlbumAdvanced = PlaylistAlbumAdapter(requireActivity(), mAlbumsAdvanced)
        mRcPlaylistAdvanced.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        mRcPlaylistAdvanced.isNestedScrollingEnabled = false
        mRcPlaylistAdvanced.adapter = mAdapterAlbumAdvanced
        mRcPlaylistAdvanced.isNestedScrollingEnabled = false

        baseActivity = mContext as BaseActivity

        tvPlayListTitle.text = mContext!!.getString(R.string.navigation_lbl_albums)

        mViewModel = ViewModelProviders.of(this).get(AlbumsViewModel::class.java)
        mViewModel.getAllAlbumBasic().observe(this, Observer {
            if (it != null) {
                mAlbums.clear()
                for (album in it) {
                    mAlbums.add(album)
                }
                checkPurchase(false)
            }
            if (mAlbums.size > 0) {
                tvNoData.visibility = View.GONE
            } else {
                tvNoData.text = getString(R.string.txt_no_albums)
                tvNoData.visibility = View.VISIBLE
            }
        })

        mViewModel.getAlbumAdvanced().observe(this, Observer {
            if (it != null) {
                mAlbumsAdvanced.clear()
                for (item in it) {
                    mAlbumsAdvanced.add(item)
                }
                checkPurchase(true)
            }
            if (mAlbumsAdvanced.size > 0) {
                tvNoDataAdvanced.visibility = View.GONE
            } else {
                tvNoDataAdvanced.text = getString(R.string.txt_no_albums)
                tvNoDataAdvanced.visibility = View.VISIBLE
            }
        })

        if (!Utils.isTablet(mContext)) {
            tvBack.visibility = View.VISIBLE
            tvBack.setOnClickListener {
                mContext!!.sendBroadcast(Intent(Constants.BROADCAST_BACK_ALBUM_FROM_PHONE))
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mContext!!.registerReceiver(
                broadcastReceiverPlaylistAlbumController,
                IntentFilter(Constants.BROADCAST_ACTION_PURCHASED),
                Context.RECEIVER_EXPORTED
            )
        } else {
            mContext!!.registerReceiver(
                broadcastReceiverPlaylistAlbumController,
                IntentFilter(Constants.BROADCAST_ACTION_PURCHASED)
            )
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun checkPurchase(isAdvanced: Boolean) {
        if (isAdvanced) {
            for (i in 0 until mAdapterAlbumAdvanced.mData.size) {
                mAdapterAlbumAdvanced.mData[i].isPurchase = SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_ADVANCED)
            }
        } else {
            for (i in 0 until mAdapterAlbum.mData.size) {
                if (mAdapterAlbum.mData.get(i).album_priority < 3) {
                    mAdapterAlbum.mData[i].isPurchase = true
                } else {
                    mAdapterAlbum.mData.get(i).isPurchase = SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED)
                }
            }
//            if (mAdapterAlbum.mData.size > 0) {
//                for (i in 0..2) {
//                    if (i < mAdapterAlbum.mData.size) {
//                        mAdapterAlbum.mData[i].isPurchase = true
//                    }
//                }
//            }
//            for (i in 3..mAdapterAlbum.mData.size - 1) {
//                mAdapterAlbum.mData[i].isPurchase = SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED)
//            }
        }
        mAdapterAlbumAdvanced.notifyDataSetChanged()
        mAdapterAlbum.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun searchTracks() {
        val database = QFDatabase.getDatabase(mContext!!.applicationContext)
        var positionPurchase = 2

        if (SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED)) {
            if (SharedPreferenceHelper.getInstance().getInt(Constants.INApp_PACKED) == Constants.INApp_FREE ||
                    SharedPreferenceHelper.getInstance().getInt(Constants.INApp_PACKED) == Constants.INApp_1_MONTH_25 ||
                    SharedPreferenceHelper.getInstance().getInt(Constants.INApp_PACKED) == Constants.INApp_1_MONTH_99 ||
                    SharedPreferenceHelper.getInstance().getInt(Constants.INApp_PACKED) == Constants.INApp_LIFETIME) {
                positionPurchase = 7
            }
        }

        val albumIds = IntArray(positionPurchase)
        for (i in 0 until mAdapterAlbum.mData.size) {
            if (i < positionPurchase) {
                albumIds[i] = mAdapterAlbum.mData[i].id.toInt()
            } else {
                break
            }
        }
        val arrs = database.songDAO().searchTracksByAlbum("%" + edtSearch.text.toString() + "%", albumIds)
        mSong.clear()
        for (item in arrs) {
            mSong.add(item)
        }

        mAdapterSong.notifyDataSetChanged()

        var positionPurchaseAdvanced = 0
        if (SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_ADVANCED)) {
            positionPurchaseAdvanced = mAdapterAlbumAdvanced.mData.size
        }
        val albumAdvancedIds = IntArray(positionPurchaseAdvanced)
        for (i in 0 until mAdapterAlbumAdvanced.mData.size) {
            if (i < positionPurchaseAdvanced) {
                albumAdvancedIds[i] = mAdapterAlbumAdvanced.mData[i].id.toInt()
            } else {
                break
            }
        }
        val listSongAdvanced = database.songDAO().searchTracksByAlbum("%" + edtSearch.text.toString() + "%", albumAdvancedIds)
        mSongAdvanced.clear()
        for (item in listSongAdvanced) {
            mSongAdvanced.add(item)
        }
        mAdapterSongAdvanced.notifyDataSetChanged()
        if (mSongAdvanced.size > 0 || mSong.size > 0) {
            tvNoData.visibility = View.GONE
        } else {
            tvNoData.visibility = View.VISIBLE
        }
    }

    override fun addListener() {
        mAdapterAlbum.setOnItemListener(object : PlaylistAlbumAdapter.IOnItemClicklistener {
            override fun onItemClick(position: Int) {
                if (mAdapterAlbum.mData.get(position).isPurchase) {
                    val parent = parentFragment as BaseFragment
                    parent.addFragment(AlbumDetailFragment.newInstance(mAlbums[position],0), R.id.frame2)
                } else {

                    var isShowDialog = true
                    if (SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED)) {
                        if (SharedPreferenceHelper.getInstance().getInt(Constants.INApp_PACKED) == Constants.INApp_FREE ||
                                SharedPreferenceHelper.getInstance().getInt(Constants.INApp_PACKED) == Constants.INApp_1_MONTH_25 ||
                                SharedPreferenceHelper.getInstance().getInt(Constants.INApp_PACKED) == Constants.INApp_1_MONTH_99 ||
                                SharedPreferenceHelper.getInstance().getInt(Constants.INApp_PACKED) == Constants.INApp_LIFETIME) {
                            if (position >= 7) {
                                isShowDialog = false
                            }
                        }
                    }
                    if (isShowDialog) {
                        if (baseActivity != null) {
                            baseActivity!!.showDialogSubscriptionFS(0)
                        }
                    } else {
                        FilesUtils.showComingSoon(context)
                    }
                }
            }
        })

        mAdapterAlbumAdvanced.setOnItemListener(object : PlaylistAlbumAdapter.IOnItemClicklistener {
            override fun onItemClick(position: Int) {
                if (mAdapterAlbumAdvanced.mData[position].isPurchase) {
                    val parent = parentFragment as BaseFragment
                    parent.addFragment(AlbumDetailFragment.newInstance(mAlbumsAdvanced[position],1), R.id.frame2)
                } else {

                    var isShowDialog = true
                    if (SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_ADVANCED)) {
                        isShowDialog = false
                    }
                    if (isShowDialog) {
                        if (baseActivity != null) {
                            baseActivity!!.showDialogSubscriptionFS(1)
                        }
                    } else {
                        FilesUtils.showComingSoon(context)
                    }
                }
            }
        })

        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (edtSearch.text.isEmpty()) {
                    mHandler.removeCallbacksAndMessages(null)
                    rcPlaylist.adapter = mAdapterAlbum
                    mRcPlaylistAdvanced.adapter = mAdapterAlbumAdvanced
                    tvNoData.text = getString(R.string.txt_no_albums)
                    mSong.clear()
                    mAdapterSong.notifyDataSetChanged()

                    mSongAdvanced.clear()
                    mAdapterAlbumAdvanced.notifyDataSetChanged()
                    if (mAlbums.size > 0 || mAlbumsAdvanced.size > 0) {
                        tvNoData.visibility = View.GONE
                    } else {
                        tvNoData.visibility = View.VISIBLE
                    }
                } else {
                    rcPlaylist.adapter = mAdapterSong
                    mRcPlaylistAdvanced.adapter = mAdapterSongAdvanced
                    mHandler.removeCallbacksAndMessages(null)
                    mHandler.postDelayed(mSearchRunnable, 700)
                    tvNoData.text = getString(R.string.txt_no_songs)
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mContext!!.unregisterReceiver(broadcastReceiverPlaylistAlbumController)
    }
}
