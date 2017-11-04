package com.ziank.novelreader.view_models

import android.databinding.BindingAdapter
import android.databinding.DataBindingComponent
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.ziank.novelreader.R
import com.ziank.novelreader.views.ReadBackgroundShowView

/**
 * Created by zhaixianqi on 2017/11/1.
 */
class MyComponent:DataBindingComponent {
    override fun getMyComponent(): MyComponent {
        return MyComponent()
    }

    @BindingAdapter("imageUrl")
    fun loadImage(view: ImageView, imageUrl: String) {
        Picasso.with(view.context)
                .load(imageUrl)
                .placeholder(R.drawable.booklist)
                .into(view)
    }

    @BindingAdapter("select")
    fun selectImage(view: ReadBackgroundShowView, selected: Boolean) {
        view.choosed = selected
        view.invalidate()
    }
}