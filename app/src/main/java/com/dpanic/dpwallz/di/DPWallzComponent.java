package com.dpanic.dpwallz.di;

import com.dpanic.dpwallz.ui.CategoryFragment;
import com.dpanic.dpwallz.ui.detail.DetailActivity;
import com.dpanic.dpwallz.ui.ExploreFragment;
import com.dpanic.dpwallz.ui.FavoriteFragment;
import com.dpanic.dpwallz.ui.imagelist.ImageListFragment;
import com.dpanic.dpwallz.ui.MainActivity;
import com.dpanic.dpwallz.ui.PreviewActivity;
import com.dpanic.dpwallz.ui.SearchActivity;
import dagger.Component;

/**
 * Created by dpanic on 1/9/2017.
 * Project: DPWallz
 */
@SuppressWarnings("unused")
@ApplicationScope
@Component(modules = {AppModule.class, EventBusModule.class, FirebaseModule.class, LocalDataModule.class, AdModule.class})
public interface DPWallzComponent {

    void inject(MainActivity activity);
    void inject(DetailActivity activity);
    void inject(SearchActivity activity);
    void inject(PreviewActivity activity);

    void inject(ImageListFragment fragment);
    void inject(CategoryFragment fragment);
    void inject(ExploreFragment fragment);
    void inject(FavoriteFragment fragment);
}
