package com.kodak.rss.tablet.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import android.content.Context;
import android.util.DisplayMetrics;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.n2r.bean.greetingcard.GCLayer;
import com.kodak.rss.core.n2r.bean.greetingcard.GCPage;
import com.kodak.rss.core.n2r.bean.greetingcard.GreetingCard;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.bean.text.TextBlock;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.view.GCMainView;

public class GreetingCardUtil extends ProductUtil{
	
	public static void addCurrentGreetingCard(GreetingCard card){
		if (RssTabletApp.getInstance().gCardList == null) {
			RssTabletApp.getInstance().gCardList = new ArrayList<GreetingCard>();
		}
		if (card == null) return;
		card.isCurrentChosen = true;
		if (card.chosenpics == null) {
			card.chosenpics = new ArrayList<ImageInfo>();
		}
		for (GreetingCard gcard : RssTabletApp.getInstance().gCardList) {
			if (gcard != null && gcard.isCurrentChosen) {
				gcard.isCurrentChosen = false;				
			}
		}				
		RssTabletApp.getInstance().gCardList.add(card);				
	}
	
	public static GreetingCard getCurrentGreetingCard(){
		GreetingCard currentCard = null;
		if (RssTabletApp.getInstance().gCardList == null) {
			RssTabletApp.getInstance().gCardList = new ArrayList<GreetingCard>();
		}
		for (GreetingCard card : RssTabletApp.getInstance().gCardList) {
			if (card != null && card.isCurrentChosen) {
				currentCard = card;
				if (currentCard.chosenpics == null) {
					currentCard.chosenpics = new ArrayList<ImageInfo>();
				}
				break;
			}
		}
		return currentCard;
	}
		
	/**
	 * Get the orientation of greeting card. 
	 * @param card
	 * @return true: landscape,  false: portrait. 
	 */
	public static boolean isGCLandScape(GreetingCard card) {
		if (card.theme != null && card.theme.length() > 1) {
			char orientation = card.theme.charAt(1);
			return orientation == 'L' || orientation == 'l';
		}
		
		return false;
	}
	
	/**
	 * check if the card is folded
	 * @param card
	 * @return
	 */
	public static boolean isGCFolded(GreetingCard card) {
		return card.maxNumberOfPages == 4;
	}
	
	/**
	 * get mode of card
	 * @param card
	 * @return int, see @GCMainView.MODE_xxxxx
	 */
	public static int getGCMode(GreetingCard card) {
		if (card.maxNumberOfPages == 1) {
			return isGCLandScape(card) ? GCMainView.MODE_SINGLE_LANDSCAPE : GCMainView.MODE_SINGLE_PORTRAIT;
		} else if (isGCLandScape(card)) {
			return isGCFolded(card) ? GCMainView.MODE_FOLDED_LANDSCAPE : GCMainView.MODE_DUPLEX_LANDSCAPE;
		} else {
			return isGCFolded(card) ? GCMainView.MODE_FOLDED_PORTRAIT : GCMainView.MODE_DUPLEX_PORTRAIT;
		}
	}
		
	public static URI getSampleTextURI(GCPage page, int width, int height) {
		URI pictureURI = null;
		try {
			if (page != null && !"".equals(page.id.trim())) {			
				String url = page.baseURI+page.id+"/previewSampleText?maxWidth="+width+"&maxHeight="+height;
				url= url.replaceAll(" ", "%20");
				pictureURI = new URI(url);			
			}
		} catch (URISyntaxException e) {
			pictureURI = null;
		}
		return pictureURI;
	}
	
	public static int getIndexByPageId(GreetingCard card,String id){
		if (card == null) return -1;
		if (card.pages == null) return -1;		
		for(int i=0; i < card.pages.length; i++){
			GCPage page = card.pages[i];
			if(id.equals(page.id)){
				return i;
			}
		}
		return -1;
	}

	public static boolean updatePageInCard(GCPage newPage,boolean isChange){
		GreetingCard currentCard = getCurrentGreetingCard();
		if(currentCard!=null){
			synchronized (currentCard) {
				int index = getIndexByPageId(currentCard, newPage.id);
				if(index != -1){
					GCPage oldPage= currentCard.pages[index];
					newPage.baseURI = oldPage.baseURI;
					newPage.setPageRefresh(oldPage.getMainRefreshCount(), oldPage.getMainRefreshSucCount());
					if (isChange) {
						newPage.setPageRefresh();
					}
					currentCard.pages[index] = newPage;
					return true;
				}
			}
		}
		return false;
	}
	
	public static void dealWithItem(Context context,GreetingCard card,int num){
		if (card == null) return;		
		if (RssTabletApp.getInstance().products == null) {
			RssTabletApp.getInstance().products = new ArrayList<ProductInfo>();
		}			
		ProductInfo dealInfo = null;														
		for(ProductInfo pInfo : RssTabletApp.getInstance().products) {
			if (pInfo != null && AppConstants.cardType.equals(pInfo.productType)) {				
				if (pInfo.correspondId.equals(card.id) ) {												
					dealInfo = pInfo;
					break;												
				}
			}
		}											
		if (dealInfo != null) {
			if (num < 1) {
				RssTabletApp.getInstance().products.remove(dealInfo);
			}else {
				dealInfo.chosenImageList = card.chosenpics;
				setDisplayPath(dealInfo, card, context);
				dealInfo.num = num;
			}			
		}else if (num > 0 ){
			dealInfo = new ProductInfo();
			dealInfo.descriptionId = card.proDescId;
			dealInfo.num = num;
			dealInfo.productType = AppConstants.cardType;
			dealInfo.correspondId = dealInfo.cartItemId = card.id;
			dealInfo.chosenImageList = card.chosenpics;	
			setDisplayPath(dealInfo, card, context);
			RssTabletApp.getInstance().products.add(dealInfo);
		}
	}
	
	private static void setDisplayPath(ProductInfo dealInfo,GreetingCard card,Context context){		
		if (dealInfo.downloadDisplayImageUrl != null) return;		
		if (card.pages == null) return;	
		if (card.pages.length == 0) return;	
		GCPage page = card.pages[0];
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
	
	public static void refreshSucPageInCard(String pageId,int refreshNum){
		GreetingCard currentCard = getCurrentGreetingCard();
		if(currentCard!=null){
			synchronized (currentCard) {
				int index = getIndexByPageId(currentCard, pageId);
				if(index != -1){
					GCPage page = currentCard.pages[index];										
					page.mainRefreshSuc(refreshNum);														
					return ;
				}
			}
		}
	}				
	
	public static void deleteUnselectPhoto(GreetingCard card){
		if (card == null) return;
		synchronized (card) {
			if (card.chosenpics == null) return;
			if (card.chosenpics.size() == 0) return;
			if (card.pages == null) return;		
			ArrayList saveList = new ArrayList<ImageInfo>(4);
			for (int i = 0; i < card.chosenpics.size(); i++) {
				ImageInfo iInfo = card.chosenpics.get(i);
				if (iInfo == null) continue;
				if (iInfo.imageOriginalResource == null) continue;
				for (int j = 0; j < card.pages.length; j++) {
					GCPage page = card.pages[j];
					if (page == null) continue;
					if (page.layers == null) continue;
					if (page.layers.size() == 0) continue;
					for (int k = 0; k < page.layers.size(); k++) {
						GCLayer layer = page.layers.get(k);
						if (layer == null) continue;
						if (layer.contentId == null) continue;
						if (!GCLayer.TYPE_IMAGE.equals(layer.type)) continue;
						if (layer.contentId.equals(iInfo.imageOriginalResource.id)) {
							saveList.add(iInfo);
						}		
					}				
				}				
			}
			card.chosenpics = saveList;
		}
	}	
	
	public static String getLocalImagePathByContentId(String contentId){
		if(contentId == null) {
			return null;
		}
		
		GreetingCard card = getCurrentGreetingCard();
		if(card.chosenpics!= null && card.chosenpics.size()>0){
			for(ImageInfo info : card.chosenpics){
				if(info != null && info.imageOriginalResource != null && contentId.equals(info.imageOriginalResource.id)){
					if (info.isfromNative) {
						return info.editUrl;
					}else {
						return  FilePathConstant.getLoadFilePath(FilePathConstant.externalType, info.id, true);
					}				
				}
			}
		}
		return null;
	}
	
	public static String getLayerImageCacheFileName(Layer layer){
		return ".layer_cache_"+layer.contentId+".jpg";
	}

	public static boolean isOkAddPhotosToCard(){
		boolean isOk = true;
		GreetingCard card = getCurrentGreetingCard();
		if (card == null) return false;
		if (card.pages == null) return false;
		synchronized (card) {				
			for (int i = 0; i < card.pages.length; i++) {
				GCPage page = card.pages[i];
				if (page == null) continue;
				if (page.layers == null) continue;
				for (int j = 0; j < page.layers.size(); j++) {
					GCLayer layer = page.layers.get(j);
					if (layer == null) continue;
					if (layer.type == null) continue;
					if (!Layer.TYPE_IMAGE.equals(layer.type)) continue;
					if (layer.contentId == null || (layer.contentId != null && "".equals(layer.contentId))) {						
						return false;
					}	
				}
			}
		}
		return isOk;
	}
	
	public static boolean isOkAddPhotosToCardFristPage(){
		boolean isOk = true;
		GreetingCard card = getCurrentGreetingCard();
		if (card == null) return false;
		if (card.pages == null) return false;
		if (card.pages.length == 0) return false;
		synchronized (card) {							
			GCPage page = card.pages[0];				
			if (page.layers == null) return false;
			for (int j = 0; j < page.layers.size(); j++) {
				GCLayer layer = page.layers.get(j);
				if (layer == null) continue;
				if (layer.type == null) continue;
				if (!Layer.TYPE_IMAGE.equals(layer.type)) continue;
				if (layer.contentId == null || (layer.contentId != null && "".equals(layer.contentId))) {						
					return false;
				}	
			}	
			if (isOk && page.isWantMainRefresh()) {
				String dispalyPath =  FilePathConstant.getLoadFilePath(FilePathConstant.cardType, page.id, true,page.getMainRefreshCount(),page.getMainRefreshSucCount());	
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
	
	public static boolean isHaveNullTextLayer(GCPage page){
		boolean isHave = false;
		if (page == null) return isHave;	
		if (page.layers == null) return isHave;
		synchronized (page) {										
			for (int j = 0; j < page.layers.size(); j++) {
				GCLayer layer = page.layers.get(j);
				if (layer == null) continue;
				if (layer.type == null) continue;
				if (Layer.TYPE_TEXT_BLOCK.equals(layer.type)) {
					isHave = true;
					TextBlock tBlock = layer.getTextBlock(); 
					if (tBlock != null && tBlock.text != null && !"".equals(tBlock.text)) {
						isHave = false;
					}
					if (isHave) break;
				}
			}
		}
		return isHave;
	}	
	
	public static ImageInfo getImageInfoIncardUseLayerId(GreetingCard card, String layerId){
		ImageInfo info = null;
		if (layerId == null) return info;		
		if ("".equals(layerId)) return info;
		if (card == null) return info;
		if (card.chosenpics == null) return info;
		for(int i = 0; i < card.chosenpics.size(); i++){
			ImageInfo imageInfo = card.chosenpics.get(i);
			if (imageInfo == null) continue;
			if (imageInfo.imageOriginalResource == null) continue;
			if (imageInfo.imageOriginalResource.id == null) continue;			
			if (imageInfo.imageOriginalResource.id.equals(layerId)) {
				info = imageInfo;
				return info;
			}			
		}
		return info;
	}
	
	public static void addImageToCard(ImageInfo addInfo){
		if (addInfo == null) return;
		GreetingCard card = getCurrentGreetingCard();		
		card.chosenpics.add(addInfo);	
	}

	public static GCLayer getLayerUseId(String iORId){
		GCLayer layer = null;
		if (iORId == null) return layer;
		GreetingCard card = getCurrentGreetingCard();
		if (card == null)  return layer;
		if (card.pages == null)  return layer;			
		for (int i = 0; i < card.pages.length; i++) {
			GCPage gcPage = card.pages[i];
			if (gcPage == null) continue;
			if (gcPage.layers == null) continue;			
			for (int j = 0; j < gcPage.layers.size(); j++) {
				GCLayer gcLayer = gcPage.layers.get(j);
				if (gcLayer == null) continue;
				if (gcLayer.type == null) continue;
				if (!GCLayer.TYPE_IMAGE.equals(gcLayer.type)) continue;
				if (gcLayer.contentId == null) continue;				
				if (gcLayer.contentId.equals(iORId)) {
					layer = gcLayer;
					break;
				}				
			}
			if (layer != null) {
				return layer;
			}
		}
		return layer;
	}
	
}
