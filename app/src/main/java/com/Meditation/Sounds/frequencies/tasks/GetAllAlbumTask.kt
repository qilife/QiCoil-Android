package com.Meditation.Sounds.frequencies.tasks

import android.content.Context
import com.Meditation.Sounds.frequencies.FileEncyptUtil
import com.Meditation.Sounds.frequencies.api.ApiListener
import com.Meditation.Sounds.frequencies.db.QFDatabase
import com.Meditation.Sounds.frequencies.models.Album
import com.google.gson.Gson

class GetAllAlbumTask(var context: Context, listener: ApiListener<Any>) : BaseTask<Any>(context, listener) {
    override fun callApiMethod(): Any {
        val tokenOutput = mApi.token
        mApi.setCredentials("Bearer ".plus(tokenOutput.token))
        val albumMaster = mApi.albums.albums
        val albumAdvanced = mApi.albumsAdvanced.albums
        val albumHigher = mApi.albumsHigherAbundance.albums
        val albumHigherQuanTum = mApi.albumsHigherQuantum.albums

        val albums = ArrayList<Album>()
        albums.addAll(albumMaster)
        albums.addAll(albumAdvanced)
        albums.addAll(albumHigher)
        albums.addAll(albumHigherQuanTum)
        for (item in albums) {
            val description = convertJsonToString(item.listDescription)
            QFDatabase.getDatabase(context).albumDAO().updateDescriptionAlbum(
                    item.name, description)
        }

        FileEncyptUtil.setDescription(albumMaster, albumAdvanced, albumHigher, albumHigherQuanTum)
        return albums
    }

    private fun convertJsonToString(data: ArrayList<String>): String {
        val gson = Gson()
        return gson.toJson(data)
    }
}
