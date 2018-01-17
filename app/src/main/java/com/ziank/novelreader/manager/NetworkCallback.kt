package com.ziank.novelreader.manager

/**
* Created by ziank on 2017/9/26.
* @copyright ziank.2018
*/

interface NetworkCallback<T> {
    fun success(response: T)
    fun fail(errorMessage: String?)
}
