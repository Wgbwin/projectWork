package com.kodak.rss.tablet.util.load;

import android.graphics.Bitmap;

public class Response {

    private Request request;
    private Exception error;
    private boolean isCached;
    private Bitmap bitmap;  

    Response(Request request, Exception error, boolean isCached, Bitmap bitmap) {
        this.request = request;
        this.error = error;
        this.bitmap = bitmap;
        this.isCached = isCached;        
    }

    public Request getRequest() {
        return request;
    }

    public Exception getError() {
        return error;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public boolean isCached() {
        return isCached;
    }
     
}
