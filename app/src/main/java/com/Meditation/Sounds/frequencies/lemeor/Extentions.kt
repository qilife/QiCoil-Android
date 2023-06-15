package com.Meditation.Sounds.frequencies.lemeor

import android.content.Context
import android.os.Build
import android.os.Environment
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.Meditation.Sounds.frequencies.BuildConfig
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.api.exception.ApiException
import com.Meditation.Sounds.frequencies.lemeor.data.api.ApiConfig
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper
import com.Meditation.Sounds.frequencies.lemeor.tools.player.MusicRepository
import com.Meditation.Sounds.frequencies.views.CustomFontEditText
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URL
import java.util.*


const val FAVORITES = "Favorites"

var trackList: ArrayList<MusicRepository.Track>? = null
var downloadedTracks: ArrayList<Track>? = null

val currentTrack = MutableLiveData<MusicRepository.Track>()
val currentTrackIndex = MutableLiveData<Int>()
val currentPosition = MutableLiveData<Long>()
val max = MutableLiveData<Long>()
val duration = MutableLiveData<Long>()

//for track add to program
var trackIdForProgram: Int? = -1
var albumIdBackProgram: Int? = -1
var isTrackAdd: Boolean = false
var positionFor: Int? = -1

var isPlayAlbum: Boolean = false
var playAlbumId: Int = -1
var isPlayProgram: Boolean = false
var playProgramId: Int = -1

var isMultiPlay: Boolean = false

var tierPosition: Int = 0

var hashMapTiers: HashMap<Int, Int> = HashMap()
var selectedNaviFragment: Fragment? = null

var isUserPaused = false

fun loadImage(context: Context, imageView: ImageView, album: Album) {
    val assetsPath = "file:///android_asset/albums/" + album.image

    val requestOptions = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .signature(ObjectKey(album.updated_at))
    Glide.with(context)
        .load(getImageUrl(album))
        .thumbnail(Glide.with(context).load(assetsPath).apply(RequestOptions().override(300, 300)))
        .apply(requestOptions)
        .dontTransform()
        .dontAnimate()
        .placeholder(R.drawable.ic_album_placeholder)
        .into(imageView)
}

fun getImageUrl(album: Album): String {
    return ApiConfig.getStorage() +
            File.separator +
            album.audio_folder +
            File.separator +
            album.image
}

fun getTrackUrl(album: Album?, track: Track): String {
    val trackUrl = ApiConfig.getStorage() +
            File.separator +
            album?.audio_folder +
            File.separator +
            track.filename

    val url = URL(trackUrl)
    val uri = URI(url.protocol, url.userInfo, url.host, url.port, url.path, url.query, url.ref)

    return uri.toASCIIString()
}

@RequiresApi(Build.VERSION_CODES.KITKAT)
fun getPreloadedSaveDir(context: Context, track: Track, album: Album): String {

    if (BuildConfig.IS_FREE) {
        return context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() +
                File.separator +
                "tracks" +
                File.separator +
                album.audio_folder +
                File.separator +
                track.filename.replace("%", "")
    } else {
        return context.getExternalFilesDir(null).toString() +
                File.separator +
                ".tracks" +
                File.separator +
                album.audio_folder +
                File.separator +
                track.filename.replace("%", "")
    }
}

@RequiresApi(Build.VERSION_CODES.KITKAT)
fun getSaveDir(context: Context, track: Track, album: Album): String {
//    val oldFolder = File( context.getExternalFilesDir(
//        Environment.DIRECTORY_DOCUMENTS).toString(), "tracks")
//    val newFolder = File(context.getExternalFilesDir(
//        Environment.DIRECTORY_DOCUMENTS).toString(), ".tracks")
//    val success = oldFolder.renameTo(newFolder)
    if (BuildConfig.IS_FREE) {
        return context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() +
                File.separator +
                ".tracks" +
                File.separator +
                album.audio_folder +
                File.separator +
                track.filename.replace("%", "")
    } else {
        return context.getExternalFilesDir(null).toString() +
                File.separator +
                ".tracks" +
                File.separator +
                album.audio_folder +
                File.separator +
                track.filename.replace("%", "")
    }
}

fun getConvertedTime(millis: Long): String {
    //Log.i("milisecond","s-->"+millis);
    val second: Long = millis / 1000 % 60
    val minute: Long = millis / (1000 * 60) % 60
    val hour: Long = millis / (1000 * 60 * 60) % 24

    return if (hour > 0) {
        String.format("%02d:%02d:%02d", hour, minute, second)
    } else {
        String.format("%02d:%02d", minute, second)
    }
}

fun showAlert(context: Context, content: String) {
    AlertDialog.Builder(context)
        .setMessage(content)
        .setPositiveButton(R.string.txt_ok, null)
        .show()
}

fun showAlertInfo(context: Context, e: Exception) {
    when (e) {
        is ApiException -> showAlert(
            context,
            context.getString(R.string.err_unexpected_exception_api)
        )
        is IOException -> showAlert(context, context.getString(R.string.err_network_available))
        else -> showAlert(context, context.getString(R.string.err_unexpected_exception))
    }
}

fun convertedTrackName(album: Album, track: Track): String {
    return track.name.replace(album.name, "")
        .replace(" - ", "")
        .replace("Life Force the Source of Qi ", "")
}

fun isFlashSale(context: Context): Boolean {
    return PreferenceHelper.getFlashSaleTime(context) > 0
}

fun hideKeyboard(context: Context, view: CustomFontEditText) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

