package com.Meditation.Sounds.frequencies.lemeor.ui.purchase.new_flow

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.album.detail.DescriptionAdapter
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.loadImage
import kotlinx.android.synthetic.main.fragment_purchase_album.*

private const val ARG_ALBUM = "arg_album"

class PurchaseAlbumFragment : Fragment() {

    private var mAlbum: Album? = null
    private var mDescriptionAdapter: DescriptionAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mAlbum = it.getParcelable(ARG_ALBUM)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_purchase_album, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        purchase_album_image.radius = resources.getDimensionPixelOffset(R.dimen.corner_radius_album)
        mAlbum?.let { loadImage(requireContext(), purchase_album_image, it) }

        mDescriptionAdapter = mAlbum?.descriptions?.let { DescriptionAdapter(requireContext(), it) }
        purchase_album_description_recycler.adapter = mDescriptionAdapter
    }

    companion object {
        @JvmStatic
        fun newInstance(album: Album) =
                PurchaseAlbumFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_ALBUM, album)
                    }
                }
    }
}