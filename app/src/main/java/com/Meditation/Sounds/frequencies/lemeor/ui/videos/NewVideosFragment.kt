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
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import kotlinx.android.synthetic.main.fragment_new_videos.*
import kotlinx.android.synthetic.main.fragment_new_videos.mvideoView
import kotlinx.android.synthetic.main.fragment_video.*
import org.json.JSONException


class NewVideosFragment : Fragment(), YouTubePlayer.OnInitializedListener {

    private lateinit var mViewModel: NewVideosViewModel
    private var mYouTubePlayer: YouTubePlayer? = null
    private var mVideoAdapter: VideoAdapter? = null
    private var mListVideo = ArrayList<Video>()
    private var mPlaylistAdapter: PlaylistAdapter? = null
    private var mListPlaylist = ArrayList<Playlist>()

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

        if (Utils.isConnectedToNetwork(activity) && isAdded){
            if (mListVideo.isNotEmpty()) {
                val videoId = mListVideo[0].videoId
                val videoStr = """<html><body style='margin:0;padding:0;'><iframe class="youtube-player" style="border: 0; width: 100%; height: 96%;padding:0px; margin:0px" id="ytplayer" type="text/html" src="http://www.youtube.com/embed/$videoId?&theme=dark&autohide=2&modestbranding=1&showinfo=0&autoplay=1s=0" frameborder="0" allowfullscreen autobuffer controls onclick="this.play()"></iframe></body></html>"""
                mvideoView?.setWebViewClient(object : WebViewClient() {
                    @Deprecated("Deprecated in Java", ReplaceWith("false"))
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        return false
                    }
                })
                val ws: WebSettings = mvideoView.settings
                ws.javaScriptEnabled = true
                ws.mediaPlaybackRequiresUserGesture = false
                mvideoView?.loadData(videoStr, "text/html", "utf-8")
            }

        }

        mVideoAdapter = VideoAdapter(context, mListVideo)
        mVideoAdapter?.setOnClickListener(object : VideoAdapter.IOnClickItemListener {
            override fun onClickItem(video: Video, position: Int) {
                val videoId = video.videoId
                val videoStr = """<html><body style='margin:0;padding:0;'><iframe class="youtube-player" style="border: 0; width: 100%; height: 96%;padding:0px; margin:0px" id="ytplayer" type="text/html" src="http://www.youtube.com/embed/$videoId?&theme=dark&autohide=2&modestbranding=1&showinfo=0&autoplay=1s=0" frameborder="0" allowfullscreen autobuffer controls onclick="this.play()"></iframe></body></html>"""
                mvideoView.setWebViewClient(object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        return false
                    }
                })
                val ws: WebSettings = mvideoView.getSettings()
                ws.javaScriptEnabled = true
                ws.mediaPlaybackRequiresUserGesture = false
                mvideoView.loadData(videoStr, "text/html", "utf-8")
            }
        })
        videos_recycler_view.adapter = mVideoAdapter
    }

    override fun onInitializationSuccess(provider: YouTubePlayer.Provider?, player: YouTubePlayer?, wasRestored: Boolean) {
        mYouTubePlayer = player
        mYouTubePlayer?.setShowFullscreenButton(false)
        mYouTubePlayer?.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT)
        if (mListVideo.isNotEmpty()) {
            mYouTubePlayer?.cueVideo(mListVideo[0].videoId)
        }
    }

    override fun onInitializationFailure(provider: YouTubePlayer.Provider?, result: YouTubeInitializationResult?) {
        if (result?.isUserRecoverableError == true) {
            result.getErrorDialog(activity, RECOVERY_DIALOG_REQUEST).show()
        } else {
            Toast.makeText(requireContext(), getString(R.string.player_error, result.toString()), Toast.LENGTH_LONG).show()
        }
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
                    val videoStr = """<html><body style='margin:0;padding:0;'><iframe class="youtube-player" style="border: 0; width: 100%; height: 96%;padding:0px; margin:0px" id="ytplayer" type="text/html" src="http://www.youtube.com/embed/$videoId?&theme=dark&autohide=2&modestbranding=1&showinfo=0&autoplay=1s=0" frameborder="0" allowfullscreen autobuffer controls onclick="this.play()"></iframe></body></html>"""
                    mvideoView.setWebViewClient(object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                            return false
                        }
                    })
                    val ws: WebSettings = mvideoView.getSettings()
                    ws.javaScriptEnabled = true
                    ws.mediaPlaybackRequiresUserGesture = false
                    mvideoView.loadData(videoStr, "text/html", "utf-8")
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