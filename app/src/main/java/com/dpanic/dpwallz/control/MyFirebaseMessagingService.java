package com.dpanic.dpwallz.control;

import java.io.File;
import java.util.Map;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dpanic.dpwallz.R;
import com.dpanic.dpwallz.model.Image;
import com.dpanic.dpwallz.util.Constants;
import com.dpanic.dpwallz.ui.DetailActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by dpanic on 10/28/2016.
 * Project: DPWallz
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private String url;
    private Map<String, String> data;
    private Image imageObj;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle data payload of FCM messages.
        //        Log.e("thanh.dao", "FCM Message Id: " + remoteMessage.getMessageId());
        //        Log.e("thanh.dao", "FCM Notification Message: " + remoteMessage.getNotification());
        //        Log.e("thanh.dao", "FCM Data Message: " + remoteMessage.getData());
        data = remoteMessage.getData();
        if (isHadPermission()) {
            HTMLParsingUtil.getImageFromDetailLink(data.get(Constants.FCM_DETAIL_LINK))
                    .subscribe(new Observer<Image>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(Image image) {
                            imageObj = image;
                            downloadImage(image);
                        }
                    });
        }
    }

    private void downloadImage(Image image) {
        this.url = image.getLargeLink();
        DownloadUtil.downloadImageFromUrl(this, url, true).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Integer>() {
            @Override
            public void onCompleted() {
                handleDownloadedImage();
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Integer integer) {

            }
        });
    }

    private void handleDownloadedImage() {
        String root = getFilesDir().toString();
        String downloadFolder = root + "/Download";
        String filePath = downloadFolder + "/" + FileUtil.getFileName(url);

        File file = new File(filePath);
        if (file.exists()) {
            String dimen = getImageDimension(filePath);
            int width = Integer.parseInt(dimen.substring(0, dimen.indexOf(" ")));
            int height = Integer.parseInt(dimen.substring(dimen.indexOf(" ") + 1));
            Glide.with(this).load(filePath).asBitmap().dontTransform().override(width, height)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE).listener(new RequestListener<String, Bitmap>() {
                @Override
                public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target,
                                               boolean isFromMemoryCache, boolean isFirstResource) {
                    sendNotification(resource);
                    return false;
                }
            }).preload();
        }
    }

    public boolean isHadPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private String getImageDimension(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        //Returns null, sizes are in the options variable
        BitmapFactory.decodeFile(filePath, options);
        int width = options.outWidth;
        int height = options.outHeight;
        return width + " " + height;
    }

    private void sendNotification(Bitmap bitmap) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (imageObj != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.IMAGE_INSTANCE, imageObj);
            intent.putExtras(bundle);
        }

        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT); //
        // currently is one shot

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification notification = new Notification.Builder(this).setSmallIcon(R.drawable.ic_small_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(data.get(Constants.FCM_MESSAGE)).setAutoCancel(true).setSound(defaultSoundUri).setStyle(
                        new Notification.BigPictureStyle().bigPicture(bitmap)
                                .setSummaryText(data.get(Constants.FCM_MESSAGE))).setContentIntent(pendingIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notification);
    }
}
