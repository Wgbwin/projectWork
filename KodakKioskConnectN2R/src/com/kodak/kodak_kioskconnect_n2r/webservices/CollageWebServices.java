package com.kodak.kodak_kioskconnect_n2r.webservices;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.kodak.kodak_kioskconnect_n2r.PrintMakerWebService;
import com.kodak.kodak_kioskconnect_n2r.bean.CollageParse;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.Collage;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.CollagePage;
import com.kodak.kodak_kioskconnect_n2r.bean.text.Font;
import com.kodak.kodak_kioskconnect_n2r.bean.text.TextBlock;

public class CollageWebServices extends PrintMakerWebService {

	private CollageParse parse;

	public CollageWebServices(Context c, String serviceName) {
		super(c, serviceName);
		parse = new CollageParse();
	}

	// The same as iOS task: ChangeZOrderCollageImageTask
	public CollagePage moveToTopTask(String pageId, String contentId) {
		String url = mCollageURL + "collages/page/" + pageId + "/moveToTop?contentId=" + contentId + "&Alternates=true";
		CollagePage page = null;
		int count = 0;
		while (page == null && count < connTryTimes) {
			String result = httpPutTask(url, "", "moveToTopTask");
			page = parse.parseCollagePage(result);
			count++;
		}
		return page;
	}

	/**
	 * Create Collage
	 * 
	 * @param proDesId
	 * @return
	 */
	public Collage createCollageTask(String proDesId) {
		String themeId = "";
		String backgroundId = "";
		boolean isPortrait = false;
		String url = mCollageURL + "collages?productId=" + proDesId + "&themeId=" + themeId + "&backgroundId=" + backgroundId + "&isPortrait="
				+ isPortrait;
		Collage collage = null;
		int count = 0;
		while (collage == null && count < connTryTimes) {
			String result = httpPostTask(url, "", "createCollageTask");
			collage = parse.parseCollage(result);
			count++;
		}
		return collage;
	}

	public Collage getCollageTask(String collageId) {
		String url = mCollageURL + "collages/" + collageId + "?alternates=true";
		Collage collage = null;
		int count = 0;
		while (collage == null && count < connTryTimes) {
			String result = httpGetTask(url, "getCollageTask");
			collage = parse.parseCollage(result);
			count++;
		}
		return collage;
	}
	
	/* -------------------- CollageTextEdit -------------------- */
	public List<Font> getAvailableFontsTask(String language) {
		int count = 0;
		String url = mTextBlockURL + "/textblocks/fonts?language=" + language;
		List<Font> fonts = null;
		while (fonts == null && count < connTryTimes) {
				String result = httpGetTask(url, "getAvailableFontsTask");
				fonts = parse.parseFonts(result);
				count++;
		}
		return fonts;
	}

	public CollagePage insertContentTask(String pageId, List<String> contents) {
		String url = mCollageURL + "collages/page/" + pageId + "/insert-content?alternates=true";
		JSONArray jsPostData = new JSONArray();
		for (String contentId : contents) {
			JSONObject jsContent = new JSONObject();
			try {
				jsContent.put("ContentId", contentId);
				jsContent.put("LayerIndex", 0);
				jsPostData.put(jsContent);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		String postData = jsPostData.toString();
		CollagePage page = null;
		int count = 0;
		while (page == null && count < connTryTimes) {
			String result = httpPostTask(url, postData, "insertContentTask");
			page = parse.parseCollagePage(result);
			count++;
		}
		return page;
	}

	public CollagePage setCollagePageLayoutTask(String pageId, String layoutId, boolean alternates) {
		String url = "";
		if (!layoutId.equals("")) {
			url = mCollageURL + "collages/page/" + pageId + "/set-layout?layoutTitle=" + layoutId + "&alternates=" + alternates;
		} else {
			url = mCollageURL + "collages/page/" + pageId + "/layout?alternates=" + alternates;
		}
		CollagePage page = null;
		int count = 0;
		while (page == null && count < connTryTimes) {
			String result = httpPutTask(url, "", "setCollagePageLayoutTask");
			page = parse.parseCollagePage(result);
			count++;
		}
		return page;

	}

	public CollagePage removeCollageContentTask(String pageId, String contentId) {
		String url = mCollageURL + "collages/page/" + pageId + "/remove-content?alternates=true";
		JSONArray jsPostData = new JSONArray();
		jsPostData.put(contentId);
		String postData = jsPostData.toString();
		CollagePage page = null;
		int count = 0;
		while (page == null && count < connTryTimes) {
			String result = httpPostTask(url, postData, "removeCollageContentTask");
			page = parse.parseCollagePage(result);
			count++;
		}
		return page;
	}

	public Collage rotateCollageTask(String collageId, boolean isPortrait) {
		String url = mCollageURL+"collages/" + collageId + "/orientation?isPortrait=" + isPortrait + "&alternates=true";
		Collage collage = null;
		int count = 0;
		while (collage == null && count < connTryTimes) {
			String result = httpPutTask(url, "", "rotateCollageTask");
			collage = parse.parseCollage(result);
			count++;
		}
		return collage;
	}

	public CollagePage rotateCollageContentTask(String pageId, String contentId, int angle) {
		String url = mCollageURL + "collages/page/" + pageId + "/rotate-content?contentId=" + contentId + "&angle=" + angle + "&alternates=true";
		CollagePage page = null;
		int count = 0;
		while (page == null && count < connTryTimes) {
			String result = httpPostTask(url, "", "rotateCollageContentTask");
			page = parse.parseCollagePage(result);
			count++;
		}
		return page;
	}

	public CollagePage setCollageBackgroundImageTask(String pageId, String imageId, float opacity) {
		String url = mCollageURL + "collages/page/" + pageId + "/background-image?imageId=" + imageId + "&opacity=" + opacity;
		CollagePage page = null;
		int count = 0;
		while (page == null && count < connTryTimes) {
			String result = httpPutTask(url, "", "setCollageBackgroundImageTask");
			page = parse.parseCollagePage(result);
			count++;
		}
		return page;
	}

	public Collage setCollageThemeTask(String collageId, String themeId) {
		String url = mCollageURL + "collages/" + collageId + "/theme?themeId=" + themeId + "&alternates=true";
		Collage collage = null;
		int count = 0;
		while (collage == null && count < connTryTimes) {
			String result = httpPutTask(url, "", "setCollageThemeTask");
			collage = parse.parseCollage(result);
			count++;
		}
		return collage;
	}

	public CollagePage shuffleCollageTask(String pageId) {
		String url = mCollageURL + "collages/page/" + pageId + "/shuffle-content?alternates=true";
		CollagePage page = null;
		int count = 0;
		while (page == null && count < connTryTimes) {
			String result = httpPostTask(url, "", "shuffleCollageTask");
			page = parse.parseCollagePage(result);
			count++;
		}
		return page;
	}

	public CollagePage swapCollageContentTask(String pageId, String contentId1, String contentId2) {
		String url = mCollageURL + "collages/page/" + pageId + "/swap-content?contentId1=" + contentId1 + "&contentId2=" + contentId2 + "&alternates=true";
		CollagePage page = null;
		int count = 0;
		while (page == null && count < connTryTimes) {
			String result = httpPostTask(url, "", "swapCollageContentTask");
			page = parse.parseCollagePage(result);
			count++;
		}
		return page;
	}
	
	public TextBlock updateTextBlockTask(TextBlock textBlock){
		String url = mTextBlockURL + "/textblocks/" + textBlock.id;
		JSONObject jsObj = new JSONObject();
		try {
			jsObj.put("Alignment", textBlock.getFontAlignmentIndex(textBlock.alignment));
			jsObj.put("Color", textBlock.color);
			
			JSONObject jsFont = new JSONObject();
			jsFont.put("Name", textBlock.fontName);
			jsFont.put("Size", textBlock.fontSize.equalsIgnoreCase("Auto") ? 0:Float.parseFloat(textBlock.fontSize));
			jsFont.put("SizeMin", textBlock.sizeMin==-1 ? 8:textBlock.sizeMin);
			jsFont.put("SizeMax", textBlock.sizeMax==-1 ? 48:textBlock.sizeMax);
			jsObj.put("Font", jsFont);
			
			jsObj.put("Justification", textBlock.getFontJustificationIndex(textBlock.justification));
			jsObj.put("Language", textBlock.language);
			jsObj.put("Text", textBlock.formatText());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		int count = 0;
		TextBlock tbResult = null;
		while(tbResult==null && count<connTryTimes){
			String result = httpPutTask(url, jsObj.toString(), "updateTextBlockTask");
			tbResult = parse.parseTextBlock(result);
			count ++;
		}
		return tbResult;
	}
	
	public void setCaptionTask(String imageContentId, String text) throws Exception {
		String url = mImageEditingServiceURL + imageContentId + "/caption";
		boolean succeed = false;
		int count = 0;
		JSONObject jsObj = new JSONObject();
		try {
			jsObj.put("Text", text);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		while(!succeed && count<connTryTimes){
			try {
				String result = httpPutTask(url, jsObj.toString(), "setCaptionTask");
				parse.checkError(result);
				succeed = true;
			} catch (Exception e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
	}

}
