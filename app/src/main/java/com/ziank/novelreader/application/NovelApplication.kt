package com.ziank.novelreader.application

import android.app.Application
import android.content.SharedPreferences
import android.graphics.Typeface
import android.preference.PreferenceManager
import com.ziank.novelreader.R
import com.ziank.novelreader.views.slider.SlideMode

/**
* Created by ziank on 2017/10/12.
* @copyright ziank.2018
*/

class NovelApplication : Application() {

    private lateinit var mPreference: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        instance = this
        mPreference = PreferenceManager.getDefaultSharedPreferences(this)
    }

    var readTextSize: Int
        get() = mPreference.getInt(kReadSize, 20)
        set(size) = mPreference.edit().putInt(kReadSize, size).apply()

    var backgroundResource: Int
        get() {
            val resourceIndex = mPreference.getInt(kReadBackgroundResource, 0)
            return when (resourceIndex) {
                1 -> R.drawable.read_bg_0
                2 -> R.drawable.read_bg_1
                3 -> R.drawable.read_bg_2
                4 -> R.drawable.read_bg_3
                5 -> R.drawable.read_bg_4
                else -> R.color.white_color
            }
        }
        set(resource) = mPreference.edit().putInt(kReadBackgroundResource, resource).apply()

    val readTextColor: Int
        get() {
            return when (backgroundResource) {
                R.drawable.read_bg_1 -> R.color.light_grey
                else -> R.color.text_dark_grey
            }
        }
    var fontType: Int
        get() = mPreference.getInt(kFontType, 0)
        set(type) = mPreference.edit().putInt(kFontType, type).apply()

    val fontFace: Typeface
        get() {
            return when (fontType) {
                1 -> Typeface.createFromAsset(assets, "fonts/kai.ttf")
                else -> Typeface.DEFAULT
            }
        }

    fun getSlideMode(): Int {
        return mPreference.getInt(kReadSlideMode, SlideMode.PageMode)
    }

    fun setSlideMode(pageMode: Int) {
        mPreference.edit().putInt(kReadSlideMode, pageMode).apply()
    }

    companion object {
        lateinit var instance: NovelApplication
        const val kReadSize = "read_size"
        const val kReadBackgroundResource = "read_bg_resource"
        const val kReadSlideMode = "slide_mode"
        const val kFontType = "font_type"
    }
}
