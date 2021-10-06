package com.Meditation.Sounds.frequencies.lemeor.ui.main


import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.BuildConfig
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.api.ApiListener
import com.Meditation.Sounds.frequencies.api.models.GetFlashSaleOutput
import com.Meditation.Sounds.frequencies.feature.discover.DiscoverFragment
import com.Meditation.Sounds.frequencies.lemeor.*
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Program
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.Resource
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.isFirstSync
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.isHighQuantum
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.isInnerCircle
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.isLogged
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.isShowDisclaimer
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.preference
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.DownloadInfo
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.DownloadService
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.DownloaderActivity
import com.Meditation.Sounds.frequencies.lemeor.tools.player.PlayerUIFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.AdvActivity
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail.NewAlbumDetailFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.search.AlbumsSearchAdapter
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.search.ProgramsSearchAdapter
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.search.TracksSearchAdapter
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs.CategoriesPagerFragment.CategoriesPagerListener
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs.TiersPagerFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs.TiersPagerFragment.OnTiersFragmentListener
import com.Meditation.Sounds.frequencies.lemeor.ui.auth.AuthActivity
import com.Meditation.Sounds.frequencies.lemeor.ui.auth.updateTier
import com.Meditation.Sounds.frequencies.lemeor.ui.options.NewOptionsFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.NewProgramFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.detail.ProgramDetailFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.purchase.new_flow.NewPurchaseActivity
import com.Meditation.Sounds.frequencies.lemeor.ui.videos.NewVideosFragment
import com.Meditation.Sounds.frequencies.tasks.BaseTask
import com.Meditation.Sounds.frequencies.tasks.GetFlashSaleTask
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.QcAlarmManager
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.views.DisclaimerDialog
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener
import com.google.gson.Gson
import com.suddenh4x.ratingdialog.AppRating
import com.suddenh4x.ratingdialog.buttons.ConfirmButtonClickListener
import com.suddenh4x.ratingdialog.preferences.RatingThreshold
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.android.synthetic.main.activity_navigation.mTvDownloadPercent
import kotlinx.android.synthetic.main.activity_navigation.viewGroupDownload
import kotlinx.android.synthetic.main.fragment_albums_category.*
import kotlinx.android.synthetic.main.fragment_new_options.*
import kotlinx.android.synthetic.main.item_program.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File


const val REQUEST_CODE_PERMISSION = 1111

class NavigationActivity : AppCompatActivity(), OnNavigationItemSelectedListener,
    CategoriesPagerListener, OnTiersFragmentListener, ApiListener<Any> {

    private lateinit var mViewModel: HomeViewModel
    private var playerUI: PlayerUIFragment? = null

    private var mLocalApkPath: String? = null
    private var refId: Long = 0

    //search
    private var mAlbumsSearchAdapter: AlbumsSearchAdapter? = null
    private var mTracksSearchAdapter: TracksSearchAdapter? = null
    private var mProgramsSearchAdapter: ProgramsSearchAdapter? = null

    private var albumsSearch = MutableLiveData<List<Album>>()
    private var tracksSearch = MutableLiveData<List<Track>>()
    private var programsSearch = MutableLiveData<List<Program>>()

    private var mCallbackManager: CallbackManager? = null

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: Any?) {
        if (event?.javaClass == DownloadInfo::class.java) {
            val download = event as DownloadInfo
            mTvDownloadPercent.text = getString(
                R.string.downloader_quantity_collapse,
                download.completed + 1,
                download.total
            )
            viewGroupDownload.visibility = View.VISIBLE
        }

        if (event == DownloadService.DOWNLOAD_FINISH) {
            viewGroupDownload.visibility = View.GONE
            downloadedTracks = null
        }
    }

    private fun quantumOnCreate() {
        if (preference(applicationContext).isFirstSync) {
            createFolder()
        } else {
            hideFolder()
        }

        GlobalScope.launch {
            val apkList = mViewModel.getApkList()

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

                        CoroutineScope(Dispatchers.Main).launch { dialogConfirmUpdateApk(apkUrl) }
                    }
                }
            }
        }

        registerReceiver(
            downloadNewApkReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )

        checkPermissions()
    }

    private fun checkPermissions() {
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
        val root: String = getExternalFilesDir(null).toString()
        val myDir = File("$root/tracks")
        myDir.mkdir()
    }

    private fun hideFolder() {
        val root: String = getExternalFilesDir(null).toString()
        val oldFolder = File("$root/tracks")
        if (oldFolder.exists()) {
            val newFolder = File("$root/.tracks")
            oldFolder.renameTo(newFolder)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        EventBus.getDefault().register(this)

        init()

        syncData()

        initSearch()

        if (!preference(applicationContext).isLogged) {
            startActivity(Intent(applicationContext, AuthActivity::class.java))
        }

        val handler = Handler()
        handler.postDelayed(Runnable {
            // yourMethod();
            val intent = Intent(this, AdvActivity::class.java)
            startActivity(intent)
        }, 5000)


        if (BuildConfig.IS_FREE) {
            quantumOnCreate()
        }

        GetFlashSaleTask(this, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        if (preference(applicationContext).isShowDisclaimer) {
            showDisclaimerDialog()
        }

        album_search?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().trim().isNotEmpty()) {
                    album_search_clear.visibility = View.VISIBLE
                    search(s)
                } else {
                    clearSearch()
                    album_search_clear.visibility = View.GONE
                }
            }
        })

        album_search.onFocusChangeListener = View.OnFocusChangeListener { _, b ->
            if (b) {
                view_data.visibility = View.VISIBLE
            } else {
                view_data.visibility = View.GONE
                hideKeyboard(applicationContext, album_search)
            }
        }

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
    }

    private fun init() {
        mViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(applicationContext).apiService),
                DataBase.getInstance(applicationContext)
            )
        ).get(HomeViewModel::class.java)

        nav_view.setOnNavigationItemSelectedListener(this)

        setFragment(TiersPagerFragment())

        flash_sale.visibility = View.GONE //At the request of the client

        if (BuildConfig.IS_FREE) {
            flash_sale.visibility = View.GONE
            nav_view.menu.removeItem(R.id.navigation_videos)
        }

        viewGroupDownload.setOnClickListener {
            if (downloadedTracks != null) {
                startActivity(DownloaderActivity.newIntent(applicationContext, downloadedTracks!!))
            }
        }


        mCallbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance()
            .registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Log.d("FACEBOOK", "Login onSuccess")
                }

                override fun onCancel() {
                    Log.d("FACEBOOK", "Login onCancel")
                }

                override fun onError(exception: FacebookException) {
                    Log.d("FACEBOOK", "Login onError")
                }
            })
    }

    private fun syncData() {

        mViewModel.home.observe(this, {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    if (preference(applicationContext).isFirstSync) {
                        preference(applicationContext).isFirstSync = true
                        if (it.data == null && !BuildConfig.IS_FREE) {
                            mViewModel.loadFromCache(applicationContext)
                        }
                    }
                }
                Resource.Status.ERROR -> {
                }
                Resource.Status.LOADING -> {
                }
            }
        })
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
        )
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        askRating()
        when (item.itemId) {
            R.id.navigation_albums -> {
                search_layout.visibility = View.VISIBLE
                setFragment(TiersPagerFragment())
                return true
            }
            R.id.navigation_programs -> {
                isTrackAdd = false
                search_layout.visibility = View.VISIBLE
                setFragment(NewProgramFragment())
                return true
            }
            R.id.navigation_videos -> {
                search_layout.visibility = View.VISIBLE
                setFragment(NewVideosFragment())
                return true
            }
            R.id.navigation_discover -> {
                search_layout.visibility = View.GONE
                setFragment(DiscoverFragment())
                return true
            }
            R.id.navigation_options -> {

                search_layout.visibility = View.VISIBLE
                setFragment(NewOptionsFragment())
                return true
            }

        }

        return false
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

    //region SEARCH
    private fun initSearch() {
        //albums search
        mAlbumsSearchAdapter = AlbumsSearchAdapter(applicationContext, ArrayList())
        mAlbumsSearchAdapter!!.setOnClickListener(object : AlbumsSearchAdapter.Listener {
            override fun onAlbumSearchClick(album: Album, i: Int) {
                startAlbumDetails(album)
            }
        })
        search_albums_recycler.adapter = mAlbumsSearchAdapter
        search_albums_recycler.setHasFixedSize(true)
        search_albums_recycler.itemAnimator = null

        //track search
        mTracksSearchAdapter = TracksSearchAdapter(applicationContext, ArrayList())
        mTracksSearchAdapter!!.setOnClickListener(object : TracksSearchAdapter.Listener {
            override fun onTrackSearchClick(track: Track, i: Int) {
                GlobalScope.launch {
                    val album = mViewModel.getAlbumById(track.albumId)
                    CoroutineScope(Dispatchers.Main).launch { album?.let { startAlbumDetails(it) } }
                }
            }
        })
        search_tracks_recycler.adapter = mTracksSearchAdapter
        search_tracks_recycler.setHasFixedSize(true)
        search_tracks_recycler.itemAnimator = null

        //program search
        mProgramsSearchAdapter = ProgramsSearchAdapter(applicationContext, ArrayList())
        mProgramsSearchAdapter!!.setOnClickListener(object : ProgramsSearchAdapter.Listener {
            override fun onProgramSearchClick(program: Program, i: Int) {
                supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(
                        R.anim.trans_right_to_left_in,
                        R.anim.trans_right_to_left_out
                    )
                    .replace(
                        R.id.nav_host_fragment,
                        ProgramDetailFragment.newInstance(program.id),
                        ProgramDetailFragment().javaClass.simpleName
                    )
                    .commit()
            }
        })
        search_programs_recycler.adapter = mProgramsSearchAdapter
        search_programs_recycler.setHasFixedSize(true)
        search_programs_recycler.itemAnimator = null

        albumsSearch.observe(this, {
            val converted = ArrayList<Album>()

            it.forEach { album ->
                if (album.tier_id == 1 || album.tier_id == 2) {
                    converted.add(album)
                } else {
                    if (album.tier_id == 3
                        && preference(applicationContext).isHighQuantum
                    ) {
                        converted.add(album)
                    }
                    if (album.tier_id == 4
                        && preference(applicationContext).isInnerCircle
                    ) {
                        converted.add(album)
                    }
                }
            }
            mAlbumsSearchAdapter?.setData(converted)
            if (converted.size != 0) {
                lblheaderalbums.visibility = View.VISIBLE
                lblnoresult.visibility = View.GONE
            } else {
                lblheaderalbums.visibility = View.GONE
                if (mAlbumsSearchAdapter?.itemCount == 0 && mProgramsSearchAdapter?.itemCount == 0 && mTracksSearchAdapter?.itemCount == 0)
                    lblnoresult.visibility = View.VISIBLE
            }

        })

        tracksSearch.observe(this, {
            val converted = ArrayList<Track>()

            it.forEach { track ->
                if (track.tier_id == 1 || track.tier_id == 2) {
                    converted.add(track)
                } else {
                    if (track.tier_id == 3
                        && preference(applicationContext).isHighQuantum
                    ) {
                        converted.add(track)
                    }
                    if (track.tier_id == 4
                        && preference(applicationContext).isInnerCircle
                    ) {
                        converted.add(track)
                    }
                }
            }
            if (converted.size != 0) {
                lblheaderfrequencies.visibility = View.VISIBLE
                lblnoresult.visibility = View.GONE
            } else {
                lblheaderfrequencies.visibility = View.GONE
                if (mAlbumsSearchAdapter?.itemCount == 0 && mProgramsSearchAdapter?.itemCount == 0 && mTracksSearchAdapter?.itemCount == 0)
                    lblnoresult.visibility = View.VISIBLE
            }
            mTracksSearchAdapter?.setData(converted)
        })

        programsSearch.observe(this, {
            if (it.size != 0) {
                lblheaderprograms.visibility = View.VISIBLE
                lblnoresult.visibility = View.GONE
            } else {
                lblheaderprograms.visibility = View.GONE
                if (mAlbumsSearchAdapter?.itemCount == 0 && mProgramsSearchAdapter?.itemCount == 0 && mTracksSearchAdapter?.itemCount == 0)
                    lblnoresult.visibility = View.VISIBLE
            }
            mProgramsSearchAdapter?.setData(it)
        })
    }

    private fun search(s: CharSequence) {
        GlobalScope.launch {
            val albums = mViewModel.searchAlbum("%$s%")
            val tracks = mViewModel.searchTrack("%$s%")
            val programs = mViewModel.searchProgram("%$s%")
            CoroutineScope(Dispatchers.Main).launch {
                albumsSearch.value = albums
                tracksSearch.value = tracks
                programsSearch.value = programs
            }
        }
    }

    private fun closeSearch() {
        album_search.text = null
        hideKeyboard(applicationContext, album_search)
        album_search.clearFocus()
        lblnoresult.visibility = View.GONE
        lblheaderprograms.visibility = View.GONE
        lblheaderfrequencies.visibility = View.GONE
        lblheaderalbums.visibility = View.GONE
    }

    private fun clearSearch() {
        mAlbumsSearchAdapter?.setData(ArrayList())
        mTracksSearchAdapter?.setData(ArrayList())
        mProgramsSearchAdapter?.setData(ArrayList())
        lblheaderprograms.visibility = View.GONE
        lblheaderfrequencies.visibility = View.GONE
        lblheaderalbums.visibility = View.GONE
        lblnoresult.visibility = View.VISIBLE
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
                    NewAlbumDetailFragment.newInstance(album.id),
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
        } catch (ex: WindowManager.BadTokenException) {
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
        request.setDestinationUri(Uri.fromFile(apkFile))
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
                    Uri.fromFile(File(mLocalApkPath!!)),
                    "application/vnd.android.package-archive"
                )
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivityForResult(intent, REQUEST_CODE_AFTER_INSTALL)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mCallbackManager!!.onActivityResult(requestCode, resultCode, data)
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

            mViewModel.getProfile().observe(this, { user ->
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
            })
        }
    }

    override fun onConnectionOpen(task: BaseTask<*>?) {

    }

    override fun onConnectionSuccess(task: BaseTask<*>?, data: Any?) {
        if (task is GetFlashSaleTask) {
            val jsonFlashSale = data as String
            Log.i("jsonre", "r-->" + jsonFlashSale.toString());
            if (jsonFlashSale != null && jsonFlashSale.length > 0) {
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
                    if (jsonOrgrial != null && jsonOrgrial.flashSale != null) {
                        flashsaleOrgrialString = Gson().toJson(jsonOrgrial.flashSale)
                    }
                }

                SharedPreferenceHelper.getInstance().set(Constants.PREF_FLASH_SALE, jsonFlashSale)

                if (!flashsaleCurrentString.equals(flashsaleOrgrialString, ignoreCase = true)) {
                    SharedPreferenceHelper.getInstance()
                        .setInt(Constants.PREF_FLASH_SALE_COUNTERED, 0)
                    loadDs()
                }

                fetchInterstitialDs()

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

    fun loadDs() {
        val jsonOrgrialString = SharedPreferenceHelper.getInstance().get(Constants.PREF_FLASH_SALE)
        if (jsonOrgrialString != null && jsonOrgrialString.length > 0) {
            val flashSaleOutput = Gson().fromJson<GetFlashSaleOutput>(
                jsonOrgrialString,
                GetFlashSaleOutput::class.java!!
            )

            if (flashSaleOutput.advertisements != null && flashSaleOutput.advertisements.enableBanner!! && (this is NavigationActivity)) {

            }
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
                var hour: String = if (hours > 9) "" + hours else "0$hours"
                var min: String = if (mins > 9) "" + mins else "0$mins"
                var second: String = if (secs > 9) "" + secs else "0$secs"
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
                    flash_sale.visibility = View.VISIBLE
                }
//                mTvDurationCountDown.text = "$hour:$min:$second"
                flash_sale_hours.text = "$hour"
                flash_sale_minutes.text = "$min"
                flash_sale_seconds.text = "$second"
            }

            override fun onFinish() {
                flash_sale.visibility = View.GONE
//                initComponents()
            }
        }
        mCountDownTimer!!.start()
    }


    fun fetchInterstitialDs() {
        val jsonOrgrialString = SharedPreferenceHelper.getInstance().get(Constants.PREF_FLASH_SALE)
        if (jsonOrgrialString != null && jsonOrgrialString.length > 0) {
            val flashSaleOutput = Gson().fromJson<GetFlashSaleOutput>(
                jsonOrgrialString,
                GetFlashSaleOutput::class.java!!
            )
            if (flashSaleOutput != null && flashSaleOutput.advertisements != null && flashSaleOutput.advertisements.enable!!) {
                if (!SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED)) {

                }
            }
        }
    }

    fun askRating() {
        AppRating.Builder(this)
            .setMinimumLaunchTimes(9)
            .setMinimumDays(3)
            .setMinimumLaunchTimesToShowAgain(9)
            .setMinimumDaysToShowAgain(3)
            .setRatingThreshold(RatingThreshold.FOUR)
            .setConfirmButtonClickListener(object : ConfirmButtonClickListener {
                override fun onClick(userRating: Float) {
                    AppRating.openPlayStoreListing(this@NavigationActivity)
                }
            })
            .showIfMeetsConditions()
    }


}