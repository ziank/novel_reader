package com.ziank.novelreader.activities

import android.app.LoaderManager
import android.content.Intent
import android.content.Loader
import android.os.Handler
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.TextView

import com.ziank.novelreader.R
import com.ziank.novelreader.config.Constants
import com.ziank.novelreader.loaders.BookChapterListLoader
import com.ziank.novelreader.manager.BookManager
import com.ziank.novelreader.model.Book
import com.ziank.novelreader.model.Chapter
import com.ziank.novelreader.model.NovelEvent
import com.ziank.novelreader.views.ReadView
import com.ziank.novelreader.views.slider.*

import org.jsoup.helper.StringUtil

import java.util.ArrayList

class ReadActivity : BaseActivity() {
    private lateinit var mTitleView: TextView

    private lateinit var mBook: Book
    private var mChapters: List<Chapter>? = null
    private var mChapterIndex = 0
    private var mCurrentLineIndex = 0
    private var mCurrentCharIndex: Int = 0

    private lateinit var mSlidingLayout: SlidingLayout

    private var mLineNumber = DEFAULT_LINE_NUMBER

    private lateinit var mSlidingAdapter: ReadSlidingAdapter
    private lateinit var mHandler: Handler

    val readContentWidth: Float
        get() {
            val dm = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(dm)
            return dm.widthPixels - dm.scaledDensity * 30
        }
    val readTextSizeFloat: Float
        get() {
            val dm = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(dm)
            return mNovel.readTextSize.toFloat()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read)

        mSlidingLayout = findViewById(R.id.sliding_container) as SlidingLayout
        mTitleView = findViewById(R.id.chapter_title) as TextView

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mHandler = Handler()
        mBook = intent.getSerializableExtra(Constants.BOOK) as Book
        mChapterIndex = mBook.chapterIndex
        mCurrentCharIndex = mBook.charIndex

        initViews()
        initData()
    }

    private fun initViews() {
        mSlidingLayout.setOnTapListener { event ->

            val screenWidth = resources.displayMetrics.widthPixels
            val x = event.x.toInt()
            if (x > screenWidth * 2 / 3) {
                mSlidingLayout.slideNext()
            } else if (x < screenWidth / 3) {
                mSlidingLayout.slidePrevious()
            } else {
                val intent = Intent(this@ReadActivity,
                        ReadSettingActivity::class.java)
                mChapters?.let {
                    intent.putExtra(Constants.TITLE,
                            mChapters!![mChapterIndex].title)
                }
                intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                startActivityForResult(intent, Constants
                        .REQUEST_CODE_READ_SETTING)
            }
        }

        mSlidingAdapter = ReadSlidingAdapter()
        mSlidingAdapter.setCurrentChapterContent(BookManager.instance.testContent())
        mSlidingLayout.adapter = mSlidingAdapter

        switchSlidingMode(mNovel.getSlideMode())
    }

    private fun initData() {
        showProgressHud()
        loaderManager.initLoader(GET_CHAPTER_LIST, null,
                object : LoaderManager.LoaderCallbacks<ArrayList<Chapter>> {

                    override fun onCreateLoader(i: Int,
                                                bundle: Bundle?):
                            Loader<ArrayList<Chapter>> {
                        return BookChapterListLoader(this@ReadActivity,
                                mBook)
                    }

                    override fun onLoadFinished(loader: Loader<ArrayList<Chapter>>,
                                                chapters: ArrayList<Chapter>?) {
                        mChapters = chapters
                        reloadData()
                    }

                    override fun onLoaderReset(loader: Loader<ArrayList<Chapter>>) {}
                })
    }

    override fun onEvent(event: NovelEvent) {
        when (event.eventType) {
            NovelEvent.EventTypeFetchChapterList -> {
                val book = event.eventData as Book
                if (mBook.equalsToBook(book)) {
                    val chapters = BookManager.instance
                            .readChapterListFromDisk(mBook)
                    if (null != chapters && !chapters.isEmpty()) {
                        mChapters = chapters
                    }
                    hideProgressHud()
                    reloadData()
                }
            }

            NovelEvent.EventTypeFetchChapterContent -> {
                val chapter = event.eventData as Chapter
                val content = BookManager.instance
                        .getChapterContent(mBook, chapter)
                content?.let {
                    if (chapter.id == mChapters!![mChapterIndex].id) {
                        mSlidingAdapter.setCurrentChapterContent(content)
                        hideProgressHud()
                    } else if (mChapterIndex > 0 && chapter.id == mChapters!![mChapterIndex - 1].id) {
                        mSlidingAdapter.setPreviousChapterContent(content)
                    } else if (mChapterIndex < mChapters!!.size - 1 && chapter
                            .id == mChapters!![mChapterIndex + 1].id) {
                        mSlidingAdapter.setNextChapterContent(content)
                    }
                    mSlidingAdapter.notifyDataSetChanged()
                }
            }

            NovelEvent.EventTypeChangeReadBg -> {
                val resourceId = event.eventData as Int
                mHandler.post {
                    reloadBackground(resourceId)
                }
            }

            NovelEvent.EventTypeChangeSlideMode -> {
                val slidingMode = event.eventData as Int
                switchSlidingMode(slidingMode)
            }
            NovelEvent.EventTypeChangeFontSize -> {
                mSlidingAdapter.refreshTextSize()
            }
        }
    }

    private fun reloadBackground(resourceId: Int) {
        changeBackground(mSlidingAdapter.currentView, resourceId)

        mSlidingAdapter.nextView?.let {
            changeBackground(mSlidingAdapter.nextView!!, resourceId)
        }
        mSlidingAdapter.previousView?.let {
            changeBackground(mSlidingAdapter.previousView!!, resourceId)
        }
    }

    private fun changeBackground(view: View, resourceId: Int) {
        val holder = view.tag as Holder
        holder.updateBackground(resourceId)
    }

    private fun reloadData() {
        if (null == mChapters || mChapters!!.isEmpty()) {
            return
        }
        if (mChapterIndex >= mChapters!!.size) {
            mChapterIndex = 0
        }

        reloadTitle()
        val currentChapter = mChapters!![mChapterIndex]
        val content = BookManager.instance.getChapterContent(mBook,
                currentChapter)
        if (content != null && content.isNotEmpty()) {
            reloadData(content)
            hideProgressHud()
        } else {
            BookManager.instance.downloadChapterContent(mBook, currentChapter)
            showProgressHud()
        }

        if (mChapterIndex < mChapters!!.size - 1) {
            val nextChapter = mChapters!![mChapterIndex + 1]
            val text = BookManager.instance.getChapterContent(mBook, nextChapter)
            if (text == null || text.isEmpty()) {
                BookManager.instance.downloadChapterContent(mBook, nextChapter)
            } else {
                mSlidingAdapter.setNextChapterContent(text)
            }
        }

        if (mChapterIndex > 0) {
            val prevChapter = mChapters!![mChapterIndex - 1]
            val text = BookManager.instance.getChapterContent(mBook, prevChapter)
            if (text == null || text.isEmpty()) {
                BookManager.instance.downloadChapterContent(mBook, prevChapter)
            } else {
                mSlidingAdapter.setPreviousChapterContent(text)
            }
        }
    }

    private fun reloadData(chapterContent: String) {
        mSlidingAdapter.setCurrentChapterContent(chapterContent)
        mCurrentLineIndex = mSlidingAdapter.getLineNumber(mCurrentCharIndex)
        mSlidingAdapter.notifyDataSetChanged()
    }

    private fun switchSlidingMode(slideMode: Int) {
        when(slideMode) {
            SlideMode.OverlappedMode -> mSlidingLayout.setSlider(OverlappedSlider())
            else -> mSlidingLayout.setSlider(PageSlider())
        }
    }

    override fun onActivityResult(requestCode: Int,
                                  resultCode: Int,
                                  data: Intent?) {
        when (requestCode) {
            Constants.REQUEST_CODE_BOOK_MENU -> {
                if (null == data) {
                    return
                }
                mChapterIndex = data.getIntExtra(Constants.CHAPTER_INDEX,
                        mChapterIndex)
                mCurrentCharIndex = data.getIntExtra(Constants
                        .READ_POS, mCurrentCharIndex)
                mCurrentLineIndex = mSlidingAdapter.getLineNumber(mCurrentCharIndex)
            }
            Constants.REQUEST_CODE_READ_SETTING -> {
                when (resultCode) {
                    Constants.RESULT_ACTIVITY_FINISH -> finish()
                    Constants.RESULT_OPEN_CATALOG -> {
                        openBookCatalog()
                    }
                    Constants.RESULT_OPEN_DETAIL -> {
                        val intent = Intent(this, BookPageActivity::class.java)
                        intent.putExtra(Constants.BOOK, mBook)
                        intent.putExtra(Constants.ACTIVITY_FROM,
                                BookPageActivity.FROM_CONTENT)
                        startActivityForResult(intent, Constants
                                .REQUEST_CODE_BOOK_PAGE)
                    }
                }
            }
            Constants.REQUEST_CODE_BOOK_PAGE -> {
                when(resultCode) {
                    Constants.RESULT_OPEN_CATALOG -> openBookCatalog()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun openBookCatalog() {
        val intent = Intent(this, BookMenuListActivity::class.java)
        intent.putExtra(Constants.BOOK, mBook)
        intent.putExtra(Constants.ACTIVITY_FROM,
                BookMenuListActivity.FROM_CONTENT)
        intent.putExtra(Constants.CHAPTER_INDEX, mChapterIndex)
        startActivityForResult(intent, Constants.REQUEST_CODE_BOOK_MENU)
    }

    private inner class ReadSlidingAdapter : SlidingAdapter<List<String>>() {
        private var mCurrentChapterContent: List<String>? = null
        private var mNextChapterContent: List<String>? = null
        private var mPreviousChapterContent: List<String>? = null
        private var mCurrentContentString:String = ""
        private lateinit var mReadView:ReadView

        init {
            val contentView = layoutInflater.inflate(R.layout
                    .read_content, null)
            mReadView = contentView.findViewById<TextView>(R.id
                    .text_content) as ReadView
            BookManager.instance.paint = mReadView.paint
        }

        fun setCurrentChapterContent(text: String) {
            mCurrentContentString = text
            this.mCurrentChapterContent = BookManager.instance
                    .splitTextWithTextSize(text, readContentWidth)
        }

        fun setNextChapterContent(text: String) {
            this.mNextChapterContent = BookManager.instance
                    .splitTextWithTextSize(text, readContentWidth)
        }

        fun setPreviousChapterContent(text: String) {
            this.mPreviousChapterContent = BookManager.instance
                    .splitTextWithTextSize(text, readContentWidth)
        }

        override fun getView(contentView: View?, content: List<String>): View {
            var contentView = contentView
            if (contentView == null) {
                contentView = layoutInflater.inflate(R.layout.read_content, null)
                contentView!!.tag = Holder(contentView)
            }
            val holder = contentView.tag as Holder
            holder.updateData(content)
            return contentView
        }

        override fun getCurrent(): List<String> {
            if (mCurrentLineIndex >= mCurrentChapterContent!!.size) {
                return mCurrentChapterContent!!
            }
            return if (mCurrentLineIndex + mLineNumber < mCurrentChapterContent!!.size) {
                mCurrentChapterContent!!
                        .subList(mCurrentLineIndex, mCurrentLineIndex + mLineNumber)
            } else {
                mCurrentChapterContent!!
                        .subList(mCurrentLineIndex, mCurrentChapterContent!!.size)
            }
        }

        override fun getNext(): List<String> {
            val readView = currentView.findViewById<View>(R.id.text_content) as ReadView
            val lineCount = readView.lineCount

            val nextIndex = mCurrentLineIndex + lineCount
            if (nextIndex + mLineNumber < mCurrentChapterContent!!.size) {
                return mCurrentChapterContent!!
                        .subList(nextIndex, nextIndex + mLineNumber)
            } else if (nextIndex < mCurrentChapterContent!!.size) {
                return mCurrentChapterContent!!
                        .subList(nextIndex, mCurrentChapterContent!!.size)
            } else {
                if (mNextChapterContent == null) {
                    return ArrayList()
                }
                return if (mNextChapterContent!!.size > mLineNumber) {
                    mNextChapterContent!!.subList(0, mLineNumber)
                } else {
                    mNextChapterContent!!.subList(0, mNextChapterContent!!
                            .size)
                }
            }
        }

        override fun getPrevious(): List<String> {
            val readView = currentView.findViewById<View>(R.id.text_content) as ReadView

            if (mCurrentLineIndex >= mCurrentChapterContent!!.size) {
                return mCurrentChapterContent!!
            }
            if (mCurrentLineIndex - mLineNumber >= 0) {
                return mCurrentChapterContent!!
                        .subList(mCurrentLineIndex - mLineNumber, mCurrentLineIndex)
            } else if (mCurrentLineIndex > 0) {
                return mCurrentChapterContent!!.subList(0, mLineNumber)
            } else {
                if (mPreviousChapterContent == null) {
                    return ArrayList()
                }
                return if (mPreviousChapterContent!!.size > mLineNumber) {
                    mPreviousChapterContent!!
                            .subList(mPreviousChapterContent!!.size - mLineNumber,
                                    mPreviousChapterContent!!.size)
                } else {
                    mPreviousChapterContent!!.subList(0,
                            mPreviousChapterContent!!.size)
                }
            }
        }

        override fun hasNext(): Boolean {
            val readView = currentView.findViewById<View>(R.id.text_content) as ReadView
            if (mCurrentLineIndex + readView.lineCount < mCurrentChapterContent!!
                    .size - 1) {
                return true
            }
            return mChapterIndex < mChapters!!.size - 1 && mNextChapterContent != null && mNextChapterContent!!.isNotEmpty()
        }

        override fun hasPrevious(): Boolean {
            if (mCurrentLineIndex > 0) {
                return true
            }
            return mChapterIndex > 0 && mPreviousChapterContent != null &&
                    mPreviousChapterContent!!.isNotEmpty()
        }

        override fun computeNext() {
            val readView = currentView.findViewById<View>(R.id.text_content) as ReadView
            if (mCurrentLineIndex + readView.lineCount < mCurrentChapterContent!!
                    .size - 1) {
                mCurrentLineIndex += readView.lineCount
                mCurrentCharIndex += readView.charNum
            } else {
                mCurrentLineIndex = 0
                mCurrentCharIndex = 0
                mChapterIndex++

                reloadTitle()

                mPreviousChapterContent = mCurrentChapterContent
                mCurrentChapterContent = mNextChapterContent

                if (mChapterIndex < mChapters!!.size - 1) {
                    val nextChapter = mChapters!![mChapterIndex + 1]
                    val text = BookManager.instance.getChapterContent(mBook, nextChapter)
                    if (text == null || text.isEmpty()) {
                        BookManager.instance.downloadChapterContent(mBook, nextChapter)
                    } else {
                        setNextChapterContent(text)
                    }
                }
            }
        }

        override fun computePrevious() {
            if (mCurrentLineIndex == 0) {
                mCurrentLineIndex = mPreviousChapterContent!!.size - mLineNumber
                mChapterIndex--
                mNextChapterContent = mCurrentChapterContent
                mCurrentChapterContent = mPreviousChapterContent
                reloadTitle()

                if (mChapterIndex > 0) {
                    val prevChapter = mChapters!![mChapterIndex - 1]
                    val text = BookManager.instance.getChapterContent(mBook, prevChapter)
                    if (text == null || text.isEmpty()) {
                        BookManager.instance.downloadChapterContent(mBook, prevChapter)
                    } else {
                        setPreviousChapterContent(text)
                    }
                }
            } else {
                mCurrentLineIndex = mCurrentLineIndex - mLineNumber
            }
            mCurrentLineIndex = if (mCurrentLineIndex >= 0) mCurrentLineIndex else 0
            mCurrentCharIndex = getReadCharIndex()
        }

        fun getLineNumber(charIndex: Int): Int {
            when {
                (charIndex == 0) -> return 0
                (mCurrentContentString.isEmpty()) -> return 0
                (charIndex > mCurrentContentString.length) -> return 0
                else -> {
                    var charCount = 0
                    for ((index, line) in mCurrentChapterContent!!.withIndex()) {
                        charCount += line.length
                        if (charIndex <= charCount) {
                            return index
                        }
                    }
                    return 0
                }
            }
        }

        private fun getReadCharIndex(): Int {
            if (mCurrentChapterContent == null) {
                return 0
            }
            var charIndex = 0
            for ((index, line) in mCurrentChapterContent!!.withIndex()) {
                if (index < mCurrentLineIndex) {
                    charIndex += line.length
                } else {
                    break
                }
            }
            return charIndex
        }

        fun refreshTextSize() {
            mReadView.textSize = mNovel.readTextSize.toFloat()
            mLineNumber = DEFAULT_LINE_NUMBER
            reloadData()
            BookManager.instance.paint = mReadView.paint
        }
    }


    private fun reloadTitle() {
        mChapters?.let {
            val chapter = mChapters!![mChapterIndex]
            mTitleView.text = chapter.title
        }
    }

    override fun onPause() {
        super.onPause()
        mBook.chapterIndex = mChapterIndex
        mBook.charIndex = mCurrentCharIndex
        BookManager.instance.updateReadPost(mBook)
    }

    private inner class Holder(contentView: View) {
        private val mTextView: TextView = contentView.findViewById(R.id.text_content)
        init {
            updateBackground(mNovel.backgroundResource)
            mTextView.setTextColor(resources.getColorStateList(mNovel
                    .readTextColor))
        }

        fun updateData(content: List<String>) {
            mTextView.text = StringUtil.join(content, "\n")
            mTextView.textSize = mNovel.readTextSize.toFloat()
            if (mLineNumber == DEFAULT_LINE_NUMBER && mTextView.height > 0) {
                mLineNumber = (mTextView.height - mTextView.paddingTop -
                        mTextView.paddingBottom) / mTextView.lineHeight
            }
        }

        fun updateBackground(resourceId: Int) {
            mTextView.setTextColor(resources.getColorStateList(mNovel
                    .readTextColor))
            mTextView.setBackgroundResource(resourceId)
        }
    }

    companion object {
        private val GET_CHAPTER_LIST = 0
        private val DEFAULT_LINE_NUMBER = 40
    }
}
