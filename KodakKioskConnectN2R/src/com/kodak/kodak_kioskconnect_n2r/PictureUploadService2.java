package com.kodak.kodak_kioskconnect_n2r;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.AppContext;
import com.kodak.flip.SelectedImage;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.PrintInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.Collage;
import com.kodak.kodak_kioskconnect_n2r.bean.photobook.Photobook;
import com.kodak.kodak_kioskconnect_n2r.collage.CollageManager;
import com.kodak.shareapi.GalleryService;

public class PictureUploadService2 extends Service {
	public static boolean mTerminated = false;
	private static final String TAG = "PictureUploadService2";
	static final int NO_COMMAND_RECEIVED_TIMEOUT = 2000;
	NotificationManager mManager = null;
	PrintMakerWebService service = null;
	GalleryService galleryService = null;
	Notification notification = null;

	/** Manual start Upload_Share_To_WMC, set True when click the Upload Button. */
	public static boolean isManualStartShare = false;

	/**
	 * Automatic start Upload_Share_To_WMC, set True when click the Buy Button
	 * on ShoppingCart UI(and the Automatic-CheckBox should be checked).
	 */
	public static boolean isAutoStartShare = false;

	public static final String FIRST_UPLOAD_THUMBNAILS = "FIRST_UPLOAD_THUMBNAILS";
	public static boolean isDoneSelectPics = false;
	public static boolean isDoneUploadThumbnails = false;
	private String uploadingImage = null;
	private int iFirstTimeUploadIndex = 0;
	//public static String uploadingURI;
	public static PhotoInfo uploadingPhoto;

	public static boolean canUploadFullSize = true;

	private String error = "Error";
	private String control = "";

	// enum Command
	// {
	// getAlbums, getThumbnail,
	// }

	// Null because this isn't going to be attached to any Activity
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	// Called only once when the Service is created
	@Override
	public void onCreate() {
		// service = new
		// PrintMakerWebService(PictureUploadService2.this.getApplicationContext(),
		// "kiosk");
		galleryService = new GalleryService();
		mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notification = new Notification(R.drawable.notificationupload, "Notify", System.currentTimeMillis());
		Log.d(TAG, "onCreate() complete");
		service = new PrintMakerWebService(PictureUploadService2.this.getApplicationContext(), "kiosk");	
	}

	// Called when we stop the service
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy() complete");
	}

	boolean isBackFromBook2AddPicsAgain;

	private void checkIsBackFromBook2AddPicsAgain() {
		iFirstTimeUploadIndex++;
		isBackFromBook2AddPicsAgain = false;
		isBackFromBook2AddPicsAgain = isPhotoInPhotoBookThumnailNotUpload();

		// try {
		// for (int j = 0; j < PrintHelper.uploadQueue.size(); j++) {
		// String uri = PrintHelper.uploadQueue.get(j);
		// if (uri != null && !uri.startsWith(FIRST_UPLOAD_THUMBNAILS) &&
		// PrintHelper.inQuickbook) {
		// isBackFromBook2AddPicsAgain = true;
		// break;
		// }
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	// Called whenever Context.startservice is called
//	@Override
//	public void onStart(Intent intent, int startid) {
//		service = new PrintMakerWebService(PictureUploadService2.this.getApplicationContext(), "kiosk");
//		uploadSelectedPhotos();
//	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if (UploadImagesThread.mUploadImagesThread != null && UploadImagesThread.mUploadImagesThread.isAlive()) {
			
		} else {
			UploadImagesThread.getInstance(new Runnable() {
				
				@Override
				public void run() {
					uploadSelectedPhotos(control);							
				}
			}).start();
					
		}
		// TODO Auto-generated method stub
		notifyUploadThread(control);
		return START_NOT_STICKY ;
	}

	public void notifyUploadThread(String control){
		synchronized (control){
			control.notifyAll();
		}
		
	}
	
	public void uploadSelectedPhotos(String control) {
		while (!mTerminated) {

			// TODO WMC FUNCTION
			/******* Do share task --Begin *******/
			if (isManualStartShare || isAutoStartShare) {
				isManualStartShare = isAutoStartShare = false;
				new Thread(new UploadShare2WmcRunnable()).start();
			}
			/******* First, upload Thumbnails for PhotoBook --Begin *******/
			if (AppContext.getApplication().getmUploadPhotoList() == null || AppContext.getApplication().getmUploadPhotoList().isEmpty()) {
				try {
					synchronized (control){
						control.wait();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (AppContext.getApplication().getmUploadPhotoList() != null && !AppContext.getApplication().getmUploadPhotoList().isEmpty()) {

				if (isPhotoInPhotoBookExist() && isPhotoInPhotoBookThumnailNotUpload()) {
					isDoneUploadThumbnails = false;
					checkIsBackFromBook2AddPicsAgain();
					for (iFirstTimeUploadIndex = 0; iFirstTimeUploadIndex < AppContext.getApplication().getmUploadPhotoList().size() 
							|| !isDoneSelectPics
							|| isBackFromBook2AddPicsAgain;
							checkIsBackFromBook2AddPicsAgain()) {
						PhotoInfo photo = null;
						synchronized (AppContext.getApplication().getmUploadPhotoList()) {
							if (iFirstTimeUploadIndex >= AppContext.getApplication().getmUploadPhotoList().size() ||
									AppContext.getApplication().getmUploadPhotoList().size()==0) {
								iFirstTimeUploadIndex = -1;
								continue;
							}

							photo = AppContext.getApplication().getmUploadPhotoList().get(iFirstTimeUploadIndex);
						}
						
						if (photo!=null && photo.getFlowType().isPhotoBookWorkFlow()) {
							if (photo.isThumbnailUploaded()) {
								continue;
							}

							// Upload Thumbnail
							canUploadFullSize = false;
							//uploadingURI = photo.getLocalUri();
							uploadingPhoto = photo;
							String result = error;
							long begin = 0;
							long diff = 0;
							int count = 0;
							Log.d(TAG, "sunny___First start upload Thumbnails, File: " + photo);
							if (service == null) {
								service = new PrintMakerWebService(PictureUploadService2.this.getApplicationContext(), "kiosk");
							}
							begin = System.currentTimeMillis();
							while (count < 5 && error.equals(result)) {
								if (photo.getPhotoSource().isFromPhone()) {
									result = service.UploadPicture(getApplicationContext(),	photo, false);
								} else {
									result = service.addImageFromWebTask(photo);
								}
								count++;
							}
							double costSecond = (System.currentTimeMillis() - begin) / 1000.00;
							Log.i(TAG, "sunny___Thumbnail upload Time: " + costSecond + " retry times:" + count + " result:" + result);

							if (!error.equals(result)) {
								diff = System.currentTimeMillis() - begin;
								Log.d(TAG, "sunny___Done thumbnails upload: " + (diff / 1000.00) + " seconds. File: " + photo + " content_id: " + result);
								photo.setContentId(result);
								photo.setThumbnailUploaded(true);
								if (photo.getFlowType().isPhotoBookWorkFlow() && photo.getPhotoSource().isFromFaceBook()) {
									AppContext.getApplication().removePhotoFromUploadQueue(photo);
									AppContext.getApplication().getUploadSucceedImages().add(photo);
								}
								boolean found = false;
								// TODO UPDATE CONTENT_id for photo
								if (photo.getFlowType().isPhotoBookWorkFlow()) {
									List<Photobook> photobooks = AppContext.getApplication().getPhotobooks();
									if (photobooks != null) {
										for (Photobook photobook : photobooks) {
											if (photo.getProductId().equals(photobook.id)) {
												for (PhotoInfo photoInfo : photobook.selectedImages) {
													if (photoInfo.equals(photo)) {
														SelectedImage si = photobook.imageEditParams.get(photoInfo);
														si.sUploadImageID = photo.getContentId();
														photoInfo.setContentId(photo.getContentId());
														found = true;
														break;
													}
												}
											}
											if (found) {
												break;
											}
										}
									}
								}
							} else { // upload thumbnail failed
								if (iFirstTimeUploadIndex >= AppContext.getApplication().getmUploadPhotoList().size()) {
									iFirstTimeUploadIndex = -1;
									continue;
								}
								photo.setThumbnailUploaded(false);
								PrintHelper.qbUploadThumbError = true;
								Log.d(TAG, "Failed to upload Thumbnails: " + photo);
							}

							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						} else {
							continue;
						}

					}

							}
							// Done upload Thumbnails, should do PhotoBook work.
							isDoneUploadThumbnails = true;
							// if only Facebook images are selected for photobook
							if(AppContext.getApplication().getmUploadPhotoList().isEmpty()){
								continue;
							}
							/******* First, upload Thumbnails for PhotoBook --End *******/
							// check if has photo in photobook ,go out of this
							// loop
							if (isPhotoInPhotoBookExist() && !canUploadFullSize && isPhotoInPhotoBookThumnailNotUpload()) {
								continue;
							} else {
								// upload original photo
								int total = AppContext.getApplication().getUploadSucceedImages().size() + AppContext.getApplication().getmUploadPhotoList().size();
								int number = 1;

					try {
						number = AppContext.getApplication()
								.getUploadSucceedImages().size() + 1;
						notification.setLatestEventInfo(getApplicationContext(),getString(R.string.app_name),getString(R.string.upload1) + " "+ number + "/" + total,PendingIntent.getActivity(getApplicationContext(), 0,new Intent(), 0));
						(mManager == null ? (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE) : mManager).notify(0, notification);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					PhotoInfo photo = AppContext.getApplication().getmUploadPhotoList().get(0);
					String result = error;
					long begin = 0;
					long diff = 0;
					int count = 0;

					Log.d(TAG, "sunny___Start upload: " + photo);
					if (service == null) {
						service = new PrintMakerWebService(PictureUploadService2.this.getApplicationContext(), "kiosk");
					}
					begin = System.currentTimeMillis();
					long uploadBegin = System.currentTimeMillis();
					while (count < 5 && error.equals(result)) {
						if (photo.getFlowType().isPhotoBookWorkFlow() && photo.getPhotoSource().isFromFaceBook()) {
							// donothing
							result = error;
						} else {
							if (photo.getPhotoSource().isFromPhone()) {
								result = service.UploadPicture(getApplicationContext(),photo, true);
							} else {
								result = service.addImageFromWebTask(photo);
							}
						}
						count++;
					}
					double costSecond = (System.currentTimeMillis() - uploadBegin) / 1000.00;
					Log.i(TAG, "sunny___Upload Time: " + costSecond + " retry times:" + count + " result:" + result);

					if (!error.equals(result)) {
						if (photo.getPhotoSource().isFromPhone()) {
							diff = System.currentTimeMillis() - begin;
							PrintHelper.uploadTimes.add((diff / 1000.00));
							File fl = new File(photo.getPhotoPath());
							DecimalFormat df = new DecimalFormat("0.000#");
							double size = -1;
							if (fl.exists()) {
								size = ((fl.length()) / 1024.00) / 1024.00;
							}
							PrintHelper.uploadFileSize.add(size);
							Log.d(TAG, "Time to upload: " + (diff / 1000.00) + " seconds; File Size: " + df.format(size) + " mb; File " + photo + " content_id: " + result);
						}
						if (!(photo.getFlowType().isPhotoBookWorkFlow() && photo.getPhotoSource().isFromFaceBook())) {
							photo.setContentId(result);
						}

						// TODO update content_id for photo
						boolean found = false;
						if (photo.getFlowType().isPhotoBookWorkFlow()) {
							List<Photobook> photobooks = AppContext.getApplication().getPhotobooks();
							if (photobooks != null) {
								for (Photobook photobook : photobooks) {
									if (photo.getProductId().equals(photobook.id)) {
										for (PhotoInfo photoInfo : photobook.selectedImages) {
											if (photoInfo.equals(photo)) {
												SelectedImage si = photobook.imageEditParams.get(photoInfo);
												si.sUploadImageID = photo.getContentId();
												photoInfo.setContentId(photo.getContentId());
												found = true;
												break;
											}
										}
									}
									if (found) {
										break;
									}
								}
							}
						} else if (photo.getFlowType().isPrintWorkFlow()) {
							List<PrintInfo> prints = AppContext.getApplication().getmPrints();
							if (prints != null && prints.size() > 0) {
								for (PrintInfo printInfo : prints) {
									if (photo.equals(printInfo.getPhoto())) {
										printInfo.getPhoto().setContentId(photo.getContentId());
									}
								}

							}

						}else if(photo.getFlowType().isCollageWorkFlow()){
							Collage currentCollage = CollageManager.getInstance().getCurrentCollage() ;
							if(currentCollage!=null){
								if(photo.getProductId().equals(currentCollage.id)){
									if(AppContext.getApplication().getmUploadPhotoList().contains(photo)){
										currentCollage.page.addPhotoToList(photo) ;
									}
									
									
									
								}
								
							}
							
						}

						AppContext.getApplication()
								.removePhotoFromUploadQueue(photo);
						AppContext.getApplication()
								.getUploadSucceedImages()
								.add(photo);
						Log.e(TAG, "getUploadSucceedImages: " + AppContext.getApplication().getUploadSucceedImages().size() + ", PhotoFromUploadQueue: " + AppContext.getApplication().getmUploadPhotoList().size());

						total = AppContext.getApplication()
								.getUploadSucceedImages().size()
								+ AppContext.getApplication()
										.getmUploadPhotoList()
										.size();
						number = AppContext.getApplication()
								.getUploadSucceedImages().size() + 1;
						notification.setLatestEventInfo(getApplicationContext(),getString(R.string.app_name),getString(R.string.upload1)+" "+ AppContext.getApplication().getUploadSucceedImages().size()+ "/" + total, PendingIntent.getActivity(getApplicationContext(),0,new Intent(),0));

					} else {
						if (photo.getFlowType().isPhotoBookWorkFlow() && photo.getPhotoSource().isFromFaceBook()) {
							// right now don't know what have to do
						} else {
							Log.d(TAG, "Failed to upload: " + photo);
							PrintHelper.uploadError = true;
						}
					}
					try {
						(mManager == null ? (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)
								: mManager).notify(0, notification);
					} catch (Exception ex) {
					}
					Log.d(TAG, "uploadQueue size: " + PrintHelper.uploadQueue.size());
				}
			}
		}
	}

	private boolean isPhotoInPhotoBookExist() {
		boolean hasPhotoBook = false;
		synchronized ( AppContext.getApplication().getmUploadPhotoList()) {
			for (PhotoInfo photo : AppContext.getApplication().getmUploadPhotoList()) {
				if (photo.getFlowType().isPhotoBookWorkFlow()) {
					hasPhotoBook = true;
					break;
				}
			}
		}
		return hasPhotoBook;
	}

	private boolean isPhotoInPhotoBookThumnailNotUpload() {
		boolean thumnaiNotlUploaded = false;
		synchronized (AppContext.getApplication().getmUploadPhotoList()) {
			for (PhotoInfo photo : AppContext.getApplication().getmUploadPhotoList()) {
				if (photo.getFlowType().isPhotoBookWorkFlow() && !photo.isThumbnailUploaded()) {
					thumnaiNotlUploaded = true;
					break;
				}
			}
		}
		return thumnaiNotlUploaded;
	}

	public class LocalBinder extends Binder {
		PictureUploadService2 getService() {
			return PictureUploadService2.this;
		}
	}

	private static class UploadImagesThread extends Thread {

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

	public class UploadShare2WmcRunnable implements Runnable {

		@Override
		public void run() {
			while (PrintHelper.uploadShare2WmcQueue != null && !PrintHelper.uploadShare2WmcQueue.isEmpty()) {

				if (uploadingImage != null && uploadingImage.equals(PrintHelper.uploadShare2WmcQueue.get(0))) {
					try {
						Thread.sleep(3000);
						continue;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				int total = PrintHelper.uploadedShare2WmcQueue.size() + PrintHelper.uploadShare2WmcQueue.size();
				int number = PrintHelper.uploadedShare2WmcQueue.size() + 1;
				int totalUploading = AppContext.getApplication().getUploadSucceedImages().size() + PrintHelper.uploadQueue.size();
				int numberUploading = AppContext.getApplication().getUploadSucceedImages().size() + 1;
				String uploadingContent = getString(R.string.upload1)+ " "+ (numberUploading > totalUploading ? totalUploading: numberUploading) + "/" + totalUploading+ "; ";
				String shareingContent = getString(R.string.share_notification)+ " " + number + "/" + total;
				notification.setLatestEventInfo(getApplicationContext(),getString(R.string.app_name),(PrintHelper.uploadQueue.isEmpty() ? "": uploadingContent) + shareingContent,PendingIntent.getActivity(getApplicationContext(), 0,new Intent(), 0));
				(mManager == null ? (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE) : mManager).notify(0, notification);
				String uri = PrintHelper.uploadShare2WmcQueue.get(0);
				String filename = PrintHelper.selectedFileNames.get(uri);
				long begin = 0, end = 0, diff = 0;
				int count = 0;
				String result = "Error";
				Log.d(TAG, "Start UploadShare2WmcRunnable-Uploaded: " + filename + " id: " + result);
				while (count < 5 && !result.equals("OK")) {
					begin = System.currentTimeMillis();
					result = galleryService.addAPhoto(galleryService.galleryURL,PrintHelper.galleryUUID,PrintHelper.getAccessTokenResponse(getApplicationContext()).access_token,null, filename);
					count++;
				}

				if (result.equals("OK")) {
					end = System.currentTimeMillis();
					diff = end - begin;
					// PrintHelper.uploadTimes.add((diff / 1000.00));
					File fl = new File(filename);
					DecimalFormat df = new DecimalFormat("0.000#");
					double size = -1;
					if (fl.exists()) {
						size = ((fl.length()) / 1024.00) / 1024.00;
					}
					// PrintHelper.uploadFileSize.add(size);
					// PrintHelper.uploadShare2WmcImageIDs.put(filename,
					// result);
					PrintHelper.uploadedShare2WmcQueue.add(uri);
					PrintHelper.uploadShare2WmcQueue.remove(uri);
					Log.d(TAG, "UploadShare2WmcRunnable-Time to upload: "+ (diff / 1000.00) + " seconds  ||  File Size: "+ df.format(size) + " mb, Filename: " + filename+ " id: " + result);
				} else {
					Log.d(TAG, "UploadShare2WmcRunnable-Failed to upload: "+ filename);
					PrintHelper.uploadShare2WmcError = true;
					PrintHelper.uploadShare2WmcQueue = new ArrayList<String>();
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	}
}
