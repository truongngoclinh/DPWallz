package com.dpanic.dpwallz.busevent;

/**
 * Created by dpanic on 10/12/2016.
 * Project: DPWallz
 */

public class DownloadEvent {
    public static final int STATUS_ERROR = 0;
    public static final int STATUS_COMPLETE = 1;
    public static final int STATUS_CANCEL_BY_USER = 2;

    private int status;
    private boolean isForView = false;
    private Throwable exception;

    public DownloadEvent(int status) {
        this.status = status;
    }

    public boolean isForView() {
        return isForView;
    }

    public void setForView(boolean forView) {
        isForView = forView;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public int getStatus() {
        return status;
    }
}
