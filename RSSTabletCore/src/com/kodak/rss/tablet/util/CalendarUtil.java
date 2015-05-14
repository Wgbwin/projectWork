package com.kodak.rss.tablet.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.DisplayMetrics;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.n2r.bean.calendar.Calendar;
import com.kodak.rss.core.n2r.bean.calendar.CalendarDaysGridInfo;
import com.kodak.rss.core.n2r.bean.calendar.CalendarGridItemPO;
import com.kodak.rss.core.n2r.bean.calendar.CalendarLayer;
import com.kodak.rss.core.n2r.bean.calendar.CalendarPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.util.load.FilePathConstant;

public class CalendarUtil extends ProductUtil{
		
	public static void addCurrentCalendar(Calendar calendar){
		if (RssTabletApp.getInstance().calendarList == null) {
			RssTabletApp.getInstance().calendarList = new ArrayList<Calendar>();
		}
		if (calendar == null) return;
		calendar.isCurrentChosen = true;
		if (calendar.chosenpics == null) {
			calendar.chosenpics = new ArrayList<ImageInfo>();
		}
		for (Calendar caldar : RssTabletApp.getInstance().calendarList) {
			if (caldar != null && caldar.isCurrentChosen) {
				caldar.isCurrentChosen = false;				
			}
		}				
		RssTabletApp.getInstance().calendarList.add(calendar);				
	}
	
	public static void setCurrentCalendar(Calendar calendar){
		if (calendar == null) return;
		if (RssTabletApp.getInstance().calendarList == null) {
			RssTabletApp.getInstance().calendarList = new ArrayList<Calendar>();
		}
		calendar.isCurrentChosen = true;
		Calendar currentCalendar = getCurrentCalendar();			
		if (currentCalendar != null ) {
			calendar.chosenpics = currentCalendar.chosenpics;
			RssTabletApp.getInstance().calendarList.remove(currentCalendar);			
		}
		RssTabletApp.getInstance().calendarList.add(calendar);				
	}
	
	public static Calendar getCurrentCalendar(){
		Calendar currentCalendar = null;
		if (RssTabletApp.getInstance().calendarList == null) {
			RssTabletApp.getInstance().calendarList = new ArrayList<Calendar>();
		}
		for (Calendar caldar : RssTabletApp.getInstance().calendarList) {
			if (caldar != null && caldar.isCurrentChosen) {
				currentCalendar = caldar;
				if (currentCalendar.chosenpics == null) {
					currentCalendar.chosenpics = new ArrayList<ImageInfo>();
				}
				break;
			}
		}
		return currentCalendar;
	}	
	
	public static int getIndexByPageId(List<CalendarPage> pages,String id){
		if (pages == null) return -1;
		for(int i=0; i < pages.size(); i++){
			CalendarPage page = pages.get(i);
			if(id.equals(page.id)){
				return i;
			}
		}
		return -1;
	}
	
	public static int getPagePositionByPageIndex(Calendar calendar, int index) {
		if (calendar == null || index < 0 || calendar.pages == null || calendar.pages.size() == 0) {
			return -1;
		}
		
		if (calendar.getCalendarType() == Calendar.Monthly_Simplex || calendar.getCalendarType() == Calendar.Annual_Calendars) {
			return index;
		} 
		
		if (calendar.getCalendarType() == Calendar.Monthly_Duplex) {
			return index + 1 / 2;
		}
		
		return index;
	}
	
	public static void refreshSucPageInCalendar(String pageId,int refreshNum){
		Calendar calendar = getCurrentCalendar();
		if(calendar!=null){
			synchronized (calendar) {
				int index = getIndexByPageId(calendar.pages, pageId);
				if(index != -1){
					CalendarPage page = calendar.pages.get(index);										
					page.mainRefreshSuc(refreshNum);									
					return ;
				}
			}
		}
	}

	//if change the page ,must call the function to replace old data
	public static boolean updatePageInCalendar(CalendarPage newPage,boolean isChange){
		Calendar calendar = getCurrentCalendar();
		if(calendar!=null){
			synchronized (calendar) {
				int index = getIndexByPageId(calendar.pages, newPage.id);
				if(index != -1){
					CalendarPage oldPage = calendar.pages.get(index);
					newPage.baseURI = oldPage.baseURI;					
					newPage.setPageRefresh(oldPage.getMainRefreshCount(), oldPage.getMainRefreshSucCount());
					if (isChange) {
						newPage.setPageRefresh();
					}
					calendar.pages.set(index, newPage);
					return true;
				}
			}
		}
		return false;
	}
	
	//if change the page Use adding Image or layer ,must call the function to replace old data
	public static boolean updatePageInCalendar(CalendarPage newPage,boolean isChange,int addImageNum){
		Calendar calendar = getCurrentCalendar();
		if(calendar!=null){
			synchronized (calendar) {
				int index = getIndexByPageId(calendar.pages, newPage.id);
				if(index != -1){
					CalendarPage oldPage = calendar.pages.get(index);
					newPage.baseURI = oldPage.baseURI;					
					newPage.setPageRefresh(oldPage.getMainRefreshCount(), oldPage.getMainRefreshSucCount());					
					if (isChange) {
						newPage.setPageRefresh();
					}
					if (calendar.isDuplex && addImageNum > 0 && !isDaysGridPage(newPage) && !isEditableCoverPage(newPage)) {
						newPage.setImageNum(oldPage.getImageNum());
						removeImageInCalendarPage(newPage,addImageNum);
					}															
					calendar.pages.set(index, newPage);
					return true;
				}
			}
		}
		return false;
	}
	
	public static void dealWithItem(Context context,Calendar calendar,int num){
		if (calendar == null) return;		
		if (RssTabletApp.getInstance().products == null) {
			RssTabletApp.getInstance().products = new ArrayList<ProductInfo>();
		}			
		ProductInfo dealInfo = null;														
		for(ProductInfo pInfo : RssTabletApp.getInstance().products) {
			if (pInfo != null && AppConstants.calendarType.equals(pInfo.productType)) {				
				if (pInfo.correspondId.equals(calendar.id) ) {												
					dealInfo = pInfo;
					break;												
				}
			}
		}											
		if (dealInfo != null) {
			if (num < 1) {
				RssTabletApp.getInstance().products.remove(dealInfo);
			}else {
				dealInfo.chosenImageList = calendar.chosenpics;
				setDisplayPath(dealInfo, calendar, context);
				dealInfo.num = num;
			}			
		}else if (num > 0 ){
			dealInfo = new ProductInfo();
			dealInfo.descriptionId = calendar.proDescId;
			dealInfo.num = num;
			dealInfo.productType = AppConstants.calendarType;
			dealInfo.correspondId = dealInfo.cartItemId = calendar.id;
			dealInfo.chosenImageList = calendar.chosenpics;	
			setDisplayPath(dealInfo, calendar, context);
			RssTabletApp.getInstance().products.add(dealInfo);
		}
	}
	
	private static void setDisplayPath(ProductInfo dealInfo,Calendar calendar,Context context){		
		if (dealInfo.downloadDisplayImageUrl != null) return;		
		if (calendar.pages == null) return;	
		if (calendar.pages.size() == 0) return;	
		CalendarPage page = calendar.pages.get(0);
		if (page == null) return;
		float ratio = 1f;
		if (page.width > 0 && page.height >0  ) {
			 ratio = page.height/page.width;
		}
		DisplayMetrics dm = context.getResources().getDisplayMetrics();	
		int pageWidth = (int) ((dm.widthPixels - dm.density*50)/8f);							
		int pageHeight = (int) (pageWidth*ratio);	
		String displayId = "";
		
		if (page != null ) {
			displayId = page.id;
			dealInfo.displayImageUrl = displayId;
			dealInfo.downloadDisplayImageUrl = getUrl(page, pageWidth, pageHeight).toString();
		}		
	}
	
	public static boolean isDaysGridPage(CalendarPage page) {
		return getDaysGridLayer(page) != null;
	}
	
	public static CalendarLayer getDaysGridLayer(CalendarPage page) {
		if (page != null && page.layers != null) {
			for (int i = 0; i < page.layers.length; i++) {
				if (page.layers[i].isCalendarDaysGridLayer()) {
					return page.layers[i];
				}
			}
		}
		
		return null;
	}
	
	public static boolean isDateAdded(CalendarPage page){
		if(page!=null && page.layers!=null){
			for(int i=0; i<page.layers.length; i++){
				if(page.layers[i].sublayers != null){
					Layer[] subLayers = page.layers[i].sublayers;
					for(int j=0; j<subLayers.length; j++){
						if(subLayers[j].getTextBlock() != null){
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public static CalendarDaysGridInfo getDaysGridInfo(CalendarLayer layer) {
		return new CalendarDaysGridInfo(layer);
	}
	
	public static CalendarGridItemPO getDayInfoByIndex(CalendarLayer layer, int index) {
		if (layer.isCalendarDaysGridLayer()) {
			CalendarGridItemPO item = new CalendarGridItemPO();
			CalendarDaysGridInfo info = getDaysGridInfo(layer);
			
			item.year = info.year;
			item.month = info.month;
			item.holdIndex = index;
			
			return item;
		}
		
		return null;
	}
	
	public static int getCellIndexInLayer(CalendarLayer layer){
		int cellIndex = -1;
		if (layer != null && layer.data != null) {
			for (int i = 0; i < layer.data.length; i++) {
				String name = layer.data[i].name;
				if (CalendarLayer.FLAG_DATA_CELL_INDEX.equals(name)) {
					cellIndex = (Integer) layer.data[i].value;
					break;
				} 
			}
		}	
		return cellIndex;
	}		
	
	public static CalendarLayer getSubLayerByItemPo(CalendarLayer layer,CalendarGridItemPO itemPo) {
		if (layer != null && itemPo != null && layer.sublayers != null) {
			for (CalendarLayer slayer : layer.sublayers) {
				if (slayer == null) continue;
				int cellIndex = getCellIndexInLayer(slayer);
				if (cellIndex != -1 && cellIndex == itemPo.holdIndex) {
					return slayer;
				}
			}
		}		
		return null;
	}
	
	
	public static void addImageToCalendar(ImageInfo addInfo){
		if (addInfo == null) return;
		Calendar calendar = getCurrentCalendar();	
		calendar.chosenpics.add(addInfo);	
	}
	
	public static boolean canAddContent(CalendarPage page){
		if(page.layers==null || page.layers.length<page.maxNumberOfImages){
			return true;
		}
		return false;
	}
	
	
	public static boolean isOkAddPhotosToCardFristPage(){
		boolean isOk = true;
		Calendar calendar = getCurrentCalendar();
		if (calendar == null) return false;
		if (calendar.pages == null) return false;
		if (calendar.pages.size() == 0) return false;
		synchronized (calendar) {							
			CalendarPage page = calendar.pages.get(0);	
			if (page.layers == null) return false;
			for (int j = 0; j < page.layers.length; j++) {
				CalendarLayer layer = page.layers[j];
				if (layer == null) continue;
				if (layer.type == null) continue;
				if (!Layer.TYPE_IMAGE.equals(layer.type)) continue;
				if (layer.contentId == null || (layer.contentId != null && "".equals(layer.contentId))) {						
					return false;
				}	
			}	
			if (isOk && page.isWantMainRefresh()) {
				String dispalyPath =  FilePathConstant.getLoadFilePath(FilePathConstant.calendarType, page.id, false, page.getMainRefreshCount(),page.getMainRefreshSucCount());	
				if (dispalyPath != null) {
					File downLoadedFile = new File(dispalyPath);
					if (downLoadedFile.exists()) {
						downLoadedFile.delete();
					}	
				}
			}			
		}
		return isOk;
	}
	
	public static boolean isDuplex() {	
		Calendar calendar = getCurrentCalendar();
		if (calendar == null) return false;		
		return calendar.getCalendarType() == Calendar.Monthly_Duplex;
	}
	
	public static boolean isSimplex() {	
		Calendar calendar = getCurrentCalendar();
		if (calendar == null) return false;		
		return calendar.getCalendarType() == Calendar.Monthly_Simplex;
	}

	
	public static boolean isDisplayPages() {	
		Calendar calendar = getCurrentCalendar();
		if (calendar == null) return false;		
		return calendar.getCalendarType() > 0;
	}

	public static boolean isTypeEqualLayer(CalendarLayer layer,String type) {
		boolean isImageLayer = false;
		if (layer == null) return isImageLayer;
		if (layer.type == null) return isImageLayer;
		if (type == null) return isImageLayer;
		if ("".equals(type)) return isImageLayer;
		if (type.equals(layer.type)) return true;			
		return isImageLayer;
	}
	
	public static boolean isContentNull(CalendarLayer layer) {
		boolean isNull = true;
		if (layer == null) return isNull;
		if (layer.contentId == null) return isNull;
		if ("".equals(layer.contentId)) return isNull;			
		return false;
	}
	
	public static int getHoleIndexInPage(CalendarLayer layer,CalendarLayer[] layers) {
		int holeIndex = -1;
		if (layer == null || layers == null) return holeIndex;		
		for(int i = 0; i < layers.length; i++) {
			CalendarLayer mLayer = layers[i];
			if (mLayer == null) continue;		
			if (mLayer==layer){
				holeIndex = i;
				break;
			}			
		}
		return holeIndex;
	}
	
	public static int getHoleIndexInPage(String layerId,CalendarLayer[] layers) {
		int holeIndex = -1;
		if (layerId == null || layers == null) return holeIndex;	
		if ("".equals(layerId)) return holeIndex;		
		for(int i = 0; i < layers.length; i++) {
			CalendarLayer mLayer = layers[i];
			if (mLayer == null) continue;
			if (mLayer.contentId == null) continue;		
			if (layerId.equals(mLayer.contentId)){
				holeIndex = i;
				break;
			}			
		}
		return holeIndex;
	}
	
	public static boolean isEditable(CalendarPage page){
		if (page == null) return false;			
		if (page.pageType == null) return false;
		boolean isEditable = false;
		if (CalendarPage.TYPE_STANDARD.equals(page.pageType)){
			isEditable = true;
		}else if (CalendarPage.TYPE_COVER.equals(page.pageType)){
			if (page.layers != null && page.layers.length > 0) {
				isEditable = true;
			}	
		}else if (CalendarPage.TYPE_BACK_COVER.equals(page.pageType)){
			if (page.layers != null && page.layers.length > 0) {
				isEditable = true;
			}	
		}
		return isEditable;
	}
	
	public static boolean isEditableCoverPage(CalendarPage page){
		if (page == null) return false;			
		if (page.pageType == null) return false;
		boolean isEditable = false;
		 if (CalendarPage.TYPE_COVER.equals(page.pageType)){
			if (page.layers != null && page.layers.length > 0) {
				isEditable = true;
			}	
		}
		return isEditable;
	}	
	
	public static int getFristImageLayerInPage(CalendarPage page){
		int index = -1;
		if (page == null) return index;			
		if (page.layers == null) return index;
		for (int i = 0; i < page.layers.length; i++) {
			CalendarLayer layer =  page.layers[i];
			if (layer == null) continue;
			if (layer.type == null) continue;	
			if (CalendarLayer.TYPE_IMAGE.equals(layer.type)){
				index = i;
				break;
			}
		}
		return index;
	}	
	
	public static int findFirstBlankNotFixedPage(Calendar calendar) {
		if (calendar == null || calendar.pages == null) {
			return -1;
		}
		
		for (int i = 0; i < calendar.pages.size(); i++) {
			CalendarPage page = calendar.pages.get(i);
			
			if (page == null || CalendarPage.TYPE_LAYOUT_FIXED.equals(page.layoutType) || page.maxNumberOfImages <= 0) {
				continue;
			}
			
			if (page.layers == null || page.layers.length == 0) {
				return i;
			}
			
			boolean isBlank = true;
			for (int j = 0; j < page.layers.length; j++) {
				CalendarLayer layer = page.layers[j];
				if (layer.contentId != null && !"".equals(layer.contentId)) {
					isBlank = false;
					break;
				}
			}
			
			if (isBlank) {
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * Find first index for not fixed page.
	 * All the layout in templeted page should be filled
	 * @param calendar
	 * @return
	 */
	public static int findFirstNotFilledFixedPage(Calendar calendar) {
		if (calendar == null || calendar.pages == null) {
			return -1;
		}
		
		for (int i = 0; i < calendar.pages.size(); i++) {
			CalendarPage page = calendar.pages.get(i);
			
			if (page == null || !CalendarPage.TYPE_LAYOUT_FIXED.equals(page.layoutType) || page.minNumberOfImages <= 0) {
				continue;
			}
			
			//Normally this case is not exist
			if (page.layers == null || page.layers.length < page.minNumberOfImages) {
				return i;
			}
			
			int imageCount = 0;
			for (int j = 0; j < page.layers.length; j++) {
				CalendarLayer layer = page.layers[j];
				if (layer != null && Layer.TYPE_IMAGE.equals(layer.type) && layer.contentId != null && !"".equals(layer.contentId)) {
					imageCount ++;
				}
			}
			
			if (imageCount < page.minNumberOfImages) {
				return i;
			}
		}
		
		return -1;
	}
	
	public static List<CalendarGridItemPO> getDatesInLayer(CalendarLayer layer){
		int daysInMonth = layer.getDataValue(CalendarLayer.FLAG_DATA_DAYS_IN_MONTH);
		CalendarDaysGridInfo info = getDaysGridInfo(layer);
		int year = info.year;
		int month = info.month;
		List<CalendarGridItemPO> items = new ArrayList<CalendarGridItemPO>();
		// calculate the real number of cells
		int itemNumber = layer.getDataValue(CalendarLayer.FLAG_DATA_CELL_COLUMNS) * layer.getDataValue(CalendarLayer.FLAG_DATA_CELL_ROWS);
		itemNumber -= layer.getDataValue(CalendarLayer.FLAG_DATA_FIRST_DAY_CELL_INDEX);
		if(itemNumber>daysInMonth){
			itemNumber = daysInMonth;
		}
		
		for(int i=1; i<=itemNumber; i++){
			CalendarGridItemPO item = new CalendarGridItemPO();
			item.day = i;
			item.year = year;
			item.month = month;
			item.textContent = "";
			items.add(item);
		}
		return items;
	}
	
	public static String getLayerImageInfoPath(Layer layer){
		if (layer == null) return null;
		if (layer.type == null) return null;
		if (!Layer.TYPE_IMAGE.equals(layer.type)) return null;			
		Calendar curentCalendar = getCurrentCalendar();
		ArrayList<ImageInfo> chosenpics = curentCalendar.chosenpics;				
		ImageInfo imageInfo = getLayerImageInfo(layer, chosenpics, false);		
		if (imageInfo != null) return imageInfo.editUrl;
		return null;
	}
	
	public static CalendarLayer getParentLayer(CalendarPage page, Layer layer) {
		if (page != null && page.layers != null && layer != null) {
			for (int i = 0; i < page.layers.length; i++) {
				if (page.layers[i].sublayers != null) {
					for (int j = 0; j < page.layers[i].sublayers.length; j++) {
						if (layer.equals(page.layers[i].sublayers[j])) {
							return page.layers[i];
						}
					}
				}
			}
		}
		
		return null;
	}

	public static boolean isCanAddImageForPage(CalendarPage selectedPage) {
		boolean isCan = false;
		if (selectedPage == null) return isCan;
		if (selectedPage.id == null) return isCan;		
		synchronized(selectedPage){						
			CalendarLayer[] layers = selectedPage.layers;
			if (layers == null) return isCan;
			int imageLayerNum = 0;
			for (CalendarLayer layer : layers) {
				if (layer == null) continue;
				if (layer.type == null) continue;
				imageLayerNum += 1;
			}
			imageLayerNum += selectedPage.getImageNum();			
			if (imageLayerNum < selectedPage.maxNumberOfImages) {
				isCan = true;
			}							
		}
		return isCan;
	}
	
	public static void addImageToCalendarPage(CalendarPage selectedPage){
		if (selectedPage == null) return;
		if (!CalendarPage.TYPE_STANDARD.equals(selectedPage.pageType)) return;
		selectedPage.addImageNum();
	}
	
	public static void removeImageInCalendarPage(CalendarPage selectedPage,int removeNum){
		if (selectedPage == null) return;
		if (!CalendarPage.TYPE_STANDARD.equals(selectedPage.pageType)) return;		
		selectedPage.removeImageNum(removeNum);						
	}
	
	public static boolean isDayGridCellLayer(CalendarLayer layer) {
		if (layer != null && layer.data != null) {
			for (int i = 0; i < layer.data.length; i++) {
				if (layer.data[i].name.equals(CalendarLayer.FLAG_DATA_CELL_INDEX)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
}
