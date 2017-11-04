package com.ziank.novelreader.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.ziank.novelreader.database.DatabaseManager;
import com.ziank.novelreader.model.Book;

import java.util.List;

/**
 * Created by zhaixianqi on 2017/10/23.
 */

public class FavoriatedBookListLoader extends AsyncTaskLoader<List<Book>> {

    public FavoriatedBookListLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Book> loadInBackground() {
        return DatabaseManager.Companion.getSharedManager().fetchAllBooks();
    }
}
