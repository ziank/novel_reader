package com.ziank.novelreader.fragments


import android.app.Fragment
import android.app.LoaderManager
import android.content.Context
import android.content.Intent
import android.content.Loader
import android.databinding.DataBindingComponent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView

import com.baoyz.widget.PullRefreshLayout
import com.ziank.novelreader.R
import com.ziank.novelreader.activities.BookMenuListActivity
import com.ziank.novelreader.activities.BookPageActivity
import com.ziank.novelreader.activities.MainActivity
import com.ziank.novelreader.activities.ReadActivity
import com.ziank.novelreader.config.Constants
import com.ziank.novelreader.database.DatabaseManager
import com.ziank.novelreader.databinding.BookItemBinding
import com.ziank.novelreader.loaders.FavoriatedBookListLoader
import com.ziank.novelreader.manager.BookManager
import com.ziank.novelreader.model.Book
import com.ziank.novelreader.model.NovelEvent
import com.ziank.novelreader.view_models.BookItemViewModule
import com.ziank.novelreader.view_models.BookPageViewModel
import com.ziank.novelreader.view_models.MyComponent

import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * A simple [Fragment] subclass.
 */
class BooklistFragment : BaseFragment(), LoaderManager.LoaderCallbacks<List<Book>> {

    private lateinit var mRefreshLayout:
            PullRefreshLayout
    private lateinit var mBooklistView: ListView
    private lateinit var mEmptyView: View
    private lateinit var mGotoSuggestView: View

    private var mAdapter: BooklistAdapter? = null
    private var mIsRefreshing: Boolean = false
    private var mBooklist: MutableList<Book>? = null
    private var mHandler: Handler? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_booklist, container,
                false)
        mRefreshLayout = view.findViewById(R.id.refresher);
        mBooklistView = view.findViewById(R.id.book_list)
        mEmptyView = view.findViewById(R.id.empty_view)
        mGotoSuggestView = view.findViewById(R.id.goto_suggest)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mHandler = Handler()

        initViews()
        initData()
    }

    private fun initViews() {
        mAdapter = BooklistAdapter(activity)
        mBooklistView.adapter = mAdapter

        mBooklistView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val book = mBooklist!![i]
            book.isHasUpdate = false
            DatabaseManager.sharedManager.updateBookStatus(book)
            val intent = Intent(activity,
                    ReadActivity::class.java)
            intent.putExtra(Constants.BOOK, book)
            startActivity(intent)
        }

        registerForContextMenu(mBooklistView)

        mRefreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_WATER_DROP)
        mRefreshLayout.setOnRefreshListener {
                    mIsRefreshing = true
                    BookManager.instance.refreshBooklist()
                }

        mGotoSuggestView.setOnClickListener {
            val myAcitvity = activity as MainActivity
            myAcitvity.selectTabbar(MainActivity.SUGGEST_INDEX)
        }
    }

    private fun initData() {
        loadDataFromLocal()
        BookManager.instance.refreshBooklist()
    }

    override fun onCreateContextMenu(menu: ContextMenu,
                                     v: View,
                                     menuInfo: ContextMenu.ContextMenuInfo) {
        menu.add(0, 1, 0, R.string.delete)
        menu.add(0, 2, 0, R.string.view_chapter_list)
        menu.add(0, 3, 0, R.string.view_book_page)
        menu.add(0, 4, 0, R.string.download)
        super.onCreateContextMenu(menu, v, menuInfo)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val itemInfo = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val book = mAdapter!!.getItem(itemInfo.position)
        when (item.itemId) {
            1 -> {
                mBooklist!!.remove(book)
                BookManager.instance.deleteBook(book)
                mAdapter!!.notifyDataSetChanged()
            }

            2 -> {
                val intent = Intent(activity,
                        BookMenuListActivity::class.java)
                intent.putExtra(Constants.BOOK, book)
                startActivity(intent)
            }

            3 -> {
                val intent = Intent(activity, BookPageActivity::class.java)
                intent.putExtra(Constants.BOOK, book)
                startActivity(intent)
            }
            4 -> {
                BookManager.instance.downloadBook(book, activity,
                        loaderManager)
            }
            else -> {
            }
        }
        return super.onContextItemSelected(item)
    }

    override fun onCreateLoader(id: Int, bundle: Bundle?): Loader<List<Book>> {
        return FavoriatedBookListLoader(activity)
    }

    override fun onLoadFinished(loader: Loader<List<Book>>, books: List<Book>) {
        mBooklist = books.toMutableList()

        if (null == mBooklist || mBooklist!!.size == 0) {
            mEmptyView!!.visibility = View.VISIBLE
            mRefreshLayout!!.visibility = View.GONE
        } else {
            mEmptyView!!.visibility = View.GONE
            mRefreshLayout!!.visibility = View.VISIBLE
        }

        if (mIsRefreshing) {
            mIsRefreshing = false
        }

        mAdapter!!.notifyDataSetChanged()
        mRefreshLayout!!.setRefreshing(false)
    }

    override fun onLoaderReset(loader: Loader<List<Book>>) {

    }

    private inner class BooklistAdapter internal constructor(context: Context) : BaseAdapter() {
        private val mInflater: LayoutInflater

        init {
            mInflater = LayoutInflater.from(context)
        }

        override fun getCount(): Int {
            return if (mBooklist == null) 0 else mBooklist!!.size
        }

        override fun getItem(i: Int): Book {
            return mBooklist!![i]
        }

        override fun getItemId(i: Int): Long {
            return getItem(i).bookId
        }

        override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View {
            var view = view
            if (view == null) {

                val binding = BookItemBinding.inflate(mInflater,
                        viewGroup, false, MyComponent())
                view = binding.root
                view!!.tag = binding
            }

            val book = getItem(i)
            val binding = view.tag as BookItemBinding
            binding.book = BookItemViewModule(book)

            return view
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onEvent(event: NovelEvent) {
        when (event.eventType) {
            NovelEvent.EventTypeFavoriteChanged,
            NovelEvent.EventTypeLastUpdateChanged,
            NovelEvent.EventTypeFetchChapterList,
            NovelEvent.EventTypeRefreshBookList -> loadDataFromLocal()
            else -> {
            }
        }
    }

    private fun loadDataFromLocal() {
        if (loaderManager.getLoader<Any>(LOAD_FAVORITED_BOOK) != null) {
            loaderManager.restartLoader(LOAD_FAVORITED_BOOK, null, this)
        } else {
            loaderManager.initLoader(LOAD_FAVORITED_BOOK, null, this)
        }
    }

    companion object {
        private val LOAD_FAVORITED_BOOK = 0
    }
}
