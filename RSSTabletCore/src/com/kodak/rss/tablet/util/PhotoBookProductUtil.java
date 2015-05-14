package com.kodak.rss.tablet.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.DisplayMetrics;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.LocalCustomerInfo;
import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.bean.ROI;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.bean.upload.ImageResource;
import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.core.util.SharedPreferrenceUtil;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.adapter.PhotoBooksProductAdapter;
import com.kodak.rss.tablet.bean.PhotoLocationPo;
import com.kodak.rss.tablet.bean.ProductLayerLocalInfo;

/**
 * Purpose: exchange the layer of page  
 * Author: Bing Wang
 * Created Time: Jan 10 2014 
 */
public class PhotoBookProductUtil extends ProductUtil{		
	
	public static Photobook getCurrentPhotoBook(){
		Photobook currentPhotoBook = null;
		if (RssTabletApp.getInstance().chosenBookList == null) {
			RssTabletApp.getInstance().chosenBookList = new ArrayList<Photobook>();
		}
		for (Photobook book : RssTabletApp.getInstance().chosenBookList) {
			if (book != null && book.isCurrentChosen) {
				currentPhotoBook = book;
				if (currentPhotoBook.chosenpics == null) {
					currentPhotoBook.chosenpics = new ArrayList<ImageInfo>();
				}
				break;
			}
		}
		return currentPhotoBook;
	}
	
	public static Photobook getPhotoBook(String productId){
		if (productId == null) return null;
		Photobook photoBook = null;
		if (RssTabletApp.getInstance().chosenBookList == null) {
			RssTabletApp.getInstance().chosenBookList = new ArrayList<Photobook>();
		}
		for (Photobook book : RssTabletApp.getInstance().chosenBookList) {
			if (book != null && book.id.equals(productId)) {
				photoBook = book;
				if (photoBook.chosenpics == null) {
					photoBook.chosenpics = new ArrayList<ImageInfo>();
				}
				break;
			}
		}
		return photoBook;
	}
	
	public static void setCurrentPhotoBook(Photobook photoBook){
		if (photoBook == null) return;
		photoBook.isCurrentChosen = true;
		Photobook currentPhotoBook = getCurrentPhotoBook();			
		if (currentPhotoBook != null ) {
			photoBook.chosenpics = currentPhotoBook.chosenpics;
			photoBook.chosenLayers = currentPhotoBook.chosenLayers;
			photoBook.author = currentPhotoBook.author;
			photoBook.title = currentPhotoBook.title;
			photoBook.subTitle = currentPhotoBook.subTitle;
			
			RssTabletApp.getInstance().chosenBookList.remove(currentPhotoBook);			
		}
		RssTabletApp.getInstance().chosenBookList.add(photoBook);				
	}
	
	public static void addCurrentPhotoBook(Photobook photoBook){
		if (RssTabletApp.getInstance().chosenBookList == null) {
			RssTabletApp.getInstance().chosenBookList = new ArrayList<Photobook>();
		}
		if (photoBook == null) return;
		photoBook.isCurrentChosen = true;
		if (photoBook.chosenpics == null) {
			photoBook.chosenpics = new ArrayList<ImageInfo>();
		}
		for (Photobook book : RssTabletApp.getInstance().chosenBookList) {
			if (book != null && book.isCurrentChosen) {
				book.isCurrentChosen = false;				
			}
		}				
		RssTabletApp.getInstance().chosenBookList.add(photoBook);				
	}
	
	public static ArrayList<Layer> getChosenServerPhotoList(List<PhotobookPage> pages,ArrayList<ImageInfo> nationPhotos, ArrayList<Layer> chosenLayers){		
		ArrayList<Layer> spList = new ArrayList<Layer>();	
		if (pages == null) return spList;
		for (PhotobookPage page : pages) {	
			if (page == null) continue;
			Layer[] layers = page.layers;				
			for (int i = 0; i < layers.length; i++) {
				Layer layer = layers[i];
				if (layer != null && layer.type.equals(Layer.TYPE_IMAGE) && layer.contentId != null && !"".equals(layer.contentId) ) {							
					if (getLayerImageInfo(layer,nationPhotos) == null && getLayerInfo(layer,chosenLayers) == null) {
						spList.add(layer);
					}
				}
			}								
		}
		if (chosenLayers != null) {
			spList.addAll(chosenLayers);
		}
		return spList;
	}		

	public static ImageInfo getLayerImageInfo(Layer layer){								
		Photobook currentPhotoBook = getCurrentPhotoBook();	
		ArrayList<ImageInfo> photobookschosenpics = currentPhotoBook.chosenpics;				
		return getLayerImageInfo(layer, photobookschosenpics, true);				
	}	
	
	public static List<PhotobookPage> getSimplexPage(List<PhotobookPage> pages){	
		int size = pages.size();
		List<PhotobookPage> simplexPages = new ArrayList<PhotobookPage>(size);				
		for (int j = 0; j < size; j++) {
			PhotobookPage page = pages.get(j);
			Layer[] layers = page.layers;
			if (layers != null) {							
				simplexPages.add(page);
			}									
		}					
		return simplexPages;
	}
	
	public static boolean isInlayer(ImageResource imageThumbnailResource){		
		boolean isInlayer = false;		
		if (imageThumbnailResource == null) return isInlayer;
		isInlayer = isInlayer(imageThumbnailResource.id);	
		if (!isInlayer && imageThumbnailResource.copyIds != null && imageThumbnailResource.copyIds.size() > 0) {
			for (String id : imageThumbnailResource.copyIds) {
				isInlayer = isInlayer(id);
				if (isInlayer) {
					break;
				}					
			}
		}	
		return isInlayer;			
	}	
	
	public static boolean isInPagelayer(Layer mlayer){
		boolean isInlayer = false;	
		if (mlayer == null) return isInlayer;
		isInlayer = isInlayer(mlayer.contentId);	
		if (!isInlayer && mlayer.copyIds != null && mlayer.copyIds.size() > 0) {
			for (String id :  mlayer.copyIds) {
				isInlayer = PhotoBookProductUtil.isInlayer(id);
				if (isInlayer) {
					break;
				}					
			}
		}	
		return isInlayer;			
	}	
	
	public static boolean isInlayer(String layerId){
		boolean isInlayer = false;	
		if (layerId == null || (layerId != null && "".equals(layerId))) return isInlayer;
		Photobook currentPhotoBook = getCurrentPhotoBook();			
		List<PhotobookPage> pages = currentPhotoBook.pages;		
		for (int j = 0; j < pages.size(); j++) {
			if (isInlayer) {
				break;
			}
			Layer[] layers = pages.get(j).layers;
			if (layers != null) {
				for (int k = 0; k < layers.length; k++) {
					Layer layer = layers[k];
					if (layer != null&& layer.contentId.equals(layerId)) {
						isInlayer = true;
						break;
					}
				}
			}		
		}				
		return isInlayer;
	}	
	
	public static void setLayerNull(String layerId,Layer[] layers){
		if (layers == null || layerId == null) return;
		int dealNum = -1;
		for (int i = 0; i < layers.length; i++) {
			Layer layer = layers[i];
			if (layer != null && layer.contentId != null && layerId.equals(layer.contentId)) {
				dealNum = i;
				break;
			}
		}
		if (dealNum >= 0) {
			layers[dealNum] = null;
		}
	}
		
	public static int getIsNotNullNum(Layer[] layers){
		int size = 0 ;
		if (layers != null) {
			for (int i = 0; i < layers.length; i++) {
				if (layers[i] != null) {
					size++;	
				}
			}
		}		
		return size;
	}
	
	public static void resetLayers(Layer[] layers){
		ArrayList<Layer> layerList = null;
		if (layers != null) {
			layerList = new ArrayList<Layer>(layers.length);
			for (int i = 0; i < layers.length; i++) {
				if (layers[i] != null) {
					layerList.add(layers[i]);
				}
			}			
		}		
		if (layerList != null) {
			for (int i = 0; i < layerList.size(); i++) {
				layers[i] = layerList.get(i);				
			}
			for (int i = layerList.size(); i < layers.length; i++) {
				layers[i] = null;	
			}	
		}	
	}
	
	public static void insertLayers(PhotobookPage page,int pos,Layer layer){
		if (page.layers != null && layer != null && pos >= 0 && pos < page.layers.length) {
			int notNullNum = 0;
			for (int i = 0; i < page.layers.length; i++) {
				if (page.layers[i] != null) {
					if (page.layers[i].contentId.equals(layer.contentId)) {
						return;
					}
					notNullNum += 1;
				}
			}			
			if (notNullNum < page.layers.length) {			
				for (int i = pos+1; i < page.layers.length; i++) {					
					page.layers[i] = page.layers[i-1];	
					page.layers[i-1] = null;
				}		
				page.layers[pos] = layer;		
			}						
		}				
	}
	
	public static ArrayList<Layer> getImageTypeLayers(Layer[] layers){
		ArrayList<Layer> layerList = null;
		if (layers != null) {
			layerList = new ArrayList<Layer>(layers.length);
			for (int i = 0; i < layers.length; i++) {
				if (layers[i] != null && Layer.TYPE_IMAGE.equals(layers[i].type) && layers[i].contentId != null && !"".equals(layers[i].contentId) ) {
					layerList.add(layers[i]);
				}
			}			
		}		
		return layerList;
	}
		
	public static ArrayList<PhotobookPage[]> getPageItems(Photobook mPhotobook) {	
		ArrayList<PhotobookPage[]> pageItems = null;
		if (mPhotobook.isDuplex) {			
			int mSize =mPhotobook.pages.size()/2 + 1 ;		
			pageItems = new ArrayList<PhotobookPage[]>(mSize);
			for (int i = 0; i < mSize; i++) {
				PhotobookPage[] pageItem = new PhotobookPage[2];
				if ( i == 0) {
					pageItem[1] = mPhotobook.pages.get(0);
				}else if(i == mSize -1){
					pageItem[0] = mPhotobook.pages.get(2*i-1);
				}else {
					pageItem[0] = mPhotobook.pages.get(2*i-1);
					pageItem[1] = mPhotobook.pages.get(2*i);
				}
				pageItems.add(pageItem);	
			}						
		}else {
			int mSize = mPhotobook.pages.size() - 1;		
			pageItems = new ArrayList<PhotobookPage[]>(mSize);			
			for (int i = 0; i < mSize; i++) {
				PhotobookPage[] pageItem = new PhotobookPage[2];
				if ( i == 0) {
					pageItem[1] = mPhotobook.pages.get(0);
				}else if(i == 1){
					pageItem[0] = mPhotobook.pages.get(1);
					pageItem[1] = mPhotobook.pages.get(2);
				}else if(i == mSize-1){
					pageItem[0] = mPhotobook.pages.get(i+1);
				}else {				
					pageItem[1] = mPhotobook.pages.get(i+1);
				}
				pageItems.add(pageItem);	
			}							
		}
		return pageItems;
	}

	public static PhotobookPage getPageFromDuplex(List<PhotobookPage[]> pageItems,PhotoLocationPo po){
		PhotobookPage page = null;		
		PhotobookPage[] pageItem = pageItems.get(po.hPosition);					
		if (po.isFront == 0) {
			page = pageItem[0];
		}else {
			page = pageItem[1];
		}
		return page;
	} 
	
	public static PhotobookPage getPageFromDuplex(List<PhotobookPage[]> pageItems, int position ,int isFront) {
		PhotobookPage page = null;
		PhotobookPage[] pageItem = pageItems.get(position);
		if (isFront == 0) {
			page = pageItem[0];
		} else {
			page = pageItem[1];
		}
		return page;
	}

	public static String getLocalImagePathByContentId(String contentId){
		if(contentId == null) {
			return null;
		}
		
		Photobook currentPhotoBook = getCurrentPhotoBook();	
		if(currentPhotoBook.chosenpics!= null && currentPhotoBook.chosenpics.size()>0){
			for(ImageInfo info : currentPhotoBook.chosenpics){
				if(info != null && info.imageThumbnailResource != null && info.isfromNative && contentId.equals(info.imageThumbnailResource.id)){
					return info.thumbnailUrl;
				}
			}
		}
		return null;
	}
	
	public static int getPageMaxNumInPhotoBook(List<PhotobookPage> pages){
	    int maxNum = 0;
	    if (pages == null) return maxNum;
	    for (int i = 0; i < pages.size(); i++) {
	    	PhotobookPage page = pages.get(i);	    	
	    	if (page != null ) {
	    		int num  = Integer.valueOf(page.maxNumberOfImages);
	    		if (num > maxNum) {
	    			maxNum = num;
				}
			}
		}
		return maxNum;
	}
	
	public static PhotobookPage getCurrentPage(int position,PhotoBooksProductAdapter adapter){
		PhotobookPage currentPage = adapter.activity.simplexPages.get(position);
		return currentPage;
	}
	
	public static PhotobookPage getCurrentPage(int position){
		PhotobookPage currentPage = null;
		Photobook currentPhotoBook = getCurrentPhotoBook();	
		PhotobookPage[] pageItems = getPageItems(currentPhotoBook).get(position);
		if (pageItems[0] != null && pageItems[1] == null) {
			currentPage = pageItems[0];
		}else if (pageItems[0] != null && pageItems[1] != null) {
			if (pageItems[0].layers != null && pageItems[1].layers == null) {
				currentPage = pageItems[0];
			}else if (pageItems[0].layers != null && pageItems[1].layers != null) {
				if (getIsNotNullNum(pageItems[0].layers) > getIsNotNullNum(pageItems[1].layers)) {
					currentPage = pageItems[1];
				}else {
					currentPage = pageItems[0];
				}
			}else { 
				currentPage = pageItems[1];
			}
		}else { 
			currentPage = pageItems[1];
		}		
		return currentPage;
	}
	
	public static PhotobookPage[] getCurrentPages(int position){
		PhotobookPage[] currentPages = null;
		Photobook currentPhotoBook = getCurrentPhotoBook();	
		currentPages = getPageItems(currentPhotoBook).get(position);		
		return currentPages;
	}
	
	public static int getPageInListPosition(PhotobookPage page){	
		int position = -1;
		if (page == null ) return 0;
		Photobook currentPhotoBook = getCurrentPhotoBook();	
		ArrayList<PhotobookPage[]> pageItems = getPageItems(currentPhotoBook);
		for (int i = 0; i < pageItems.size(); i++) {
			if (position != -1) {
				break;
			}			
			PhotobookPage[] item = pageItems.get(i);
			if (item != null) {
				for (int j = 0; j < item.length; j++) {
					PhotobookPage pageI = item[j];
					if (pageI!= null &&  pageI.id.equals(page.id)) {
						position = i;
						break;
					}
				}
			}
		}
		return position;
	}
	
	public static boolean getPhotobookPageEditable(PhotobookPage page){
		if (page == null) return false;
		boolean isEditable = true;
		if (PhotobookPage.TYPE_STANDARD.equalsIgnoreCase(page.pageType)) {
			isEditable = true;
		}else if (PhotobookPage.TYPE_COVER.equalsIgnoreCase(page.pageType)) {
			if (page.layers != null) {
				isEditable = true;
			}else {
				isEditable = false;
			}		
		}else if (PhotobookPage.TYPE_BACK_COVER.equalsIgnoreCase(page.pageType)) {
			if (page.layers != null) {
				isEditable = true;
			}else {
				isEditable = false;
			}		
		}else if (PhotobookPage.TYPE_TITLE.equalsIgnoreCase(page.pageType)) {
			isEditable = true;
		}else {
			isEditable = false;
		}
		return isEditable;
	}
	
	public static String getPageIndexText(Context context,Photobook photobook, PhotobookPage page){
		if(page==null)return "";
		
		if(PhotobookPage.TYPE_STANDARD.equals(page.pageType)){
			int index = 0;
			for(PhotobookPage p : photobook.pages){
				if(!PhotobookPage.TYPE_STANDARD.equals(p.pageType)){
					index++;
				}else{
					break;
				}
			}
			return String.valueOf(page.sequenceNumber- index +1);
		}else if(PhotobookPage.TYPE_TITLE.equals(page.pageType)){
			return context.getString(R.string.photobook_title);
		}else if(PhotobookPage.TYPE_COVER.equals(page.pageType)){
			return context.getString(R.string.photobook_cover);
		}else if(PhotobookPage.TYPE_BACK_COVER.equals(page.pageType)){
			return context.getString(R.string.photobook_back_cover);
		}else if(PhotobookPage.TYPE_INSIDE_COVER.equals(page.pageType)){
			return context.getString(R.string.photobook_inside_cover);
		}else if(PhotobookPage.TYPE_INSIDE_BACK_COVER.equals(page.pageType)){
			return context.getString(R.string.photobook_inside_back_cover);
		}else if(PhotobookPage.TYPE_DUPLEX_FILLER.equals(page.pageType)){
			return context.getString(R.string.photobook_duplex_filler);
		}else{
			return "";
		}
	}
	
	/**
	 * get the page in the same face(for duplex)
	 * @param photobook
	 * @param page
	 * @return
	 */
	public static PhotobookPage getNearbyPage(Photobook photobook, PhotobookPage page){
		if(photobook.isDuplex){
			int num = page.sequenceNumber%2==0 ? page.sequenceNumber+1:page.sequenceNumber-1;
			for(PhotobookPage p : photobook.pages){
				if(p.sequenceNumber == num){
					return p;
				}
			}			
		}
		return null;
	}
	
	// refresh page
	public static void updatePageData(ArrayList<PhotobookPage> pageList,PhotobookPage newPage,boolean isChange){
		if (newPage == null) return;
		if (pageList == null) return;
		if (isChange) {
			Photobook currentPhotoBook = getCurrentPhotoBook();
			if(currentPhotoBook!=null){
				synchronized (currentPhotoBook) {
					int index = getIndexByPageId(currentPhotoBook.pages, newPage.id);
					if(index != -1){
						PhotobookPage oldPage = currentPhotoBook.pages.get(index);
						oldPage.setPageRefresh();												
					}
				}
			}		
		}
		synchronized (pageList) {
			int index = getIndexByPageId(pageList, newPage.id);
			if(index != -1){			
				pageList.set(index, newPage);			
			}else {
				pageList.add(newPage);
			}
		}				
	}
	
	/**
	 * @param photobook
	 * @param id
	 * @return -1 means not found
	 */
	public static int getIndexByPageId(List<PhotobookPage> pages,String id){
		if (pages == null) return -1;
		for(int i=0; i < pages.size(); i++){
			PhotobookPage page = pages.get(i);
			if(id.equals(page.id)){
				return i;
			}
		}
		return -1;
	}
	
	public static boolean updatePageInPhotobook(PhotobookPage newPage,boolean isChange){
		Photobook currentPhotoBook = getCurrentPhotoBook();
		if(currentPhotoBook!=null){
			synchronized (currentPhotoBook) {
				int index = getIndexByPageId(currentPhotoBook.pages, newPage.id);
				if(index != -1){
					PhotobookPage oldPage = currentPhotoBook.pages.get(index);
					newPage.baseURI = oldPage.baseURI;
					newPage.setPageRefresh(oldPage.getMainRefreshCount(), oldPage.getMainRefreshSucCount(),oldPage.getThumbRefreshCount(), oldPage.getThumbRefreshSucCount());
					if (isChange) {
						newPage.setPageRefresh();
					}
					currentPhotoBook.pages.set(index, newPage);
					return true;
				}
			}
		}
		return false;
	}
	
	public static void refreshSucPageInPhotobook(String pageId,boolean isThumbanil,int refreshNum){
		Photobook currentPhotoBook = getCurrentPhotoBook();
		if(currentPhotoBook!=null){
			synchronized (currentPhotoBook) {
				int index = getIndexByPageId(currentPhotoBook.pages, pageId);
				if(index != -1){
					PhotobookPage page = currentPhotoBook.pages.get(index);					
					if (isThumbanil) {
						page.thumbRefreshSuc(refreshNum);
					}else {
						page.mainRefreshSuc(refreshNum);					
					}					
					return ;
				}
			}
		}
	}	
	
	public static boolean isLayerEditable(Layer layer){
		return layer.contentId != null && !layer.contentId.isEmpty();
	}
	
	public static boolean isTitlePage(PhotobookPage page){
		if(PhotobookPage.TYPE_TITLE.equals(page.pageType)){
			return true;
		}
		
		if(PhotobookPage.TYPE_COVER.equals(page.pageType)){
			if(page.layers!=null){
				return true;
			}
		}
		return false;
	}
	
	public static void dealWithItem(Context context,float ratio){		
		Photobook currentPhotoBook = getCurrentPhotoBook();
		if (RssTabletApp.getInstance().products == null) {
			RssTabletApp.getInstance().products = new ArrayList<ProductInfo>();
		}			
		ProductInfo dealInfo = null;														
		for(ProductInfo pInfo : RssTabletApp.getInstance().products) {
			if (pInfo != null && AppConstants.bookType.equals(pInfo.productType)) {				
				if (pInfo.correspondId.equals(currentPhotoBook.id) ) {												
					dealInfo = pInfo;
					break;												
				}
			}
		}							
		if (dealInfo != null) {
			if (dealInfo.num < 1) {
				RssTabletApp.getInstance().products.remove(dealInfo);
			}else {
				dealInfo.chosenImageList = currentPhotoBook.chosenpics;
				setDisplayPath(dealInfo,context,ratio);
			}			
		}else {
			dealInfo = new ProductInfo();
			dealInfo.descriptionId = currentPhotoBook.proDescId;
			dealInfo.num = 1;
			dealInfo.productType = AppConstants.bookType;
			dealInfo.correspondId = dealInfo.cartItemId = currentPhotoBook.id;
			dealInfo.chosenImageList = currentPhotoBook.chosenpics;
			setDisplayPath(dealInfo,context,ratio);					
			RssTabletApp.getInstance().products.add(dealInfo);
		}
	}
	
	private static void setDisplayPath(ProductInfo dealInfo,Context context,float ratio){		
		if (dealInfo.downloadDisplayImageUrl != null) return;
		if (ratio <= 0) return;
		Photobook currentPhotoBook = getCurrentPhotoBook();
		DisplayMetrics dm = context.getResources().getDisplayMetrics();	
		int pageWidth = (int) ((dm.widthPixels - dm.density*50)/8f);							
		int pageHeight = (int) (pageWidth*ratio);	
		String displayId = "";
		PhotobookPage page = getTitlePage(currentPhotoBook);
		if (page != null ) {
			displayId = page.id;
			dealInfo.displayImageUrl = displayId;
			dealInfo.downloadDisplayImageUrl = getUrl(page, pageWidth, pageHeight).toString();
		}		
	}

	/**
	 * get photobook default author
	 * @param context
	 * @return
	 */
	public static String getDefaultPhotobookAuthor(Context context){
		String author = SharedPreferrenceUtil.getFacebookFristName(context)+ " " + SharedPreferrenceUtil.getFacebookLastName(context);
		author = author.trim();
		if("".equals(author)){
			LocalCustomerInfo customerInfo = new LocalCustomerInfo(context);
			if(customerInfo != null){
				author = customerInfo.getCusFirstName() + " " + customerInfo.getCusLastName();
				author = author.trim();
			}
		}
		return author==null ? "" : author;
	}
	
	/**
	 * get photobook default title 
	 * @return
	 */
	public static String getDefaultPhotobookTitle(){
		String title = "";
		Photobook currentPhotoBook = getCurrentPhotoBook();
		List<ImageInfo> list = currentPhotoBook.chosenpics;
		if(list != null && !list.isEmpty()){
			title = list.get(list.size()-1).bucketDisplayName;
		}		
		return title==null ? "" : title;
	}
	
	public static boolean isLayerLowRes(Layer layer){		
		int DPI = 300;
		float acceptablescalefactor = 2.0f;
		
		boolean returnValue = false;
		
		if(layer == null) {
			return returnValue;
		}
		
		ROI cropRoi = getImageCropROI(layer);
		ROI locationRoi = layer.location;
		
		if(cropRoi == null || locationRoi == null) {
			return returnValue;
		}
		
		double needW = locationRoi.w * DPI;
		double needH = locationRoi.h * DPI;
		
		String filePath = getLocalImagePathByContentId(layer.contentId);
		double w , h ;
		if(filePath != null){//local image
			int originalW, originalH;
			int[] size = ImageUtil.getImageSize(filePath);
			Photobook photobook = getCurrentPhotoBook();
			String proDescId = null;
			if (photobook != null) {
				proDescId = photobook.proDescId;
			}
			
			int[] resize = RssTabletApp.getInstance().getSizeForResize(size[0], size[1],AppConstants.bookType,proDescId);
			if(resize == null){//no resize
				originalW = size[0];
				originalH = size[1];
			}else{
				String resizePath = ImageUtil.getResizeFilePath(filePath, resize);
				if(new File(resizePath).exists()){//resized
					originalW = resize[0];
					originalH = resize[1];
				}else{//resized but not succeed, app will use original image
					originalW = size[0];
					originalH = size[1];
				}
			}
			
			//check exif rotate
			int exifRotateDegrees = ImageUtil.getDegreesExifOrientation(filePath);
			if(exifRotateDegrees == 90 || exifRotateDegrees == 270){
				int temp = originalW;
				originalW = originalH;
				originalH = temp;
			}
			
			//check image roate
			ProductLayerLocalInfo layerLocalInfo = RssTabletApp.getInstance().getProductLayerLocalInfos().get(layer.contentId);
			if(layerLocalInfo != null && (layerLocalInfo.getRotateAngle() == 90 || layerLocalInfo.getRotateAngle() == 270) ){//roated vertical
				int temp = originalW;
				originalW = originalH;
				originalH = temp;
			}
			
			double percentOfHeight = cropRoi.h > 1 ? cropRoi.h / cropRoi.ContainerH : cropRoi.h;
			double percentOfWidth = cropRoi.w > 1 ? cropRoi.w / cropRoi.ContainerW : cropRoi.w;
			
			w = originalW * percentOfWidth ;
			h = originalH * percentOfHeight ;
		}else{
			//This case means image downloaded from server(ex: from my project), the original image must have been uploaded
			//So we directly use server data
			w = cropRoi.w > 1 ? cropRoi.w : cropRoi.ContainerW * cropRoi.w;
			h = cropRoi.h > 1 ? cropRoi.h : cropRoi.ContainerW * cropRoi.h;
		}
		
		if( w < h ){
			returnValue = w * acceptablescalefactor < needW;
		}else{
			returnValue = h * acceptablescalefactor < needH;
		}			
		return returnValue;
	}
	
	/**
	 * if back cover can add image and now it has no image, return true
	 * @param book
	 * @return
	 */
	public static boolean isBackCoverPageBlank(Photobook book){
		for(int i=book.pages.size()-1;i>=0;i--){
			PhotobookPage page = book.pages.get(i);
			if(PhotobookPage.TYPE_BACK_COVER.equals(page.pageType) && page.layers!=null && page.layers.length>0){
				Layer layer = page.layers[0];
				if(layer.contentId.equals("")){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * if back cover can add image and now it has no image, return true
	 * @param PhotobookPage
	 * @return
	 */
	public static boolean isBackCoverPageBlank(PhotobookPage page){		
		if (page == null) return false;
		if(PhotobookPage.TYPE_BACK_COVER.equals(page.pageType) && page.layers!=null && page.layers.length>0){
			Layer layer = page.layers[0];
			if(layer!=null && layer.contentId.equals("")&& Layer.TYPE_IMAGE.equals(layer.type)){
				return true;
			}
		}		
		return false;
	}
	
	/**
	 * if inside cover can add image and now it has no image, return true
	 * @param PhotobookPage
	 * @return
	 */
	public static boolean isInsideCoverPageBlank(PhotobookPage page){		
		if (page == null) return false;
		if(PhotobookPage.TYPE_INSIDE_COVER.equals(page.pageType) && page.layers!=null && page.layers.length>0){
			Layer layer = page.layers[0];
			if(layer!=null && layer.contentId.equals("")&& Layer.TYPE_IMAGE.equals(layer.type)){
				return true;
			}
		}		
		return false;
	}
	
	/**
	 * check whether the count of images in photobook is greater than the minNumberOfImages of photobook  
	 * @param book
	 * @return
	 */
	public static boolean hasEnoughImages(Photobook book){
		int num = 0;
		for(PhotobookPage page : book.pages){
			if(page.layers!=null && page.layers.length>0){
				for(int i=0;i<page.layers.length;i++){
					Layer layer = page.layers[i];
					if(layer != null && Layer.TYPE_IMAGE.equals(layer.type)){
						num++;
					}
				}
			}
		}	
		return num >= book.minNumberOfImages;
	}
	
	/**
	 * check whether photobook has blank page
	 * @param book
	 * @return
	 */
	public static boolean hasBlankPage(Photobook book){
		for(PhotobookPage page : book.pages){
			if(isPageBlank(page)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * check whether photobook has filler page
	 * @param book
	 * @return
	 */
	public static int getFillerPageNum(Photobook book) {
		int haveDuplexFillerNum = 0;
		int size = book.pages.size();
		for (int i = size -1; i>=0; i--) {
			PhotobookPage page = book.pages.get(i);
			if (PhotobookPage.TYPE_DUPLEX_FILLER.equals(page.pageType)) {
				haveDuplexFillerNum = haveDuplexFillerNum + 1;
			}
		}	
		return haveDuplexFillerNum;
	}
	
	public static boolean isPageBlank(PhotobookPage page){
		if(page.layers!=null){
			boolean hasLayer = false;
			for(int i=0;i<page.layers.length;i++){
				if(page.layers[i]!=null) {
					hasLayer = true;
					break;
				}
			}			
			return !hasLayer;
		}	
		return false;
	}
	
	public static PhotobookPage getBackCoverPage(Photobook book){
		for(int i=book.pages.size()-1;i>=0;i--){
			PhotobookPage page = book.pages.get(i);
			if(PhotobookPage.TYPE_BACK_COVER.equals(page.pageType)){
				return page;
			}
		}
		return null;
	}
	
	public static PhotobookPage getTitlePage(Photobook book){
		for(PhotobookPage page : book.pages){
			if(isTitlePage(page)){
				return page;
			}
		}
		return null;
	}
	
	public static boolean isCoverHollow(Photobook book){
		return book.isDuplex && !isTitlePage(book.pages.get(0));
	}
		
	public static boolean isDiaplayMove(PhotobookPage[] pageItems){
		boolean isDiaplayMove = false;
		if (pageItems == null) return isDiaplayMove;
		if ((pageItems[1]!= null && PhotobookPage.TYPE_TITLE.equalsIgnoreCase(pageItems[1].pageType))					
				||(pageItems[0]!= null && PhotobookPage.TYPE_BACK_COVER.equalsIgnoreCase(pageItems[0].pageType))
				||(pageItems[1]!= null && PhotoBookProductUtil.getPhotobookPageEditable(pageItems[1]) && PhotobookPage.TYPE_COVER.equalsIgnoreCase(pageItems[1].pageType))) {
			isDiaplayMove = true;
		}						
		return isDiaplayMove;	
	}

	public static int getLayerCount(PhotobookPage page){
		int sum = 0;
		for(int i=0;i<page.layers.length;i++){
			if(page.layers[i] != null){
				sum++;
			}
		}
		return sum;
	}
	
	public static Layer findLayerById(PhotobookPage page, String id){
		if(page != null && id != null && page.layers!= null){
			for(int i=0; i<page.layers.length ; i++){
				if(page.layers[i].contentId.equals(id)){
					return page.layers[i];
				}
			}
		}		
		return null;
	}
	
	public static int findLayerPosition(PhotobookPage page, Layer layer){
		if(page != null && layer != null && layer.contentId != null && page.layers!= null){
			for(int i=0; i<page.layers.length ; i++){
				if(page.layers[i].contentId.equals(layer.contentId)){
					return i;
				}
			}
		}		
		return -1;
	}
	
	public static void deleteUnselectPhoto(Photobook photoBook){		
		if (photoBook == null) return;			
		deleteUnselectNativePhoto(photoBook);
		deleteUnselectServerPhoto(photoBook);
	}
	
	public static void deleteUnselectNativePhoto(Photobook photoBook){					
		if (photoBook.chosenpics == null ) return;
		if (photoBook.chosenpics.size() == 0) return;
		ArrayList<ImageInfo>  delList = null;
		for (int pos = 0; pos < photoBook.chosenpics.size(); pos++) {
			ImageInfo info = photoBook.chosenpics.get(pos);
			if (info == null) continue;
			if (info.imageThumbnailResource == null) continue;			
			if (!isInlayer(info.imageThumbnailResource)) {
				if (delList == null) {
					delList = new ArrayList<ImageInfo>();
				}
				delList.add(info);
			}
		}
		if (delList != null && delList.size() > 0) {
			synchronized (photoBook){
				photoBook.chosenpics.removeAll(delList);
			}	
		}		
	}
	
	public static void deleteUnselectServerPhoto(Photobook photoBook){						
		if (photoBook.chosenLayers == null ) return;
		if (photoBook.chosenLayers.size() == 0) return;
		ArrayList<Layer>  delList = null;
		for (int pos = 0; pos < photoBook.chosenLayers.size(); pos++) {
			Layer layer = photoBook.chosenLayers.get(pos);
			if (layer == null) continue;					
			if (!isInPagelayer(layer)) {
				if (delList == null) {
					delList = new ArrayList<Layer>();
				}
				delList.add(layer);
			}
		}
		if (delList != null && delList.size() > 0) {
			synchronized (photoBook){
				photoBook.chosenLayers.removeAll(delList);
			}	
		}		
	}	

}
