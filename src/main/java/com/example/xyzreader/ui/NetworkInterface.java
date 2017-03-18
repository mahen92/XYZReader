package com.example.xyzreader.ui;

/**
 * Created by Mahendran on 12-03-2017.
 */

public interface NetworkInterface {
    void showProgressBar();

    void hideProgressBar();

    void showArticleDetails(long articleId);

    void onArticlesUpdateFailed(String errorMessage);
}
