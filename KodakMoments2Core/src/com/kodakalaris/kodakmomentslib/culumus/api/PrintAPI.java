package com.kodakalaris.kodakmomentslib.culumus.api;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.kodakalaris.kodakmomentslib.bean.ProductInfo;
import com.kodakalaris.kodakmomentslib.culumus.bean.prints.StandardPrint;
import com.kodakalaris.kodakmomentslib.culumus.parse.PrintParse;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;
import com.kodakalaris.kodakmomentslib.util.DoubleToStringUtil;

public class PrintAPI extends GeneralAPI{
	private PrintParse mPrintParse;
	
	public PrintAPI(Context context) {
		super(context);
		mPrintParse = new PrintParse();
	}
	
	public List<StandardPrint> createStandardPrintsTask(List<ProductInfo> proInfos) throws WebAPIException{
		String url = standardServicePrintsURL;
		JSONArray jsProducts = new JSONArray();
		List<ProductInfo> tempProducts = new ArrayList<ProductInfo>();
		for(ProductInfo product : proInfos){
			if(product.num == 0 || !product.productType.equalsIgnoreCase(ProductInfo.PRO_TYPE_PRINT)){
				continue;
			}
			JSONObject jsObj = new JSONObject();
			try {
				jsObj.put("ProductDescriptionId", product.descriptionId);
				jsObj.put("ImageId", product.correspondId);
				JSONObject jsRoi = new JSONObject();
				jsRoi.put("X", DoubleToStringUtil.formatDouble(product.roi.x, 6));
				jsRoi.put("Y", DoubleToStringUtil.formatDouble(product.roi.y, 6));
				jsRoi.put("W", DoubleToStringUtil.formatDouble(product.roi.w, 6));
				jsRoi.put("H", DoubleToStringUtil.formatDouble(product.roi.h, 6));
				jsRoi.put("ContainerW", DoubleToStringUtil.formatDouble(product.roi.ContainerW, 6));
				jsRoi.put("ContainerH", DoubleToStringUtil.formatDouble(product.roi.ContainerH, 6));
				jsObj.put("ImageROI", jsRoi);
				tempProducts.add(product);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			jsProducts.put(jsObj);
		}
		if(jsProducts.length() == 0){
			return new ArrayList<StandardPrint>();
		}
		List<StandardPrint> standardPrints = null;
		int count = 0;
		while(standardPrints==null && count<connTryTimes){
			try {
				String result = httpPostTask(url, jsProducts.toString(), "createStandardPrintsTask");
				standardPrints = mPrintParse.parseStandardSevicePrints(result);
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		// set productInfo cart item id, need to improve
		if(standardPrints!=null && standardPrints.size()==tempProducts.size()){
			for(int i=0; i<standardPrints.size(); i++){
				tempProducts.get(i).cartItemId = standardPrints.get(i).id;
				standardPrints.get(i).quantity = tempProducts.get(i).num;
			}
		}
		return standardPrints;
		
	}

}
