package com.ziank.novelreader.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.Window

import com.kaopiz.kprogresshud.KProgressHUD
import com.ziank.novelreader.application.NovelApplication
import com.ziank.novelreader.model.NovelEvent

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

open class BaseActivity : AppCompatActivity() {
    private var mProgressHUD: KProgressHUD? = null
    val mNovel = NovelApplication.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home && this !is MainActivity) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onEvent(event: NovelEvent){}

    protected fun showProgressHud() {
        if (null == mProgressHUD) {
            mProgressHUD = KProgressHUD.create(this, KProgressHUD.Style
                    .SPIN_INDETERMINATE).setAnimationSpeed(2).setDimAmount(0.5f).show()
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
