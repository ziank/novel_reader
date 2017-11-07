package com.ziank.novelreader.activities

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Button
import com.ziank.novelreader.BR
import com.ziank.novelreader.R
import com.ziank.novelreader.config.Constants
import com.ziank.novelreader.database.DatabaseManager
import com.ziank.novelreader.databinding.ActivityBookPageBinding
import com.ziank.novelreader.manager.BookManager
import com.ziank.novelreader.model.Book
import com.ziank.novelreader.view_models.BookPageViewModel
import com.ziank.novelreader.view_models.MyComponent

class BookPageActivity : BaseActivity(), View.OnClickListener {
    private lateinit var mBook: Book

    private lateinit var mChapterListView: View
    private lateinit var mReadButton: Button
    private lateinit var mStarButton: Button
    private var mBookModel: BookPageViewModel? = null

    private var mActivityFrom:Int = FROM_DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityBookPageBinding>(this, R.layout
                .activity_book_page, MyComponent())
        mChapterListView = binding.chapterListView
        mReadButton = binding.readBtn
        mStarButton = binding.starBtn

        intent?.let {
            mBook = intent.getSerializableExtra(Constants.BOOK) as Book
            mBookModel = BookPageViewModel(mBook)
            binding.book = mBookModel

            mActivityFrom = intent.getIntExtra(Constants.ACTIVITY_FROM, FROM_DEFAULT)

            mChapterListView.setOnClickListener(this)
            mReadButton.setOnClickListener(this)
            mStarButton.setOnClickListener(this)
        }


        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.chapter_list_view -> openBookMenus()
            R.id.read_btn -> readBook()
            R.id.star_btn -> favoriteBook()
        }
        return
    }

    private fun favoriteBook() {
        BookManager.instance.addBook(mBook)
        mBookModel!!.notifyPropertyChanged(BR.favorited)
    }

    private fun readBook() {
        when (mActivityFrom) {
            FROM_CONTENT -> finish()
            else -> {
                mBook.isHasUpdate = false
                DatabaseManager.sharedManager.updateBookStatus(mBook)
                val intent = Intent(this,
                        ReadActivity::class.java)
                intent.putExtra(Constants.BOOK, mBook)
                startActivity(intent)
            }
        }
    }

    private fun openBookMenus() {
        when (mActivityFrom) {
            FROM_CONTENT -> {
                setResult(Constants.RESULT_OPEN_CATALOG)
                finish()
            }
            else -> {
                val intent = Intent(this, BookMenuListActivity::class.java)
                intent.putExtra(Constants.BOOK, mBook)
                startActivity(intent)
            }
        }
    }

    companion object {
        val FROM_DEFAULT = 0
        val FROM_CONTENT = 1
    }
}
