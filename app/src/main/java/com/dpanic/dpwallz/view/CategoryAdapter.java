package com.dpanic.dpwallz.view;

import java.util.ArrayList;

import org.greenrobot.eventbus.EventBus;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dpanic.dpwallz.R;
import com.dpanic.dpwallz.busevent.OpenCategoryEvent;
import com.dpanic.dpwallz.model.Category;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by dpanic on 10/12/2016.
 * Project: DPWallz
 */

class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryVH> {
    private Context mContext;
    private ArrayList<Category> categoryList;

    CategoryAdapter(Context context, ArrayList<Category> list) {
        mContext = context;
        categoryList = list;
    }

    @Override
    public CategoryVH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);

        return new CategoryVH(layoutInflater.inflate(R.layout.category_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(CategoryVH holder, int position) {
        holder.tvCategoryText.setText(categoryList.get(position).getName());
        Glide.with(mContext).load(categoryList.get(position).getThumbLink()).thumbnail(0.5f)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.ivCategoryImage);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    class CategoryVH extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_category_image)
        ImageView ivCategoryImage;

        @BindView(R.id.tv_category_text)
        TextView tvCategoryText;

        CategoryVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.category_item_container)
        void onClick() {
            EventBus.getDefault().post(new OpenCategoryEvent(categoryList.get(getAdapterPosition()), false));
        }
    }
}
