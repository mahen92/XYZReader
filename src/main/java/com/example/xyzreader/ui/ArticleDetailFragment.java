package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";
    private static final float PARALLAX_FACTOR = 1.5f;

    private Cursor cursor;
    private long itemId;
    private View rootView;
    private int mutedColor = 0xa4c639;
    private ObservableScrollView scrollView;
    private ColorDrawable statusBarColorDrawable;
    private int topInset;
    private ImageView photoView;
    private int scrollY;
    private int statusBarFullOpacityBottom;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Toolbar toolbar;
    private AppBarLayout appBar;
    private FloatingActionButton floatingActionButton;
    private View heading;
    private TextView wiki;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            itemId = getArguments().getLong(ARG_ITEM_ID);
        }
        statusBarFullOpacityBottom = getResources().getDimensionPixelSize(
                R.dimen.detail_card_top_margin);
        setHasOptionsMenu(true);
    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        collapsingToolbarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.article_detail_toolbar);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        photoView = (ImageView) rootView.findViewById(R.id.photo);
        scrollView = (ObservableScrollView) rootView.findViewById(R.id.scrollView);
        statusBarColorDrawable = new ColorDrawable(0);
        floatingActionButton = (FloatingActionButton) getActivity().findViewById(R.id.share_fab);
        heading = rootView.findViewById(R.id.meta_bar);



       // boolean shouldAddScrollViewTranslations = getResources().getBoolean(R.bool.add_scroll_view_translations);

        toolbar.setTitle("");
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final String appPackageName = getActivity().getPackageName();
        rootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Hey, I have just seen "+ cursor.getString(ArticleLoader.Query.TITLE) +" XYZReader App!")
                        .getIntent(), getString(R.string.action_share)));
            }
        });



        bindViews();
        updateStatusBar();
       /* if (shouldAddScrollViewTranslations) {
            addScrollViewTranslations();
        }*/
        return rootView;
    }


    private void bindViews() {
        if (rootView == null) {
            return;
        }

        TextView titleView = (TextView) rootView.findViewById(R.id.article_title);
        TextView bylineView = (TextView) rootView.findViewById(R.id.article_byline);
        bylineView.setMovementMethod(new LinkMovementMethod());
        TextView bodyView = (TextView) rootView.findViewById(R.id.article_body);

        if (cursor != null) {
            rootView.setAlpha(0);
            rootView.setVisibility(View.VISIBLE);
            rootView.animate().alpha(1);
            titleView.setText(cursor.getString(ArticleLoader.Query.TITLE));
            bylineView.setText(
                    DateUtils.getRelativeTimeSpanString(
                            cursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by "
                            + cursor.getString(ArticleLoader.Query.AUTHOR));
            bodyView.setText(Html.fromHtml(cursor.getString(ArticleLoader.Query.BODY)));
            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(cursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
                                Palette p = Palette.generate(bitmap, 12);
                                mutedColor = p.getDarkMutedColor(0xa4c639);
                                photoView.setImageBitmap(imageContainer.getBitmap());
                                rootView.findViewById(R.id.meta_bar)
                                        .setBackgroundColor(0xa4c639);
                                if (collapsingToolbarLayout != null)
                                    collapsingToolbarLayout.setContentScrimColor(0xa4c639);
                                updateStatusBar();
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });
        } else {
            rootView.setVisibility(View.GONE);
            titleView.setText(" ");
            bylineView.setText(" ");
            bodyView.setText(" ");
        }
    }

    private void updateStatusBar() {
        int color = 0;
        if (photoView != null && topInset != 0 && scrollY > 0) {
            float f = progress(scrollY,
                    statusBarFullOpacityBottom - topInset * 3,
                    statusBarFullOpacityBottom - topInset);
            color = Color.argb((int) (255 * f),
                    (int) (Color.red(mutedColor) * 0.9),
                    (int) (Color.green(mutedColor) * 0.9),
                    (int) (Color.blue(mutedColor) * 0.9));
        }
        statusBarColorDrawable.setColor(color);
    }

    private void addScrollViewTranslations() {
        scrollView.setCallbacks(new ObservableScrollView.Callbacks() {
            @Override
            public void onScrollChanged() {
                scrollY = scrollView.getScrollY();
                ViewGroup bodyContainer = (ViewGroup) rootView.findViewById(R.id.article_detail_toolbar);
                getActivityCast().onUpButtonFloorChanged(itemId, ArticleDetailFragment.this);
                int translationY = (int) (scrollY - scrollY / PARALLAX_FACTOR);
                bodyContainer.setTranslationY(-translationY);
                photoView.setTranslationY(translationY * PARALLAX_FACTOR);
                updateStatusBar();
            }
        });
    }

    static float progress(float v, float min, float max) {
        return constrain((v - min) / (max - min), 0, 1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    static float constrain(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), itemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        this.cursor = cursor;
        if (this.cursor != null && !this.cursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            this.cursor.close();
            this.cursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        cursor = null;
        bindViews();
    }
}