package com.kodak.rss.tablet.services;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.n2r.bean.calendar.Calendar;
import com.kodak.rss.core.n2r.bean.collage.Collage;
import com.kodak.rss.core.n2r.bean.greetingcard.GreetingCard;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.webservice.WebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.util.CalendarUtil;
import com.kodak.rss.tablet.util.CollageUtil;
import com.kodak.rss.tablet.util.GreetingCardUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.UploadProgressUtil;

/**
 * Purpose: Upload the selected picture to server 
 * Author: Bing Wang 
 * Created Time: Sep 10, 2013 15:20:43 PM
 */
public class PictureUploadService extends Service {

	private static final String TAG = "PictureUploadService :";
	public static boolean mTerminated = false;		
	public static String flowType = "print";			
	NotificationManager mManager = null;
	WebService service = null;
	Notification notification = null;

	private  String FIRST_UPLOAD_THUMBNAIL = "FIRST_UPLOAD_THUMBNAIL";
	private  String FIRST_UPLOAD_ORIGINAL = "FIRST_UPLOAD_ORIGINAL";
	private int iFirstTimeUploadIndex = 0;
	public static String uploadingURI;

	RssTabletApp app;
	public static boolean isRunning = true;
	private static final int CORE_POOL_SIZE = 2;
	private static final int MAXIMUM_POOL_SIZE = 3;
	private static final int KEEP_ALIVE = 10;
	
	Object RSS = new Object();
	
	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
		private AtomicInteger mCount = new AtomicInteger(1);
		public Thread newThread(Runnable r) {
			int count = mCount.getAndIncrement();			
			return new Thread(r, count + "_thread");
		}
	};
	private static final BlockingQueue<Runnable> sWorkQueue = new ArrayBlockingQueue<Runnable>(CORE_POOL_SIZE);//LinkedBlockingQueue<Runnable>(MAXIMUM_POOL_SIZE)
	private static RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
	private static final ThreadPoolExecutor sExecutor = new ThreadPoolExecutor(
			CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
			sWorkQueue, sThreadFactory, rejectedExecutionHandler);	

		
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate() {
		Log.w(TAG, "onCreate()");		
		mTerminated = false;	
		isRunning = true;		
		service = new WebService(PictureUploadService.this.getApplicationContext());
		app = RssTabletApp.getInstance();
		mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notification = new Notification(R.drawable.notificationupload,"Notify", System.currentTimeMillis());	
	}

	@Override
	public void onDestroy() {
		isRunning = false;
		mTerminated = true;			
		UploadImagesThread.mUploadImagesThread = null;		
		super.onDestroy();
		Log.w(TAG, "onDestroy() complete");
	}
	
	private boolean checkWantUpload(List<ImageInfo> list,boolean isThumbnail){
		boolean isWant = false;
		if (list==null) return false;
		try {
			for (int i = 0; i < list.size(); i++) {
				ImageInfo imageInfo = list.get(i);
				if (isThumbnail) {
					if (imageInfo != null && imageInfo.imageThumbnailResource == null && imageInfo.uploadThumbnailUrl != null && !imageInfo.uploadThumbnailUrl.startsWith(FIRST_UPLOAD_THUMBNAIL)) {
						isWant = true;
						break;
					}
				}else {
					if (imageInfo != null && imageInfo.imageOriginalResource == null && imageInfo.uploadOriginalUrl != null && !imageInfo.uploadOriginalUrl.startsWith(FIRST_UPLOAD_ORIGINAL)) {
						isWant = true;
						break;
					}
				}
			}
		} catch (Exception e) {
		}		
		return isWant;
	}
	
	boolean isBackFromBook2AddPicsAgain;
	private void checkIsBackAddPicsAgain(List<ImageInfo> list,boolean isThumbnail) {
		iFirstTimeUploadIndex++;
		isBackFromBook2AddPicsAgain = false;
		if (list==null) return ;
		try {
			for (int j = 0; j < list.size(); j++) {
				ImageInfo imageInfo = list.get(j);
				if (isThumbnail) {
					if (imageInfo != null && imageInfo.imageThumbnailResource == null && imageInfo.uploadThumbnailUrl != null && !imageInfo.uploadThumbnailUrl.startsWith(FIRST_UPLOAD_THUMBNAIL)){
						isBackFromBook2AddPicsAgain = true;
						break;
					}
				}else {
					if (imageInfo != null && imageInfo.imageOriginalResource == null && imageInfo.uploadOriginalUrl != null && !imageInfo.uploadOriginalUrl.startsWith(FIRST_UPLOAD_ORIGINAL)){
						isBackFromBook2AddPicsAgain = true;
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startid) {			
		Log.w(TAG, "onStart()" );
		if (UploadImagesThread.mUploadImagesThread != null && UploadImagesThread.mUploadImagesThread.isAlive()) {
			return;
		} else {		
			UploadImagesThread.getInstance(new Runnable() {
				@Override
				public void run() {
					while (isRunning) {	
						if (mTerminated) {
							Log.d(TAG, "stop  UploadImagesThread sleep" );
							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}	
						}else {
							Log.d(TAG, "UploadImagesThread start run" );						
							if (flowType.equals(AppConstants.bookType)) {							
								uploadThumbnailImageInPhotoBook();							
								uploadOriginalImageInPhotoBook();						
								uploadOriginalImageInPrint();
								uploadOriginalImageInCard();
								uploadOriginalImageInCalendar();
								uploadOriginalImageInCollage();
							}else if (flowType.equals(AppConstants.printType)){
								uploadOriginalImageInPrint();							
								uploadThumbnailImageInPhotoBook();
								uploadOriginalImageInPhotoBook();
								uploadOriginalImageInCard();
								uploadOriginalImageInCalendar();
								uploadOriginalImageInCollage();
							}else if (flowType.equals(AppConstants.cardType)) {
								uploadOriginalImageInCard();
								uploadOriginalImageInPrint();							
								uploadThumbnailImageInPhotoBook();
								uploadOriginalImageInPhotoBook();
								uploadOriginalImageInCalendar();
								uploadOriginalImageInCollage();
							}else if (flowType.equals(AppConstants.calendarType)) {
								uploadOriginalImageInCalendar();								
								uploadOriginalImageInPrint();							
								uploadThumbnailImageInPhotoBook();
								uploadOriginalImageInPhotoBook();
								uploadOriginalImageInCard();
								uploadOriginalImageInCollage();
							}else if (flowType.equals(AppConstants.collageType)) {
								uploadOriginalImageInCollage();																
								uploadOriginalImageInPrint();							
								uploadThumbnailImageInPhotoBook();
								uploadOriginalImageInPhotoBook();
								uploadOriginalImageInCard();
								uploadOriginalImageInCalendar();
							}								
							
							try {
								Log.d(TAG,  "start UploadImagesThread sleep" );
								Thread.sleep(3000);
								synchronized (RSS) {
									List<ImageInfo>  imageInfoList =  UploadProgressUtil.allImages();
									int mUploadTotal = imageInfoList.size();	
									long[] info = UploadProgressUtil.getUploadPicSuccessNum(imageInfoList);									
									if (mUploadTotal <= info[0]) {
										if (new Date().getTime() - info[1] > 2*1000*60) {// 3*1000*60
											PictureUploadService.mTerminated = true;
											stopSelf();
										}	
									}
								}
								
							} catch (InterruptedException e) {
								e.printStackTrace();
							}	
						}					
					}
				}
			}).start();		
		}
	}
	//upload prints photos
	private void uploadOriginalImageInPrint(){
		Log.d(TAG, "uploadOriginalImageInPrint start");
		uploadOriginalImage(RssTabletApp.getInstance().chosenList, flowType,AppConstants.printType, null,true,false,null);	
		Log.d(TAG, "uploadOriginalImageInPrint end");
	}
	
	//upload card photos
	private void uploadOriginalImageInCard(){
		Log.d(TAG, "uploadOriginalImageInCard start");		
		List<GreetingCard> gCardList = RssTabletApp.getInstance().gCardList;
		if (gCardList != null ) {
			GreetingCard currentCard = GreetingCardUtil.getCurrentGreetingCard();
			if (currentCard != null) {
				uploadOriginalImage(currentCard.chosenpics, flowType, AppConstants.cardType, currentCard.id,true,false,currentCard.proDescId);				
			}			
			for (GreetingCard card : gCardList) {
				if (card!= null && !card.isCurrentChosen) {
					uploadOriginalImage(card.chosenpics, flowType, AppConstants.cardType, card.id,false,false,card.proDescId);					
				}					
			}	
		}			
		Log.d(TAG, "uploadOriginalImageInCard end");
	}	
	
	//upload calendar photos
	private void uploadOriginalImageInCalendar(){
		Log.d(TAG, "uploadOriginalImageInCalendar start");		
		List<Calendar> calendarList = RssTabletApp.getInstance().calendarList;
		if (calendarList != null ) {
			Calendar currentCalendar = CalendarUtil.getCurrentCalendar();
			if (currentCalendar != null) {
				uploadOriginalImage(currentCalendar.chosenpics, flowType, AppConstants.calendarType, currentCalendar.id,true,false,currentCalendar.proDescId);				
			}			
			for (Calendar calendar : calendarList) {
				if (calendar != null && !calendar.isCurrentChosen) {
					uploadOriginalImage(calendar.chosenpics, flowType, AppConstants.calendarType, calendar.id,false,false,calendar.proDescId);					
				}					
			}	
		}			
		Log.d(TAG, "uploadOriginalImageInCalendar end");
	}	
	
	//upload collage photos
	private void uploadOriginalImageInCollage(){
		Log.d(TAG, "uploadOriginalImageInCollage start");		
		List<Collage> collageList = RssTabletApp.getInstance().collageList;
		if (collageList != null ) {
			Collage currentCollage = CollageUtil.getCurrentCollage();
			if (currentCollage != null) {
				uploadOriginalImage(currentCollage.chosenpics, flowType, AppConstants.collageType, currentCollage.id,true,false,currentCollage.proDescId);				
			}			
			for (Collage collage : collageList) {
				if (collage != null && !collage.isCurrentChosen) {
					uploadOriginalImage(collage.chosenpics, flowType, AppConstants.collageType, collage.id,false,false,collage.proDescId);					
				}					
			}	
		}			
		Log.d(TAG, "uploadOriginalImageInCollage end");
	}		
	
	//upload photobook photos isThumbnail
	private void uploadThumbnailImageInPhotoBook(){
		Log.d(TAG, "uploadThumbnailImageInPhotoBook start");
		List<Photobook> chosenBookList = RssTabletApp.getInstance().chosenBookList;
		if (chosenBookList != null ) {
			Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
			if (currentPhotoBook != null) {
				uploadThumbnailImage(currentPhotoBook.chosenpics, flowType, AppConstants.bookType, currentPhotoBook.id,true,currentPhotoBook.isTempStopUpload,currentPhotoBook.proDescId);								
			}			
			for (Photobook photobook : chosenBookList) {
				if (photobook!= null && !photobook.isCurrentChosen) {
					uploadThumbnailImage(photobook.chosenpics, flowType, AppConstants.bookType, photobook.id,false,photobook.isTempStopUpload,photobook.proDescId);					
				}					
			}	
		}	
		Log.d(TAG, "uploadThumbnailImageInPhotoBook end");
	}

	//upload photobook photos isOriginal
	private void uploadOriginalImageInPhotoBook(){	
		Log.d(TAG, "uploadOriginalImageInPhotoBook start");
		List<Photobook> chosenBookList = RssTabletApp.getInstance().chosenBookList;
		if (chosenBookList != null ) {
			Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
			if (currentPhotoBook != null) {
				uploadOriginalImage(currentPhotoBook.chosenpics, flowType, AppConstants.bookType, currentPhotoBook.id,true,currentPhotoBook.isTempStopUpload,currentPhotoBook.proDescId);				
			}			
			for (Photobook photobook : chosenBookList) {
				if (photobook!= null && !photobook.isCurrentChosen) {
					uploadOriginalImage(photobook.chosenpics, flowType, AppConstants.bookType, photobook.id,false,photobook.isTempStopUpload,photobook.proDescId);					
				}					
			}	
		}	
		Log.d(TAG, "uploadOriginalImageInPhotoBook end");
	}
	
	//upload Thumbnail photos
	private void uploadThumbnailImage(List<ImageInfo> chosenpics, String flowType,String uploadType, String productId,boolean isCurrent,boolean isTempStopUpload,String proDescriptionId){						
		if (isTempStopUpload) return;
		if (checkWantUpload(chosenpics,true)) {							
			iFirstTimeUploadIndex--;
			checkIsBackAddPicsAgain(chosenpics,true);  
			
			if (!(iFirstTimeUploadIndex < chosenpics.size() && isBackFromBook2AddPicsAgain)) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}								
			}

			for (;  chosenpics!= null &&((iFirstTimeUploadIndex < chosenpics.size()) || isBackFromBook2AddPicsAgain);checkIsBackAddPicsAgain(chosenpics,true)) {
				if (iFirstTimeUploadIndex >= chosenpics.size()) {
					iFirstTimeUploadIndex = -1;
					continue;
				}
				if (mTerminated || !isRunning) return;				
				
				ImageInfo imageInfo = null;
				try {
					imageInfo = chosenpics.get(iFirstTimeUploadIndex);
				} catch (Exception e) {					
				}
				
				if (imageInfo == null) continue;
				if (imageInfo.imageThumbnailResource != null) continue;
				if (imageInfo.uploadThumbnailUrl == null) continue;
				if (imageInfo.uploadThumbnailUrl.startsWith(FIRST_UPLOAD_THUMBNAIL)) continue;			
				
				Log.d(TAG, "upload ThumbnailPic--"+"ProductId :"+productId + " uploadType:"+uploadType+" imageInfo:"+imageInfo.id);
				Runnable picUploadTask = new UploadThumbnailPicTask(PictureUploadService.this.getApplicationContext(),mManager,notification,imageInfo,uploadType,productId,proDescriptionId);
				sExecutor.execute(picUploadTask);
																				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}	
				
				if (!uploadType.equals(flowType)) {
					if (flowType.equals(AppConstants.cardType)) {
						uploadOriginalImageInCard();
					}else if (flowType.equals(AppConstants.printType)) {
						uploadOriginalImageInPrint();
					}else if (flowType.equals(AppConstants.bookType)){
						uploadThumbnailImageInPhotoBook();
					}else if (flowType.equals(AppConstants.calendarType)){
						uploadOriginalImageInCalendar();
					}else if (flowType.equals(AppConstants.collageType)){
						uploadOriginalImageInCollage();
					}								
				}else if (flowType.equals(AppConstants.bookType) && !isCurrent){
					Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
					if (currentPhotoBook != null && checkWantUpload(currentPhotoBook.chosenpics,true)) {
						uploadThumbnailImageInPhotoBook();											
					}							
				}				
			}			
		}						
	}
	
	//upload Original photos 
	private void uploadOriginalImage(List<ImageInfo> chosenpics, String flowType, String uploadType, String productId,boolean isCurrent,boolean isTempStopUpload,String proDescriptionId){						
		if (isTempStopUpload) return;
		if (checkWantUpload(chosenpics,false)) {							
			iFirstTimeUploadIndex--;
			checkIsBackAddPicsAgain(chosenpics,false);  
			
			if (!(iFirstTimeUploadIndex < chosenpics.size() && isBackFromBook2AddPicsAgain)) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}								
			}

			for (; chosenpics!= null &&((iFirstTimeUploadIndex < chosenpics.size()) || isBackFromBook2AddPicsAgain);checkIsBackAddPicsAgain(chosenpics,false)) {
				if (iFirstTimeUploadIndex >= chosenpics.size()) {
					iFirstTimeUploadIndex = -1;
					continue;
				}	
				if (mTerminated || !isRunning) return;		
				
				ImageInfo imageInfo = null;
				try {
					imageInfo = chosenpics.get(iFirstTimeUploadIndex);
				} catch (Exception e) {					
				}
				
				if (imageInfo == null) continue;	
				if (imageInfo.imageOriginalResource != null) continue;
				if (imageInfo.uploadOriginalUrl == null) continue;
				if (imageInfo.uploadOriginalUrl.startsWith(FIRST_UPLOAD_ORIGINAL)) continue;				
				if (uploadType.equals(AppConstants.bookType)&& imageInfo.imageThumbnailResource == null) continue;

				Log.d(TAG, "upload OriginalPic--"+"ProductId :"+productId + " uploadType:"+uploadType+" imageInfo:"+imageInfo.id);
				Runnable picUploadTask = new UploadOriginalPicTask(PictureUploadService.this.getApplicationContext(),mManager,notification,imageInfo,uploadType,productId,proDescriptionId);
				sExecutor.execute(picUploadTask);
																				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}	
				
				if (!uploadType.equals(flowType)) {
					if (flowType.equals(AppConstants.cardType)) {
						uploadOriginalImageInCard();
					}else if (flowType.equals(AppConstants.printType)) {
						uploadOriginalImageInPrint();
					}else if (flowType.equals(AppConstants.bookType)){
						uploadThumbnailImageInPhotoBook();
					}else if (flowType.equals(AppConstants.calendarType)){
						uploadOriginalImageInCalendar();
					}else if (flowType.equals(AppConstants.collageType)){
						uploadOriginalImageInCollage();
					}																				
				}else if (flowType.equals(AppConstants.bookType)){	
					Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
					if (currentPhotoBook != null && checkWantUpload(currentPhotoBook.chosenpics,true)) {
						uploadThumbnailImageInPhotoBook();				
					}			
				}			
			}																					
		}		
	}
	
	public class LocalBinder extends Binder {
		PictureUploadService getService() {
			return PictureUploadService.this;
		}
	}
	
	public static class UploadImagesThread extends Thread {
		public static UploadImagesThread mUploadImagesThread;		
		private UploadImagesThread(Runnable runnable) {
			super(runnable);
		}

		public static UploadImagesThread getInstance(Runnable runnable) {
			if (mUploadImagesThread == null) {
				mUploadImagesThread = new UploadImagesThread(runnable);
			}
			return mUploadImagesThread;
		}

	}
	
}