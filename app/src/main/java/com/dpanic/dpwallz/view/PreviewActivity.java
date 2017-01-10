package com.dpanic.dpwallz.view;

import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dpanic.dpwallz.R;
import com.dpanic.dpwallz.busevent.DownloadEvent;
import com.dpanic.dpwallz.control.FileUtil;
import com.dpanic.dpwallz.control.ImageActionHelper;
import com.dpanic.dpwallz.model.DataManager;
import com.dpanic.dpwallz.model.Image;
import com.dpanic.dpwallz.util.Constants;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PreviewActivity extends BaseActivity {

    @BindView(R.id.preview_toolbar)
    Toolbar toolbar;

    @BindView(R.id.preview_image)
    ImageView preImage;

    @BindView(R.id.preview_scroll_view)
    HorizontalScrollView previewScrollView;

    @BindView(R.id.preview_error_container)
    RelativeLayout layoutError;

    @BindView(R.id.preview_loading_progress)
    ContentLoadingProgressBar clpImageLoading;

    private ActionBar actionBar;

    private static class ScrollingHandler extends Handler {
        private final WeakReference<PreviewActivity> mActivity;

        ScrollingHandler(PreviewActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            PreviewActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleScrolling();
            }
        }
    }

    private final ScrollingHandler mHandler = new ScrollingHandler(this);

    private String imgLink;
    private ImageActionHelper actionHelper;
    private Image image;
    private DataManager mDataManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_preview);

        ButterKnife.bind(this);


        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.hide();
        }
        setTitle("");

        init();
    }

    @Override
    protected void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onMessage(DownloadEvent event) {
        if (event.getStatus() == DownloadEvent.STATUS_COMPLETE) {
            if (event.isForView()) {
                loadPreviewImage();
            }

            image.setLocalLink(imgLink);

            mDataManager.forceToAddImage(image);
        } else if (event.getStatus() == DownloadEvent.STATUS_CANCEL_BY_USER) {
            finish();
        } else {
            layoutError.setVisibility(View.VISIBLE);
            clpImageLoading.setVisibility(View.GONE);
            Throwable throwable = event.getException();

            actionBar.show();
            FileUtil.deleteFile(imgLink);
            //            if (throwable instanceof SocketTimeoutException || throwable instanceof UnknownHostException) {
            //                Toast.makeText(this, getResources().getString(R.string.string_no_internet_connection),
            //                               Toast.LENGTH_SHORT).show();
            //            }
            //            throwable.printStackTrace();
        }
    }

    private void init() {
        mDataManager = DataManager.getInstance(getApplicationContext());
//        mDataManager = new DataManager(this);

        Intent intent = getIntent();
        image = intent.getParcelableExtra(Constants.IMAGE_INSTANCE);

        imgLink = FileUtil.getLocalPath(image.getOriginalLink());

        actionHelper = new ImageActionHelper(this, image);

        if (FileUtil.isFileDownloaded(image.getOriginalLink())) {
            loadPreviewImage();
        } else {
            actionHelper.downloadForPreview();
        }

        previewScrollView.setOnTouchListener(new View.OnTouchListener() {
            float startX = 0, startY = 0, endX, endY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mHandler.removeMessages(0);
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getRawX();
                    startY = event.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    endX = event.getRawX();
                    endY = event.getRawY();

                    if (Math.abs(endX - startX) < 20 && Math.abs(endY - startY) < 20) {
                        if (actionBar != null) {
                            if (actionBar.isShowing()) {
                                actionBar.hide();
                            } else {
                                actionBar.show();
                            }
                        }
                    }
                    break;
                }
                return false;
            }
        });
    }

    public void handleScrolling() {
        previewScrollView.smoothScrollBy(2, 0);
        mHandler.sendEmptyMessageDelayed(0, 50);
    }
    private void loadPreviewImage() {
        if (actionHelper.checkPermission()) {
            return;
        }

        clpImageLoading.setVisibility(View.VISIBLE);
        Glide.with(this).load(imgLink).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE).dontTransform()
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target,
                                               boolean isFirstResource) {
                        if (e instanceof SocketTimeoutException) {
                            Toast.makeText(PreviewActivity.this,
                                           getResources().getString(R.string.string_no_internet_connection),
                                           Toast.LENGTH_SHORT).show();
                        }
                        if (e != null) {
                            e.printStackTrace();
                        }
                        clpImageLoading.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target,
                                                   boolean isFromMemoryCache, boolean isFirstResource) {
                        preImage.setVisibility(View.VISIBLE);
                        clpImageLoading.setVisibility(View.GONE);
                        mHandler.sendEmptyMessage(0);
                        Log.e("thanh.dao", "onResourceReady: PreviewActivity");
                        Log.e("thanh.dao", "onResourceReady: size = " + (resource.getByteCount()/1024));
                        Log.e("thanh.dao", "onResourceReady: isFromMemoryCache = " + isFromMemoryCache);
                        Log.e("thanh.dao", "onResourceReady: isFirstResource = " + isFirstResource);
                        Log.e("thanh.dao", "onResourceReady: resource w = " + resource.getWidth() + " - h = " +
                                resource.getHeight());
                        return false;
                    }
                }).skipMemoryCache(true).into(preImage);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_preview_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            onBackPressed();
            break;
        //        case R.id.pre_menu_download:
        //            actionHelper.downloadAction();
        //            break;
        case R.id.pre_menu_set_as:
            actionHelper.setAsAction();
            break;
        case R.id.pre_menu_share:
            actionHelper.performShare();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (actionHelper.getCurrentAction() == -1) {
                loadPreviewImage();
            } else {
                actionHelper.resumeInvokedAction();
            }
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (actionHelper != null) {
            actionHelper.destruct();
            actionHelper = null;
        }

        Glide.clear(preImage);

//        if (mDataManager != null) {
//            mDataManager.destruct();
//            mDataManager = null;
//        }

        super.onDestroy();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.preview_btn_retry)
    void onClick() {
        layoutError.setVisibility(View.GONE);
        actionHelper.downloadForPreview();
        actionBar.hide();
    }
}
