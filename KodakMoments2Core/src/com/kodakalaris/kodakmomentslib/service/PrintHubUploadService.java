package com.kodakalaris.kodakmomentslib.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Service;
import android.content.Intent;
import android.graphics.Point;
import android.os.IBinder;
import android.util.Log;

import com.kodakalaris.kodakmomentslib.AppConstants;
import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.bean.SimpleKPPCommandCallback;
import com.kodakalaris.kodakmomentslib.bean.items.PrintItem;
import com.kodakalaris.kodakmomentslib.culumus.bean.product.ROI;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.RssEntry;
import com.kodakalaris.kodakmomentslib.manager.PrintHubManager;
import com.kodakalaris.kodakmomentslib.util.ImageUtil;
import com.kodakalaris.kpp.EPrintSizeType;
import com.kodakalaris.kpp.KPPAsset;
import com.kodakalaris.kpp.KodakPrintPlace;
import com.kodakalaris.kpp.PrinterInfo;

public class PrintHubUploadService extends Service {
	public static boolean mTerminated = false;
	public static boolean isRunning = true;
	public static String TAG = "PrintHubUploadService";
	private List<KPPAsset> kppAssets;
	private int currAssetIndex = 0;
	private int retryTimes = 0;
	private String currentJobId = "";
	private KodakPrintPlace kpp;

	@Override
	public void onCreate() {
		setKppCommandCallback();
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				createPrintJobNow();
			}
		}).start();

		return START_STICKY;

	}

	private void PrepareOrder() {
		if (kpp == null) {
			kpp = PrintHubManager.getInstance().getKodakPrintPlace();
		}
		PrinterInfo pi = kpp.getCachedPrinterInformation();
		kppAssets = new ArrayList<KPPAsset>();
		for (PrintItem printItem : PrintHubManager.getInstance().getPrintItems()) {
			if (printItem == null)
				continue;
			Point p = getRatePoint(printItem);
			if (p == null || p.x == 0 || p.y == 0)
				continue;
			int w = p.x;
			int h = p.y;

			ROI roi = printItem.getRoi();
			int rotateDegree = printItem.rotateDegree;

			String editFileParentPath = KM2Application.getInstance().getTempImageFolderPath() + "/printHub";
			File baseDir = new File(editFileParentPath);
			if (!baseDir.exists()) {
				baseDir.mkdirs();
			}
			String loadFilePath = "";
			String photoEditPath = printItem.getImage().getPhotoEditPath();
			if (!photoEditPath.equals("")) {
				loadFilePath = photoEditPath;
			} else {
				loadFilePath = printItem.getImage().getPhotoPath();
			}
			if (loadFilePath == null || "".equals(loadFilePath))
				return;

			String editName = printItem.getImage().getPhotoId();
			String editSize = printItem.getEntry().proDescription.name;

			String desPicFilePath = editFileParentPath + "/" + "range_" + editName + "_" + editSize + ".jpeg";
			boolean isSuccess = ImageUtil.rangePic(loadFilePath, desPicFilePath, roi, rotateDegree);
			Log.i(TAG, "isSuccess:" + isSuccess + " desPicFilePath:" + desPicFilePath);
			if (!isSuccess)
				continue;

			int[] imageInfo = ImageUtil.getImageSize(desPicFilePath);
			if (imageInfo == null)
				continue;

			if (is305Printter(pi) && !is5X7(printItem)) {
				w = 2422;
				h = 1864;
			}
			if ((w < h && imageInfo[0] > imageInfo[1]) || (w > h && imageInfo[0] < imageInfo[1])) {
				w = w ^ h;
				h = w ^ h;
				w = w ^ h;
			}

			String desFilePath = editFileParentPath + "/" + editName  + "_" + editSize+ ".jpeg";
			if (!is305Printter(pi) || is5X7(printItem)) {
				isSuccess = ImageUtil.resizePic(desPicFilePath, desFilePath, w, h);
				Log.i(TAG, "isSuccess:" + isSuccess + " desFilePath:" + desFilePath);
				if (!isSuccess)
					continue;
			} else {
				isSuccess = ImageUtil.placeImageWithWhite(desPicFilePath, desFilePath, w, h);
				Log.i(TAG, "isSuccess:" + isSuccess + " desFilePath:" + desFilePath);
				if (!isSuccess)
					continue;
			}
			printItem.getImage().setPhotoEditPath(desFilePath);

			KPPAsset asset = new KPPAsset(printItem.getImage().getPhotoEditPath());
			asset.setPrintCopies(printItem.getCount());
			asset.setPrintSize(getPrintSize(printItem));
			kppAssets.add(asset);
		}
	}

	private void setKppCommandCallback() {
		kpp = PrintHubManager.getInstance().getKodakPrintPlace();
		kpp.setCommandCallback(new SimpleKPPCommandCallback() {
			@Override
			public void onUploadAssetDone(KPPAsset asset) {
				// prepare to upload next asset now!
				retryTimes = 0;
				currAssetIndex++;
				if (currAssetIndex == kppAssets.size()) {
					kpp.finishUpload(currentJobId);
				} else {
					kpp.uploadAsset(currentJobId, kppAssets.get(currAssetIndex));
				}
				sendBroadcast(AppConstants.UPLOAD_PRINTHUB_PHOTO_SUCCESS);
				super.onUploadAssetDone(asset);
			}

			@Override
			public void onUploadAssetFailed(KPPAsset asset, String errMsg) {
				retryTimes++;
				if (retryTimes < AppConstants.RETRY_TIMES_PRINTHUB) {
					kpp.uploadAsset(currentJobId, kppAssets.get(currAssetIndex));
				} else {
					sendBroadcast(AppConstants.UPLOAD_PRINTHUB_PHOTO_FAIL);
					super.onUploadAssetFailed(asset, errMsg);
				}
			}

			@Override
			public void onPrintJobCreated(String jobId) {
				currentJobId = jobId;
				kpp.uploadAsset(currentJobId, kppAssets.get(currAssetIndex));
				super.onPrintJobCreated(jobId);
			}

			@Override
			public void onCreatePrintJobFailed(String errMsg) {
				sendBroadcast(AppConstants.CREATE_PRINTJOB_FAIL);
				super.onCreatePrintJobFailed(errMsg);
			}

		});
	}

	private void sendBroadcast(int uploadResult) {
		Intent intent = new Intent(AppConstants.UPLOAD_PRINTHUB_PHOTO_ACTION);
		intent.putExtra(AppConstants.UPLOAD_PRINTHUB_PHOTO_RESULT, uploadResult);
		intent.putExtra(AppConstants.UPLOAD_PRINTHUB_PHOTO_INDEX, currAssetIndex);
		intent.putExtra(AppConstants.UPLOAD_PRINTHUB_JOB_ID, currentJobId);
		PrintHubUploadService.this.sendBroadcast(intent);
	}

	private void createPrintJobNow() {
		boolean hasFoundPrintHub = kpp.isPrintHubConnected();
		if (hasFoundPrintHub) {
			PrepareOrder();
			kpp.createJob("", "");
			Log.i(TAG, "*** Create Print Job Request Sent!");
		} else {
			kpp.searchPrintHub();
			sendBroadcast(AppConstants.CONNECT_PRINTHUB_FAIL);
		}
	}

	private EPrintSizeType getPrintSize(PrintItem printItem) {
		EPrintSizeType mPrintSize = null;
		if (printItem.getEntry().proDescription.name.contains("3_5x5")) {
			mPrintSize = EPrintSizeType.size3_5x5;
		} else if (printItem.getEntry().proDescription.name.contains("4x6")) {
			mPrintSize = EPrintSizeType.size4x6;
		} else if (printItem.getEntry().proDescription.name.contains("5x7")) {
			mPrintSize = EPrintSizeType.size5x7;
		} else if (printItem.getEntry().proDescription.name.contains("6x8")) {
			mPrintSize = EPrintSizeType.size6x8;
		} else if (printItem.getEntry().proDescription.name.contains("8x10")) {
			mPrintSize = EPrintSizeType.size8x10;
		}
		return mPrintSize;
	}

	private Point getRatePoint(PrintItem pItem) {
		Point point = null;
		RssEntry pRssEntry = pItem.getEntry();
		if (pRssEntry == null)
			return point;
		if (pRssEntry.proDescription == null)
			return point;
		int pageHeight = pRssEntry.proDescription.pageHeight;
		int pageWidth = pRssEntry.proDescription.pageWidth;
		point = new Point(pageWidth, pageHeight);
		return point;
	}

	private boolean is305Printter(PrinterInfo pin) {
		if (pin != null && pin.getModel() != null) {
			Pattern p = Pattern.compile("305\\D|\\D305\\D|\\D305$");
			Matcher ma = p.matcher(pin.getModel());
			if (ma.find()) {
				return true;
			}
		}
		return false;
	}

	private boolean is5X7(PrintItem printItem) {
		if (printItem.getEntry().proDescription.name.contains("5x7")) {
			return true;
		}
		return false;
	}
}
