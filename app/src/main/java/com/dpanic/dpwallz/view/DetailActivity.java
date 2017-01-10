package com.dpanic.dpwallz.view;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dpanic.dpwallz.R;
import com.dpanic.dpwallz.busevent.DownloadEvent;
import com.dpanic.dpwallz.busevent.OpenCategoryEvent;
import com.dpanic.dpwallz.control.DownloadUtil;
import com.dpanic.dpwallz.control.FileUtil;
import com.dpanic.dpwallz.control.FixFlowLayoutManager;
import com.dpanic.dpwallz.control.HTMLParsingUtil;
import com.dpanic.dpwallz.control.ImageActionHelper;
import com.dpanic.dpwallz.model.Category;
import com.dpanic.dpwallz.model.DataManager;
import com.dpanic.dpwallz.model.Image;
import com.dpanic.dpwallz.model.ImageDetail;
import com.dpanic.dpwallz.util.Constants;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.xiaofeng.flowlayoutmanager.Alignment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;
import rx.functions.Action1;

public class DetailActivity extends BaseActivity {

    @BindView(R.id.detail_toolbar)
    Toolbar toolbar;

    @BindView(R.id.clp_backdrop_loading)
    ContentLoadingProgressBar clpBackdropLoading;

    @BindView(R.id.detail_backdrop_image)
    ImageView backDropImage;

    @BindView(R.id.detail_title)
    TextView tvTitle;

    @BindView(R.id.detail_author)
    TextView tvAuthor;

    @BindView(R.id.dimen_text)
    TextView tvDimen;

    @BindView(R.id.size_text)
    TextView tvSize;

    @BindView(R.id.rv_colors)
    RecyclerView rvColors;

    @BindView(R.id.rv_tags)
    RecyclerView rvTags;

    @BindView(R.id.fav_on_btn_container)
    LinearLayout btnFavOn;

    @BindView(R.id.fav_off_btn_container)
    LinearLayout btnFavOff;

    @BindView(R.id.detail_dimen_vertical_divider)
    ImageView verticalDivider;

    @BindView(R.id.detail_dimen_container)
    RelativeLayout detailDimenContainer;

    @BindView(R.id.detail_container)
    LinearLayout mContentLayout;

    @BindView(R.id.clp_detail_content)
    ContentLoadingProgressBar clpContentLoading;

    @BindView(R.id.detail_error_container)
    LinearLayout layoutError;

    @BindView(R.id.detail_ad_container)
    RelativeLayout layoutAd;
    //
    //    @BindView(R.id.content_container)
    //    RelativeLayout contentContainer;
    //
    //    @BindView(R.id.detail_appbar)
    //    AppBarLayout appbar;
    //
    //    @BindView(R.id.detail_collapsing_toolbar)
    //    CollapsingToolbarLayout collapsingToolbar;

    private Image image;
    private Context mContext;
    private ColorAdapter mColorAdapter;
    private TagAdapter mTagAdapter;
    private ArrayList<String> colors;
    private ArrayList<String> tags;
    private ImageActionHelper actionHelper;
    private boolean performingAction = false;
    private boolean isFavorite = false;
    private boolean isShowAds;
    private DataManager mDataManager;
    private NativeExpressAdView adView;

//    private CompositeSubscription compositeSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

//        compositeSubscription = new CompositeSubscription();

        mContext = this;

        setSupportActionBar(toolbar);
        setTitle("");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        isShowAds = preferences.getBoolean(Constants.IS_SHOW_ADS, false);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        image = bundle.getParcelable(Constants.IMAGE_INSTANCE);

        mDataManager = DataManager.getInstance(mContext.getApplicationContext());
//        mDataManager = new DataManager(mContext);

        loadBackdropImage(image);
        backDropImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent previewIntent = new Intent(mContext, PreviewActivity.class);

                Bundle previewBundle = new Bundle();
                previewBundle.putParcelable(Constants.IMAGE_INSTANCE, image);
                previewIntent.putExtras(previewBundle);

                startActivity(previewIntent);
            }
        });

        actionHelper = new ImageActionHelper(this, image);

        initView();
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);
        performingAction = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onMessage(OpenCategoryEvent event) {
        launchSearchActivity(event.getCategory(), event.isColor());
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onMessage(DownloadEvent event) {
        if (event.getStatus() == DownloadEvent.STATUS_COMPLETE) {
            Log.e("thanh.dao", "DownloadEvent.STATUS_COMPLETE: ");
            image.setLocalLink(FileUtil.getLocalPath(image.getOriginalLink()));
            mDataManager.forceToAddImage(image);
        } else if (event.getStatus() == DownloadEvent.STATUS_ERROR) {
            Throwable throwable = event.getException();
            if (throwable != null) {
                if (throwable instanceof SocketTimeoutException || throwable instanceof UnknownHostException) {
                    Toast.makeText(this, getResources().getString(R.string.string_no_internet_connection),
                                   Toast.LENGTH_SHORT).show();
                } else {
                    throwable.printStackTrace();
                }
            }
        }

        performingAction = false;
    }


    private void launchSearchActivity(Category category, boolean isColor) {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra(Constants.CATEGORY_INSTANCE, category);
        intent.putExtra(Constants.IS_COLOR_SEARCH, isColor);

        startActivity(intent);
        overridePendingTransition(R.anim.above_activity_enter, R.anim.below_activity_exit);
    }

    private void initView() {
        colors = new ArrayList<>();
        tags = new ArrayList<>();

        mColorAdapter = new ColorAdapter(mContext, colors);
        FixFlowLayoutManager mColorLayoutManager = new FixFlowLayoutManager();
        mColorLayoutManager.setAutoMeasureEnabled(true);
        mColorLayoutManager.setAlignment(Alignment.LEFT);
        rvColors.setAdapter(mColorAdapter);
        rvColors.setLayoutManager(mColorLayoutManager);
        rvColors.setHasFixedSize(true);                 // fix scroll problem of rv in nestedscrollview
        rvColors.setNestedScrollingEnabled(false);      // fix scroll problem of rv in nestedscrollview

        mTagAdapter = new TagAdapter(mContext, tags);
        FixFlowLayoutManager mTagLayoutManager = new FixFlowLayoutManager();
        mTagLayoutManager.setAutoMeasureEnabled(true);
        mTagLayoutManager.setAlignment(Alignment.LEFT);
        rvTags.setAdapter(mTagAdapter);
        rvTags.setLayoutManager(mTagLayoutManager);
        rvTags.setHasFixedSize(true);                   // fix scroll problem of rv in nestedscrollview
        rvTags.setNestedScrollingEnabled(false);        // fix scroll problem of rv in nestedscrollview

        ViewTreeObserver viewTreeObserver = detailDimenContainer.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (detailDimenContainer.getMeasuredHeight() > 0) {
                    ViewTreeObserver vto = detailDimenContainer.getViewTreeObserver();
                    vto.removeOnGlobalLayoutListener(this);
                    RelativeLayout.LayoutParams layoutParams =
                            (RelativeLayout.LayoutParams) verticalDivider.getLayoutParams();
                    layoutParams.height = detailDimenContainer.getMeasuredHeight();
                    Log.e("thanh.dao", "refreshData: height = " + detailDimenContainer.getHeight());

                    verticalDivider.setLayoutParams(layoutParams);
                }
            }
        });

        boolean isNetworkAvailable = DownloadUtil.checkNetworkStatus(mContext.getApplicationContext());

        AdRequest mNativeAdRequest = new AdRequest.Builder().build();
        adView = new NativeExpressAdView(mContext.getApplicationContext());
        String native_ads_id = getResources().getString(R.string.string_detail_native_ad_id);
        adView.setAdUnitId(native_ads_id);
        adView.setAdSize(new AdSize(360, 100));
        adView.loadAd(mNativeAdRequest);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                             ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, (int) getResources().getDimension(R.dimen.ad_vertical_margin), 0,
                          (int) getResources().getDimension(R.dimen.ad_vertical_margin));
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        adView.setLayoutParams(params);

        if (isNetworkAvailable && isShowAds) {
            layoutAd.setVisibility(View.VISIBLE);
            layoutAd.addView(adView);
        }
    }

    @SuppressWarnings("unused")
    @OnClick({ R.id.fav_off_btn_container, R.id.fav_on_btn_container })
    void favClick(View view) {
        switch (view.getId()) {
        case R.id.fav_off_btn_container:
            image.setFavorite(true);
            btnFavOn.setVisibility(View.VISIBLE);
            btnFavOff.setVisibility(View.GONE);
            break;
        case R.id.fav_on_btn_container:
            image.setFavorite(false);
            btnFavOn.setVisibility(View.GONE);
            btnFavOff.setVisibility(View.VISIBLE);
            break;
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_download)
    void downloadAction() {
        if (!performingAction) {
            performingAction = true;
            actionHelper.downloadAction();
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_set_as)
    void setAsAction() {
        if (!performingAction) {
            performingAction = true;
            actionHelper.setAsAction();
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.detail_btn_retry)
    void retryAction() {
        clpContentLoading.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);

        loadData();
    }

    private void loadData() {
        //        mDataManager.isFavoriteImage(image).subscribe(new Action1<Boolean>() {
        //            @Override
        //            public void call(Boolean isFav) {
        //                isFavorite = isFav;
        //
        //                image.setFavorite(isFavorite);
        //                if (isFavorite) {
        //                    btnFavOn.setVisibility(View.VISIBLE);
        //                    btnFavOff.setVisibility(View.GONE);
        //                } else {
        //                    btnFavOn.setVisibility(View.GONE);
        //                    btnFavOff.setVisibility(View.VISIBLE);
        //                }
        //            }
        //        });


        compositeSubscription.add(mDataManager.getImage(image).subscribe(new Action1<Image>() {
            @Override
            public void call(Image img) {
                if (image != null) {
                    isFavorite = img.isFavorite();
                    if (!img.getLocalLink().equals("")) {
                        image.setLocalLink(img.getLocalLink());
                    }

                    if (img.isFavorite()) {
                        image.setFavorite(img.isFavorite());

                        btnFavOff.setVisibility(View.GONE);
                        btnFavOn.setVisibility(View.VISIBLE);
                    }
                }
            }
        }));
        //        RealmResults<Image> result = realm.where(Image.class).equalTo("pexelId", image.getPexelId()).findAll();
        //        if (result.size() > 0) {
        //            Image img = result.get(0);
        //            isFavorite = img.isFavorite();
        //            if (!img.getLocalLink().equals("")) {
        //                image.setLocalLink(img.getLocalLink());
        //            }
        //
        //            if (img.isFavorite()) {
        //                image.setFavorite(img.isFavorite());
        //
        //                btnFavOff.setVisibility(View.GONE);
        //                btnFavOn.setVisibility(View.VISIBLE);
        //            }
        //        }

        Log.e("thanh.dao", "loadData: detail link = " + image.getDetailLink());
        compositeSubscription.add(HTMLParsingUtil.getImageDetailFromUrl(image.getDetailLink())
                .subscribe(new Observer<ImageDetail>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (throwable instanceof SocketTimeoutException || throwable instanceof UnknownHostException) {
                            layoutError.setVisibility(View.VISIBLE);
                            clpContentLoading.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onNext(ImageDetail imageDetail) {
                        refreshData(imageDetail);
                    }
                }));
    }

    private void refreshData(ImageDetail imageDetail) {
        tvTitle.setText(imageDetail.getTitle());
        tvAuthor.setText(String.format(getResources().getString(R.string.string_prefix_by), imageDetail.getAuthor()));

        if (image.isFavorite()) {
            btnFavOn.setVisibility(View.VISIBLE);
            btnFavOff.setVisibility(View.GONE);
        } else {
            btnFavOn.setVisibility(View.GONE);
            btnFavOff.setVisibility(View.VISIBLE);
        }

        tvDimen.setText(imageDetail.getDimen());
        tvSize.setText(imageDetail.getSize());

        if (colors.size() == 0) {
            colors.addAll(imageDetail.getColors());
        }

        if (tags.size() == 0) {
            tags.addAll(imageDetail.getTags());
        }

        mColorAdapter.notifyDataSetChanged();
        mTagAdapter.notifyDataSetChanged();

        clpContentLoading.setVisibility(View.GONE);
        mContentLayout.setVisibility(View.VISIBLE);
    }

    private void loadBackdropImage(Image image) {
        clpBackdropLoading.setVisibility(View.VISIBLE);
        Glide.with(this).load(image.getLargeLink()).asBitmap().diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target,
                                               boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target,
                                                   boolean isFromMemoryCache, boolean isFirstResource) {
                        Log.e("thanh.dao", "onResourceReady: DetailActivity");
                        Log.e("thanh.dao", "onResourceReady: size = " + (resource.getByteCount()/1024));
                        Log.e("thanh.dao", "onResourceReady: isFromMemoryCache = " + isFromMemoryCache);
                        Log.e("thanh.dao", "onResourceReady: isFirstResource = " + isFirstResource);
                        Log.e("thanh.dao", "onResourceReady: resource w = " + resource.getWidth() + " - h = " +
                                resource.getHeight());
                        clpBackdropLoading.setVisibility(View.GONE);
                        return false;
                    }
                }).dontAnimate().into(backDropImage);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            onBackPressed();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            actionHelper.resumeInvokedAction();
        }
    }

    @Override
    protected void onDestroy() {

        Glide.clear(backDropImage);

        if (layoutAd != null) {
            layoutAd.removeAllViews();
        }
        if (adView != null) {
            adView.destroy();
            adView.setAdListener(null);
            adView = null;
        }

        if (mColorAdapter != null) {
            mColorAdapter = null;
        }
        if (mTagAdapter != null) {
            mTagAdapter = null;
        }
        if (colors != null) {
            colors = null;
        }
        if (tags != null) {
            tags = null;
        }
        if (actionHelper != null) {
            actionHelper.destruct();
            actionHelper = null;
        }
        if (mContext != null) {
            mContext = null;
        }
//        if (mDataManager != null) {
//            mDataManager.destruct();
//            mDataManager = null;
//        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!isFavorite && image.isFavorite()) {
            mDataManager.forceToAddImage(image);
        } else if (isFavorite && !image.isFavorite()) {
            if (image.getLocalLink().equals("")) {
                compositeSubscription.add(mDataManager.deleteImage(image).subscribe
                (new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        Log.e("thanh.dao", "onBackPress: deleted " + integer + " item");
                    }
                }));
            } else {
                mDataManager.forceToAddImage(image);
            }
        }

        if (isTaskRoot()) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                finishAfterTransition();
//            } else {
//                finish();

//            }
        }
//        else {
//            super.onBackPressed();
//        }
        finish();
        overridePendingTransition(R.anim.below_activity_enter, R.anim.above_activity_exit);
    }
}
