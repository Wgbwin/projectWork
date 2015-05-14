package com.kodak.rss.tablet.util.load;

import java.net.URI;

import android.content.Context;

public class Request {

    private Context context;
    private URI imageUri;
    private Callback callback;
    private boolean allowCached;
    private Object callerTag;   
    private boolean isThumbnail; 
    private int refreshCount;
    private long createTime;   
    private String imageId;
    private String filePath;
    
    public interface Callback {      
        void onCompleted(Response response);
    }

    private Request(Builder builder) {
        this.context = builder.context;
        this.imageUri = builder.imageUrl;
        this.imageId = builder.imageId;
        this.filePath = builder.filePath;
        this.callback = builder.callback;
        this.allowCached = builder.allowCached;
        this.isThumbnail = builder.isThumbnail;
        this.refreshCount = builder.refreshCount;
        this.callerTag = builder.callerTag == null ? new Object() : builder.callerTag;
        this.createTime = System.currentTimeMillis();
    }

    public Context getContext() {
        return context;
    }

    public URI getImageUri() {
        return imageUri;
    }
    
    public String getImageId() {
        return imageId;
    }

    public String getFilePath() {
        return filePath;
    }
   
    public Callback getCallback() {
        return callback;
    }

    public boolean isCachedRedirectAllowed() {
        return allowCached;
    }

    public boolean isThumbnail() {
        return isThumbnail;
    }
    
    public int getRefreshCount() {
        return refreshCount;
    }
    
    public Object getCallerTag() {
        return callerTag;
    }
    
    public long getCreateTime() {
		return createTime;
	}

    public static class Builder {      
        private Context context;
        private URI imageUrl;
        
        private String imageId;
        private String filePath;   
       
        private Callback callback;
        private boolean allowCached;
        private boolean isThumbnail; 
        private int refreshCount;
        private Object callerTag;

        public Builder(Context context, String imageId, URI imageUrl) {           
            this.context = context;
            this.imageId = imageId;
            this.imageUrl = imageUrl;
        }
        
        public Builder(Context context,String imageId, String filePath) {           
            this.context = context;
            this.imageId = imageId;
            this.filePath = filePath;
        }

        public Builder setCallback(Callback callback) {
            this.callback = callback;
            return this;
        }

        public Builder setCallerTag(Object callerTag) {
            this.callerTag = callerTag;
            return this;
        }

        public Builder setAllowCached(boolean allowCached) {
            this.allowCached = allowCached;
            return this;
        }

        public Builder setThumbnail(boolean isThumbnail) {
            this.isThumbnail = isThumbnail;
            return this;
        }
        
        public Builder setRefreshCount(int refreshCount) {
            this.refreshCount = refreshCount;
            return this;
        }
        
        public Request build() {
            return new Request(this);
        }
    }
}
