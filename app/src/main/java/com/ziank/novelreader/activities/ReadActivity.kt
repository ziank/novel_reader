package com.ziank.novelreader.activities

import android.app.LoaderManager
import android.content.*
import android.content.res.AssetManager
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.text.Html
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
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
import java.text.SimpleDateFormat
import java.util.*

class ReadActivity : BaseActivity() {
    private lateinit var mTitleView: TextView
    private lateinit var mBatteryView: ProgressBar
    private lateinit var mTimeLabel: TextView
    private lateinit var mProgressLabel: TextView
    private lateinit var mPageNumView: TextView

    private var mBatInfoReceiver: BroadcastReceiver = object:
            BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                    val batteryLevel = intent.getIntExtra("level", 0)
                    mBatteryView.progress = 100 - batteryLevel
                }
            }
        }
    }

    private lateinit var mBook: Book
    private var mChapters: List<Chapter>? = null
    private var mChapterIndex = 0
    private var mCurrentLineIndex = 0
    private var mCurrentCharIndex: Int = 0
    private val mDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read)

        mSlidingLayout = findViewById(R.id.sliding_container)
        mTitleView = findViewById(R.id.chapter_title)
        mBatteryView = findViewById(R.id.footer_bar_battery)
        mTimeLabel = findViewById(R.id.footer_bar_time)
        mProgressLabel = findViewById(R.id.footer_bar_progress)
        mPageNumView = findViewById(R.id.footer_bar_page)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        mHandler = Handler()
        mBook = intent.getSerializableExtra(Constants.BOOK) as Book
        mChapterIndex = mBook.chapterIndex
        mCurrentCharIndex = mBook.charIndex

        initViews()
        initData()

        registerReceiver(mBatInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onDestroy() {
        unregisterReceiver(mBatInfoReceiver)
        super.onDestroy()
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
        mSlidingAdapter.setCurrentChapterContent("测试", BookManager.instance.testContent())
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
                        mBook.chapterCount = chapters?.size ?: 0
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
                    if (chapter.url == mChapters!![mChapterIndex].url) {
                        mSlidingAdapter.setCurrentChapterContent(if (chapter.title.isNullOrEmpty()) { "第${chapter.id + 1}章" } else { chapter.title!! } , content)
                        hideProgressHud()
                    } else if (mChapterIndex > 0 && chapter.url ==
                            mChapters!![mChapterIndex - 1].url) {
                        mSlidingAdapter.setPreviousChapterContent(if (chapter.title.isNullOrEmpty()) { "第${chapter.id + 1}章" } else { chapter.title!! } , content)
                    } else if (mChapterIndex < mChapters!!.size - 1 && chapter
                            .id == mChapters!![mChapterIndex + 1].id) {
                        mSlidingAdapter.setNextChapterContent(if (chapter.title.isNullOrEmpty()) { "第${chapter.id + 1}章" } else { chapter.title!! } , content)
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
            NovelEvent.EventtypeChangeFontTypeface -> {
                mSlidingAdapter.refreshTypeface()
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
                mSlidingAdapter.setNextChapterContent(if (nextChapter.title.isNullOrEmpty()) { "第${nextChapter.id + 1}章" } else { nextChapter.title!! } , text)
            }
        }

        if (mChapterIndex > 0) {
            val prevChapter = mChapters!![mChapterIndex - 1]
            val text = BookManager.instance.getChapterContent(mBook, prevChapter)
            if (text == null || text.isEmpty()) {
                BookManager.instance.downloadChapterContent(mBook, prevChapter)
            } else {
                mSlidingAdapter.setPreviousChapterContent(if (prevChapter.title.isNullOrEmpty()) { "第${prevChapter.id + 1}章" } else { prevChapter.title!! } , text)
            }
        }

        mSlidingAdapter.updateFooterBar()
    }

    private fun reloadData(chapterContent: String) {
        val chapter = mChapters!![mChapterIndex]
        mSlidingAdapter.setCurrentChapterContent(if (chapter.title.isNullOrEmpty()) { "第${chapter.id + 1}章" } else { chapter.title!! } , chapterContent)
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

        mTimeLabel.text = mDateFormat.format(Date())
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
        private var mReadView:ReadView

        init {
            val nullParent = null
            val contentView = layoutInflater.inflate(R.layout
                    .read_content, nullParent)
            mReadView = contentView.findViewById<TextView>(R.id
                    .text_content) as ReadView
            mReadView.textSize = mNovel.readTextSize.toFloat()
            mReadView.typeface = mNovel.fontFace
            BookManager.instance.paint = mReadView.paint
        }

        fun setCurrentChapterContent(title: String, text: String) {
            mCurrentContentString = text
            this.mCurrentChapterContent = BookManager.instance
                    .splitTextWithTextSize(title, text, readContentWidth)
        }

        fun setNextChapterContent(title: String, text: String) {
            this.mNextChapterContent = BookManager.instance
                    .splitTextWithTextSize(title, text, readContentWidth)
        }

        fun setPreviousChapterContent(title: String, text: String) {
            this.mPreviousChapterContent = BookManager.instance
                    .splitTextWithTextSize(title, text, readContentWidth)
        }

        override fun getView(contentView: View?, content: List<String>): View {
            var currentViewView = contentView
            if (currentViewView == null) {
                val nullParent = null
                currentViewView = layoutInflater.inflate(R.layout
                        .read_content, nullParent)
                currentViewView!!.tag = Holder(currentViewView)
            }
            val holder = currentViewView.tag as Holder
            holder.updateData(content)
            return currentViewView
        }

        override fun getCurrent(): List<String> {
            if (mCurrentLineIndex >= mCurrentChapterContent!!.size) {
                return mCurrentChapterContent!!
            }
            val lineNumber = mLineNumber
            return if (mCurrentLineIndex + lineNumber < mCurrentChapterContent!!.size) {
                mCurrentChapterContent!!
                        .subList(mCurrentLineIndex, mCurrentLineIndex + lineNumber)
            } else {
                mCurrentChapterContent!!
                        .subList(mCurrentLineIndex, mCurrentChapterContent!!.size)
            }
        }

        override fun getNext(): List<String> {
            val nextIndex = mCurrentLineIndex + mLineNumber
            when {
                nextIndex + mLineNumber < mCurrentChapterContent!!.size -> return mCurrentChapterContent!!
                        .subList(nextIndex, nextIndex + mLineNumber)
                nextIndex <= mCurrentChapterContent!!.size - 1 -> return mCurrentChapterContent!!
                        .subList(nextIndex, mCurrentChapterContent!!.size)
                else -> {
                    if (mNextChapterContent == null) {
                        return ArrayList()
                    }
                    return if (mNextChapterContent!!.size > mLineNumber - 1) {
                        mNextChapterContent!!.subList(0, mLineNumber)
                    } else {
                        mNextChapterContent!!.subList(0, mNextChapterContent!!
                                .size)
                    }
                }
            }
        }

        override fun getPrevious(): List<String> {
            if (mCurrentLineIndex >= mCurrentChapterContent!!.size) {
                return mCurrentChapterContent!!
            }

            when {
                mCurrentLineIndex - mLineNumber >= 0 -> return mCurrentChapterContent!!
                        .subList(mCurrentLineIndex - mLineNumber, mCurrentLineIndex)
                mCurrentLineIndex > 0 -> return mCurrentChapterContent!!.subList(0, mLineNumber)
                else -> {
                    if (mPreviousChapterContent == null) {
                        return ArrayList()
                    }

                    var startIndex: Int = 0
                    if (mPreviousChapterContent!!.size > mLineNumber - 1) {
                        val pageNum = (mPreviousChapterContent!!.size + 1) / mLineNumber
                        startIndex = if ((mPreviousChapterContent!!.size + 1) % mLineNumber == 0) { (pageNum - 1) * mLineNumber - 1 } else { pageNum * mLineNumber - 1 }
                    }

                    return mPreviousChapterContent!!.subList(startIndex, mPreviousChapterContent!!.size)
                }
            }
        }

        override fun hasNext(): Boolean {
            val readView = currentView.findViewById<View>(R.id.text_content) as ReadView
            if (mCurrentLineIndex + mLineNumber < mCurrentChapterContent!!
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
            if (mCurrentLineIndex + mLineNumber <= mCurrentChapterContent!!
                    .size - 1) {
                mCurrentLineIndex += mLineNumber
                mCurrentCharIndex += readView.charCount
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
                        setNextChapterContent(if (nextChapter.title.isNullOrEmpty()) { "第${nextChapter.id + 1}章" } else { nextChapter.title!! } , text)
                    }
                }
            }
            updateFooterBar()
        }

        override fun computePrevious() {
            if (mCurrentLineIndex == 0) {

                mCurrentLineIndex = if (mPreviousChapterContent!!.size > mLineNumber - 1) {
                    val pageNum = (mPreviousChapterContent!!.size + 1) / mLineNumber
                    if ((mPreviousChapterContent!!.size + 1) % mLineNumber == 0) { (pageNum - 1) * mLineNumber - 1 } else { pageNum * mLineNumber - 1 }
                } else {
                    0
                }

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
                        setPreviousChapterContent(if (prevChapter.title.isNullOrEmpty()) { "第${prevChapter.id + 1}章" } else { prevChapter.title!! } , text)
                    }
                }
            } else {
                mCurrentLineIndex -= mLineNumber
            }
            mCurrentLineIndex = if (mCurrentLineIndex >= 0) mCurrentLineIndex else 0
            mCurrentCharIndex = getReadCharIndex()

            updateFooterBar()
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
                        if (charIndex < charCount) {
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

        fun refreshTypeface() {
            if (mNovel.fontType == 0) {
                mReadView.typeface = Typeface.DEFAULT
            } else {
                val typeface = Typeface.createFromAsset(assets, "fonts/kai.ttf")
                mReadView.typeface = typeface
            }
            mLineNumber = DEFAULT_LINE_NUMBER
            reloadData()
            BookManager.instance.paint = mReadView.paint
        }

        fun updateFooterBar() {
            if (mChapters == null) {
                mProgressLabel.text = "0%%"
                mPageNumView.text = "/"
            } else {
                var progress:Float = mChapterIndex.toFloat() * 100 / mChapters!!
                        .count()

                mCurrentChapterContent?.let {
                    if (progress < 50) {
                        progress += mCurrentLineIndex.toFloat() * 100 / mChapters!!
                                .count() / mCurrentChapterContent!!.count()
                    } else {
                        progress += (mCurrentLineIndex + mLineNumber).toFloat() * 100 / mChapters!!.count() / mCurrentChapterContent!!.count()
                    }
                }
                if (progress > 100) {
                    progress = 100f
                }
                mProgressLabel.text = "%.2f%%".format(progress)
            }

            mTimeLabel.text = mDateFormat.format(Date())

            val num = if (mCurrentChapterContent!!.size % mLineNumber == 0) { 0 } else 1
            val pageNum = mCurrentChapterContent!!.size / mLineNumber + num
            mPageNumView.text = "${(mCurrentLineIndex + 1) / mLineNumber + 1}/$pageNum"

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
        }

        fun updateData(content: List<String>) {
            mTextView.text = Html.fromHtml(StringUtil.join(content, "<br/>"))
            mTextView.textSize = mNovel.readTextSize.toFloat()
            mTextView.typeface = mNovel.fontFace
            if (mLineNumber == DEFAULT_LINE_NUMBER && mTextView.height > 0) {
                mLineNumber = (mTextView.height - mTextView.paddingTop -
                        mTextView.paddingBottom) / mTextView.lineHeight
                if (content.size > mLineNumber) {
                    mTextView.text = Html.fromHtml(StringUtil.join(content.subList(0, mLineNumber), "<br/>"))
                }
            }
        }

        fun updateBackground(resourceId: Int) {
            mTextView.setBackgroundResource(resourceId)
            val textColor = ContextCompat.getColor(this@ReadActivity,
                    mNovel.readTextColor)
            mTextView.setTextColor(textColor)
            mTimeLabel.setTextColor(textColor)
            mProgressLabel.setTextColor(textColor)
        }
    }

    companion object {
        private val GET_CHAPTER_LIST = 0
        private val DEFAULT_LINE_NUMBER = 40
    }
}
