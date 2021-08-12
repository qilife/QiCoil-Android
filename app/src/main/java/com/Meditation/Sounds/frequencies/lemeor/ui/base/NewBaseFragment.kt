package com.Meditation.Sounds.frequencies.lemeor.ui.base

import androidx.fragment.app.Fragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

open class NewBaseFragment : Fragment() {

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: String?) { }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}