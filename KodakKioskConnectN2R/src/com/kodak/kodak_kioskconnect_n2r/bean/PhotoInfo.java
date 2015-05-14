package com.kodak.kodak_kioskconnect_n2r.bean;

import java.io.File;
import java.io.Serializable;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore.Images;
import android.text.TextUtils;
import android.util.Log;

import com.AppConstants;
import com.AppConstants.FlowType;
import com.AppConstants.PhotoSource;
import com.AppContext;
import com.kodak.utils.ImageUtil;

/**
 * photo data description
 * @author sunny
 *
 */
public class PhotoInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2383722791571366874L;
	private String photoId ;
	private String photoPath = "" ;
	private String bucketId ;    //for local photo
	private String bucketName ;  //for local photo
	private String localUri = "";
	
	private String thumbnailUrl ; //for photo on net
	private String sourceUrl ;   //for photo on net
	
	private boolean isSelected ;
	private PhotoSource photoSource ;
	
	private String contentId ;
	
	private String productId="" ;
	private String descIdByPro = "";
	
	private FlowType flowType ;
	
	private boolean thumbnailUploaded ;
	
	private int width ;

	private int height ;
	
	private boolean isInsertedForCollagePage ;
	
	public PhotoInfo(){
	
	}
	
	


	public String getPhotoId() {
		return photoId;
	}

	public void setPhotoId(String photoId) {
		this.photoId = photoId;
	}

	public String getPhotoPath() {

		return photoPath;
	}

	public void setPhotoPath(String photoPath) {
		if(!TextUtils.isEmpty(photoPath)){
			if(this.photoSource.isFromFaceBook()){
				String key = ImageUtil.hashKeyForDisk(photoPath);
				String filePath = AppContext.getApplication().getExternalKodakTempPictureWeb().getAbsolutePath()
						+"/"+key+".tmp" ;
				this.photoPath = filePath ;
			}else {
				this.photoPath = photoPath;
			}
		}else {
			this.photoPath = photoPath ;
		}
		
		
	}
	
	public String getBucketId() {
		return bucketId;
	}

	public void setBucketId(String bucketId) {
		this.bucketId = bucketId;
	}

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
	
	public PhotoSource getPhotoSource() {
		return photoSource;
	}

	public void setPhotoSource(PhotoSource photoSource) {
		this.photoSource = photoSource;
	}

	public boolean isPhotoInLocal(){
		
		return this.photoSource == PhotoSource.PHONE ;
		
	}
	public String getLocalUri() {
		return localUri;
	}

	public void setLocalUri(String localUri) {
		this.localUri = localUri;
	}

	
	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public FlowType getFlowType() {
		return flowType;
	}

	public void setFlowType(FlowType flowType) {
		this.flowType = flowType;
	}
	
	public boolean isThumbnailUploaded() {
		return thumbnailUploaded;
	}

	public void setThumbnailUploaded(boolean thumbnailUploaded) {
		this.thumbnailUploaded = thumbnailUploaded;
	}
	public int getWidth() {
		return width;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
	
	public boolean isInsertedForCollagePage() {
		return isInsertedForCollagePage;
	}

	public String getDescIdByPro() {
		return descIdByPro;
	}

	public void setDescIdByPro(String descIdByPro) {
		this.descIdByPro = descIdByPro;
	}




	public void setInsertedForCollagePage(boolean isInsertedForCollagePage) {
		this.isInsertedForCollagePage = isInsertedForCollagePage;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bucketId == null) ? 0 : bucketId.hashCode());
		result = prime * result + ((flowType == null) ? 0 : flowType.hashCode());
		result = prime * result + ((photoId == null) ? 0 : photoId.hashCode());
		result = prime * result + ((productId == null) ? 0 : productId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PhotoInfo other = (PhotoInfo) obj;
		
		if (bucketId == null) {
			if (other.bucketId != null)
				return false;
		} else if (!bucketId.equals(other.bucketId))
			return false;
		if (flowType != other.flowType)
			return false;
		if (photoId == null) {
			if (other.photoId != null)
				return false;
		} else if (!photoId.equals(other.photoId))
			return false;
		if (productId == null) {
			if (other.productId != null)
				return false;
		} else if (!productId.equals(other.productId))
			return false;
		return true;
	}

	
	
	@Override
	public String toString() {
		return "PhotoInfo [photoId=" + photoId + ", photoPath=" + photoPath + ", bucketId=" + bucketId + ", bucketName=" + bucketName + ", localUri="
				+ localUri + ", thumbnailUrl=" + thumbnailUrl + ", sourceUrl=" + sourceUrl + ", isSelected=" + isSelected + ", photoSource="
				+ photoSource + ", contentId=" + contentId + ", productId=" + productId + ", flowType=" + flowType + ", thumbnailUploaded="
				+ thumbnailUploaded + ", width=" + width + ", height=" + height + "]";
	}




	public Bitmap getBitmap(Context context){
		Bitmap bitmap = null ;
		if(this.getPhotoSource().isFromPhone()){
			bitmap = Images.Thumbnails.getThumbnail(
                    context.getContentResolver(), Long.parseLong(this.getPhotoId()) ,
                    Images.Thumbnails.MINI_KIND, null);
		}else if(this.getPhotoSource().isFromFaceBook()){
			File picture = new File(this.getPhotoPath()) ;
			
			if(picture.exists()){
				bitmap = ImageUtil.decodeSampledBitmapFromFile(this.getPhotoPath(),
						AppConstants.THUMNAIL_STANDARD_WIDTH, AppConstants.THUMNAIL_STANDARD_HEIGHT) ;
			}
			
		}
		
		return bitmap ;
		
	}




	
	
	
	
	
	 
	

}
