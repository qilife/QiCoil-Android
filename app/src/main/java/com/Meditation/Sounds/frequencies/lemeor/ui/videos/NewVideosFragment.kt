package com.Meditation.Sounds.frequencies.lemeor.ui.videos

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.MediaController
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.video.VideoAdapter
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Playlist
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.models.Video
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.Utils
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import kotlinx.android.synthetic.main.fragment_new_videos.*
import kotlinx.android.synthetic.main.fragment_new_videos.mvideoView
import kotlinx.android.synthetic.main.fragment_video.*
import org.json.JSONException

class NewVideosFragment : Fragment() {

    private lateinit var mViewModel: NewVideosViewModel
    private var mVideoAdapter: VideoAdapter? = null
    private var mListVideo = ArrayList<Video>()
    private var mPlaylistAdapter: PlaylistAdapter? = null
    private var mListPlaylist = ArrayList<Playlist>()
    private var mYouTubePlayer: YouTubePlayer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_new_videos, container, false)
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("SetJavaScriptEnabled")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mViewModel = ViewModelProvider(this,
                ViewModelFactory(
                        ApiHelper(RetrofitBuilder(requireContext()).apiService),
                        DataBase.getInstance(requireContext()))
        ).get(NewVideosViewModel::class.java)

        mPlaylistAdapter = PlaylistAdapter(requireContext(), mListPlaylist)
        mPlaylistAdapter!!.setOnClickListener(object : PlaylistAdapter.Listener {
            override fun onClickItem(playlist: Playlist, i: Int) {
                mPlaylistAdapter!!.setSelected(i)
                getJsonPlaylist(getString(R.string.video_url, playlist.youtube_id, API_KEY+Constants.API_KEY))
            }
        })
        playlist_recycler_view.adapter = mPlaylistAdapter

        mViewModel.playlists?.observe(viewLifecycleOwner) {
            mPlaylistAdapter?.setData(it)

            if (it.isNotEmpty()) {
                getJsonPlaylist(
                    getString(
                        R.string.video_url,
                        it[0].youtube_id,
                        API_KEY + Constants.API_KEY
                    )
                )
            }
        }

        //todo check info about support version
       /* val youtubeFragment = YouTubePlayerSupportFragment.newInstance()
        childFragmentManager.beginTransaction().add(R.id.videos_youtube_player, youtubeFragment).commit()
        youtubeFragment.initialize(API_KEY, this@NewVideosFragment)*/
        Log.i("newvideo", "video");

        val mediaController = MediaController(activity)
      //  mediaController.setAnchorView(mvideoView)
       // mvideoView.setMediaController(mediaController)

        mvideoView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady( youTubePlayer: YouTubePlayer) {
                try {
                    if (Utils.isConnectedToNetwork(activity) && isAdded) {
                        mYouTubePlayer = youTubePlayer
                        youTubePlayer.cueVideo(mListVideo[0].videoId, 0f)
                    }
                }catch (_:Throwable){}

            }
        })

        mVideoAdapter = VideoAdapter(context, mListVideo)
        mVideoAdapter?.setOnClickListener(object : VideoAdapter.IOnClickItemListener {
            override fun onClickItem(video: Video, position: Int) {
                val videoId = video.videoId
                mYouTubePlayer?.loadVideo(videoId, 0f)
            }
        })
        videos_recycler_view.adapter = mVideoAdapter
    }


    //todo make this pretty
    private fun getJsonPlaylist(url: String) {
        if (!Utils.isConnectedToNetwork(requireContext()) || !isAdded) { return }

        val requestQueue = Volley.newRequestQueue(requireContext())
        val jsonObject = JsonObjectRequest(Request.Method.GET, url, null, { response ->
            try {
                mListVideo.clear()
                val jsonItems = response.getJSONArray("items")
                var title: String
                var urlImage: String
                var idVideo: String
                for (i in 0 until jsonItems.length()) {
                    val jsonItem = jsonItems.getJSONObject(i)
                    val jsonSnippet = jsonItem.getJSONObject("snippet")
                    title = jsonSnippet.getString("title")
                    if (!title.equals("Private video", true) && !title.equals("Deleted video", true)) {
                        val jsonThumbnail = jsonSnippet.getJSONObject("thumbnails")
                        val jsonImageMedium = jsonThumbnail.getJSONObject("medium")
                        urlImage = jsonImageMedium.getString("url")
                        val jsonResourceId = jsonSnippet.getJSONObject("resourceId")
                        idVideo = jsonResourceId.getString("videoId")

                        mListVideo.add(Video(idVideo, title, urlImage))
                    }
                }
                mVideoAdapter?.setListVideo(mListVideo)
                if (mListVideo.isNotEmpty() && mvideoView != null) {
                    //  val videoStr = "<html><body><iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/"+mListVideo[0].videoId+"\\frameborder=\"0\" allowfullscreen></iframe></body></html>"

                    val videoId = mListVideo[0].videoId;
                    mYouTubePlayer?.cueVideo(videoId, 0f)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
        }) { error -> if (isAdded) Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show() }

        requestQueue.add(jsonObject)
    }

    override fun onDetach() {
        super.onDetach()
        //mvideoView?.rele()
       // mYouTubePlayer = null
    }

    companion object {
        const val RECOVERY_DIALOG_REQUEST = 1
        const val API_KEY = "AIzaSyB7gg_gAT4mg7d"
    }
}
