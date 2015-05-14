package com.kodakalaris.kodakmomentslib.manager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.kodakalaris.kodakmomentslib.AppConstants;
import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.bean.items.PrintItem;
import com.kodakalaris.kodakmomentslib.culumus.bean.product.ROI;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.Catalog;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.RssEntry;
import com.kodakalaris.kodakmomentslib.interfaces.IKM2Manager;
import com.kodakalaris.kodakmomentslib.interfaces.SaveRestoreAble;
import com.kodakalaris.kodakmomentslib.util.Log;
import com.kodakalaris.kodakmomentslib.util.SharedPreferrenceUtil;

public class PrintManager implements SaveRestoreAble, IKM2Manager {
	private static final String TAG = PrintManager.class.getSimpleName();
	private static final String KEY_PERFIX = "PrintManager_";
	private static final String DEFAULT_PRINT_SIZE = "default_print_size";

	private KM2Application mApp;
	private Context mContext;
	private RssEntry mDefaultPrintProduct;
	private List<RssEntry> mPrintProducts;

	private List<PrintItem> mPrintItems;
	private List<PhotoInfo> mPrintPhotos;

	private volatile static PrintManager instance;

	private PrintManager(Context context) {
		this.mContext = context.getApplicationContext();
		mApp = KM2Application.getInstance();
		mPrintItems = new ArrayList<PrintItem>();
	}

	public static PrintManager getInstance(Context context) {
		if (instance == null) {
			synchronized (PrintManager.class) {
				if (instance == null) {
					instance = new PrintManager(context);
				}
			}
		}
		return instance;
	}

	public void createNewPrintItems(List<PhotoInfo> photos) {
		if (photos == null || photos.size() == 0) {
			return;
		}
		if (mPrintItems == null || mPrintItems.size() == 0) {
			createPrintItems(photos);
		} else {
			for (PhotoInfo photoInfo : photos) {
				if (isPhotoAlreadyInPrintItems(photoInfo)) {
					continue;
				} else {
					createPrintItem(photoInfo, getDefaultPrintSize());
				}
			}
		}
	}

	public List<PrintItem> createPrintItems(List<PhotoInfo> images) {
		return createPrintItems(images, getDefaultPrintSize());
	}

	private List<PrintItem> createPrintItems(List<PhotoInfo> images,
			RssEntry entry) {
		List<PrintItem> newItems = new ArrayList<PrintItem>();
		if (images != null) {
			for (int i = 0; i < images.size(); i++) {
				PrintItem item = createPrintItem(images.get(i), entry);
				if (item != null) {
					newItems.add(item);
				}
			}
		}
		return newItems;
	}

	public PrintItem createPrintItem(PhotoInfo image, RssEntry entry) {

		PhotoInfo photo = image.builderPhotoWithRSSEntry(entry);
		PrintItem item = new PrintItem(photo, entry);
		if (mPrintItems.contains(item)) {
			return null;
		}
		mPrintItems.add(item);
		ShoppingCartManager.getInstance().getShoppingCartItems().add(item);
		addPhoto(photo);
		return item;
	}

	public void changeAllPhotoSize(List<PhotoInfo> images, int counts,
			RssEntry entry) {
		if (counts == 0) {
			for (PhotoInfo image : images) {
				deletePrintItem(image, entry);
			}
		} else if (counts > 0) {
			for (PhotoInfo image : images) {
				createPrintItem(image, entry);
			}
			for (PrintItem itemTemp : mPrintItems) {
				if (itemTemp.getEntry().equals(entry)) {
					itemTemp.setCount(counts);
				}
			}
		}

	}

	public void updateNumber(int count , PrintItem printItem){
		if(mPrintItems!=null){
			for (PrintItem item : mPrintItems) {
				if(item.equals(printItem)){
					item.setCount(count);
					break;
				}
			}
		}
	}

	public void updateRoi(ROI roi, PrintItem printItem) {
		PhotoInfo mImage = printItem.getImage();
		for (PrintItem itemTemp : mPrintItems) {
			if (itemTemp.equals(printItem)) {

				Log.i(TAG, "1.pre:" + roi.toString());
				if (!mImage.getPhotoEditPath().equals(
						itemTemp.getImage().getPhotoEditPath())) {
					itemTemp.getImage().setPhotoEditPath(
							mImage.getPhotoEditPath());
				}
				if (itemTemp.getImage().isNeedSwapWidthAndHeightForCalculate()
						&& !itemTemp.isServerImage && printItem.isServerImage) {
					itemTemp.isServerImage = true;
					itemTemp.getRoi().x = roi.y;
					itemTemp.getRoi().y = roi.x;
					itemTemp.getRoi().w = roi.h;
					itemTemp.getRoi().h = roi.w;
					itemTemp.getRoi().ContainerH = roi.ContainerW;
					itemTemp.getRoi().ContainerW = roi.ContainerH;
				} else {
					itemTemp.setRoi(roi);
				}
				Log.i(TAG, "2.late:" + itemTemp.getRoi().toString());
				itemTemp.isUseEnhance = printItem.isUseEnhance;
				itemTemp.isUseRedEye = printItem.isUseRedEye;
				itemTemp.isCheckedInstance = false;
				itemTemp.rotateDegree = printItem.rotateDegree;
				if (printItem.colorEffect != null) {
					itemTemp.colorEffect = printItem.colorEffect;
				}
				return;
			}
		}
	}

	public void deletePrintItem(PhotoInfo photo, RssEntry entry) {
		PhotoInfo photoDel = photo.builderPhotoWithRSSEntry(entry);
		PrintItem tempItem = new PrintItem(photoDel, entry);
		deletePrintItem(tempItem);
	}

	public void deletePrintItem(PrintItem printItem) {
		if (mPrintItems.contains(printItem)) {
			mPrintItems.remove(printItem);
			ShoppingCartManager.getInstance().getShoppingCartItems()
					.remove(printItem);
			// need to delete photo in photo list
			removePhoto(printItem.getImage());

		}
	}

	public void deletePrintItemsWithAppointedPhoto(PhotoInfo photo) {
		if (mPrintItems == null || mPrintItems.size() == 0) {
			removePhoto(photo);
			return;
		}
		synchronized (mPrintItems) {
			Iterator<PrintItem> itor = mPrintItems.iterator();
			while (itor.hasNext()) {
				PrintItem item = itor.next();
				if (item.getImage().equalsNotConsiderDesId(photo)) {
					itor.remove();
					ShoppingCartManager.getInstance().getShoppingCartItems()
							.remove(item);
					// need to delete photo in photo list
					removePhoto(item.getImage());

				}
			}
		}
	}

	public RssEntry getDefaultPrintSize() {
		if (mDefaultPrintProduct == null) {
			Catalog catalog = null;
			if (mApp.getCatalogs() != null && mApp.getCatalogs().size() > 0) {
				catalog = mApp.getCatalogs().get(0);
			}
			if (catalog != null) {
				String defaultPrintSize = SharedPreferrenceUtil.getString(
						mContext, DEFAULT_PRINT_SIZE);
				mDefaultPrintProduct = catalog
						.getProductEntry(defaultPrintSize);
				if (mDefaultPrintProduct == null) {
					// TODO choose the specified print product following spec
					mDefaultPrintProduct = getPrintProducts().get(0);
				}
			}
		}
		return mDefaultPrintProduct;
	}

	public void setDefaultPrintSize(RssEntry defaultPrintProduct) {
		this.mDefaultPrintProduct = defaultPrintProduct;
		SharedPreferrenceUtil.setString(mContext, DEFAULT_PRINT_SIZE,
				defaultPrintProduct.proDescription.id);
	}

	public List<RssEntry> getPrintProducts() {
		if (mApp.getCatalogs() != null && mApp.getCatalogs().size() > 0) {
			mPrintProducts = mApp.getCatalogs().get(0)
					.getProducts(AppConstants.PRO_TYPE_PRINT);
		}
		return mPrintProducts;

	}

	/**
	 * get all print items
	 * 
	 * @return
	 */
	public List<PrintItem> getPrintItems() {
		if (mPrintItems == null) {
			mPrintItems = new ArrayList<PrintItem>();
		}
		return mPrintItems;
	}
    /**
     * get print items which use the pointed photo
     * @param image
     * @return photo list
     */
	public List<PrintItem> getPrintItemsWithSamePhoto(PhotoInfo image) {
		List<PrintItem> items = new ArrayList<PrintItem>();
		if(mPrintItems!=null && mPrintItems.size()>0){
			for (PrintItem item : mPrintItems) {
				if (item.getImage().equalsNotConsiderDesId(image)) {
					items.add(item);
				}
			}
		}
		return items;
	}

	/**
	 * if photo in print item return true else return false
	 * just compare the photo's ID
	 * 
	 * @param photo
	 * @return
	 */
	public boolean isPhotoAlreadyInPrintItems(PhotoInfo photo) {
		boolean photoInPrintItems = false;
		if (mPrintItems == null || mPrintItems.size() == 0) {
			photoInPrintItems = false;
		} else {
			for (PrintItem item : mPrintItems) {
				if (item.getImage().equalsNotConsiderDesId(photo)) {
					photoInPrintItems = true;
					break;
				}
			}
		}
		return photoInPrintItems;
	}
	
	/**
	 * if a photo is used for more than one printItem return true else return false
	 * @param photo
	 * @return
	 */
	public boolean isPhotoMultipleSelected(PhotoInfo photo){
		boolean isMultiple = false ;
		if(photo!=null){
			int printItemCount = getPrintItemsWithSamePhoto(photo).size();
			isMultiple =  printItemCount > 1;
			
		}
		return isMultiple;
	}

	public List<PhotoInfo> getmPrintPhotos() {
		if (mPrintPhotos == null) {
			mPrintPhotos = new ArrayList<PhotoInfo>();
		}

		return mPrintPhotos;
	}

	public boolean addPhoto(PhotoInfo photo) {
		boolean success = false;
		if (mPrintPhotos == null) {
			mPrintPhotos = new ArrayList<PhotoInfo>();
		}

		if (!mPrintPhotos.contains(photo)) {
			synchronized (mPrintPhotos) {
				success = mPrintPhotos.add(photo);
			}
		}

		return success;
	}

	public boolean removePhoto(PhotoInfo photo) {
		boolean success = false;
		synchronized (mPrintPhotos) {
			if (mPrintPhotos != null && mPrintPhotos.size() > 0) {
				Iterator<PhotoInfo> itor = mPrintPhotos.iterator();
				while (itor.hasNext()) {
					PhotoInfo photoInList = (PhotoInfo) itor.next();
					if (photo.equals(photoInList)) {
						itor.remove();
						success = true;
						break;
					}

				}

			}
		}

		return success;
	}
	
	public List<RssEntry> getEnableProducts(List<RssEntry> selectedProducts){
		List<RssEntry> allEntries = getPrintProducts();
		List<RssEntry> enableProducts = new ArrayList<RssEntry>();
		
		List<String> enableRetailers = null;
		if(selectedProducts != null){
			for(RssEntry entry : selectedProducts){
				if(enableRetailers == null){
					enableRetailers = entry.proDescription.getEnableRetailers();
				} else {
					enableRetailers.retainAll(entry.proDescription.getEnableRetailers());
				}
			}
		}
		
		if(enableRetailers == null){
			enableProducts = allEntries;
		} else {
			for(RssEntry entry : allEntries){
				List<String> retailers = entry.proDescription.getEnableRetailers();
				retailers.retainAll(enableRetailers);
				if(retailers.size() > 0){
					enableProducts.add(entry);
				}	
			}
		}
		return enableProducts;
	}

	@Override
	public void saveGlobalVariables(Map<String, Serializable> saveMaps) {
		saveMaps.put(KEY_PERFIX + "mDefaultPrintProduct", mDefaultPrintProduct);
		saveMaps.put(KEY_PERFIX + "mPrintProducts",
				(Serializable) mPrintProducts);
		saveMaps.put(KEY_PERFIX + "mPrintItems", (Serializable) mPrintItems);
		saveMaps.put(KEY_PERFIX + "mPrintPhotos", (Serializable) mPrintPhotos);
	}

	@Override
	public void restoreGlobalVariables(Map<String, Serializable> restoreMaps) {
		try {
			mDefaultPrintProduct = (RssEntry) restoreMaps.get(KEY_PERFIX
					+ "mDefaultPrintProduct");
		} catch (Exception e) {
			Log.e(TAG, e);
		}
		try {
			mPrintProducts = (List<RssEntry>) restoreMaps.get(KEY_PERFIX
					+ "mPrintProducts");
		} catch (Exception e) {
			Log.e(TAG, e);
		}
		try {
			mPrintItems = (List<PrintItem>) restoreMaps.get(KEY_PERFIX
					+ "mPrintItems");
		} catch (Exception e) {
			Log.e(TAG, e);
		}
		try {
			mPrintPhotos = (List<PhotoInfo>) restoreMaps.get(KEY_PERFIX
					+ "mPrintPhotos");
		} catch (Exception e) {
			Log.e(TAG, e);
		}
	}

	@Override
	public void startOver() {
		mDefaultPrintProduct = null;
		mPrintProducts = null;
		if(mPrintItems!=null){
			mPrintItems.clear();
		}
		if(mPrintPhotos!=null){
			mPrintPhotos.clear();
		}
		
		
	}

}
