package com.dpanic.dpwallz.ui;

import java.util.ArrayList;
import org.greenrobot.eventbus.EventBus;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.dpanic.dpwallz.R;
import com.dpanic.dpwallz.ui.base.BaseFragment;
import com.dpanic.dpwallz.busevent.NoResultFoundEvent;
import com.dpanic.dpwallz.control.HTMLParsingUtil;
import com.dpanic.dpwallz.model.Image;
import com.dpanic.dpwallz.util.Constants;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;

/**
 * Created by dpanic on 29/09/2016.
 * Project: DPWallz
 */

public class ImageListFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.image_list_recycler_view)
    RecyclerView rvImageList;

    @BindView(R.id.image_list_container)
    SwipeRefreshLayout imageListContainer;

    @BindView(R.id.image_list_error_container)
    LinearLayout layoutError;

    //    @BindView(R.id.image_list_btn_no_internet)
    //    LinearLayout btnRetry;

    ArrayList<Image> imageList;
    private ImageAdapter imageListAdapter;
    private GridLayoutManager imageListLayoutManager;
    private int totalItemCount;
    private int firstVisibleItem;
    private int visibleItemCount;
    private boolean isLoading = false;
    private int visibleThreshold = 5;
    private int curLastPage = 0;
    private String mainLink = "";
    boolean isShowAds = false;
    private int fragmentType;
    private int orientation = Configuration.ORIENTATION_PORTRAIT;

    public static ImageListFragment newInstance(int type, String link) {
        ImageListFragment fragment = new ImageListFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(Constants.FRAGMENT_TYPE, type);
        bundle.putString(Constants.MAIN_LINK, link);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_image_list_layout, container, false);
        ButterKnife.bind(this, fragmentView);
        return fragmentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        imageList = new ArrayList<>();
        imageListContainer.setRefreshing(true);
        rvImageList.setVisibility(View.GONE);

        Bundle bundle = getArguments();
        fragmentType = bundle.getInt(Constants.FRAGMENT_TYPE);
        mainLink = bundle.getString(Constants.MAIN_LINK);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        isShowAds = preferences.getBoolean(Constants.IS_SHOW_ADS, false);

        initView();
        loadDataFromPage(curLastPage + 1);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        orientation = newConfig.orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            imageListLayoutManager.setSpanCount(2);
        } else {
            imageListLayoutManager.setSpanCount(4);
        }
    }

    private void loadDataFromPage(int page) {
        addSubscription(HTMLParsingUtil.getImageListStartAt(mainLink, page).subscribe(new
                                                                                                    Observer<ArrayList<Image>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable throwable) {
//                if (throwable instanceof SocketTimeoutException) {
                if (isLoading) {
                    imageList.remove(imageList.size() - 1);
                    imageListAdapter.notifyItemRemoved(imageList.size());

                    isLoading = false;
                } else {
                    imageListContainer.setRefreshing(false);
                }
                throwable.printStackTrace();

                if (imageList.size() == 0) {
                    layoutError.setVisibility(View.VISIBLE);
                }

                imageListContainer.setRefreshing(false);
            }

            @Override
            public void onNext(ArrayList<Image> images) {
                int size = images.size();
                if (size == 0 && imageList.size() == 0 && fragmentType == Constants.FRAG_TYPE_SEARCH) {
                    EventBus.getDefault().post(new NoResultFoundEvent());
                }
                if (isLoading) {
                    imageList.remove(imageList.size() - 1);
                    imageListAdapter.notifyItemRemoved(imageList.size());

                    isLoading = false;
                } else {
                    imageListContainer.setRefreshing(false);
                    rvImageList.setVisibility(View.VISIBLE);
                }

                for (int i = 0; i < size; i++) {
                    if ((i != 0) && (i % Constants.AD_PER_IMAGES == 0) && isShowAds) {
                        Image img = new Image();
                        img.setName("ad");
                        imageList.add(img);
                    }

                    imageList.add(images.get(i));
                }

                //                imageList.addAll(images);
                imageListAdapter.notifyDataSetChanged();

                curLastPage += 3;

                imageListContainer.setRefreshing(false);
            }
        }));
    }

    private void initView() {
        imageListContainer.setOnRefreshListener(this);
        imageListContainer.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        imageListAdapter = new ImageAdapter(getActivity(), imageList);
        imageListAdapter.setFragmentType(fragmentType);

        configLayoutManager();
        rvImageList.setLayoutManager(imageListLayoutManager);

        rvImageList.setAdapter(imageListAdapter);

        rvImageList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    totalItemCount = imageListLayoutManager.getItemCount();
                    //                    pastVisibleItems = layoutManager.findLastVisibleItemPosition();
                    firstVisibleItem = imageListLayoutManager.findFirstVisibleItemPosition();
                    visibleItemCount = imageListLayoutManager.getChildCount();

                    if (!isLoading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                        isLoading = true;
                        onLoadMore();
                    }
                }
            }
        });
    }

    private void configLayoutManager() {
        orientation = getActivity().getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            imageListLayoutManager = new GridLayoutManager(getActivity(), 2);
        } else {
            imageListLayoutManager = new GridLayoutManager(getActivity(), 4);
        }

        imageListLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (imageListAdapter.getItemViewType(position)) {
                case ImageAdapter.VIEW_TYPE_ITEM:
                    return 1;
                case ImageAdapter.VIEW_TYPE_LOADING:
                case ImageAdapter.VIEW_TYPE_AD:
                    return 2;
                }
                return 0;
            }
        });
    }

    private void onLoadMore() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                imageList.add(null);
                imageListAdapter.notifyItemInserted(imageList.size() - 1);
            }
        };
        handler.post(runnable);


        loadDataFromPage(curLastPage + 1);
    }

    @Override
    public void onRefresh() {

        layoutError.setVisibility(View.GONE);
        if (curLastPage == 0) {
            loadDataFromPage(curLastPage + 1);
        } else {
            imageListContainer.setRefreshing(false);
        }
    }

    @OnClick(R.id.image_list_btn_retry)
    void onClick() {
        imageListContainer.setRefreshing(true);
        onRefresh();
    }

    @Override
    public void onDestroy() {
        if (imageListAdapter != null) {
            imageListAdapter = null;
        }

        if (imageListLayoutManager != null) {
            imageListLayoutManager = null;
        }
        super.onDestroy();
    }
}
