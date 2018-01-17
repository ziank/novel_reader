package com.ziank.novelreader.activities

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.SearchView
import com.ziank.novelreader.R
import com.ziank.novelreader.config.Constants
import com.ziank.novelreader.databinding.SearchBookItemBinding
import com.ziank.novelreader.manager.BookManager
import com.ziank.novelreader.model.Book
import com.ziank.novelreader.model.NovelEvent
import com.ziank.novelreader.view_models.MyComponent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
* Created by ziank on 2017/11/6.
* @copyright ziank.2018
*/

class SearchBookActivity:BaseActivity() {
    private lateinit var mSearchHistoryView: ListView
    private lateinit var mSearchResultView: ListView
    private lateinit var mSearchResultAdapter: BooklistAdapter
    private lateinit var mSearchView: SearchView
    private lateinit var mSearchBookHint: View
    private var mSearchBookResult: MutableList<Book>? = null
    private var mHandler: Handler? = null

    private var mSearchText:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_book)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mSearchHistoryView = findViewById(R.id.search_history) as ListView
        mSearchResultView = findViewById(R.id.search_result) as ListView
        mSearchBookHint = findViewById(R.id.search_book_hint)
        mHandler = Handler()

        mSearchResultAdapter = BooklistAdapter(this)
        mSearchResultView.adapter = mSearchResultAdapter
        mSearchResultView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, pos, id ->
            val book = mSearchResultAdapter.getItem(pos)
            val intent = Intent(this, BookPageActivity::class.java)
            intent.putExtra(Constants.BOOK, book)
            startActivity(intent)
        }

        val searchBookName = intent.getStringExtra(Constants.BOOK_NAME)
        if (!searchBookName.isNullOrEmpty()) {
            mSearchText = searchBookName
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        menu?.let {
            val searchView = menu.findItem(R.id.search_book).actionView as
                    SearchView
            searchView.isIconified = false
            searchView.queryHint = getString(R.string.search_book_hint)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(book_name: String): Boolean {
                    mSearchResultView.visibility = View.VISIBLE
                    mSearchHistoryView.visibility = View.GONE
                    mSearchBookHint.visibility = View.GONE
                    mSearchBookResult?.clear()
                    mSearchText = book_name
                    BookManager.instance.searchBook(book_name)
                    searchView.clearFocus()
                    showProgressHud()
                    return true
                }

                override fun onQueryTextChange(s: String): Boolean {
                    mSearchResultView.visibility = View.GONE
                    mSearchHistoryView.visibility = View.VISIBLE
                    mSearchBookHint.visibility = View.VISIBLE
                    mSearchBookResult?.clear()

                    return true
                }
            })
            mSearchView = searchView
            if (mSearchText.isNotEmpty()) {
                mSearchView.setQuery(mSearchText, true)
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    private inner class BooklistAdapter
        internal constructor(context: Context,
                             private val mInflater: LayoutInflater
                             = LayoutInflater.from(context)) : BaseAdapter() {

        override fun getCount(): Int {
            return if (mSearchBookResult == null) 0 else mSearchBookResult!!.size
        }

        override fun getItem(i: Int): Book {
            return mSearchBookResult!![i]
        }

        override fun getItemId(i: Int): Long {
            return getItem(i).bookId
        }

        override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
            var itemView = view
            if (itemView == null) {
                val binding = DataBindingUtil.inflate<SearchBookItemBinding>(
                        mInflater, R.layout.search_book_item, viewGroup,
                        false, MyComponent())
                itemView = binding.root
                itemView!!.tag = binding
            }
            val binding = itemView.tag as SearchBookItemBinding
            binding.book = getItem(i)

            return itemView
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onEvent(event: NovelEvent) {
        if (event.eventType != NovelEvent.EventTypeSearchResult) {
            return
        }
        val result = event.eventData as List<Book>
        if (null == mSearchBookResult) {
            mSearchBookResult = result.toMutableList()
        } else {
            mSearchBookResult!!.addAll(result)
            mSearchBookResult!!.sortBy {
                BookManager.instance.getDistanceBetweenString(it.title!!,
                        mSearchText)
            }
        }
        hideProgressHud()
        mSearchResultAdapter.notifyDataSetChanged()
    }

}