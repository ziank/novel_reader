package com.ziank.novelreader.fragments

import android.app.Fragment
import android.os.Bundle

import com.kaopiz.kprogresshud.KProgressHUD
import com.ziank.novelreader.application.NovelApplication
import com.ziank.novelreader.model.NovelEvent

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by zhaixianqi on 2017/10/19.
 */

open class BaseFragment : Fragment() {

    private var mProgressHUD: KProgressHUD? = null
    val mNovel = NovelApplication.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onEvent(event: NovelEvent) {
    }

    protected fun showProgressHud() {
        if (null == mProgressHUD) {
            mProgressHUD = KProgressHUD.create(activity, KProgressHUD
                    .Style.SPIN_INDETERMINATE).setAnimationSpeed(2)
                    .setDimAmount(0.5f).show()
        } else if (!mProgressHUD!!.isShowing) {
            mProgressHUD!!.show()
        }
    }

    protected fun hideProgressHud() {
        if (null == mProgressHUD) {
            return
        }
        if (mProgressHUD!!.isShowing) {
            mProgressHUD!!.dismiss()
        }
    }
}
