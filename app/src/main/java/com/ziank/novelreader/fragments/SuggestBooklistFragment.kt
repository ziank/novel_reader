package com.ziank.novelreader.fragments


import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.app.Fragment
import android.text.Layout
import android.view.*
import android.widget.*

import com.ziank.novelreader.R
import com.ziank.novelreader.activities.BookPageActivity
import com.ziank.novelreader.activities.SearchBookActivity
import com.ziank.novelreader.databinding.SearchBookItemBinding
import com.ziank.novelreader.manager.BookManager
import com.ziank.novelreader.model.Book

import com.ziank.novelreader.config.Constants
import com.ziank.novelreader.databinding.BookItemBinding
import com.ziank.novelreader.databinding.SuggestBookItemBinding
import com.ziank.novelreader.databinding.SuggestHeaderViewBinding
import com.ziank.novelreader.manager.BookSuggestType
import com.ziank.novelreader.manager.SuggestBookListResult
import com.ziank.novelreader.model.NovelEvent
import com.ziank.novelreader.view_models.BookItemViewModule
import com.ziank.novelreader.view_models.MyComponent

import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.ArrayList


/**
 * A simple [Fragment] subclass.
 */
class SuggestBooklistFragment : BaseFragment() {
    private lateinit var mSuggestBookListView: ListView
    var mBookList:List<Book> = ArrayList<Book>()
    private lateinit var mBookListAdapter:SuggestBookListAdapter
    private var mViewList:ArrayList<TextView> = ArrayList<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_suggest_booklist,
                container, false)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViews()
        initData()
    }

    private fun initData() {
        BookManager.instance.fetchAdviseWeekBookList()
    }

    private fun initViews() {
        mSuggestBookListView = view.findViewById(R.id.suggest_book_list)
        mBookListAdapter = SuggestBookListAdapter(activity)
        mSuggestBookListView.adapter = mBookListAdapter
        val headerViewBinding = DataBindingUtil
                .inflate<SuggestHeaderViewBinding>(LayoutInflater.from
                (activity), R.layout.suggest_header_view, null,
                        false, MyComponent())
        val headerView = headerViewBinding.root
        mSuggestBookListView.addHeaderView(headerView, null, false)
        mSuggestBookListView.setOnItemClickListener { adapterView, view, pos, id ->
            val book = adapterView.adapter.getItem(pos) as Book
            val intent = Intent(activity, SearchBookActivity::class.java)
            intent.putExtra(Constants.BOOK_NAME, book.title)
            startActivity(intent)
        }

        var cview = view.findViewById<TextView>(R.id.rank_week)
        cview.setOnClickListener {
            BookManager.instance.fetchRankWeekBookList()
            showProgressHud()
            headerViewBinding.bindindex = 3
        }
        mViewList.add(cview)

        cview = view.findViewById<TextView>(R.id.rank_month)
        cview.setOnClickListener {
            BookManager.instance.fetchRankMonthBookList()
            showProgressHud()
            headerViewBinding.bindindex = 4
        }
        mViewList.add(cview)

        cview = view.findViewById<TextView>(R.id.rank_all)
        cview.setOnClickListener {
            BookManager.instance.fetchRankAllBookList()
            showProgressHud()
            headerViewBinding.bindindex = 5
        }
        mViewList.add(cview)

        cview = view.findViewById<TextView>(R.id.advise_all)
        cview.setOnClickListener {
            BookManager.instance.fetchAdviseAllBookList()
            showProgressHud()
            headerViewBinding.bindindex = 2
        }
        mViewList.add(cview)

        cview = view.findViewById<TextView>(R.id.advise_month)
        cview.setOnClickListener {
            BookManager.instance.fetchAdviseMonthBookList()
            showProgressHud()
            headerViewBinding.bindindex = 1
        }
        mViewList.add(cview)

        cview = view.findViewById<TextView>(R.id.advise_week)
        cview.setOnClickListener {
            BookManager.instance.fetchAdviseWeekBookList()
            showProgressHud()
            headerViewBinding.bindindex = 0
        }
        mViewList.add(cview)
    }

    private inner class SuggestBookListAdapter(context: Context,
                                 private val mInflater: LayoutInflater = LayoutInflater.from(context)) : BaseAdapter() {
        override fun getView(pos: Int, convertView: View?, viewGroup: ViewGroup?):
                View {
            var lView = convertView
            if (lView == null) {
                val binding = SuggestBookItemBinding.inflate(mInflater,
                        viewGroup, false, MyComponent())
                lView = binding.root
                lView!!.tag = binding
            }

            val book = getItem(pos)
            val binding = lView.tag as SuggestBookItemBinding
            binding.book = BookItemViewModule(book)

            return lView
        }

        override fun getItem(pos: Int): Book {
            return mBookList[pos]
        }

        override fun getItemId(pos: Int): Long {
            return getItem(pos).bookId
        }

        override fun getCount(): Int {
            return mBookList.size
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.suggest_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.search_view -> {
                val intent = Intent(activity, SearchBookActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onEvent(event: NovelEvent) {
        if (event.eventType != NovelEvent.EventTypeSuggestResult) {
            return
        }

        val bookResult = event.eventData as SuggestBookListResult
        hideProgressHud()
        mBookList = bookResult.bookList
        mBookListAdapter.notifyDataSetChanged()
    }

}// Required empty public constructor
