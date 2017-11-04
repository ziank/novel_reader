package com.ziank.novelreader.view_models

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.BindingAdapter
import android.widget.ImageView

import com.squareup.picasso.Picasso
import com.ziank.novelreader.R
import com.ziank.novelreader.manager.BookManager
import com.ziank.novelreader.model.Book

/**
 * Created by zhaixianqi on 2017/10/23.
 */

class BookPageViewModel(private val mBook: Book) : BaseObservable() {

    val title: String?
        get() = mBook.title

    val author: String?
        get() = mBook.author

    val updateContent: String?
        get() = mBook.updateContent

    val coverUrl: String?
        get() = mBook.bookCoverUrl

    val summary: String?
        get() = mBook.summary

    val isFavorited: Boolean
        @Bindable
        get() {
            val favList = BookManager.instance.fetchAllBookList()
            favList?.let {
                for (book in favList) {
                    if (mBook.equalsToBook(book)) {
                        return true
                    }
                }
            }
            return false
        }
}
