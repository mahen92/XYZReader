package com.example.xyzreader.ui;

import com.example.xyzreader.data.UpdaterService;

/**
 * Created by Mahendran on 12-03-2017.
 */

public class NetworkInfo {
    private NetworkInterface articleListView;

    public NetworkInfo(NetworkInterface articleListView) {
        this.articleListView = articleListView;
    }

    public void onArticleListItemClick(long articleId, boolean isRefreshing) {
        if (!isRefreshing)
            articleListView.showArticleDetails(articleId);
    }

    public void onArticlesStateChange(@UpdaterService.ArticlesStatus int articlesStatus) {
        switch (articlesStatus) {
            case UpdaterService.ARTICLES_STATUS_UNKNOWN:
                articleListView.showProgressBar();
                break;
            case UpdaterService.ARTICLES_STATUS_NETWORK_ERROR:
                articleListView.onArticlesUpdateFailed("No Internet Connection Available");
                articleListView.hideProgressBar();
                break;
            case UpdaterService.ARTICLES_STATUS_SERVER_ERROR:
                articleListView.onArticlesUpdateFailed("Server Error");
                articleListView.hideProgressBar();
                break;
            default:
                articleListView.hideProgressBar();
        }
    }
}
