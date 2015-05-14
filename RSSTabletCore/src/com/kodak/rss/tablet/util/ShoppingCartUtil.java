package com.kodak.rss.tablet.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.LocalCustomerInfo;
import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.n2r.bean.calendar.Calendar;
import com.kodak.rss.core.n2r.bean.collage.Collage;
import com.kodak.rss.core.n2r.bean.greetingcard.GreetingCard;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.retailer.Catalog;
import com.kodak.rss.core.n2r.bean.retailer.CountryInfo;
import com.kodak.rss.core.n2r.bean.retailer.Retailer;
import com.kodak.rss.core.n2r.bean.retailer.RssEntry;
import com.kodak.rss.core.n2r.bean.storelocator.StoreInfo;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.BaseActivity;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageDownloader;
import com.kodak.rss.tablet.util.load.Request;

public class ShoppingCartUtil {
	
	public static void setListViewHeightBasedOnChildren(ListView listView, int margin, int spacing, int height) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		/*int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}*/

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = height;//totalHeight + spacing;
		((MarginLayoutParams) params).setMargins(margin, margin, margin, margin);
		listView.setLayoutParams(params);
	}
	
	public static List<ProductInfo> getGroupProductInfoList(List<ProductInfo> proInfos, String type){
		List<ProductInfo> list = new ArrayList<ProductInfo>();
		for(ProductInfo proInfo: proInfos){
			if(proInfo.descriptionId.equals(type) && proInfo.num>0){
				list.add(proInfo);
			}
		}
		return list;		
	}
	
	public static int getProductsCount(List<ProductInfo> proInfos, String desId){
		int count = 0;
		for(ProductInfo proInfo: proInfos){
			if(proInfo.descriptionId.equals(desId)){
				count += proInfo.num;
			}
		}
		return count;
	}
	
	public static int getProductsCount(List<ProductInfo> proInfos, String desId, String proId){
		int count = 0;
		for(ProductInfo proInfo: proInfos){
			if(proInfo.descriptionId.equals(desId) && proInfo.cartItemId.equals(proId)){
				count += proInfo.num;
			}
		}
		return count;
	}
	
	public static String getProductName(List<Catalog> catalogs, String type){
		if(catalogs != null){
			for(Catalog catalog : catalogs){
				for(RssEntry entry : catalog.rssEntries){
					if(entry.proDescription.id.equals(type)){
						return entry.proDescription.name;
					}
				}
			}
		}
		return "";
	}
	
	public static String getProductDescriptionIDs(List<ProductInfo> proInfos){
		List<String> desIds = getProductDescriptionIDList(proInfos);
		String strDesIds = "";
		for(int i=0; i<desIds.size(); i++){
			if(i != desIds.size()-1){
				strDesIds += desIds.get(i) + ",";
			} else {
				strDesIds += desIds.get(i);
			}
		}
		
		return strDesIds;
	}
	
	public static List<String> getProductDescriptionIDList(List<ProductInfo> productInfos){
		List<String> products = new ArrayList<String>();
		List<String> tempProducts = new ArrayList<String>();
		if(productInfos != null){
			for(ProductInfo proInfo : productInfos){
				boolean exist = false;
				for(String tempType : tempProducts){
					if(proInfo.descriptionId.equals(tempType)){
						exist = true;
						break;
					}
				}
				if(!exist){
					tempProducts.add(proInfo.descriptionId);
				}
			}
		}
		// reorder the products
		List<Catalog> catalogs = RssTabletApp.getInstance().getCatalogList();
		if(catalogs != null){
			for(Catalog catalog : catalogs){
				List<RssEntry> entries = catalog.rssEntries;
				if(entries != null){
					for(RssEntry entry : entries){
						for(String id : tempProducts){
							if(entry.proDescription.id.equals(id)){
								products.add(id);
								break;
							}
						}
					}
				}
			}
		}
		
		return products;	
	}
	
	public static String getProductsWithCount(List<ProductInfo> proInfos){
		List<String> prodcutDesIds = getProductDescriptionIDList(proInfos);
		List<List<ProductInfo>> groupProducts = new ArrayList<List<ProductInfo>>();
		for(String descId : prodcutDesIds){
			groupProducts.add(getGroupProductInfoList(proInfos, descId));
		}
		String productsWithCount = "";
		int productCount = 0;
		for(int i=0; i<prodcutDesIds.size(); i++){
			String descId = prodcutDesIds.get(i);
			List<ProductInfo> products = groupProducts.get(i);
			RssEntry entry = getProductEntry(descId);
			if(entry.proDescription.type.equalsIgnoreCase(AppConstants.bookType)){
				List<Photobook> photobooks = RssTabletApp.getInstance().chosenBookList;
				for(ProductInfo proInfo : products){
					for(Photobook photobook: photobooks){
						if(proInfo.correspondId.equals(photobook.id)){
							int realPageNumber = photobook.pages.size();
							for(PhotobookPage page : photobook.pages){
								if(PhotobookPage.TYPE_DUPLEX_FILLER.equalsIgnoreCase(page.pageType)){
									realPageNumber --;
								}
							}
							productsWithCount += descId + "(" + realPageNumber + ")" + "=" + proInfo.num + ",";
						}
					}
				}
			} else {
				productCount = getProductsCount(products, descId);
				productsWithCount += descId + "=" + productCount + ",";
			}
		}
		String[] tempArr = productsWithCount.split(",");
		productsWithCount = "";
		for(int i=0; i<tempArr.length; i++){
			if(i != tempArr.length-1){
				productsWithCount += tempArr[i] + ",";
			} else {
				productsWithCount += tempArr[i];
			}
		}
		return productsWithCount;
	}
	
	public static RssEntry getProductEntry(String descriptionId){
		List<Catalog> catalogs = RssTabletApp.getInstance().getCatalogList();
		if(catalogs != null){
			for(Catalog catalog : catalogs){
				RssEntry entry = catalog.getProductEntry(descriptionId);
				if(entry != null){
					return entry;
				}
			}
		}
		return null;
	}
	
	public static boolean isCustomerInfoValid(Context context, boolean shippingToHome){
		int[] requiredInfo = null;
		RssTabletApp app = RssTabletApp.getInstance();
		List<Retailer> retailers = app.getRetailers();
		LocalCustomerInfo customer = new LocalCustomerInfo(context);
		StoreInfo store = StoreInfo.loadSelectedStore(context);
		if(retailers == null || customer==null){
			return false;
		}
		if(shippingToHome){
			for(Retailer r : retailers){
				if(r.shipToHome == true){
					requiredInfo = r.requiredCustomerInfo;
					break;
				}
			}
		} else {
			if(store == null){
				return false;
			}
			for(Retailer r : retailers){
				if(r.id.equals(store.retailerID)){
					requiredInfo = r.requiredCustomerInfo;
					break;
				}
			}
		}
		boolean valid = true;
		if(requiredInfo != null){
			for(int i=0; i<requiredInfo.length; i++){
				switch (requiredInfo[i]) {
				case 0:
					if(customer.getCusFirstName().equals("")){
						valid = false;
					}
					break;
				case 1:
					if(customer.getCusLastName().equals("")){
						valid = false;
					}
					break;
				case 2:
					if(customer.getCusPhone().equals("")){
						valid = false;
					}
					break;
				case 6:
					if(customer.getCusEmail().equals("")){
						valid = false;
					}
					break;
				}
			}
		}
		return valid;
	}
	
	public static boolean isShippingAddressValid(Context context){
		RssTabletApp app = RssTabletApp.getInstance();
		CountryInfo countryInfo = app.getCountryInfo(app.getCountrycodeCurrentUsed());
		LocalCustomerInfo customer = new LocalCustomerInfo(context);
		boolean valid = true;
		if(customer.getShipFirstName().equals("")){
			valid = false;
		}
		if(customer.getShipLastName().equals("")){
			valid = false;
		}
		if(customer.getShipAddress1().equals("")){
			valid = false;
		}
		if(countryInfo != null){
			if(countryInfo.addressStyle.contains(CountryInfo.CITY)){
				if(customer.getShipCity().equals("")){
					valid = false;
				}
			}
			if(countryInfo.addressStyle.contains(CountryInfo.STATE)){
				if(customer.getShipState().equals("")){
					valid = false;
				}
			}
			if(countryInfo.addressStyle.contains(CountryInfo.ZIP)){
				if(customer.getShipZip().equals("")){
					valid = false;
				}
			}
		}
		return valid;
	}
	
	public static boolean isStoreInfoValid(Context context){
		StoreInfo store = StoreInfo.loadSelectedStore(context);
		if(store == null){
			return false;
		}
		return true;
	}
	
	public static String PAYMENT_SUCCESS = "0";
	public static String PAYMENT_ABORTED = "100";
	public static String PAYMENT_CANCELED = "150";
	public static String PAYMENT_FAILED = "200";
	public static String PAYMENT_ERROR = "300";
	public static String PAYMENT_UNEXPECTED = "400";
	public static String getPaymentStatus(String url){
		final String STATUS = "status";
		String status = "";
		if(url.contains(STATUS)){
			status = url.substring(url.lastIndexOf("=")+1, url.length());
			return status;
		}
		return PAYMENT_UNEXPECTED;
	}

	public static void updateImageInfoList(List<ProductInfo> proInfos){
		for(ProductInfo proInfo : proInfos){
			if(proInfo.productType.equalsIgnoreCase(AppConstants.printType)){
				ImageInfo selectImageInfo = null;
				if (proInfo.chosenImageList != null && proInfo.chosenImageList.size() > 0) {
					selectImageInfo = proInfo.chosenImageList.get(0);
				}	
				if (selectImageInfo == null) continue;
				List<ProductInfo> products = selectImageInfo.typeMap.get(AppConstants.printType);
				if (products != null) {
					for (ProductInfo productInfo : products) {
						if (productInfo != null && productInfo.num > 0) {	
							if(productInfo.descriptionId.equals(proInfo.descriptionId)){
								productInfo.num = proInfo.num;
								break;
							}
						}
					}
				}									
			} else if(proInfo.productType.equalsIgnoreCase(AppConstants.bookType)){
				Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
				if(currentPhotoBook != null && proInfo.correspondId.equals(currentPhotoBook.id)){
					currentPhotoBook.quantity = proInfo.num;
					break;
				}
			}
		}
	}
	
	public static void dealWithItem(ImageInfo info,ProductInfo productInfo){
		if (info == null || productInfo == null) return;		
		if (RssTabletApp.getInstance().products == null) {
			RssTabletApp.getInstance().products = new ArrayList<ProductInfo>();
		}			
		boolean isHave = false;																
		for(ProductInfo pInfo : RssTabletApp.getInstance().products) {
			if (pInfo != null && pInfo.chosenImageList!= null && AppConstants.printType.equals(pInfo.productType)) {
				ImageInfo choseIamgeInfo = pInfo.chosenImageList.get(0);
				if (choseIamgeInfo.id.equals(info.id) && pInfo.category.equals(productInfo.category) ) {												
					isHave = true;
					break;												
				}
			}
		}							
		if (isHave) {
			if (productInfo.num < 1) {
				RssTabletApp.getInstance().products.remove(productInfo);
			}			
		}else {
			productInfo.productType = AppConstants.printType;			
			productInfo.chosenImageList = new ArrayList<ImageInfo>();
			productInfo.chosenImageList.add(info);			
			RssTabletApp.getInstance().products.add(productInfo);
		}
	}
	
	public static void delItem(ImageInfo info){
		if (info == null || RssTabletApp.getInstance().products == null) return;								
		List<ProductInfo> delList = new ArrayList<ProductInfo>();
		for(ProductInfo pInfo : RssTabletApp.getInstance().products) {
			if (pInfo != null && pInfo.chosenImageList!= null && AppConstants.printType.equals(pInfo.productType)) {
				ImageInfo choseIamgeInfo = pInfo.chosenImageList.get(0);
				if (choseIamgeInfo.id.equals(info.id) ) {												
					delList.add(pInfo);																	
				}
			}
		}	
		RssTabletApp.getInstance().products.removeAll(delList);		
	}
	
	public static void delItem(ProductInfo pInfo){
		if (pInfo == null || RssTabletApp.getInstance().products == null) return;
		pInfo.num = 0;
		if (AppConstants.printType.equals(pInfo.productType)) {	
			boolean isDel = true;
			ImageInfo delIamgeInfo = pInfo.chosenImageList.get(0);
			if (delIamgeInfo != null && delIamgeInfo.typeMap != null) {			
				List <ProductInfo> productInfoList = delIamgeInfo.typeMap.get(AppConstants.printType);
				for (int j = 0; productInfoList != null && j < productInfoList.size(); j++) {							
					ProductInfo productInfo = productInfoList.get(j);
					if (productInfo != null && productInfo.num > 0) {
						isDel = false;
					}						
				}														
			}
			if (isDel) {
				RssTabletApp.getInstance().chosenList.remove(delIamgeInfo);
			}			
		}else if (AppConstants.bookType.equals(pInfo.productType)){
			List<Photobook> chosenBookList = RssTabletApp.getInstance().chosenBookList;
			Photobook delBook = null;
			if (chosenBookList != null) {
				for (int i = 0; i < chosenBookList.size(); i++) {
					Photobook book = chosenBookList.get(i);
					if (book!= null && book.id.equals(pInfo.correspondId)) {
						delBook = book;
						break;
					}
				}
			}
			if (delBook != null) {
				RssTabletApp.getInstance().chosenBookList.remove(delBook);
			}	
		}			
		RssTabletApp.getInstance().products.remove(pInfo);		
	}
	
	public static void judgeImageDownload(Context mContext,boolean isSetCurrentBook, boolean isOriginal){
		cancelDownPhotos();
		RssTabletApp app = RssTabletApp.getInstance();	
		if (isSetCurrentBook) {
			Photobook  currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
			if (currentPhotoBook == null) return;
			if (isOriginal) {
				downLoadOriginalAgain(currentPhotoBook.chosenpics,FilePathConstant.bookType,currentPhotoBook.id);
			}else {
				downLoadThumbnailAgain(currentPhotoBook.chosenpics,FilePathConstant.bookType,currentPhotoBook.id);				
			}
			return;
		}
		if (app.chosenBookList != null) {
			for (Photobook book : app.chosenBookList) {
				if (book == null) continue;
				if (isOriginal) {
					downLoadOriginalAgain(book.chosenpics,FilePathConstant.bookType,book.id);
				}else {
					downLoadThumbnailAgain(book.chosenpics,FilePathConstant.bookType,book.id);				
				}				
			}					
		}
		if (isOriginal) {
			downLoadOriginalAgain(app.chosenList,FilePathConstant.printType,null);			
		}else {
			downLoadThumbnailAgain(app.chosenList,FilePathConstant.printType,null);
		}				
	}
	
	private static void downLoadOriginalAgain(List<ImageInfo> chosenList,String flowType, String bookId){		
		if (chosenList == null) return;					
		if (chosenList.size() == 0) return;		
		for (ImageInfo imageInfo : chosenList) {
			if (imageInfo == null) continue;
			if (imageInfo.isfromNative) continue;
			if (imageInfo.originalUrl != null) continue;				
			String originalPath =  FilePathConstant.getLoadFilePath(FilePathConstant.externalType, imageInfo.id, false);
			if (originalPath == null) {
				URI pictureURI = null;
				try {
					pictureURI = new URI(imageInfo.downloadOriginalUrl);
				} catch (URISyntaxException e) {
					pictureURI = null;
				}
				if (pictureURI != null && !(imageInfo.origHeight > 0 && imageInfo.origWidth > 0)) {					
					RssTabletApp.getInstance().imageDownloader.downloadProfilePicture(imageInfo.id, pictureURI, null, 0, true,false,flowType,bookId);
				}
			}else {
				imageInfo.originalUrl= originalPath;
				imageInfo.editUrl = originalPath;	
				if (!(imageInfo.origHeight > 0 && imageInfo.origWidth > 0)) {
					imageInfo.uploadOriginalUrl = originalPath;
				}				
			}							
		}			
	}
	
	private static void downLoadThumbnailAgain(List<ImageInfo> chosenList,String flowType, String bookId){		
		if (chosenList == null) return;					
		if (chosenList.size() == 0) return;		
		for (ImageInfo imageInfo : chosenList) {
			if (imageInfo == null) continue;
			if (imageInfo.isfromNative) continue;
			if (imageInfo.thumbnailUrl != null) continue;				
			String thumbnailPath = FilePathConstant.getLoadFilePath(FilePathConstant.externalType, imageInfo.id, true);
			if (thumbnailPath == null) {
				URI pictureURI = null;
				try {
					pictureURI = new URI(imageInfo.downloadThumbnailUrl);
				} catch (URISyntaxException e) {
					pictureURI = null;
				}
				if (pictureURI != null && !(imageInfo.origHeight > 0 && imageInfo.origWidth > 0)) {					
					RssTabletApp.getInstance().imageDownloader.downloadProfilePicture(imageInfo.id, pictureURI, null, 0, true,true,flowType,bookId);
				}
			}else {
				imageInfo.thumbnailUrl = thumbnailPath;	
				if (!(imageInfo.origHeight > 0 && imageInfo.origWidth > 0)) {
					imageInfo.uploadThumbnailUrl = thumbnailPath;
				}
			}					
		}			
	}	
	
	public static void judgeImageUpload(Context mContext,boolean isSetCurrentBook,boolean isOriginal){	
		int wantNum = 0;
		RssTabletApp app = RssTabletApp.getInstance();	
		if (isSetCurrentBook) {
			Photobook  currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
			if (currentPhotoBook == null) return;
			if (isOriginal) {
				wantNum += setUploadOriginalAgain(currentPhotoBook.chosenpics);
			}else {
				wantNum += setUploadThumbnailAgain(currentPhotoBook.chosenpics);				
			}		
			((BaseActivity)mContext).startUploadService();					
			return;
		}
		if (app.chosenBookList != null) {
			for (Photobook book : app.chosenBookList) {
				if (book == null) continue;
				if (isOriginal) {
					wantNum += setUploadOriginalAgain(book.chosenpics);
				}else {
					wantNum += setUploadThumbnailAgain(book.chosenpics);					
				}				
			}					
		}
		
		if (app.gCardList != null) {
			for (GreetingCard card : app.gCardList) {
				if (card == null) continue;
				if (isOriginal) {
					wantNum += setUploadOriginalAgain(card.chosenpics);
				}		
			}					
		}
		
		if (app.calendarList != null) {
			for (Calendar calenar : app.calendarList) {
				if (calenar == null) continue;
				if (isOriginal) {
					wantNum += setUploadOriginalAgain(calenar.chosenpics);
				}		
			}					
		}
		
		if (app.collageList != null) {
			for (Collage collage : app.collageList) {
				if (collage == null) continue;
				if (isOriginal) {
					wantNum += setUploadOriginalAgain(collage.chosenpics);
				}		
			}					
		}

		if (isOriginal) {
			wantNum += setUploadOriginalAgain(app.chosenList);
		}else {
			wantNum += setUploadThumbnailAgain(app.chosenList);
		}			
		((BaseActivity)mContext).startUploadService();				
	}

	private static int setUploadOriginalAgain(List<ImageInfo> chosenList){
		int wantUploadNum = 0;
		if (chosenList == null) return wantUploadNum;					
		if (chosenList.size() == 0) return wantUploadNum;
		int sucNum = UploadProgressUtil.getUploadPicSuccessNum(chosenList, false);
		if (chosenList.size() <= sucNum) return wantUploadNum;
		String FIRST_UPLOAD_ORIGINAL = "FIRST_UPLOAD_ORIGINAL";	
		for (ImageInfo imageInfo : chosenList) {
			if (imageInfo == null) continue;
			if (imageInfo.imageOriginalResource != null) continue;
			if (imageInfo.uploadOriginalUrl == null) continue;							
			if(!imageInfo.uploadOriginalUrl.startsWith(FIRST_UPLOAD_ORIGINAL)) {
				imageInfo.isHavedOriginalUpload = false;
				continue;
			}
					
			imageInfo.isHavedOriginalUpload = false;			
			if (imageInfo.isfromNative) {
				Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageInfo.id);
				imageInfo.uploadOriginalUrl = uri.toString();	
			}else {
				if ((imageInfo.origHeight > 0 && imageInfo.origWidth > 0)) {
					imageInfo.uploadOriginalUrl = imageInfo.downloadOriginalUrl;
				}else {
					imageInfo.uploadOriginalUrl = imageInfo.originalUrl;
				}
			}		
			if (imageInfo.editUrl == null) {									
				imageInfo.editUrl = imageInfo.originalUrl;
			}	
			wantUploadNum += 1;
		}	
		return wantUploadNum;
	}
	
	private static int setUploadThumbnailAgain(List<ImageInfo> chosenList){
		int wantUploadNum = 0;
		if (chosenList == null) return wantUploadNum;					
		if (chosenList.size() == 0) return wantUploadNum;
		int sucNum = UploadProgressUtil.getUploadPicSuccessNum(chosenList, true);
		if (chosenList.size() <= sucNum) return wantUploadNum;
		String FIRST_UPLOAD_THUMBNAIL = "FIRST_UPLOAD_THUMBNAIL";
		for (ImageInfo imageInfo : chosenList) {
			if (imageInfo == null) continue;
			if (imageInfo.imageThumbnailResource != null) continue;
			if (imageInfo.uploadThumbnailUrl == null) continue;		
			if(!imageInfo.uploadThumbnailUrl.startsWith(FIRST_UPLOAD_THUMBNAIL)){
				imageInfo.isHavedThumbnailUpload = false;
				continue;
			}	
			if (imageInfo.isfromNative) {
				Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageInfo.id);
				imageInfo.uploadThumbnailUrl = uri.toString();	
			}else {
				if ((imageInfo.origHeight > 0 && imageInfo.origWidth > 0)) {
					imageInfo.uploadThumbnailUrl = imageInfo.downloadOriginalUrl;
				}else {
					imageInfo.uploadThumbnailUrl = imageInfo.thumbnailUrl;
				}	
			}							
			wantUploadNum += 1;
		}	
		return wantUploadNum;
	}
	
	public static void cancelDownPhotos(){	
		RssTabletApp app = RssTabletApp.getInstance();	
		if (app.pendingRequests == null) return;
		if (app.pendingRequests.size() == 0) return;	
		if (app.chosenBookList != null) {
			for (Photobook book : app.chosenBookList) {
				if (book == null) continue;
				if (book.chosenpics == null) continue;
				if (book.chosenpics.size() == 0) continue;
				cancelDownPhotos(book.chosenpics, book.id);					
			}					
		}
		if (app.chosenList != null && app.chosenList.size() > 0) {			
			cancelDownPhotos(app.chosenList, null);													
		}				
	}	
	
	private static void cancelDownPhotos(List<ImageInfo> chosenList,String productId){
		if (chosenList == null) return;					
		if (chosenList.size() == 0) return;
		RssTabletApp app = RssTabletApp.getInstance();				
		for (ImageInfo imageInfo : chosenList) {
			if (imageInfo == null) continue;
			if (imageInfo.isfromNative) continue;
			if (!(imageInfo.origHeight > 0 && imageInfo.origWidth > 0)) continue;
			if (imageInfo.originalUrl == null) {
				String pendKey = productId + imageInfo.id;
				Request request = app.pendingRequests.get(pendKey);
			    if (request != null) {       	
			        ImageDownloader.cancelRequest(request);       		
			        RssTabletApp.getInstance().pendingRequests.remove(pendKey);				          				        	
			     }      
			}
			if (imageInfo.thumbnailUrl == null) {
				String pendKey = productId + FilePathConstant.thumbnail + imageInfo.id;
				Request request = app.pendingRequests.get(pendKey);
				if (request != null) {       	
			        ImageDownloader.cancelRequest(request);       		
			        RssTabletApp.getInstance().pendingRequests.remove(pendKey);				          				        	
			    }    
			}										     			
		}					
	}		
	
	public static String formatStoreDetail(StoreInfo store){
		String storeInfo = "";
		
		if(!store.name.equals("")){
			storeInfo += store.name + "\n";
		}
		if(!store.address.address1.equals("")){
			storeInfo += store.address.address1 + "\n";
		}
		if(!store.address.address2.equals("")){
			storeInfo += store.address.address2 + "\n";
		}
		if(!store.address.address3.equals("")){
			storeInfo += store.address.address3 + "\n";
		}
		if(!store.address.city.equals("")){
			storeInfo += store.address.city + "\n";
		}
		if(!store.address.stateProvince.equals("")){
			storeInfo += store.address.stateProvince + "\n";
		}
		if(!store.address.postalCode.equals("")){
			storeInfo += store.address.postalCode + "\n";
		}
		/*if(!store.address.country.equals("")){
			storeInfo += store.address.country + "\n";
		}*/
		if(!store.phone.equals("")){
			storeInfo += store.phone + "\n";
		}
		
		return storeInfo;
	}
	
	public static String formatShippingDetail(LocalCustomerInfo customer){
		String shipInfo = "";
		if(!customer.getShipFirstName().equals("") || !customer.getShipLastName().equals("")){
			shipInfo += customer.getShipFirstName() + " " + customer.getShipLastName() + "\n";
		}
		if(!customer.getShipAddress1().equals("")){
			shipInfo += customer.getShipAddress1() + "\n";
		}
		if(!customer.getShipAddress2().equals("")){
			shipInfo += customer.getShipAddress2() + "\n";
		}
		if(!customer.getShipCity().equals("")){
			shipInfo += customer.getShipCity() + "\n";
		}
		if(!customer.getShipState().equals("")){
			shipInfo += customer.getShipState() + "\n";
		}
		if(!customer.getShipZip().equals("")){
			shipInfo += customer.getShipZip() + "\n";
		}
		return shipInfo;
	}
	
	/*
	 * fixed for RSSMOBILEPDC-1566
	 * get the value of ShowsMSRPPricing form server
	 */
	public static boolean isShowsMSRPPricing(){
		String temValue = "";
		RssTabletApp app = RssTabletApp.getInstance();
		CountryInfo countryInfo = app.getCountryInfo(app.getCountrycodeCurrentUsed());
		boolean isShowsMSRPPricing = false;
		if (countryInfo != null) {
			if (countryInfo != null) {
				temValue = countryInfo.countryAttributes.get("ShowsMSRPPricing");
				if (temValue.equals("true")){
					isShowsMSRPPricing = true;
				}
			}
		}
		return isShowsMSRPPricing;
	}
	
}
