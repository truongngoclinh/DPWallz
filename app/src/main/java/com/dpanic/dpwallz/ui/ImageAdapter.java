package com.dpanic.dpwallz.ui;

import java.util.ArrayList;
import org.greenrobot.eventbus.EventBus;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dpanic.dpwallz.R;
import com.dpanic.dpwallz.busevent.OpenImageEvent;
import com.dpanic.dpwallz.model.Image;
import com.dpanic.dpwallz.util.Constants;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by dpanic on 9/29/2016.
 * Project: DPWallz
 */

class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    static final int VIEW_TYPE_ITEM = 0;
    static final int VIEW_TYPE_LOADING = 1;
    static final int VIEW_TYPE_AD = 2;

    private final Context mContext;
    private final ArrayList<Image> imageList;
//    private final boolean isDownloaded;
//    @Inject
    AdRequest mNativeAdRequest;
    private final NativeExpressAdView adView;

    // collect user event for analytics
    private int fragmentType = 0;
//    private int memorySize = 0;

    ImageAdapter(Context context, ArrayList<Image> list) {
        mContext = context;
        imageList = list;
//        isDownloaded = downloaded;

        mNativeAdRequest = new AdRequest.Builder().build();

        adView = new NativeExpressAdView(mContext.getApplicationContext());
        String native_ads_id = mContext.getResources().getString(R.string.string_image_list_native_ad_id);
        adView.setAdUnitId(native_ads_id);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                             ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, (int) context.getResources().getDimension(R.dimen.ad_vertical_margin), 0,
                          (int) context.getResources().getDimension(R.dimen.ad_vertical_margin));
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        adView.setLayoutParams(params);
//        memorySize = calculateMultipleSize();
    }

//    private int calculateMultipleSize() {
//        Runtime rt = Runtime.getRuntime();
//        return (int) (rt.maxMemory() / (1024 * 1024));
//    }

    void setFragmentType(int type) {
        this.fragmentType = type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(mContext);

        switch (viewType) {
        case VIEW_TYPE_ITEM:
            View itemView = inflater.inflate(R.layout.item_layout, parent, false);
            return new ImageVH(itemView);
        case VIEW_TYPE_LOADING:
            View loadingView = inflater.inflate(R.layout.loading_item_layout, parent, false);
            return new LoadMoreVH(loadingView);
        case VIEW_TYPE_AD:
            View adViewContainer = inflater.inflate(R.layout.ad_item_layout, parent, false);
            return new AdVH(adViewContainer);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ImageVH) {
            final String imageLink;
            //            if (isDownloaded) {
            //                imageLink = imageList.get(position).getLocalLink();
            //            } else {
            imageLink = imageList.get(position).getLargeLink();
            //            }

            Glide.with(mContext).load(imageLink).asBitmap().sizeMultiplier(0.7f)
                    .animate(R.anim.showing_image_anim)/*.listener(new RequestListener<String, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, String model, Target<GlideDrawable> target,
                                           boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target,
                                               boolean isFromMemoryCache, boolean isFirstResource) {

                    return false;
                }
            })*/.listener(new RequestListener<String, Bitmap>() {
                @Override
                public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target,
                                               boolean isFromMemoryCache, boolean isFirstResource) {

                    Log.e("thanh.dao", "onResourceReady: path = " + imageLink);
                    Log.e("thanh.dao", "onResourceReady: size = " + (resource.getByteCount()/1024));
                    Log.e("thanh.dao", "onResourceReady: isFromMemoryCache = " + isFromMemoryCache);
                    Log.e("thanh.dao", "onResourceReady: isFirstResource = " + isFirstResource);
                    Log.e("thanh.dao", "onResourceReady: resource w = " + resource.getWidth() + " - h = " +
                            resource.getHeight());
                    return false;
                }
            }).diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(((ImageVH) holder).imageView);
        } else if (holder instanceof LoadMoreVH) {
            ((LoadMoreVH) holder).loadingProgressBar.setIndeterminate(true);
        } else if (holder instanceof AdVH) {
            ViewGroup parent = (ViewGroup) adView.getParent();
            if (parent != null) {
                parent.removeView(adView);
            }

            adView.loadAd(mNativeAdRequest);
            ((AdVH) holder).layoutAdContainer.addView(adView);
        }
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Image img = imageList.get(position);
        if (imageList.get(position) == null) {
            return VIEW_TYPE_LOADING;
        } else if (img.getName().equals("ad")) {
            return VIEW_TYPE_AD;
        } else {
            return VIEW_TYPE_ITEM;
        }
        //        return imageList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    class ImageVH extends RecyclerView.ViewHolder {

        @BindView(R.id.item_container)
        FrameLayout itemContainer;

        @BindView(R.id.iv_item)
        ImageView imageView;

        ImageVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getAdapterPosition() >= 0 && getAdapterPosition() < getItemCount()) {
                        EventBus.getDefault().post(new OpenImageEvent(imageList.get(getAdapterPosition())));
                        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(mContext);
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.VALUE, "1");
                        String action = Constants.RECENT_IMAGE;
                        switch (fragmentType) {
                        case Constants.FRAG_TYPE_RECENT:
                            action = Constants.RECENT_IMAGE;
                            break;
                        case Constants.FRAG_TYPE_MONTH_POPULAR:
                            action = Constants.MONTH_POPULAR_IMAGE;
                            break;
                        case Constants.FRAG_TYPE_ALL_TIME_POPULAR:
                            action = Constants.ALL_TIME_POPULAR_IMAGE;
                            break;
                        case Constants.FRAG_TYPE_SEARCH:
                            action = Constants.SEARCH_IMAGE;
                            break;
                        }

                        analytics.logEvent(action, bundle);


                        //                    Intent intent = new Intent(mContext, DetailActivity.class);
                        //
                        //                    Bundle detailBundle = new Bundle();
                        //                    detailBundle.putParcelable(Constants.IMAGE_INSTANCE, imageList.get(getAdapterPosition()));
                        //
                        //                    intent.putExtras(detailBundle);
                        //                    ActivityOptionsCompat options = ActivityOptionsCompat.
                        //                            makeSceneTransitionAnimation(((Activity) mContext), v, "open_detail");
                        //
                        //                    mContext.startActivity(intent, options.toBundle());
                    }
                }
            });
        }
    }

    class LoadMoreVH extends RecyclerView.ViewHolder {

        @BindView(R.id.progressbar_load_more)
        ContentLoadingProgressBar loadingProgressBar;

        LoadMoreVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class AdVH extends RecyclerView.ViewHolder {
        @BindView(R.id.image_list_ad_container)
        RelativeLayout layoutAdContainer;

        AdVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
