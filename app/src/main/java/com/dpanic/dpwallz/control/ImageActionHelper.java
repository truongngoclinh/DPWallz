package com.dpanic.dpwallz.control;

import org.greenrobot.eventbus.EventBus;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;
import com.dpanic.dpwallz.R;
import com.dpanic.dpwallz.busevent.DownloadEvent;
import com.dpanic.dpwallz.model.Image;
import com.dpanic.dpwallz.util.Constants;
import com.dpanic.dpwallz.view.CustomProgressDialog;
import rx.Observer;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by dpanic on 11/10/2016.
 * Project: DPWallz
 */

public class ImageActionHelper {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int SET_AS_WALLPAPER_REQUEST_CODE = 101;

    private CustomProgressDialog mDownloadDialog;
    private Context context;
    private Image img;
    private int currentAction = -1;
    private long downloadId;
    private CompositeSubscription compositeSubscription;

    public ImageActionHelper(Context context, Image img) {
        this.context = context;
        this.img = img;
        compositeSubscription = new CompositeSubscription();
    }

    public void setAsAction() {
        if (FileUtil.isFileDownloaded(img.getOriginalLink())) {
            performSetAs();
        } else {
            performDownload(Constants.DOWNLOAD_FOR_SET_AS);
        }
    }

    private void performSetAs() {
        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
        intent.setDataAndType(FileUtil.getImageUri(img.getOriginalLink()), "image/*");
        intent.putExtra("jpg", "image/*");
        ((Activity) context)
                .startActivityForResult(Intent.createChooser(intent, context.getString(R.string.string_set_as)),
                                        SET_AS_WALLPAPER_REQUEST_CODE);
    }

    public void downloadAction() {
        if (FileUtil.isFileDownloaded(img.getOriginalLink())) {
            Toast.makeText(context, context.getResources().getString(R.string.string_the_image_has_already_downloaded),
                           Toast.LENGTH_SHORT).show();
            return;
        }

        performDownload(Constants.DOWNLOAD_FOR_DOWNLOAD);
    }

    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                                                                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showMessageDialog(context.getResources().getString(R.string.permission_explaination),
                                      new DialogInterface.OnClickListener() {
                                          @Override
                                          public void onClick(DialogInterface dialog, int which) {
                                              ActivityCompat.requestPermissions((Activity) context, new String[] {
                                                                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                                                        Manifest.permission.READ_EXTERNAL_STORAGE },
                                                                                PERMISSION_REQUEST_CODE);
                                          }
                                      });
                } else {
                    ActivityCompat.requestPermissions((Activity) context,
                                                      new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                              Manifest.permission.READ_EXTERNAL_STORAGE },
                                                      PERMISSION_REQUEST_CODE);
                }
                return true;
            }
        }
        return false;
    }

    private void showMessageDialog(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(context).setMessage(message).setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null).create().show();
    }

    public int getCurrentAction() {
        return currentAction;
    }

    public void downloadForPreview() {
        if (FileUtil.isFileDownloaded(img.getOriginalLink())) {
            downloadCompleted(true);
        } else {
            performDownload(Constants.DOWNLOAD_FOR_PREVIEW);
        }
    }

    private void downloadCompleted(boolean isForView) {
        DownloadEvent downloadEvent = new DownloadEvent(DownloadEvent.STATUS_COMPLETE);
        downloadEvent.setForView(isForView);
        EventBus.getDefault().post(downloadEvent);
    }

    private void performDownload(final int action) {
        currentAction = action;
        if (checkPermission()) {
            return;
        }

        initProgressDialog();

        downloadId = DownloadUtil.enqueueDownload(context, img.getOriginalLink(), false);
        compositeSubscription.add(DownloadUtil.getDownloadProgressFromId(context, downloadId).subscribe(new
                                                                                                            Observer<Integer>() {
            @Override
            public void onCompleted() {
                Toast.makeText(context, context.getResources().getString(R.string.string_download_completed),
                               Toast.LENGTH_SHORT).show();
                if (mDownloadDialog != null) {
                    mDownloadDialog.setProgress(100);
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        hideProgressDialog();

                        performActionAfterDownload(action);

//                        downloadCompleted(currentAction == Constants.DOWNLOAD_FOR_PREVIEW);

                        currentAction = -1;
                        downloadId = -1;
                    }
                }, 900);

            }

            @Override
            public void onError(Throwable throwable) {
                hideProgressDialog();

                DownloadEvent event = new DownloadEvent(DownloadEvent.STATUS_ERROR);
                event.setException(throwable);
                EventBus.getDefault().post(event);

                currentAction = -1;
            }

            @Override
            public void onNext(Integer progress) {
                showProgressDialog(progress);
            }
        }));
    }

    private void performActionAfterDownload(int action) {
        switch (action) {
        case Constants.DOWNLOAD_FOR_DOWNLOAD:
            break;
        case Constants.DOWNLOAD_FOR_SET_AS:
            performSetAs();
            break;
        case Constants.DOWNLOAD_FOR_SHARE:
            performShare();
            break;
        case Constants.DOWNLOAD_FOR_PREVIEW:
            downloadCompleted(true);
            break;
        }
    }

    @SuppressWarnings("unused")
    private void shareAction() {
        if (FileUtil.isFileDownloaded(img.getOriginalLink())) {
            performShare();
        } else {
            performDownload(Constants.DOWNLOAD_FOR_SHARE);
        }
    }

    public void performShare() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.putExtra(Intent.EXTRA_STREAM, FileUtil.getImageUri(img.getOriginalLink()));
        share.setType("image/*");
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(share, "Share image File"));
    }

    private void initProgressDialog() {
        if (mDownloadDialog == null) {
            mDownloadDialog = new CustomProgressDialog(context);

            mDownloadDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    DownloadUtil.dequeueDownload(context, downloadId);
                    FileUtil.deleteFile(FileUtil.getLocalPath(img.getOriginalLink()));

                    EventBus.getDefault().post(new DownloadEvent(DownloadEvent.STATUS_CANCEL_BY_USER));
                }
            });
        }

        mDownloadDialog.show();

    }

    private void showProgressDialog(int progress) {
        Log.e("thanh.dao", "showProgressDialog: progress = " + progress);
        if (mDownloadDialog != null && mDownloadDialog.isShowing()) {
            mDownloadDialog.setProgress(progress);
        }
    }

    private void hideProgressDialog() {
        if (mDownloadDialog != null && mDownloadDialog.isShowing()) {
            mDownloadDialog.dismiss();
            mDownloadDialog = null;
        }
    }

    public void destruct() {
        hideProgressDialog();

        if (compositeSubscription != null) {
            compositeSubscription.unsubscribe();
        }
    }

    public void resumeInvokedAction() {
        switch (currentAction) {
        case Constants.DOWNLOAD_FOR_DOWNLOAD:
            downloadAction();
            break;
        case Constants.DOWNLOAD_FOR_SET_AS:
            setAsAction();
            break;
        case Constants.DOWNLOAD_FOR_PREVIEW:
            downloadForPreview();
            break;
        case Constants.DOWNLOAD_FOR_SHARE:
            shareAction();
            break;
        }
    }
}
