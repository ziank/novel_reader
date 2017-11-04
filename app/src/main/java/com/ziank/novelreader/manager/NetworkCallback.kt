package com.ziank.novelreader.manager

/**
 * Created by zhaixianqi on 2017/9/26.
 */

interface NetworkCallback<T> {
    fun success(response: T)
    fun fail(errorMessage: String?)
}
