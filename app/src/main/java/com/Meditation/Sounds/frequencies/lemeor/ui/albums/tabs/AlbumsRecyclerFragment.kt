package com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.ui.purchase.new_flow.NewPurchaseActivity
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.views.ItemOffsetDecoration
import kotlinx.android.synthetic.main.fragment_albums_category.*

class AlbumsRecyclerFragment : Fragment() {

    interface AlbumsRecyclerListener {
        fun onStartAlbumDetail(album: Album)
    }

    private var mListener: AlbumsRecyclerListener? = null
    private lateinit var mViewModel: AlbumsViewModel
    private var mAlbumAdapter: AlbumsAdapter? = null
    private var mListAlbum = ArrayList<Album>()

    private var categoryId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryId = arguments?.getInt(ARG_SECTION_NUMBER)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_albums_category, container, false)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            albums_recycler_view.layoutManager = GridLayoutManager(context, 3)
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            albums_recycler_view.layoutManager = GridLayoutManager(context, 2)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mViewModel = ViewModelProvider(this,
                ViewModelFactory(
                        ApiHelper(RetrofitBuilder(requireContext()).apiService),
                        DataBase.getInstance(requireContext()))
        ).get(AlbumsViewModel::class.java)

        mViewModel.albums(categoryId!!)?.observe(viewLifecycleOwner, {
            mAlbumAdapter!!.setData(it)
        })

        mAlbumAdapter = AlbumsAdapter(requireContext(), mListAlbum)
        mAlbumAdapter!!.setOnClickListener(object : AlbumsAdapter.Listener {
            override fun onClickItem(album: Album) {
                startAlbumDetails(album)
            }
        })

        albums_recycler_view.setHasFixedSize(true)
        if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            albums_recycler_view.layoutManager = GridLayoutManager(context, 3)
        } else if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT){
            albums_recycler_view.layoutManager = GridLayoutManager(context, 2)
        }
        albums_recycler_view.adapter = mAlbumAdapter

        val itemDecoration = ItemOffsetDecoration(requireContext(),
                if (Utils.isTablet(requireContext())) R.dimen.margin_buttons else R.dimen.item_offset)
        albums_recycler_view.addItemDecoration(itemDecoration)
    }

    fun startAlbumDetails(album: Album) {
        if (album.isUnlocked) {
            mListener?.onStartAlbumDetail(album)
        } else {
            startActivity(NewPurchaseActivity.newIntent(requireContext(), album.category_id, album.tier_id))
        }
    }

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"
        @JvmStatic
        fun newInstance(sectionNumber: Int, listener: AlbumsRecyclerListener): AlbumsRecyclerFragment {
            return AlbumsRecyclerFragment().apply {
                mListener = listener
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}