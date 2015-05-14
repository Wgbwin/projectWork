package com.kodak.rss.tablet.util.load;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

public class ImageDownloader {    
    private static final int DOWN_READ_QUEUE_MAX_CONCURRENT = 3;
    private static final int CACHE_READ_QUEUE_MAX_CONCURRENT = 5;
    private static Handler handler;
    private static WorkQueue downloadQueue = new WorkQueue(DOWN_READ_QUEUE_MAX_CONCURRENT);
    private static WorkQueue cacheReadQueue = new WorkQueue(CACHE_READ_QUEUE_MAX_CONCURRENT);
    private static final Map<RequestKey, Downloader> pendingRequests = new HashMap<RequestKey, Downloader>();
   
    private static RequestKey getRequestKey(Request request){
    	RequestKey key = null;
    	if (request == null) return key;
        if (request.getImageUri() != null) {
        	key = new RequestKey(request.getImageId(),request.getImageUri(), request.getCallerTag());
		}else {
			key = new RequestKey(request.getImageId(),request.getFilePath(),request.getCallerTag());
		}
        return key;
    }  
    
    public static boolean cancelRequest(Request request) {
        boolean cancelled = false;
        RequestKey key = getRequestKey(request);       
        synchronized (pendingRequests) {
            Downloader downloader = pendingRequests.get(key);
            if (downloader != null) {             
                cancelled = true;
                if (downloader.workItem.cancel()) {
                    pendingRequests.remove(key);
                } else {                	 
                	downloader.isCancelled = true;
                }
            }
        }
        return cancelled;
    }

    public static void prioritizeRequest(Request request) {   	
        RequestKey key = getRequestKey(request); 
        synchronized (pendingRequests) {
            Downloader downloader = pendingRequests.get(key);
            if (downloader != null) {
            	downloader.workItem.moveToFront();
            }
        }
    }

    public static void downloadAsync(Request request,String saveType,int[] viewParameters) {
        if (request == null) return;   
        RequestKey key = getRequestKey(request); 
        synchronized (pendingRequests) {
        	Downloader downloader = pendingRequests.get(key);
            if (downloader != null) {
            	downloader.request = request;
            	downloader.isCancelled = false;
            	downloader.workItem.moveToFront();
            } else {
            	String profileId = request.getImageId();
            	boolean isThumbnail = request.isThumbnail();
                enqueueCacheRead(profileId,request, key, request.isCachedRedirectAllowed(),saveType,isThumbnail,viewParameters);
            }
        }
    }   

    public static void enqueueCacheRead(String profileId,Request request, RequestKey key, boolean allowCachedRedirects,String saveType,boolean isThumbnail,int[] viewParameters) {   	
    	if (request.getImageUri() != null) {
    		enqueueRequest(request,key,cacheReadQueue, new ReadCacheWorkItem(profileId,request.getContext(), key, allowCachedRedirects,saveType,request.getRefreshCount(),isThumbnail,viewParameters));
		}else {
			if (request.getFilePath() != null) {
				enqueueRequest(request,key,cacheReadQueue, new ResolverPhotoWorkItem(key, request.getFilePath(), viewParameters));	    			
			}else {
				enqueueRequest(request,key,cacheReadQueue, new ResolverReadWorkItem(Integer.valueOf(request.getImageId()),request.getContext(),key));		
			}   		  
		}		       
    }

    public static void enqueueDownload(String profileId,Request request, RequestKey key,boolean allowCachedRedirects,String saveType,boolean isThumbnail,int[] viewParameters ) {   	
    	enqueueRequest(request,key,downloadQueue,new DownloadWorkItem(profileId,request.getContext(), key, allowCachedRedirects,saveType,request.getRefreshCount(),isThumbnail,viewParameters));    	
    }

    private static void enqueueRequest(Request request,RequestKey key,WorkQueue workQueue,Runnable workItem) {
        synchronized (pendingRequests) {
            Downloader downloader = new Downloader();
            downloader.request = request;
            pendingRequests.put(key, downloader);         
            downloader.workItem = workQueue.addActiveWorkItem(workItem);
        }
    }

    public static void issueResponse(RequestKey key,final Exception error,final Bitmap bitmap,final boolean isCachedRedirect) {     
        Downloader completedRequestContext = removePendingRequest(key);
        if (completedRequestContext != null && !completedRequestContext.isCancelled) {
            final Request request = completedRequestContext.request;
            final Request.Callback callback = request.getCallback();
            if (callback != null) {
                getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Response response = new Response(request,error,isCachedRedirect,bitmap);
                        callback.onCompleted(response);
                    }
                });
            }
        }
    }

    private static synchronized Handler getHandler() {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        return handler;
    }

    public static Downloader removePendingRequest(RequestKey key) {
        synchronized (pendingRequests) {
            return pendingRequests.remove(key);
        }
    }
    
    public static Downloader getRequest(RequestKey key) {
        synchronized (pendingRequests) {
            return pendingRequests.get(key);
        }
    }

    public static class RequestKey {
        private static final int HASH_SEED = 29; 
        private static final int HASH_MULTIPLIER = 37; 

        URI uri;
        Object tag;       
        String imageId;      
        String filePath;

        RequestKey(String imageId,URI uri, Object tag) {
            this.uri = uri;
            this.imageId = imageId;
            this.tag = tag;
        }
        
        RequestKey(String imageId, String filePath, Object tag) {
            this.imageId = imageId;
            this.filePath = filePath;          
            this.tag = tag;
        }

        @Override
        public int hashCode() {
            int result = HASH_SEED;
            result = (result * HASH_MULTIPLIER) + imageId.hashCode();
            if (uri != null) {
            	result += uri.hashCode() ;
			}else if (filePath != null) {
				result += filePath.hashCode();
			}
            result += tag.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object o) {
            boolean isEqual = false;
            if (o != null && o instanceof RequestKey) {
                RequestKey compareTo = (RequestKey)o;
                isEqual = compareTo.imageId.equals(imageId) && compareTo.tag == tag;
                if (uri != null) {
                	isEqual &= compareTo.uri == uri;
				} else if (filePath != null){
					isEqual &= compareTo.filePath.equals(filePath);
				}
            }
            return isEqual;
        }
    }

    public static class Downloader {
        WorkQueue.WorkItem workItem;
        Request request;
        boolean isCancelled;
    }
    
    public static void clearCache(Context context) {       
        deleteAllPic();       
    }
    
    public static void deleteAllPic(){
		File baseDir = new File(FilePathConstant.tempFolder);
		deleteFolder(baseDir);
	}
    
    public static void clearCache(Context context,String folderPath) {       
        deleteAllPic(folderPath);       
    }
    
    public static void deleteAllPic(String folderPath){
		File baseDir = new File(FilePathConstant.tempFolder,folderPath);
		deleteFolder(baseDir);
	}
    
    private static void deleteFolder(File baseDir){
    	if (baseDir == null) return;
    	if (!baseDir.exists()) return;
    	if (baseDir.isDirectory()) {
    		File[] files = baseDir.listFiles();	
    		for (int i = 0; i < files.length; i++) {
    			deleteFolder(files[i]);
    		}   		
		}else {
			baseDir.delete();
		}	
    }    
    
}
