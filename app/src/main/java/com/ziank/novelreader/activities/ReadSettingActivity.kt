package com.ziank.novelreader.activities

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.transition.Slide
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.ziank.novelreader.BR
import com.ziank.novelreader.R
import com.ziank.novelreader.config.Constants
import com.ziank.novelreader.databinding.ActivityBookPageBinding
import com.ziank.novelreader.databinding.ActivityReadSettingBinding
import com.ziank.novelreader.model.NovelEvent
import com.ziank.novelreader.view_models.MyComponent
import com.ziank.novelreader.view_models.ReadSettingViewModel
import com.ziank.novelreader.views.slider.SlideMode
import org.greenrobot.eventbus.EventBus

/**
 * Created by zhaixianqi on 2017/11/2.
 */
class ReadSettingActivity: BaseActivity() {
    private lateinit var mToolbar:Toolbar
    private lateinit var mReadToolbar:View
    private lateinit var mReadSettingView:View
    private lateinit var mTextSizeView:TextView

    private lateinit var mReadSettingModel: ReadSettingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil
                .setContentView<ActivityReadSettingBinding>(this, R.layout
                        .activity_read_setting, MyComponent())
        mReadSettingModel = ReadSettingViewModel(mNovel)

        val title = intent.getStringExtra(Constants.TITLE)
        if (!title.isNullOrEmpty()) {
            mReadSettingModel.title = title
        }


        binding.setting = mReadSettingModel

        mToolbar = binding.toolbar
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mReadToolbar = binding.readToolBar!!

        mToolbar.visibility = View.VISIBLE
        mToolbar.startAnimation(AnimationUtils.
                loadAnimation(this, R.anim.top_down))

        mReadToolbar.visibility = View.VISIBLE
        mReadToolbar.startAnimation(AnimationUtils.
                loadAnimation(this, R.anim.down_top))

        mReadSettingView = binding.readSettingBar
        mTextSizeView = binding.tvTextSize
        mTextSizeView.text = mNovel.readTextSize.toString()

        initListeners()

    }

    private fun initListeners() {
        val catalogBtn = findViewById(R.id.catalog_btn)
        val block = { view:View ->
            onClickView(view)
        }
        catalogBtn.setOnClickListener(block)
        val settingBtn = findViewById(R.id.setting_btn)
        settingBtn.setOnClickListener(block)
        val detailBtn = findViewById(R.id.detail_btn)
        detailBtn.setOnClickListener(block)
    }

    public fun onClickView(view: View) {
        when(view.id) {
            R.id.catalog_btn -> {
                setResult(Constants.RESULT_OPEN_CATALOG)
                dismissSelfWithAnim()
            }
            R.id.setting_btn -> {
                showSettingBar()
            }
            R.id.detail_btn -> {
                setResult(Constants.RESULT_OPEN_DETAIL)
                dismissSelfWithAnim()
            }
            R.id.content_view -> {
                if (mReadSettingView.isShown) {
                    hideSettingBar()
                } else {
                    dismissSelfWithAnim()
                }
            }
            R.id.btn_zoom_down -> {
                var size = mNovel.readTextSize
                if (size > 12) {
                    size -= 2
                    mNovel.readTextSize = size
                    mTextSizeView.text = size.toString()
                    mReadSettingModel.textSize = size
                    mReadSettingModel.notifyPropertyChanged(BR.textSizeString)
                    mReadSettingModel.notifyPropertyChanged(BR.leastSize)
                    mReadSettingModel.notifyPropertyChanged(BR.maximumSize)

                    val event = NovelEvent(NovelEvent.EventTypeChangeFontSize,
                            size)
                    EventBus.getDefault().post(event)
                }
            }

            R.id.btn_zoom_up -> {
                var size = mNovel.readTextSize
                if (size < 30) {
                    size += 2
                    mNovel.readTextSize = size
                    mTextSizeView.text = size.toString()
                    mReadSettingModel.textSize = size
                    mReadSettingModel.notifyPropertyChanged(BR.textSizeString)
                    mReadSettingModel.notifyPropertyChanged(BR.leastSize)
                    mReadSettingModel.notifyPropertyChanged(BR.maximumSize)
                    val event = NovelEvent(NovelEvent.EventTypeChangeFontSize,
                            size)
                    EventBus.getDefault().post(event)
                }
            }

            R.id.read_bg_white -> {
                val event = NovelEvent(NovelEvent.EventTypeChangeReadBg, R
                        .color.white_color)
                mReadSettingModel.backgroundResourceId = R.color.white_color
                mNovel.backgroundResource = mReadSettingModel.backgroundResouceIndex

                EventBus.getDefault().post(event)
                mReadSettingModel.notifyPropertyChanged(BR.backgroundResouceIndex)
            }

            R.id.read_bg_image0 -> {
                val event = NovelEvent(NovelEvent.EventTypeChangeReadBg, R
                        .drawable.read_bg_0)
                EventBus.getDefault().post(event)
                mReadSettingModel.backgroundResourceId = R.drawable.read_bg_0
                mNovel.backgroundResource = mReadSettingModel.backgroundResouceIndex
                mReadSettingModel.notifyPropertyChanged(BR.backgroundResouceIndex)
            }

            R.id.read_bg_image1 -> {
                val event = NovelEvent(NovelEvent.EventTypeChangeReadBg, R
                        .drawable.read_bg_1)
                EventBus.getDefault().post(event)
                mReadSettingModel.backgroundResourceId = R.drawable.read_bg_1
                mNovel.backgroundResource = mReadSettingModel.backgroundResouceIndex
                mReadSettingModel.notifyPropertyChanged(BR.backgroundResouceIndex)
            }

            R.id.read_bg_image2 -> {
                val event = NovelEvent(NovelEvent.EventTypeChangeReadBg, R
                        .drawable.read_bg_2)
                EventBus.getDefault().post(event)
                mReadSettingModel.backgroundResourceId = R.drawable.read_bg_2
                mNovel.backgroundResource = mReadSettingModel.backgroundResouceIndex
                mReadSettingModel.notifyPropertyChanged(BR.backgroundResouceIndex)
            }

            R.id.read_bg_image3 -> {
                val event = NovelEvent(NovelEvent.EventTypeChangeReadBg, R
                        .drawable.read_bg_3)
                EventBus.getDefault().post(event)
                mReadSettingModel.backgroundResourceId = R.drawable.read_bg_3
                mNovel.backgroundResource = mReadSettingModel.backgroundResouceIndex
                mReadSettingModel.notifyPropertyChanged(BR.backgroundResouceIndex)
            }

            R.id.read_bg_image4 -> {
                val event = NovelEvent(NovelEvent.EventTypeChangeReadBg, R
                        .drawable.read_bg_4)
                EventBus.getDefault().post(event)
                mReadSettingModel.backgroundResourceId = R.drawable.read_bg_4
                mNovel.backgroundResource = mReadSettingModel.backgroundResouceIndex
                mReadSettingModel.notifyPropertyChanged(BR.backgroundResouceIndex)
            }

            R.id.switch_mode_translation -> {
                val event = NovelEvent(NovelEvent.EventTypeChangeSlideMode,
                        SlideMode.PageMode)
                mNovel.setSlideMode(SlideMode.PageMode)
                EventBus.getDefault().post(event)
                mReadSettingModel.slideMode = SlideMode.PageMode
                mReadSettingModel.notifyPropertyChanged(BR.slideMode)
            }

            R.id.switch_mode_simple -> {
                val event = NovelEvent(NovelEvent.EventTypeChangeSlideMode,
                        SlideMode.OverlappedMode)
                mNovel.setSlideMode(SlideMode.OverlappedMode)
                EventBus.getDefault().post(event)
                mReadSettingModel.slideMode = SlideMode.OverlappedMode
                mReadSettingModel.notifyPropertyChanged(BR.slideMode)
            }
        }
    }

    private fun dismissSelfWithAnim() {
        mToolbar.animate()
                .setDuration(100)
                .translationY((- mToolbar.height).toFloat())
                .withEndAction {
                    mToolbar.visibility = View.GONE
                    finish()
                    overridePendingTransition(0, 0)
                }
        mReadToolbar.animate()
                .setDuration(100)
                .translationY(mReadToolbar.height.toFloat())
    }

    private fun showSettingBar() {
        mToolbar.animate()
                .setDuration(100)
                .translationY((- mToolbar.height).toFloat())
                .withEndAction {
                    mToolbar.visibility = View.GONE
                    mReadSettingView.visibility = View.VISIBLE
                    mReadSettingView.startAnimation(AnimationUtils.
                            loadAnimation(this, R.anim.down_top))
                }
        mReadToolbar.animate()
                .setDuration(100)
                .translationY(mReadToolbar.height.toFloat())
                .withEndAction {
            mReadToolbar.visibility = View.GONE
        }
    }

    private fun hideSettingBar() {
        mReadSettingView.animate()
                .setDuration(100)
                .translationY(mReadToolbar.height.toFloat())
                .withEndAction {
                    finish()
                    overridePendingTransition(0, 0)
                }
    }

    override fun onBackPressed() {
        dismissSelfWithAnim()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                setResult(Constants.RESULT_ACTIVITY_FINISH)
                dismissSelfWithAnim()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}