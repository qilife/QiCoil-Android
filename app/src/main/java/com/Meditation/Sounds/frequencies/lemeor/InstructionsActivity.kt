package com.Meditation.Sounds.frequencies.lemeor

import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.base.BaseActivity
import com.shockwave.pdfium.PdfDocument
import kotlinx.android.synthetic.main.activity_instructions.*

class InstructionsActivity : BaseActivity(), OnPageChangeListener, OnLoadCompleteListener{

    override fun initLayout(): Int {
        return R.layout.activity_instructions
    }

    override fun initComponents() {
        mPdfView.fromAsset("qfa_user_guider.pdf")
                .defaultPage(0)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(DefaultScrollHandle(this))
                .load()
    }

    override fun addListener() {
        imvBack.setOnClickListener {
            finish()
        }
    }

    override fun onPageChanged(page: Int, pageCount: Int) {

    }

    override fun loadComplete(nbPages: Int) {
        printBookmarksTree(mPdfView.tableOfContents, "-")
    }

    private fun printBookmarksTree(tree: List<PdfDocument.Bookmark>, sep: String) {
        for (b in tree) {
            if (b.hasChildren()) {
                printBookmarksTree(b.children, "$sep-")
            }
        }
    }
}
