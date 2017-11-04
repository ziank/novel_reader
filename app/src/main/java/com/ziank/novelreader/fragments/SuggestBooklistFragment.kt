package com.ziank.novelreader.fragments


import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.SearchView

import com.ziank.novelreader.R
import com.ziank.novelreader.activities.BookPageActivity
import com.ziank.novelreader.databinding.SearchBookItemBinding
import com.ziank.novelreader.manager.BookManager
import com.ziank.novelreader.model.Book

import java.util.ArrayList

import com.ziank.novelreader.config.Constants
import com.ziank.novelreader.model.NovelEvent

import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode



/**
 * A simple [Fragment] subclass.
 */
class SuggestBooklistFragment : BaseFragment() {

    private lateinit var mSearchBookView: SearchView
    private lateinit var mSearchHistoryView: ListView
    private lateinit var mSearchResultView: ListView

    private var mSearchBookResult: MutableList<Book>? = null
    private var mSearchResultAdapter: BooklistAdapter? = null
    private var mHandler: Handler? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_suggest_booklist,
                container, false)
        mSearchBookView = view.findViewById(R.id.search_book)
        mSearchHistoryView = view.findViewById(R.id.search_history)
        mSearchResultView = view.findViewById(R.id.search_result)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mHandler = Handler()
        initViews()
    }

    private fun initViews() {
        val searchPlateId = mSearchBookView!!.context.resources
                .getIdentifier("android:id/search_plate", null, null)
        val searchPlateView = mSearchBookView!!.findViewById<View>(searchPlateId)
        searchPlateView?.setBackgroundResource(R.color.white_color)
        mSearchBookView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(bookname: String): Boolean {
                mSearchResultView!!.visibility = View.VISIBLE
                mSearchHistoryView!!.visibility = View.GONE
                BookManager.instance.searchBook(bookname)
                mSearchBookView!!.clearFocus()
                showProgressHud()
                return true
            }

            override fun onQueryTextChange(s: String): Boolean {
                mSearchResultView!!.visibility = View.GONE
                mSearchHistoryView!!.visibility = View.VISIBLE
                if (null != mSearchBookResult) {
                    mSearchBookResult!!.clear()
                }

                return true
            }
        })

        mSearchResultView!!.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, pos, id ->
            val book = mSearchResultAdapter!!.getItem(pos)
            val intent = Intent(activity, BookPageActivity::class.java)
            intent.putExtra(Constants.BOOK, book)
            startActivity(intent)
        }

        mSearchResultAdapter = BooklistAdapter(activity)
        mSearchResultView!!.adapter = mSearchResultAdapter
    }

    private inner class BooklistAdapter internal constructor(context: Context) : BaseAdapter() {
        private val mInflater: LayoutInflater

        init {
            mInflater = LayoutInflater.from(context)
        }

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
            var view = view
            if (view == null) {
                val binding = DataBindingUtil.inflate<SearchBookItemBinding>(mInflater, R.layout.search_book_item, viewGroup, false)
                view = binding.root
                view!!.tag = binding
            }
            val binding = view.tag as SearchBookItemBinding
            binding.book = getItem(i)

            return view
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
        }
        hideProgressHud()
        mSearchResultAdapter!!.notifyDataSetChanged()
    }
}// Required empty public constructor
