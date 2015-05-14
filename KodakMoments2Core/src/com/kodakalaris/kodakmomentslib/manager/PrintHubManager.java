package com.kodakalaris.kodakmomentslib.manager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.bean.items.PrintItem;
import com.kodakalaris.kodakmomentslib.culumus.bean.product.ROI;
import com.kodakalaris.kodakmomentslib.culumus.bean.project.ProductDescription;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.RssEntry;
import com.kodakalaris.kodakmomentslib.interfaces.IKM2Manager;
import com.kodakalaris.kodakmomentslib.interfaces.SaveRestoreAble;
import com.kodakalaris.kpp.KodakPrintPlace;
import com.kodakalaris.kpp.PrintSize;
import com.kodakalaris.kpp.PrinterInfo;

public class PrintHubManager implements SaveRestoreAble, IKM2Manager {
	private static PrintHubManager sInstance;

	private KodakPrintPlace mKpp;
	private List<PrintItem> mPrintItems;
	private List<PhotoInfo> mPrintPhotos;
	private RssEntry mDefaultPrintProduct;
	private List<RssEntry> mPrintProducts;
	private PrintSize[] mSupportPrintSizes;
	private PrintHubManager() {
		mPrintItems = new ArrayList<PrintItem>();
	}

	public static PrintHubManager getInstance() {
		if (sInstance == null) {
			sInstance = new PrintHubManager();
		}

		return sInstance;
	}

	public void setKodakPrintPlace(KodakPrintPlace kpp) {
		mKpp = kpp;
	}

	public KodakPrintPlace getKodakPrintPlace() {
		return mKpp;
	}

	public List<PhotoInfo> getmPrintPhotos() {
		if (mPrintPhotos == null) {
			mPrintPhotos = new ArrayList<PhotoInfo>();
		}

		return mPrintPhotos;
	}
	
	
	public PrintSize[] getSupportPrintSizes() {
		return mSupportPrintSizes;
	}

	public void setSupportPrintSizes(PrintSize[] supportPrintSizes) {
		this.mSupportPrintSizes = supportPrintSizes;
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

	private List<PrintItem> createPrintItems(List<PhotoInfo> images, RssEntry entry) {
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

	/**
	 * if photo in print item return true else return false just compare the photo's ID
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

	public RssEntry getDefaultPrintSize() {
		return mDefaultPrintProduct;
	}

	public void setDefaultPrintSize(RssEntry defaultPrintProduct) {
		this.mDefaultPrintProduct = defaultPrintProduct;
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
	
	public List<RssEntry> getPrintProducts() {
		mPrintProducts = new ArrayList<RssEntry>();
		for (PrintSize printSize : mSupportPrintSizes) {
			RssEntry rssEntry = new RssEntry();
			ProductDescription proDescription = new ProductDescription();
			proDescription.id = printSize.getSize().name();
			proDescription.name = printSize.getSize().name();
			proDescription.shortName = printSize.getSize().name();
			proDescription.pageHeight = printSize.getLongEdgePixel();
			proDescription.pageWidth = printSize.getShortEdgePixel();
			rssEntry.proDescription = proDescription;
			mPrintProducts.add(rssEntry);
		}
		return mPrintProducts;

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
				
				if (!mImage.getPhotoEditPath().equals(itemTemp.getImage().getPhotoEditPath())) {
					itemTemp.getImage().setPhotoEditPath(mImage.getPhotoEditPath());
				}
				
				itemTemp.isCheckedInstance = false;
				itemTemp.setRoi(roi);
				itemTemp.rotateDegree = printItem.rotateDegree;			
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

	@Override
	public void startOver() {

	}

	@Override
	public void saveGlobalVariables(Map<String, Serializable> saveMaps) {

	}

	@Override
	public void restoreGlobalVariables(Map<String, Serializable> restoreMaps) {

	}

}
