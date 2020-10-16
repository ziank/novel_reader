package com.ziank.novelreader.activities

import android.app.LoaderManager
import android.content.Context
import android.content.Intent
import android.content.Loader
import android.os.Handler
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView

import com.ziank.novelreader.R
import com.ziank.novelreader.config.Constants
import com.ziank.novelreader.loaders.BookChapterListLoader
import com.ziank.novelreader.manager.BookManager
import com.ziank.novelreader.model.Book
import com.ziank.novelreader.model.Chapter
import com.ziank.novelreader.model.NovelEvent

import java.util.ArrayList


class BookMenuListActivity : BaseActivity(), LoaderManager.LoaderCallbacks<ArrayList<Chapter>> {

    private lateinit var mBook: Book
    private var mChapterList: ArrayList<Chapter>? = null

    private var mAdapter: ChapterListAdapter? = null
    private var mHandler: Handler? = null
    private var mActivityFrom: Int = 0

    private lateinit var mMenuListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_menu_list)

        mMenuListView = findViewById(R.id.menu_list)

        mHandler = Handler()

        mBook = intent.getSerializableExtra(Constants.BOOK) as Book
        mActivityFrom = intent.getIntExtra(Constants.ACTIVITY_FROM, 0)

        fetchBookChapterList()

        mAdapter = ChapterListAdapter(this)

        mMenuListView.adapter = mAdapter
        mMenuListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, pos, _ ->
            if (mActivityFrom == FROM_CONTENT) {
                val intent = Intent()
                intent.putExtra(Constants.CHAPTER_INDEX, pos)
                intent.putExtra(Constants.READ_POS, 0)
                setResult(Constants.RESULT_SELECT_CHAPTER, intent)
                finish()
            } else {
                val intent = Intent(this@BookMenuListActivity,
                        ReadActivity::class.java)
                mBook.chapterIndex = pos
                mBook.charIndex = 0
                intent.putExtra(Constants.BOOK, mBook)
                startActivity(intent)
            }
        }
        mMenuListView.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, pos, _ ->
            val chapterList = mChapterList ?: return@OnItemLongClickListener false
            if (chapterList.size <= pos) {
                return@OnItemLongClickListener true
            } else {
                val chapter = chapterList[pos]
                BookManager.instance.downloadChapterContent(mBook, chapter)
                return@OnItemLongClickListener true
            }

        }

        if (mActivityFrom == FROM_CONTENT) {
            val pos = intent.getIntExtra(Constants.CHAPTER_INDEX, 0)
            mMenuListView.setItemChecked(pos, true)

            mHandler!!.postDelayed({
                mMenuListView.setSelectionFromTop(pos, mMenuListView
                        .height / 2)
            }, 100)

        }

        val toolbar:Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun fetchBookChapterList() {
        showProgressHud()
        if (loaderManager.getLoader<Any>(GET_CHAPTER_LIST) !=
                null) {
            loaderManager.restartLoader(GET_CHAPTER_LIST, null, this)
        } else {
            loaderManager.initLoader(GET_CHAPTER_LIST, null, this)
        }
    }


    override fun onCreateLoader(i: Int, bundle: Bundle?):
            Loader<ArrayList<Chapter>> {
        return BookChapterListLoader(this, mBook)
    }

    override fun onLoadFinished(loader: Loader<ArrayList<Chapter>>,
                                chapters: ArrayList<Chapter>?) {
        mChapterList = chapters
        mBook.chapterCount = chapters?.size ?: 0
        mAdapter!!.notifyDataSetChanged()
        if (null != chapters && chapters.size > 0) {
            hideProgressHud()
        }
    }

    override fun onLoaderReset(loader: Loader<ArrayList<Chapter>>) {}

    override fun onEvent(event: NovelEvent) {
        if (event.eventType != NovelEvent.EventTypeFetchChapterList) {
            return
        }

        val book = event.eventData as Book
        if (mBook.equalsToBook(book)) {
            val chapters = BookManager.instance
                    .readChapterListFromDisk(mBook)
            if (null == chapters || chapters.isEmpty()) {
                hideProgressHud()
                return
            }
            mChapterList = chapters
            mAdapter!!.notifyDataSetChanged()
            hideProgressHud()
        }
    }

    private inner class ChapterListAdapter(
            context: Context) : BaseAdapter() {
        private val mInflater: LayoutInflater = LayoutInflater.from(context)

        override fun getCount(): Int {
            return if (null == mChapterList) 0 else mChapterList!!.size
        }

        override fun getItem(i: Int): Chapter? {
            return if (null == mChapterList) null else mChapterList!![i]
        }

        override fun getItemId(i: Int): Long {
            return getItem(i)!!.id
        }

        override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
            var result = view
            if (result == null) {
                result = mInflater.inflate(R.layout.menu_list_item,
                        viewGroup, false)
            }
            val textView = result as TextView?
            textView!!.text = getItem(i)!!.title

            return result!!
        }
    }

    companion object {

        const val FROM_CONTENT = 1
        const val FROM_BOOK_PAGE = FROM_CONTENT + 1
        private const val GET_CHAPTER_LIST = 0
    }
}
