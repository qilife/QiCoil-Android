package com.Meditation.Sounds.frequencies.feature.album.detail

import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.db.QFDatabase
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.feature.playlist.detail.PlaylistDetailFragment
import com.Meditation.Sounds.frequencies.models.Album
import com.Meditation.Sounds.frequencies.models.PlaylistItem

/**
 * Created by DC-MEN on 8/18/2018.
 */
class AlbumDetailGroupFragment : BaseFragment() {
    private lateinit var mAlbum: Album
    private lateinit var mViewModel: AlbumsDetailViewModel
    private var fromItemSearch = false
    private var playlistId: Long = 0
    private var mTypeAlbum: Int = 0

    override fun initLayout(): Int {
        return R.layout.fragment_album_group
    }

    override fun initComponents() {
        if (fromItemSearch) {
            val database = QFDatabase.getDatabase(mContext!!.applicationContext)
            val playLists = database.playlistDAO().getAll()
            if (playlistId >= 0) {
                for (item in playLists) {
                    if (playlistId == item.id) {
                        replaceFragment(PlaylistDetailFragment.newInstance(item, PlaylistItem(), false, false), R.id.frame2)
                    }
                }
            }
        } else {
//            mViewModel = ViewModelProviders.of(this).get(AlbumsDetailViewModel::class.java)
//            replaceFragment(PlaylistDetailFragment.newInstance(mViewModel.getFirstModifiedPlaylist().blockingGet()), R.id.frame1)
//            replaceFragment(PlaylistAlbumFragment(), R.id.frame1)
            addFragment(AlbumDetailFragment.newInstance(mAlbum, mTypeAlbum), R.id.frame2)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun addListener() {

    }

    companion object {
        @JvmStatic
        fun newInstance(album: Album, typeAlbum: Int, playlistId: Long, fromSearch: Boolean): AlbumDetailGroupFragment {
            val fragment = AlbumDetailGroupFragment()
            fragment.mAlbum = album
            fragment.playlistId = playlistId
            fragment.fromItemSearch = fromSearch
            fragment.mTypeAlbum = typeAlbum
            return fragment
        }
    }
}
