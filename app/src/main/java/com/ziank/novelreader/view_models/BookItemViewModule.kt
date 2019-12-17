package com.ziank.novelreader.view_models

import android.databinding.BaseObservable
import android.databinding.BindingAdapter
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.ziank.novelreader.R
import com.ziank.novelreader.model.Book

/**
* Created by ziank on 2017/11/1.
* @copyright ziank.2018
*/
class BookItemViewModule(private var mBook: Book) : BaseObservable() {
    val title: String?
        get() = mBook.title

    val author: String?
        get() = mBook.author

    val updateContent: String?
        get() = mBook.updateContent

    val hasUpdate: Boolean
        get() = mBook.isHasUpdate

    val bookCoverUrl: String?
        get() = mBook.bookCoverUrl
    val desc: String?
        get() = mBook.summary

    val resourceName:String
        get() = mBook.resourceName

    val resourceColor: Int
        get() = mBook.resourceColor
}