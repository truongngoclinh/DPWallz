package com.dpanic.dpwallz.view;

import java.util.ArrayList;
import java.util.List;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import com.dpanic.dpwallz.R;
import com.dpanic.dpwallz.control.FileUtil;
import com.dpanic.dpwallz.model.DataManager;
import com.dpanic.dpwallz.model.Image;
import com.dpanic.dpwallz.util.Constants;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by dpanic on 10/14/2016.
 * Project: DPWallz
 */

public class FavoriteFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private ArrayList<Image> favList;
    private ArrayList<Image> displayList;

    @BindView(R.id.image_list_recycler_view)
    RecyclerView rvFavorite;

    @BindView(R.id.image_list_container)
    SwipeRefreshLayout refreshLayout;

    //    @BindView(R.id.no_download_layout)
    //    LinearLayout noDownloadLayout;
    //
    //    @BindView(R.id.no_fav_layout)
    //    LinearLayout noFavLayout;

    @BindView(R.id.no_download_view_stub)
    ViewStub noDownloadViewStub;

    @BindView(R.id.no_fav_view_stub)
    ViewStub noFavViewStub;

    View noDownloadView;
    View noFavView;

    private ImageAdapter iaFavorite;
    private int totalItemCount;
    private int firstVisibleItem;
    private int visibleItemCount;
    private boolean isLoading = false;
    private int visibleThreshold = 5;
    private int setCount = 0;

    private static final int batchSize = 20;
    private boolean isFavorite;
    private DataManager mDataManager;

    public static FavoriteFragment getInstance(boolean isFavorite) {
        FavoriteFragment favoriteFragment = new FavoriteFragment();

        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.IS_FAVORITE_FRAGMENT, isFavorite);

        favoriteFragment.setArguments(bundle);

        return favoriteFragment;
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

        compositeSubscription = new CompositeSubscription();

        Bundle bundle = getArguments();
        isFavorite = bundle.getBoolean(Constants.IS_FAVORITE_FRAGMENT);

        //        mDataManager = DataManager.getInstance(getActivity());
        mDataManager = new DataManager(getActivity());

        if (noDownloadView == null) {
            noDownloadView = noDownloadViewStub.inflate();
        }

        if (noFavView == null) {
            noFavView = noFavViewStub.inflate();
        }

        favList = new ArrayList<>();
        displayList = new ArrayList<>();
        iaFavorite = new ImageAdapter(getActivity(), displayList);
        final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        rvFavorite.setAdapter(iaFavorite);
        rvFavorite.setLayoutManager(layoutManager);

        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        refreshLayout.setRefreshing(true);

        rvFavorite.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    totalItemCount = layoutManager.getItemCount();
                    //                    pastVisibleItems = layoutManager.findLastVisibleItemPosition();
                    firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                    visibleItemCount = layoutManager.getChildCount();

                    if (!isLoading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                        isLoading = true;
                        onLoadMore();
                    }
                }
            }
        });


        initData();
    }

    private void onLoadMore() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                favList.add(null);
                iaFavorite.notifyItemInserted(favList.size() - 1);
            }
        };
        handler.post(runnable);

        addDataBatch(setCount);
    }

    private void addDataBatch(int setCount) {
        int nextSetCount = setCount + 1;
        int startIndex = setCount * batchSize;
        int endIndex = nextSetCount * batchSize;
        int favListSize = favList.size();

        for (int i = startIndex; i < endIndex && i < favListSize; i++) {
            displayList.add(favList.get(i));
        }

        iaFavorite.notifyDataSetChanged();
    }

    private void initData() {
        if (isFavorite) {
            compositeSubscription.add(mDataManager.getFavoriteImageList().subscribeOn(Schedulers.io())
                                              .observeOn(AndroidSchedulers.mainThread())
                                              .subscribe(new Action1<List<Image>>() {
                                                  @Override
                                                  public void call(List<Image> images) {
                                                      if (noFavView != null) {
                                                          if (images.size() == 0) {
                                                              noFavView.setVisibility(View.VISIBLE);
                                                          } else {
                                                              noFavView.setVisibility(View.GONE);
                                                          }
                                                      }

                                                      processInitData(images);
                                                  }
                                              }));
        } else {
            compositeSubscription.add(mDataManager.getHistoryImageList().subscribeOn(Schedulers.io())
                                              .observeOn(AndroidSchedulers.mainThread())
                                              .subscribe(new Action1<List<Image>>() {
                                                  @Override
                                                  public void call(List<Image> images) {
                                                      if (noDownloadView != null) {
                                                          if (images.size() == 0) {
                                                              noDownloadView.setVisibility(View.VISIBLE);
                                                          } else {
                                                              noDownloadView.setVisibility(View.GONE);
                                                          }
                                                      }

                                                      processInitData(images);
                                                  }
                                              }));
        }
    }

    private void processInitData(List<Image> images) {
        setCount = 0;
        favList.clear();
        displayList.clear();
        refreshLayout.setRefreshing(false);

        if (!isFavorite) {
            for (Image image : images) {
                if (FileUtil.isFileExists(image.getLocalLink())) {
                    favList.add(image);
                } else {
                    compositeSubscription.add(mDataManager.deleteImage(image).subscribeOn(Schedulers.io())
                                                      .observeOn(AndroidSchedulers.mainThread())
                                                      .subscribe(new Action1<Integer>() {
                                                          @Override
                                                          public void call(Integer integer) {
                                                              Log.e("thanh.dao",
                                                                    "call: delete " + integer + " image(s)");
                                                          }
                                                      }));
                }
            }
        } else {
            favList.addAll(images);
        }

        addDataBatch(setCount);
        setCount++;
    }

    @Override
    public void onRefresh() {
        setCount = 0;
        favList.clear();
        displayList.clear();
        initData();
    }

    @Override
    public void onDestroy() {
        if (mDataManager != null) {
            mDataManager.destruct();
            mDataManager = null;
        }

        super.onDestroy();
    }
}
