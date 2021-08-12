package com.Meditation.Sounds.frequencies.tasks

import android.content.Context
import android.util.Log
import com.Meditation.Sounds.frequencies.api.ApiListener
import com.Meditation.Sounds.frequencies.api.TaskApi
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

/**
 * Created by dcmen on 13-Apr-17.
 */
class GetFlashSaleTask(context: Context, listener: ApiListener<Any>) : BaseTask<Any>(context, listener) {

    @Throws(Exception::class)
    override fun callApiMethod(): Any {
        //Get new data
        var connection: HttpURLConnection? = null
        var reader: BufferedReader? = null

        try {
            val url = URL(TaskApi.TASK_WS_PLASH_SALE)
            Log.i("jsonre","r-->"+url.toString());
            connection = url.openConnection() as HttpURLConnection
            connection!!.connect()
            val stream = connection!!.getInputStream()
            reader = BufferedReader(InputStreamReader(stream))
            val buffer = StringBuffer()
            var line = reader!!.readLine()
            while (line != null) {
                buffer.append(line + "\n")
                line = reader!!.readLine()
            }
            return buffer.toString()
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (connection != null) {
                connection!!.disconnect()
            }
            try {
                if (reader != null) {
                    reader!!.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return ""

    }

}
