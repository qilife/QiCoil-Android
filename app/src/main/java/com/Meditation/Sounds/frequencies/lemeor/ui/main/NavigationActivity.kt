package com.Meditation.Sounds.frequencies.lemeor.ui.main


import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.Meditation.Sounds.frequencies.BuildConfig
import com.Meditation.Sounds.frequencies.QApplication
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.api.ApiListener
import com.Meditation.Sounds.frequencies.api.models.GetFlashSaleOutput
import com.Meditation.Sounds.frequencies.feature.discover.DiscoverFragment
import com.Meditation.Sounds.frequencies.lemeor.*
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.*
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.Resource
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.isFirstSync
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.isHighQuantum
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.isInnerCircle
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.isLogged
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.isShowDisclaimer
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.preference
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.*
import com.Meditation.Sounds.frequencies.lemeor.tools.player.PlayerService
import com.Meditation.Sounds.frequencies.lemeor.tools.player.PlayerUIFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail.NewAlbumDetailFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.search.SearchAdapter
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs.CategoriesPagerFragment.CategoriesPagerListener
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs.TiersPagerFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs.TiersPagerFragment.OnTiersFragmentListener
import com.Meditation.Sounds.frequencies.lemeor.ui.auth.AuthActivity
import com.Meditation.Sounds.frequencies.lemeor.ui.auth.updateTier
import com.Meditation.Sounds.frequencies.lemeor.ui.options.NewOptionsFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.NewProgramFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.NewProgramViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.detail.ProgramDetailFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.purchase.new_flow.NewPurchaseActivity
import com.Meditation.Sounds.frequencies.lemeor.ui.rife.NewRifeFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.rife.NewRifeViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.videos.NewVideosFragment
import com.Meditation.Sounds.frequencies.models.event.SyncDataEvent
import com.Meditation.Sounds.frequencies.tasks.BaseTask
import com.Meditation.Sounds.frequencies.tasks.GetFlashSaleTask
import com.Meditation.Sounds.frequencies.utils.*
import com.Meditation.Sounds.frequencies.utils.CopyAssets.copyAssetFolder
import com.Meditation.Sounds.frequencies.views.DisclaimerDialog
import com.google.gson.Gson
import com.tonyodev.fetch2core.isNetworkAvailable
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.HttpException
import java.io.File


const val REQUEST_CODE_PERMISSION = 1111

class NavigationActivity : AppCompatActivity(),
    CategoriesPagerListener, OnTiersFragmentListener, ApiListener<Any> {
    private var mViewGroupCurrent: View? = null
    private lateinit var mViewModel: HomeViewModel
    private lateinit var mNewProgramViewModel: NewProgramViewModel
    private lateinit var mNewRifeViewModel: NewRifeViewModel
    private var playerUI: PlayerUIFragment? = null

    private var mLocalApkPath: String? = null
    private var refId: Long = 0

    //search
    private val searchAdapter by lazy {
        SearchAdapter { item, i ->
            hideKeyboard(this@NavigationActivity, album_search)
            view_data.visibility = View.GONE
            if (item.obj is Album) {
                val album = item.obj as Album
                startAlbumDetails(album)
            } else if (item.obj is Track) {
                val track = item.obj as Track
                CoroutineScope(Dispatchers.IO).launch {
                    val album = mViewModel.getAlbumById(track.albumId, track.category_id)
                    withContext(Dispatchers.Main) {
                        view_data.visibility = View.GONE
                        album?.let { startAlbumDetails(it) }
                    }
                }
            } else if (item.obj is Program) {
                val program = item.obj as Program
                if (program.isUnlocked) {
                    if (isTrackAdd && trackIdForProgram != Constants.defaultHz - 1) {
                        val db = DataBase.getInstance(this@NavigationActivity)
                        val programDao = db.programDao()

                        CoroutineScope(Dispatchers.IO).launch {
                            val p = programDao.getProgramById(program.id)
                            p?.records?.add((trackIdForProgram ?: 0).toDouble())
                            p?.let { it1 ->
                                programDao.updateProgram(it1)
                                if (it1.user_id.isNotEmpty()) {
                                    try {
                                        mNewProgramViewModel.updateTrackToProgram(
                                            UpdateTrack(
                                                track_id = listOf(
                                                    (trackIdForProgram ?: 0.0).toDouble()
                                                ),
                                                id = it1.id,
                                                track_type = if ((trackIdForProgram
                                                        ?: 0.0).toDouble() >= 0
                                                ) "mp3" else "rife",
                                                request_type = "add",
                                                is_favorite = (it1.name.uppercase() == FAVORITES.uppercase() && it1.favorited)
                                            )
                                        )
                                    } catch (_: Exception) {
                                    }
                                }
                            }
                        }
                    }

                    supportFragmentManager.beginTransaction().setCustomAnimations(
                        R.anim.trans_right_to_left_in,
                        R.anim.trans_right_to_left_out,
                        R.anim.trans_left_to_right_in,
                        R.anim.trans_left_to_right_out
                    ).replace(
                        R.id.nav_host_fragment,
                        ProgramDetailFragment.newInstance(program.id),
                        ProgramDetailFragment().javaClass.simpleName
                    ).commit()
                } else {
                    startActivity(
                        NewPurchaseActivity.newIntent(
                            this@NavigationActivity,
                            NewPurchaseActivity.QUANTUM_TIER_ID,
                            NewPurchaseActivity.QUANTUM_TIER_ID,
                            1
                        )
                    )
                }
            } else if (item.obj is Rife) {
                val rife = item.obj as Rife
                supportFragmentManager.beginTransaction().setCustomAnimations(
                        R.anim.trans_right_to_left_in,
                        R.anim.trans_right_to_left_out,
                        R.anim.trans_left_to_right_in,
                        R.anim.trans_left_to_right_out
                    ).replace(
                        R.id.nav_host_fragment,
                        NewAlbumDetailFragment.newInstance(0, 0, Constants.TYPE_RIFE, rife),
                        NewAlbumDetailFragment().javaClass.simpleName
                    ).commit()
            }
        }
    }

    private var albumsSearch = MutableLiveData<List<Album>>()
    private var tracksSearch = MutableLiveData<List<Track>>()
    private var programsSearch = MutableLiveData<List<Program>>()

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEvent(event: Any?) {
        runOnUiThread {
            if (event is DownloadInfo && event.total > 0) {
                mTvDownloadPercent.text = getString(
                    R.string.downloader_quantity_collapse,
                    event.completed,
                    event.total
                )
                if (event.completed < event.total) {
                    viewGroupDownload.visibility = View.VISIBLE
                }
            }

            if (event is DownloadErrorEvent && !QApplication.isActivityDownloadStarted) {
                AlertDialog.Builder(this@NavigationActivity)
                    .setTitle(R.string.download_error)
                    .setMessage(getString(R.string.download_error_message))
                    .setPositiveButton(R.string.txt_ok, null).show()
            }

            if (event == DownloadService.DOWNLOAD_FINISH) {
                findViewById<View>(R.id.viewGroupDownload).visibility = View.GONE
            }

            if (event is DownloadTrackErrorEvent) {
                if (isNetworkAvailable()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            mViewModel.reportTrack(event.id, event.url)
                        } catch (_: HttpException) {
                        }

                    }
                }
            }

            if (event == SyncDataEvent) {
                syncData()
            }
        }
    }

    private fun quantumOnCreate() {
        if (preference(applicationContext).isFirstSync) {
            createFolder()
        } else {
            hideFolder()
        }

        if (isNetworkAvailable()) {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    var apkList = listOf<String>()
                    try {
                        apkList = mViewModel.getApkList()
                    } catch (_: Exception) {
                    }
                    if (apkList.isNotEmpty()) {
                        val currentVer = BuildConfig.VERSION_NAME
                        val apkUrl = apkList[0]

                        val pathSplit = apkUrl.split("/")

                        if (pathSplit.isNotEmpty()) {
                            val fileName = pathSplit[pathSplit.size - 1]
                            val newVersion = fileName.replace("Quantum_v", "").replace(".apk", "")
                            val newVs = newVersion.split(".")
                            val currentVs = currentVer.split(".")

                            if (newVs.size == 3 && currentVs.size == 3) {
                                if ((newVs[0].toInt() * 100 + newVs[1].toInt() * 10 + newVs[2].toInt())
                                    > currentVs[0].toInt() * 100 + currentVs[1].toInt() * 10 + currentVs[2].toInt()
                                ) {

                                    CoroutineScope(Dispatchers.Main).launch {
                                        dialogConfirmUpdateApk(
                                            apkUrl
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) {

            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(
                    downloadNewApkReceiver,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    Context.RECEIVER_EXPORTED
                )
            }else{
                registerReceiver(
                    downloadNewApkReceiver,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                )
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(
                    this,
                    READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(
                    this,
                    READ_MEDIA_VIDEO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_AUDIO, READ_MEDIA_VIDEO),
                    REQUEST_CODE_PERMISSION
                )
            } else {
                deleteOldFiles()
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE_PERMISSION
                )
            } else {
                deleteOldFiles()
            }
        }
    }

    private fun deleteOldFiles() {
        //delete old files
        val oldFolder = File("/storage/emulated/0/.QuantumConsoleFrequenciesV3")
        val oldFolder1 = File("/storage/emulated/0/.QuantumConsoleFrequenciesHigherV3")
        val oldFolder2 = File("/storage/emulated/0/.QuantumConsoleFrequenciesInnerV3")

        oldFolder.deleteRecursively()
        oldFolder1.deleteRecursively()
        oldFolder2.deleteRecursively()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "denied", Toast.LENGTH_SHORT).show()
            } else {
                deleteOldFiles()
            }
        }
    }

    private fun createFolder() {
        if (BuildConfig.IS_FREE) {
            val root: String = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString()
            val myDir = File("$root/tracks")
            myDir.mkdir()
        } else {
            val root: String = getExternalFilesDir(null).toString()
            val myDir = File("$root/tracks")
            myDir.mkdir()
        }
    }

    private fun hideFolder() {
        if (BuildConfig.IS_FREE) {
            val root: String = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString()
            val oldFolder = File("$root/tracks")
            if (oldFolder.exists()) {
                val newFolder = File("$root/.tracks")
                oldFolder.renameTo(newFolder)
            }
        } else {
            val root: String = getExternalFilesDir(null).toString()
            val oldFolder = File("$root/tracks")
            if (oldFolder.exists()) {
                val newFolder = File("$root/.tracks")
                oldFolder.renameTo(newFolder)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (BuildConfig.IS_FREE) {
            quantumOnCreate()
        }
    }

    @OptIn(FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        EventBus.getDefault().register(this)

        checkPermissions()

        init()

        syncData()

        initSearch()

        getAllCategories()

        if (BuildConfig.IS_FREE) {
            assets.copyAssetFolder(
                "tracks",
                getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + File.separator + "tracks"
            )
        }

        if (!preference(applicationContext).isLogged) {
            startActivity(Intent(applicationContext, AuthActivity::class.java))
        }

        if (preference(applicationContext).isShowDisclaimer) {
            showDisclaimerDialog()
        }

        lifecycleScope.launch(Dispatchers.Main) {
            FlowSearch.fromSearchView(album_search).debounce(500).map { text -> text.trim() }
                .distinctUntilChanged().flowOn(Dispatchers.IO).collect { result ->
                    run {
                        if (result.isNotEmpty()) {
                            album_search_clear.visibility = View.VISIBLE
                            view_data.visibility = View.VISIBLE
                            search(result)
                        } else {
                            clearSearch()
                            album_search_clear.visibility = View.GONE
                            view_data.visibility = View.GONE
                            hideKeyboard(applicationContext, album_search)
                        }
                    }
                }
        }

//        album_search.onFocusChangeListener = View.OnFocusChangeListener { _, b ->
//            if (b) {
//                view_data.visibility = View.VISIBLE
//            } else {
//                view_data.visibility = View.GONE
//                hideKeyboard(applicationContext, album_search)
//            }
//        }

        album_search_clear.setOnClickListener { closeSearch() }

        orientationChangesUI(resources.configuration.orientation)

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        orientationChangesUI(newConfig.orientation)
    }

    private fun orientationChangesUI(orientation: Int) {
        val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(0, 0)
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.weight = 2.0f
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            params.weight = 0.0f
        }

        search_divider.layoutParams = params
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        DownloadService.stopService(this)
        stopService(Intent(this, PlayerService::class.java))
    }

    private fun init() {
        mViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(applicationContext).apiService),
                DataBase.getInstance(applicationContext)
            )
        )[HomeViewModel::class.java]

        mNewProgramViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(this).apiService), DataBase.getInstance(this)
            )
        )[NewProgramViewModel::class.java]

        mNewRifeViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(this).apiService), DataBase.getInstance(this)
            )
        )[NewRifeViewModel::class.java]

        navigation_albums.onSelected {
            closeSearch()
            search_layout.visibility = View.VISIBLE
            setFragment(TiersPagerFragment())
        }

        flash_sale.visibility = View.GONE //At the request of the client


        viewGroupDownload.setOnClickListener {
            startActivity(
                DownloaderActivity.newIntent(
                    applicationContext,
                )
            )
        }
        onButtonNavigationSelected()
    }


    private fun syncData() {
        if (isNetworkAvailable()) {
            try {
                val user = PreferenceHelper.getUser(this)
                if(user?.id != null){
                    mViewModel.syncProgramsToServer()
                }
                mViewModel.getHome("" + user?.id).observe(this) {
                    when (it.status) {
                        Resource.Status.SUCCESS -> {
                            if (preference(applicationContext).isFirstSync) {
                                preference(applicationContext).isFirstSync = true
                                if (it.data == null && !BuildConfig.IS_FREE) {
                                    try {
                                        mViewModel.loadFromCache(applicationContext)
                                    } catch (_: Exception) {
                                    }
                                }
                            }
                        }

                        Resource.Status.ERROR -> {
//                        if (BuildConfig.IS_FREE) {
//                            mViewModel.loadDataLastHomeResponse(this@NavigationActivity)
//                        }
                        }

                        Resource.Status.LOADING -> {
                        }
                    }
                }
                if (user?.id != null) {
                    mViewModel.getRife().observe(this) {
                        mNewRifeViewModel.getRifeLocal()
                    }
                }
            }catch (_:Exception){}
        } else {
//            if (BuildConfig.IS_FREE) {
//                mViewModel.loadDataLastHomeResponse(this@NavigationActivity)
//            }
        }

    }

    private fun showDisclaimerDialog() {
        val mDisclaimerDialog = DisclaimerDialog(
            this@NavigationActivity,
            true,
            object : DisclaimerDialog.IOnSubmitListener {
                override fun submit(isCheck: Boolean) {
                    if (isCheck) {
                        preference(applicationContext).isShowDisclaimer = false
                    }
                }
            })
        mDisclaimerDialog.show()
    }

    private fun setFragment(fragment: Fragment) {
        selectedNaviFragment = fragment

        album_search.clearFocus()

        supportFragmentManager.beginTransaction().replace(
            R.id.nav_host_fragment,
            fragment,
            fragment.javaClass.simpleName
        ).commit()
    }

    @Suppress("UNSAFE_CALL_ON_PARTIALLY_DEFINED_RESOURCE")
    private fun onButtonNavigationSelected() {
//        askRating()
//        hideKeyboard(applicationContext, album_search)
        navigation_albums.setOnClickListener {
            navigation_albums.onSelected {
                closeSearch()
                search_layout.visibility = View.VISIBLE
                setFragment(TiersPagerFragment())
            }
        }
        navigation_rife.setOnClickListener {
            navigation_rife.onSelected {
                closeSearch()
                isTrackAdd = false
                if (Utils.isTablet(this)) {
                    search_layout.visibility = View.VISIBLE
                } else {
                    search_layout.visibility = View.GONE
                }
                setFragment(NewRifeFragment())
            }
        }
        navigation_programs.setOnClickListener {
            navigation_programs.onSelected {
                closeSearch()
                isTrackAdd = false
                search_layout.visibility = View.VISIBLE
                setFragment(NewProgramFragment())
            }
        }
        navigation_videos.setOnClickListener {
            navigation_videos.onSelected {
                closeSearch()
                search_layout.visibility = View.VISIBLE
                setFragment(NewVideosFragment())
            }
        }
        navigation_discover.setOnClickListener {
            navigation_discover.onSelected {
                closeSearch()
                search_layout.visibility = View.VISIBLE
                setFragment(DiscoverFragment())
            }
        }
        navigation_options.setOnClickListener {
            navigation_options.onSelected {
                closeSearch()
                search_layout.visibility = View.VISIBLE
                setFragment(NewOptionsFragment())
            }
        }
    }

    fun showPlayerUI() {
        if (playerUI == null) {
            playerUI = PlayerUIFragment()

            supportFragmentManager
                .beginTransaction()
                .add(R.id.player_ui_container, playerUI!!, playerUI!!.javaClass.simpleName)
                .commitNow()
        }
    }

    fun hidePlayerUI() {
        playerUI?.let {
            supportFragmentManager
                .beginTransaction()
                .remove(it)
                .commitNow()
        }
        playerUI = null
    }


    private fun View.onSelected(listener: () -> Unit) {
        if (mViewGroupCurrent != this) {
            mViewGroupCurrent?.isSelected = false
            mViewGroupCurrent = this
            mViewGroupCurrent?.isSelected = true
            listener.invoke()
        }
    }

    //region SEARCH
    private fun initSearch() {
        search_categories_recycler.apply {
            adapter = searchAdapter
            itemAnimator = null
        }
        Combined4LiveData(
            albumsSearch,
            tracksSearch,
            programsSearch,
            mNewRifeViewModel.result,
            combine = { data1, data2, data3, data4 ->
                val search = mutableListOf<Search>()
                var i = 0
//                Frequencies
                data2?.let {
                    val converted = ArrayList<Track>()
                    it.forEach { track ->
                        if (track.tier_id == 1 || track.tier_id == 2) {
                            converted.add(track)
                            search.add(Search(i, track))
                            i++
                        } else {
                            if (track.tier_id == 3 && (preference(applicationContext).isHighQuantum || BuildConfig.IS_FREE)) {
                                converted.add(track)
                                search.add(Search(i, track))
                                i++
                            }
                            if (track.tier_id == 4 && (preference(applicationContext).isInnerCircle || BuildConfig.IS_FREE)) {
                                converted.add(track)
                                search.add(Search(i, track))
                                i++
                            }
                        }
                    }
                }
//                Albums
                data1?.let {
                    val converted = ArrayList<Album>()
                    if (searchAdapter.getCategories().isEmpty()) {
                        getAllCategories()
                    }
                    if (BuildConfig.IS_FREE) {
                        it.forEach { album ->
                            search.add(Search(i, album))
                            i++
                        }
                        converted.addAll(it)
                    } else {
                        it.forEach { album ->
                            if (album.tier_id == 1 || album.tier_id == 2) {
                                converted.add(album)
                                search.add(Search(i, album))
                                i++
                            } else {
                                if (album.tier_id == 3 && (preference(applicationContext).isHighQuantum)) {
                                    converted.add(album)
                                    search.add(Search(i, album))
                                    i++
                                }
                                if (album.tier_id == 4 && (preference(applicationContext).isInnerCircle)) {
                                    converted.add(album)
                                    search.add(Search(i, album))
                                    i++
                                }
                            }
                        }
                    }
                    var groupAlbum = converted.groupingBy { it.name }.eachCount()
                    searchAdapter.setGroupAlbum(groupAlbum)
                }
//                Programs
                data3?.let {
                    it.forEach { program ->
                        search.add(Search(i, program))
                        i++
                    }
                }
//                Rife
                data4?.let {
                    it.forEach { rife ->
                        search.add(Search(i, rife))
                        i++
                    }
                }

                return@Combined4LiveData search
            }).observe(this) {
            if (it.isEmpty()) {
                lblnoresult.visibility = View.VISIBLE
            } else {
                search_categories_recycler.scrollToPosition(0)
                lblnoresult.visibility = View.GONE
            }
            searchAdapter.submitList(it)
        }
    }

    private fun search(s: CharSequence) {
        CoroutineScope(Dispatchers.IO).launch {
            val albums = mViewModel.searchAlbum("%$s%")
            val tracks = mViewModel.searchTrack("%$s%")
            val programs = mViewModel.searchProgram("%$s%")
            withContext(Dispatchers.Main) {
                mNewRifeViewModel.searchMain(s.toString())
            }
            CoroutineScope(Dispatchers.Main).launch {
                albumsSearch.value = albums
                tracksSearch.value = tracks
                programsSearch.value = programs
            }
        }
    }

    private fun getAllCategories(){
        val categoryDao = DataBase.getInstance(applicationContext).categoryDao()
        GlobalScope.launch {
            val categories = categoryDao.getData()
            withContext(Dispatchers.Main) {
                searchAdapter.setCategories(categories)
            }
        }
    }

    private fun closeSearch() {
        album_search.text.clear()
        album_search.clearFocus()
        hideKeyboard(applicationContext, album_search)
    }

    private fun clearSearch() {
        searchAdapter.submitList(arrayListOf())
    }

    private fun startAlbumDetails(album: Album) {
        if (album.isUnlocked) {
            supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(
                    R.anim.trans_right_to_left_in,
                    R.anim.trans_right_to_left_out,
                    R.anim.trans_left_to_right_in,
                    R.anim.trans_left_to_right_out
                )
                .replace(
                    R.id.nav_host_fragment,
                    NewAlbumDetailFragment.newInstance(album.id, album.category_id),
                    NewAlbumDetailFragment().javaClass.simpleName
                )
                .commit()
        } else {
            startActivity(
                NewPurchaseActivity.newIntent(
                    applicationContext,
                    album.category_id,
                    album.tier_id,
                    album.id
                )
            )
        }
    }

    override fun onAlbumDetails(album: Album) {
        startAlbumDetails(album)
    }

    override fun onLongAlbumDetails(album: Album) {
        val user = PreferenceHelper.getUser(this@NavigationActivity)
        if (user != null && (user.email == "kristenaizalapina@gmail.com" || user.email == "tester02@yopmail.com" || user.email == "manufacturing@qilifestore.com")) {
            val track = album.tracks.first()
            androidx.appcompat.app.AlertDialog.Builder(this@NavigationActivity)
                .setTitle(R.string.app_name)
                .setMessage(
                    getSaveDir(
                        this@NavigationActivity,
                        track.filename,
                        track.album?.audio_folder ?: ""
                    )
                )
                .setPositiveButton(R.string.txt_ok, null).show()
        }
    }
//endregion

    private fun dialogConfirmUpdateApk(apkUrl: String) {
        try {
            AlertDialog.Builder(this)
                .setTitle(R.string.txt_warning_update_newversion_title)
                .setMessage(R.string.txt_warning_update_newversion)
                .setCancelable(false)
                .setPositiveButton(R.string.txt_agree) { _, _ ->
                    downloadAPK(apkUrl)
                }.setNegativeButton(R.string.txt_disagree) { _, _ -> }.show()
        } catch (_: Throwable) {
        }
    }

    private fun deleteAPKFolder() {
        val apkFile = File(getExternalFilesDir(null).toString() + "/.cache/", "cache.apk")
        if (apkFile.exists()) {
            apkFile.delete()
        }
    }

    private fun downloadAPK(apkUrl: String) {
        val apkFile = File(getExternalFilesDir(null).toString() + "/.cache/", "cache.apk")
        if (apkFile.exists()) {
            apkFile.delete()
        }
        mLocalApkPath = apkFile.path

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(apkUrl))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setAllowedOverRoaming(false)
        request.setTitle("[New APK] Quantum Frequencies")
        request.setDestinationUri(FileProvider.getUriForFile(applicationContext,getString(R.string.authorities), apkFile))
        refId = downloadManager.enqueue(request)

        Toast.makeText(
            applicationContext,
            getString(R.string.txt_downloading_dot),
            Toast.LENGTH_LONG
        ).show()
    }

    private val downloadNewApkReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            val referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (refId == referenceId) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.toast_download_complete),
                    Toast.LENGTH_LONG
                ).show()

                autoInstallNewAPK()
                unregisterReceiver(this)
            }
        }
    }

    private fun autoInstallNewAPK() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                startActivityForResult(
                    Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                        .setData(Uri.parse(String.format("package:%s", packageName))),
                    REQUEST_CODE_BEFORE_INSTALL
                )
                return
            }
        }

        if (mLocalApkPath != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val install = Intent(Intent.ACTION_INSTALL_PACKAGE)
                install.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                val apkUri =
                    FileProvider.getUriForFile(this, "$packageName.provider", File(mLocalApkPath!!))
                install.data = apkUri
                startActivityForResult(install, REQUEST_CODE_AFTER_INSTALL)
            } else {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(
                    FileProvider.getUriForFile(applicationContext,getString(R.string.authorities), File(mLocalApkPath!!)),
                    "application/vnd.android.package-archive"
                )
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivityForResult(intent, REQUEST_CODE_AFTER_INSTALL)
            }
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_BEFORE_INSTALL -> autoInstallNewAPK()
            REQUEST_CODE_AFTER_INSTALL -> deleteAPKFolder()
        }
    }

    companion object {
        private const val REQUEST_CODE_BEFORE_INSTALL: Int = 1234
        private const val REQUEST_CODE_AFTER_INSTALL: Int = 5678
    }
//endregion

    override fun onRefreshTiers() {
        if (BuildConfig.IS_FREE) {
            if (isNetworkAvailable()) {
                mViewModel.getProfile().observe(this) { user ->
                    user?.let { resource ->
                        when (resource.status) {
                            Resource.Status.SUCCESS -> {
                                user.data?.let { u -> updateTier(applicationContext, u) }
                            }

                            Resource.Status.ERROR -> {
                            }

                            Resource.Status.LOADING -> {
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onConnectionOpen(task: BaseTask<*>?) {

    }

    override fun onConnectionSuccess(task: BaseTask<*>?, data: Any?) {
        if (task is GetFlashSaleTask) {
            val jsonFlashSale = data as String
            if (jsonFlashSale.isNotEmpty()) {
                val jsonCurrent = Gson().fromJson(jsonFlashSale, GetFlashSaleOutput::class.java)

//                Luon hien flash sale
//                jsonCurrent.flashSale.enable = true
//                jsonFlashSale = Gson().toJson(jsonCurrent)

                var flashsaleCurrentString = ""
                if (jsonCurrent != null && jsonCurrent.flashSale != null) {
                    flashsaleCurrentString = Gson().toJson(jsonCurrent.flashSale)
                }
                val jsonOrgrialString =
                    SharedPreferenceHelper.getInstance().get(Constants.PREF_FLASH_SALE)
                var flashsaleOrgrialString = ""
                if (jsonOrgrialString != null) {
                    val jsonOrgrial =
                        Gson().fromJson(jsonOrgrialString, GetFlashSaleOutput::class.java)
                    if (jsonOrgrial?.flashSale != null) {
                        flashsaleOrgrialString = Gson().toJson(jsonOrgrial.flashSale)
                    }
                }

                SharedPreferenceHelper.getInstance().set(Constants.PREF_FLASH_SALE, jsonFlashSale)

                if (!flashsaleCurrentString.equals(flashsaleOrgrialString, ignoreCase = true)) {
                    SharedPreferenceHelper.getInstance()
                        .setInt(Constants.PREF_FLASH_SALE_COUNTERED, 0)
                }

                if (SharedPreferenceHelper.getInstance()
                        .getInt(Constants.PREF_FLASH_SALE_COUNTERED) <= jsonCurrent.flashSale.proposalsCount!!
                ) {
                    QcAlarmManager.createAlarms(this)
                } else {
                    QcAlarmManager.clearAlarms(this)
                }

                //Create reminder
                QcAlarmManager.createReminderAlarm(this)

                loadCountdownTime()
            } else {
                QcAlarmManager.clearAlarms(this)
            }
        }
    }

    override fun onConnectionError(task: BaseTask<*>?, exception: Exception?) {

    }

    fun loadCountdownTime() {
        val flashSaleRemainTimeGloble = Utils.getFlashSaleRemainTime()
        if (flashSaleRemainTimeGloble > 0) {
            setCountdownTimer(flashSaleRemainTimeGloble)
        } else {
            flash_sale.visibility = View.GONE
        }
    }

    private var mCountDownTimer: CountDownTimer? = null

    private fun setCountdownTimer(totalTime: Long) {
        if (mCountDownTimer != null) {
            mCountDownTimer!!.cancel()
        }
        mCountDownTimer = object : CountDownTimer(totalTime, 1000) {
            override fun onTick(l: Long) {
                val totalSeconds = (l / 1000).toInt()
                val days = totalSeconds / (24 * 3600)
                var remainder = totalSeconds - (days * 24 * 3600)
                val hours = remainder / 3600
                remainder -= (hours * 3600)
                val mins = remainder / 60
                remainder -= mins * 60
                val secs = remainder
                val hour: String = if (hours > 9) "" + hours else "0$hours"
                val min: String = if (mins > 9) "" + mins else "0$mins"
                val second: String = if (secs > 9) "" + secs else "0$secs"
                if (SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED)
                    && SharedPreferenceHelper.getInstance()
                        .getBool(Constants.KEY_PURCHASED_ADVANCED)
                    && SharedPreferenceHelper.getInstance()
                        .getBool(Constants.KEY_PURCHASED_HIGH_ABUNDANCE)
                    && SharedPreferenceHelper.getInstance()
                        .getBool(Constants.KEY_PURCHASED_HIGH_QUANTUM)
                ) {
                    flash_sale.visibility = View.GONE
                } else {
                    flash_sale.visibility = View.GONE
                }
                flash_sale_hours.text = hour
                flash_sale_minutes.text = min
                flash_sale_seconds.text = second
            }

            override fun onFinish() {
                flash_sale.visibility = View.GONE
//                initComponents()
            }
        }
        mCountDownTimer!!.start()
    }


//    fun askRating() {
//        AppRating.Builder(this)
//            .setMinimumLaunchTimes(9)
//            .setMinimumDays(3)
//            .setMinimumLaunchTimesToShowAgain(9)
//            .setMinimumDaysToShowAgain(3)
//            .setRatingThreshold(RatingThreshold.FOUR)
//            .setConfirmButtonClickListener(ConfirmButtonClickListener {
//                AppRating.openPlayStoreListing(
//                    this@NavigationActivity
//                )
//            })
//            .showIfMeetsConditions()
//    }

}