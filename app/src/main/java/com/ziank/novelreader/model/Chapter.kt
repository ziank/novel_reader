package com.ziank.novelreader.model

import android.os.Parcel
import android.os.Parcelable

import org.json.JSONException
import org.json.JSONObject

/**
* Created by ziank on 2017/10/9.
* @copyright ziank.2018
*/

class Chapter() : Parcelable {
    var id: Long = 0

    var title: String? = null

    var url: String? = null

    var isDownloaded = false

    fun toJson(): JSONObject? {
        val `object` = JSONObject()
        try {
            `object`.put("id", id)
            `object`.put("title", title)
            `object`.put("url", url)
            `object`.put("downloaded", isDownloaded)
            return `object`
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return null
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {}

    companion object {
        fun fromJson(`object`: JSONObject): Chapter {
            val chapter = Chapter()
            chapter.id = `object`.optLong("id")
            chapter.title = `object`.optString("title")
            chapter.url = `object`.optString("url")
            chapter.isDownloaded = `object`.optBoolean("downloaded", false)
            return chapter
        }

        @JvmField
        val CREATOR: Parcelable.Creator<Chapter> = object : Parcelable.Creator<Chapter> {
            override fun createFromParcel(source: Parcel): Chapter = Chapter()
            override fun newArray(size: Int): Array<Chapter?> = arrayOfNulls(size)
        }
    }
}
