package com.dpanic.dpwallz.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.Window;
import com.dpanic.dpwallz.R;
import com.pitt.library.fresh.FreshDownloadView;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by dpanic on 11/8/2016.
 * Project: DPWallz
 */

public class CustomProgressDialog {
    private Dialog mProgressDialog;

    @BindView(R.id.download_view)
    FreshDownloadView downloadView;

    public CustomProgressDialog(Context context) {
        @SuppressLint("InflateParams")
        View dialogView = ((Activity) context).getLayoutInflater().inflate(R.layout.progress_dialog_layout, null);

        ButterKnife.bind(this, dialogView);

        mProgressDialog = new Dialog(context);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setContentView(dialogView);
        mProgressDialog.setCanceledOnTouchOutside(false);
    }

    public void setProgress(int progress) {
        downloadView.upDateProgress(progress);
    }

//    public void reset() {
//        downloadView.reset();
//    }
//
//    public void showProgressError() {
//        downloadView.showDownloadError();
//    }

    public void show() {
        mProgressDialog.show();
    }

    public void dismiss() {
        mProgressDialog.dismiss();
    }

    public boolean isShowing() {
        return mProgressDialog.isShowing();
    }

//    public void showDownloadOk(){
//        downloadView.showDownloadOk();
//    }

//    public void showDownloadError(){
//        downloadView.showDownloadError();
//    }

    public void setOnCancelListener(DialogInterface.OnCancelListener listener) {
        mProgressDialog.setOnCancelListener(listener);
    }
}
