package com.Meditation.Sounds.frequencies

import android.content.Context
import android.net.Uri
import android.util.Log
import com.Meditation.Sounds.frequencies.models.Album
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.FilesUtils
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import com.Meditation.Sounds.frequencies.utils.StringsUtils
import com.google.android.exoplayer2.upstream.DataSink
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.crypto.AesCipherDataSink
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import java.io.*
import kotlin.experimental.xor

object FileEncyptUtil {

    private fun getByte(paramArrayOfByte: ByteArray, encodeString: String): ByteArray {
        val encodeByteArray = encodeString.toByteArray()
        val arrayOfByte = paramArrayOfByte.clone()
        val byteToReplace = if (arrayOfByte.size < encodeByteArray.size)
            arrayOfByte.size
        else
            encodeByteArray.size
        for (i in 0 until byteToReplace) {
            arrayOfByte[i] = (arrayOfByte[i] xor encodeByteArray[i])
        }

        return arrayOfByte
    }

    fun changeAccessFile(filePath: String, encodeString: String): Boolean {
        try {
            val file = File(filePath)
            val localRandomAccessFile = RandomAccessFile(
                    file,
                    "rw"
            )
            val numByte = Math.min(128L, file.length())
            val arrayOfByte = ByteArray(numByte.toInt())
            localRandomAccessFile.read(arrayOfByte, 0, arrayOfByte.size)
            localRandomAccessFile.seek(0L)
            if (localRandomAccessFile.length() > arrayOfByte.size) {
                localRandomAccessFile.write(getByte(arrayOfByte, encodeString))
            }
            localRandomAccessFile.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun encryptFile(sourceFile: File, secretKey: String): String {
        try {
            var pathWithoutExtension = StringsUtils.getFileNameWithoutExtension(sourceFile.path)
            var desFile = File(pathWithoutExtension + "." + Constants.EXTENSION_ENCRYPT_FILE)

            val inputStream = FileInputStream(sourceFile)
            var inputStreamBytes = Util.toByteArray(inputStream)
            inputStream.close()
            val encryptingDataSink = AesCipherDataSink(
                    Util.getUtf8Bytes(secretKey),
                    object : DataSink {
                        private var fileOutputStream: FileOutputStream? = null
                        @Throws(IOException::class)
                        override fun open(dataSpec: DataSpec) {
                            fileOutputStream = FileOutputStream(desFile)
                        }

                        @Throws(IOException::class)
                        override fun write(buffer: ByteArray, offset: Int, length: Int) {
                            fileOutputStream!!.write(buffer, offset, length)
                        }

                        @Throws(IOException::class)
                        override fun close() {
                            fileOutputStream!!.close()
                        }
                    })
            encryptingDataSink.open(DataSpec(Uri.fromFile(desFile)))
            encryptingDataSink.write(inputStreamBytes, 0, inputStreamBytes.size)
            encryptingDataSink.close()
//            inputStreamBytes = null
            sourceFile.delete()
            return desFile.path
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return sourceFile.path
    }

    fun renameToMp3File(sourceFile: String): String {
        var deviceId = SharedPreferenceHelper.getInstance()[Constants.KEY_DEVICE_ID]
        var pathWithoutExtension = StringsUtils.getFileNameWithoutExtension(sourceFile)
        var fromFile = File(sourceFile)
        var toFile = File(pathWithoutExtension + "." + Constants.EXTENSION_MP3_FILE)
        if (toFile.exists()) {
            return toFile.path
        }
        if (sourceFile.endsWith(Constants.EXTENSION_ENCRYPT_FILE, ignoreCase = true)) {
//            changeAccessFile(sourceFile, deviceId)
            if (fromFile.exists()) {
                fromFile.renameTo(toFile)
                return toFile.path
            }
        } else if (sourceFile.endsWith(Constants.EXTENSION_MP3_FILE, ignoreCase = true)) {
            var sourceFileMp3 = pathWithoutExtension + "." + Constants.EXTENSION_ENCRYPT_FILE
            fromFile = File(sourceFileMp3)
            if (fromFile.exists()) {
                fromFile.renameTo(toFile)
                return toFile.path
            }
        }
        return sourceFile
    }

    fun renameToEncryptFile(sourceFile: String): String {
        var deviceId = SharedPreferenceHelper.getInstance()[Constants.KEY_DEVICE_ID]

        var pathWithoutExtension = StringsUtils.getFileNameWithoutExtension(sourceFile)
        var fromFile = File(sourceFile)
        var toFile = File(pathWithoutExtension + "." + Constants.EXTENSION_ENCRYPT_FILE)
        if (toFile.exists()) {
            return toFile.path
        }
        if (sourceFile.endsWith(Constants.EXTENSION_MP3_FILE, ignoreCase = true)) {
//            changeAccessFile(sourceFile, deviceId)
            if (fromFile.exists()) {
                fromFile.renameTo(toFile)
                return toFile.path
            }
        } else if (sourceFile.endsWith(Constants.EXTENSION_ENCRYPT_FILE, ignoreCase = true)) {
            var sourceFileMp3 = pathWithoutExtension + "." + Constants.EXTENSION_MP3_FILE
            fromFile = File(sourceFileMp3)
            if (fromFile.exists()) {
                fromFile.renameTo(toFile)
                return toFile.path
            }
        }
        return sourceFile
    }

    fun saveJsonToFile(context: Context, file: File, jsonString: String) {
        try {
            val fileWriter = FileWriter(file, false)
            fileWriter.write(jsonString)
            fileWriter.flush()
            fileWriter.close()
        } catch (e: IOException) {
            Log.e("TAG", "Error in Writing: " + e.localizedMessage)
        }
    }

    fun getJsonFromFile(context: Context, file: File): String? {
        try {
            val fileInputStream = FileInputStream(file)
            val size = fileInputStream.available()
            val buffer = ByteArray(size)
            fileInputStream.read(buffer)
            fileInputStream.close()
            return String(buffer)
        } catch (e: IOException) {
            Log.e("TAG", "Error in Reading: " + e.localizedMessage)
            return null
        }

    }

    fun deleteFile(path: String) {
        val file = File(path)
        if (file.exists()) {
            val deleteCmd = "rm -r $path"
            val runtime = Runtime.getRuntime()
            try {
                runtime.exec(deleteCmd)
            } catch (e: IOException) {
            }
        }
        if (file.exists()) {
            file.delete()
        }
    }

    fun countMp3Files(): Int {
        val CACHE_FOLDER = File(FilesUtils.getSdcardStore(), Constants.DEFAULT_DATA_FOLDER)
        val CACHE_FOLDER_ADVANCED = File(FilesUtils.getSdcardStore(), Constants.DEFAULT_DATA_ADVANCED_FOLDER)
        val CACHE_FOLDER_ABUNDANCE = File(FilesUtils.getSdcardStore(), Constants.DEFAULT_DATA_ABUNDANCE_FOLDER)
        val CACHE_FOLDER_HIGHER_QUANTUM = File(FilesUtils.getSdcardStore(), Constants.DEFAULT_DATA_HIGHER_QUANTUM_FOLDER)

        if (!CACHE_FOLDER.exists()) {
            CACHE_FOLDER.mkdir()
        }
        val dataFolders = CACHE_FOLDER.listFiles()

        if (!CACHE_FOLDER_ADVANCED.exists()) {
            CACHE_FOLDER_ADVANCED.mkdir()
        }
        val dataFoldersAdvanced = CACHE_FOLDER_ADVANCED.listFiles()

        if (!CACHE_FOLDER_ABUNDANCE.exists()) {
            CACHE_FOLDER_ABUNDANCE.mkdir()
        }
        val dataFoldersHigherAbundance = CACHE_FOLDER_ABUNDANCE.listFiles()

        if (!CACHE_FOLDER_HIGHER_QUANTUM.exists()) {
            CACHE_FOLDER_HIGHER_QUANTUM.mkdir()
        }
        val dataFoldersHigherQuantum = CACHE_FOLDER_HIGHER_QUANTUM.listFiles()
        var count: Int = 0
        if ((dataFolders != null && dataFolders.isNotEmpty()) || (dataFoldersAdvanced != null && dataFoldersAdvanced.isNotEmpty())
                || dataFoldersHigherAbundance?.isNotEmpty() == true || dataFoldersHigherQuantum?.isNotEmpty() == true
        ) {
            val listFolders = ArrayList<File>()
            listFolders.add(CACHE_FOLDER)
            listFolders.add(CACHE_FOLDER_ADVANCED)
            listFolders.add(CACHE_FOLDER_ABUNDANCE)
            listFolders.add(CACHE_FOLDER_HIGHER_QUANTUM)

            for (folders in listFolders) {
                for (albumFolder in folders.listFiles()) {
                    if (albumFolder.isDirectory) {
                        for (songFile in albumFolder.listFiles()) {
                            if (songFile.path.endsWith("." + Constants.EXTENSION_MP3_FILE, ignoreCase = true)) {
                                count++
                            }
                        }
                    }
                }
            }
        }
        return count
    }

    var master = ArrayList<Album>()
    var advanced = ArrayList<Album>()
    var higherAbundance = ArrayList<Album>()
    var higherQuantum = ArrayList<Album>()

    fun setDescription(master: ArrayList<Album>, advanced: ArrayList<Album>
                       , higherAbundance: ArrayList<Album>, higherQuantum: ArrayList<Album>) {
        this.master = master
        this.advanced = advanced
        this.higherAbundance = higherAbundance
        this.higherQuantum = higherQuantum
    }

    fun getDescription(album: Album):ArrayList<String>{
        val description: ArrayList<String>
        val benefits = album.benefits
        val gs= Gson()
        description = gs.fromJson<ArrayList<String>>(benefits, ArrayList::class.java)
        return description
    }
}
