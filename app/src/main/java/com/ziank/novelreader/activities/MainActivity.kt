package com.ziank.novelreader.activities

import android.app.Fragment
import android.app.FragmentManager
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View

import com.ziank.novelreader.R
import com.ziank.novelreader.fragments.BooklistFragment
import com.ziank.novelreader.fragments.SettingsFragment
import com.ziank.novelreader.fragments.SuggestBooklistFragment
import com.ziank.novelreader.views.TabbarItemView

class MainActivity : BaseActivity(), View.OnClickListener {

    private val mTabbarItems = arrayOfNulls<TabbarItemView>(TAB_BAR_ITEM_COUNT)

    private var mCurIndex = -1
    private var mFragManager: FragmentManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initListeners()
        initTabbars()

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
    }

    private fun initTabbars() {
        selectTabbar(BOOK_LIST_INDEX)
    }

    private fun initListeners() {
        mFragManager = fragmentManager

        val booklistView = findViewById(R.id.tabbar_booklist) as TabbarItemView
        booklistView.initTabs(R.string.my_book_list, R.drawable.maintab_bookstand_icon, R.drawable.maintab_bookstand_icon_hover)
        booklistView.setOnClickListener(this)
        mTabbarItems[0] = booklistView

        val suggestView = findViewById(R.id.tabbar_suggest) as TabbarItemView
        suggestView.initTabs(R.string.suggest_book_list, R.drawable.maintab_city_icon, R.drawable.maintab_city_icon_hover)
        suggestView.setOnClickListener(this)
        mTabbarItems[1] = suggestView

//        val settingsView = findViewById(R.id.tabbar_settings) as TabbarItemView
//        settingsView.initTabs(R.string.my_settings, R.drawable.maintab_category_icon, R.drawable.maintab_category_icon_hover)
//        settingsView.setOnClickListener(this)
//        mTabbarItems[2] = settingsView
    }

    override fun onClick(view: View) {
        val id = view.id
        when (id) {
            R.id.tabbar_booklist -> selectTabbar(BOOK_LIST_INDEX)

            R.id.tabbar_suggest -> selectTabbar(SUGGEST_INDEX)

//            R.id.tabbar_settings -> selectTabbar(SETTINGS_INDEX)
        }
    }

    internal fun selectTabbar(index: Int) {
        if (mCurIndex == index) {
            return
        }

        val lastTag = getFragmentTag(mCurIndex)

        mCurIndex = index

        for (i in 0 until TAB_BAR_ITEM_COUNT) {
            val itemView = mTabbarItems[i]
            itemView!!.isSelected = i == index
        }

        val transaction = mFragManager!!.beginTransaction()
        val tag = getFragmentTag(index)
        val curFrag = mFragManager!!.findFragmentByTag(tag)
        val lastFrag = mFragManager!!.findFragmentByTag(lastTag)
        if (lastFrag != null) {
            transaction.hide(lastFrag)
        }
        if (curFrag != null) {
            transaction.show(curFrag)
        } else {
            val fragment = newFragment(index)
            transaction.add(R.id.container, fragment, tag)
        }
        transaction.commitAllowingStateLoss()
        supportActionBar?.title = getShowTitle(index)
    }

    private fun getShowTitle(index: Int): String? {
        return when(index) {
            BOOK_LIST_INDEX -> getString(R.string.my_book_list)
            SUGGEST_INDEX -> getString(R.string.suggest_book_list)
            else -> ""
        }
    }

    private fun newFragment(index: Int): Fragment? {
        return when (index) {
            BOOK_LIST_INDEX -> BooklistFragment()
            SUGGEST_INDEX -> SuggestBooklistFragment()
//            SETTINGS_INDEX -> SettingsFragment()
            else -> null
        }
    }

    private fun getFragmentTag(index: Int): String {
        return "main_tab_" + index
    }

    companion object {
        val TAB_BAR_ITEM_COUNT = 2
        val BOOK_LIST_INDEX = 0
        val SUGGEST_INDEX = 1
        val SETTINGS_INDEX = 2
    }
}
