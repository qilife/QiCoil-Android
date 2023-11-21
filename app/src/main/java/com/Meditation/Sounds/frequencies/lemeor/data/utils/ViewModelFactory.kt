package com.Meditation.Sounds.frequencies.lemeor.data.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.DownloaderRepository
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.DownloaderViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail.AlbumDetailRepository
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail.NewAlbumDetailViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs.AlbumsRepository
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs.AlbumsViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.auth.AuthRepository
import com.Meditation.Sounds.frequencies.lemeor.ui.auth.AuthViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.main.HomeRepository
import com.Meditation.Sounds.frequencies.lemeor.ui.main.HomeViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.options.NewOptionsRepository
import com.Meditation.Sounds.frequencies.lemeor.ui.options.NewOptionsViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.options.change_pass.ChangePassRepository
import com.Meditation.Sounds.frequencies.lemeor.ui.options.change_pass.ChangePassViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.NewProgramViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.ProgramRepository
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.detail.ProgramDetailRepository
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.detail.ProgramDetailViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.rife.NewRifeViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.rife.RifeRepository
import com.Meditation.Sounds.frequencies.lemeor.ui.rife.SearchRifeViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.rife.tabs.FrequencyViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.videos.NewVideosViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.videos.VideoRepository

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val apiHelper: ApiHelper, private val localData: DataBase) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(HomeRepository(apiHelper, localData),localData) as T
        }

        if (modelClass.isAssignableFrom(AlbumsViewModel::class.java)) {
            return AlbumsViewModel(AlbumsRepository(apiHelper,localData)) as T
        }

        if (modelClass.isAssignableFrom(NewAlbumDetailViewModel::class.java)) {
            return NewAlbumDetailViewModel(AlbumDetailRepository(localData)) as T
        }

        if (modelClass.isAssignableFrom(DownloaderViewModel::class.java)) {
            return DownloaderViewModel(DownloaderRepository(localData)) as T
        }

        if (modelClass.isAssignableFrom(NewVideosViewModel::class.java)) {
            return NewVideosViewModel(VideoRepository(localData)) as T
        }

        if (modelClass.isAssignableFrom(NewProgramViewModel::class.java)) {
            return NewProgramViewModel(ProgramRepository(localData,apiHelper)) as T
        }

        if (modelClass.isAssignableFrom(ProgramDetailViewModel::class.java)) {
            return ProgramDetailViewModel(ProgramDetailRepository(localData)) as T
        }

        if (modelClass.isAssignableFrom(NewOptionsViewModel::class.java)) {
            return NewOptionsViewModel(NewOptionsRepository(apiHelper, localData)) as T
        }

        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(AuthRepository(apiHelper, localData)) as T
        }

        if (modelClass.isAssignableFrom(ChangePassViewModel::class.java)) {
            return ChangePassViewModel(ChangePassRepository(apiHelper)) as T
        }

        if (modelClass.isAssignableFrom(NewRifeViewModel::class.java)) {
            return NewRifeViewModel(RifeRepository(apiHelper, localData)) as T
        }
        if (modelClass.isAssignableFrom(SearchRifeViewModel::class.java)) {
            return SearchRifeViewModel(RifeRepository(apiHelper, localData)) as T
        }
        if (modelClass.isAssignableFrom(FrequencyViewModel::class.java)) {
            return FrequencyViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}