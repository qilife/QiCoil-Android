package com.Meditation.Sounds.frequencies.feature.discover

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_discover.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


/**
 * A simple [Fragment] subclass.
 * Use the [DiscoverFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DiscoverFragment : BaseFragment()
{

    override fun initLayout(): Int {
        return R.layout.fragment_discover
    }

    override fun initComponents() {
        webview_discover.settings.setJavaScriptEnabled(true)

        webview_discover.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url.toString())
                return true
            }
        }
       // webview_discover.loadUrl("https://qilifestore.com/collections/qi-coils")
        webview_discover.loadUrl("https://qilifestore.com/collections/qi-coils?utm_source=app&utm_medium=android&utm_campaign=qicoilapp&utm_id=qicoilappandroid")
    }

    override fun addListener() {
    }
}