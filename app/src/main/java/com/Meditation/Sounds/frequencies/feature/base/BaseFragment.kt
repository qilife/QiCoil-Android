package com.Meditation.Sounds.frequencies.feature.base

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.Meditation.Sounds.frequencies.R



/**
 * Created by Admin on 3/22/2017.
 */

abstract class BaseFragment : Fragment() {
    protected var mFragment: Fragment? = null
    protected var mView: View? = null
    protected var mViewId: Int = 0
    protected var mContext: Context? = null
    private var mProgressDialog: ProgressDialog? = null
    protected abstract fun initLayout(): Int

    protected abstract fun initComponents()

    protected abstract fun addListener()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        if(mView == null) {
            val layoutId = initLayout()
            if (layoutId != 0) {
                mViewId = layoutId
            }
            mView = LayoutInflater.from(activity).inflate(mViewId, container, false)
//        }
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mProgressDialog = ProgressDialog(mContext)
        mProgressDialog!!.setCancelable(false)
        mProgressDialog!!.setMessage(getString(R.string.txt_waiting))

        initComponents()
        addListener()
    }

    fun showLoading(isShow: Boolean) {
        try {
            if (isShow) {
                mProgressDialog!!.show()
            } else {
                if (mProgressDialog!!.isShowing) {
                    mProgressDialog!!.dismiss()
                }
            }
        } catch (_: IllegalArgumentException) {
        }

    }

    fun setNewPage(fragment: Fragment, containerId : Int) {
        try {
            if (childFragmentManager.backStackEntryCount > 0) {
                for (i in 0 until childFragmentManager.backStackEntryCount) {
                    childFragmentManager.popBackStackImmediate()
                }
            }
            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(containerId, fragment, "currentFragment")
            transaction.commitAllowingStateLoss()
            mFragment?.let{
                transaction.remove(it)
            }
            mFragment = fragment

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun addFragment(fragment: Fragment, containerId : Int) {
        childFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.trans_right_to_left_in, R.anim.trans_right_to_left_out,
                        R.anim.trans_left_to_right_in, R.anim.trans_left_to_right_out)
                .replace(containerId, fragment)
                .addToBackStack(fragment.javaClass.name)
                .commit()
    }

    fun replaceFragment(fragment: Fragment, containerId : Int) {
        childFragmentManager.popBackStackImmediate(containerId, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        childFragmentManager.beginTransaction()
                .replace(containerId, fragment)
                .commit()
    }


    fun addToStackFragment(fragment: Fragment, containerId : Int, prevFragment: Fragment) {
        childFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.trans_right_to_left_in, R.anim.trans_right_to_left_out,
                        R.anim.trans_left_to_right_in, R.anim.trans_left_to_right_out)
                .hide(prevFragment)
                .replace(containerId, fragment)
                .addToBackStack(fragment.javaClass.name)
                .commit()
    }
}
