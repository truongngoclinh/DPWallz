package com.dpanic.dpwallz.view;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.dpanic.dpwallz.R;
import com.dpanic.dpwallz.busevent.OpenCategoryEvent;
import com.dpanic.dpwallz.model.Category;
import com.dpanic.dpwallz.control.TextUtil;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by dpanic on 10/7/2016.
 * Project: DPWallz
 */

class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.colorVH> {

    private Context mContext;
    private ArrayList<String> colorList;

    ColorAdapter(Context context, ArrayList<String> colorList) {
        this.mContext = context;
        this.colorList = colorList;
    }

    @Override
    public colorVH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        return new colorVH(inflater.inflate(R.layout.color_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(colorVH holder, int position) {
        holder.ivColorItem.setColorFilter(Color.parseColor(colorList.get(position)), PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public int getItemCount() {
        return colorList.size();
    }

    class colorVH extends RecyclerView.ViewHolder {

        @BindView(R.id.color_item)
        ImageView ivColorItem;

        colorVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.color_item)
        void onClick() {
            String colorSearch = mContext.getResources().getString(R.string.string_color_search_prefix_text) + colorList.get(getAdapterPosition());
            Category category = new Category(colorSearch, TextUtil.getSearchLink(colorSearch), "");
            EventBus.getDefault().post(new OpenCategoryEvent(category, true));
        }
    }
}
