package com.dpanic.dpwallz.ui;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.dpanic.dpwallz.R;
import com.dpanic.dpwallz.control.HTMLParsingUtil;
import com.dpanic.dpwallz.model.Category;
import com.dpanic.dpwallz.model.DataManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by dpanic on 29/09/2016.
 * Project: DPWallz
 */

public class CategoryFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener{

    @BindView(R.id.category_container)
    SwipeRefreshLayout categoryContainer;

    @BindView(R.id.rv_category)
    RecyclerView rvCategory;

    @BindView(R.id.category_error_container)
    LinearLayout layoutError;

    ArrayList<Category> categoryList;
    private CategoryAdapter categoryAdapter;
    private DataManager mDataManager;
    private Subscription subscription;
    private int orientation = Configuration.ORIENTATION_PORTRAIT;
    private GridLayoutManager layoutManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_category_layout, container, false);
        ButterKnife.bind(this, fragmentView);
        return fragmentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initFragment();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        orientation = newConfig.orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutManager.setSpanCount(2);
        } else {
            layoutManager.setSpanCount(3);
        }
    }

    private void initFragment() {
        mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
//        mDataManager = new DataManager(getActivity());
        categoryList = new ArrayList<>();

        categoryAdapter = new CategoryAdapter(getActivity(), categoryList);
        orientation = getActivity().getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutManager = new GridLayoutManager(getActivity(), 2);
        } else {
            layoutManager = new GridLayoutManager(getActivity(), 3);
        }

        rvCategory.setAdapter(categoryAdapter);
        rvCategory.setLayoutManager(layoutManager);

        categoryContainer.setOnRefreshListener(this);
        categoryContainer.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        categoryContainer.setRefreshing(true);

        loadCategoryDataFromDB();
    }

    private void loadCategoryDataFromDB() {
        subscription = mDataManager.getCategoryList().subscribe(new Action1<List<Category>>() {
            @Override
            public void call(List<Category> categories) {
                Log.d("thanh.dao", "getCategoryList: size = " + categories.size());
                if (subscription != null) {
                    subscription.unsubscribe();
                }
                if (categories.size() > 0) {
                    categoryList.clear();
                    categoryList.addAll(categories);
                    sortCategory(categoryList);
                    categoryAdapter.notifyDataSetChanged();
                    categoryContainer.setRefreshing(false);
                } else {
                    loadCategoryFromWebsite(false);
                }
            }
        });
        addSubscription(subscription);
    }

    private void loadCategoryFromWebsite(final boolean isRefreshing) {
        final long startTime = System.currentTimeMillis();

        addSubscription(HTMLParsingUtil.getCategory().subscribe(new Observer<List<Category>>() {
            @Override
            public void onCompleted() {
                Log.e("thanh.dao", "getCategory onCompleted: time = " + (System.currentTimeMillis() - startTime));
                categoryContainer.setRefreshing(false);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("thanh.dao", "onError: " + throwable.getMessage());
                throwable.printStackTrace();
                if (throwable instanceof SocketTimeoutException) {
                    Log.e("thanh.dao", "onError: " + getResources().getString(R.string.string_no_internet_connection));
                    //                    Toast.makeText(getActivity(), getResources().getString(R.string.string_no_internet_connection), Toast.LENGTH_SHORT)
                    //                            .show();
                }
                //                Toast.makeText(getActivity(), "Error when loading category from server", Toast.LENGTH_SHORT).show();
                categoryContainer.setRefreshing(false);
                if (categoryList.size() == 0) {
                    layoutError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNext(final List<Category> list) {
                Log.e("thanh.dao", "onNext: " + list.size());
                if (list.size() > 0) {

//                    final List<Category> orderedList = new ArrayList<>();
//                    orderedList.addAll(list);

                    //                    sortCategory(orderedList);

//                    Log.e("thanh.dao", "orderedList: " + orderedList.size());
                    Log.e("thanh.dao", "isRefreshing: " + isRefreshing);
                    if (isRefreshing) {
                        addSubscription(mDataManager.deleteCategoryData().subscribe(new Observer<Integer>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable throwable) {
                                throwable.printStackTrace();
                            }

                            @Override
                            public void onNext(Integer integer) {
                                addCategoriesToDB(list);
                            }
                        }));
                    } else {
                        addCategoriesToDB(list);
                    }

                    categoryList.clear();

                    categoryList.addAll(list);

                    categoryAdapter.notifyDataSetChanged();
                }
            }
        }));
    }

    private static void sortCategory(List<Category> list) {
        Collections.sort(list, new Comparator<Category>() {
            @Override
            public int compare(Category cat1, Category cat2) {
                return cat1.getName().compareTo(cat2.getName());
            }
        });
    }

    private void addCategoriesToDB(List<Category> list) {
        mDataManager.addCategories(list).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                Log.d("thanh.dao", "inserted " + integer + " categories into DB.");
            }
        });
    }

    @Override
    public void onRefresh() {
        Log.e("thanh.dao", "onRefresh: ");
        layoutError.setVisibility(View.GONE);
        loadCategoryFromWebsite(true);
    }

    @OnClick(R.id.category_btn_retry)
    void onClick() {
        categoryContainer.setRefreshing(true);
        onRefresh();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mDataManager = null;
    }
}
