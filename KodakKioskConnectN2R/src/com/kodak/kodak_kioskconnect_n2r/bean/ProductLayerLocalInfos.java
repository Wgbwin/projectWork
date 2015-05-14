package com.kodak.kodak_kioskconnect_n2r.bean;

import java.io.Serializable;
import java.util.HashMap;

import com.kodak.kodak_kioskconnect_n2r.bean.print.Layer;

public class ProductLayerLocalInfos implements Serializable{
	private static final long serialVersionUID = 1L;
	/**
	 * key is layer content id
	 */
	private HashMap<String, ProductLayerLocalInfo> layerInfos = new HashMap<String, ProductLayerLocalInfo>();
	
	public boolean contains(String layerContentId){
		return layerInfos.containsKey(layerContentId);
	}
	
	public ProductLayerLocalInfo get(String layerContentId){
		return layerInfos.get(layerContentId);
	}
	
	public void put(String layerContentId, ProductLayerLocalInfo info){
		layerInfos.put(layerContentId, info);
	}
	
	public void putIfNotExist(String layerContentId, ProductLayerLocalInfo info){
		if(!contains(layerContentId)){
			put(layerContentId, info);
		}
	}
	
	public void put(Layer layer, boolean needDownload){
		if(layer != null && Layer.TYPE_IMAGE.equals(layer.type)){
			ProductLayerLocalInfo info = new ProductLayerLocalInfo(needDownload, needDownload);
			put(layer.contentId, info);
		}
		
	}
	
	public void putIfNotExist(Layer layer, boolean needDownload){
		if(layer != null && Layer.TYPE_IMAGE.equals(layer.type)){
			if(!contains(layer.contentId)){
				put(layer,needDownload);
			}
		}
	}
	
}
