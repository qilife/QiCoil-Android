package com.Meditation.Sounds.frequencies

import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.crypto.AesCipherDataSource
import com.google.android.exoplayer2.util.Util

class EncryptedFileDataSourceFactory(var dataSource: DataSource) : DataSource.Factory {

    override fun createDataSource(): DataSource {
        return AesCipherDataSource(Util.getUtf8Bytes(SharedPreferenceHelper.getInstance()[Constants.KEY_DEVICE_ID]), dataSource)
    }
}