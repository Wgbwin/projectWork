package com.kodak.kodak_kioskconnect_n2r.greetingcard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.AppContext;
import com.kodak.kodak_kioskconnect_n2r.ImageInfo;
import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.kodak_kioskconnect_n2r.PrintProduct;
import com.kodak.kodak_kioskconnect_n2r.ROI;
import com.kodak.kodak_kioskconnect_n2r.bean.DeleveryPrompt;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.utils.ImageUtil;

public class GreetingCardManager{
	private static final String TAG = GreetingCardManager.class.getSimpleName();

	private Context context;
	private GreetingCardWebService service;
	//private static GreetingCardManager manager;
	private HashMap<Integer, Integer> albumMap4Click = new HashMap<Integer, Integer>();
	private List<GreetingCardTheme> themes;
	private List<GreetingCard> cards;
	private List<GreetingCardCatalogData> data;
	private int naviHeight = 0;
	private int layoutWidth = 0;
	private int layoutHeight = 0;
	private final int EDITFLAG = 16;
	private final int PREVIEWFLAG = 17;

//	private String desIds;
	public int getNaviHeight() {
		return naviHeight;
	}

	public void setNaviHeight(int naviHeight) {
		this.naviHeight = naviHeight;
	}

	public int getLayoutWidth() {
		return layoutWidth;
	}

	public void setLayoutWidth(int layoutWidth) {
		this.layoutWidth = layoutWidth;
	}

	public int getLayoutHeight() {
		return layoutHeight;
	}

	public void setLayoutHeight(int layoutHeight) {
		this.layoutHeight = layoutHeight;
	}

	public HashMap<Integer, Integer> getAlbumMap4Click() {
		return albumMap4Click;
	}

	public void setAlbumMap4Click(HashMap<Integer, Integer> albumMap4Click) {
		this.albumMap4Click = albumMap4Click;
	}

	private GreetingCardProduct cardProduct;

	private GreetingCardPage editPage;
	private GreetingCardPageLayer editLayer;
	private int editPageIndex;

	public int getEditPageIndex() {
		return editPageIndex;
	}

	public void setEditPageIndex(int editPageIndex) {
		this.editPageIndex = editPageIndex;
	}

	public GreetingCardManager(Context context) {
		this.context = context.getApplicationContext();
		service = new GreetingCardWebService(this.context, "");
	}

	public static GreetingCardManager getGreetingCardManager(Context context) {
		String id = PrintHelper.GreetingCardProductID;
		GreetingCardManager manager = null;
		List<GreetingCardManager> mGreetingCardManagers = AppContext.getApplication().getmGreetingCardManagers();
		if (!id.equals("")){
			for (GreetingCardManager tempProduct : mGreetingCardManagers){
				if (tempProduct.cardProduct ==null){
					continue;
				}
				if (tempProduct.cardProduct.id.equals(id)){
					manager = tempProduct;
				}
			}
		}else {
			if (mGreetingCardManagers.size()>0){
				manager = mGreetingCardManagers.get(mGreetingCardManagers.size()-1);
			}
		}	
		return manager;
	}
	/**
	 * <p>
	 * This function must be used in Thread.
	 * 
	 * @return
	 */
	public boolean createGreetingCardThemes(String desIds) {
		boolean succeed = false;
		GreetingCardTheme[] arrThemes = null;
		int count = 0;
		try {
			while (arrThemes == null && count < 5) {
				arrThemes = service.getGreetingCardThemes(desIds);
				count++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (arrThemes != null && arrThemes.length > 0) {
			if (themes == null) {
				themes = new ArrayList<GreetingCardTheme>();
			} else {
				themes.clear();
			}
			for (GreetingCardTheme theme : arrThemes) {
				themes.add(theme);
			}
			succeed = true;
		}
		return succeed;
	}

	public String getDesIds() {
//		if (TextUtils.isEmpty(desIds)) {
		String desIds ="";
			if (PrintHelper.products != null) {
				StringBuffer sb = new StringBuffer() ;
				for (int i = 0; i < PrintHelper.products.size(); i++) {
					PrintProduct pro = PrintHelper.products.get(i);
					if ("Greeting Cards".equals(pro.getType())
							|| "DuplexMyGreeting".equals(pro.getType())) {
						if (i != PrintHelper.products.size() - 1) {
							sb.append(pro.getId()).append(",");
							
						} else {
							sb.append(pro.getId());
						}
					}
				}
				
				desIds = sb.toString() ;
				
			}

//		}
        Log.v(TAG, "desIds: "+desIds);
		return desIds;

	}

	/**
	 * 
	 * @return 1.If return null means there must have some error when getting
	 *         Themes from server. <br>
	 *         2.If return a list which size is 0 means the calling have
	 *         response, but no themes got or something wrong occurred when
	 *         parse the response. <br>
	 *         3.If return a list which size>0 means getting themes
	 *         successfully.
	 */
	public List<GreetingCardTheme> getGreetingCardThemes() {
		return themes;
	}

	public boolean hasValidGreetingCardThemes() {
		if (themes != null && themes.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This function must be used in Thread.
	 * 
	 * @param language
	 *            language of theme
	 * @param filters
	 *            filters of theme
	 * @return
	 */
	public boolean createGreetingCardCategory(String language, String filters) {
		boolean succeed = false;
		GreetingCard[] arrCards = null;
		int count = 0;
		try {
			while (arrCards == null && count < 5) {
				arrCards = service.getGreetingCardCategory(language, filters);
				count++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (arrCards != null && arrCards.length > 0) {
			if (cards == null) {
				cards = new ArrayList<GreetingCard>();
			} else {
				cards.clear();
			}
			// Hard code here for filter the 2-side flat card
			final String des = "MyGreetingFlatDuplex";
			for (GreetingCard card : arrCards) {
				/*boolean needFilter = false;
				if (card.productIdentifiers != null) {
					for (String identifier : card.productIdentifiers) {
						if (identifier.contains(des)) {
							needFilter = true;
						}
					}
				}
				if (!needFilter) {*/
					cards.add(card);
				//}
			}
			succeed = true;
		}
		return succeed;
	}

	public List<GreetingCard> getContentForDesigns(String usage, String cardId,
			String descIds) {
		List<GreetingCard> greetingCards = null;
		GreetingCard[] cards = null;
		int count = 0;
		while (cards == null && count < 5) {
			cards = service.getContentForDesigns(usage, cardId, descIds);
			count++;
		}
		if (cards != null && cards.length > 0) {
			greetingCards = new ArrayList<GreetingCard>();
			for (GreetingCard greetingCard : cards) {
				greetingCards.add(greetingCard);
			}

		}
		return greetingCards;
	}

	/**
	 * 
	 * @return
	 */
	public List<GreetingCard> getGreetingCardCategory() {
		return cards;
	}

	public boolean hasValidGreetingCardCategory() {
		if (cards != null && cards.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This function must be used in Thread.
	 * 
	 * @param productType
	 *            right now, this param is not useful. It has been hard coded as
	 *            "DuplexMyGreeting,Greeting%20Cards"
	 * @param language
	 *            right now, use "en_us" as default. Maybe change it in the
	 *            future
	 * @return if create successfully return true.
	 */
	public boolean createGreetingCardCatalogData(String productType) {
		boolean succeed = false;
		GreetingCardCatalogData[] _data = null;
		int count = 0;
		try {
			while (_data == null && count < 5) {
				_data = service.getGreetingCardCatalogData(productType);
				count++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (_data != null && _data.length > 0) {
			if (data == null) {
				data = new ArrayList<GreetingCardCatalogData>();
			} else {
				data.clear();
			}
			for (GreetingCardCatalogData catalogData : _data) {
				data.add(catalogData);
			}
			succeed = true;
		}
		return succeed;
	}

	/**
	 * 
	 * @return
	 */
	public List<GreetingCardCatalogData> getGreetingCardCatalogData() {
		return data;
	}

	public boolean hasValidGreetingCardCatalogData() {
		if (data != null && data.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 
	 * @param card
	 *            the choosing card
	 * @return the card's description of marketing of which format is HTML. If
	 *         return "" means there is no marketing detail match the card
	 */
//	public String getGreetingCardMaketing(GreetingCard card) {
//		if (card.productIdentifiers != null && data != null) {
//			for (String identifier : card.productIdentifiers) {
//				for (GreetingCardCatalogData _data : data) {
//					GreetingCardCatalogDataEntry entry = _data
//							.getEntry(identifier);
//					if (entry != null) {
//						String result = entry
//								.getMarketing(GreetingCardCatalogDataEntry.TYPE_MARKETING);
//						if (!result.equals("")) {
//							if (result.contains("%@")) {
//								result = result.replaceFirst("%@",
//										card.localizedName);
//							}
//							if (result.contains("%@")) {
//								result = result.replace("%@",
//										entry.maxUnitPrice.priceStr);
//							}
//							return result;
//						}
//					}
//				}
//			}
//		}
//		return "";
//	}
	
	
	
	
	

	/**
	 * @param identifier
	 *            the card identifier
	 * @param card
	 *            the GreetingCard
	 * 
	 * @return the card's description of marketing of which format is HTML. If
	 *         return "" means there is no marketing detail match the card
	 */
	public String getGreetingCardMaketing(String identifier, GreetingCard card) {
		if (identifier != null && data != null && card != null) {
			for (GreetingCardCatalogData _data : data) {
				GreetingCardCatalogDataEntry entry = _data.getEntry(identifier);
				if (entry != null) {
					String result = entry
							.getMarketing(GreetingCardCatalogDataEntry.TYPE_MARKETING);
					if (!"".equals(result)) {
						if (result.contains("%@")) {
							result = result.replaceFirst("%@",
									card.localizedName);
						}
						if (result.contains("%@")) {
							result = result.replace("%@",
									entry.maxUnitPrice.priceStr);
						}
						return result;
					}
				}
			}
		}
		return "";
	}
	
	
    public String getGreetingCardProductDeliveryPrompt(String identifier, GreetingCard card){
		if (identifier != null && data != null && card != null) {
			for (GreetingCardCatalogData _data : data) {
				GreetingCardCatalogDataEntry entry = _data.getEntry(identifier);
				if (entry != null) {
					String result = entry
							.getMarketing(GreetingCardCatalogDataEntry.TYPE_PRODUCTDELIVERYPROMPT);
					if (!"".equals(result)) {
						return result ;
						
					}
				}
			}
		}
		return "";
	}
    
    
    public List<DeleveryPrompt> getGreetingCardProductDeliveryPromptList(GreetingCard card){
    	List<DeleveryPrompt> deleveryPromptList =null;
		if ( data != null && card != null) {
			
			String[] ids = card.productIdentifiers ;
			for (String identifier : ids) {
				for (GreetingCardCatalogData _data : data) {
					GreetingCardCatalogDataEntry entry = _data.getEntry(identifier);
					if (entry != null) {
						String result = entry
								.getMarketing(GreetingCardCatalogDataEntry.TYPE_PRODUCTDELIVERYPROMPT);
						if (!"".equals(result)) {
							if(deleveryPromptList == null){
								deleveryPromptList = new ArrayList<DeleveryPrompt>() ;
							}
							DeleveryPrompt deleveryPrompt = new DeleveryPrompt(identifier, result );
							
							
							deleveryPromptList.add(deleveryPrompt);
							break ;
							
						}
					}
				}
			}
			
		}
		return deleveryPromptList;
	}
	
	

	public String getGreetingCardProductShortName(String productionId) {
		String name = "";
		boolean found = false;
		if (data != null) {
			for (GreetingCardCatalogData _data : data) {
				if (_data.entries == null) {
					continue;
				}
				for (GreetingCardCatalogDataEntry entry : _data.entries) {
					if (entry.description.id.equals(productionId)) {
						name = entry.description.shortName;
						found = true;
						break;
					}
				}
				if (found) {
					break;
				}
			}
		}
		return name;
	}

	public String getGreetingCardProductName(String productionId) {
		String name = "";
		boolean found = false;
		if (data != null) {
			for (GreetingCardCatalogData _data : data) {
				if (_data.entries == null) {
					continue;
				}
				for (GreetingCardCatalogDataEntry entry : _data.entries) {
					if (entry.description.id.equals(productionId)) {
						name = entry.description.name;
						found = true;
						break;
					}
				}
				if (found) {
					break;
				}
			}
		}
		return name;
	}

	/**
	 * This function must be used in Thread.
	 * 
	 * @param language
	 *            the language of the card
	 * @param contentId
	 *            the content id of the card
	 * @return
	 */
	public boolean createGreetingCard(String language, String contentId,
			String productIdentifier) {
		boolean succeed = false;
		cardProduct = null;
		try {
			int count = 0;
			while (cardProduct == null && count < 5) {
				cardProduct = service.createGreetingCard(language, contentId,
						productIdentifier);
				count++;
			}
			if (cardProduct != null) {
				for (GreetingCardPage page : cardProduct.pages) {
					/*cardProduct.putPagePreview(page.id, null, EDITFLAG);
					cardProduct.putPagePreview(page.id, null, PREVIEWFLAG);*/
					cardProduct.putPagePreviewByPath(page.id, null, EDITFLAG);
					cardProduct.putPagePreviewByPath(page.id, null, PREVIEWFLAG);
				}
				succeed = true;
				// Log.e(TAG, cardProduct.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return succeed;
	}

	public GreetingCardProduct getGreetingCardProduct() {
		return cardProduct;
	}

	/**
	 * This function must be used in Thread.
	 * 
	 * @param filename
	 * @param uri
	 * @param isSecondUpload
	 * @return
	 */
	public String uploadPicture(PhotoInfo photo) {
		
		String result = "Error";
		int count = 0;
		if (photo.getPhotoSource().isFromPhone()){
			while (result.equals("Error") && count < 5) {
				result = service.UploadPicture(context, photo.getPhotoPath(), photo.getLocalUri(),true,photo);
				count++;
			}
		}else {
			while (count < 5 && ("Error").equals(result)) {	
				result = service.addImageFromWebTask(photo);
				count++;
			}
		}
		
		if (result.equals("Error")) {
			return "";
		}
		return result;
	}

	public GreetingCardProduct getGreetingCardProductCardProduct() {
		return cardProduct;
	}

	public GreetingCardPage getEditPage() {
		return editPage;
	}

	/**
	 * 
	 * @param sequenceNumber
	 *            sequenceNumber is start from 1.
	 */
	public GreetingCardPage setEditPage(int sequenceNumber) {
		if (sequenceNumber <= cardProduct.pages.length) {
			try {
				editPage = (GreetingCardPage) cardProduct.pages[sequenceNumber - 1]
						.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		} else {
			try {
				Log.e(TAG,
						"Please set a correct sequenceNumber, its range is ["
								+ 1 + "-" + cardProduct.pages.length + "]");
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
		return editPage;
	}

	public GreetingCardPageLayer getEditLayer() {
		return editLayer;
	}

	/**
	 * 
	 * @param layer
	 *            if the param is null means edit the layer successfully.
	 */
	public GreetingCardPageLayer setEditLayer(String layerContentId,
			int holeIndex) {
		if (layerContentId == null || layerContentId.equals("")) {
			for (GreetingCardPageLayer layer : editPage.layers) {
				/*
				 * if(layer.type.equals(GreetingCardPageLayer.TYPE_IMAGE)){ try
				 * { editLayer = (GreetingCardPageLayer) layer.clone(); } catch
				 * (CloneNotSupportedException e) { e.printStackTrace(); } }
				 */
				if (layer.type.equals(GreetingCardPageLayer.TYPE_IMAGE)
						&& layer.holeIndex == holeIndex) {
					try {
						editLayer = (GreetingCardPageLayer) layer.clone();
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		if (!layerContentId.equals("") && editPage != null
				&& editPage.layers != null) {
			for (int i = 0; i < editPage.layers.length; i++) {
				if (editPage.layers[i].contentId.equals(layerContentId)) {
					try {
						editLayer = (GreetingCardPageLayer) editPage.layers[i]
								.clone();
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}
		return editLayer;
	}

	// public boolean createPagePreviewSampleText(String pageId, int width, int
	// height){
	// boolean succeed = false;
	// Bitmap bmp = null;
	// int count = 0;
	// while(bmp==null && count<5){
	// bmp = service.previewSampleTextCardPage(pageId, width, height);
	// count ++;

	// if(null != cardProduct){
	// if(bmp!=null){
	// succeed = true;
	// cardProduct.putPagePreviewSampleText(pageId, bmp);
	// }else{
	// cardProduct.putPagePreviewSampleText(pageId, bmp);
	// }
	// }
	// return succeed;

	// }

	public boolean createPagePreview(String pageId, int width, int height,
			int flag) {
		Log.i(TAG, "!!!!!!!!!! createPagePreview flag = " + flag);
		boolean succeed = false;
		Bitmap bmp = null;
		String priviewFilePath = null;
		int count = 0;
		while (bmp == null && count < 5) {
			if (flag == EDITFLAG) {
				bmp = service.previewSampleTextCardPage(pageId, width, height);
			} else if (flag == PREVIEWFLAG) {
				bmp = service.previewCardPage(pageId, width, height);
			}
			count++;
		}

		if (null != cardProduct) {
			if (bmp != null) {
				succeed = true;
				String tempFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + PrintHelper.TEMP_FOLDER+  "/";
				File folder = new File(tempFolder);
				if (!folder.exists()) {
					folder.mkdirs();
				}
				priviewFilePath = tempFolder +pageId + flag + ".temp";
				ImageUtil.saveBitmapToLocal(bmp,priviewFilePath);
			}
			//cardProduct.putPagePreview(pageId, bmp, flag);
			cardProduct.putPagePreviewByPath(pageId, priviewFilePath, flag);
		}
		return succeed;

	}

	public Bitmap getPagePreview(String pageId, int flag) {
//		return cardProduct.getPagePreview(pageId, flag);
		return cardProduct.getPagePreviewByPath(pageId, flag);
	}

	/**
	 * added by robin.qian check if the page is loaded
	 * 
	 * @param pageId
	 * @return
	 */
	public boolean isLoaded(String pageId, int flag) {
//		return cardProduct != null
//				&& cardProduct.getPagePreview(pageId, flag) != null;
		return cardProduct != null
				&& cardProduct.getPagePreviewByPath(pageId, flag) != null;
	}

	/**
	 * This function must be used in Thread. This function maybe need to add a
	 * param which should be imageURI
	 * 
	 * @param holeIndex
	 * @param imageContentId
	 *            imageContentId is the id which return from server when upload
	 *            image successfully
	 * @return
	 */
	public boolean addImageToCardTask(int holeIndex, String imageContentId) {
		boolean succeed = false;
		GreetingCardPage page = null;
		int count = 0;
		while (page == null && count < 5) {
			page = service.addImageToCard(editPage.id, holeIndex,
					imageContentId);
			count++;
		}
		if (page != null) {
			editLayer.contentId = imageContentId;
			// editPage.imageContentId = imageContentId;
			// TODO: Maybe here need something to do with image contentId.
			copyLayer2Page();
			succeed = true;
		}
		return succeed;
	}

	public boolean setImageCropTask(String imageContentId, ROI roi) {
		boolean succeed = false;
		succeed = service.setImageCrop(imageContentId, roi);
		if (succeed) {
			copyLayer2Page();
		}
		return succeed;
	}

	/**
	 * This function must be used in Thread.
	 * 
	 * @param imageContentId
	 * 
	 * @return
	 */
	public boolean deleteImageTask(String imageContentId, boolean needRemoveURI) {
		boolean succeed = false;
		int count = 0;
		GreetingCardPage page = null;
		while (page == null && count < 5) {
			page = service.deleteImageFromCard(editPage.id, imageContentId);
			count++;
		}
		if (page != null) {
			editLayer.clearEditInfo();
			editLayer.contentId = "";
			if (needRemoveURI) {
				PrintHelper.selectedHash.put(editLayer.getPhotoInfo().getLocalUri(), "0");
				editLayer.getPhotoInfo().setLocalUri("") ;
			}
			copyLayer2Page();
			succeed = true;
		}

		return succeed;
	}

	/**
	 * This function must be used in Thread.
	 * 
	 * @param newImageContentId
	 *            the latest uploaded image id return from server
	 * @param holeIndex
	 * 
	 * @return
	 * @see {@link #deleteImageTask(String)} and
	 *      {@link #addImageToCardTask(String, String)}
	 */
	public boolean replaceImageTask(String lastImageContentId,
			String newImageContentId, int holeIndex) {
		boolean succeed = false;
		if (editLayer.contentId != null && !editLayer.contentId.equals("")) {
			succeed = deleteImageTask(lastImageContentId, false);
			if (!succeed) {
				return false;
			} else {
				editLayer.clearEditInfo();
			}
		}
		succeed = addImageToCardTask(holeIndex, newImageContentId);
		return succeed;
	}

	/**
	 * This function must be used in Thread.
	 * 
	 * @param layerContentId
	 * @param text
	 * @return
	 */
	public boolean setTextBlockTask(String layerContentId, String text) {
		boolean succeedForText = false;
		boolean succeedForPage = false;
		int count = 0;
		while (!succeedForText && count < 5) {
			succeedForText = service
					.setTextBlockAndLayout(layerContentId, text);
			count++;
		}
		if (succeedForText) {
			editLayer.textValue = text;
			copyLayer2Page();
		}
		count = 0;
		while (!succeedForPage && count < 5) {
			GreetingCardPage page = service.layoutCardPage(editPage.id, "");
			if (page != null) {
				int i = 0;
				for (GreetingCardPageLayer layer : page.layers) {
					if (layer.contentId.equalsIgnoreCase(editLayer.contentId)) {
						page.layers[i].setEditedBefore(editLayer
								.isEditedBefore());
						page.layers[i].setTextInputVlaue(editLayer
								.getTextInputVlaue());
						page.layers[i].setTextInputDefaultValue(editLayer
								.getTextInputDefaultValue());
					}
					i++;
				}
				copyPage2Product(page);
				succeedForPage = true;
			}
			count++;
		}

		return succeedForText && succeedForPage;
	}

	/**
	 * This function must be used in Thread.
	 * 
	 * @param degree
	 * @return
	 */
	public boolean rotateImageTask(int degree, boolean needChangeDegree) {
		boolean succeed = false;
		int count = 0;
		while (!succeed && count < 5) {
			succeed = service.rotateImage(editLayer.contentId, -degree);
			count++;
		}
		if (succeed) {
			if (needChangeDegree) {
				editLayer.degree = Math.abs((editLayer.degree - degree)) % 360;
				copyLayer2Page();
			}
		}
		return succeed;
	}

	/**
	 * 
	 * @param text
	 * @param sticky
	 * @return
	 * @see {@link #setTextBlockTask(String, String)}
	 */
	public boolean setTextAndLayoutTask(String text, String sticky) {
		boolean succeed = false;
		succeed = setTextBlockTask(editLayer.contentId, text);
		if (!succeed) {
			return false;
		}

		GreetingCardPage page = null;
		int count = 0;
		succeed = false;
		while (page == null && count < 5) {
			page = service.layoutCardPage(editPage.id, sticky);
			count++;
		}
		if (page != null) {
			copyPage2Product(page);
			succeed = true;
		}
		return succeed;
	}

	/**
	 * 
	 * @param imageContentId
	 * @return
	 */
	public ImageInfo getImageInfo(String imageContentId) {
		ImageInfo imageInfo = null;
		int count = 0;
		while (imageInfo == null && count < 5) {
			imageInfo = service.getImageInfoObject(imageContentId);
			count++;
		}
		return imageInfo;
	}

	private void copyLayer2Page() {
		boolean succeed = false;
		if (editPage != null && editLayer != null) {
			int layerIndex = -1;
			if (editLayer.type.equals(GreetingCardPageLayer.TYPE_IMAGE)) {
				for (int i = 0; i < editPage.layers.length; i++) {
					if (editPage.layers[i].holeIndex == editLayer.holeIndex) {
						layerIndex = i;
						break;
					}
				}
			} else {
				for (int i = 0; i < editPage.layers.length; i++) {
					if (editPage.layers[i].contentId
							.equals(editLayer.contentId)) {
						layerIndex = i;
						break;
					}
				}
			}
			if (layerIndex != -1) {
				try {
					editPage.layers[layerIndex] = (GreetingCardPageLayer) editLayer
							.clone();
					succeed = true;
					copyPage2Product(editPage);
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
		}
		if (!succeed) {
			Log.e(TAG, "copyLayer2Page failed. Please pay more attention.");
		}
	}

	private void copyPage2Product(GreetingCardPage page) {
		boolean succeed = false;
		if (page != null && cardProduct != null) {
			for (int i = 0; i < cardProduct.pages.length; i++) {
				if (cardProduct.pages[i].id.equals(page.id)) {
					try {
						if (editPage != page) {
							for (int j = 0; j < cardProduct.pages[i].layers.length; j++) {
								page.layers[j]
										.copyLayerInfo(cardProduct.pages[i].layers[j]);
							}
						}
						cardProduct.pages[i] = (GreetingCardPage) page.clone();
						editPage = (GreetingCardPage) page.clone();
						if (editLayer != null
								&& !editLayer.contentId.equals("")) {
							editLayer = (GreetingCardPageLayer) editPage
									.getLayer(editLayer.contentId).clone();
						}
						succeed = true;
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}
		if (!succeed) {
			Log.e(TAG, "copyPage2Product failed. Please pay more attention.");
		}
	}

}
